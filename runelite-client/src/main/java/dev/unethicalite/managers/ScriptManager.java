package dev.unethicalite.managers;

import dev.unethicalite.api.input.Keyboard;
import dev.unethicalite.client.MinimalClient;
import dev.unethicalite.client.config.MinimalConfig;
import dev.unethicalite.client.script.Script;
import dev.unethicalite.client.script.ScriptEntry;
import dev.unethicalite.client.script.ScriptMeta;
import dev.unethicalite.client.script.ScriptThread;
import dev.unethicalite.client.script.events.ScriptChanged;
import dev.unethicalite.client.script.events.ScriptState;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.WorldService;
import net.runelite.client.util.WorldUtil;
import net.runelite.http.api.worlds.World;
import net.runelite.http.api.worlds.WorldResult;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.event.KeyEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.jar.JarFile;

@Singleton
@Slf4j
public class ScriptManager
{
	@Inject
	private MinimalConfig minimalConfig;

	@Inject
	private ExecutorService executorService;

	@Inject
	private Client client;

	@Inject
	private WorldService worldService;

	private String[] args = null;
	private ScriptEntry scriptEntry = null;
	private ScriptThread scriptThread = null;
	private Script script = null;

	private long randomDelay = 0;
	private boolean worldSet;

	public List<ScriptEntry> loadScripts()
	{
		return loadScripts(MinimalClient.SCRIPTS_DIR);
	}

	public List<ScriptEntry> loadScripts(File dir)
	{
		List<ScriptEntry> scripts = new ArrayList<>();

		try
		{
			File[] files = dir.listFiles();
			if (files == null)
			{
				return scripts;
			}
			for (File file : files)
			{
				if (file.isDirectory() || !file.getName().endsWith(".jar"))
				{
					continue;
				}

				JarFile jar = new JarFile(file);
				try (ScriptClassLoader ucl = new ScriptClassLoader(new URL[]{file.toURI().toURL()}))
				{
					var elems = jar.entries();

					while (elems.hasMoreElements())
					{
						var entry = elems.nextElement();
						if (!entry.getName().endsWith(".class"))
						{
							continue;
						}

						String name = entry.getName();
						name = name.substring(0, name.length() - ".class".length())
								.replace('/', '.');

						try
						{
							var clazz = ucl.loadClass(name);
							if (!Script.class.isAssignableFrom(clazz)
									|| Modifier.isAbstract(clazz.getModifiers())
									|| clazz.getAnnotation(ScriptMeta.class) == null)
							{
								continue;
							}

							Class<? extends Script> scriptClass = (Class<? extends Script>) clazz;
							scripts.add(new ScriptEntry(scriptClass, scriptClass.getAnnotationsByType(ScriptMeta.class)[0]));
						}
						catch (Exception | NoClassDefFoundError e)
						{
							log.error("Failed to load class: " + name, e.getMessage());
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return scripts;
	}

	public void startScript(ScriptEntry entry, String... scriptArgs)
	{
		if (scriptThread != null && scriptThread.isAlive())
		{
			return;
		}

		try
		{
			scriptThread = new ScriptThread(entry, scriptArgs);
			scriptThread.start();
			script = scriptThread.getScript();
			args = scriptArgs;
			scriptEntry = entry;
		}
		catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e)
		{
			e.printStackTrace();
		}
	}

	public void stopScript()
	{
		if (scriptThread == null || script == null)
		{
			return;
		}

		while (scriptThread.isAlive() || script.isRunning())
		{
			script.stopLooping();
		}

		script = null;
		scriptThread = null;
	}

	public void restartScript()
	{
		while (scriptThread != null)
		{
			stopScript();
		}

		if (args != null && scriptEntry != null)
		{
			ScriptEntry reloaded = loadScripts().stream()
					.filter(x -> x.getMeta().equals(scriptEntry.getMeta()))
					.findFirst()
					.orElse(null);
			startScript(reloaded, args);
		}
	}

	public void pauseScript()
	{
		if (script == null)
		{
			return;
		}

		script.pauseScript();
	}

	public boolean isRunning()
	{
		return scriptThread != null && scriptThread.isAlive() &&
				script != null && script.isRunning() && !script.isPaused();
	}

	public Script getBotScript()
	{
		return script;
	}

	@Subscribe
	private void onScriptChanged(ScriptChanged e)
	{
		log.info("Script state changed: {} [{}]", e.getName(), e.getState());

		if (e.getState() == ScriptState.RESTARTING)
		{
			executorService.execute(this::restartScript);
		}
	}

	@Subscribe
	private void onGameTick(GameTick e)
	{
		if (minimalConfig.neverLog() && checkIdle())
		{
			randomDelay = randomDelay();
			executorService.submit(this::pressKey);
		}
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged e)
	{
		if (worldSet || e.getGameState() != GameState.LOGIN_SCREEN)
		{
			return;
		}

		Optional<Integer> worldArg = Optional.ofNullable(System.getProperty("cli.world")).map(Integer::parseInt);
		worldArg.ifPresent(this::setWorld);
		worldSet = true;
	}

	private boolean checkIdle()
	{
		int idleClientTicks = client.getKeyboardIdleTicks();
		if (client.getMouseIdleTicks() < idleClientTicks)
		{
			idleClientTicks = client.getMouseIdleTicks();
		}

		return idleClientTicks >= randomDelay;
	}

	private long randomDelay()
	{
		return (long) clamp(Math.round(ThreadLocalRandom.current().nextGaussian() * 8000));
	}

	private double clamp(double value)
	{
		return Math.max(1.0, Math.min(13000.0, value));
	}

	private void pressKey()
	{
		Keyboard.pressed(KeyEvent.VK_UP);
		Keyboard.released(KeyEvent.VK_UP);
	}

	private void setWorld(int cliWorld)
	{
		int correctedWorld = cliWorld < 300 ? cliWorld + 300 : cliWorld;

		if (correctedWorld <= 300 || client.getWorld() == correctedWorld)
		{
			return;
		}

		final WorldResult worldResult = worldService.getWorlds();

		if (worldResult == null)
		{
			log.warn("Failed to lookup worlds.");
			return;
		}

		final World world = worldResult.findWorld(correctedWorld);

		if (world != null)
		{
			final net.runelite.api.World rsWorld = client.createWorld();
			rsWorld.setActivity(world.getActivity());
			rsWorld.setAddress(world.getAddress());
			rsWorld.setId(world.getId());
			rsWorld.setPlayerCount(world.getPlayers());
			rsWorld.setLocation(world.getLocation());
			rsWorld.setTypes(WorldUtil.toWorldTypes(world.getTypes()));

			client.changeWorld(rsWorld);
			log.debug("Applied new world {}", correctedWorld);
		}
		else
		{
			log.warn("World {} not found.", correctedWorld);
		}
	}
}

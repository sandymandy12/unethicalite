package dev.unethicalite.managers;

import dev.unethicalite.api.plugins.LoopedPlugin;
import dev.unethicalite.api.plugins.Script;
import dev.unethicalite.api.plugins.SettingsPlugin;
import dev.unethicalite.api.plugins.Task;
import dev.unethicalite.api.plugins.TaskPlugin;
import dev.unethicalite.client.Static;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.PluginChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.config.PluginConfigurationDescriptor;
import net.runelite.client.plugins.config.PluginListPanel;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.concurrent.Executors;

@Singleton
@Slf4j
public class LoopedPluginManager
{
	private final EventBus eventBus;

	@Inject
	private ConfigManager configManager;

	@Inject
	private Provider<PluginListPanel> pluginListPanelProvider;

	@Inject
	private Client client;

	private LoopedPlugin loopedPlugin = null;
	private Thread currentLoop = null;

	@Inject
	LoopedPluginManager(EventBus eventBus)
	{
		this.eventBus = eventBus;
		eventBus.register(this);
	}

	public void unregister()
	{
		if (currentLoop == null || loopedPlugin == null)
		{
			return;
		}

		while (currentLoop.isAlive() || loopedPlugin.isRunning())
		{
			loopedPlugin.stop();
		}

		if (loopedPlugin instanceof Script)
		{
			Script script = (Script) loopedPlugin;
			script.onStop();
			script.getPaint().clear();
		}

		if (loopedPlugin instanceof TaskPlugin)
		{
			for (Task task : ((TaskPlugin) loopedPlugin).getTasks())
			{
				if (task.subscribe())
				{
					eventBus.unregister(task);
				}
			}
		}

		currentLoop = null;
		loopedPlugin = null;

		client.setQueuedMenu(null);
	}

	public void submit(Plugin plugin)
	{
		if (!(plugin instanceof LoopedPlugin))
		{
			log.error("Only LoopedPlugins may be submitted to the LoopedPluginManager");
			return;
		}

		if (currentLoop != null && currentLoop.isAlive())
		{
			log.debug("A plugin is already running, stopping it");
			Executors.newSingleThreadExecutor().execute(this::unregister);
		}

		log.debug("Registering {} as a LoopedPlugin", plugin.getClass().getSimpleName());

		loopedPlugin = (LoopedPlugin) plugin;

		if (loopedPlugin instanceof TaskPlugin)
		{
			for (Task task : ((TaskPlugin) loopedPlugin).getTasks())
			{
				if (task.subscribe())
				{
					eventBus.register(task);
				}
			}
		}

		if (plugin instanceof Script)
		{
			Script script = (Script) plugin;
			script.getPaint().clear();
			script.onStart(Static.getScriptArgs());
		}

		currentLoop = new Thread(loopedPlugin);
		currentLoop.start();
	}

	public boolean isPluginRegistered()
	{
		return currentLoop != null && currentLoop.isAlive() && loopedPlugin != null && loopedPlugin.isRunning();
	}

	@Subscribe
	private void onPluginChanged(PluginChanged pluginChanged)
	{
		Plugin plugin = pluginChanged.getPlugin();
		if (pluginChanged.isLoaded())
		{
			if (plugin instanceof SettingsPlugin)
			{
				SettingsPlugin settingsPlugin = (SettingsPlugin) plugin;
				PluginListPanel pluginListPanel = pluginListPanelProvider.get();
				pluginListPanel.addFakePlugin(
						new PluginConfigurationDescriptor(
								settingsPlugin.getPluginName(),
								settingsPlugin.getPluginDescription(),
								settingsPlugin.getPluginTags(),
								settingsPlugin.getConfig(),
								configManager.getConfigDescriptor(settingsPlugin.getConfig())
						)
				);
				pluginListPanel.rebuildPluginList();
				return;
			}

			if (plugin instanceof LoopedPlugin)
			{
				submit(plugin);
			}
		}
		else
		{
			if (plugin instanceof LoopedPlugin && plugin == this.loopedPlugin)
			{
				unregister();
			}
		}
	}
}

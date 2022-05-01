package dev.unethicalite.api.plugins;

import dev.unethicalite.client.script.Task;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.PluginChanged;

import javax.inject.Inject;

public abstract class TaskPlugin extends LoopedPlugin
{
	protected abstract Task[] getTasks();

	@Inject
	private EventBus eventBus;

	@Override
	protected int loop()
	{
		for (Task task : getTasks())
		{
			if (task.validate())
			{
				int delay = task.execute();
				if (task.isBlocking())
				{
					return delay;
				}
			}
		}

		return 1000;
	}

	@Subscribe
	public void onPluginChanged(PluginChanged pluginChanged)
	{
		if (pluginChanged.getPlugin() != this)
		{
			return;
		}

		if (pluginChanged.isLoaded())
		{
			for (Task task : getTasks())
			{
				if (task.subscribe())
				{
					eventBus.register(task);
				}
			}
		}
		else
		{
			for (Task task : getTasks())
			{
				if (task.subscribe())
				{
					eventBus.unregister(task);
				}
			}
		}
	}
}

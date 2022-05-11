package dev.unethicalite.client.devtools.scriptinspector;

import com.google.common.collect.Lists;
import dev.unethicalite.client.devtools.widgetinspector.WidgetInspector;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.util.Text;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.FontManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import static net.runelite.api.widgets.WidgetInfo.TO_CHILD;
import static net.runelite.api.widgets.WidgetInfo.TO_GROUP;

@Singleton
@Slf4j
public class ScriptInspector
{
	// These scripts are the only ones that fire every client tick regardless of location.
	private final static String DEFAULT_BLACKLIST = "3174,1004";
	private final static int MAX_LOG_ENTRIES = 10000;

	private final Client client;
	private final EventBus eventBus;
	private final ConfigManager configManager;

	private final JPanel tracker = new JPanel();
	private ScriptTreeNode currentNode;
	private int lastTick;
	private Set<Integer> blacklist;
	private Set<Integer> highlights;
	private JList jList;
	private DefaultListModel listModel;
	private ListState state = ListState.BLACKLIST;
	private JFrame frame;

	private enum ListState
	{
		BLACKLIST,
		HIGHLIGHT
	}

	@Data
	@EqualsAndHashCode(callSuper = true)
	private class ScriptTreeNode extends DefaultMutableTreeNode
	{
		private final int scriptId;
		private Widget source;
		private int duplicateNumber = 1;

		@Override
		public String toString()
		{
			String output = Integer.toString(scriptId);

			if (duplicateNumber != 1)
			{
				output += " (" + duplicateNumber + ")";
			}

			if (source != null)
			{
				int id = source.getId();
				output += "  -  " + TO_GROUP(id) + "." + TO_CHILD(id);

				if (source.getIndex() != -1)
				{
					output += "[" + source.getIndex() + "]";
				}

				WidgetInfo info = WidgetInspector.getWidgetInfo(id);
				if (info != null)
				{
					output += " " + info.name();
				}
			}

			return output;
		}
	}

	@Inject
	ScriptInspector(Client client, EventBus eventBus, ConfigManager configManager)
	{
		this.eventBus = eventBus;
		this.client = client;
		this.configManager = configManager;
	}

	@Subscribe
	public void onScriptPreFired(ScriptPreFired event)
	{
		ScriptTreeNode newNode = new ScriptTreeNode(event.getScriptId());
		if (event.getScriptEvent() != null)
		{
			newNode.setSource(event.getScriptEvent().getSource());
		}

		if (currentNode == null)
		{
			currentNode = newNode;
		}
		else
		{
			int count = 0;
			Enumeration children = currentNode.children();
			if (children != null)
			{
				while (children.hasMoreElements())
				{
					ScriptTreeNode child = (ScriptTreeNode) children.nextElement();

					if (child.getScriptId() == event.getScriptId())
					{
						count++;
					}
				}

				newNode.setDuplicateNumber(count + 1);
			}

			currentNode.add(newNode);
			currentNode = newNode;
		}
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event)
	{
		if (currentNode == null || currentNode.getScriptId() != event.getScriptId())
		{
			log.warn("a script was post-fired that was never pre-fired. Script id: " + event.getScriptId());
			return;
		}

		if (currentNode.getParent() != null)
		{
			currentNode = (ScriptTreeNode) currentNode.getParent();
		}
		else
		{
			addScriptLog(currentNode);
			currentNode = null;
		}
	}

	public void open()
	{
		if (frame != null && frame.isVisible())
		{
			close();
			return;
		}

		if (frame == null)
		{
			frame = new JFrame();
			frame.setTitle("Script Inspector");

			frame.setLayout(new BorderLayout());

			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			frame.addWindowListener(new WindowAdapter()
			{
				@Override
				public void windowClosing(WindowEvent e)
				{
					close();
				}
			});

			tracker.setLayout(new DynamicGridLayout(0, 1, 0, 3));

			final JPanel leftSide = new JPanel();
			leftSide.setLayout(new BorderLayout());

			final JPanel trackerWrapper = new JPanel();
			trackerWrapper.setLayout(new BorderLayout());
			trackerWrapper.add(tracker, BorderLayout.NORTH);

			final JScrollPane trackerScroller = new JScrollPane(trackerWrapper);
			trackerScroller.setPreferredSize(new Dimension(400, 400));

			final JScrollBar vertical = trackerScroller.getVerticalScrollBar();
			vertical.addAdjustmentListener(new AdjustmentListener()
			{
				int lastMaximum = actualMax();

				private int actualMax()
				{
					return vertical.getMaximum() - vertical.getModel().getExtent();
				}

				@Override
				public void adjustmentValueChanged(AdjustmentEvent e)
				{
					if (vertical.getValue() >= lastMaximum)
					{
						vertical.setValue(actualMax());
					}
					lastMaximum = actualMax();
				}
			});

			leftSide.add(trackerScroller, BorderLayout.CENTER);

			final JPanel bottomLeftRow = new JPanel();
			final JButton clearBtn = new JButton("Clear");
			clearBtn.addActionListener(e ->
			{
				tracker.removeAll();
				tracker.revalidate();
			});

			bottomLeftRow.add(clearBtn);
			leftSide.add(bottomLeftRow, BorderLayout.SOUTH);
			frame.add(leftSide, BorderLayout.CENTER);

			String blacklistConfig = configManager.getConfiguration("unethicalite", "devtools.blacklist");

			if (blacklistConfig == null)
			{
				blacklistConfig = DEFAULT_BLACKLIST;
			}

			try
			{
				blacklist = new HashSet<>(Lists.transform(Text.fromCSV(blacklistConfig), Integer::parseInt));
			}
			catch (NumberFormatException e)
			{
				blacklist = new HashSet<>(Lists.transform(Text.fromCSV(DEFAULT_BLACKLIST), Integer::parseInt));
			}

			String highlightsConfig = configManager.getConfiguration("unethicalite", "devtools.highlights");

			if (highlightsConfig == null)
			{
				highlightsConfig = "";
			}

			try
			{
				highlights = new HashSet<>(Lists.transform(Text.fromCSV(highlightsConfig), Integer::parseInt));
			}
			catch (NumberFormatException e)
			{
				blacklist = new HashSet<>();
			}

			final JPanel rightSide = new JPanel();
			rightSide.setLayout(new BorderLayout());

			listModel = new DefaultListModel();
			changeState(ListState.BLACKLIST);
			jList = new JList(listModel);
			jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			JScrollPane listScrollPane = new JScrollPane(jList);

			final JButton blacklistButton = new JButton("Blacklist");
			blacklistButton.addActionListener(e -> changeState(ListState.BLACKLIST));

			final JButton highlightsButton = new JButton("Highlights");
			highlightsButton.addActionListener(e -> changeState(ListState.HIGHLIGHT));

			final JPanel topLeftRow = new JPanel();
			topLeftRow.setLayout(new FlowLayout());
			topLeftRow.add(blacklistButton);
			topLeftRow.add(highlightsButton);

			rightSide.add(topLeftRow, BorderLayout.NORTH);
			rightSide.add(listScrollPane, BorderLayout.CENTER);

			final JSpinner jSpinner = new JSpinner();
			Component mySpinnerEditor = jSpinner.getEditor();
			JFormattedTextField textField = ((JSpinner.DefaultEditor) mySpinnerEditor).getTextField();
			textField.setColumns(5);

			final JButton addButton = new JButton("Add");
			addButton.addActionListener(e -> addToSet(jSpinner));

			final JButton removeButton = new JButton("Remove");
			removeButton.addActionListener(e -> removeSelectedFromSet());

			final JPanel bottomButtonRow = new JPanel();
			bottomButtonRow.setLayout(new FlowLayout());
			bottomButtonRow.add(addButton);
			bottomButtonRow.add(jSpinner);
			bottomButtonRow.add(removeButton);

			rightSide.add(bottomButtonRow, BorderLayout.SOUTH);

			frame.add(rightSide, BorderLayout.EAST);

			frame.pack();
		}

		eventBus.register(this);
		frame.setVisible(true);
		frame.toFront();
		frame.repaint();
	}

	public void close()
	{
		configManager.setConfiguration("unethicalite", "devtools.highlights",
				Text.toCSV(Lists.transform(new ArrayList<>(highlights), String::valueOf)));
		configManager.setConfiguration("unethicalite", "devtools.blacklist",
				Text.toCSV(Lists.transform(new ArrayList<>(blacklist), String::valueOf)));
		currentNode = null;
		eventBus.unregister(this);
		frame.setVisible(false);
	}

	private void addScriptLog(ScriptTreeNode treeNode)
	{
		if (blacklist.contains(treeNode.getScriptId()))
		{
			return;
		}

		int tick = client.getTickCount();
		SwingUtilities.invokeLater(() ->
		{
			if (tick != lastTick)
			{
				lastTick = tick;
				JLabel header = new JLabel("Tick " + tick);
				header.setFont(FontManager.getRunescapeSmallFont());
				header.setBorder(new CompoundBorder(
						BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.LIGHT_GRAY_COLOR),
						BorderFactory.createEmptyBorder(3, 6, 0, 0)
				));
				tracker.add(header);
			}
			DefaultTreeModel treeModel = new DefaultTreeModel(treeNode);
			JTree tree = new JTree(treeModel);
			tree.setRootVisible(true);
			tree.setShowsRootHandles(true);
			tree.collapsePath(new TreePath(treeNode));

			ScriptTreeNode highlightNode = findHighlightPathNode(treeNode);

			if (highlightNode != null)
			{
				tree.setExpandsSelectedPaths(true);
				tree.setSelectionPath(new TreePath(treeModel.getPathToRoot(highlightNode)));
			}

			tracker.add(tree);

			// Cull very old stuff
			for (; tracker.getComponentCount() > MAX_LOG_ENTRIES; )
			{
				tracker.remove(0);
			}

			tracker.revalidate();
		});
	}

	private void changeState(ListState state)
	{
		this.state = state;
		refreshList();
	}

	private void addToSet(JSpinner spinner)
	{
		int script = (Integer) spinner.getValue();
		Set<Integer> set = getSet();
		set.add(script);
		refreshList();
		spinner.setValue(0);
	}

	private void removeSelectedFromSet()
	{
		int index = jList.getSelectedIndex();

		if (index == -1)
		{
			return;
		}

		int script = (Integer) listModel.get(index);
		getSet().remove(script);
		refreshList();
	}

	private void refreshList()
	{
		listModel.clear();
		Set<Integer> set = getSet();

		for (Integer i : set)
		{
			listModel.addElement(i);
		}
	}

	private Set<Integer> getSet()
	{
		Set<Integer> set;

		if (state == ListState.BLACKLIST)
		{
			set = blacklist;
		}
		else
		{
			set = highlights;
		}

		return set;
	}

	private ScriptTreeNode findHighlightPathNode(ScriptTreeNode node)
	{
		if (highlights.contains(node.getScriptId()))
		{
			return node;
		}

		Enumeration children = node.children();
		if (children != null)
		{
			while (children.hasMoreElements())
			{
				ScriptTreeNode child = (ScriptTreeNode) children.nextElement();

				ScriptTreeNode find = findHighlightPathNode(child);

				if (find != null)
				{
					return find;
				}
			}
		}

		return null;
	}
}



PLUGINS_DIR = "runelite-client/src/main/java/net/runelite/client/plugins"
WH_SWITCHER_DEFAULT= """\
if (previous)
			{
				worldIdx--;

				if (worldIdx < 0)
				{
					worldIdx = worlds.size() - 1;
				}
			}
			else
			{
				worldIdx++;

				if (worldIdx >= worlds.size())
				{
					worldIdx = 0;
				}
			}"""
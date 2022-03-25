import os

from constants import PLUGINS_DIR

def main():
    wh_plugin_file = os.path.join(os.getcwd(), PLUGINS_DIR, "worldhopper/WorldHopperPlugin.java")

    replacement="java.util.concurrent.ThreadLocalRandom"

main()
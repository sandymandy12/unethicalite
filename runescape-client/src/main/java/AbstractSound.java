import net.runelite.mapping.ObfuscatedName;
import net.runelite.mapping.Implements;
import net.runelite.mapping.Export;
@ObfuscatedName("bv")
@Implements("AbstractSound")
public abstract class AbstractSound extends Node {
	@ObfuscatedName("q")
	@Export("position")
	int position;

	AbstractSound() {
	}
}
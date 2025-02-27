import java.util.LinkedList;
import net.runelite.mapping.ObfuscatedName;
import net.runelite.mapping.ObfuscatedSignature;
import net.runelite.mapping.Implements;
import java.util.List;
import java.util.HashSet;
import net.runelite.mapping.Export;
@ObfuscatedName("ir")
@Implements("WorldMapAreaData")
public class WorldMapAreaData extends WorldMapArea {
	@ObfuscatedName("m")
	@Export("worldMapData0Set")
	HashSet worldMapData0Set;

	@ObfuscatedName("u")
	@Export("worldMapData1Set")
	HashSet worldMapData1Set;

	@ObfuscatedName("l")
	@Export("iconList")
	List iconList;

	WorldMapAreaData() {
	}

	@ObfuscatedName("bf")
	@ObfuscatedSignature(descriptor = "(Lqr;Lqr;IZI)V", garbageValue = "255529643")
	@Export("init")
	void init(Buffer var1, Buffer var2, int var3, boolean var4) {
		this.read(var1, var3);
		int var5 = var2.readUnsignedShort();
		this.worldMapData0Set = new HashSet(var5);
		int var6;
		for (var6 = 0; var6 < var5; ++var6) {
			WorldMapData_0 var7 = new WorldMapData_0();
			try {
				var7.init(var2);
			} catch (IllegalStateException var12) {
				continue;
			}
			this.worldMapData0Set.add(var7);
		}
		var6 = var2.readUnsignedShort();
		this.worldMapData1Set = new HashSet(var6);
		for (int var10 = 0; var10 < var6; ++var10) {
			WorldMapData_1 var8 = new WorldMapData_1();
			try {
				var8.init(var2);
			} catch (IllegalStateException var11) {
				continue;
			}
			this.worldMapData1Set.add(var8);
		}
		this.initIconsList(var2, var4);
	}

	@ObfuscatedName("ce")
	@ObfuscatedSignature(descriptor = "(Lqr;ZB)V", garbageValue = "-86")
	@Export("initIconsList")
	void initIconsList(Buffer var1, boolean var2) {
		this.iconList = new LinkedList();
		int var3 = var1.readUnsignedShort();
		for (int var4 = 0; var4 < var3; ++var4) {
			int var5 = var1.readNullableLargeSmart();
			Coord var6 = new Coord(var1.readInt());
			boolean var7 = var1.readUnsignedByte() == 1;
			if (var2 || !var7) {
				this.iconList.add(new WorldMapIcon_0(((Coord) (null)), var6, var5, ((WorldMapLabel) (null))));
			}
		}
	}
}
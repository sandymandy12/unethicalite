import net.runelite.mapping.ObfuscatedName;
import java.util.HashMap;
import net.runelite.mapping.ObfuscatedSignature;
import net.runelite.mapping.Export;
@ObfuscatedName("lq")
public class class329 {
	@ObfuscatedName("i")
	@ObfuscatedSignature(descriptor = "Lqe;")
	static IndexedSprite field4091;

	static {
		new HashMap();
	}

	@ObfuscatedName("hc")
	@ObfuscatedSignature(descriptor = "(Lcq;IIIB)V", garbageValue = "-125")
	@Export("addPlayerToMenu")
	static final void addPlayerToMenu(Player var0, int var1, int var2, int var3) {
		if (ScriptFrame.localPlayer != var0) {
			if (Client.menuOptionsCount < 400) {
				String var4;
				int var7;
				if (var0.skillLevel == 0) {
					String var5 = var0.actions[0] + var0.username + var0.actions[1];
					var7 = var0.combatLevel;
					int var8 = ScriptFrame.localPlayer.combatLevel;
					int var9 = var8 - var7;
					String var6;
					if (var9 < -9) {
						var6 = Clock.colorStartTag(16711680);
					} else if (var9 < -6) {
						var6 = Clock.colorStartTag(16723968);
					} else if (var9 < -3) {
						var6 = Clock.colorStartTag(16740352);
					} else if (var9 < 0) {
						var6 = Clock.colorStartTag(16756736);
					} else if (var9 > 9) {
						var6 = Clock.colorStartTag(65280);
					} else if (var9 > 6) {
						var6 = Clock.colorStartTag(4259584);
					} else if (var9 > 3) {
						var6 = Clock.colorStartTag(8453888);
					} else if (var9 > 0) {
						var6 = Clock.colorStartTag(12648192);
					} else {
						var6 = Clock.colorStartTag(16776960);
					}
					var4 = var5 + var6 + " " + " (" + "level-" + var0.combatLevel + ")" + var0.actions[2];
				} else {
					var4 = var0.actions[0] + var0.username + var0.actions[1] + " " + " (" + "skill-" + var0.skillLevel + ")" + var0.actions[2];
				}
				int var10;
				if (Client.isItemSelected == 1) {
					ChatChannel.insertMenuItemNoShift("Use", Client.selectedItemName + " " + "->" + " " + Clock.colorStartTag(16777215) + var4, 14, var1, var2, var3);
				} else if (Client.isSpellSelected) {
					if ((class149.selectedSpellFlags & 8) == 8) {
						ChatChannel.insertMenuItemNoShift(Client.selectedSpellActionName, Client.selectedSpellName + " " + "->" + " " + Clock.colorStartTag(16777215) + var4, 15, var1, var2, var3);
					}
				} else {
					for (var10 = 7; var10 >= 0; --var10) {
						if (Client.playerMenuActions[var10] != null) {
							short var11 = 0;
							if (Client.playerMenuActions[var10].equalsIgnoreCase("Attack")) {
								if (Client.playerAttackOption == AttackOption.AttackOption_hidden) {
									continue;
								}
								if (AttackOption.AttackOption_alwaysRightClick == Client.playerAttackOption || AttackOption.AttackOption_dependsOnCombatLevels == Client.playerAttackOption && var0.combatLevel > ScriptFrame.localPlayer.combatLevel) {
									var11 = 2000;
								}
								if (ScriptFrame.localPlayer.team != 0 && var0.team != 0) {
									if (var0.team == ScriptFrame.localPlayer.team) {
										var11 = 2000;
									} else {
										var11 = 0;
									}
								} else if (AttackOption.field1287 == Client.playerAttackOption && var0.isClanMember()) {
									var11 = 2000;
								}
							} else if (Client.playerOptionsPriorities[var10]) {
								var11 = 2000;
							}
							boolean var12 = false;
							var7 = Client.playerMenuOpcodes[var10] + var11;
							ChatChannel.insertMenuItemNoShift(Client.playerMenuActions[var10], Clock.colorStartTag(16777215) + var4, var7, var1, var2, var3);
						}
					}
				}
				for (var10 = 0; var10 < Client.menuOptionsCount; ++var10) {
					if (Client.menuOpcodes[var10] == 23) {
						Client.menuTargets[var10] = Clock.colorStartTag(16777215) + var4;
						break;
					}
				}
			}
		}
	}
}
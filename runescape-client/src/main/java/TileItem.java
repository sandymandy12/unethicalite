import net.runelite.mapping.ObfuscatedName;
import net.runelite.mapping.ObfuscatedSignature;
import net.runelite.mapping.ObfuscatedGetter;
import net.runelite.mapping.Implements;
import net.runelite.mapping.Export;
@ObfuscatedName("cy")
@Implements("TileItem")
public final class TileItem extends Renderable {
	@ObfuscatedName("s")
	@ObfuscatedGetter(intValue = 504973657)
	@Export("id")
	int id;

	@ObfuscatedName("h")
	@ObfuscatedGetter(intValue = -1383840147)
	@Export("quantity")
	int quantity;

	TileItem() {
	}

	@ObfuscatedName("h")
	@ObfuscatedSignature(descriptor = "(B)Lhp;", garbageValue = "4")
	@Export("getModel")
	protected final Model getModel() {
		return AttackOption.ItemDefinition_get(this.id).getModel(this.quantity);
	}

	@ObfuscatedName("s")
	@ObfuscatedSignature(descriptor = "(Lqz;B)V", garbageValue = "67")
	@Export("updatePlayer")
	static final void updatePlayer(PacketBuffer var0) {
		var0.importIndex();
		int var1 = Client.localPlayerIndex;
		Player var2 = ScriptFrame.localPlayer = Client.players[var1] = new Player();
		var2.index = var1;
		int var3 = var0.readBits(30);
		byte var4 = ((byte) (var3 >> 28));
		int var5 = var3 >> 14 & 16383;
		int var6 = var3 & 16383;
		var2.pathX[0] = var5 - Decimator.baseX * 8;
		var2.x = (var2.pathX[0] << 7) + (var2.transformedSize() << 6);
		var2.pathY[0] = var6 - class7.baseY * 64;
		var2.y = (var2.pathY[0] << 7) + (var2.transformedSize() << 6);
		class268.Client_plane = var2.plane = var4;
		if (Players.field1308[var1] != null) {
			var2.read(Players.field1308[var1]);
		}
		Players.Players_count = 0;
		Players.Players_indices[++Players.Players_count - 1] = var1;
		Players.field1297[var1] = 0;
		Players.Players_emptyIdxCount = 0;
		for (int var7 = 1; var7 < 2048; ++var7) {
			if (var7 != var1) {
				int var8 = var0.readBits(18);
				int var9 = var8 >> 16;
				int var10 = var8 >> 8 & 597;
				int var11 = var8 & 597;
				Players.Players_regions[var7] = (var10 << 14) + var11 + (var9 << 28);
				Players.Players_orientations[var7] = 0;
				Players.Players_targetIndices[var7] = -1;
				Players.Players_emptyIndices[++Players.Players_emptyIdxCount - 1] = var7;
				Players.field1297[var7] = 0;
			}
		}
		var0.exportIndex();
	}

	@ObfuscatedName("gj")
	@ObfuscatedSignature(descriptor = "(Lcx;IIIIIB)V", garbageValue = "75")
	@Export("drawActor2d")
	static final void drawActor2d(Actor var0, int var1, int var2, int var3, int var4, int var5) {
		if (var0 != null && var0.isVisible()) {
			if (var0 instanceof NPC) {
				NPCComposition var6 = ((NPC) (var0)).definition;
				if (var6.transforms != null) {
					var6 = var6.transform();
				}
				if (var6 == null) {
					return;
				}
			}
			int var75 = Players.Players_count;
			int[] var7 = Players.Players_indices;
			byte var8 = 0;
			if (var1 < var75 && var0.playerCycle == Client.cycle && CollisionMap.method3794(((Player) (var0)))) {
				Player var9 = ((Player) (var0));
				if (var1 < var75) {
					SoundCache.method757(var0, var0.defaultHeight + 15);
					AbstractFont var10 = ((AbstractFont) (Client.fontsMap.get(FontName.FontName_plain12)));
					byte var11 = 9;
					var10.drawCentered(var9.username.getName(), var2 + Client.viewportTempX, var3 + Client.viewportTempY - var11, 16777215, 0);
					var8 = 18;
				}
			}
			int var76 = -2;
			int var15;
			int var22;
			int var23;
			if (!var0.healthBars.method6073()) {
				SoundCache.method757(var0, var0.defaultHeight + 15);
				for (HealthBar var87 = ((HealthBar) (var0.healthBars.last())); var87 != null; var87 = ((HealthBar) (var0.healthBars.previous()))) {
					HealthBarUpdate var79 = var87.get(Client.cycle);
					if (var79 == null) {
						if (var87.isEmpty()) {
							var87.remove();
						}
					} else {
						HealthBarDefinition var80 = var87.definition;
						SpritePixels var13 = var80.getBackSprite();
						SpritePixels var81 = var80.getFrontSprite();
						int var82 = 0;
						if (var13 != null && var81 != null) {
							if (var80.widthPadding * 2 < var81.subWidth) {
								var82 = var80.widthPadding;
							}
							var15 = var81.subWidth - var82 * 2;
						} else {
							var15 = var80.width;
						}
						int var17 = 255;
						boolean var83 = true;
						int var84 = Client.cycle - var79.cycle;
						int var85 = var15 * var79.health2 / var80.width;
						int var86;
						int var94;
						if (var79.cycleOffset > var84) {
							var86 = (var80.field1867 == 0) ? 0 : var80.field1867 * (var84 / var80.field1867);
							var22 = var15 * var79.health / var80.width;
							var94 = var86 * (var85 - var22) / var79.cycleOffset + var22;
						} else {
							var94 = var85;
							var86 = var79.cycleOffset + var80.int5 - var84;
							if (var80.int3 >= 0) {
								var17 = (var86 << 8) / (var80.int5 - var80.int3);
							}
						}
						if (var79.health2 > 0 && var94 < 1) {
							var94 = 1;
						}
						if (var13 != null && var81 != null) {
							if (var15 == var94) {
								var94 += var82 * 2;
							} else {
								var94 += var82;
							}
							var86 = var13.subHeight;
							var76 += var86;
							var22 = var2 + Client.viewportTempX - (var15 >> 1);
							var23 = var3 + Client.viewportTempY - var76;
							var22 -= var82;
							if (var17 >= 0 && var17 < 255) {
								var13.drawTransAt(var22, var23, var17);
								Rasterizer2D.Rasterizer2D_expandClip(var22, var23, var22 + var94, var23 + var86);
								var81.drawTransAt(var22, var23, var17);
							} else {
								var13.drawTransBgAt(var22, var23);
								Rasterizer2D.Rasterizer2D_expandClip(var22, var23, var94 + var22, var23 + var86);
								var81.drawTransBgAt(var22, var23);
							}
							Rasterizer2D.Rasterizer2D_setClip(var2, var3, var2 + var4, var3 + var5);
							var76 += 2;
						} else {
							var76 += 5;
							if (Client.viewportTempX > -1) {
								var86 = var2 + Client.viewportTempX - (var15 >> 1);
								var22 = var3 + Client.viewportTempY - var76;
								Rasterizer2D.Rasterizer2D_fillRectangle(var86, var22, var94, 5, 65280);
								Rasterizer2D.Rasterizer2D_fillRectangle(var94 + var86, var22, var15 - var94, 5, 16711680);
							}
							var76 += 2;
						}
					}
				}
			}
			if (var76 == -2) {
				var76 += 7;
			}
			var76 += var8;
			if (var1 < var75) {
				Player var88 = ((Player) (var0));
				if (var88.isHidden) {
					return;
				}
				if (var88.headIconPk != -1 || var88.headIconPrayer != -1) {
					SoundCache.method757(var0, var0.defaultHeight + 15);
					if (Client.viewportTempX > -1) {
						if (var88.headIconPk != -1) {
							var76 += 25;
							BufferedSource.headIconPkSprites[var88.headIconPk].drawTransBgAt(var2 + Client.viewportTempX - 12, var3 + Client.viewportTempY - var76);
						}
						if (var88.headIconPrayer != -1) {
							var76 += 25;
							MusicPatchNode.headIconPrayerSprites[var88.headIconPrayer].drawTransBgAt(var2 + Client.viewportTempX - 12, var3 + Client.viewportTempY - var76);
						}
					}
				}
				if (var1 >= 0 && Client.hintArrowType == 10 && var7[var1] == Client.hintArrowPlayerIndex) {
					SoundCache.method757(var0, var0.defaultHeight + 15);
					if (Client.viewportTempX > -1) {
						var76 += class229.headIconHintSprites[1].subHeight;
						class229.headIconHintSprites[1].drawTransBgAt(var2 + Client.viewportTempX - 12, var3 + Client.viewportTempY - var76);
					}
				}
			} else {
				NPCComposition var89 = ((NPC) (var0)).definition;
				if (var89.transforms != null) {
					var89 = var89.transform();
				}
				if (var89.headIconPrayer >= 0 && var89.headIconPrayer < MusicPatchNode.headIconPrayerSprites.length) {
					SoundCache.method757(var0, var0.defaultHeight + 15);
					if (Client.viewportTempX > -1) {
						MusicPatchNode.headIconPrayerSprites[var89.headIconPrayer].drawTransBgAt(var2 + Client.viewportTempX - 12, var3 + Client.viewportTempY - 30);
					}
				}
				if (Client.hintArrowType == 1 && Client.npcIndices[var1 - var75] == Client.hintArrowNpcIndex && Client.cycle % 20 < 10) {
					SoundCache.method757(var0, var0.defaultHeight + 15);
					if (Client.viewportTempX > -1) {
						class229.headIconHintSprites[0].drawTransBgAt(var2 + Client.viewportTempX - 12, var3 + Client.viewportTempY - 28);
					}
				}
			}
			if (var0.overheadText != null && (var1 >= var75 || !var0.field1164 && (Client.publicChatMode == 4 || !var0.isAutoChatting && (Client.publicChatMode == 0 || Client.publicChatMode == 3 || Client.publicChatMode == 1 && ((Player) (var0)).isFriend())))) {
				SoundCache.method757(var0, var0.defaultHeight);
				if (Client.viewportTempX > -1 && Client.overheadTextCount < Client.overheadTextLimit) {
					Client.overheadTextXOffsets[Client.overheadTextCount] = WorldMapSection2.fontBold12.stringWidth(var0.overheadText) / 2;
					Client.overheadTextAscents[Client.overheadTextCount] = WorldMapSection2.fontBold12.ascent;
					Client.overheadTextXs[Client.overheadTextCount] = Client.viewportTempX;
					Client.overheadTextYs[Client.overheadTextCount] = Client.viewportTempY;
					Client.overheadTextColors[Client.overheadTextCount] = var0.overheadTextColor;
					Client.overheadTextEffects[Client.overheadTextCount] = var0.overheadTextEffect;
					Client.overheadTextCyclesRemaining[Client.overheadTextCount] = var0.overheadTextCyclesRemaining;
					Client.overheadText[Client.overheadTextCount] = var0.overheadText;
					++Client.overheadTextCount;
				}
			}
			for (int var77 = 0; var77 < 4; ++var77) {
				int var90 = var0.hitSplatCycles[var77];
				int var12 = var0.hitSplatTypes[var77];
				HitSplatDefinition var91 = null;
				int var14 = 0;
				if (var12 >= 0) {
					if (var90 <= Client.cycle) {
						continue;
					}
					var91 = HealthBar.method2311(var0.hitSplatTypes[var77]);
					var14 = var91.field2019;
					if (var91 != null && var91.transforms != null) {
						var91 = var91.transform();
						if (var91 == null) {
							var0.hitSplatCycles[var77] = -1;
							continue;
						}
					}
				} else if (var90 < 0) {
					continue;
				}
				var15 = var0.hitSplatTypes2[var77];
				HitSplatDefinition var16 = null;
				if (var15 >= 0) {
					var16 = HealthBar.method2311(var15);
					if (var16 != null && var16.transforms != null) {
						var16 = var16.transform();
					}
				}
				if (var90 - var14 <= Client.cycle) {
					if (var91 == null) {
						var0.hitSplatCycles[var77] = -1;
					} else {
						SoundCache.method757(var0, var0.defaultHeight / 2);
						if (Client.viewportTempX > -1) {
							boolean var92 = true;
							if (var77 == 1) {
								Client.viewportTempY -= 20;
							}
							if (var77 == 2) {
								Client.viewportTempX -= 15;
								Client.viewportTempY -= 10;
							}
							if (var77 == 3) {
								Client.viewportTempX += 15;
								Client.viewportTempY -= 10;
							}
							SpritePixels var18 = null;
							SpritePixels var19 = null;
							SpritePixels var20 = null;
							SpritePixels var21 = null;
							var22 = 0;
							var23 = 0;
							int var24 = 0;
							int var25 = 0;
							int var26 = 0;
							int var27 = 0;
							int var28 = 0;
							int var29 = 0;
							SpritePixels var30 = null;
							SpritePixels var31 = null;
							SpritePixels var32 = null;
							SpritePixels var33 = null;
							int var34 = 0;
							int var35 = 0;
							int var36 = 0;
							int var37 = 0;
							int var38 = 0;
							int var39 = 0;
							int var40 = 0;
							int var41 = 0;
							int var42 = 0;
							var18 = var91.method3551();
							int var43;
							if (var18 != null) {
								var22 = var18.subWidth;
								var43 = var18.subHeight;
								if (var43 > var42) {
									var42 = var43;
								}
								var26 = var18.xOffset;
							}
							var19 = var91.method3552();
							if (var19 != null) {
								var23 = var19.subWidth;
								var43 = var19.subHeight;
								if (var43 > var42) {
									var42 = var43;
								}
								var27 = var19.xOffset;
							}
							var20 = var91.method3556();
							if (var20 != null) {
								var24 = var20.subWidth;
								var43 = var20.subHeight;
								if (var43 > var42) {
									var42 = var43;
								}
								var28 = var20.xOffset;
							}
							var21 = var91.method3563();
							if (var21 != null) {
								var25 = var21.subWidth;
								var43 = var21.subHeight;
								if (var43 > var42) {
									var42 = var43;
								}
								var29 = var21.xOffset;
							}
							if (var16 != null) {
								var30 = var16.method3551();
								if (var30 != null) {
									var34 = var30.subWidth;
									var43 = var30.subHeight;
									if (var43 > var42) {
										var42 = var43;
									}
									var38 = var30.xOffset;
								}
								var31 = var16.method3552();
								if (var31 != null) {
									var35 = var31.subWidth;
									var43 = var31.subHeight;
									if (var43 > var42) {
										var42 = var43;
									}
									var39 = var31.xOffset;
								}
								var32 = var16.method3556();
								if (var32 != null) {
									var36 = var32.subWidth;
									var43 = var32.subHeight;
									if (var43 > var42) {
										var42 = var43;
									}
									var40 = var32.xOffset;
								}
								var33 = var16.method3563();
								if (var33 != null) {
									var37 = var33.subWidth;
									var43 = var33.subHeight;
									if (var43 > var42) {
										var42 = var43;
									}
									var41 = var33.xOffset;
								}
							}
							Font var78 = var91.getFont();
							if (var78 == null) {
								var78 = class306.fontPlain11;
							}
							Font var44;
							if (var16 != null) {
								var44 = var16.getFont();
								if (var44 == null) {
									var44 = class306.fontPlain11;
								}
							} else {
								var44 = class306.fontPlain11;
							}
							String var45 = null;
							String var46 = null;
							boolean var47 = false;
							int var48 = 0;
							var45 = var91.getString(var0.hitSplatValues[var77]);
							int var93 = var78.stringWidth(var45);
							if (var16 != null) {
								var46 = var16.getString(var0.hitSplatValues2[var77]);
								var48 = var44.stringWidth(var46);
							}
							int var49 = 0;
							int var50 = 0;
							if (var23 > 0) {
								if (var20 == null && var21 == null) {
									var49 = 1;
								} else {
									var49 = var93 / var23 + 1;
								}
							}
							if (var16 != null && var35 > 0) {
								if (var32 == null && var33 == null) {
									var50 = 1;
								} else {
									var50 = var48 / var35 + 1;
								}
							}
							int var51 = 0;
							int var52 = var51;
							if (var22 > 0) {
								var51 += var22;
							}
							var51 += 2;
							int var53 = var51;
							if (var24 > 0) {
								var51 += var24;
							}
							int var54 = var51;
							int var55 = var51;
							int var56;
							if (var23 > 0) {
								var56 = var49 * var23;
								var51 += var56;
								var55 += (var56 - var93) / 2;
							} else {
								var51 += var93;
							}
							var56 = var51;
							if (var25 > 0) {
								var51 += var25;
							}
							int var57 = 0;
							int var58 = 0;
							int var59 = 0;
							int var60 = 0;
							int var61 = 0;
							int var62;
							if (var16 != null) {
								var51 += 2;
								var57 = var51;
								if (var34 > 0) {
									var51 += var34;
								}
								var51 += 2;
								var58 = var51;
								if (var36 > 0) {
									var51 += var36;
								}
								var59 = var51;
								var61 = var51;
								if (var35 > 0) {
									var62 = var35 * var50;
									var51 += var62;
									var61 += (var62 - var48) / 2;
								} else {
									var51 += var48;
								}
								var60 = var51;
								if (var37 > 0) {
									var51 += var37;
								}
							}
							var62 = var0.hitSplatCycles[var77] - Client.cycle;
							int var63 = var91.field2015 - var62 * var91.field2015 / var91.field2019;
							int var64 = var62 * var91.field2025 / var91.field2019 + -var91.field2025;
							int var65 = var63 + (var2 + Client.viewportTempX - (var51 >> 1));
							int var66 = var64 + (var3 + Client.viewportTempY - 12);
							int var67 = var66;
							int var68 = var66 + var42;
							int var69 = var66 + var91.field2031 + 15;
							int var70 = var69 - var78.maxAscent;
							int var71 = var69 + var78.maxDescent;
							if (var70 < var66) {
								var67 = var70;
							}
							if (var71 > var68) {
								var68 = var71;
							}
							int var72 = 0;
							int var73;
							int var74;
							if (var16 != null) {
								var72 = var66 + var16.field2031 + 15;
								var73 = var72 - var44.maxAscent;
								var74 = var72 + var44.maxDescent;
								if (var73 < var67) {
								}
								if (var74 > var68) {
								}
							}
							var73 = 255;
							if (var91.field2026 >= 0) {
								var73 = (var62 << 8) / (var91.field2019 - var91.field2026);
							}
							if (var73 >= 0 && var73 < 255) {
								if (var18 != null) {
									var18.drawTransAt(var52 + var65 - var26, var66, var73);
								}
								if (var20 != null) {
									var20.drawTransAt(var53 + var65 - var28, var66, var73);
								}
								if (var19 != null) {
									for (var74 = 0; var74 < var49; ++var74) {
										var19.drawTransAt(var23 * var74 + (var65 + var54 - var27), var66, var73);
									}
								}
								if (var21 != null) {
									var21.drawTransAt(var56 + var65 - var29, var66, var73);
								}
								var78.drawAlpha(var45, var55 + var65, var69, var91.textColor, 0, var73);
								if (var16 != null) {
									if (var30 != null) {
										var30.drawTransAt(var57 + var65 - var38, var66, var73);
									}
									if (var32 != null) {
										var32.drawTransAt(var65 + var58 - var40, var66, var73);
									}
									if (var31 != null) {
										for (var74 = 0; var74 < var50; ++var74) {
											var31.drawTransAt(var35 * var74 + (var59 + var65 - var39), var66, var73);
										}
									}
									if (var33 != null) {
										var33.drawTransAt(var65 + var60 - var41, var66, var73);
									}
									var44.drawAlpha(var46, var65 + var61, var72, var16.textColor, 0, var73);
								}
							} else {
								if (var18 != null) {
									var18.drawTransBgAt(var52 + var65 - var26, var66);
								}
								if (var20 != null) {
									var20.drawTransBgAt(var53 + var65 - var28, var66);
								}
								if (var19 != null) {
									for (var74 = 0; var74 < var49; ++var74) {
										var19.drawTransBgAt(var23 * var74 + (var65 + var54 - var27), var66);
									}
								}
								if (var21 != null) {
									var21.drawTransBgAt(var56 + var65 - var29, var66);
								}
								var78.draw(var45, var65 + var55, var69, var91.textColor | -16777216, 0);
								if (var16 != null) {
									if (var30 != null) {
										var30.drawTransBgAt(var57 + var65 - var38, var66);
									}
									if (var32 != null) {
										var32.drawTransBgAt(var65 + var58 - var40, var66);
									}
									if (var31 != null) {
										for (var74 = 0; var74 < var50; ++var74) {
											var31.drawTransBgAt(var74 * var35 + (var65 + var59 - var39), var66);
										}
									}
									if (var33 != null) {
										var33.drawTransBgAt(var65 + var60 - var41, var66);
									}
									var44.draw(var46, var61 + var65, var72, var16.textColor | -16777216, 0);
								}
							}
						}
					}
				}
			}
		}
	}
}
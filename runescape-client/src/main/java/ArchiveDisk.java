import net.runelite.mapping.ObfuscatedName;
import net.runelite.mapping.ObfuscatedSignature;
import net.runelite.mapping.ObfuscatedGetter;
import net.runelite.mapping.Implements;
import java.io.IOException;
import java.io.EOFException;
import net.runelite.mapping.Export;
@ObfuscatedName("nk")
@Implements("ArchiveDisk")
public final class ArchiveDisk {
	@ObfuscatedName("s")
	@Export("ArchiveDisk_buffer")
	static byte[] ArchiveDisk_buffer = new byte[520];

	@ObfuscatedName("h")
	@ObfuscatedSignature(descriptor = "Lpf;")
	@Export("datFile")
	BufferedFile datFile = null;

	@ObfuscatedName("w")
	@ObfuscatedSignature(descriptor = "Lpf;")
	@Export("idxFile")
	BufferedFile idxFile = null;

	@ObfuscatedName("v")
	@ObfuscatedGetter(intValue = -1863173991)
	@Export("archive")
	int archive;

	@ObfuscatedName("c")
	@ObfuscatedGetter(intValue = -1536954985)
	@Export("maxEntrySize")
	int maxEntrySize = 65000;

	@ObfuscatedSignature(descriptor = "(ILpf;Lpf;I)V")
	public ArchiveDisk(int var1, BufferedFile var2, BufferedFile var3, int var4) {
		this.archive = var1;
		this.datFile = var2;
		this.idxFile = var3;
		this.maxEntrySize = var4;
	}

	@ObfuscatedName("s")
	@ObfuscatedSignature(descriptor = "(II)[B", garbageValue = "-1926872003")
	@Export("read")
	public byte[] read(int var1) {
		synchronized(this.datFile) {
			try {
				Object var10000;
				if (this.idxFile.length() < ((long) (var1 * 6 + 6))) {
					var10000 = null;
					return ((byte[]) (var10000));
				} else {
					this.idxFile.seek(((long) (var1 * 6)));
					this.idxFile.read(ArchiveDisk_buffer, 0, 6);
					int var3 = ((ArchiveDisk_buffer[0] & 255) << 16) + (ArchiveDisk_buffer[2] & 255) + ((ArchiveDisk_buffer[1] & 255) << 8);
					int var4 = (ArchiveDisk_buffer[5] & 255) + ((ArchiveDisk_buffer[3] & 255) << 16) + ((ArchiveDisk_buffer[4] & 255) << 8);
					if (var3 < 0 || var3 > this.maxEntrySize) {
						var10000 = null;
						return ((byte[]) (var10000));
					} else if (var4 <= 0 || ((long) (var4)) > this.datFile.length() / 520L) {
						var10000 = null;
						return ((byte[]) (var10000));
					} else {
						byte[] var5 = new byte[var3];
						int var6 = 0;
						int var7 = 0;
						while (var6 < var3) {
							if (var4 == 0) {
								var10000 = null;
								return ((byte[]) (var10000));
							}
							this.datFile.seek(((long) (var4)) * 520L);
							int var8 = var3 - var6;
							int var9;
							int var10;
							int var11;
							int var12;
							byte var13;
							if (var1 > 65535) {
								if (var8 > 510) {
									var8 = 510;
								}
								var13 = 10;
								this.datFile.read(ArchiveDisk_buffer, 0, var8 + var13);
								var9 = ((ArchiveDisk_buffer[1] & 255) << 16) + ((ArchiveDisk_buffer[0] & 255) << 24) + (ArchiveDisk_buffer[3] & 255) + ((ArchiveDisk_buffer[2] & 255) << 8);
								var10 = (ArchiveDisk_buffer[5] & 255) + ((ArchiveDisk_buffer[4] & 255) << 8);
								var11 = (ArchiveDisk_buffer[8] & 255) + ((ArchiveDisk_buffer[7] & 255) << 8) + ((ArchiveDisk_buffer[6] & 255) << 16);
								var12 = ArchiveDisk_buffer[9] & 255;
							} else {
								if (var8 > 512) {
									var8 = 512;
								}
								var13 = 8;
								this.datFile.read(ArchiveDisk_buffer, 0, var8 + var13);
								var9 = (ArchiveDisk_buffer[1] & 255) + ((ArchiveDisk_buffer[0] & 255) << 8);
								var10 = (ArchiveDisk_buffer[3] & 255) + ((ArchiveDisk_buffer[2] & 255) << 8);
								var11 = ((ArchiveDisk_buffer[5] & 255) << 8) + ((ArchiveDisk_buffer[4] & 255) << 16) + (ArchiveDisk_buffer[6] & 255);
								var12 = ArchiveDisk_buffer[7] & 255;
							}
							if (var9 == var1 && var7 == var10 && var12 == this.archive) {
								if (var11 >= 0 && ((long) (var11)) <= this.datFile.length() / 520L) {
									int var14 = var13 + var8;
									for (int var15 = var13; var15 < var14; ++var15) {
										var5[var6++] = ArchiveDisk_buffer[var15];
									}
									var4 = var11;
									++var7;
									continue;
								}
								var10000 = null;
								return ((byte[]) (var10000));
							}
							var10000 = null;
							return ((byte[]) (var10000));
						} 
						byte[] var20 = var5;
						return var20;
					}
				}
			} catch (IOException var18) {
				return null;
			}
		}
	}

	@ObfuscatedName("h")
	@ObfuscatedSignature(descriptor = "(I[BII)Z", garbageValue = "-2068211733")
	@Export("write")
	public boolean write(int var1, byte[] var2, int var3) {
		synchronized(this.datFile) {
			if (var3 >= 0 && var3 <= this.maxEntrySize) {
				boolean var5 = this.write0(var1, var2, var3, true);
				if (!var5) {
					var5 = this.write0(var1, var2, var3, false);
				}
				return var5;
			} else {
				throw new IllegalArgumentException("" + this.archive + ',' + var1 + ',' + var3);
			}
		}
	}

	@ObfuscatedName("w")
	@ObfuscatedSignature(descriptor = "(I[BIZB)Z", garbageValue = "0")
	@Export("write0")
	boolean write0(int var1, byte[] var2, int var3, boolean var4) {
		synchronized(this.datFile) {
			try {
				int var6;
				boolean var10000;
				if (var4) {
					if (this.idxFile.length() < ((long) (var1 * 6 + 6))) {
						var10000 = false;
						return var10000;
					}
					this.idxFile.seek(((long) (var1 * 6)));
					this.idxFile.read(ArchiveDisk_buffer, 0, 6);
					var6 = (ArchiveDisk_buffer[5] & 255) + ((ArchiveDisk_buffer[3] & 255) << 16) + ((ArchiveDisk_buffer[4] & 255) << 8);
					if (var6 <= 0 || ((long) (var6)) > this.datFile.length() / 520L) {
						var10000 = false;
						return var10000;
					}
				} else {
					var6 = ((int) ((this.datFile.length() + 519L) / 520L));
					if (var6 == 0) {
						var6 = 1;
					}
				}
				ArchiveDisk_buffer[0] = ((byte) (var3 >> 16));
				ArchiveDisk_buffer[1] = ((byte) (var3 >> 8));
				ArchiveDisk_buffer[2] = ((byte) (var3));
				ArchiveDisk_buffer[3] = ((byte) (var6 >> 16));
				ArchiveDisk_buffer[4] = ((byte) (var6 >> 8));
				ArchiveDisk_buffer[5] = ((byte) (var6));
				this.idxFile.seek(((long) (var1 * 6)));
				this.idxFile.write(ArchiveDisk_buffer, 0, 6);
				int var7 = 0;
				int var8 = 0;
				while (true) {
					if (var7 < var3) {
						label168 : {
							int var9 = 0;
							int var10;
							if (var4) {
								this.datFile.seek(520L * ((long) (var6)));
								int var11;
								int var12;
								if (var1 > 65535) {
									try {
										this.datFile.read(ArchiveDisk_buffer, 0, 10);
									} catch (EOFException var17) {
										break label168;
									}
									var10 = ((ArchiveDisk_buffer[1] & 255) << 16) + ((ArchiveDisk_buffer[0] & 255) << 24) + (ArchiveDisk_buffer[3] & 255) + ((ArchiveDisk_buffer[2] & 255) << 8);
									var11 = (ArchiveDisk_buffer[5] & 255) + ((ArchiveDisk_buffer[4] & 255) << 8);
									var9 = (ArchiveDisk_buffer[8] & 255) + ((ArchiveDisk_buffer[7] & 255) << 8) + ((ArchiveDisk_buffer[6] & 255) << 16);
									var12 = ArchiveDisk_buffer[9] & 255;
								} else {
									try {
										this.datFile.read(ArchiveDisk_buffer, 0, 8);
									} catch (EOFException var16) {
										break label168;
									}
									var10 = (ArchiveDisk_buffer[1] & 255) + ((ArchiveDisk_buffer[0] & 255) << 8);
									var11 = (ArchiveDisk_buffer[3] & 255) + ((ArchiveDisk_buffer[2] & 255) << 8);
									var9 = ((ArchiveDisk_buffer[5] & 255) << 8) + ((ArchiveDisk_buffer[4] & 255) << 16) + (ArchiveDisk_buffer[6] & 255);
									var12 = ArchiveDisk_buffer[7] & 255;
								}
								if (var10 != var1 || var8 != var11 || var12 != this.archive) {
									var10000 = false;
									return var10000;
								}
								if (var9 < 0 || ((long) (var9)) > this.datFile.length() / 520L) {
									var10000 = false;
									return var10000;
								}
							}
							if (var9 == 0) {
								var4 = false;
								var9 = ((int) ((this.datFile.length() + 519L) / 520L));
								if (var9 == 0) {
									++var9;
								}
								if (var6 == var9) {
									++var9;
								}
							}
							if (var1 > 65535) {
								if (var3 - var7 <= 510) {
									var9 = 0;
								}
								ArchiveDisk_buffer[0] = ((byte) (var1 >> 24));
								ArchiveDisk_buffer[1] = ((byte) (var1 >> 16));
								ArchiveDisk_buffer[2] = ((byte) (var1 >> 8));
								ArchiveDisk_buffer[3] = ((byte) (var1));
								ArchiveDisk_buffer[4] = ((byte) (var8 >> 8));
								ArchiveDisk_buffer[5] = ((byte) (var8));
								ArchiveDisk_buffer[6] = ((byte) (var9 >> 16));
								ArchiveDisk_buffer[7] = ((byte) (var9 >> 8));
								ArchiveDisk_buffer[8] = ((byte) (var9));
								ArchiveDisk_buffer[9] = ((byte) (this.archive));
								this.datFile.seek(((long) (var6)) * 520L);
								this.datFile.write(ArchiveDisk_buffer, 0, 10);
								var10 = var3 - var7;
								if (var10 > 510) {
									var10 = 510;
								}
								this.datFile.write(var2, var7, var10);
								var7 += var10;
							} else {
								if (var3 - var7 <= 512) {
									var9 = 0;
								}
								ArchiveDisk_buffer[0] = ((byte) (var1 >> 8));
								ArchiveDisk_buffer[1] = ((byte) (var1));
								ArchiveDisk_buffer[2] = ((byte) (var8 >> 8));
								ArchiveDisk_buffer[3] = ((byte) (var8));
								ArchiveDisk_buffer[4] = ((byte) (var9 >> 16));
								ArchiveDisk_buffer[5] = ((byte) (var9 >> 8));
								ArchiveDisk_buffer[6] = ((byte) (var9));
								ArchiveDisk_buffer[7] = ((byte) (this.archive));
								this.datFile.seek(((long) (var6)) * 520L);
								this.datFile.write(ArchiveDisk_buffer, 0, 8);
								var10 = var3 - var7;
								if (var10 > 512) {
									var10 = 512;
								}
								this.datFile.write(var2, var7, var10);
								var7 += var10;
							}
							var6 = var9;
							++var8;
							continue;
						}
					}
					var10000 = true;
					return var10000;
				} 
			} catch (IOException var18) {
				return false;
			}
		}
	}

	public String toString() {
		return "" + this.archive;
	}

	@ObfuscatedName("s")
	@ObfuscatedSignature(descriptor = "(II)Lfh;", garbageValue = "-2136725647")
	@Export("SpotAnimationDefinition_get")
	public static SpotAnimationDefinition SpotAnimationDefinition_get(int var0) {
		SpotAnimationDefinition var1 = ((SpotAnimationDefinition) (SpotAnimationDefinition.SpotAnimationDefinition_cached.get(((long) (var0)))));
		if (var1 != null) {
			return var1;
		} else {
			byte[] var2 = SpotAnimationDefinition.SpotAnimationDefinition_archive.takeFile(13, var0);
			var1 = new SpotAnimationDefinition();
			var1.id = var0;
			if (var2 != null) {
				var1.decode(new Buffer(var2));
			}
			SpotAnimationDefinition.SpotAnimationDefinition_cached.put(var1, ((long) (var0)));
			return var1;
		}
	}

	@ObfuscatedName("q")
	@ObfuscatedSignature(descriptor = "(IIIB)V", garbageValue = "52")
	static final void method6835(int var0, int var1, int var2) {
		int var3;
		for (var3 = 0; var3 < 8; ++var3) {
			for (int var4 = 0; var4 < 8; ++var4) {
				Tiles.Tiles_heights[var0][var3 + var1][var4 + var2] = 0;
			}
		}
		if (var1 > 0) {
			for (var3 = 1; var3 < 8; ++var3) {
				Tiles.Tiles_heights[var0][var1][var3 + var2] = Tiles.Tiles_heights[var0][var1 - 1][var3 + var2];
			}
		}
		if (var2 > 0) {
			for (var3 = 1; var3 < 8; ++var3) {
				Tiles.Tiles_heights[var0][var3 + var1][var2] = Tiles.Tiles_heights[var0][var3 + var1][var2 - 1];
			}
		}
		if (var1 > 0 && Tiles.Tiles_heights[var0][var1 - 1][var2] != 0) {
			Tiles.Tiles_heights[var0][var1][var2] = Tiles.Tiles_heights[var0][var1 - 1][var2];
		} else if (var2 > 0 && Tiles.Tiles_heights[var0][var1][var2 - 1] != 0) {
			Tiles.Tiles_heights[var0][var1][var2] = Tiles.Tiles_heights[var0][var1][var2 - 1];
		} else if (var1 > 0 && var2 > 0 && Tiles.Tiles_heights[var0][var1 - 1][var2 - 1] != 0) {
			Tiles.Tiles_heights[var0][var1][var2] = Tiles.Tiles_heights[var0][var1 - 1][var2 - 1];
		}
	}

	@ObfuscatedName("at")
	@ObfuscatedSignature(descriptor = "(ILbz;ZI)I", garbageValue = "-133301813")
	static int method6836(int var0, Script var1, boolean var2) {
		if (var0 != 7000 && var0 != 7005 && var0 != 7010 && var0 != 7015 && var0 != 7020 && var0 != 7025 && var0 != 7030 && var0 != 7035) {
			if (var0 != 7001 && var0 != 7002 && var0 != 7011 && var0 != 7012 && var0 != 7021 && var0 != 7022) {
				if (var0 != 7003 && var0 != 7013 && var0 != 7023) {
					if (var0 != 7006 && var0 != 7007 && var0 != 7016 && var0 != 7017 && var0 != 7026 && var0 != 7027) {
						if (var0 != 7008 && var0 != 7018 && var0 != 7028) {
							if (var0 != 7031 && var0 != 7032) {
								if (var0 == 7033) {
									--UserComparator8.Interpreter_stringStackSize;
									return 1;
								} else if (var0 != 7036 && var0 != 7037) {
									if (var0 == 7038) {
										--User.Interpreter_intStackSize;
										return 1;
									} else if (var0 != 7004 && var0 != 7009 && var0 != 7014 && var0 != 7019 && var0 != 7024 && var0 != 7029 && var0 != 7034 && var0 != 7039) {
										return 2;
									} else {
										--User.Interpreter_intStackSize;
										return 1;
									}
								} else {
									User.Interpreter_intStackSize -= 2;
									return 1;
								}
							} else {
								--UserComparator8.Interpreter_stringStackSize;
								--User.Interpreter_intStackSize;
								return 1;
							}
						} else {
							--User.Interpreter_intStackSize;
							return 1;
						}
					} else {
						User.Interpreter_intStackSize -= 2;
						return 1;
					}
				} else {
					User.Interpreter_intStackSize -= 2;
					return 1;
				}
			} else {
				User.Interpreter_intStackSize -= 3;
				return 1;
			}
		} else {
			User.Interpreter_intStackSize -= 5;
			return 1;
		}
	}
}
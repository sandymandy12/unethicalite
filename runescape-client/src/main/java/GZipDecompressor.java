import net.runelite.mapping.ObfuscatedName;
import net.runelite.mapping.ObfuscatedSignature;
import net.runelite.mapping.Implements;
import java.util.zip.Inflater;
import net.runelite.mapping.Export;
@ObfuscatedName("qh")
@Implements("GZipDecompressor")
public class GZipDecompressor {
	@ObfuscatedName("v")
	@ObfuscatedSignature(descriptor = "Lls;")
	@Export("SequenceDefinition_skeletonsArchive")
	static AbstractArchive SequenceDefinition_skeletonsArchive;

	@ObfuscatedName("s")
	@Export("inflater")
	Inflater inflater;

	public GZipDecompressor() {
		this(-1, 1000000, 1000000);
	}

	@ObfuscatedSignature(descriptor = "(III)V", garbageValue = "1000000")
	GZipDecompressor(int var1, int var2, int var3) {
	}

	@ObfuscatedName("s")
	@ObfuscatedSignature(descriptor = "(Lqr;[BB)V", garbageValue = "29")
	@Export("decompress")
	public void decompress(Buffer var1, byte[] var2) {
		if (var1.array[var1.offset] == 31 && var1.array[var1.offset + 1] == -117) {
			if (this.inflater == null) {
				this.inflater = new Inflater(true);
			}
			try {
				this.inflater.setInput(var1.array, var1.offset + 10, var1.array.length - (var1.offset + 8 + 10));
				this.inflater.inflate(var2);
			} catch (Exception var4) {
				this.inflater.reset();
				throw new RuntimeException("");
			}
			this.inflater.reset();
		} else {
			throw new RuntimeException("");
		}
	}
}
import net.runelite.mapping.ObfuscatedName;
import net.runelite.mapping.ObfuscatedSignature;
import net.runelite.mapping.ObfuscatedGetter;
import net.runelite.mapping.Implements;
import net.runelite.mapping.Export;
@ObfuscatedName("qz")
@Implements("PacketBuffer")
public class PacketBuffer extends Buffer {
	@ObfuscatedName("h")
	static final int[] field4699 = new int[]{ 0, 1, 3, 7, 15, 31, 63, 127, 255, 511, 1023, 2047, 4095, 8191, 16383, 32767, 65535, 131071, 262143, 524287, 1048575, 2097151, 4194303, 8388607, 16777215, 33554431, 67108863, 134217727, 268435455, 536870911, 1073741823, Integer.MAX_VALUE, -1 };

	@ObfuscatedName("eh")
	@ObfuscatedSignature(descriptor = "Lln;")
	@Export("archive6")
	static Archive archive6;

	@ObfuscatedName("s")
	@ObfuscatedSignature(descriptor = "Lqb;")
	@Export("isaacCipher")
	IsaacCipher isaacCipher;

	@ObfuscatedName("w")
	@ObfuscatedGetter(intValue = -996644633)
	@Export("bitIndex")
	int bitIndex;

	public PacketBuffer(int var1) {
		super(var1);
	}

	@ObfuscatedName("s")
	@ObfuscatedSignature(descriptor = "([IB)V", garbageValue = "2")
	@Export("newIsaacCipher")
	public void newIsaacCipher(int[] var1) {
		this.isaacCipher = new IsaacCipher(var1);
	}

	@ObfuscatedName("h")
	@ObfuscatedSignature(descriptor = "(Lqb;B)V", garbageValue = "12")
	@Export("setIsaacCipher")
	public void setIsaacCipher(IsaacCipher var1) {
		this.isaacCipher = var1;
	}

	@ObfuscatedName("w")
	@ObfuscatedSignature(descriptor = "(II)V", garbageValue = "-1869855095")
	@Export("writeByteIsaac")
	public void writeByteIsaac(int var1) {
		super.array[++super.offset - 1] = ((byte) (var1 + this.isaacCipher.nextInt()));
	}

	@ObfuscatedName("v")
	@ObfuscatedSignature(descriptor = "(B)I", garbageValue = "-7")
	@Export("readByteIsaac")
	public int readByteIsaac() {
		return super.array[++super.offset - 1] - this.isaacCipher.nextInt() & 255;
	}

	@ObfuscatedName("c")
	@ObfuscatedSignature(descriptor = "(I)Z", garbageValue = "-1385121995")
	public boolean method7580() {
		int var1 = super.array[super.offset] - this.isaacCipher.method8195() & 255;
		return var1 >= 128;
	}

	@ObfuscatedName("q")
	@ObfuscatedSignature(descriptor = "(I)I", garbageValue = "1527124495")
	@Export("readSmartByteShortIsaac")
	public int readSmartByteShortIsaac() {
		int var1 = super.array[++super.offset - 1] - this.isaacCipher.nextInt() & 255;
		return var1 < 128 ? var1 : (var1 - 128 << 8) + (super.array[++super.offset - 1] - this.isaacCipher.nextInt() & 255);
	}

	@ObfuscatedName("i")
	@ObfuscatedSignature(descriptor = "([BIII)V", garbageValue = "-2140817283")
	public void method7582(byte[] var1, int var2, int var3) {
		for (int var4 = 0; var4 < var3; ++var4) {
			var1[var4 + var2] = ((byte) (super.array[++super.offset - 1] - this.isaacCipher.nextInt()));
		}
	}

	@ObfuscatedName("k")
	@ObfuscatedSignature(descriptor = "(I)V", garbageValue = "1216158312")
	@Export("importIndex")
	public void importIndex() {
		this.bitIndex = super.offset * 8;
	}

	@ObfuscatedName("o")
	@ObfuscatedSignature(descriptor = "(II)I", garbageValue = "1473614556")
	@Export("readBits")
	public int readBits(int var1) {
		int var2 = this.bitIndex >> 3;
		int var3 = 8 - (this.bitIndex & 7);
		int var4 = 0;
		for (this.bitIndex += var1; var1 > var3; var3 = 8) {
			var4 += (super.array[var2++] & field4699[var3]) << var1 - var3;
			var1 -= var3;
		}
		if (var3 == var1) {
			var4 += super.array[var2] & field4699[var3];
		} else {
			var4 += super.array[var2] >> var3 - var1 & field4699[var1];
		}
		return var4;
	}

	@ObfuscatedName("n")
	@ObfuscatedSignature(descriptor = "(B)V", garbageValue = "-85")
	@Export("exportIndex")
	public void exportIndex() {
		super.offset = (this.bitIndex + 7) / 8;
	}

	@ObfuscatedName("d")
	@ObfuscatedSignature(descriptor = "(IB)I", garbageValue = "-6")
	@Export("bitsRemaining")
	public int bitsRemaining(int var1) {
		return var1 * 8 - this.bitIndex;
	}
}
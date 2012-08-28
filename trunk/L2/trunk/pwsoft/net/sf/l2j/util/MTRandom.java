package net.sf.l2j.util;

import java.util.Random;

public class MTRandom extends Random
{
  private static final long serialVersionUID = -515082678588212038L;
  private static final int UPPER_MASK = -2147483648;
  private static final int LOWER_MASK = 2147483647;
  private static final int N = 624;
  private static final int M = 397;
  private static final int[] MAGIC = { 0, -1727483681 };
  private static final int MAGIC_FACTOR1 = 1812433253;
  private static final int MAGIC_FACTOR2 = 1664525;
  private static final int MAGIC_FACTOR3 = 1566083941;
  private static final int MAGIC_MASK1 = -1658038656;
  private static final int MAGIC_MASK2 = -272236544;
  private static final int MAGIC_SEED = 19650218;
  private static final long DEFAULT_SEED = 5489L;
  private transient int[] mt;
  private transient int mti;
  private transient boolean compat = false;
  private transient int[] ibuf;

  public MTRandom()
  {
  }

  public MTRandom(boolean compatible)
  {
    super(0L);
    compat = compatible;
    setSeed(compat ? 5489L : System.currentTimeMillis());
  }

  public MTRandom(long seed)
  {
    super(seed);
  }

  public MTRandom(byte[] buf)
  {
    super(0L);
    setSeed(buf);
  }

  public MTRandom(int[] buf)
  {
    super(0L);
    setSeed(buf);
  }

  private final void setSeed(int seed)
  {
    if (mt == null) mt = new int[624];

    mt[0] = seed;
    for (mti = 1; mti < 624; mti += 1)
      mt[mti] = (1812433253 * (mt[(mti - 1)] ^ mt[(mti - 1)] >>> 30) + mti);
  }

  public final synchronized void setSeed(long seed)
  {
    if (compat) {
      setSeed((int)seed);
    }
    else
    {
      if (ibuf == null) ibuf = new int[2];

      ibuf[0] = (int)seed;
      ibuf[1] = (int)(seed >>> 32);
      setSeed(ibuf);
    }
  }

  public final void setSeed(byte[] buf)
  {
    setSeed(pack(buf));
  }

  public final synchronized void setSeed(int[] buf)
  {
    int length = buf.length;
    if (length == 0) throw new IllegalArgumentException("Seed buffer may not be empty");

    int i = 1; int j = 0; int k = 624 > length ? 624 : length;
    setSeed(19650218);
    for (; k > 0; k--) {
      mt[i] = ((mt[i] ^ (mt[(i - 1)] ^ mt[(i - 1)] >>> 30) * 1664525) + buf[j] + j);
      i++; j++;
      if (i >= 624) { mt[0] = mt[623]; i = 1; }
      if (j < length) continue; j = 0;
    }
    for (k = 623; k > 0; k--) {
      mt[i] = ((mt[i] ^ (mt[(i - 1)] ^ mt[(i - 1)] >>> 30) * 1566083941) - i);
      i++;
      if (i < 624) continue; mt[0] = mt[623]; i = 1;
    }
    mt[0] = -2147483648;
  }

  protected final synchronized int next(int bits)
  {
    if (mti >= 624)
    {
      for (int kk = 0; kk < 227; kk++) {
        int y = mt[kk] & 0x80000000 | mt[(kk + 1)] & 0x7FFFFFFF;
        mt[kk] = (mt[(kk + 397)] ^ y >>> 1 ^ MAGIC[(y & 0x1)]);
      }
      for (; kk < 623; kk++) {
        int y = mt[kk] & 0x80000000 | mt[(kk + 1)] & 0x7FFFFFFF;
        mt[kk] = (mt[(kk + -227)] ^ y >>> 1 ^ MAGIC[(y & 0x1)]);
      }
      int y = mt[623] & 0x80000000 | mt[0] & 0x7FFFFFFF;
      mt[623] = (mt[396] ^ y >>> 1 ^ MAGIC[(y & 0x1)]);

      mti = 0;
    }

    int y = mt[(mti++)];

    y ^= y >>> 11;
    y ^= y << 7 & 0x9D2C5680;
    y ^= y << 15 & 0xEFC60000;
    y ^= y >>> 18;

    return y >>> 32 - bits;
  }

  public static int[] pack(byte[] buf)
  {
    int blen = buf.length; int ilen = buf.length + 3 >>> 2;
    int[] ibuf = new int[ilen];
    for (int n = 0; n < ilen; n++) {
      int m = n + 1 << 2;
      if (m > blen) m = blen;
      m--; for (int k = buf[m] & 0xFF; (m & 0x3) != 0; k = k << 8 | buf[m] & 0xFF) m--;
      ibuf[n] = k;
    }
    return ibuf;
  }
}
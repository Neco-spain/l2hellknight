package net.sf.l2j.util;

public class Rnd
{
  private static MTRandom _rnd = new MTRandom();

  public static final float get()
  {
    return _rnd.nextFloat();
  }

  public static final int get(int n)
  {
    return (int)Math.floor(_rnd.nextDouble() * n);
  }

  public static final int get(int min, int max)
  {
    return min + (int)Math.floor(_rnd.nextDouble() * (max - min + 1));
  }

  public static final int nextInt(int n) {
    return (int)Math.floor(_rnd.nextDouble() * n);
  }

  public static final int nextInt() {
    return _rnd.nextInt();
  }

  public static final double nextDouble() {
    return _rnd.nextDouble();
  }

  public static final double nextGaussian() {
    return _rnd.nextGaussian();
  }

  public static final boolean nextBoolean() {
    return _rnd.nextBoolean();
  }

  public static final void nextBytes(byte[] array) {
    _rnd.nextBytes(array);
  }

  public static boolean chance(double chance)
  {
    return _rnd.nextDouble() <= chance / 100.0D;
  }
}
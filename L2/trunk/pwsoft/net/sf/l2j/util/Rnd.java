package net.sf.l2j.util;

import java.util.Random;
import net.sf.l2j.Config;

public class Rnd
{
  private static MTRandom _rnd = new MTRandom();

  public static Random skillRnd = new Random(System.nanoTime());

  public static float get()
  {
    return _rnd.nextFloat();
  }

  public static int get(int n)
  {
    return (int)Math.floor(_rnd.nextDouble() * n);
  }

  public static int get(int min, int max)
  {
    return min + (int)Math.floor(_rnd.nextDouble() * (max - min + 1));
  }

  public static boolean chance(int chance) {
    return _rnd.nextInt(99) + 1 <= chance;
  }

  public static boolean calcEnchant(double chance, boolean premium) {
    if (premium) {
      chance += Config.PREMIUM_ENCH_ITEM;
    }

    if (Config.ENCHANT_ALT_FORMULA) {
      return poker(chance) < chance;
    }
    return _rnd.nextDouble() <= chance / 100.0D;
  }

  public static int nextInt(int n) {
    return (int)Math.floor(_rnd.nextDouble() * n);
  }

  public static int nextInt() {
    return _rnd.nextInt();
  }

  public static double nextDouble() {
    return _rnd.nextDouble();
  }

  public static double nextGaussian() {
    return _rnd.nextGaussian();
  }

  public static boolean nextBoolean() {
    return _rnd.nextBoolean();
  }

  public static void nextBytes(byte[] array) {
    _rnd.nextBytes(array);
  }

  public static double chance(double pMin, double pMax)
  {
    skillRnd.setSeed(System.nanoTime());
    return pMin + skillRnd.nextDouble() * (pMax - pMin);
  }

  public static double poker(double chance) {
    return chance(chance * 0.75D, 100.0D);
  }

  public static double poker(double chance, double pMax) {
    return chance(chance * 0.75D, pMax);
  }
}
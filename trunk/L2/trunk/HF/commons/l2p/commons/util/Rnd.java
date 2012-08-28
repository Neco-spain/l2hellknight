package l2m.commons.util;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.math.random.MersenneTwister;
import org.apache.commons.math.random.RandomGenerator;

public class Rnd
{
  private static final ThreadLocal<RandomGenerator> rnd = new ThreadLocalGeneratorHolder();

  private static AtomicLong seedUniquifier = new AtomicLong(8682522807148012L);

  private static RandomGenerator rnd()
  {
    return (RandomGenerator)rnd.get();
  }

  public static double get()
  {
    return rnd().nextDouble();
  }

  public static int get(int n)
  {
    return rnd().nextInt(n);
  }

  public static long get(long n)
  {
    return ()(rnd().nextDouble() * n);
  }

  public static int get(int min, int max)
  {
    return min + get(max - min + 1);
  }

  public static long get(long min, long max)
  {
    return min + get(max - min + 1L);
  }

  public static int nextInt()
  {
    return rnd().nextInt();
  }

  public static double nextDouble()
  {
    return rnd().nextDouble();
  }

  public static double nextGaussian()
  {
    return rnd().nextGaussian();
  }

  public static boolean nextBoolean()
  {
    return rnd().nextBoolean();
  }

  public static boolean chance(int chance)
  {
    return (chance >= 1) && ((chance > 99) || (rnd().nextInt(99) + 1 <= chance));
  }

  public static boolean chance(double chance)
  {
    return rnd().nextDouble() <= chance / 100.0D;
  }

  public static <E> E get(E[] list)
  {
    return list[get(list.length)];
  }

  public static int get(int[] list)
  {
    return list[get(list.length)];
  }

  public static <E> E get(List<E> list)
  {
    return list.get(get(list.size()));
  }

  static final class ThreadLocalGeneratorHolder extends ThreadLocal<RandomGenerator>
  {
    public RandomGenerator initialValue()
    {
      return new MersenneTwister(Rnd.seedUniquifier.getAndIncrement() + System.nanoTime());
    }
  }
}
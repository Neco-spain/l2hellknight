package l2m.commons.lang.reference;

public abstract interface HardReference<T>
{
  public abstract T get();

  public abstract void clear();
}
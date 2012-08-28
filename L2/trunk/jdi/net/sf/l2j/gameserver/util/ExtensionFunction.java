package net.sf.l2j.gameserver.util;

public abstract interface ExtensionFunction
{
  public abstract Object get(String paramString);

  public abstract void set(String paramString, Object paramObject);
}
package net.sf.l2j.gameserver.templates;

import javolution.util.FastMap;
import javolution.util.FastMap.Entry;

public final class StatsSet
{
  private final FastMap<String, Object> _set = new FastMap().shared("StatsSet._set");

  public final FastMap<String, Object> getSet()
  {
    return _set;
  }

  public void add(StatsSet newSet)
  {
    FastMap newMap = newSet.getSet();

    FastMap.Entry e = newMap.head(); for (FastMap.Entry end = newMap.tail(); (e = e.getNext()) != end; )
    {
      _set.put(e.getKey(), e.getValue());
    }
  }

  public boolean getBool(String name)
  {
    Object val = _set.get(name);
    if (val == null)
      throw new IllegalArgumentException("Boolean value required, but not specified");
    if ((val instanceof Boolean))
      return ((Boolean)val).booleanValue();
    try {
      return Boolean.parseBoolean((String)val); } catch (Exception e) {
    }
    throw new IllegalArgumentException("Boolean value required, but found: " + val);
  }

  public boolean getBool(String name, boolean deflt)
  {
    Object val = _set.get(name);
    if (val == null)
      return deflt;
    if ((val instanceof Boolean))
      return ((Boolean)val).booleanValue();
    try {
      return Boolean.parseBoolean((String)val); } catch (Exception e) {
    }
    throw new IllegalArgumentException("Boolean value required, but found: " + val);
  }

  public byte getByte(String name, byte deflt)
  {
    Object val = _set.get(name);
    if (val == null)
      return deflt;
    if ((val instanceof Number))
      return ((Number)val).byteValue();
    try {
      return Byte.parseByte((String)val); } catch (Exception e) {
    }
    throw new IllegalArgumentException("Byte value required, but found: " + val);
  }

  public byte getByte(String name)
  {
    Object val = _set.get(name);
    if (val == null)
      throw new IllegalArgumentException("Byte value required, but not specified");
    if ((val instanceof Number))
      return ((Number)val).byteValue();
    try {
      return Byte.parseByte((String)val); } catch (Exception e) {
    }
    throw new IllegalArgumentException("Byte value required, but found: " + val);
  }

  public short getShort(String name, short deflt)
  {
    Object val = _set.get(name);
    if (val == null)
      return deflt;
    if ((val instanceof Number))
      return ((Number)val).shortValue();
    try {
      return Short.parseShort((String)val); } catch (Exception e) {
    }
    throw new IllegalArgumentException("Short value required, but found: " + val);
  }

  public short getShort(String name)
  {
    Object val = _set.get(name);
    if (val == null)
      throw new IllegalArgumentException("Short value required, but not specified");
    if ((val instanceof Number))
      return ((Number)val).shortValue();
    try {
      return Short.parseShort((String)val); } catch (Exception e) {
    }
    throw new IllegalArgumentException("Short value required, but found: " + val);
  }

  public int getInteger(String name)
  {
    Object val = _set.get(name);
    if (val == null)
      throw new IllegalArgumentException("Integer value required, but not specified");
    if ((val instanceof Number))
      return ((Number)val).intValue();
    try {
      return Integer.parseInt((String)val); } catch (Exception e) {
    }
    throw new IllegalArgumentException("Integer value required, but found: " + val);
  }

  public int getInteger(String name, int deflt)
  {
    Object val = _set.get(name);
    if (val == null)
      return deflt;
    if ((val instanceof Number))
      return ((Number)val).intValue();
    try {
      return Integer.parseInt((String)val); } catch (Exception e) {
    }
    throw new IllegalArgumentException("Integer value required, but found: " + val);
  }

  public int[] getIntegerArray(String name)
  {
    Object val = _set.get(name);
    if (val == null)
      throw new IllegalArgumentException("Integer value required, but not specified");
    if ((val instanceof Number)) {
      int[] result = { ((Number)val).intValue() };
      return result;
    }
    int c = 0;
    String[] vals = ((String)val).split(";");
    int[] result = new int[vals.length];
    for (String v : vals) {
      try
      {
        result[c] = Integer.parseInt(v);
        c++;
      } catch (Exception e) {
        throw new IllegalArgumentException("Integer value required, but found: " + val);
      }
    }
    return result;
  }

  public long getLong(String name)
  {
    Object val = _set.get(name);
    if (val == null)
      throw new IllegalArgumentException("Integer value required, but not specified");
    if ((val instanceof Number))
      return ((Number)val).longValue();
    try {
      return Long.parseLong((String)val); } catch (Exception e) {
    }
    throw new IllegalArgumentException("Integer value required, but found: " + val);
  }

  public long getLong(String name, int deflt)
  {
    Object val = _set.get(name);
    if (val == null)
      return deflt;
    if ((val instanceof Number))
      return ((Number)val).longValue();
    try {
      return Long.parseLong((String)val); } catch (Exception e) {
    }
    throw new IllegalArgumentException("Integer value required, but found: " + val);
  }

  public float getFloat(String name)
  {
    Object val = _set.get(name);
    if (val == null)
      throw new IllegalArgumentException("Float value required, but not specified");
    if ((val instanceof Number))
      return ((Number)val).floatValue();
    try {
      return (float)Double.parseDouble((String)val); } catch (Exception e) {
    }
    throw new IllegalArgumentException("Float value required, but found: " + val);
  }

  public float getFloat(String name, float deflt)
  {
    Object val = _set.get(name);
    if (val == null)
      return deflt;
    if ((val instanceof Number))
      return ((Number)val).floatValue();
    try {
      return (float)Double.parseDouble((String)val); } catch (Exception e) {
    }
    throw new IllegalArgumentException("Float value required, but found: " + val);
  }

  public double getDouble(String name)
  {
    Object val = _set.get(name);
    if (val == null)
      throw new IllegalArgumentException("Float value required, but not specified");
    if ((val instanceof Number))
      return ((Number)val).doubleValue();
    try {
      return Double.parseDouble((String)val); } catch (Exception e) {
    }
    throw new IllegalArgumentException("Float value required, but found: " + val);
  }

  public double getDouble(String name, float deflt)
  {
    Object val = _set.get(name);
    if (val == null)
      return deflt;
    if ((val instanceof Number))
      return ((Number)val).doubleValue();
    try {
      return Double.parseDouble((String)val); } catch (Exception e) {
    }
    throw new IllegalArgumentException("Float value required, but found: " + val);
  }

  public String getString(String name)
  {
    Object val = _set.get(name);
    if (val == null)
      throw new IllegalArgumentException("String value required, but not specified");
    return String.valueOf(val);
  }

  public String getString(String name, String deflt)
  {
    Object val = _set.get(name);
    if (val == null)
      return deflt;
    return String.valueOf(val);
  }

  public <T extends Enum<T>> T getEnum(String name, Class<T> enumClass)
  {
    Object val = _set.get(name);
    if (val == null)
      throw new IllegalArgumentException("Enum value of type " + enumClass.getName() + " required, but not specified");
    if (enumClass.isInstance(val))
      return (Enum)val;
    try {
      return Enum.valueOf(enumClass, String.valueOf(val)); } catch (Exception e) {
    }
    throw new IllegalArgumentException("Enum value of type " + enumClass.getName() + "required, but found: " + val);
  }

  public <T extends Enum<T>> T getEnum(String name, Class<T> enumClass, T deflt)
  {
    Object val = _set.get(name);
    if (val == null)
      return deflt;
    if (enumClass.isInstance(val))
      return (Enum)val;
    try {
      return Enum.valueOf(enumClass, String.valueOf(val)); } catch (Exception e) {
    }
    throw new IllegalArgumentException("Enum value of type " + enumClass.getName() + "required, but found: " + val);
  }

  public void set(String name, String value)
  {
    _set.put(name, value);
  }

  public void set(String name, boolean value)
  {
    _set.put(name, Boolean.valueOf(value));
  }

  public void set(String name, int value)
  {
    _set.put(name, Integer.valueOf(value));
  }

  public void set(String name, double value)
  {
    _set.put(name, Double.valueOf(value));
  }

  public void set(String name, long value)
  {
    _set.put(name, Long.valueOf(value));
  }

  public void set(String name, Enum value)
  {
    _set.put(name, value);
  }

  public long getLong(String name, long deflt)
  {
    Object val = _set.get(name);
    if (val == null)
      return deflt;
    if ((val instanceof Number))
      return ((Number)val).longValue();
    try
    {
      return Long.parseLong((String)val);
    }
    catch (Exception e) {
    }
    throw new IllegalArgumentException("Integer value required, but found: " + val);
  }

  public void unset(String name)
  {
    _set.remove(name);
  }
}
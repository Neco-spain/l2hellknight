package net.sf.l2j.gameserver.util;

public final class StringUtil
{
  public static String concat(String[] strings)
  {
    StringBuilder sbString = new StringBuilder(getLength(strings));

    for (String string : strings)
    {
      sbString.append(string);
    }

    return sbString.toString();
  }

  public static StringBuilder startAppend(int sizeHint, String[] strings)
  {
    int length = getLength(strings);
    StringBuilder sbString = new StringBuilder(sizeHint > length ? sizeHint : length);

    for (String string : strings)
    {
      sbString.append(string);
    }

    return sbString;
  }

  public static void append(StringBuilder sbString, String[] strings)
  {
    sbString.ensureCapacity(sbString.length() + getLength(strings));

    for (String string : strings)
    {
      sbString.append(string);
    }
  }

  private static int getLength(String[] strings)
  {
    int length = 0;

    for (String string : strings)
    {
      length += string.length();
    }

    return length;
  }
}
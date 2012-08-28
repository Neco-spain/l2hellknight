package l2m.commons.util;

import javolution.text.TextBuilder;

public final class StringUtil
{
  public static String concat(String[] strings)
  {
    TextBuilder sbString = TextBuilder.newInstance();

    for (String string : strings)
    {
      sbString.append(string);
    }

    String result = sbString.toString();
    TextBuilder.recycle(sbString);
    return result;
  }

  public static StringBuilder startAppend(int sizeHint, String[] strings) {
    int length = getLength(strings);
    StringBuilder sbString = new StringBuilder(sizeHint > length ? sizeHint : length);

    for (String string : strings)
    {
      sbString.append(string);
    }

    return sbString;
  }

  public static void append(StringBuilder sbString, String[] strings) {
    sbString.ensureCapacity(sbString.length() + getLength(strings));

    for (String string : strings)
    {
      sbString.append(string);
    }
  }

  private static int getLength(String[] strings) {
    int length = 0;

    for (String string : strings)
    {
      if (string == null)
        length += 4;
      else {
        length += string.length();
      }
    }
    return length;
  }

  public static String getTraceString(StackTraceElement[] trace) {
    TextBuilder sbString = TextBuilder.newInstance();
    for (StackTraceElement element : trace)
    {
      sbString.append(element.toString()).append("\n");
    }

    String result = sbString.toString();
    TextBuilder.recycle(sbString);
    return result;
  }
}
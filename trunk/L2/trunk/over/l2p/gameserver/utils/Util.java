package l2p.gameserver.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import l2p.commons.util.Rnd;
import l2p.gameserver.Config;

public class Util
{
  static final String PATTERN = "0.0000000000E00";
  static final DecimalFormat df;
  private static NumberFormat adenaFormatter = NumberFormat.getIntegerInstance(Locale.FRANCE);
  private static Pattern _pattern;

  public static boolean isMatchingRegexp(String text, String template)
  {
    Pattern pattern = null;
    try
    {
      pattern = Pattern.compile(template);
    }
    catch (PatternSyntaxException e)
    {
      e.printStackTrace();
    }
    if (pattern == null)
      return false;
    Matcher regexp = pattern.matcher(text);
    return regexp.matches();
  }

  public static String formatDouble(double x, String nanString, boolean forceExponents)
  {
    if (Double.isNaN(x))
      return nanString;
    if (forceExponents)
      return df.format(x);
    if (()x == x)
      return String.valueOf(()x);
    return String.valueOf(x);
  }

  public static String formatAdena(long amount)
  {
    return adenaFormatter.format(amount);
  }

  public static String formatTime(int time)
  {
    if (time == 0)
      return "now";
    time = Math.abs(time);
    String ret = "";
    long numDays = time / 86400;
    time = (int)(time - numDays * 86400L);
    long numHours = time / 3600;
    time = (int)(time - numHours * 3600L);
    long numMins = time / 60;
    time = (int)(time - numMins * 60L);
    long numSeconds = time;
    if (numDays > 0L)
      ret = new StringBuilder().append(ret).append(numDays).append("d ").toString();
    if (numHours > 0L)
      ret = new StringBuilder().append(ret).append(numHours).append("h ").toString();
    if (numMins > 0L)
      ret = new StringBuilder().append(ret).append(numMins).append("m ").toString();
    if (numSeconds > 0L)
      ret = new StringBuilder().append(ret).append(numSeconds).append("s").toString();
    return ret.trim();
  }

  public static long rollDrop(long min, long max, double calcChance, boolean rate)
  {
    if ((calcChance <= 0.0D) || (min <= 0L) || (max <= 0L))
      return 0L;
    int dropmult = 1;
    if (rate)
      calcChance *= Config.RATE_DROP_ITEMS;
    if (calcChance > 1000000.0D)
      if (calcChance % 1000000.0D == 0.0D) {
        dropmult = (int)(calcChance / 1000000.0D);
      }
      else {
        dropmult = (int)Math.ceil(calcChance / 1000000.0D);
        calcChance /= dropmult;
      }
    return Rnd.chance(calcChance / 10000.0D) ? Rnd.get(min * dropmult, max * dropmult) : 0L;
  }

  public static int packInt(int[] a, int bits) throws Exception
  {
    int m = 32 / bits;
    if (a.length > m) {
      throw new Exception("Overflow");
    }
    int result = 0;

    int mval = (int)Math.pow(2.0D, bits);
    for (int i = 0; i < m; i++)
    {
      result <<= bits;
      int next;
      if (a.length > i)
      {
        int next = a[i];
        if ((next >= mval) || (next < 0))
          throw new Exception("Overload, value is out of range");
      }
      else {
        next = 0;
      }result += next;
    }
    return result;
  }

  public static long packLong(int[] a, int bits) throws Exception
  {
    int m = 64 / bits;
    if (a.length > m) {
      throw new Exception("Overflow");
    }
    long result = 0L;

    int mval = (int)Math.pow(2.0D, bits);
    for (int i = 0; i < m; i++)
    {
      result <<= bits;
      int next;
      if (a.length > i)
      {
        int next = a[i];
        if ((next >= mval) || (next < 0))
          throw new Exception("Overload, value is out of range");
      }
      else {
        next = 0;
      }result += next;
    }
    return result;
  }

  public static int[] unpackInt(int a, int bits)
  {
    int m = 32 / bits;
    int mval = (int)Math.pow(2.0D, bits);
    int[] result = new int[m];

    for (int i = m; i > 0; i--)
    {
      int next = a;
      a >>= bits;
      result[(i - 1)] = (next - a * mval);
    }
    return result;
  }

  public static int[] unpackLong(long a, int bits)
  {
    int m = 64 / bits;
    int mval = (int)Math.pow(2.0D, bits);
    int[] result = new int[m];

    for (int i = m; i > 0; i--)
    {
      long next = a;
      a >>= bits;
      result[(i - 1)] = (int)(next - a * mval);
    }
    return result;
  }

  public static String joinStrings(String glueStr, String[] strings, int startIdx, int maxCount)
  {
    return Strings.joinStrings(glueStr, strings, startIdx, maxCount);
  }

  public static String joinStrings(String glueStr, String[] strings, int startIdx)
  {
    return Strings.joinStrings(glueStr, strings, startIdx, -1);
  }

  public static boolean isNumber(String s)
  {
    try
    {
      Double.parseDouble(s);
    }
    catch (NumberFormatException e)
    {
      return false;
    }
    return true;
  }

  public static String dumpObject(Object o, boolean simpleTypes, boolean parentFields, boolean ignoreStatics)
  {
    Class cls = o.getClass();
    String result = new StringBuilder().append("[").append(simpleTypes ? cls.getSimpleName() : cls.getName()).append("\n").toString();

    List fields = new ArrayList();
    while (cls != null)
    {
      for (Field fld : cls.getDeclaredFields()) {
        if (fields.contains(fld))
          continue;
        if ((ignoreStatics) && (Modifier.isStatic(fld.getModifiers())))
          continue;
        fields.add(fld);
      }
      cls = cls.getSuperclass();
      if (!parentFields) {
        break;
      }
    }
    for (Field fld : fields) {
      fld.setAccessible(true);
      String val;
      try { Object fldObj = fld.get(o);
        String val;
        if (fldObj == null)
          val = "NULL";
        else
          val = fldObj.toString();
      }
      catch (Throwable e)
      {
        e.printStackTrace();
        val = "<ERROR>";
      }
      String type = simpleTypes ? fld.getType().getSimpleName() : fld.getType().toString();

      result = new StringBuilder().append(result).append(String.format("\t%s [%s] = %s;\n", new Object[] { fld.getName(), type, val })).toString();
    }

    result = new StringBuilder().append(result).append("]\n").toString();
    return result;
  }

  public static HashMap<Integer, String> parseTemplate(String html)
  {
    Matcher m = _pattern.matcher(html);
    HashMap tpls = new HashMap();
    while (m.find())
    {
      tpls.put(Integer.valueOf(Integer.parseInt(m.group(1))), m.group(2));
      html = html.replace(m.group(0), "");
    }

    tpls.put(Integer.valueOf(0), html);
    return tpls;
  }

  static
  {
    df = (DecimalFormat)NumberFormat.getNumberInstance(Locale.ENGLISH);
    df.applyPattern("0.0000000000E00");
    df.setPositivePrefix("+");

    _pattern = Pattern.compile("<!--TEMPLET(\\d+)(.*?)TEMPLET-->", 32);
  }
}
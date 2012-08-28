package net.sf.l2j.util;

import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sf.l2j.Base64;
import net.sf.l2j.Config;

public class Strings
{
  private static Logger _log = Logger.getLogger(Strings.class.getName());

  private static final char[] hex = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
  private static String[] tr;
  private static String[] trb;

  public static String bytesToString(byte[] b)
  {
    String ret = "";
    for (byte element : b)
    {
      ret = ret + String.valueOf(hex[((element & 0xF0) >> 4)]);
      ret = ret + String.valueOf(hex[(element & 0xF)]);
    }
    return ret;
  }

  public static String addSlashes(String s)
  {
    if (s == null) {
      return "";
    }
    s = s.replace("\\", "\\\\");
    s = s.replace("\"", "\\\"");
    s = s.replace("@", "\\@");
    s = s.replace("'", "\\'");
    return s;
  }

  public static String stripSlashes(String s)
  {
    if (s == null)
      return "";
    s = s.replace("\\'", "'");
    s = s.replace("\\\\", "\\");
    return s;
  }

  public static Integer parseInt(Object x)
  {
    if (x == null) {
      return Integer.valueOf(0);
    }
    if ((x instanceof Integer)) {
      return (Integer)x;
    }
    if ((x instanceof Double)) {
      return Integer.valueOf(((Double)x).intValue());
    }
    if ((x instanceof Boolean)) {
      return Integer.valueOf(((Boolean)x).booleanValue() ? -1 : 0);
    }
    Integer res = Integer.valueOf(0);
    try
    {
      res = Integer.valueOf(Integer.parseInt("" + x));
    }
    catch (Exception e) {
    }
    return res;
  }

  public static Double parseFloat(Object x)
  {
    if ((x instanceof Double)) {
      return (Double)x;
    }
    if ((x instanceof Integer)) {
      return Double.valueOf(0.0D + ((Integer)x).intValue());
    }
    if (x == null) {
      return Double.valueOf(0.0D);
    }
    Double res = Double.valueOf(0.0D);
    try
    {
      res = Double.valueOf(Double.parseDouble("" + x));
    }
    catch (Exception e) {
    }
    return res;
  }

  public static Boolean parseBoolean(Object x)
  {
    if ((x instanceof Integer)) {
      return Boolean.valueOf(((Integer)x).intValue() != 0);
    }
    if (x == null) {
      return Boolean.valueOf(false);
    }
    if ((x instanceof Boolean)) {
      return (Boolean)x;
    }
    if ((x instanceof Double)) {
      return Boolean.valueOf(Math.abs(((Double)x).doubleValue()) < 1.E-005D);
    }
    return Boolean.valueOf(!("" + x).equals(""));
  }

  public static void reload()
  {
    Files.cacheClean();
    String[] pairs = Files.read("data/translit.txt").split("\n");
    tr = new String[pairs.length * 2];
    for (int i = 0; i < pairs.length; i++)
    {
      String[] ss = pairs[i].split(" +");
      tr[(i * 2)] = ss[0];
      tr[(i * 2 + 1)] = ss[1];
    }

    pairs = Files.read("data/translit_back.txt").split("\n");
    trb = new String[pairs.length * 2];
    for (int i = 0; i < pairs.length; i++)
    {
      String[] ss = pairs[i].split(" +");
      trb[(i * 2)] = ss[0];
      trb[(i * 2 + 1)] = ss[1];
    }
  }

  public static String translit(String s)
  {
    for (int i = 0; i < tr.length; i += 2) {
      s = s.replace(tr[i], tr[(i + 1)]);
    }
    return s;
  }

  public static String fromTranslit(String s)
  {
    for (int i = 0; i < trb.length; i += 2) {
      s = s.replace(trb[i], trb[(i + 1)]);
    }
    return s;
  }

  public static String replace(String str, String regex, int flags, String replace)
  {
    return Pattern.compile(regex, flags).matcher(str).replaceAll(replace);
  }

  public static boolean matches(String str, String regex, int flags)
  {
    return Pattern.compile(regex, flags).matcher(str).matches();
  }

  public static String bbParse(String s)
  {
    if (s == null) {
      return null;
    }
    s = s.replace("\r", "");

    if (Config.DEBUG) {
      _log.info("Parse string\n==========================\n" + s + "\n==========================\n");
    }
    s = s.replaceAll("(\\s|\"|'|\\(|^|\n)\\*(.*?)\\*(\\s|\"|'|\\)|\\?|\\.|!|:|;|,|$|\n)", "$1<font color=\"LEVEL\">$2</font>$3");

    s = replace(s, "^!(.*?)$", 8, "<font color=\"LEVEL\">$1</font>\n\n");

    s = s.replaceAll("%%\\s*\n", "<br1>");

    s = s.replaceAll("\n\n+", "<br>");

    s = replace(s, "\\[([^\\]\\|]*?)\\|([^\\]]*?)\\]", 32, "<a action=\"bypass -h $1\">$2</a>");

    s = s.replaceAll(" @", "\" msg=\"");

    if (Config.DEBUG) {
      _log.info("to \n==========================\n" + s + "\n==========================\n");
    }
    return s;
  }

  public static String utf2win(String utfString)
  {
    String winString;
    try {
      winString = new String(utfString.getBytes("Cp1251"));
    }
    catch (UnsupportedEncodingException uee)
    {
      winString = utfString;
    }
    return winString;
  }

  public static String getText(String string)
  {
    try
    {
      return new String(Base64.decode(string), "UTF-8");
    }
    catch (UnsupportedEncodingException e) {
    }
    return null;
  }
}
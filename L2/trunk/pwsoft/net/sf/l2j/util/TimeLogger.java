package net.sf.l2j.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeLogger
{
  private static Date _date = new Date();
  private static SimpleDateFormat _sdf = new SimpleDateFormat("dd (HH:mm:ss)");

  public static String getLogTime()
  {
    return "#";
  }

  public static String getTime()
  {
    return _sdf.format(_date) + "#";
  }
}
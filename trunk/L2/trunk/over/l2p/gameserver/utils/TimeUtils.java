package l2p.gameserver.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeUtils
{
  private static final SimpleDateFormat SIMPLE_FORMAT = new SimpleDateFormat("HH:mm dd.MM.yyyy");

  public static String toSimpleFormat(Calendar cal)
  {
    return SIMPLE_FORMAT.format(cal.getTime());
  }

  public static String toSimpleFormat(long cal)
  {
    return SIMPLE_FORMAT.format(Long.valueOf(cal));
  }
}
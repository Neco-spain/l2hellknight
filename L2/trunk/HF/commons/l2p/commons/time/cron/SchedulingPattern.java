package l2m.commons.time.cron;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;

public class SchedulingPattern
{
  private static final int MINUTE_MIN_VALUE = 0;
  private static final int MINUTE_MAX_VALUE = 59;
  private static final int HOUR_MIN_VALUE = 0;
  private static final int HOUR_MAX_VALUE = 23;
  private static final int DAY_OF_MONTH_MIN_VALUE = 1;
  private static final int DAY_OF_MONTH_MAX_VALUE = 31;
  private static final int MONTH_MIN_VALUE = 1;
  private static final int MONTH_MAX_VALUE = 12;
  private static final int DAY_OF_WEEK_MIN_VALUE = 0;
  private static final int DAY_OF_WEEK_MAX_VALUE = 7;
  private static final ValueParser MINUTE_VALUE_PARSER = new MinuteValueParser();

  private static final ValueParser HOUR_VALUE_PARSER = new HourValueParser();

  private static final ValueParser DAY_OF_MONTH_VALUE_PARSER = new DayOfMonthValueParser();

  private static final ValueParser MONTH_VALUE_PARSER = new MonthValueParser();

  private static final ValueParser DAY_OF_WEEK_VALUE_PARSER = new DayOfWeekValueParser();
  private String asString;
  protected List<ValueMatcher> minuteMatchers = new ArrayList();

  protected List<ValueMatcher> hourMatchers = new ArrayList();

  protected List<ValueMatcher> dayOfMonthMatchers = new ArrayList();

  protected List<ValueMatcher> monthMatchers = new ArrayList();

  protected List<ValueMatcher> dayOfWeekMatchers = new ArrayList();

  protected int matcherSize = 0;

  public static boolean validate(String schedulingPattern)
  {
    try
    {
      new SchedulingPattern(schedulingPattern);
    } catch (InvalidPatternException e) {
      return false;
    }
    return true;
  }

  public SchedulingPattern(String pattern)
    throws SchedulingPattern.InvalidPatternException
  {
    asString = pattern;
    StringTokenizer st1 = new StringTokenizer(pattern, "|");
    if (st1.countTokens() < 1) {
      throw new InvalidPatternException("invalid pattern: \"" + pattern + "\"");
    }
    while (st1.hasMoreTokens()) {
      String localPattern = st1.nextToken();
      StringTokenizer st2 = new StringTokenizer(localPattern, " \t");
      if (st2.countTokens() != 5)
        throw new InvalidPatternException("invalid pattern: \"" + localPattern + "\"");
      try
      {
        minuteMatchers.add(buildValueMatcher(st2.nextToken(), MINUTE_VALUE_PARSER));
      } catch (Exception e) {
        throw new InvalidPatternException("invalid pattern \"" + localPattern + "\". Error parsing minutes field: " + e.getMessage() + ".");
      }

      try
      {
        hourMatchers.add(buildValueMatcher(st2.nextToken(), HOUR_VALUE_PARSER));
      } catch (Exception e) {
        throw new InvalidPatternException("invalid pattern \"" + localPattern + "\". Error parsing hours field: " + e.getMessage() + ".");
      }

      try
      {
        dayOfMonthMatchers.add(buildValueMatcher(st2.nextToken(), DAY_OF_MONTH_VALUE_PARSER));
      } catch (Exception e) {
        throw new InvalidPatternException("invalid pattern \"" + localPattern + "\". Error parsing days of month field: " + e.getMessage() + ".");
      }

      try
      {
        monthMatchers.add(buildValueMatcher(st2.nextToken(), MONTH_VALUE_PARSER));
      } catch (Exception e) {
        throw new InvalidPatternException("invalid pattern \"" + localPattern + "\". Error parsing months field: " + e.getMessage() + ".");
      }

      try
      {
        dayOfWeekMatchers.add(buildValueMatcher(st2.nextToken(), DAY_OF_WEEK_VALUE_PARSER));
      } catch (Exception e) {
        throw new InvalidPatternException("invalid pattern \"" + localPattern + "\". Error parsing days of week field: " + e.getMessage() + ".");
      }

      matcherSize += 1;
    }
  }

  private ValueMatcher buildValueMatcher(String str, ValueParser parser)
    throws Exception
  {
    if ((str.length() == 1) && (str.equals("*"))) {
      return new AlwaysTrueValueMatcher(null);
    }
    List values = new ArrayList();
    StringTokenizer st = new StringTokenizer(str, ",");
    Iterator i;
    while (st.hasMoreTokens()) { String element = st.nextToken();
      List local;
      try { local = parseListElement(element, parser);
      } catch (Exception e) {
        throw new Exception("invalid field \"" + str + "\", invalid element \"" + element + "\", " + e.getMessage());
      }

      for (i = local.iterator(); i.hasNext(); ) {
        Integer value = (Integer)i.next();
        if (!values.contains(value)) {
          values.add(value);
        }
      }
    }
    if (values.size() == 0) {
      throw new Exception("invalid field \"" + str + "\"");
    }
    if (parser == DAY_OF_MONTH_VALUE_PARSER) {
      return new DayOfMonthValueMatcher(values);
    }
    return new IntArrayValueMatcher(values);
  }

  private List<Integer> parseListElement(String str, ValueParser parser)
    throws Exception
  {
    StringTokenizer st = new StringTokenizer(str, "/");
    int size = st.countTokens();
    if ((size < 1) || (size > 2))
      throw new Exception("syntax error");
    List values;
    try {
      values = parseRange(st.nextToken(), parser);
    } catch (Exception e) {
      throw new Exception("invalid range, " + e.getMessage());
    }
    if (size == 2) { String dStr = st.nextToken();
      int div;
      try { div = Integer.parseInt(dStr);
      } catch (NumberFormatException e) {
        throw new Exception("invalid divisor \"" + dStr + "\"");
      }
      if (div < 1) {
        throw new Exception("non positive divisor \"" + div + "\"");
      }
      List values2 = new ArrayList();
      for (int i = 0; i < values.size(); i += div) {
        values2.add(values.get(i));
      }
      return values2;
    }
    return values;
  }

  private List<Integer> parseRange(String str, ValueParser parser)
    throws Exception
  {
    if (str.equals("*")) {
      int min = parser.getMinValue();
      int max = parser.getMaxValue();
      List values = new ArrayList();
      for (int i = min; i <= max; i++) {
        values.add(new Integer(i));
      }
      return values;
    }
    StringTokenizer st = new StringTokenizer(str, "-");
    int size = st.countTokens();
    if ((size < 1) || (size > 2)) {
      throw new Exception("syntax error");
    }String v1Str = st.nextToken();
    int v1;
    try { v1 = parser.parse(v1Str);
    } catch (Exception e) {
      throw new Exception("invalid value \"" + v1Str + "\", " + e.getMessage());
    }

    if (size == 1) {
      List values = new ArrayList();
      values.add(new Integer(v1));
      return values;
    }String v2Str = st.nextToken();
    int v2;
    try { v2 = parser.parse(v2Str);
    } catch (Exception e) {
      throw new Exception("invalid value \"" + v2Str + "\", " + e.getMessage());
    }

    List values = new ArrayList();
    if (v1 < v2) {
      for (int i = v1; i <= v2; i++)
        values.add(new Integer(i));
    }
    else if (v1 > v2) {
      int min = parser.getMinValue();
      int max = parser.getMaxValue();
      for (int i = v1; i <= max; i++) {
        values.add(new Integer(i));
      }
      for (int i = min; i <= v2; i++)
        values.add(new Integer(i));
    }
    else
    {
      values.add(new Integer(v1));
    }
    return values;
  }

  public boolean match(TimeZone timezone, long millis)
  {
    GregorianCalendar gc = new GregorianCalendar(timezone);
    gc.setTimeInMillis(millis);
    gc.set(13, 0);
    gc.set(14, 0);

    int minute = gc.get(12);
    int hour = gc.get(11);
    int dayOfMonth = gc.get(5);
    int month = gc.get(2) + 1;
    int dayOfWeek = gc.get(7) - 1;
    int year = gc.get(1);

    for (int i = 0; i < matcherSize; i++) {
      ValueMatcher minuteMatcher = (ValueMatcher)minuteMatchers.get(i);
      ValueMatcher hourMatcher = (ValueMatcher)hourMatchers.get(i);
      ValueMatcher dayOfMonthMatcher = (ValueMatcher)dayOfMonthMatchers.get(i);
      ValueMatcher monthMatcher = (ValueMatcher)monthMatchers.get(i);
      ValueMatcher dayOfWeekMatcher = (ValueMatcher)dayOfWeekMatchers.get(i);
      boolean eval = (minuteMatcher.match(minute)) && (hourMatcher.match(hour)) && ((dayOfMonthMatcher instanceof DayOfMonthValueMatcher) ? ((DayOfMonthValueMatcher)dayOfMonthMatcher).match(dayOfMonth, month, gc.isLeapYear(year)) : dayOfMonthMatcher.match(dayOfMonth)) && (monthMatcher.match(month)) && (dayOfWeekMatcher.match(dayOfWeek));

      if (eval) {
        return true;
      }
    }
    return false;
  }

  public boolean match(long millis)
  {
    return match(TimeZone.getDefault(), millis);
  }

  public long next(TimeZone timezone, long millis)
  {
    long next = -1L;

    label489: for (int i = 0; i < matcherSize; i++)
    {
      GregorianCalendar gc = new GregorianCalendar(timezone);
      gc.setTimeInMillis(millis);
      gc.set(13, 0);
      gc.set(14, 0);

      ValueMatcher minuteMatcher = (ValueMatcher)minuteMatchers.get(i);
      ValueMatcher hourMatcher = (ValueMatcher)hourMatchers.get(i);
      ValueMatcher dayOfMonthMatcher = (ValueMatcher)dayOfMonthMatchers.get(i);
      ValueMatcher monthMatcher = (ValueMatcher)monthMatchers.get(i);
      ValueMatcher dayOfWeekMatcher = (ValueMatcher)dayOfWeekMatchers.get(i);
      while (true)
      {
        int year = gc.get(1);
        boolean isLeapYear = gc.isLeapYear(year);

        for (int month = gc.get(2) + 1; month <= 12; month++)
        {
          if (monthMatcher.match(month))
          {
            gc.set(2, month - 1);
            int maxDayOfMonth = DayOfMonthValueMatcher.getLastDayOfMonth(month, isLeapYear);
            for (int dayOfMonth = gc.get(5); dayOfMonth <= maxDayOfMonth; dayOfMonth++)
            {
              if ((dayOfMonthMatcher instanceof DayOfMonthValueMatcher) ? ((DayOfMonthValueMatcher)dayOfMonthMatcher).match(dayOfMonth, month, isLeapYear) : dayOfMonthMatcher.match(dayOfMonth))
              {
                gc.set(5, dayOfMonth);
                int dayOfWeek = gc.get(7) - 1;
                if (dayOfWeekMatcher.match(dayOfWeek))
                {
                  for (int hour = gc.get(11); hour <= 23; hour++)
                  {
                    if (hourMatcher.match(hour))
                    {
                      gc.set(11, hour);
                      for (int minute = gc.get(12); minute <= 59; minute++)
                      {
                        if (!minuteMatcher.match(minute))
                          continue;
                        gc.set(12, minute);
                        long next0 = gc.getTimeInMillis();
                        if ((next != -1L) && (next0 >= next)) break label489; next = next0; break label489;
                      }

                    }

                    gc.set(12, 0);
                  }
                }
              }
              gc.set(11, 0);
              gc.set(12, 0);
            }
          }
          gc.set(5, 1);
          gc.set(11, 0);
          gc.set(12, 0);
        }
        gc.set(2, 0);
        gc.set(11, 0);
        gc.set(12, 0);
        gc.roll(1, true);
      }
    }

    return next;
  }

  public long next(long millis)
  {
    return next(TimeZone.getDefault(), millis);
  }

  public String toString()
  {
    return asString;
  }

  private static int parseAlias(String value, String[] aliases, int offset)
    throws Exception
  {
    for (int i = 0; i < aliases.length; i++) {
      if (aliases[i].equalsIgnoreCase(value)) {
        return offset + i;
      }
    }
    throw new Exception("invalid alias \"" + value + "\"");
  }

  private static class DayOfMonthValueMatcher extends SchedulingPattern.IntArrayValueMatcher
  {
    private static final int[] lastDays = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

    public DayOfMonthValueMatcher(List<Integer> integers)
    {
      super();
    }

    public boolean match(int value, int month, boolean isLeapYear)
    {
      return (super.match(value)) || ((value > 27) && (match(32)) && (isLastDayOfMonth(value, month, isLeapYear)));
    }

    public static int getLastDayOfMonth(int month, boolean isLeapYear) {
      if ((isLeapYear) && (month == 2)) {
        return 29;
      }
      return lastDays[(month - 1)];
    }

    public static boolean isLastDayOfMonth(int value, int month, boolean isLeapYear)
    {
      return value == getLastDayOfMonth(month, isLeapYear);
    }
  }

  private static class IntArrayValueMatcher
    implements SchedulingPattern.ValueMatcher
  {
    private int[] values;

    public IntArrayValueMatcher(List<Integer> integers)
    {
      int size = integers.size();
      values = new int[size];
      for (int i = 0; i < size; i++)
        try {
          values[i] = ((Integer)integers.get(i)).intValue();
        } catch (Exception e) {
          throw new IllegalArgumentException(e.getMessage());
        }
    }

    public boolean match(int value)
    {
      for (int i = 0; i < values.length; i++) {
        if (values[i] == value) {
          return true;
        }
      }
      return false;
    }
  }

  private static class AlwaysTrueValueMatcher
    implements SchedulingPattern.ValueMatcher
  {
    public boolean match(int value)
    {
      return true;
    }
  }

  private static abstract interface ValueMatcher
  {
    public abstract boolean match(int paramInt);
  }

  private static class DayOfWeekValueParser extends SchedulingPattern.SimpleValueParser
  {
    private static String[] ALIASES = { "sun", "mon", "tue", "wed", "thu", "fri", "sat" };

    public DayOfWeekValueParser()
    {
      super(7);
    }

    public int parse(String value) throws Exception
    {
      try {
        return super.parse(value) % 7;
      } catch (Exception e) {
      }
      return SchedulingPattern.access$100(value, ALIASES, 0);
    }
  }

  private static class MonthValueParser extends SchedulingPattern.SimpleValueParser
  {
    private static String[] ALIASES = { "jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec" };

    public MonthValueParser()
    {
      super(12);
    }

    public int parse(String value) throws Exception
    {
      try {
        return super.parse(value);
      } catch (Exception e) {
      }
      return SchedulingPattern.access$100(value, ALIASES, 1);
    }
  }

  private static class DayOfMonthValueParser extends SchedulingPattern.SimpleValueParser
  {
    public DayOfMonthValueParser()
    {
      super(31);
    }

    public int parse(String value)
      throws Exception
    {
      if (value.equalsIgnoreCase("L")) {
        return 32;
      }
      return super.parse(value);
    }
  }

  private static class HourValueParser extends SchedulingPattern.SimpleValueParser
  {
    public HourValueParser()
    {
      super(23);
    }
  }

  private static class MinuteValueParser extends SchedulingPattern.SimpleValueParser
  {
    public MinuteValueParser()
    {
      super(59);
    }
  }

  private static class SimpleValueParser
    implements SchedulingPattern.ValueParser
  {
    protected int minValue;
    protected int maxValue;

    public SimpleValueParser(int minValue, int maxValue)
    {
      this.minValue = minValue;
      this.maxValue = maxValue;
    }
    public int parse(String value) throws Exception {
      int i;
      try {
        i = Integer.parseInt(value);
      } catch (NumberFormatException e) {
        throw new Exception("invalid integer value");
      }
      if ((i < minValue) || (i > maxValue)) {
        throw new Exception("value out of range");
      }
      return i;
    }

    public int getMinValue() {
      return minValue;
    }

    public int getMaxValue() {
      return maxValue;
    }
  }

  private static abstract interface ValueParser
  {
    public abstract int parse(String paramString)
      throws Exception;

    public abstract int getMinValue();

    public abstract int getMaxValue();
  }

  public class InvalidPatternException extends RuntimeException
  {
    private static final long serialVersionUID = 1L;

    InvalidPatternException()
    {
    }

    InvalidPatternException(String message)
    {
      super();
    }
  }
}
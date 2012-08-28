package l2m.commons.math;

public class SafeMath
{
  public static int addAndCheck(int a, int b)
    throws ArithmeticException
  {
    return addAndCheck(a, b, "overflow: add", false);
  }

  public static int addAndLimit(int a, int b)
  {
    return addAndCheck(a, b, null, true);
  }

  private static int addAndCheck(int a, int b, String msg, boolean limit)
  {
    int ret;
    if (a > b)
    {
      ret = addAndCheck(b, a, msg, limit);
    }
    else
    {
      int ret;
      if (a < 0) {
        if (b < 0)
        {
          int ret;
          if (-2147483648 - b <= a) {
            ret = a + b;
          }
          else
          {
            int ret;
            if (limit)
              ret = -2147483648;
            else
              throw new ArithmeticException(msg);
          }
        }
        else {
          ret = a + b;
        }
      }
      else
      {
        int ret;
        if (a <= 2147483647 - b) {
          ret = a + b;
        }
        else
        {
          int ret;
          if (limit)
            ret = 2147483647;
          else
            throw new ArithmeticException(msg);
        }
      }
    }
    int ret;
    return ret;
  }

  public static long addAndLimit(long a, long b)
  {
    return addAndCheck(a, b, "overflow: add", true);
  }

  public static long addAndCheck(long a, long b)
    throws ArithmeticException
  {
    return addAndCheck(a, b, "overflow: add", false);
  }

  private static long addAndCheck(long a, long b, String msg, boolean limit)
  {
    long ret;
    if (a > b)
    {
      ret = addAndCheck(b, a, msg, limit);
    }
    else
    {
      long ret;
      if (a < 0L) {
        if (b < 0L)
        {
          long ret;
          if (-9223372036854775808L - b <= a) {
            ret = a + b;
          }
          else
          {
            long ret;
            if (limit)
              ret = -9223372036854775808L;
            else
              throw new ArithmeticException(msg);
          }
        }
        else {
          ret = a + b;
        }
      }
      else
      {
        long ret;
        if (a <= 9223372036854775807L - b) {
          ret = a + b;
        }
        else
        {
          long ret;
          if (limit)
            ret = 9223372036854775807L;
          else
            throw new ArithmeticException(msg);
        }
      }
    }
    long ret;
    return ret;
  }

  public static int mulAndCheck(int a, int b)
    throws ArithmeticException
  {
    return mulAndCheck(a, b, "overflow: mul", false);
  }

  public static int mulAndLimit(int a, int b)
  {
    return mulAndCheck(a, b, "overflow: mul", true);
  }

  private static int mulAndCheck(int a, int b, String msg, boolean limit)
  {
    int ret;
    int ret;
    if (a > b)
    {
      ret = mulAndCheck(b, a, msg, limit);
    }
    else
    {
      int ret;
      if (a < 0) {
        if (b < 0)
        {
          int ret;
          if (a >= 2147483647 / b) {
            ret = a * b;
          }
          else
          {
            int ret;
            if (limit)
              ret = 2147483647;
            else
              throw new ArithmeticException(msg); 
          }
        } else if (b > 0)
        {
          int ret;
          if (-2147483648 / b <= a) {
            ret = a * b;
          }
          else
          {
            int ret;
            if (limit)
              ret = -2147483648;
            else
              throw new ArithmeticException(msg);
          }
        } else {
          ret = 0;
        }
      } else if (a > 0)
      {
        int ret;
        if (a <= 2147483647 / b) {
          ret = a * b;
        }
        else
        {
          int ret;
          if (limit)
            ret = 2147483647;
          else
            throw new ArithmeticException(msg);
        }
      } else {
        ret = 0;
      }
    }return ret;
  }

  public static long mulAndCheck(long a, long b)
    throws ArithmeticException
  {
    return mulAndCheck(a, b, "overflow: mul", false);
  }

  public static long mulAndLimit(long a, long b)
  {
    return mulAndCheck(a, b, "overflow: mul", true);
  }

  private static long mulAndCheck(long a, long b, String msg, boolean limit)
  {
    long ret;
    long ret;
    if (a > b)
    {
      ret = mulAndCheck(b, a, msg, limit);
    }
    else
    {
      long ret;
      if (a < 0L) {
        if (b < 0L)
        {
          long ret;
          if (a >= 9223372036854775807L / b) {
            ret = a * b;
          }
          else
          {
            long ret;
            if (limit)
              ret = 9223372036854775807L;
            else
              throw new ArithmeticException(msg); 
          }
        } else if (b > 0L)
        {
          long ret;
          if (-9223372036854775808L / b <= a) {
            ret = a * b;
          }
          else
          {
            long ret;
            if (limit)
              ret = -9223372036854775808L;
            else
              throw new ArithmeticException(msg);
          }
        } else {
          ret = 0L;
        }
      } else if (a > 0L)
      {
        long ret;
        if (a <= 9223372036854775807L / b) {
          ret = a * b;
        }
        else
        {
          long ret;
          if (limit)
            ret = 9223372036854775807L;
          else
            throw new ArithmeticException(msg);
        }
      } else {
        ret = 0L;
      }
    }return ret;
  }
}
package l2m.commons.logging;

import java.io.PrintWriter;
import java.io.StringWriter;

public class LogUtils
{
  public static String dumpStack()
  {
    return dumpStack(new Throwable());
  }

  public static String dumpStack(Throwable t)
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    t.printStackTrace(pw);
    pw.flush();
    pw.close();
    return sw.toString();
  }
}
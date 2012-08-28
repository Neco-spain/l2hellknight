package net.sf.l2j;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class GMAuditFilter
  implements Filter
{
  public boolean isLoggable(LogRecord record)
  {
    return record.getLoggerName().equalsIgnoreCase("gmaudit");
  }
}
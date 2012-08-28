package net.sf.l2j;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class AuditFilter
  implements Filter
{
  public boolean isLoggable(LogRecord record)
  {
    return record.getLoggerName().equalsIgnoreCase("audit");
  }
}
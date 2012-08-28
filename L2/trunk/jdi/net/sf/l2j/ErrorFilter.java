package net.sf.l2j;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class ErrorFilter
  implements Filter
{
  public boolean isLoggable(LogRecord record)
  {
    return record.getThrown() != null;
  }
}
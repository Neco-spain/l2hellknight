package net.sf.l2j;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class ChatFilter
  implements Filter
{
  public boolean isLoggable(LogRecord record)
  {
    return record.getLoggerName() == "chat";
  }
}
package net.sf.l2j;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class GMAuditFormatter extends Formatter
{
  public String format(LogRecord record)
  {
    return record.getMessage() + "\r\n";
  }
}
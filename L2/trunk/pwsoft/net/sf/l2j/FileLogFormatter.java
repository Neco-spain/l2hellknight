package net.sf.l2j;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javolution.text.TextBuilder;

public class FileLogFormatter extends Formatter
{
  private static final String a = "\r\n";
  private static final String b = "\t";
  private SimpleDateFormat c = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss,SSS");

  public String format(LogRecord record)
  {
    TextBuilder output = new TextBuilder();

    return output.append(c.format(new Date(record.getMillis()))).append("\t").append(record.getLevel().getName()).append("\t").append(record.getThreadID()).append("\t").append(record.getLoggerName()).append("\t").append(record.getMessage()).append("\r\n").toString();
  }
}
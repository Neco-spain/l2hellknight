package net.sf.l2j;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import javolution.text.TextBuilder;

public class ChatLogFormatter extends Formatter
{
  private static final String a = "\r\n";
  private SimpleDateFormat b = new SimpleDateFormat("dd MMM H:mm:ss");

  public String format(LogRecord record)
  {
    Object[] params = record.getParameters();
    TextBuilder output = new TextBuilder();
    output.append('[');
    output.append(b.format(new Date(record.getMillis())));
    output.append(']');
    output.append(' ');
    if (params != null) {
      for (Object p : params) {
        output.append(p);
        output.append(' ');
      }
    }
    output.append(record.getMessage());
    output.append("\r\n");

    return output.toString();
  }
}
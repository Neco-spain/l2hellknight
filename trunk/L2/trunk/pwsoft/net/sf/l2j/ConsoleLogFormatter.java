package net.sf.l2j;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import javolution.text.TextBuilder;

public class ConsoleLogFormatter extends Formatter
{
  private static final String a = "\r\n";

  public String format(LogRecord record)
  {
    TextBuilder output = new TextBuilder();

    output.append(record.getMessage());
    output.append("\r\n");
    if (record.getThrown() != null) {
      try {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        record.getThrown().printStackTrace(pw);
        pw.close();
        output.append(sw.toString());
        output.append("\r\n"); } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    return output.toString();
  }
}
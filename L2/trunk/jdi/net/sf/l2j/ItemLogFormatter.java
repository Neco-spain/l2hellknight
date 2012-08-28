package net.sf.l2j;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import javolution.text.TextBuilder;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.templates.L2Item;

public class ItemLogFormatter extends Formatter
{
  private static final String CRLF = "\r\n";
  private SimpleDateFormat dateFmt = new SimpleDateFormat("dd MMM H:mm:ss");

  public String format(LogRecord record)
  {
    TextBuilder output = new TextBuilder();
    output.append('[');
    output.append(dateFmt.format(new Date(record.getMillis())));
    output.append(']');
    output.append(' ');
    output.append(record.getMessage());
    for (Object p : record.getParameters())
    {
      if (p != null) {
        output.append(',');
        output.append(' ');
        if ((p instanceof L2ItemInstance))
        {
          L2ItemInstance item = (L2ItemInstance)p;
          output.append("item " + item.getObjectId() + ":");
          if (item.getEnchantLevel() > 0) output.append("+" + item.getEnchantLevel() + " ");
          output.append(item.getItem().getName());
          output.append("(" + item.getCount() + ")");
        }
        else
        {
          output.append(p.toString());
        }
      }
    }
    output.append("\r\n");

    return output.toString();
  }
}
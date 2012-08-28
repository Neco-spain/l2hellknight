package net.sf.l2j;

import java.util.logging.Filter;
import java.util.logging.LogRecord;
import net.sf.l2j.gameserver.model.L2ItemInstance;

public class ItemFilter
  implements Filter
{
  private String _excludeProcess;
  private String _excludeItemType;

  public boolean isLoggable(LogRecord record)
  {
    if (record.getLoggerName() != "item") return false;
    if (_excludeProcess != null)
    {
      String[] messageList = record.getMessage().split(":");
      if ((messageList.length < 2) || (!_excludeProcess.contains(messageList[1]))) return true;
    }
    if (_excludeItemType != null)
    {
      L2ItemInstance item = (L2ItemInstance)record.getParameters()[0];
      if (!_excludeItemType.contains(item.getItemType().toString())) return true;
    }
    return (_excludeProcess == null) && (_excludeItemType == null);
  }
}
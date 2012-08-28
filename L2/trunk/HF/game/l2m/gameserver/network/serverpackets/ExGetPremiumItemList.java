package l2m.gameserver.network.serverpackets;

import java.util.Map;
import java.util.Map.Entry;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.PremiumItem;

public class ExGetPremiumItemList extends L2GameServerPacket
{
  private int _objectId;
  private Map<Integer, PremiumItem> _list;

  public ExGetPremiumItemList(Player activeChar)
  {
    _objectId = activeChar.getObjectId();
    _list = activeChar.getPremiumItemList();
  }

  protected void writeImpl()
  {
    writeEx(134);
    if (!_list.isEmpty())
    {
      writeD(_list.size());
      for (Map.Entry entry : _list.entrySet())
      {
        writeD(((Integer)entry.getKey()).intValue());
        writeD(_objectId);
        writeD(((PremiumItem)entry.getValue()).getItemId());
        writeQ(((PremiumItem)entry.getValue()).getCount());
        writeD(0);
        writeS(((PremiumItem)entry.getValue()).getSender());
      }
    }
  }
}
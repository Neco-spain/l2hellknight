package net.sf.l2j.gameserver.network.serverpackets;

import java.util.List;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2TradeList;
import net.sf.l2j.gameserver.templates.L2Item;

public class WearList extends L2GameServerPacket
{
  private static final String _S__EF_WEARLIST = "[S] EF WearList";
  private int _listId;
  private L2ItemInstance[] _list;
  private int _money;
  private int _expertise;

  public WearList(L2TradeList list, int currentMoney, int expertiseIndex)
  {
    _listId = list.getListId();
    List lst = list.getItems();
    _list = ((L2ItemInstance[])lst.toArray(new L2ItemInstance[lst.size()]));
    _money = currentMoney;
    _expertise = expertiseIndex;
  }

  public WearList(List<L2ItemInstance> lst, int listId, int currentMoney)
  {
    _listId = listId;
    _list = ((L2ItemInstance[])lst.toArray(new L2ItemInstance[lst.size()]));
    _money = currentMoney;
  }

  protected final void writeImpl()
  {
    writeC(239);
    writeC(192);
    writeC(19);
    writeC(0);
    writeC(0);
    writeD(_money);
    writeD(_listId);

    int newlength = 0;
    for (L2ItemInstance item : _list) {
      if ((item.getItem().getCrystalType() <= _expertise) && (item.isEquipable()))
        newlength++;
    }
    writeH(newlength);

    for (L2ItemInstance item : _list)
    {
      if ((item.getItem().getCrystalType() <= _expertise) && (item.isEquipable())) {
        writeD(item.getItemId());
        writeH(item.getItem().getType2());

        if (item.getItem().getType1() != 4)
        {
          writeH(item.getItem().getBodyPart());
        }
        else
        {
          writeH(0);
        }

        writeD(Config.WEAR_PRICE);
      }
    }
  }

  public String getType()
  {
    return "[S] EF WearList";
  }
}
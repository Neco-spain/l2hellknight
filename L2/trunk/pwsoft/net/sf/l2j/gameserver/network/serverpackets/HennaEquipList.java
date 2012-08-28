package net.sf.l2j.gameserver.network.serverpackets;

import javolution.util.FastTable;
import net.sf.l2j.gameserver.datatables.HennaTreeTable;
import net.sf.l2j.gameserver.model.L2HennaInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class HennaEquipList extends L2GameServerPacket
{
  private boolean can_writeImpl = true;
  private L2PcInstance _player;
  private FastTable<L2HennaInstance> _hennaEquipList;

  public HennaEquipList(L2PcInstance player)
  {
    _player = player;
    _hennaEquipList = HennaTreeTable.getInstance().getAvailableHenna(_player.getClassId());
    if ((_hennaEquipList == null) || (_hennaEquipList.isEmpty()))
    {
      player.sendMessage("\u041F\u0440\u0438\u0445\u043E\u0434\u0438\u0442\u0435 \u043F\u043E\u0441\u043B\u0435 2\u0439 \u043F\u0440\u043E\u0444\u044B");
      can_writeImpl = false;
    }
  }

  protected final void writeImpl()
  {
    if (!can_writeImpl) {
      return;
    }
    writeC(226);
    writeD(_player.getAdena());
    writeD(3);

    writeD(_hennaEquipList.size());

    int i = 0; for (int n = _hennaEquipList.size(); i < n; i++)
    {
      L2HennaInstance temp = (L2HennaInstance)_hennaEquipList.get(i);

      if (_player.getInventory().getItemByItemId(temp.getItemIdDye()) != null)
      {
        writeD(temp.getSymbolId());
        writeD(temp.getItemIdDye());
        writeD(temp.getAmountDyeRequire());
        writeD(temp.getPrice());
        writeD(1);
      }
      else
      {
        writeD(0);
        writeD(0);
        writeD(0);
        writeD(0);
        writeD(0);
      }
    }
  }
}
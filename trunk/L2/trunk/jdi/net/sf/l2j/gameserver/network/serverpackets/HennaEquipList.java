package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2HennaInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class HennaEquipList extends L2GameServerPacket
{
  private static final String _S__E2_HennaEquipList = "[S] E2 HennaEquipList";
  private L2PcInstance _player;
  private L2HennaInstance[] _hennaEquipList;

  public HennaEquipList(L2PcInstance player, L2HennaInstance[] hennaEquipList)
  {
    _player = player;
    _hennaEquipList = hennaEquipList;
  }

  protected final void writeImpl()
  {
    writeC(226);
    writeD(_player.getAdena());
    writeD(3);

    writeD(_hennaEquipList.length);

    for (int i = 0; i < _hennaEquipList.length; i++)
    {
      if (_player.getInventory().getItemByItemId(_hennaEquipList[i].getItemIdDye()) != null)
      {
        writeD(_hennaEquipList[i].getSymbolId());
        writeD(_hennaEquipList[i].getItemIdDye());
        writeD(_hennaEquipList[i].getAmountDyeRequire());
        writeD(_hennaEquipList[i].getPrice());
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

  public String getType()
  {
    return "[S] E2 HennaEquipList";
  }
}
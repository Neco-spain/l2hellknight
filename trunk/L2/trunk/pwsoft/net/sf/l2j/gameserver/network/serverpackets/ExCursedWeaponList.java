package net.sf.l2j.gameserver.network.serverpackets;

import java.util.List;

public class ExCursedWeaponList extends L2GameServerPacket
{
  private List<Integer> _cursedWeaponIds;

  public ExCursedWeaponList(List<Integer> cursedWeaponIds)
  {
    _cursedWeaponIds = cursedWeaponIds;
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(69);

    writeD(_cursedWeaponIds.size());
    for (Integer i : _cursedWeaponIds)
    {
      writeD(i.intValue());
    }
  }

  public void gcb()
  {
    _cursedWeaponIds.clear();
  }
}
package net.sf.l2j.gameserver.network.serverpackets;

import java.util.List;

public class ExCursedWeaponList extends L2GameServerPacket
{
  private static final String _S__FE_45_EXCURSEDWEAPONLIST = "[S] FE:45 ExCursedWeaponList";
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

  public String getType()
  {
    return "[S] FE:45 ExCursedWeaponList";
  }
}
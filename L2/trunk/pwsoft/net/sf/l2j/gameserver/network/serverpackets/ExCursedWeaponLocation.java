package net.sf.l2j.gameserver.network.serverpackets;

import java.util.List;
import net.sf.l2j.util.Point3D;

public class ExCursedWeaponLocation extends L2GameServerPacket
{
  private List<CursedWeaponInfo> _cursedWeaponInfo;

  public ExCursedWeaponLocation(List<CursedWeaponInfo> cursedWeaponInfo)
  {
    _cursedWeaponInfo = cursedWeaponInfo;
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(70);

    if (!_cursedWeaponInfo.isEmpty())
    {
      writeD(_cursedWeaponInfo.size());
      for (CursedWeaponInfo w : _cursedWeaponInfo)
      {
        writeD(w.id);
        writeD(w.activated);

        writeD(w.pos.getX());
        writeD(w.pos.getY());
        writeD(w.pos.getZ());
      }
    }
    else
    {
      writeD(0);
      writeD(0);
    }
  }

  public static class CursedWeaponInfo {
    public Point3D pos;
    public int id;
    public int activated;

    public CursedWeaponInfo(Point3D p, int ID, int status) {
      pos = p;
      id = ID;
      activated = status;
    }
  }
}
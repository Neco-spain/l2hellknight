package l2p.gameserver.serverpackets;

import java.util.List;
import l2p.gameserver.utils.Location;

public class ExCursedWeaponLocation extends L2GameServerPacket
{
  private List<CursedWeaponInfo> _cursedWeaponInfo;

  public ExCursedWeaponLocation(List<CursedWeaponInfo> cursedWeaponInfo)
  {
    _cursedWeaponInfo = cursedWeaponInfo;
  }

  protected final void writeImpl()
  {
    writeEx(71);

    if (_cursedWeaponInfo.isEmpty()) {
      writeD(0);
    }
    else {
      writeD(_cursedWeaponInfo.size());
      for (CursedWeaponInfo w : _cursedWeaponInfo)
      {
        writeD(w._id);
        writeD(w._status);

        writeD(w._pos.x);
        writeD(w._pos.y);
        writeD(w._pos.z);
      }
    }
  }

  public static class CursedWeaponInfo {
    public Location _pos;
    public int _id;
    public int _status;

    public CursedWeaponInfo(Location p, int ID, int status) {
      _pos = p;
      _id = ID;
      _status = status;
    }
  }
}
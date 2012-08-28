package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2BoatInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class GetOffVehicle extends L2GameServerPacket
{
  private int _x;
  private int _y;
  private int _z;
  private L2PcInstance _activeChar;
  private L2BoatInstance _boat;

  public GetOffVehicle(L2PcInstance activeChar, L2BoatInstance boat, int x, int y, int z)
  {
    _activeChar = activeChar;
    _boat = boat;
    _x = x;
    _y = y;
    _z = z;

    if (_activeChar != null)
    {
      _activeChar.setInBoat(false);
      _activeChar.setBoat(null);
    }
  }

  protected void writeImpl()
  {
    if ((_boat == null) || (_activeChar == null)) return;

    writeC(93);
    writeD(_activeChar.getObjectId());
    writeD(_boat.getObjectId());
    writeD(_x);
    writeD(_y);
    writeD(_z);
  }

  public String getType()
  {
    return "[S] 5d GetOffVehicle";
  }
}
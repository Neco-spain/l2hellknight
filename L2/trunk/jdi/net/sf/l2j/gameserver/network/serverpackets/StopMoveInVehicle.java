package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.position.ObjectPosition;
import net.sf.l2j.util.Point3D;

public class StopMoveInVehicle extends L2GameServerPacket
{
  private L2PcInstance _activeChar;
  private int _boatId;

  public StopMoveInVehicle(L2PcInstance player, int boatid)
  {
    _activeChar = player;
    _boatId = boatid;
  }

  protected void writeImpl()
  {
    writeC(114);
    writeD(_activeChar.getObjectId());
    writeD(_boatId);
    writeD(_activeChar.getInBoatPosition().getX());
    writeD(_activeChar.getInBoatPosition().getY());
    writeD(_activeChar.getInBoatPosition().getZ());
    writeD(_activeChar.getPosition().getHeading());
  }

  public String getType()
  {
    return "[S] 72 StopMoveInVehicle";
  }
}
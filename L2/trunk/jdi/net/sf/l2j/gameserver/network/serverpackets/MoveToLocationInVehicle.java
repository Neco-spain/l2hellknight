package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2BoatInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class MoveToLocationInVehicle extends L2GameServerPacket
{
  private int _charObjId;
  private int _boatId;
  private L2CharPosition _destination;
  private L2CharPosition _origin;

  public MoveToLocationInVehicle(L2Character actor, L2CharPosition destination, L2CharPosition origin)
  {
    if (!(actor instanceof L2PcInstance)) return;

    L2PcInstance player = (L2PcInstance)actor;

    if (player.getBoat() == null) return;

    _charObjId = player.getObjectId();
    _boatId = player.getBoat().getObjectId();
    _destination = destination;
    _origin = origin;
  }

  protected void writeImpl()
  {
    writeC(113);
    writeD(_charObjId);
    writeD(_boatId);
    writeD(_destination.x);
    writeD(_destination.y);
    writeD(_destination.z);
    writeD(_origin.x);
    writeD(_origin.y);
    writeD(_origin.z);
  }

  public String getType()
  {
    return "[S] 71 MoveToLocationInVehicle";
  }
}
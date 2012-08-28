package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2BoatInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class MoveToLocationInVehicle extends L2GameServerPacket
{
  private int char_obj_id;
  private int boat_obj_id;
  private L2CharPosition _destination;
  private L2CharPosition _origin;
  private boolean can_writeimpl = false;

  public MoveToLocationInVehicle(L2Character actor, L2CharPosition destination, L2CharPosition origin)
  {
    if (actor == null) {
      return;
    }

    if (!actor.isPlayer()) {
      return;
    }

    L2PcInstance _char = actor.getPlayer();
    L2BoatInstance _boat = _char.getBoat();
    if (_boat == null) {
      return;
    }

    char_obj_id = _char.getObjectId();
    boat_obj_id = _boat.getObjectId();
    _destination = destination;
    _origin = origin;
    can_writeimpl = true;
  }

  protected void writeImpl()
  {
    if (!can_writeimpl) {
      return;
    }

    writeC(113);
    writeD(char_obj_id);
    writeD(boat_obj_id);
    writeD(_destination.x);
    writeD(_destination.y);
    writeD(_destination.z);
    writeD(_origin.x);
    writeD(_origin.y);
    writeD(_origin.z);
  }
}
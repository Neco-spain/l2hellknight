package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2BoatInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.position.ObjectPosition;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.StopMoveInVehicle;
import net.sf.l2j.util.Point3D;

public final class CannotMoveAnymoreInVehicle extends L2GameClientPacket
{
  private int _x;
  private int _y;
  private int _z;
  private int _heading;
  private int _boatId;

  protected void readImpl()
  {
    _boatId = readD();
    _x = readD();
    _y = readD();
    _z = readD();
    _heading = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    if ((player.getBoat() != null) && (player.getBoat().getObjectId() == _boatId))
    {
      player.setInBoatPosition(new Point3D(_x, _y, _z));
      player.getPosition().setHeading(_heading);
      player.broadcastPacket(new StopMoveInVehicle(player, _boatId));
    }
  }
}
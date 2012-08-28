package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.instancemanager.BoatManager;
import net.sf.l2j.gameserver.model.actor.instance.L2BoatInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.position.ObjectPosition;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.GetOnVehicle;
import net.sf.l2j.util.Point3D;

public final class RequestGetOnVehicle extends L2GameClientPacket
{
  private static final String _C__5C_GETONVEHICLE = "[C] 5C GetOnVehicle";
  private int _id;
  private int _x;
  private int _y;
  private int _z;

  protected void readImpl()
  {
    _id = readD();
    _x = readD();
    _y = readD();
    _z = readD();
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null) return;

    L2BoatInstance boat = BoatManager.getInstance().GetBoat(_id);
    if (boat == null) return;

    GetOnVehicle Gon = new GetOnVehicle(activeChar, boat, _x, _y, _z);
    activeChar.setInBoatPosition(new Point3D(_x, _y, _z));
    activeChar.getPosition().setXYZ(boat.getPosition().getX(), boat.getPosition().getY(), boat.getPosition().getZ());
    activeChar.broadcastPacket(Gon);
    activeChar.revalidateZone(true);
  }

  public String getType()
  {
    return "[C] 5C GetOnVehicle";
  }
}
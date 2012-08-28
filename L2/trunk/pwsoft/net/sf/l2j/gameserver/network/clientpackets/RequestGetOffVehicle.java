package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.instancemanager.BoatManager;
import net.sf.l2j.gameserver.model.actor.instance.L2BoatInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.GetOffVehicle;

public final class RequestGetOffVehicle extends L2GameClientPacket
{
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
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    if (System.currentTimeMillis() - player.gCPAM() < 500L) {
      return;
    }
    player.sCPAM();

    L2BoatInstance boat = BoatManager.getInstance().GetBoat(_id);
    GetOffVehicle Gof = new GetOffVehicle(player, boat, _x, _y, _z);
    player.broadcastPacket(Gof);
  }
}
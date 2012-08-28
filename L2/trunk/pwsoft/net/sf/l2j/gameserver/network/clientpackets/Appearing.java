package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class Appearing extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }

    if (System.currentTimeMillis() - player.gCPAH() < 200L) {
      return;
    }

    player.sCPAH();

    if (player.isDeleting()) {
      player.sendActionFailed();
      return;
    }

    if ((!player.isTeleporting()) && (!player.inObserverMode()))
    {
      player.sendActionFailed();
      return;
    }

    if (player.getObserverMode() == 2) {
      player.returnFromObserverMode();
    }

    player.onTeleported();
  }
}
package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.StopRotation;

public final class FinishRotating extends L2GameClientPacket
{
  private int _degree;
  private int _unknown;

  protected void readImpl()
  {
    _degree = readD();
    _unknown = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    player.broadcastPacket(new StopRotation(player, _degree, 0));
  }
}
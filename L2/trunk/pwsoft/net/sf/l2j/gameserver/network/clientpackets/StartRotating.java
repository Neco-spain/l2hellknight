package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.BeginRotation;

public final class StartRotating extends L2GameClientPacket
{
  private int _degree;
  private int _side;

  protected void readImpl()
  {
    _degree = readD();
    _side = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    player.setHeading(_degree);
    player.broadcastPacket(new BeginRotation(player, _degree, _side, 0));
  }
}
package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class SnoopQuit extends L2GameClientPacket
{
  private int _snoopID;

  protected void readImpl()
  {
    _snoopID = readD();
  }

  protected void runImpl()
  {
    L2PcInstance snoop = L2World.getInstance().getPlayer(_snoopID);
    if (snoop == null) {
      return;
    }
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    snoop.removeSnooper(player);
    player.removeSnooped(snoop);
  }
}
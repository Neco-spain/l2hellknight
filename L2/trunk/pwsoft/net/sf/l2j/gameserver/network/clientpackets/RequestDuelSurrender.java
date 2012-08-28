package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Duel;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class RequestDuelSurrender extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    L2PcInstance p = ((L2GameClient)getClient()).getActiveChar();
    if (p == null) {
      return;
    }
    Duel d = p.getDuel();

    if (d == null) {
      return;
    }
    d.doSurrender(p);
  }
}
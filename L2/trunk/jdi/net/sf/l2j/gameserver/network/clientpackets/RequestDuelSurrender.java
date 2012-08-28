package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.instancemanager.DuelManager;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class RequestDuelSurrender extends L2GameClientPacket
{
  private static final String _C__D0_30_REQUESTDUELSURRENDER = "[C] D0:30 RequestDuelSurrender";

  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    if (!Config.DUEL_ALLOW) return;
    DuelManager.getInstance().doSurrender(((L2GameClient)getClient()).getActiveChar());
  }

  public String getType()
  {
    return "[C] D0:30 RequestDuelSurrender";
  }
}
package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class RequestOlympiadMatchList extends L2GameClientPacket
{
  private static final String _C__D0_13_REQUESTOLYMPIADMATCHLIST = "[C] D0:13 RequestOlympiadMatchList";

  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null)
      return;
    if (activeChar.inObserverMode()) Olympiad.sendMatchList(activeChar);
  }

  public String getType()
  {
    return "[C] D0:13 RequestOlympiadMatchList";
  }
}
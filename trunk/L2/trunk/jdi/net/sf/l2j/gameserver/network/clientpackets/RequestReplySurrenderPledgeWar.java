package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class RequestReplySurrenderPledgeWar extends L2GameClientPacket
{
  private static final String _C__52_REQUESTREPLYSURRENDERPLEDGEWAR = "[C] 52 RequestReplySurrenderPledgeWar";
  private int _answer;

  protected void readImpl()
  {
    String _reqName = readS();
    _answer = readD();
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null)
      return;
    L2PcInstance requestor = activeChar.getActiveRequester();
    if (requestor == null) {
      return;
    }
    if (_answer == 1)
    {
      requestor.deathPenalty(false);
      ClanTable.getInstance().deleteclanswars(requestor.getClanId(), activeChar.getClanId());
    }

    activeChar.onTransactionRequest(null);
  }

  public String getType()
  {
    return "[C] 52 RequestReplySurrenderPledgeWar";
  }
}
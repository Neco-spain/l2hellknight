package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class RequestReplyStartPledgeWar extends L2GameClientPacket
{
  private int _answer;

  protected void readImpl()
  {
    String _reqName = readS();
    _answer = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null)
      return;
    L2PcInstance requestor = player.getTransactionRequester();
    if (requestor == null) {
      return;
    }
    if (_answer == 1)
    {
      ClanTable.getInstance().storeclanswars(requestor.getClanId(), player.getClanId());
    }
    else
    {
      requestor.sendPacket(Static.WAR_PROCLAMATION_HAS_BEEN_REFUSED);
    }
    player.setTransactionRequester(null);
    requestor.onTransactionResponse();
  }

  public String getType()
  {
    return "C.ReplyStartPledgeWar";
  }
}
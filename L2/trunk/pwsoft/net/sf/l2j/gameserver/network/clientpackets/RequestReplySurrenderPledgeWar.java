package net.sf.l2j.gameserver.network.clientpackets;

import java.nio.BufferUnderflowException;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class RequestReplySurrenderPledgeWar extends L2GameClientPacket
{
  private int _answer;
  private String _reqName;

  protected void readImpl()
  {
    try
    {
      _reqName = readS();
    }
    catch (BufferUnderflowException e)
    {
      _reqName = "";
    }
    _answer = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    L2PcInstance requestor = player.getTransactionRequester();
    if (requestor == null) {
      return;
    }
    if (_answer == 1)
    {
      requestor.deathPenalty(false);
      ClanTable.getInstance().deleteclanswars(requestor.getClanId(), player.getClanId());
    }
    else {
      player.sendMessage("\u0418\u0433\u0440\u043E\u043A \u043D\u0435 \u0441\u043E\u0433\u043B\u0430\u0441\u0435\u043D");
    }
    player.setTransactionRequester(null);
    requestor.setTransactionRequester(null);
  }
}
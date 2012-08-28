package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.SendTradeDone;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class AnswerTradeRequest extends L2GameClientPacket
{
  private static final String _C__40_ANSWERTRADEREQUEST = "[C] 40 AnswerTradeRequest";
  private int _response;

  protected void readImpl()
  {
    _response = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) return;

    if ((Config.GM_DISABLE_TRANSACTION) && (player.getAccessLevel() >= Config.GM_TRANSACTION_MIN) && (player.getAccessLevel() <= Config.GM_TRANSACTION_MAX))
    {
      player.sendMessage("Transactions are disable for your Access Level");
      sendPacket(new ActionFailed());
      return;
    }

    L2PcInstance partner = player.getActiveRequester();
    if ((partner == null) || (L2World.getInstance().findObject(partner.getObjectId()) == null))
    {
      player.sendPacket(new SendTradeDone(0));
      SystemMessage msg = new SystemMessage(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
      player.sendPacket(msg);
      player.setActiveRequester(null);
      player.setAllowTrade(true);
      partner.setAllowTrade(true);

      msg = null;
      return;
    }

    if ((_response == 1) && (!partner.isRequestExpired()))
    {
      player.startTrade(partner);
      partner.setAllowTrade(true);
      player.setAllowTrade(true);
    }
    else
    {
      SystemMessage msg = new SystemMessage(SystemMessageId.S1_DENIED_TRADE_REQUEST);
      msg.addString(player.getName());
      partner.sendPacket(msg);
      player.setAllowTrade(true);
      msg = null;
    }

    player.setActiveRequester(null);
    partner.onTransactionResponse();
  }

  public String getType()
  {
    return "[C] 40 AnswerTradeRequest";
  }
}
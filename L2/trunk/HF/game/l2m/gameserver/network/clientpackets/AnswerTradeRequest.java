package l2m.gameserver.clientpackets;

import java.util.concurrent.CopyOnWriteArrayList;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Request;
import l2m.gameserver.model.Request.L2RequestType;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.SystemMessage2;
import l2m.gameserver.network.serverpackets.TradeStart;
import l2m.gameserver.network.serverpackets.components.IStaticPacket;
import l2m.gameserver.network.serverpackets.components.SystemMsg;

public class AnswerTradeRequest extends L2GameClientPacket
{
  private int _response;

  protected void readImpl()
  {
    _response = readD();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    Request request = activeChar.getRequest();
    if ((request == null) || (!request.isTypeOf(Request.L2RequestType.TRADE_REQUEST)))
    {
      activeChar.sendActionFailed();
      return;
    }

    if (!request.isInProgress())
    {
      request.cancel();
      activeChar.sendActionFailed();
      return;
    }

    if (activeChar.isOutOfControl())
    {
      request.cancel();
      activeChar.sendActionFailed();
      return;
    }

    Player requestor = request.getRequestor();
    if (requestor == null)
    {
      request.cancel();
      activeChar.sendPacket(SystemMsg.THAT_PLAYER_IS_NOT_ONLINE);
      activeChar.sendActionFailed();
      return;
    }

    if (requestor.getRequest() != request)
    {
      request.cancel();
      activeChar.sendActionFailed();
      return;
    }

    if (_response == 0)
    {
      request.cancel();
      requestor.sendPacket(new SystemMessage2(SystemMsg.C1_HAS_DENIED_YOUR_REQUEST_TO_TRADE).addString(activeChar.getName()));
      return;
    }

    if (!activeChar.isInRangeZ(requestor, 200L))
    {
      request.cancel();
      activeChar.sendPacket(SystemMsg.YOUR_TARGET_IS_OUT_OF_RANGE);
      return;
    }

    if (requestor.isActionsDisabled())
    {
      request.cancel();
      activeChar.sendPacket(new SystemMessage2(SystemMsg.C1_IS_ON_ANOTHER_TASK).addString(requestor.getName()));
      activeChar.sendActionFailed();
      return;
    }

    try
    {
      new Request(Request.L2RequestType.TRADE, activeChar, requestor);
      requestor.setTradeList(new CopyOnWriteArrayList());
      requestor.sendPacket(new IStaticPacket[] { new SystemMessage2(SystemMsg.YOU_BEGIN_TRADING_WITH_C1).addString(activeChar.getName()), new TradeStart(requestor, activeChar) });
      activeChar.setTradeList(new CopyOnWriteArrayList());
      activeChar.sendPacket(new IStaticPacket[] { new SystemMessage2(SystemMsg.YOU_BEGIN_TRADING_WITH_C1).addString(requestor.getName()), new TradeStart(activeChar, requestor) });
    }
    finally
    {
      request.done();
    }
  }
}
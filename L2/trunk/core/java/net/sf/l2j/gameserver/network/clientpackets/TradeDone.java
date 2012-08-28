package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.TradeList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class TradeDone extends L2GameClientPacket
{
	private static final String _C__17_TRADEDONE = "[C] 17 TradeDone";
	private static Logger _log = Logger.getLogger(TradeDone.class.getName());

	private int _response;

	@Override
	protected void readImpl()
	{
		_response = readD();
	}

	@Override
	protected void runImpl()
	{
        L2PcInstance player = getClient().getActiveChar();
        if (player == null) return;
        TradeList trade = player.getActiveTradeList();
        if (trade == null)
        	{
            _log.warning("player.getTradeList == null in "+getType()+" for player "+player.getName());
        	return;
        	}
        if (trade.isLocked()) return;

		if (_response == 1)
		{
	        if (trade.getPartner() == null || L2World.getInstance().findObject(trade.getPartner().getObjectId()) == null)
	        {
	            // Trade partner not found, cancel trade
	            player.cancelActiveTrade();
	            SystemMessage msg = new SystemMessage(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
	            player.sendPacket(msg);
	            msg = null;
	            return;
	        }

	        if (Config.GM_DISABLE_TRANSACTION && player.getAccessLevel() >= Config.GM_TRANSACTION_MIN
	            && player.getAccessLevel() <= Config.GM_TRANSACTION_MAX)
	        {
	            player.cancelActiveTrade();
	            player.sendMessage("Transactions are disable for your Access Level");
	            return;
	        }
	        trade.confirm();
		}
		else player.cancelActiveTrade();
	}

	@Override
	public String getType()
	{
		return _C__17_TRADEDONE;
	}
}

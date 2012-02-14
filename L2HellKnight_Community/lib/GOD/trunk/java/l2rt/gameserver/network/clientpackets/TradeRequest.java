package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Object;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2World;
import l2rt.gameserver.model.base.Transaction;
import l2rt.gameserver.model.base.Transaction.TransactionType;
import l2rt.gameserver.network.serverpackets.SendTradeRequest;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.util.HWID;
import l2rt.util.Util;

public class TradeRequest extends L2GameClientPacket
{
	//Format: cd
	private int _objectId;

	@Override
	public void readImpl()
	{
		_objectId = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(!activeChar.getPlayerAccess().UseTrade)
		{
			activeChar.sendPacket(Msg.THIS_ACCOUNT_CANOT_TRADE_ITEMS);
			activeChar.sendActionFailed();
			return;
		}

		String tradeBan = activeChar.getVar("tradeBan");
		if(tradeBan != null && (tradeBan.equals("-1") || Long.parseLong(tradeBan) >= System.currentTimeMillis()))
		{
			activeChar.sendMessage("Your trade is banned! Expires: " + (tradeBan.equals("-1") ? "never" : Util.formatTime((Long.parseLong(tradeBan) - System.currentTimeMillis()) / 1000)) + ".");
			return;
		}

		if(activeChar.hasHWID())
		{
			int ban;
			if((ban = HWID.getBonus(activeChar, "tradeBan")) != 0)
				activeChar.sendMessage("Your trade is totally banned! Expires: " + (ban < 0 ? "never" : Util.formatTime(ban - System.currentTimeMillis() / 1000)) + ".");
		}

		if(activeChar.isDead())
		{
			activeChar.sendActionFailed();
			return;
		}

		L2Object target = L2World.getAroundObjectById(activeChar, _objectId);

		if(target == null || !target.isPlayer() || target.getObjectId() == activeChar.getObjectId())
		{
			activeChar.sendPacket(Msg.THAT_IS_THE_INCORRECT_TARGET);
			return;
		}

		L2Player pcTarget = (L2Player) target;

		if(activeChar.getPrivateStoreType() != L2Player.STORE_PRIVATE_NONE || pcTarget.getPrivateStoreType() != L2Player.STORE_PRIVATE_NONE)
		{
			activeChar.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}

		if(!pcTarget.getPlayerAccess().UseTrade)
		{
			activeChar.sendPacket(Msg.THIS_ACCOUNT_CANOT_TRADE_ITEMS);
			activeChar.sendActionFailed();
			return;
		}

		tradeBan = pcTarget.getVar("tradeBan");
		if(tradeBan != null && (tradeBan.equals("-1") || Long.parseLong(tradeBan) >= System.currentTimeMillis()))
		{
			activeChar.sendMessage("Target trade is banned! Expires: " + (tradeBan.equals("-1") ? "never" : Util.formatTime((Long.parseLong(tradeBan) - System.currentTimeMillis()) / 1000)) + ".");
			return;
		}

		if(pcTarget.hasHWID())
		{
			int ban;
			if((ban = HWID.getBonus(pcTarget, "tradeBan")) != 0)
				activeChar.sendMessage("Target trade is totally banned! Expires: " + (ban < 0 ? "never" : Util.formatTime(ban - System.currentTimeMillis() / 1000)) + ".");
		}

		if(pcTarget.getTeam() != 0)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(pcTarget.isInOlympiadMode() || activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(Msg.THAT_IS_THE_INCORRECT_TARGET);
			return;
		}

		if(pcTarget.getTradeRefusal() || pcTarget.isInBlockList(activeChar) || pcTarget.isBlockAll())
		{
			activeChar.sendPacket(Msg.YOU_HAVE_BEEN_BLOCKED_FROM_THE_CONTACT_YOU_SELECTED);
			return;
		}

		if(activeChar.isInTransaction())
		{
			activeChar.sendPacket(Msg.YOU_ARE_ALREADY_TRADING_WITH_SOMEONE);
			return;
		}

		if(pcTarget.isInTransaction())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.S1_IS_BUSY_PLEASE_TRY_AGAIN_LATER).addString(pcTarget.getName()));
			return;
		}

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
			return;
		}

		new Transaction(TransactionType.TRADE_REQUEST, activeChar, pcTarget, 10000);
		pcTarget.sendPacket(new SendTradeRequest(activeChar.getObjectId()));
		activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_REQUESTED_A_TRADE_WITH_C1).addString(pcTarget.getName()));
	}
}
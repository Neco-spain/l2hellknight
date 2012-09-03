package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2TradeList;
import l2rt.gameserver.model.base.Transaction;
import l2rt.gameserver.model.base.Transaction.TransactionType;
import l2rt.gameserver.network.serverpackets.SendTradeDone;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.tables.GmListTable;
import l2rt.util.Log;

/**
 * Вызывается при нажатии кнопки OK в окне обмена.
 */
public class TradeDone extends L2GameClientPacket
{
	private int _response;

	@Override
	public void readImpl()
	{
		_response = readD();
	}

	@Override
	public void runImpl()
	{
		synchronized (getClient())
		{
			L2Player activeChar = getClient().getActiveChar();
			if(activeChar == null)
				return;

			Transaction transaction = activeChar.getTransaction();
			L2Player requestor;

			if(transaction == null || (requestor = transaction.getOtherPlayer(activeChar)) == null)
			{
				if(transaction != null)
					transaction.cancel();
				activeChar.sendPacket(SendTradeDone.Fail, Msg.ActionFail);
				return;
			}

			if(activeChar.getPrivateStoreType() != L2Player.STORE_PRIVATE_NONE || requestor.getPrivateStoreType() != L2Player.STORE_PRIVATE_NONE)
			{
				transaction.cancel();
				activeChar.sendPacket(SendTradeDone.Fail, Msg.ActionFail);
				activeChar.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
				requestor.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
				return;
			}

			if(!transaction.isTypeOf(TransactionType.TRADE))
			{
				transaction.cancel();
				activeChar.sendPacket(SendTradeDone.Fail, Msg.ActionFail, new SystemMessage("Something wrong. Maybe, cheater?"));
				requestor.sendPacket(SendTradeDone.Fail, Msg.ActionFail, new SystemMessage("Something wrong. Maybe, cheater?"));
				return;
			}

			if(_response == 1)
			{
				// first party accepted the trade
				// notify clients that "OK" button has been pressed.
				transaction.confirm(activeChar);
				requestor.sendPacket(new SystemMessage(SystemMessage.C1_HAS_CONFIRMED_THE_TRADE).addString(activeChar.getName()), Msg.TradePressOtherOk);

				if(!transaction.isConfirmed(activeChar) || !transaction.isConfirmed(requestor)) // Check for dual confirmation
				{
					activeChar.sendActionFailed();
					return;
				}

				//Can't exchange on a big distance
				if(!activeChar.isInRange(requestor, 1000))
				{
					transaction.cancel();
					activeChar.sendPacket(SendTradeDone.Fail, new SystemMessage(SystemMessage.C1_HAS_CANCELLED_THE_TRADE).addString(requestor.getName()));
					requestor.sendPacket(SendTradeDone.Fail, new SystemMessage(SystemMessage.C1_HAS_CANCELLED_THE_TRADE).addString(activeChar.getName()));
					return;
				}

				boolean trade1Valid = L2TradeList.validateTrade(activeChar, transaction.getExchangeList(activeChar));
				boolean trade2Valid = L2TradeList.validateTrade(requestor, transaction.getExchangeList(requestor));

				if(trade1Valid && trade2Valid)
				{
					transaction.tradeItems();
					requestor.sendPacket(Msg.YOUR_TRADE_IS_SUCCESSFUL, SendTradeDone.Success);
					activeChar.sendPacket(Msg.YOUR_TRADE_IS_SUCCESSFUL, SendTradeDone.Success);
				}
				else
				{
					if(!trade2Valid)
					{
						String msgToSend = requestor.getName() + " tried a trade dupe [!trade2Valid]";
						Log.add(msgToSend, "illegal-actions");
						GmListTable.broadcastMessageToGMs(msgToSend);
					}

					if(!trade1Valid)
					{
						String msgToSend = activeChar.getName() + " tried a trade dupe [!trade1Valid]";
						Log.add(msgToSend, "illegal-actions");
						GmListTable.broadcastMessageToGMs(msgToSend);
					}

					activeChar.sendPacket(Msg.THE_ATTEMPT_TO_TRADE_HAS_FAILED, SendTradeDone.Fail);
					requestor.sendPacket(Msg.THE_ATTEMPT_TO_TRADE_HAS_FAILED, SendTradeDone.Fail);
				}
			}
			else
			{
				activeChar.sendPacket(SendTradeDone.Fail);
				requestor.sendPacket(SendTradeDone.Fail, new SystemMessage(SystemMessage.C1_HAS_CANCELLED_THE_TRADE).addString(activeChar.getName()));
			}

			transaction.cancel();
		}
	}
}
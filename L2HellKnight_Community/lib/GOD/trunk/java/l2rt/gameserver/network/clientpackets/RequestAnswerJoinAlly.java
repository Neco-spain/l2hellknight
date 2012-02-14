package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Alliance;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.base.Transaction;
import l2rt.gameserver.model.base.Transaction.TransactionType;

/**
 *  format  c(d)
 */
public class RequestAnswerJoinAlly extends L2GameClientPacket
{
	private int _response;

	@Override
	public void readImpl()
	{
		_response = _buf.remaining() >= 4 ? readD() : 0;
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar != null)
		{
			Transaction transaction = activeChar.getTransaction();

			if(transaction == null)
				return;

			if(!transaction.isValid() || !transaction.isTypeOf(TransactionType.ALLY))
			{
				transaction.cancel();
				activeChar.sendPacket(Msg.TIME_EXPIRED, Msg.ActionFail);
				return;
			}

			L2Player requestor = transaction.getOtherPlayer(activeChar);

			transaction.cancel();

			if(requestor.getAlliance() == null)
				return;

			if(_response == 1)
			{
				L2Alliance ally = requestor.getAlliance();
				activeChar.sendPacket(Msg.YOU_HAVE_ACCEPTED_THE_ALLIANCE);
				activeChar.getClan().setAllyId(requestor.getAllyId());
				activeChar.getClan().updateClanInDB();
				ally.addAllyMember(activeChar.getClan(), true);
				ally.broadcastAllyStatus(true);
			}
			else
				requestor.sendPacket(Msg.YOU_HAVE_FAILED_TO_INVITE_A_CLAN_INTO_THE_ALLIANCE);
		}
	}
}
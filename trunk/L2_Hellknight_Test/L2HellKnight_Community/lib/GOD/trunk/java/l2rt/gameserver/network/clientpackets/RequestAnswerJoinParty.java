package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Party;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.base.Transaction;
import l2rt.gameserver.model.base.Transaction.TransactionType;
import l2rt.gameserver.network.serverpackets.JoinParty;
import l2rt.gameserver.network.serverpackets.SystemMessage;

import static l2rt.gameserver.model.L2Zone.ZoneType.OlympiadStadia;

public class RequestAnswerJoinParty extends L2GameClientPacket
{
	private int _response;

	@Override
	public void readImpl()
	{
		if(_buf.hasRemaining())
			_response = readD();
		else
			_response = 0;
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		Transaction transaction = activeChar.getTransaction();

		if(transaction == null)
			return;

		if(!transaction.isValid() || !transaction.isTypeOf(TransactionType.PARTY))
		{
			transaction.cancel();
			activeChar.sendPacket(Msg.TIME_EXPIRED, Msg.ActionFail);
			return;
		}

		L2Player requestor = transaction.getOtherPlayer(activeChar);
		L2Party party = requestor.getParty();
		transaction.cancel();

		if(party == null || party.getPartyLeader() == null)
		{
			activeChar.sendPacket(Msg.ActionFail);
			requestor.sendPacket(new JoinParty(0));
			return;
		}

		SystemMessage problem = activeChar.canJoinParty(requestor);
		if(problem != null)
		{
			activeChar.sendPacket(problem, Msg.ActionFail);
			requestor.sendPacket(new JoinParty(0));
			return;
		}

		requestor.sendPacket(new JoinParty(_response));

		if(_response == 1)
		{
			if(activeChar.isInZone(OlympiadStadia))
			{
				activeChar.sendPacket(Msg.A_PARTY_CANNOT_BE_FORMED_IN_THIS_AREA);
				requestor.sendPacket(Msg.A_PARTY_CANNOT_BE_FORMED_IN_THIS_AREA);
				return;
			}

			if(party.getMemberCount() >= L2Party.MAX_SIZE)
			{
				activeChar.sendPacket(Msg.PARTY_IS_FULL);
				requestor.sendPacket(Msg.PARTY_IS_FULL);
				return;
			}

			activeChar.joinParty(party);
		}
		else
		//activate garbage collection if there are no other members in party (happens when we were creating new one)
		if(party.getMemberCount() == 1)
			requestor.setParty(null);
	}
}
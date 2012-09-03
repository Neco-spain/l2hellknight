package l2rt.gameserver.network.clientpackets;

import l2rt.Config;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Party;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2World;
import l2rt.gameserver.model.base.Transaction;
import l2rt.gameserver.model.base.Transaction.TransactionType;
import l2rt.gameserver.network.L2GameClient;
import l2rt.gameserver.network.serverpackets.AskJoinParty;
import l2rt.gameserver.network.serverpackets.SystemMessage;

public class RequestJoinParty extends L2GameClientPacket
{
	private String _name;
	private int _itemDistribution;

	@Override
	public void readImpl()
	{
		_name = readS(Config.CNAME_MAXLEN);
		_itemDistribution = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		L2Player target = L2World.getPlayer(_name);

		if(target == null || target == activeChar || target.isInvisible())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInTransaction())
		{
			activeChar.sendPacket(Msg.WAITING_FOR_ANOTHER_REPLY, Msg.ActionFail);
			return;
		}

		SystemMessage problem = target.canJoinParty(activeChar);
		if(problem != null)
		{
			activeChar.sendPacket(problem);
			return;
		}

		if(!activeChar.isInParty())
			createNewParty(getClient(), _itemDistribution, target, activeChar);
		else
			addTargetToParty(getClient(), _itemDistribution, target, activeChar);
	}

	private void addTargetToParty(L2GameClient client, int itemDistribution, L2Player target, L2Player activeChar)
	{
		if(activeChar.getParty().getMemberCount() >= L2Party.MAX_SIZE)
		{
			activeChar.sendPacket(Msg.PARTY_IS_FULL);
			return;
		}

		// Только Party Leader может приглашать новых членов
		if(Config.PARTY_LEADER_ONLY_CAN_INVITE && !activeChar.getParty().isLeader(activeChar))
		{
			activeChar.sendPacket(Msg.ONLY_THE_LEADER_CAN_GIVE_OUT_INVITATIONS);
			return;
		}

		if(!target.isInTransaction())
		{
			new Transaction(TransactionType.PARTY, activeChar, target, 10000);

			target.sendPacket(new AskJoinParty(activeChar.getName(), itemDistribution));
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_INVITED_C1_TO_JOIN_YOUR_PARTY).addString(target.getName()));
		}
		else
			activeChar.sendPacket(new SystemMessage(SystemMessage.S1_IS_BUSY_PLEASE_TRY_AGAIN_LATER).addString(target.getName()));

	}

	private void createNewParty(L2GameClient client, int itemDistribution, L2Player target, L2Player requestor)
	{
		if(!target.isInTransaction())
		{
			requestor.setParty(new L2Party(requestor, itemDistribution));
			new Transaction(TransactionType.PARTY, requestor, target, 10000);
			target.sendPacket(new AskJoinParty(requestor.getName(), itemDistribution));
			requestor.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_INVITED_C1_TO_JOIN_YOUR_PARTY).addString(target.getName()));
		}
		else
			requestor.sendPacket(new SystemMessage(SystemMessage.S1_IS_BUSY_PLEASE_TRY_AGAIN_LATER).addString(target.getName()));
	}
}
package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Clan;
import l2rt.gameserver.model.L2ClanMember;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.serverpackets.PledgeShowMemberListDelete;
import l2rt.gameserver.network.serverpackets.SystemMessage;

public class RequestWithdrawalPledge extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		//is the guy in a clan  ?
		if(activeChar.getClanId() == 0)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInCombat())
		{
			activeChar.sendPacket(Msg.ONE_CANNOT_LEAVE_ONES_CLAN_DURING_COMBAT);
			return;
		}

		L2Clan clan = activeChar.getClan();
		if(clan == null)
			return;

		L2ClanMember member = clan.getClanMember(activeChar.getObjectId());
		if(member == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(member.isClanLeader())
		{
			activeChar.sendMessage("A clan leader may not be dismissed.");
			return;
		}

		// this also updated the database
		clan.removeClanMember(activeChar.getObjectId());

		clan.broadcastToOnlineMembers(new SystemMessage(SystemMessage.S1_HAS_WITHDRAWN_FROM_THE_CLAN).addString(activeChar.getName()), new PledgeShowMemberListDelete(activeChar.getName()));

		if(activeChar.getPledgeType() == L2Clan.SUBUNIT_ACADEMY)
			activeChar.setLvlJoinedAcademy(0);
		activeChar.setClan(null);
		if(!activeChar.isNoble())
			activeChar.setTitle("");

		activeChar.setLeaveClanCurTime();
		activeChar.broadcastUserInfo(true);
		activeChar.broadcastRelationChanged();

		activeChar.sendPacket(Msg.YOU_HAVE_RECENTLY_BEEN_DISMISSED_FROM_A_CLAN_YOU_ARE_NOT_ALLOWED_TO_JOIN_ANOTHER_CLAN_FOR_24_HOURS, Msg.PledgeShowMemberListDeleteAll);
	}
}
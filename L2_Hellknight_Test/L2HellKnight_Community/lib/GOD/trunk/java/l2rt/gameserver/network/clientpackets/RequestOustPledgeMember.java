package l2rt.gameserver.network.clientpackets;

import l2rt.Config;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Clan;
import l2rt.gameserver.model.L2ClanMember;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.serverpackets.PledgeShowMemberListDelete;
import l2rt.gameserver.network.serverpackets.SystemMessage;

import java.util.logging.Logger;

public class RequestOustPledgeMember extends L2GameClientPacket
{
	//Format: cS
	static Logger _log = Logger.getLogger(RequestOustPledgeMember.class.getName());

	private String _target;

	@Override
	public void readImpl()
	{
		_target = readS(Config.CNAME_MAXLEN);
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();

		if(activeChar == null || !((activeChar.getClanPrivileges() & L2Clan.CP_CL_DISMISS) == L2Clan.CP_CL_DISMISS))
			return;

		L2Clan clan = activeChar.getClan();
		L2ClanMember member = clan.getClanMember(_target);
		if(member == null)
		{
			activeChar.sendPacket(Msg.THE_TARGET_MUST_BE_A_CLAN_MEMBER);
			return;
		}

		if(member.isOnline() && member.getPlayer().isInCombat())
		{
			activeChar.sendPacket(Msg.A_CLAN_MEMBER_MAY_NOT_BE_DISMISSED_DURING_COMBAT);
			return;
		}

		if(member.isClanLeader())
		{
			activeChar.sendMessage("A clan leader may not be dismissed.");
			return;
		}

		clan.removeClanMember(member.getObjectId());
		clan.broadcastToOnlineMembers(new SystemMessage(SystemMessage.CLAN_MEMBER_S1_HAS_BEEN_EXPELLED).addString(_target), new PledgeShowMemberListDelete(_target));
		clan.setExpelledMember();

		if(member.isOnline())
		{
			L2Player player = member.getPlayer();
			if(player.getPledgeType() == L2Clan.SUBUNIT_ACADEMY)
				player.setLvlJoinedAcademy(0);
			player.setClan(null);
			if(!player.isNoble())
				player.setTitle("");
			player.setLeaveClanCurTime();

			player.broadcastUserInfo(true);
			player.broadcastRelationChanged();

			player.sendPacket(Msg.YOU_HAVE_RECENTLY_BEEN_DISMISSED_FROM_A_CLAN_YOU_ARE_NOT_ALLOWED_TO_JOIN_ANOTHER_CLAN_FOR_24_HOURS, Msg.PledgeShowMemberListDeleteAll);
		}
	}
}
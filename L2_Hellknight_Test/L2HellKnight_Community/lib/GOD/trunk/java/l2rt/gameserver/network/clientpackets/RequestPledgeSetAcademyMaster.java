package l2rt.gameserver.network.clientpackets;

import l2rt.Config;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.gameserver.model.L2Clan;
import l2rt.gameserver.model.L2ClanMember;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.serverpackets.PledgeReceiveMemberInfo;
import l2rt.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import l2rt.gameserver.network.serverpackets.SystemMessage;

import java.util.logging.Logger;

public class RequestPledgeSetAcademyMaster extends L2GameClientPacket
{
	// format: (ch)dSS
	static Logger _log = Logger.getLogger(RequestPledgeSetAcademyMaster.class.getName());

	private int _mode; // 1=set, 0=unset
	private String _sponsorName;
	private String _apprenticeName;

	@Override
	public void readImpl()
	{
		_mode = readD();
		_sponsorName = readS(Config.CNAME_MAXLEN);
		_apprenticeName = readS(Config.CNAME_MAXLEN);
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		L2Clan clan = activeChar.getClan();
		if(clan == null)
			return;

		if((activeChar.getClanPrivileges() & L2Clan.CP_CL_APPRENTICE) == L2Clan.CP_CL_APPRENTICE)
		{
			L2ClanMember sponsor = activeChar.getClan().getClanMember(_sponsorName);
			L2ClanMember apprentice = activeChar.getClan().getClanMember(_apprenticeName);
			if(sponsor != null && apprentice != null)
			{
				if(apprentice.getPledgeType() != L2Clan.SUBUNIT_ACADEMY || sponsor.getPledgeType() == L2Clan.SUBUNIT_ACADEMY)
					return; // hack?

				if(_mode == 1)
				{
					if(sponsor.hasApprentice())
					{
						activeChar.sendMessage(new CustomMessage("l2rt.gameserver.network.clientpackets.RequestOustAlly.MemberAlreadyHasApprentice", activeChar));
						return;
					}
					if(apprentice.hasSponsor())
					{
						activeChar.sendMessage(new CustomMessage("l2rt.gameserver.network.clientpackets.RequestOustAlly.ApprenticeAlreadyHasSponsor", activeChar));
						return;
					}
					sponsor.setApprentice(apprentice.getObjectId());
					clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(apprentice));
					clan.broadcastToOnlineMembers(new SystemMessage(SystemMessage.S2_HAS_BEEN_DESIGNATED_AS_THE_APPRENTICE_OF_CLAN_MEMBER_S1).addString(sponsor.getName()).addString(apprentice.getName()));
				}
				else
				{
					if(!sponsor.hasApprentice())
					{
						activeChar.sendMessage(new CustomMessage("l2rt.gameserver.network.clientpackets.RequestOustAlly.MemberHasNoApprentice", activeChar));
						return;
					}
					sponsor.setApprentice(0);
					clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(apprentice));
					clan.broadcastToOnlineMembers(new SystemMessage(SystemMessage.S2_CLAN_MEMBER_S1S_APPRENTICE_HAS_BEEN_REMOVED).addString(sponsor.getName()).addString(apprentice.getName()));
				}
				if(apprentice.isOnline())
					apprentice.getPlayer().broadcastUserInfo(true);
				activeChar.sendPacket(new PledgeReceiveMemberInfo(sponsor));
			}
		}
		else
			activeChar.sendMessage(new CustomMessage("l2rt.gameserver.network.clientpackets.RequestOustAlly.NoMasterRights", activeChar));
	}
}
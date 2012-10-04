package l2p.gameserver.clientpackets;

import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.pledge.UnitMember;
import l2p.gameserver.serverpackets.components.CustomMessage;
import l2p.gameserver.serverpackets.PledgeReceiveMemberInfo;
import l2p.gameserver.serverpackets.PledgeShowMemberListUpdate;
import l2p.gameserver.serverpackets.SystemMessage;

public class RequestPledgeSetAcademyMaster extends L2GameClientPacket
{
	private int _mode; // 1=set, 0=unset
	private String _sponsorName;
	private String _apprenticeName;

	@Override
	protected void readImpl()
	{
		_mode = readD();
		_sponsorName = readS(16);
		_apprenticeName = readS(16);
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		Clan clan = activeChar.getClan();
		if(clan == null)
			return;

		if((activeChar.getClanPrivileges() & Clan.CP_CL_APPRENTICE) == Clan.CP_CL_APPRENTICE)
		{
			UnitMember sponsor = activeChar.getClan().getAnyMember(_sponsorName);
			UnitMember apprentice = activeChar.getClan().getAnyMember(_apprenticeName);
			if(sponsor != null && apprentice != null)
			{
				if(apprentice.getPledgeType() != Clan.SUBUNIT_ACADEMY || sponsor.getPledgeType() == Clan.SUBUNIT_ACADEMY)
					return; // hack?

				if(_mode == 1)
				{
					if(sponsor.hasApprentice())
					{
						activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestOustAlly.MemberAlreadyHasApprentice", activeChar));
						return;
					}
					if(apprentice.hasSponsor())
					{
						activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestOustAlly.ApprenticeAlreadyHasSponsor", activeChar));
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
						activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestOustAlly.MemberHasNoApprentice", activeChar));
						return;
					}
					sponsor.setApprentice(0);
					clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(apprentice));
					clan.broadcastToOnlineMembers(new SystemMessage(SystemMessage.S2_CLAN_MEMBER_S1S_APPRENTICE_HAS_BEEN_REMOVED).addString(sponsor.getName()).addString(apprentice.getName()));
				}
				if(apprentice.isOnline())
					apprentice.getPlayer().broadcastCharInfo();
				activeChar.sendPacket(new PledgeReceiveMemberInfo(sponsor));
			}
		}
		else
			activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestOustAlly.NoMasterRights", activeChar));
	}
}
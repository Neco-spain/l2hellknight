package l2rt.gameserver.network.clientpackets;

import l2rt.Config;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.gameserver.model.L2Clan;
import l2rt.gameserver.model.L2ClanMember;
import l2rt.gameserver.model.L2Player;

public class RequestPledgeSetMemberPowerGrade extends L2GameClientPacket
{
	// format: (ch)Sd
	private int _powerGrade;
	private String _name;

	@Override
	public void readImpl()
	{
		_name = readS(Config.CNAME_MAXLEN);
		_powerGrade = readD();
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

		if((activeChar.getClanPrivileges() & L2Clan.CP_CL_MANAGE_RANKS) == L2Clan.CP_CL_MANAGE_RANKS)
		{
			L2ClanMember member = activeChar.getClan().getClanMember(_name);
			if(member != null)
			{
				if(clan.isAcademy(member.getPledgeType()))
				{
					activeChar.sendMessage("You cannot change academy member grade.");
					return;
				}
				if(_powerGrade > 5)
					member.setPowerGrade(clan.getAffiliationRank(member.getPledgeType()));
				else
					member.setPowerGrade(_powerGrade);
				if(member.isOnline())
					member.getPlayer().sendUserInfo(false);
			}
			else
				activeChar.sendMessage(new CustomMessage("l2rt.gameserver.network.clientpackets.RequestPledgeSetMemberPowerGrade.NotBelongClan", activeChar));
		}
		else
			activeChar.sendMessage(new CustomMessage("l2rt.gameserver.network.clientpackets.RequestPledgeSetMemberPowerGrade.HaveNotAuthority", activeChar));
	}
}
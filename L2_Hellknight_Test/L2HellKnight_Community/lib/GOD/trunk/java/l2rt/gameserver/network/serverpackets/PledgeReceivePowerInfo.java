package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.L2Clan;
import l2rt.gameserver.model.L2Clan.RankPrivs;
import l2rt.gameserver.model.L2ClanMember;

public class PledgeReceivePowerInfo extends L2GameServerPacket
{
	private int PowerGrade, privs;
	private String member_name;

	public PledgeReceivePowerInfo(L2ClanMember member)
	{
		PowerGrade = member.getPowerGrade();
		member_name = member.getName();
		if(member.isClanLeader())
			privs = L2Clan.CP_ALL;
		else
		{
			RankPrivs temp = member.getClan().getRankPrivs(member.getPowerGrade());
			if(temp != null)
				privs = temp.getPrivs();
			else
				privs = 0;
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x3d);
		writeD(PowerGrade);
		writeS(member_name);
		writeD(privs);
	}
}
package l2rt.gameserver.network.clientpackets;

import l2rt.Config;
import l2rt.gameserver.model.L2Clan;
import l2rt.gameserver.model.L2ClanMember;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.serverpackets.PledgeReceivePowerInfo;

public class RequestPledgeMemberPowerInfo extends L2GameClientPacket
{
	// format: chdS
	@SuppressWarnings("unused")
	private int _not_known;
	private String _target;

	@Override
	public void readImpl()
	{
		_not_known = readD();
		_target = readS(Config.CNAME_MAXLEN);
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		L2Clan clan = activeChar.getClan();
		if(clan != null)
		{
			L2ClanMember cm = clan.getClanMember(_target);
			if(cm != null)
				activeChar.sendPacket(new PledgeReceivePowerInfo(cm));
		}
	}
}
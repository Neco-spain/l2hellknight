package l2rt.gameserver.network.clientpackets;

import l2rt.Config;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Clan;
import l2rt.gameserver.model.L2ClanMember;
import l2rt.gameserver.model.L2Player;
import l2rt.util.Util;

import java.util.logging.Logger;

public class RequestGiveNickName extends L2GameClientPacket
{
	//Format: cSS
	static Logger _log = Logger.getLogger(RequestGiveNickName.class.getName());

	private String _target;
	private String _title;

	@Override
	public void readImpl()
	{
		_target = readS(Config.CNAME_MAXLEN);
		_title = readS();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(!_title.equals("") && !Util.isMatchingRegexp(_title, Config.CLAN_TITLE_TEMPLATE))
		{
			activeChar.sendMessage("Incorrect title.");
			return;
		}

		// Дворяне могут устанавливать/менять себе title
		if(activeChar.isNoble() && _target.matches(activeChar.getName()))
		{
			activeChar.setTitle(_title);
			activeChar.sendPacket(Msg.TITLE_HAS_CHANGED);
			activeChar.sendChanges();
			return;
		}
		// Can the player change/give a title?
		else if((activeChar.getClanPrivileges() & L2Clan.CP_CL_MANAGE_TITLES) != L2Clan.CP_CL_MANAGE_TITLES)
			return;

		if(activeChar.getClan().getLevel() < 3)
		{
			activeChar.sendPacket(Msg.TITLE_ENDOWMENT_IS_ONLY_POSSIBLE_WHEN_CLANS_SKILL_LEVELS_ARE_ABOVE_3);
			return;
		}

		L2ClanMember member = activeChar.getClan().getClanMember(_target);
		if(member != null)
		{
			member.setTitle(_title);
			if(member.isOnline())
			{
				member.getPlayer().sendPacket(Msg.TITLE_HAS_CHANGED);
				member.getPlayer().sendChanges();
			}
		}
		else
			activeChar.sendMessage(new CustomMessage("l2rt.gameserver.network.clientpackets.RequestGiveNickName.NotInClan", activeChar));

	}
}
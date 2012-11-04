package l2r.gameserver.handler.voicecommands.impl;

import java.util.List;

import l2r.gameserver.Config;
import l2r.gameserver.cache.Msg;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.skills.skillclasses.Call;
import l2r.gameserver.utils.Location;

public class Relocate extends Functions implements IVoicedCommandHandler
{

	private final String[] _commandList = new String[] { "km-all-to-me"	};

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
	    if (!Config.ENABLE_KM_ALL_TO_ME) 
	    {
	      return false;
	    }
		if(command.equalsIgnoreCase("km-all-to-me"))
		{
			if(!activeChar.isClanLeader())
			{
				activeChar.sendPacket(Msg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
				return false;
			}
			SystemMessage2 msg = Call.canSummonHere(activeChar);
			if(msg != null)
			{
				activeChar.sendPacket(msg);
				return false;
			}
			List<Player> players = activeChar.getClan().getOnlineMembers(activeChar.getObjectId());
			for(Player player : players)
			{
				if(Call.canBeSummoned(player) == null)
				{
					player.summonCharacterRequest(activeChar, Location.findAroundPosition(activeChar, 100, 150), 5);
				}
			}
			return true;
		}
		return false;
	}
}
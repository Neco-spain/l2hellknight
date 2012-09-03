package commands.voiced;

import l2rt.extensions.multilang.CustomMessage;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.handler.IVoicedCommandHandler;
import l2rt.gameserver.handler.VoicedCommandHandler;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.skills.skillclasses.Call;

public class Relocate extends Functions implements IVoicedCommandHandler, ScriptFile
{
	public static int SUMMON_PRICE = 5;

	private final String[] _commandList = new String[] { "km-all-to-me" };

	public String[] getVoicedCommandList()
	{
		return _commandList;
	}

	public boolean useVoicedCommand(String command, L2Player activeChar, String target)
	{
		if(command.equalsIgnoreCase("km-all-to-me"))
		{
			if(!activeChar.isClanLeader())
			{
				activeChar.sendPacket(Msg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
				return false;
			}

			SystemMessage msg = Call.canSummonHere(activeChar);
			if(msg != null)
			{
				activeChar.sendPacket(msg);
				return false;
			}

			if(activeChar.isAlikeDead())
			{
				activeChar.sendMessage(new CustomMessage("scripts.commands.voiced.Relocate.Dead", activeChar));
				return false;
			}

			L2Player[] clan = activeChar.getClan().getOnlineMembers(activeChar.getObjectId());

			for(L2Player pl : clan)
				if(Call.canBeSummoned(pl) == null)
					// Спрашиваем, согласие на призыв
					pl.summonCharacterRequest(activeChar.getName(), GeoEngine.findPointToStay(activeChar.getX(), activeChar.getY(), activeChar.getZ(), 100, 150, activeChar.getReflection().getGeoIndex()), SUMMON_PRICE);

			return true;
		}
		return false;
	}

	public void onLoad()
	{
		VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}
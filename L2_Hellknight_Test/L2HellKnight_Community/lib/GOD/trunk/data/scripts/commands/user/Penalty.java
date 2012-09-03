package commands.user;

import java.text.SimpleDateFormat;

import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.handler.IUserCommandHandler;
import l2rt.gameserver.handler.UserCommandHandler;
import l2rt.gameserver.instancemanager.InstancedZoneManager;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.util.Files;

/**
 * Support for commands:
 * /clanpenalty
 * /instancezone 
 */
public class Penalty extends Functions implements IUserCommandHandler, ScriptFile
{
	private static final int[] COMMAND_IDS = { 100, 114 };

	public boolean useUserCommand(int id, L2Player activeChar)
	{
		if(COMMAND_IDS[0] == id)
		{
			long leaveClan = 0;
			if(activeChar.getLeaveClanTime() != 0)
				leaveClan = activeChar.getLeaveClanTime() + 1 * 24 * 60 * 60 * 1000L;
			long deleteClan = 0;
			if(activeChar.getDeleteClanTime() != 0)
				deleteClan = activeChar.getDeleteClanTime() + 10 * 24 * 60 * 60 * 1000L;
			SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
			String html = Files.read("data/scripts/commands/user/penalty.htm", activeChar);

			if(activeChar.getClanId() == 0)
			{
				if(leaveClan == 0 && deleteClan == 0)
				{
					html = html.replaceFirst("%reason%", "No penalty is imposed.");
					html = html.replaceFirst("%expiration%", " ");
				}
				else if(leaveClan > 0 && deleteClan == 0)
				{
					html = html.replaceFirst("%reason%", "Penalty for leaving clan.");
					html = html.replaceFirst("%expiration%", format.format(leaveClan));
				}
				else if(deleteClan > 0)
				{
					html = html.replaceFirst("%reason%", "Penalty for dissolving clan.");
					html = html.replaceFirst("%expiration%", format.format(deleteClan));
				}
			}
			else if(activeChar.getClan().canInvite())
			{
				html = html.replaceFirst("%reason%", "No penalty is imposed.");
				html = html.replaceFirst("%expiration%", " ");
			}
			else
			{
				html = html.replaceFirst("%reason%", "Penalty for expelling clan member.");
				html = html.replaceFirst("%expiration%", format.format(activeChar.getClan().getExpelledMemberTime()));
			}
			show(html, activeChar);
		}
		else if(COMMAND_IDS[1] == id)
		{
			//TODO: Сделать отображение названия активного инстанса.
			// SystemMessage.INSTANT_ZONE_CURRENTLY_IN_USE__S1
			activeChar.sendPacket(Msg.INSTANCE_ZONE_TIME_LIMIT);
			int limit;
			boolean noLimit = true;
			InstancedZoneManager ilm = InstancedZoneManager.getInstance();
			for(String name : ilm.getNames())
			{
				limit = ilm.getTimeToNextEnterInstance(name, activeChar);
				if(limit > 0)
				{
					noLimit = false;
					activeChar.sendPacket(new SystemMessage(SystemMessage.S1_WILL_BE_AVAILABLE_FOR_RE_USE_AFTER_S2_HOURS_S3_MINUTES).addString(name).addNumber(limit / 60).addNumber(limit % 60));
				}
			}
			if(noLimit)
				activeChar.sendPacket(Msg.THERE_IS_NO_INSTANCE_ZONE_UNDER_A_TIME_LIMIT);
		}
		return false;
	}

	public final int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}

	public void onLoad()
	{
		UserCommandHandler.getInstance().registerUserCommandHandler(this);
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}
package l2rt.gameserver.network.clientpackets;

import l2rt.Config;
import l2rt.gameserver.communitybbs.CommunityBoard;
import l2rt.gameserver.handler.AdminCommandHandler;
import l2rt.gameserver.handler.IVoicedCommandHandler;
import l2rt.gameserver.handler.VoicedCommandHandler;
import l2rt.gameserver.model.BypassManager.DecodedBypass;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Multisell;
import l2rt.gameserver.model.L2Object;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.entity.olympiad.Olympiad;
import l2rt.gameserver.model.entity.olympiad.OlympiadManager;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.network.L2GameClient;
import l2rt.gameserver.network.serverpackets.NpcHtmlMessage;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestBypassToServer extends L2GameClientPacket
{
	//Format: cS
	private static Logger _log = Logger.getLogger(RequestBypassToServer.class.getName());
	private DecodedBypass bp = null;

	@Override
	public void readImpl()
	{
		String bypass = readS();
		if(!bypass.isEmpty())
			bp = getClient().getActiveChar().decodeBypass(bypass);
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null || bp == null)
			return;
		try
		{
			L2NpcInstance npc = activeChar.getLastNpc();
			L2Object target = activeChar.getTarget();
			if(npc == null && target != null && target.isNpc())
				npc = (L2NpcInstance) target;

			if(bp.bbs)
				CommunityBoard.getInstance().handleCommands(getClient(), bp.bypass);
			else if(bp.bypass.startsWith("admin_"))
				AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, bp.bypass);
			else if(bp.bypass.equals("come_here") && activeChar.isGM())
				comeHere(getClient());
			else if(bp.bypass.startsWith("player_help "))
				playerHelp(activeChar, bp.bypass.substring(12));
			else if(bp.bypass.startsWith("scripts_"))
			{
				String command = bp.bypass.substring(8).trim();
				String[] word = command.split("\\s+");
				String[] args = command.substring(word[0].length()).trim().split("\\s+");
				String[] path = word[0].split(":");
				if(path.length != 2)
				{
					_log.warning("Bad Script bypass!");
					return;
				}

				HashMap<String, Object> variables = new HashMap<String, Object>();

				if(npc != null)
					variables.put("npc", npc.getStoredId());
				else
					variables.put("npc", null);

				if(word.length == 1)
					activeChar.callScripts(path[0], path[1], new Object[] {}, variables);
				else
					activeChar.callScripts(path[0], path[1], new Object[] { args }, variables);
			}
			else if(bp.bypass.startsWith("user_"))
			{
				String command = bp.bypass.substring(5).trim();
				String word = command.split("\\s+")[0];
				String args = command.substring(word.length()).trim();
				IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(word);

				if(vch != null)
					vch.useVoicedCommand(word, activeChar, args);
				else
					_log.warning("Unknow voiced command '" + word + "'");
			}
			else if(bp.bypass.startsWith("npc_"))
			{
				int endOfId = bp.bypass.indexOf('_', 5);
				String id;
				if(endOfId > 0)
					id = bp.bypass.substring(4, endOfId);
				else
					id = bp.bypass.substring(4);
				L2Object object = activeChar.getVisibleObject(Integer.parseInt(id));
				if(object != null && object.isNpc() && endOfId > 0 && activeChar.isInRange(object.getLoc(), L2Character.INTERACTION_DISTANCE))
				{
					activeChar.setLastNpc((L2NpcInstance) object);
					((L2NpcInstance) object).onBypassFeedback(activeChar, bp.bypass.substring(endOfId + 1));
				}
			}
			// используется для перехода с арены на арену при обсервинге олимпиады
			else if(bp.bypass.startsWith("oly_"))
			{
				if(!Config.ENABLE_OLYMPIAD_SPECTATING)
					return;

				// Временно отключено, глючит.
				if(Boolean.TRUE)
					return;

				if(!activeChar.inObserverMode())
				{
					_log.warning(activeChar.getName() + " possible cheater: tried to switch arena usind not standart method!");
					return;
				}

				int arenaId = Integer.parseInt(bp.bypass.substring(4));

				OlympiadManager manager = Olympiad._manager;
				if(manager == null || manager.getOlympiadInstance(arenaId) == null)
					return;

				activeChar.switchOlympiadObserverArena(arenaId);
			}
			else if(bp.bypass.startsWith("manor_menu_select?")) // Navigate throught Manor windows
			{
				L2Object object = activeChar.getTarget();
				if(object != null && object.isNpc())
					((L2NpcInstance) object).onBypassFeedback(activeChar, bp.bypass);
			}
			else if(bp.bypass.startsWith("multisell "))
				L2Multisell.getInstance().SeparateAndSend(Integer.parseInt(bp.bypass.substring(10)), activeChar, 0);
			else if(bp.bypass.startsWith("Quest "))
			{
				String p = bp.bypass.substring(6).trim();
				int idx = p.indexOf(' ');
				if(idx < 0)
					activeChar.processQuestEvent(p, "", npc);
				else
					activeChar.processQuestEvent(p.substring(0, idx), p.substring(idx).trim(), npc);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			String st = "Bad RequestBypassToServer: " + bp.bypass;
			L2Object target = activeChar.getTarget();
			if(activeChar.getTarget() != null)
				if(target.isNpc())
					st = st + " via NPC #" + ((L2NpcInstance) target).getNpcId();
			_log.log(Level.WARNING, st, e);
		}
	}

	private void comeHere(L2GameClient client)
	{
		L2Object obj = client.getActiveChar().getTarget();
		if(obj != null && obj.isNpc())
		{
			L2NpcInstance temp = (L2NpcInstance) obj;
			L2Player activeChar = client.getActiveChar();
			temp.setTarget(activeChar);
			temp.moveToLocation(activeChar.getLoc(), 0, true);
		}
	}

	private void playerHelp(L2Player activeChar, String path)
	{
		String filename = "data/html/" + path;
		NpcHtmlMessage html = new NpcHtmlMessage(5);
		html.setFile(filename);
		activeChar.sendPacket(html);
	}
}
package l2.hellknight.gameserver.network.clientpackets;

import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2.hellknight.Config;
import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.communityboard.CommunityBoard;
import l2.hellknight.gameserver.datatables.AIOItemTable;
import l2.hellknight.gameserver.datatables.AdminCommandAccessRights;
import l2.hellknight.gameserver.handler.AdminCommandHandler;
import l2.hellknight.gameserver.handler.BypassHandler;
import l2.hellknight.gameserver.handler.IAdminCommandHandler;
import l2.hellknight.gameserver.handler.IBypassHandler;
import l2.hellknight.gameserver.handler.IVoicedCommandHandler;
import l2.hellknight.gameserver.handler.VoicedCommandHandler;
import l2.hellknight.gameserver.model.L2CharPosition;
import l2.hellknight.gameserver.model.L2Object;
import l2.hellknight.gameserver.model.L2World;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2MerchantSummonInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.entity.CTF;
import l2.hellknight.gameserver.model.entity.Hero;
import l2.hellknight.gameserver.model.entity.L2Event;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.communityserver.CommunityServerThread;
import l2.hellknight.gameserver.network.communityserver.writepackets.RequestShowCommunityBoard;
import l2.hellknight.gameserver.network.serverpackets.ActionFailed;
import l2.hellknight.gameserver.network.serverpackets.ConfirmDlg;
import l2.hellknight.gameserver.network.serverpackets.NpcHtmlMessage;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;
import l2.hellknight.gameserver.util.GMAudit;

public final class RequestBypassToServer extends L2GameClientPacket
{
	private static final String _C__23_REQUESTBYPASSTOSERVER = "[C] 23 RequestBypassToServer";
	private static Logger _log = Logger.getLogger(RequestBypassToServer.class.getName());

	// S
	private String _command;

	@Override
	protected void readImpl()
	{
		_command = readS();
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		if (!getClient().getFloodProtectors().getServerBypass().tryPerformAction(_command))
			return;
		
		if(_command.isEmpty())
		{
			_log.info(activeChar.getName()+" send empty requestbypass");
			activeChar.logout();
			return;
		}
		
		try
		{
			if (_command.startsWith("admin_")) //&& activeChar.getAccessLevel() >= Config.GM_ACCESSLEVEL)
			{
				String command = _command.split(" ")[0];

				IAdminCommandHandler ach = AdminCommandHandler.getInstance().getAdminCommandHandler(command);				
				if (ach == null)
				{
					if ( activeChar.isGM() )
						activeChar.sendMessage("The command " + command.substring(6) + " does not exist!");

					_log.warning("No handler registered for admin command '" + command + "'");
					return;
				}

				if (!AdminCommandAccessRights.getInstance().hasAccess(command, activeChar.getAccessLevel()))
				{
					activeChar.sendMessage("You don't have the access rights to use this command!");
					_log.warning("Character " + activeChar.getName() + " tried to use admin command " + command + ", without proper access level!");
					return;
				}
				
				if (AdminCommandAccessRights.getInstance().requireConfirm(command))
				{
					activeChar.setAdminConfirmCmd(_command);
					ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.S1);
					dlg.addString("Are you sure you want execute command "+_command.substring(6)+" ?");
					activeChar.sendPacket(dlg);
				}
				else
				{
					if (Config.GMAUDIT)
						GMAudit.auditGMAction(activeChar.getName()+" ["+activeChar.getObjectId()+"]", _command, (activeChar.getTarget() != null?activeChar.getTarget().getName():"no-target"));

					ach.useAdminCommand(_command, activeChar);
				}
			}
			else if (_command.equals("come_here") && ( activeChar.isGM()))
			{
				comeHere(activeChar);
			}
			else if (_command.startsWith("npc_"))
			{
				if(!activeChar.validateBypass(_command))
					return;

				int endOfId = _command.indexOf('_', 5);
				String id;
				if (endOfId > 0)
					id = _command.substring(4, endOfId);
				else
					id = _command.substring(4);
				
				try
				{
					L2Object object = L2World.getInstance().findObject(Integer.parseInt(id));

					if (_command.substring(endOfId+1).startsWith("event_participate"))
						L2Event.inscribePlayer(activeChar);
					else if (object instanceof L2Npc && endOfId > 0 && activeChar.isInsideRadius(object, L2Npc.INTERACTION_DISTANCE, false, false))
						{
							((L2Npc)object).onBypassFeedback(activeChar, _command.substring(endOfId+1));
						}
						
					if (_command.substring(endOfId + 1).startsWith("ctf_player_join "))
					{
						String teamName = _command.substring(endOfId + 1).substring(16);
						
						if (CTF._joining)
							CTF.addPlayer(activeChar, teamName);
						else
							activeChar.sendMessage("The event is already started. You can not join now!");
					}
					else if (_command.substring(endOfId + 1).startsWith("ctf_player_leave"))
					{
						if (CTF._joining)
							CTF.removePlayer(activeChar);
						else
							activeChar.sendMessage("The event is already started. You can not leave now!");
					}
										
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				}
				catch (NumberFormatException nfe) {}
			}
			else if (_command.startsWith("summon_"))
			{
				if(!activeChar.validateBypass(_command))
					return;

				int endOfId = _command.indexOf('_', 8);
				String id;
				if (endOfId > 0)
					id = _command.substring(7, endOfId);
				else
					id = _command.substring(7);
				try
				{
					L2Object object = L2World.getInstance().findObject(Integer.parseInt(id));

					if (object instanceof L2MerchantSummonInstance && endOfId > 0 && activeChar.isInsideRadius(object, L2Npc.INTERACTION_DISTANCE, false, false))
						((L2MerchantSummonInstance)object).onBypassFeedback(activeChar, _command.substring(endOfId+1));

					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				}
				catch (NumberFormatException nfe) {}
			}
			// Navigate through Manor windows
			else if (_command.startsWith("manor_menu_select"))
			{
				final IBypassHandler manor = BypassHandler.getInstance().getBypassHandler("manor_menu_select");
				if (manor != null)
					manor.useBypass(_command, activeChar, null);
			}
			else if (_command.startsWith("bbs_"))
			{
				if (Config.ENABLE_COMMUNITY_BOARD)
				{
					if (!CommunityServerThread.getInstance().sendPacket(new RequestShowCommunityBoard(activeChar.getObjectId(), _command)))
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CB_OFFLINE));
				}
				else
					CommunityBoard.getInstance().handleCommands(getClient(), _command);
			}
			else if (_command.startsWith("_bbsloc"))
			{
				CommunityBoard.getInstance().handleCommands(getClient(), _command);
			}
			else if (_command.startsWith("_bbs"))
			{
				if (Config.ENABLE_COMMUNITY_BOARD)
				{
					if (!CommunityServerThread.getInstance().sendPacket(new RequestShowCommunityBoard(activeChar.getObjectId(), _command)))
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CB_OFFLINE));
				}
				else
					CommunityBoard.getInstance().handleCommands(getClient(), _command);
			}
			else if (_command.startsWith("_mail"))
			{
				if (!CommunityServerThread.getInstance().sendPacket(new RequestShowCommunityBoard(activeChar.getObjectId(), "_bbsmail")))
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CB_OFFLINE));
			}
			else if (_command.startsWith("_friend"))
			{
				if (!CommunityServerThread.getInstance().sendPacket(new RequestShowCommunityBoard(activeChar.getObjectId(), "_bbsfriend")))
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CB_OFFLINE));
			}
			else if (_command.startsWith("Quest "))
			{
				if(!activeChar.validateBypass(_command))
					return;

				L2PcInstance player = getClient().getActiveChar();
				if (player == null)
					return;

				String p = _command.substring(6).trim();
				int idx = p.indexOf(' ');
				if (idx < 0)
					player.processQuestEvent(p, "");
				else
					player.processQuestEvent(p.substring(0, idx), p.substring(idx).trim());
			}
			else if (_command.startsWith("_match"))
			{
				L2PcInstance player = getClient().getActiveChar();
				if (player == null)
					return;
				
				String params = _command.substring(_command.indexOf("?")+1);
				StringTokenizer st = new StringTokenizer(params, "&");
				int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
				int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
				int heroid = Hero.getInstance().getHeroByClass(heroclass);
				if (heroid > 0)
				{
					Hero.getInstance().showHeroFights(player, heroclass, heroid, heropage);
				}
			}
			else if (_command.startsWith("_diary"))
			{
				L2PcInstance player = getClient().getActiveChar();
				if (player == null)
					return;
				
				String params = _command.substring(_command.indexOf("?")+1);
				StringTokenizer st = new StringTokenizer(params, "&");
				int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
				int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
				int heroid = Hero.getInstance().getHeroByClass(heroclass);
				if (heroid > 0)
				{
					Hero.getInstance().showHeroDiary(player, heroclass, heroid, heropage);
				}
			}
			else if (_command.startsWith("voice "))
			{
				// only voice commands allowed in bypass
				if (_command.length() > 7
						&& _command.charAt(6) == '.')
				{
					final String vc, vparams;
					final int endOfCommand = _command.indexOf(" ", 7);
					if (endOfCommand > 0)
					{
						vc = _command.substring(7, endOfCommand).trim();
						vparams = _command.substring(endOfCommand).trim();
					}
					else
					{
						vc = _command.substring(7).trim();
						vparams = null;
					}

					if (vc.length() > 0)
					{
						final IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(vc);
						if (vch != null)
							vch.useVoicedCommand(vc, activeChar, vparams);
					}
				}
			}
			else if(_command.startsWith("Aioitem"))
			{
				AIOItemTable.getInstance().handleBypass(activeChar, _command);
			}
			else
			{
				final IBypassHandler handler = BypassHandler.getInstance().getBypassHandler(_command);
				if (handler != null)
					handler.useBypass(_command, activeChar, null);
				else
					_log.log(Level.WARNING, getClient()+" sent not handled RequestBypassToServer: ["+_command+"]");
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, getClient()+" sent bad RequestBypassToServer: \""+_command+"\"", e);
			if (activeChar.isGM())
			{
				StringBuilder sb = new StringBuilder(200);
				sb.append("<html><body>");
				sb.append("Bypass error: "+e+"<br1>");
				sb.append("Bypass command: "+_command+"<br1>");
				sb.append("StackTrace:<br1>");
				for (StackTraceElement ste : e.getStackTrace())
					sb.append(ste.toString()+"<br1>");
				sb.append("</body></html>");
				
				// item html
				NpcHtmlMessage msg = new NpcHtmlMessage(0,12807);
				msg.setHtml(sb.toString());
				msg.disableValidation();
				activeChar.sendPacket(msg);
			}
		}
	}

	/**
	 * @param client
	 */
	private static void comeHere(L2PcInstance activeChar)
	{
		L2Object obj = activeChar.getTarget();
		if (obj == null)
			return;
		
		if (obj instanceof L2Npc)
		{
			L2Npc temp = (L2Npc) obj;
			temp.setTarget(activeChar);
			temp.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(activeChar.getX(),activeChar.getY(), activeChar.getZ(), 0 ));
		}
	}

	@Override
	public String getType()
	{
		return _C__23_REQUESTBYPASSTOSERVER;
	}
}

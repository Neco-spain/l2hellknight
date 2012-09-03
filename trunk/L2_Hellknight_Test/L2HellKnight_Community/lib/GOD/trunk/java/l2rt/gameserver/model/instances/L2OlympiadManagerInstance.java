package l2rt.gameserver.model.instances;

import l2rt.Config;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.gameserver.model.L2Multisell;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.entity.Hero;
import l2rt.gameserver.model.entity.olympiad.*;
import l2rt.gameserver.network.serverpackets.ExHeroList;
import l2rt.gameserver.network.serverpackets.NpcHtmlMessage;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.util.GArray;

import java.util.logging.Logger;

public class L2OlympiadManagerInstance extends L2NpcInstance
{
	private static Logger _log = Logger.getLogger(L2OlympiadManagerInstance.class.getName());

	public L2OlympiadManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		if(Config.ENABLE_OLYMPIAD && template.npcId == 31688)
			Olympiad.addOlympiadNpc(this);
	}

	@Override
	public void onBypassFeedback(L2Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(!Config.ENABLE_OLYMPIAD)
			return;

		if(command.startsWith("OlympiadDesc"))
		{
			int val = Integer.parseInt(command.substring(13, 14));
			String suffix = command.substring(14);
			showChatWindow(player, val, suffix);
		}
		else if(command.startsWith("OlympiadNoble"))
		{
			if(!Config.ENABLE_OLYMPIAD || !Olympiad.isNoble(player.getObjectId()) || !player.isNoble() || player.getBaseClassId() != player.getClassId().getId())
				return;

			int val = Integer.parseInt(command.substring(14));
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);

			switch(val)
			{
				case 1:
					Olympiad.unRegisterNoble(player);
					break;
				case 2:
					int classed = 0;
					int nonClassed = 0;
					int nonClassedRandomTeam = 0;
					int nonClassedTeam = 0;
					int[] array = Olympiad.getWaitingList();

					if(array != null)
					{
						classed = array[0];
						nonClassed = array[1];
						nonClassedRandomTeam = array[2];
						nonClassedTeam = array[3];
					}

					html.setFile(Olympiad.OLYMPIAD_HTML_PATH + "noble_registered.htm");
					if(Config.ALT_OLY_REG_DISPLAY > 0)
					{
						String FEWER_THAN = new CustomMessage("l2rt.gameserver.model.instances.L2OlympiadManagerInstance.Fewer", player, Config.ALT_OLY_REG_DISPLAY).toString();
						String MORE_THAN = new CustomMessage("l2rt.gameserver.model.instances.L2OlympiadManagerInstance.More", player, Config.ALT_OLY_REG_DISPLAY).toString();
						html.replace("%listClassed%", classed < Config.ALT_OLY_REG_DISPLAY ? FEWER_THAN : MORE_THAN);
						html.replace("%listNonClassedRandomTeam%", nonClassedRandomTeam < Config.ALT_OLY_REG_DISPLAY ? FEWER_THAN : MORE_THAN);
						html.replace("%listNonClassedTeam%", nonClassedTeam < Config.ALT_OLY_REG_DISPLAY ? FEWER_THAN : MORE_THAN);
						html.replace("%listNonClassed%", nonClassed < Config.ALT_OLY_REG_DISPLAY ? FEWER_THAN : MORE_THAN);
					}
					else
					{
						html.replace("%listClassed%", String.valueOf(classed));
						html.replace("%listNonClassedRandomTeam%", String.valueOf(nonClassedRandomTeam));
						html.replace("%listNonClassedTeam%", String.valueOf(nonClassedTeam));
						html.replace("%listNonClassed%", String.valueOf(nonClassed));
					}

					player.sendPacket(html);
					break;
				case 3:
					int points = Olympiad.getNoblePoints(player.getObjectId());
					html.setFile(Olympiad.OLYMPIAD_HTML_PATH + "noble_points1.htm");
					html.replace("%points%", String.valueOf(points));
					player.sendPacket(html);
					break;
				case 4:
					Olympiad.registerNoble(player, CompType.NON_CLASSED);
					break;
				case 5:
					Olympiad.registerNoble(player, CompType.CLASSED);
					break;
				case 6:
					int passes = Olympiad.getNoblessePasses(player);
					if(passes > 0)
					{
						player.getInventory().addItem(Config.ALT_OLY_COMP_RITEM, passes);
						player.sendPacket(SystemMessage.obtainItems(Config.ALT_OLY_COMP_RITEM, passes, 0));
					}
					else
						player.sendPacket(html.setFile(Olympiad.OLYMPIAD_HTML_PATH + "noble_nopoints.htm"));
					break;
				case 7:
					L2Multisell.getInstance().SeparateAndSend(102, player, 0);
					break;
				case 8:
					int point = Olympiad.getNoblePointsPast(player.getObjectId());
					html.setFile(Olympiad.OLYMPIAD_HTML_PATH + "noble_points2.htm");
					html.replace("%points%", String.valueOf(point));
					player.sendPacket(html);
					break;
				case 9:
					L2Multisell.getInstance().SeparateAndSend(103, player, 0);
					break;
				case 10:
					Olympiad.registerNoble(player, CompType.TEAM_RANDOM);
					break;
				case 11:
					Olympiad.registerNoble(player, CompType.TEAM);
					break;
				default:
					_log.warning("Olympiad System: Couldnt send packet for request " + val);
					break;
			}
		}
		else if(command.startsWith("Olympiad"))
		{
			if(!Config.ENABLE_OLYMPIAD)
				return;
			int val = Integer.parseInt(command.substring(9, 10));

			NpcHtmlMessage reply = new NpcHtmlMessage(player, this);

			switch(val)
			{
				case 1:
					StringBuffer replace = new StringBuffer("");

					OlympiadManager manager = Olympiad._manager;
					if(manager != null)
						for(int i = 0; i < Olympiad.STADIUMS.length; i++)
						{
							OlympiadGame game = manager.getOlympiadInstance(i);
							if(game != null && game.getState() > 0)
							{
								if(game.getType() == CompType.TEAM || game.getType() == CompType.TEAM_RANDOM)
								{
									replace.append("<br1>Arena " + (i + 1) + ":&nbsp;<a action=\"bypass -h npc_%objectId%_Olympiad 3_" + i + "\">Team vs Team:</a>");
									replace.append("<br1>- " + game.getTeam1Title() + "<br1>- " + game.getTeam2Title());
								}
								else
									replace.append("<br1>Arena " + (i + 1) + ":&nbsp;<a action=\"bypass -h npc_%objectId%_Olympiad 3_" + i + "\">" + manager.getOlympiadInstance(i).getTitle() + "</a>");
								replace.append("<img src=\"L2UI.SquareWhite\" width=270 height=1> <img src=\"L2UI.SquareBlank\" width=1 height=3>");
							}
						}

					reply.setFile(Olympiad.OLYMPIAD_HTML_PATH + "olympiad_observe.htm");
					reply.replace("%arenas%", replace.toString());
					player.sendPacket(reply);
					break;
				case 2:
					// for example >> Olympiad 1_88
					int classId = Integer.parseInt(command.substring(11));
					if(classId >= 88)
					{
						reply.setFile(Olympiad.OLYMPIAD_HTML_PATH + "olympiad_ranking.htm");

						GArray<String> names = OlympiadDatabase.getClassLeaderBoard(classId);

						int index = 1;
						for(String name : names)
						{
							reply.replace("%place" + index + "%", String.valueOf(index));
							reply.replace("%rank" + index + "%", name);
							index++;
							if(index > 10)
								break;
						}
						for(; index <= 10; index++)
						{
							reply.replace("%place" + index + "%", "");
							reply.replace("%rank" + index + "%", "");
						}

						player.sendPacket(reply);
					}
					// TODO Send player each class rank
					break;
				case 3:
					if(!Config.ENABLE_OLYMPIAD_SPECTATING)
						break;
					Olympiad.addSpectator(Integer.parseInt(command.substring(11)), player);
					break;
				case 4:
					player.sendPacket(new ExHeroList());
					break;
				case 5:
					StringBuffer replyMSG = new StringBuffer("<html><body>");
					if(Hero.getInstance().isInactiveHero(player.getObjectId()))
					{
						Hero.getInstance().activateHero(player);
						replyMSG.append("Congratulations! You are a Hero now.");
					}
					else
						replyMSG.append("You cannot be a Hero.");
					replyMSG.append("</body></html>");
					reply.setHtml(replyMSG.toString());
					player.sendPacket(reply);
					break;
				default:
					_log.warning("Olympiad System: Couldnt send packet for request " + val);
					break;
			}
		}
		else
			super.onBypassFeedback(player, command);
	}

	private void showChatWindow(L2Player player, int val, String suffix)
	{
		String filename = Olympiad.OLYMPIAD_HTML_PATH;
		filename += "noble_desc" + val;
		filename += suffix != null ? suffix + ".htm" : ".htm";
		if(filename.equals(Olympiad.OLYMPIAD_HTML_PATH + "noble_desc0.htm"))
			filename = Olympiad.OLYMPIAD_HTML_PATH + "noble_main.htm";
		player.sendPacket(new NpcHtmlMessage(player, this, filename, val));
	}
}
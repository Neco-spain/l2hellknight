package l2rt.gameserver.communitybbs;

import javolution.text.TextBuilder;
import l2rt.config.ConfigSystem;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.communitybbs.Manager.*;
import l2rt.gameserver.model.L2Multisell;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.base.Experience;
import l2rt.gameserver.model.entity.Hero;
import l2rt.gameserver.model.entity.olympiad.Olympiad;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.L2GameClient;
import l2rt.gameserver.network.serverpackets.ShowBoard;
import l2rt.gameserver.network.serverpackets.SkillList;
import l2rt.gameserver.network.serverpackets.SocialAction;
import l2rt.gameserver.tables.SkillTable;
import l2rt.gameserver.templates.L2Item;
import l2rt.gameserver.templates.StatsSet;
import l2rt.gameserver.xml.ItemTemplates;
import l2rt.util.Files;
import l2rt.util.GArray;

import java.util.HashMap;
import java.util.StringTokenizer;

public class CommunityBoard
{
	private static CommunityBoard _instance;
	private static int MONEY_ID = 4357;

	public static CommunityBoard getInstance()
	{
		if (_instance == null)
			_instance = new CommunityBoard();
		return _instance;
	}

	public void handleCommands(L2GameClient client, String command)
	{
		L2Player activeChar = client.getActiveChar();
		if (activeChar == null)
			return;

		if (!ConfigSystem.getBoolean("AllowCommunityBoard"))
		{
			activeChar.sendPacket(Msg.THE_COMMUNITY_SERVER_IS_CURRENTLY_OFFLINE);
			return;
		}
		if (!ConfigSystem.getBoolean("AllowCBInAbnormalState"))
		{
			if (activeChar.isDead() || activeChar.isAlikeDead() || activeChar.isCastingNow() || activeChar.isInCombat() || activeChar.isAttackingNow() || activeChar.isInOlympiadMode() || activeChar.isInVehicle() || activeChar.isFlying() || activeChar.isInFlyingTransform())
			{
				FailBBSManager.getInstance().parsecmd(command, activeChar);
				return;
			}
		}

		if (command.startsWith("_bbsclan"))
			ClanBBSManager.getInstance().parsecmd(command, activeChar);
		else if (command.startsWith("_bbsmemo"))
			TopicBBSManager.getInstance().parsecmd(command, activeChar);
		else if (command.startsWith("_bbstopics"))
			TopicBBSManager.getInstance().parsecmd(command, activeChar);
		else if (command.startsWith("_bbsposts"))
			PostBBSManager.getInstance().parsecmd(command, activeChar);
		else if (command.startsWith("_bbstop"))
			TopBBSManager.getInstance().parsecmd(command, activeChar);
		else if (command.startsWith("_bbshome"))
			TopBBSManager.getInstance().parsecmd(command, activeChar);
		else if (command.startsWith("_bbsloc"))
			RegionBBSManager.getInstance().parsecmd(command, activeChar);
		else if (command.startsWith("_friend") || command.startsWith("_block"))
			FriendsBBSManager.getInstance().parsecmd(command, activeChar);
		/* Попытка реализации аукциона в комьюнити боард
		 * else if (command.startsWith("_bbsauction"))
			AuctionBBSManager.getInstance().parsecmd(command, activeChar);*/
		else if (command.startsWith("_bbsgetfav"))
			GetFavBBSManager.getInstance().parsecmd(command, activeChar);
		else if (command.startsWith("bbs_add_fav"))
			AddFavBBSManager.getInstance().parsecmd(command, activeChar);
		else if (command.startsWith("_bbslink"))
			LinkBBSManager.getInstance().parsecmd(command, activeChar);
		else if(command.startsWith("_maillist")) 
		    MailBBSManager.getInstance().parsecmd(command, activeChar);
		else if (command.startsWith("_bbsteleport;"))
		{
			if (!ConfigSystem.getBoolean("AllowCBTeleport"))
			{
				FailBBSManager.getInstance().parsecmd(command, activeChar);
				return;
			}

			TeleportBBSManager.getInstance().parsecmd(command, activeChar);
		}
		else if (command.startsWith("_bbsechant"))
		{
			if (!ConfigSystem.getBoolean("AllowCBEnchant"))
			{
				FailBBSManager.getInstance().parsecmd(command, activeChar);
				return;
			}

			EnchantBBSManager.getInstance().parsecmd(command, activeChar);
		}
		else if (command.startsWith("_bbsclass"))
		{
			if (!ConfigSystem.getBoolean("AllowCBClassMaster"))
			{
				FailBBSManager.getInstance().parsecmd(command, activeChar);
				return;
			}

			ClassBBSManager.getInstance().parsecmd(command, activeChar);
		}
		else if (command.startsWith("_bbssms;"))
			SmsBBSManager.getInstance().parsecmd(command, activeChar);
		else if (command.startsWith("_bbsbuff;"))
			BuffBBSManager.getInstance().parsecmd(command, activeChar);
		else if (command.startsWith("_bbsstat;"))
			StatBBSManager.getInstance().parsecmd(command, activeChar);
		else if (command.startsWith("_bbsmultisell;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			TopBBSManager.getInstance().parsecmd("_bbstop;" + st.nextToken(), activeChar);
			L2Multisell.getInstance().SeparateAndSend(Integer.parseInt(st.nextToken()), activeChar, 0);
		}
		else if (command.startsWith("_bbsscripts;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			TopBBSManager.getInstance().parsecmd("_bbstop;" + st.nextToken(), activeChar);

			String com = st.nextToken();
			String[] word = com.split("\\s+");
			String[] args = com.substring(word[0].length()).trim().split("\\s+");
			String[] path = word[0].split(":");
			if (path.length != 2)
			{
				System.out.println("Bad Script bypass!");
				return;
			}

			HashMap<String, Object> variables = new HashMap<String, Object>();
			variables.put("npc", null);
			activeChar.callScripts(path[0], path[1], word.length == 1 ? new Object[] {} : new Object[] { args }, variables);
		}
		else if (command.startsWith("_bbsscripts_ret;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			String page = st.nextToken();

			String com = st.nextToken();
			String[] word = com.split("\\s+");
			String[] args = com.substring(word[0].length()).trim().split("\\s+");
			String[] path = word[0].split(":");
			if (path.length != 2)
			{
				System.out.println("Bad Script bypass!");
				return;
			}
			HashMap<String, Object> variables = new HashMap<String, Object>();
			variables.put("npc", null);
			Object subcontent = activeChar.callScripts(path[0], path[1], word.length == 1 ? new Object[] {} : new Object[] { args }, variables);

			TopBBSManager.getInstance().showTopPage(activeChar, page, String.valueOf(subcontent));
		}
		else if (command.startsWith("_bbssps;"))
		{
			int price = 1;
			L2Item item = ItemTemplates.getInstance().getTemplate(4357);
			L2ItemInstance pay = activeChar.getInventory().getItemByItemId(item.getItemId());
			if (pay != null && pay.getCount() >= price)
			{
				activeChar.getInventory().destroyItem(pay, price, true);
				activeChar.setSp(activeChar.getSp() + 10000000);
				activeChar.sendMessage("Вы получили 10kk SP");
				activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 16));
				activeChar.broadcastUserInfo(true);
			}
			else
				activeChar.sendMessage("Недостаточно средств.");
		}
		else if (command.startsWith("_bbsspa;"))
		{
			int price = 100000000;
			L2Item item = ItemTemplates.getInstance().getTemplate(57);
			L2ItemInstance pay = activeChar.getInventory().getItemByItemId(item.getItemId());
			if (pay != null && pay.getCount() >= price)
			{
				activeChar.getInventory().destroyItem(pay, price, true);
				activeChar.setSp(activeChar.getSp() + 10000000);
				activeChar.sendMessage("Вы получили 10kk SP");
				activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 16));
				activeChar.broadcastUserInfo(true);
			}
			else
				activeChar.sendMessage("Недостаточно средств.");
		}
		else if (command.startsWith("_bbsnobles;"))
		{
			if (!activeChar.isNoble())
			{
				if (!checkCondition(activeChar, 30))
					return;

				if (activeChar.getSubLevel() < 75)
				{
					activeChar.sendMessage("Чтобы стать дворянином, вы должны прокачать сабкласс до 75-го уровня");
					return;
				}

				L2Item item = ItemTemplates.getInstance().getTemplate(MONEY_ID);
				L2ItemInstance pay = activeChar.getInventory().getItemByItemId(item.getItemId());
				if (pay != null && pay.getCount() >= 30)
				{
					activeChar.getInventory().destroyItem(pay, 30, true);

					Olympiad.addNoble(activeChar);
					activeChar.setNoble(true);
					activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 3));
					activeChar.updatePledgeClass();
					activeChar.updateNobleSkills();
					activeChar.sendPacket(new SkillList(activeChar));
					activeChar.broadcastUserInfo(true);
				}
				else
					activeChar.sendMessage("Недостаточно средств.");
			}
			else
				activeChar.sendMessage("Вы уже являетесь дворянином. Операция отменена.");
		}
		else if (command.equals("_bbslvlup"))
		{
			String name = "None Name";
			name = ItemTemplates.getInstance().getTemplate(ConfigSystem.getInt("CBLvlUpItem")).getName();
			TextBuilder sb = new TextBuilder();
			sb.append("<html><body><br><br><center>");
			sb.append(new StringBuilder("Поднять Lvl за: <font color=\"LEVEL\">" + name + "</font>"));
			sb.append("<img src=\"l2ui.squaregray\" width=\"170\" height=\"1\">");
			for(int i = 0; i < ConfigSystem.getIntArray("CBLvlUp").length; i++)
			{
				if(activeChar.getLevel() < ConfigSystem.getIntArray("CBLvlUp")[i])
				{
					sb.append(new StringBuilder("<button value=\"Поднять Lvl до: " + ConfigSystem.getIntArray("CBLvlUp")[i] + " (Цена:" + ConfigSystem.getIntArray("CBLvlUpPrice")[i] + " " + name + ") \" action=\"bypass -h _bbslvlup;up;" + ConfigSystem.getIntArray("CBLvlUp")[i] + ";" + ConfigSystem.getIntArray("CBLvlUpPrice")[i] + "\" width=300 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">"));
					sb.append("<br1>");
				}
			}
			sb.append("</center><br><br></body></html>");
			String content = Files.read("data/html/CommunityBoardPVP/805.htm", activeChar);
			content = content.replace("%lvlup%", sb.toString());
			ShowBoard.separateAndSend(content, activeChar);
		}
		else if (command.startsWith("_bbslvlup;up;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int level = Integer.parseInt(st.nextToken());
			int count = Integer.parseInt(st.nextToken());

			L2Item item = ItemTemplates.getInstance().getTemplate(ConfigSystem.getInt("CBLvlUpItem"));
			L2ItemInstance pay = activeChar.getInventory().getItemByItemId(item.getItemId());
			if (pay != null && pay.getCount() >= count)
			{
				activeChar.getInventory().destroyItem(pay, count, true);
				setLevel(activeChar, level);
			}
			else
				activeChar.sendPacket(Msg.INCORRECT_ITEM_COUNT);
		}
		else if (command.equals("_bbshero"))
		{
			String name = "None Name";
			name = ItemTemplates.getInstance().getTemplate(ConfigSystem.getInt("CBHeroItem")).getName();
			TextBuilder sb = new TextBuilder();
			sb.append("<html><body><br><br><center>");
			sb.append("Купить статус Героя за: <font color=\"LEVEL\">" + name + "</font>");
			sb.append("<img src=\"l2ui.squaregray\" width=\"170\" height=\"1\">");
			sb.append(new StringBuilder("<button value=\"Купить статус Героя за: " + ConfigSystem.getInt("CBHeroItemPrice")+ " " + name + " \" action=\"bypass -h _bbshero;set\" width=300 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">"));
			sb.append("<br1>");
			sb.append("</center><br><br></body></html>");
			String content = Files.read("data/html/CommunityBoardPVP/806.htm", activeChar);
			content = content.replace("%sethero%", sb.toString());
			ShowBoard.separateAndSend(content, activeChar);
		}
		else if (command.startsWith("_bbshero;set"))
		{
			int count = ConfigSystem.getInt("CBHeroItemPrice");

			L2Item item = ItemTemplates.getInstance().getTemplate(ConfigSystem.getInt("CBHeroItem"));
			L2ItemInstance pay = activeChar.getInventory().getItemByItemId(item.getItemId());
			if (pay != null && pay.getCount() >= count)
			{
				activeChar.getInventory().destroyItem(pay, count, true);
				StatsSet hero = new StatsSet();
				hero.set("class_id", activeChar.getBaseClassId());
				hero.set("char_id", activeChar.getObjectId());
				hero.set("char_name", activeChar.getName());

				GArray<StatsSet> heroesToBe = new GArray<StatsSet>();
				heroesToBe.add(hero);
				Hero.getInstance().computeNewHeroes(heroesToBe);
				
				activeChar.setHero(true);
				activeChar.addSkill(SkillTable.getInstance().getInfo(395, 1));
				activeChar.addSkill(SkillTable.getInstance().getInfo(396, 1));
				activeChar.addSkill(SkillTable.getInstance().getInfo(1374, 1));
				activeChar.addSkill(SkillTable.getInstance().getInfo(1375, 1));
				activeChar.addSkill(SkillTable.getInstance().getInfo(1376, 1));
				activeChar.sendPacket(new SkillList(activeChar));
				if(activeChar.isHero())
					activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 16));
				activeChar.broadcastUserInfo(true);
			}
			else
				activeChar.sendPacket(Msg.INCORRECT_ITEM_COUNT);
		}
		else
		{
			ShowBoard.separateAndSend("<html><body><br><br><center>Функция: " + command + " пока не реализована</center><br><br></body></html>", activeChar);
		}
	}

	private void setLevel(L2Player activeChar, int level)
	{
		Long exp_add = Experience.LEVEL[level] - activeChar.getExp();
		activeChar.addExpAndSp(exp_add, 0, false, false);
	}

	public static boolean checkCondition(L2Player activeChar, int CoinCount)
	{
		synchronized (activeChar)
		{
			L2ItemInstance Coin = activeChar.getInventory().getItemByItemId(MONEY_ID);

			if (activeChar.isSitting())
				return false;
			if (Coin.getCount() < CoinCount)
			{
				activeChar.sendMessage("Недостаточно средств.");
				return false;
			}

			return true;
		}
	}

	public void handleWriteCommands(L2GameClient client, String url, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
		L2Player activeChar = client.getActiveChar();
		if (activeChar == null)
			return;
		if (!ConfigSystem.getBoolean("AllowCommunityBoard"))
		{
			activeChar.sendPacket(Msg.THE_COMMUNITY_SERVER_IS_CURRENTLY_OFFLINE);
			return;
		}

		if (url.equals("Topic"))
			TopicBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
		else if (url.equals("Post"))
			PostBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
		else if (url.equals("Region"))
			RegionBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
		else if (url.equals("Notice"))
		{
			if (arg4.length() > 512)
			{
				ShowBoard.separateAndSend("<html><body><br><br><center>Вы ввели слишком длинное сообщение, оно будет сохранено не полностью.</center><br><br></body></html>", activeChar);
				return;
			}
			ClanBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
		}
		else if(url.equals("Mail")) 
		    MailBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
		else
			ShowBoard.separateAndSend("<html><body><br><br><center>Функция: " + url + " пока не реализована</center><br><br></body></html>", activeChar);
	}
}
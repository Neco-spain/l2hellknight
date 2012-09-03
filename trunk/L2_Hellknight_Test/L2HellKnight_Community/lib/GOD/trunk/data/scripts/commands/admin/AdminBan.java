package commands.admin;

import java.util.Collection;
import java.util.StringTokenizer;

import l2rt.Config;
import l2rt.common.ThreadPoolManager;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.Announcements;
import l2rt.gameserver.network.clientpackets.Say2C;
import l2rt.gameserver.handler.AdminCommandHandler;
import l2rt.gameserver.handler.IAdminCommandHandler;
import l2rt.gameserver.loginservercon.LSConnection;
import l2rt.gameserver.loginservercon.gspackets.ChangeAccessLevel;
import l2rt.gameserver.model.L2ManufactureItem;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2World;
import l2rt.gameserver.model.TradeItem;
import l2rt.gameserver.model.L2ObjectTasks.TeleportTask;
import l2rt.gameserver.network.serverpackets.NpcHtmlMessage;
import l2rt.gameserver.tables.ReflectionTable;
import l2rt.util.AutoBan;
import l2rt.util.HWID;
import l2rt.util.Location;
import l2rt.util.Log;

public class AdminBan implements IAdminCommandHandler, ScriptFile
{
	private static enum Commands
	{
		admin_ban,
		admin_unban,
		admin_chatban,
		admin_ckarma,
		admin_cban,
		admin_chatunban,
		admin_acc_ban,
		admin_acc_unban,
		admin_trade_ban,
		admin_trade_unban,
		admin_jail,
		admin_unjail,
		admin_banhwid,
		admin_ban_hwid,
		admin_unban_hwid,
		admin_unbanhwid
	}

	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, L2Player activeChar)
	{
		Commands command = (Commands) comm;

		StringTokenizer st = new StringTokenizer(fullString);

		if(activeChar.getPlayerAccess().CanTradeBanUnban)
			switch(command)
			{
				case admin_trade_ban:
					return tradeBan(st, activeChar);
				case admin_trade_unban:
					return tradeUnban(st, activeChar);
			}

		if(activeChar.getPlayerAccess().CanBan)
			switch(command)
			{
				case admin_ban:
					ban(st, activeChar);
					break;
				case admin_acc_ban:
					if(st.countTokens() > 1)
					{
						st.nextToken();
						int time = 0;
						String reason = "command by " + activeChar.getName();
						String account = st.nextToken();
						if(account.equals("$target"))
						{
							if(activeChar.getTarget() == null || !activeChar.getTarget().isPlayer())
								return false;
							account = ((L2Player) activeChar.getTarget()).getAccountName();
						}
						if(st.hasMoreTokens())
							time = Integer.parseInt(st.nextToken());
						if(st.hasMoreTokens())
							reason = activeChar.getName() + ": " + st.nextToken();
						LSConnection.getInstance().sendPacket(new ChangeAccessLevel(account, -100, reason, time));
						activeChar.sendMessage("You banned " + account + ", reason: " + reason);
						L2Player tokick = null;
						for(L2Player p : L2ObjectsStorage.getAllPlayersForIterate())
							if(p.getAccountName().equalsIgnoreCase(account))
							{
								tokick = p;
								break;
							}
						if(tokick != null)
						{
							tokick.logout(false, false, true, true);
							activeChar.sendMessage("Player " + tokick.getName() + " kicked.");
						}
					}
					break;
				case admin_trade_ban:
					return tradeBan(st, activeChar);
				case admin_trade_unban:
					return tradeUnban(st, activeChar);
				case admin_chatban:
					try
					{
						st.nextToken();
						String player = st.nextToken();
						String srok = st.nextToken();
						int str = Integer.parseInt(srok);
						String bmsg = "admin_chatban " + player + " " + srok + " ";
						String msg = fullString.substring(bmsg.length(), fullString.length());

						if(AutoBan.ChatBan(player, Integer.parseInt(srok), msg, activeChar.getName()))
							activeChar.sendMessage("You ban chat for " + player + ".");
						else
							activeChar.sendMessage("Can't find char " + player + ".");
						if (Config.ANNOUNCE_BAN_CHAT)
							Announcements.getInstance().announceToAll("Забанен чат игроку: " + player + (str > 0 ? " на " + srok + " минут." : " навсегда!"));
					}
					catch(Exception e)
					{
						activeChar.sendMessage("Command syntax: //chatban char_name period reason");
						e.printStackTrace();
					}
					break;
				case admin_chatunban:
					try
					{
						st.nextToken();
						String player = st.nextToken();

						if(AutoBan.ChatUnBan(player, activeChar.getName()))
							activeChar.sendMessage("You unban chat for " + player + ".");
						else
							activeChar.sendMessage("Can't find char " + player + ".");
					}
					catch(Exception e)
					{
						activeChar.sendMessage("Command syntax: //chatunban char_name");
						e.printStackTrace();
					}
					break;
				case admin_jail:
					try
					{
						st.nextToken();
						String player = st.nextToken();
						String srok = st.nextToken();

						L2Player target = L2World.getPlayer(player);

						if(target != null)
						{
							target.setVar("jailedFrom", target.getX() + ";" + target.getY() + ";" + target.getZ() + ";" + target.getReflection());
							target._unjailTask = ThreadPoolManager.getInstance().scheduleGeneral(new TeleportTask(target, target.getLoc(), -3), Integer.parseInt(srok) * 60000);
							target.setVar("jailed", srok);
							target.teleToLocation(-114648, -249384, -2984);
							activeChar.sendMessage("You jailed " + player + ".");
						}
						else
							activeChar.sendMessage("Can't find char " + player + ".");
					}
					catch(Exception e)
					{
						activeChar.sendMessage("Command syntax: //jail char_name period");
						e.printStackTrace();
					}
					break;
				case admin_unjail:
					try
					{
						st.nextToken();
						String player = st.nextToken();

						L2Player target = L2World.getPlayer(player);

						if(target != null && target.getVar("jailed") != null)
						{
							String[] re = target.getVar("jailedFrom").split(";");
							target.teleToLocation(Integer.parseInt(re[0]), Integer.parseInt(re[1]), Integer.parseInt(re[2]));
							target.setReflection(re.length > 3 ? Integer.parseInt(re[3]) : 0);
							target._unjailTask.cancel(true);
							target.unsetVar("jailedFrom");
							target.unsetVar("jailed");
							activeChar.sendMessage("You unjailed " + player + ".");
						}
						else
							activeChar.sendMessage("Can't find char " + player + ".");
					}
					catch(Exception e)
					{
						activeChar.sendMessage("Command syntax: //unjail char_name");
						e.printStackTrace();
					}
					break;
				case admin_ckarma:
					try
					{
						st.nextToken();
						String player = st.nextToken();
						String srok = st.nextToken();
						String bmsg = "admin_ckarma " + player + " " + srok + " ";
						String msg = fullString.substring(bmsg.length(), fullString.length());

						L2Player plyr = L2World.getPlayer(player);
						if(plyr != null)
						{
							int newKarma = Integer.parseInt(srok) + plyr.getKarma();

							// update karma
							plyr.setKarma(newKarma);

							plyr.sendMessage("You get karma(" + srok + ") by GM " + activeChar.getName());
							AutoBan.Karma(plyr, Integer.parseInt(srok), msg, activeChar.getName());
							activeChar.sendMessage("You set karma(" + srok + ") " + plyr.getName());
						}
						else if(AutoBan.Karma(player, Integer.parseInt(srok), msg, activeChar.getName()))
							activeChar.sendMessage("You set karma(" + srok + ") " + player);
						else
							activeChar.sendMessage("Can't find char: " + player);
					}
					catch(Exception e)
					{
						activeChar.sendMessage("Command syntax: //ckarma char_name karma reason");
					}
					break;
				case admin_cban:
					activeChar.sendPacket(new NpcHtmlMessage(5).setFile("data/html/admin/cban.htm"));
					break;
				case admin_banhwid:
				case admin_ban_hwid:
					if(wordList.length < 2)
					{
						activeChar.sendMessage("USAGE: //banhwid char_name|hwid [kick:true|false] [reason]");
						return false;
					}
					try
					{
						activeChar.sendMessage(HWID.handleBanHWID(wordList));
					}
					catch(Exception e)
					{
						activeChar.sendMessage("USAGE: //banhwid char_name|hwid [kick:true|false] [reason]");
						e.printStackTrace();
					}
					break;
				case admin_unbanhwid:
				case admin_unban_hwid:
					if(!Config.PROTECT_ENABLE || !Config.PROTECT_GS_ENABLE_HWID_BANS)
					{
						activeChar.sendMessage("HWID bans feature disabled");
						return false;
					}
					if(wordList.length < 2)
					{
						activeChar.sendMessage("USAGE: //unbanhwid hwid");
						return false;
					}
					if(wordList[1].length() != 32)
					{
						activeChar.sendMessage(wordList[1] + " is not like HWID");
						return false;
					}
					HWID.UnbanHWID(wordList[1]);
					activeChar.sendMessage("HWID " + wordList[1] + " unbanned");
					break;
			}

		return true;
	}

	private boolean tradeBan(StringTokenizer st, L2Player activeChar)
	{
		if(activeChar.getTarget() == null || !activeChar.getTarget().isPlayer())
			return false;
		st.nextToken();
		L2Player targ = (L2Player) activeChar.getTarget();
		long days = -1;
		long time = -1;
		if(st.hasMoreTokens())
		{
			days = Long.parseLong(st.nextToken());
			time = days * 24 * 60 * 60 * 1000L + System.currentTimeMillis();
		}
		targ.setVar("tradeBan", String.valueOf(time));
		if(targ.hasHWID())
			HWID.setBonus(targ.getHWID(), "tradeBan", days > 0 ? (int) (time / 1000) : -1, time / 1000);

		String msg = activeChar.getName() + " заблокировал торговлю персонажу " + targ.getName() + (days == -1 ? " на бессрочный период." : " на " + days + " дней.");

		Log.add(targ.getName() + ":" + days + tradeToString(targ, targ.getPrivateStoreType()), "tradeBan", activeChar);

		if(targ.isInOfflineMode())
		{
			targ.setOfflineMode(false);
			targ.logout(false, false, true, true);
			if(targ.getNetConnection() != null)
				targ.getNetConnection().disconnectOffline();
		}
		else if(targ.isInStoreMode())
		{
			targ.setPrivateStoreType(L2Player.STORE_PRIVATE_NONE);
			targ.broadcastUserInfo(true);
			targ.getBuyList().clear();
		}

		if(Config.MAT_ANNOUNCE_FOR_ALL_WORLD)
			Announcements.getInstance().announceToAll(msg);
		else
			Announcements.shout(activeChar, msg, Say2C.CRITICAL_ANNOUNCEMENT);
		return true;
	}

	@SuppressWarnings("unchecked")
	private static String tradeToString(L2Player targ, int trade)
	{
		String ret;
		Collection list;
		switch(trade)
		{
			case L2Player.STORE_PRIVATE_BUY:
				list = targ.getBuyList();
				if(list == null || list.isEmpty())
					return "";
				ret = ":buy:";
				for(TradeItem i : (Collection<TradeItem>) list)
					ret += i.getItemId() + ";" + i.getCount() + ";" + i.getOwnersPrice() + ":";
				return ret;
			case L2Player.STORE_PRIVATE_SELL:
			case L2Player.STORE_PRIVATE_SELL_PACKAGE:
				list = targ.getSellList();
				if(list == null || list.isEmpty())
					return "";
				ret = ":sell:";
				for(TradeItem i : (Collection<TradeItem>) list)
					ret += i.getItemId() + ";" + i.getCount() + ";" + i.getOwnersPrice() + ":";
				return ret;
			case L2Player.STORE_PRIVATE_MANUFACTURE:
				list = targ.getCreateList().getList();
				if(list == null || list.isEmpty())
					return "";
				ret = ":mf:";
				for(L2ManufactureItem i : (Collection<L2ManufactureItem>) list)
					ret += i.getRecipeId() + ";" + i.getCost() + ":";
				return ret;
			default:
				return "";
		}
	}

	private boolean tradeUnban(StringTokenizer st, L2Player activeChar)
	{
		if(activeChar.getTarget() == null || !activeChar.getTarget().isPlayer())
			return false;
		L2Player targ = (L2Player) activeChar.getTarget();

		targ.unsetVar("tradeBan");

		if(targ.hasHWID())
			HWID.unsetBonus(targ.getHWID(), "tradeBan");

		if(Config.MAT_ANNOUNCE_FOR_ALL_WORLD)
			Announcements.getInstance().announceToAll(activeChar + " разблокировал торговлю персонажу " + targ + ".");
		else
			Announcements.shout(activeChar, activeChar + " разблокировал торговлю персонажу " + targ + ".", Say2C.CRITICAL_ANNOUNCEMENT);

		Log.add(activeChar + " разблокировал торговлю персонажу " + targ + ".", "tradeBan", activeChar);
		return true;
	}

	private boolean ban(StringTokenizer st, L2Player activeChar)
	{
		try
		{
			st.nextToken();

			String player = st.nextToken();

			int time = 0;
			String msg = "";

			if(st.hasMoreTokens())
				time = Integer.parseInt(st.nextToken());

			if(st.hasMoreTokens())
			{
				msg = "admin_ban " + player + " " + time + " ";
				while(st.hasMoreTokens())
					msg += st.nextToken() + " ";
				msg.trim();
			}

			L2Player plyr = L2World.getPlayer(player);
			if(plyr != null)
			{
				plyr.sendMessage(new CustomMessage("scripts.commands.admin.AdminBan.YoureBannedByGM", plyr).addString(activeChar.getName()));
				plyr.setAccessLevel(-100);
				AutoBan.Banned(plyr, time, msg, activeChar.getName());
				plyr.logout(false, false, true, true);
				activeChar.sendMessage("You banned " + plyr.getName());
			}
			else if(AutoBan.Banned(player, -100, time, msg, activeChar.getName()))
				activeChar.sendMessage("You banned " + player);
			else
				activeChar.sendMessage("Can't find char: " + player);
		}
		catch(Exception e)
		{
			activeChar.sendMessage("Command syntax: //ban char_name days reason");
		}
		return true;
	}

	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	public void onLoad()
	{
		AdminCommandHandler.getInstance().registerAdminCommandHandler(this);
		// init jail reflection
		ReflectionTable.getInstance().get(-3, true).setCoreLoc(new Location(-114648, -249384, -2984));
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}
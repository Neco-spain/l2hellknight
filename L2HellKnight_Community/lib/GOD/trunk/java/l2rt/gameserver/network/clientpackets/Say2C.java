package l2rt.gameserver.network.clientpackets;

import l2rt.Config;
import l2rt.database.DatabaseUtils;
import l2rt.database.FiltredPreparedStatement;
import l2rt.database.L2DatabaseFactory;
import l2rt.database.ThreadConnection;
import l2rt.extensions.listeners.PropertyCollection;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.handler.IVoicedCommandHandler;
import l2rt.gameserver.handler.VoicedCommandHandler;
import l2rt.gameserver.instancemanager.PartyRoomManager;
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.base.Experience;
import l2rt.gameserver.model.entity.siege.territory.TerritorySiege;
import l2rt.gameserver.network.serverpackets.ExRpItemLink;
import l2rt.gameserver.network.serverpackets.ExShowUsmVideo;
import l2rt.gameserver.network.serverpackets.Say2;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.tables.FakePlayersTable;
import l2rt.gameserver.tables.MapRegion;
import l2rt.status.GameStatusThread;
import l2rt.status.Status;
import l2rt.util.*;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Say2C extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(Say2C.class.getName());

	/** RegExp для кэширования линков предметов */
	private static Pattern PATTERN = Pattern.compile("Type=[0-9]+[^0-9]+ID=([0-9]+)[^0-9]+");

	public final static int ALL = 0;
	public final static int ALL_CHAT_RANGE = 1250; //Дальность белого чата
	public final static int SHOUT = 1; //!
	public final static int TELL = 2; //\"
	public final static int PARTY = 3; //#
	public final static int CLAN = 4; //@
	public final static int GM = 5;
	public final static int PETITION_PLAYER = 6; // used for petition
	public final static int PETITION_GM = 7; //* used for petition
	public final static int TRADE = 8; //+
	public final static int ALLIANCE = 9; //$
	public final static int ANNOUNCEMENT = 10;
	public final static int PARTY_ROOM = 14;
	public final static int COMMANDCHANNEL_ALL = 15; //`` (pink) команды лидера СС
	public final static int COMMANDCHANNEL_COMMANDER = 16; //` (yellow) чат лидеров партий в СС
	public final static int HERO_VOICE = 17; //%
	public final static int CRITICAL_ANNOUNCEMENT = 18; //dark cyan
	public final static int UNKNOWN = 19; //?
	public final static int BATTLEFIELD = 20; //^
	public final static int[] BAN_CHAN = Config.BAN_CHANNEL_LIST;

	public static String[] chatNames = { "ALL	", "SHOUT", "TELL ", "PARTY", "CLAN ", "GM	 ", "PETITION_PLAYER",
			"PETITION_GM", "TRADE", "ALLIANCE", "ANNOUNCEMENT", "", "", "", "PARTY_ROOM", "COMMANDCHANNEL_ALL",
			"COMMANDCHANNEL_COMMANDER", "HERO_VOICE", "CRITICAL_ANNOUNCEMENT", "UNKNOWN", "BATTLEFIELD" };

	protected static GArray<String> _banned = new GArray<String>();
	private String _text;
	private int _type;
	private String _target;

	/**
	 * packet type id 0x49
	 * format:		cSd (S)
	 * @param
	 */
	@Override
	public void readImpl()
	{
		_text = readS(Config.CHAT_MESSAGE_MAX_LEN);
		_type = readD();
		_target = _type == TELL ? readS(Config.CNAME_MAXLEN) : null;
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(_type < 0 || _type > chatNames.length || _text == null || _text.length() == 0)
		{
			activeChar.sendActionFailed();
			return;
		}

		_text = _text.replaceAll("\\\\n", "\n");

		if(_text.contains("\n"))
		{
			String[] lines = _text.split("\n");
			_text = "";
			for(int i = 0; i < lines.length && i < Config.CHAT_MAX_LINES; i++)
			{
				lines[i] = lines[i].trim();
				if(lines[i].length() == 0)
					continue;
				if(_text.length() > 0)
					_text += "\n  >";
				if(Config.CHAT_LINE_LENGTH > 0 && lines[i].length() > Config.CHAT_LINE_LENGTH)
					i++;
				_text += lines[i];
			}
		}

		if(_text.length() == 0)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(Config.LOG_TELNET)
		{
			String line_output;

			if(_type == TELL)
				line_output = chatNames[_type] + "[" + activeChar.getName() + " to " + _target + "] " + _text;
			else
				line_output = chatNames[_type] + "[" + activeChar.getName() + "] " + _text;
			telnet_output(line_output, _type);
		}
		if (_text.equalsIgnoreCase("q"))
		{
			activeChar.sendPacket(new ExShowUsmVideo(ExShowUsmVideo.GD1_INTRO));
			return;
		}
		if(_text.startsWith("."))
		{
			String fullcmd = _text.substring(1).trim();
			String command = fullcmd.split("\\s+")[0];
			String args = fullcmd.substring(command.length()).trim();

			if(command.length() > 0)
			{
				// then check for VoicedCommands
				IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
				if(vch != null)
				{
					vch.useVoicedCommand(command, activeChar, args);
					return;
				}
			}
			activeChar.sendMessage("Wrong command");
			return;
		}
		else if(_text.startsWith("==") || _text.startsWith("--"))
			return;

		boolean globalchat = _type != ALLIANCE && _type != CLAN && _type != PARTY;
		boolean chan_banned = false;
		for(int i = 0; i <= Config.MAT_BAN_COUNT_CHANNELS; i++)
			if(_type == BAN_CHAN[i])
				chan_banned = true;
		if((globalchat || chan_banned) && activeChar.getNoChannel() != 0)
		{
			if(activeChar.getNoChannelRemained() > 0 || activeChar.getNoChannel() < 0)
			{
				if(activeChar.getNoChannel() > 0)
				{
					int timeRemained = Math.round(activeChar.getNoChannelRemained() / 60000);
					activeChar.sendMessage(new CustomMessage("common.ChatBanned", activeChar).addNumber(timeRemained));
				}
				else
					activeChar.sendMessage(new CustomMessage("common.ChatBannedPermanently", activeChar));
				activeChar.sendActionFailed();
				return;
			}
			activeChar.updateNoChannel(0);
		}

		if(globalchat)
		{
			if(Config.MAT_REPLACE)
			{
				if(Config.containsMat(_text))
				{
					_text = Config.MAT_REPLACE_STRING;
					activeChar.sendActionFailed();
				}
			}
			else if(Config.MAT_BANCHAT && Config.containsMat(_text))
			{
				activeChar.sendMessage("You are banned in all chats. Time to unban: " + Config.UNCHATBANTIME * 60 + "sec.");
				Log.add("" + activeChar + ": " + _text, "abuse");
				activeChar.updateNoChannel(Config.UNCHATBANTIME * 60000);
				activeChar.sendActionFailed();
				return;
			}
		}

		// Кэширование линков предметов
		String links[] = _text.split("\u0008");
		int id, i = 1;
		while(i < links.length)
		{
			Matcher matcher = PATTERN.matcher(links[i]);
			if(matcher.find())
			{
				try
				{
					id = Integer.parseInt(matcher.group(1));
				}
				catch(Exception e)
				{
					id = 0;
				}
				if(id > 0 && L2ObjectsStorage.getItemByObjId(id) != null)
					ExRpItemLink.addItem(id);
				else if(links.length > 1)
					_text = _text.replace("\u0008" + links[i], ""); //TODO если все линки кривые, то в конце останется символ "\u0008"
				else
					_text = _text.replace("\u0008" + links[i] + "\u0008", "");
			}
			i += 2;
		}

		if((Config.TRADE_CHATS_REPLACE_FROM_ALL && _type == ALL) || (Config.TRADE_CHATS_REPLACE_FROM_SHOUT && _type == SHOUT))
			for(Pattern TRADE_WORD : Config.TRADE_WORDS)
				if(TRADE_WORD.matcher(_text).matches())
				{
					_type = TRADE;
					break;
				}

		LogChat.add(_text, chatNames[_type], activeChar.getName(), _type == TELL ? _target : null);

		String translit = activeChar.getVar("translit");
		if(translit != null)
			_text = Strings.fromTranslit(_text, translit.equals("tl") ? 1 : 2);

		Say2 cs = new Say2(activeChar.getObjectId(), _type, activeChar.getName(), _text);
		int mapregion = MapRegion.getInstance().getMapRegion(activeChar.getX(), activeChar.getY());
		long curTime = System.currentTimeMillis();

		switch(_type)
		{
			case TELL:
				L2Player receiver = L2World.getPlayer(_target);
				if(receiver == null && Config.ALLOW_FAKE_PLAYERS && FakePlayersTable.getActiveFakePlayers().contains(_target))
				{
					cs = new Say2(activeChar.getObjectId(), _type, "->" + _target, _text);
					activeChar.sendPacket(cs);
					return;
				}
				else if(receiver != null && receiver.isInOfflineMode())
				{
					activeChar.sendMessage("The person is in offline trade mode");
					activeChar.sendActionFailed();
				}
				else if(receiver != null && !receiver.isInBlockList(activeChar) && !receiver.isBlockAll())
				{
					Long lastShoutTime = (Long) activeChar.getProperty(PropertyCollection.TellChatLaunched);
					if(lastShoutTime != null && lastShoutTime + (activeChar.getLevel() >= 20 ? 1000L : 10000L) > curTime)
					{
						activeChar.sendMessage("Tell chat is allowed once per " + (activeChar.getLevel() >= 20 ? "1 second." : "10 seconds."));
						return;
					}
					activeChar.addProperty(PropertyCollection.TellChatLaunched, curTime);

					if(!receiver.getMessageRefusal())
					{
						receiver.sendPacket(cs);
						cs = new Say2(activeChar.getObjectId(), _type, "->" + receiver.getName(), _text);
						activeChar.sendPacket(cs);
					}
					else
						activeChar.sendPacket(Msg.THE_PERSON_IS_IN_A_MESSAGE_REFUSAL_MODE);
				}
				else if(receiver == null)
					activeChar.sendPacket(new SystemMessage(SystemMessage.S1_IS_NOT_CURRENTLY_LOGGED_IN).addString(_target), Msg.ActionFail);
				else
					activeChar.sendPacket(Msg.YOU_HAVE_BEEN_BLOCKED_FROM_THE_CONTACT_YOU_SELECTED, Msg.ActionFail);
				break;
			case SHOUT:
				if(activeChar.isCursedWeaponEquipped())
				{
					activeChar.sendPacket(Msg.SHOUT_AND_TRADE_CHATING_CANNOT_BE_USED_SHILE_POSSESSING_A_CURSED_WEAPON);
					return;
				}
				if(activeChar.inObserverMode())
				{
					activeChar.sendPacket(Msg.YOU_CANNOT_CHAT_LOCALLY_WHILE_OBSERVING);
					return;
				}

				Long lastShoutTime = (Long) activeChar.getProperty(PropertyCollection.ShoutChatLaunched);
				if(lastShoutTime != null && lastShoutTime + 5000L > curTime)
				{
					activeChar.sendMessage("Shout chat is allowed once per 5 seconds.");
					return;
				}
				activeChar.addProperty(PropertyCollection.ShoutChatLaunched, curTime);

				if(activeChar.getLevel() >= Config.GLOBAL_CHAT || activeChar.isGM() && Config.GLOBAL_CHAT < Experience.LEVEL.length)
				{
					for(L2Player player : L2ObjectsStorage.getAllPlayersForIterate())
						if(!player.isInBlockList(activeChar) && !player.isBlockAll())
							player.sendPacket(cs);
				}
				else
				{
					if(Config.SHOUT_CHAT_MODE == 1)
					{
						for(L2Player player : L2World.getAroundPlayers(activeChar, 10000, 1500))
							if(!player.isInBlockList(activeChar) && !player.isBlockAll() && player != activeChar)
								player.sendPacket(cs);
					}
					else
						for(L2Player player : L2ObjectsStorage.getAllPlayersForIterate())
							if(MapRegion.getInstance().getMapRegion(player.getX(), player.getY()) == mapregion && !player.isInBlockList(activeChar) && !player.isBlockAll() && player != activeChar)
								player.sendPacket(cs);
					activeChar.sendPacket(cs);
				}
				break;
			case TRADE:
				if(activeChar.isCursedWeaponEquipped())
				{
					activeChar.sendPacket(Msg.SHOUT_AND_TRADE_CHATING_CANNOT_BE_USED_SHILE_POSSESSING_A_CURSED_WEAPON);
					return;
				}
				if(activeChar.inObserverMode())
				{
					activeChar.sendPacket(Msg.YOU_CANNOT_CHAT_LOCALLY_WHILE_OBSERVING);
					return;
				}

				Long lastTradeTime = (Long) activeChar.getProperty(PropertyCollection.TradeChatLaunched);
				if(lastTradeTime != null && lastTradeTime + 5000L > curTime)
				{
					activeChar.sendMessage("Trade chat is allowed once per 5 seconds.");
					return;
				}
				activeChar.addProperty(PropertyCollection.TradeChatLaunched, curTime);

				if(activeChar.getLevel() >= Config.GLOBAL_TRADE_CHAT || activeChar.isGM() && Config.GLOBAL_TRADE_CHAT < Experience.LEVEL.length)
				{
					for(L2Player player : L2ObjectsStorage.getAllPlayersForIterate())
						if(!player.isInBlockList(activeChar) && !player.isBlockAll())
							player.sendPacket(cs);
				}
				else
				{
					if(Config.TRADE_CHAT_MODE == 1)
					{
						for(L2Player player : L2World.getAroundPlayers(activeChar, 10000, 1500))
							if(!player.isInBlockList(activeChar) && !player.isBlockAll() && player != activeChar)
								player.sendPacket(cs);
					}
					else
						for(L2Player player : L2ObjectsStorage.getAllPlayersForIterate())
							if(MapRegion.getInstance().getMapRegion(player.getX(), player.getY()) == mapregion && !player.isInBlockList(activeChar) && !player.isBlockAll() && player != activeChar)
								player.sendPacket(cs);
					activeChar.sendPacket(cs);
				}
				break;
			case ALL:
				if(activeChar.isCursedWeaponEquipped())
					cs = new Say2(activeChar.getObjectId(), _type, activeChar.getTransformationName(), _text);

				if(activeChar.inObserverMode() && activeChar.getObservNeighbor() != null)
				{
					GArray<L2Player> result = new GArray<L2Player>(50);
					for(L2WorldRegion neighbor : activeChar.getObservNeighbor().getNeighbors())
						neighbor.getPlayersList(result, activeChar.getObjectId(), activeChar.getReflection(), activeChar.getX(), activeChar.getY(), activeChar.getZ(), ALL_CHAT_RANGE * ALL_CHAT_RANGE, 400);

					for(L2Player player : result)
						if(!player.isInBlockList(activeChar) && !player.isBlockAll() && player != activeChar)
							player.sendPacket(cs);
				}
				else
					for(L2Player player : L2World.getAroundPlayers(activeChar, ALL_CHAT_RANGE, 400))
						if(!player.isInBlockList(activeChar) && !player.isBlockAll() && player != activeChar)
							player.sendPacket(cs);

				activeChar.sendPacket(cs);
				break;
			case CLAN:
				if(activeChar.getClan() != null)
					activeChar.getClan().broadcastToOnlineMembers(cs);
				else
					activeChar.sendActionFailed();
				break;
			case ALLIANCE:
				if(activeChar.getClan() != null && activeChar.getClan().getAlliance() != null)
					activeChar.getClan().getAlliance().broadcastToOnlineMembers(cs);
				else
					activeChar.sendActionFailed();
				break;
			case PARTY:
				if(activeChar.isInParty())
					activeChar.getParty().broadcastToPartyMembers(cs);
				else
					activeChar.sendActionFailed();
				break;
			case PARTY_ROOM:
				if(activeChar.getPartyRoom() <= 0)
				{
					activeChar.sendActionFailed();
					return;
				}
				PartyRoom room = PartyRoomManager.getInstance().getRooms().get(activeChar.getPartyRoom());
				if(room == null)
				{
					activeChar.sendActionFailed();
					return;
				}
				room.broadcastPacket(cs);
				break;
			case COMMANDCHANNEL_ALL:
				if(!activeChar.isInParty() || !activeChar.getParty().isInCommandChannel())
				{
					activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_AUTHORITY_TO_USE_THE_COMMAND_CHANNEL);
					return;
				}
				if(activeChar.getParty().getCommandChannel().getChannelLeader() == activeChar)
					activeChar.getParty().getCommandChannel().broadcastToChannelMembers(cs);
				else
					activeChar.sendPacket(Msg.ONLY_CHANNEL_OPENER_CAN_GIVE_ALL_COMMAND);
				break;
			case COMMANDCHANNEL_COMMANDER:
				if(!activeChar.isInParty() || !activeChar.getParty().isInCommandChannel())
				{
					activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_AUTHORITY_TO_USE_THE_COMMAND_CHANNEL);
					return;
				}
				if(activeChar.getParty().isLeader(activeChar))
					activeChar.getParty().getCommandChannel().broadcastToChannelPartyLeaders(cs);
				else
					activeChar.sendPacket(Msg.ONLY_A_PARTY_LEADER_CAN_ACCESS_THE_COMMAND_CHANNEL);
				break;
			case HERO_VOICE:
				if(activeChar.isHero() || activeChar.getPlayerAccess().CanAnnounce)
				{
					// Ограничение только для героев, гм-мы пускай говорят.
					if(!activeChar.getPlayerAccess().CanAnnounce)
					{
						Long lastHeroTime = (Long) activeChar.getProperty(PropertyCollection.HeroChatLaunched);
						if(lastHeroTime != null && lastHeroTime + 10000L > curTime)
						{
							activeChar.sendMessage("Hero chat is allowed once per 10 seconds.");
							return;
						}
						activeChar.addProperty(PropertyCollection.HeroChatLaunched, curTime);
					}

					for(L2Player player : L2ObjectsStorage.getAllPlayersForIterate())
						if(!player.isInBlockList(activeChar) && !player.isBlockAll())
							player.sendPacket(cs);
				}
				break;
			case PETITION_PLAYER:
			case PETITION_GM:
				//for(L2Player gm : GmListTable.getAllGMs())
				//	if(!gm.getMessageRefusal())
				//		gm.sendPacket(cs);
				break;
			case BATTLEFIELD:
				if(activeChar.getTerritorySiege() > -1 && TerritorySiege.isTerritoryChatAccessible())
				{
					for(L2Player player : L2ObjectsStorage.getAllPlayersForIterate())
						if(!player.isInBlockList(activeChar) && !player.isBlockAll() && activeChar.getTerritorySiege() == player.getTerritorySiege())
							player.sendPacket(cs);
				}
				break;
			default:
				_log.warning("Character " + activeChar.getName() + " used unknown chat type: " + _type + ". Cheater?");
		}
	}

	private void telnet_output(String _text, int type)
	{
		GameStatusThread tinstance = Status.telnetlist;

		while(tinstance != null)
		{
			if(type == TELL && tinstance.LogTell)
				tinstance.write(_text);
			else if(tinstance.LogChat)
				tinstance.write(_text);
			tinstance = tinstance.next;
		}
	}

	public class UnbanTask implements Runnable
	{
		private String _name;

		public UnbanTask(String Name)
		{
			_name = Name;
		}

		public void run()
		{
			L2Player plyr = L2World.getPlayer(_name);
			if(plyr != null)
			{
				plyr.setAccessLevel(0);
				plyr.sendMessage("Nochannel deactivated");
				Log.add("" + plyr + ": unbanchat online", "abuse");
			}
			else
			{
				setCharacterAccessLevel(_name, 0);
				Log.add("Player " + _name + ": unbanchat offline", "abuse");
			}

			_banned.remove(_name);
		}
	}

	public void setCharacterAccessLevel(String user, int banLevel)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			String stmt = "UPDATE characters SET characters.accesslevel = ? WHERE characters.char_name=?";
			statement = con.prepareStatement(stmt);
			statement.setInt(1, banLevel);
			statement.setString(2, user);
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			_log.warning("Could not set accessLevl:" + e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}
}
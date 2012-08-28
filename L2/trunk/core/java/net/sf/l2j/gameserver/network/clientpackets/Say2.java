package net.sf.l2j.gameserver.network.clientpackets;

import java.nio.BufferUnderflowException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.handler.VoicedCommandHandler;
import net.sf.l2j.gameserver.instancemanager.PetitionManager;
import net.sf.l2j.gameserver.model.BlockList;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.SayFilter;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.FloodProtector;
import net.sf.l2j.util.Rnd;

public final class Say2 extends L2GameClientPacket
{
	private static final String _C__38_SAY2 = "[C] 38 Say2";
	private static Logger _logChat = Logger.getLogger("chat");

	public final static int ALL = 0;
	public final static int SHOUT = 1; //!
	public final static int TELL = 2;
	public final static int PARTY = 3; //#
	public final static int CLAN = 4;  //@
	public final static int GM = 5;
	public final static int PETITION_PLAYER = 6; // used for petition
	public final static int PETITION_GM = 7; //* used for petition
	public final static int TRADE = 8; //+
	public final static int ALLIANCE = 9; //$
	public final static int ANNOUNCEMENT = 10;
	public final static int PARTYROOM_ALL = 16; //(Red)
	public final static int PARTYROOM_COMMANDER = 15; //(Yellow)
	public final static int HERO_VOICE = 17;

	private final static String[] CHAT_NAMES = {
	                                          "ALL  ",
	                                          "SHOUT",
	                                          "TELL ",
	                                          "PARTY",
	                                          "CLAN ",
	                                          "GM   ",
	                                          "PETITION_PLAYER",
	                                          "PETITION_GM",
	                                          "TRADE",
	                                          "ALLIANCE",
	                                          "ANNOUNCEMENT", //10
	                                          "WILLCRASHCLIENT:)",
	                                          "FAKEALL?",
	                                          "FAKEALL?",
	                                          "FAKEALL?",
	                                          "PARTYROOM_ALL",
	                                          "PARTYROOM_COMMANDER",
	                                          "HERO_VOICE"
	};

	private String _text;
	private int _type;
	private String _target;
	@Override
	protected void readImpl()
	{
		_text = readS();
		try
		{
			_type = readD();
		}
		catch (BufferUnderflowException e)
		{
			_type = CHAT_NAMES.length;
		}
		_target = (_type == TELL) ? readS() : null;
	}

	@Override
	protected void runImpl()
	{
		_text = _text.replace("\\n", "");			
		if(_type < 0 || _type >= CHAT_NAMES.length)
		{
			return;
		}

		L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null)
		{
			return;
		}

		if (activeChar.isChatBanned())
		{
			if (_text.startsWith("."))
			{
				StringTokenizer st = new StringTokenizer(_text);
				IVoicedCommandHandler vch;
				String command = "";
				String target = "";

				if (st.countTokens() > 1)
				{
					command = st.nextToken().substring(1);
					target = _text.substring(command.length() + 2);
					vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
				}
				else
				{
					command = _text.substring(1);
					vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
				}
				if (vch != null) {
					vch.useVoicedCommand(command, activeChar, target);
				return;
				}
				activeChar.sendMessage("[Неверная команда]");
				return;
			}
			else
			{
			activeChar.sendMessage("Your chat banned, you can not talk.");
			}
			return;
		}

		if (Config.USE_SAY_FILTER) 
		{
			checkText(activeChar);
		}
		if(activeChar.isCursedWeaponEquiped() && (_type == 8 || _type == 1))
        {
            activeChar.sendMessage("General and trade chat off for the owners cursed weapons.");
            return;
        }

		if (_text.length() > Config.MAX_MESSAGE_LENGHT)
		{
			activeChar.sendMessage("Your message has too many characters.");
			return;
		}

        if (activeChar.isInJail() && Config.JAIL_DISABLE_CHAT)
        {
            if (_type == TELL || _type == SHOUT || _type == TRADE || _type == HERO_VOICE)
            {
                activeChar.sendMessage("You do not hear. You are in jail.");
                return;
            }
        }

		if (_type == PETITION_PLAYER && activeChar.isGM())
			_type = PETITION_GM;

		if (Config.LOG_CHAT)
		{
			LogRecord record = new LogRecord(Level.INFO, _text);
			record.setLoggerName("chat");

			if (_type == TELL)
				record.setParameters(new Object[]{CHAT_NAMES[_type], "[" + activeChar.getName() + " to "+_target+"]"});
			else
				record.setParameters(new Object[]{CHAT_NAMES[_type], "[" + activeChar.getName() + "]"});

			_logChat.log(record);
		}

		CreatureSay cs = new CreatureSay(activeChar.getObjectId(), _type, activeChar.getName(), _text);

		switch (_type)
		{
			case TELL:
			_text = _text.replaceAll("\n", "");
			L2PcInstance receiver = L2World.getInstance().getPlayer(_target);
			if (activeChar.getLevel() < Config.TELL_CHAT_LVL)
				{
					activeChar.sendMessage("Отправлять сообщения в чат могут только достигшие "+Config.TELL_CHAT_LVL);
						return;
				}
				if (receiver != null &&
						!BlockList.isBlocked(receiver, activeChar))
				{
					if (Config.JAIL_DISABLE_CHAT && receiver.isInJail())
			        {
			                activeChar.sendMessage("Player in jail.");
			                return;
			        }
					

					if (!receiver.getMessageRefusal())
					{
						receiver.sendPacket(cs);
						activeChar.sendPacket(new CreatureSay(activeChar.getObjectId(),  _type, "->" + receiver.getName(), _text));
					}
					else
					{
						activeChar.sendPacket(new SystemMessage(SystemMessageId.THE_PERSON_IS_IN_MESSAGE_REFUSAL_MODE));
					}
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_NOT_ONLINE);
					sm.addString(_target);
					activeChar.sendPacket(sm);
					sm = null;
				}
				break;
			case SHOUT:
			_text = _text.replaceAll("\n", "");
			if (activeChar.getLevel() < Config.SHOUT_CHAT_LVL)
			{
				activeChar.sendMessage("Отправлять сообщения в shout чат могут только достигшие "+Config.SHOUT_CHAT_LVL);
					return;
			}
			if (Config.SHOUT_FLOOD_TIME > 0)
				{
					if (!activeChar.isGM() && !FloodProtector.getInstance().tryPerformAction(activeChar.getObjectId(), FloodProtector.PROTECTED_SHOUT))
					{
						activeChar.sendMessage("You are too fast write in shout chat.");
						return;
					}
				}				
                if (Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("on") ||
                        (Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("gm") && activeChar.isGM()))
                {
                    int region = MapRegionTable.getInstance().getMapRegion(activeChar.getX(), activeChar.getY());
                    for (L2PcInstance player : L2World.getInstance().getAllPlayers())
                    {
                        if (region == MapRegionTable.getInstance().getMapRegion(player.getX(),player.getY()))
                            player.sendPacket(cs);
                    }
                }
                else if (Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("global"))
                {
                    for (L2PcInstance player : L2World.getInstance().getAllPlayers())
                    {
                        player.sendPacket(cs);
                    }
                }
                break;
			case TRADE:
			_text = _text.replaceAll("\n", "");
			if (activeChar.getLevel() < Config.TRADE_CHAT_LVL)
			{
				activeChar.sendMessage("Отправлять сообщения в trade чат могут только достигшие "+Config.TRADE_CHAT_LVL);
					return;
			}
			if (Config.TRADE_FLOOD_TIME > 0)
				{
					if (!activeChar.isGM() && !FloodProtector.getInstance().tryPerformAction(activeChar.getObjectId(), FloodProtector.PROTECTED_TRADE))
					{
						activeChar.sendMessage("You are too fast write in trade chat.");
						return;
					}
				}
				if (Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("on") ||
						(Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("gm") && activeChar.isGM()))
				{
					for (L2PcInstance player : L2World.getInstance().getAllPlayers())
					{
							player.sendPacket(cs);
					}
				} else if (Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("limited"))
                {
                    int region = MapRegionTable.getInstance().getMapRegion(activeChar.getX(), activeChar.getY());
                    for (L2PcInstance player : L2World.getInstance().getAllPlayers())
                    {
                        if (region == MapRegionTable.getInstance().getMapRegion(player.getX(),player.getY()))
                            player.sendPacket(cs);
                    }
                }
                break;
			case ALL:
			_text = _text.replaceAll("\n", "");
			if (_text.startsWith("."))
				{
					StringTokenizer st = new StringTokenizer(_text);
					IVoicedCommandHandler vch;
					String command = "";
					String target = "";

					if (st.countTokens() > 1)
					{
						command = st.nextToken().substring(1);
						target = _text.substring(command.length() + 2);
						vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
					}
					else
					{
						command = _text.substring(1);
						vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
					}
					if (vch != null) {
						vch.useVoicedCommand(command, activeChar, target);
					return;
					}
					//activeChar.sendMessage("[Неверная команда]");
					for (L2PcInstance player : activeChar.getKnownList().getKnownPlayers().values())
					{
						if (player != null && activeChar.isInsideRadius(player, 1250, false, true))
							player.sendPacket(cs);
					}
					activeChar.sendPacket(cs);
					return;
				}
				else
				{
					for (L2PcInstance player : activeChar.getKnownList().getKnownPlayers().values())
					{
						if (player != null && activeChar.isInsideRadius(player, 1250, false, true))
							player.sendPacket(cs);
					}
					activeChar.sendPacket(cs);
					
				}
                break;
			case CLAN:
			_text = _text.replaceAll("\n", "");
			if (activeChar.getClan() != null)
					activeChar.getClan().broadcastToOnlineMembers(cs);
				break;
			case ALLIANCE:
				if (activeChar.getClan() != null)
					activeChar.getClan().broadcastToOnlineAllyMembers(cs);
				break;
			case PARTY:
				if (activeChar.isInParty())
					activeChar.getParty().broadcastToPartyMembers(cs);
				break;
			case PETITION_PLAYER:
			case PETITION_GM:
				if (!PetitionManager.getInstance().isPlayerInConsultation(activeChar))
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_ARE_NOT_IN_PETITION_CHAT));
					break;
				}

				PetitionManager.getInstance().sendActivePetitionMessage(activeChar, _text);
				break;
			case PARTYROOM_ALL:
				if (activeChar.isInParty())
				{
					if (activeChar.getParty().isInCommandChannel() && activeChar.getParty().isLeader(activeChar))
					{
						activeChar.getParty().getCommandChannel().broadcastToChannelMembers(cs);
					}
				}
				break;
			case PARTYROOM_COMMANDER:
				if (activeChar.isInParty())
				{
					if (activeChar.getParty().isInCommandChannel() &&
							activeChar.getParty().getCommandChannel().getChannelLeader().equals(activeChar))
					{
						activeChar.getParty().getCommandChannel().broadcastToChannelMembers(cs);
					}
				}
				break;
			case HERO_VOICE:
			_text = _text.replaceAll("\n", "");
			if (activeChar.isHero() || activeChar.isGM())
				{
					if (!activeChar.isGM() && !FloodProtector.getInstance().tryPerformAction(activeChar.getObjectId(), FloodProtector.PROTECTED_HEROVOICE))
					{
						return;
					}
					for (L2PcInstance player : L2World.getInstance().getAllPlayers())
						if (!BlockList.isBlocked(player, activeChar))
							player.sendPacket(cs);
				}
				break;
		}
	}
	
	private void checkText(L2PcInstance activeChar) 
	{
		if ((((this._type == 0) || (this._type == 1) || (this._type == 8) || (this._type == 17))) && (Config.USE_SAY_FILTER))
		{
			int _inttext = 0;
			_inttext = SayFilter.getInstance().getMatchesCnt(this._text);
			if (_inttext > 0)
			{
				String filteredText = _text;

				for (String pattern : SayFilter._badwords)
					filteredText = filteredText.replaceAll("(?i)" + pattern, Config.SAY_FILTER_REPLACEMENT_STRING);
				if ((Config.BAN_FOR_BAD_WORDS) && (Config.TIME_AUTO_CHAT_BAN > 0))
				{
					int banLength = _inttext * Config.TIME_AUTO_CHAT_BAN;
					String banReason = " " + Config.CHAT_BAN_REASON + " ";
					activeChar.setChatBanned(true, banLength);
					activeChar.sendMessage(banReason);
				}

					if ((Config.KARMA_FOR_BAD_WORDS) && (Config.KARMA_FOR_BAD_WORD_MIN > 0) && (Config.KARMA_FOR_BAD_WORD_MAX > 0))
					{
						int karm = _inttext * Rnd.get(Config.KARMA_FOR_BAD_WORD_MIN, Config.KARMA_FOR_BAD_WORD_MAX);
						activeChar.setKarma(activeChar.getKarma() + karm);
						activeChar.sendMessage("Вам добавлено " + karm + " кармы за плохое слово");
					}
					this._text = filteredText;
			}
				
		}
	}

	@Override
	public String getType()
	{
		return _C__38_SAY2;
	}
}

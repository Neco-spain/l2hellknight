package net.sf.l2j.gameserver.network.clientpackets;

import java.nio.BufferUnderflowException;
import java.util.Map;
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
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2CommandChannel;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.PcKnownList;
import net.sf.l2j.gameserver.model.entity.SayFilter;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.FloodProtector;
import net.sf.l2j.util.Rnd;

public final class Say2 extends L2GameClientPacket
{
  private static final String _C__38_SAY2 = "[C] 38 Say2";
  private static Logger _logChat = Logger.getLogger("chat");
  public static final int ALL = 0;
  public static final int SHOUT = 1;
  public static final int TELL = 2;
  public static final int PARTY = 3;
  public static final int CLAN = 4;
  public static final int GM = 5;
  public static final int PETITION_PLAYER = 6;
  public static final int PETITION_GM = 7;
  public static final int TRADE = 8;
  public static final int ALLIANCE = 9;
  public static final int ANNOUNCEMENT = 10;
  public static final int PARTYROOM_ALL = 16;
  public static final int PARTYROOM_COMMANDER = 15;
  public static final int HERO_VOICE = 17;
  private static final String[] CHAT_NAMES = { "ALL  ", "SHOUT", "TELL ", "PARTY", "CLAN ", "GM   ", "PETITION_PLAYER", "PETITION_GM", "TRADE", "ALLIANCE", "ANNOUNCEMENT", "WILLCRASHCLIENT:)", "FAKEALL?", "FAKEALL?", "FAKEALL?", "PARTYROOM_ALL", "PARTYROOM_COMMANDER", "HERO_VOICE" };
  private String _text;
  private int _type;
  private String _target;

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
    _target = (_type == 2 ? readS() : null);
  }

  protected void runImpl()
  {
    _text = _text.replace("\\n", "");
    if ((_type < 0) || (_type >= CHAT_NAMES.length))
    {
      return;
    }

    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();

    if (activeChar == null)
    {
      return;
    }

    if (activeChar.isChatBanned())
    {
      if (_text.startsWith("."))
      {
        StringTokenizer st = new StringTokenizer(_text);

        String command = "";
        String target = "";
        IVoicedCommandHandler vch;
        IVoicedCommandHandler vch;
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
        if (vch != null)
        {
          vch.useVoicedCommand(command, activeChar, target);
          return;
        }
        activeChar.sendMessage("[\u041D\u0435\u0432\u0435\u0440\u043D\u0430\u044F \u043A\u043E\u043C\u0430\u043D\u0434\u0430]");
        return;
      }

      activeChar.sendMessage("Your chat banned, you can not talk.");

      return;
    }

    if (Config.USE_SAY_FILTER)
    {
      checkText(activeChar);
    }
    if ((activeChar.isCursedWeaponEquiped()) && ((_type == 8) || (_type == 1)))
    {
      activeChar.sendMessage("General and trade chat off for the owners cursed weapons.");
      return;
    }

    if (_text.length() > Config.MAX_MESSAGE_LENGHT)
    {
      activeChar.sendMessage("Your message has too many characters.");
      return;
    }

    if ((activeChar.isInJail()) && (Config.JAIL_DISABLE_CHAT))
    {
      if ((_type == 2) || (_type == 1) || (_type == 8) || (_type == 17))
      {
        activeChar.sendMessage("You do not hear. You are in jail.");
        return;
      }
    }

    if ((_type == 6) && (activeChar.isGM())) {
      _type = 7;
    }
    if (Config.LOG_CHAT)
    {
      LogRecord record = new LogRecord(Level.INFO, _text);
      record.setLoggerName("chat");

      if (_type == 2)
        record.setParameters(new Object[] { CHAT_NAMES[_type], "[" + activeChar.getName() + " to " + _target + "]" });
      else {
        record.setParameters(new Object[] { CHAT_NAMES[_type], "[" + activeChar.getName() + "]" });
      }
      _logChat.log(record);
    }

    CreatureSay cs = new CreatureSay(activeChar.getObjectId(), _type, activeChar.getName(), _text);
    int region;
    switch (_type)
    {
    case 2:
      _text = _text.replaceAll("\n", "");
      L2PcInstance receiver = L2World.getInstance().getPlayer(_target);
      if (activeChar.getLevel() < Config.TELL_CHAT_LVL)
      {
        activeChar.sendMessage("\u041E\u0442\u043F\u0440\u0430\u0432\u043B\u044F\u0442\u044C \u0441\u043E\u043E\u0431\u0449\u0435\u043D\u0438\u044F \u0432 \u0447\u0430\u0442 \u043C\u043E\u0433\u0443\u0442 \u0442\u043E\u043B\u044C\u043A\u043E \u0434\u043E\u0441\u0442\u0438\u0433\u0448\u0438\u0435 " + Config.TELL_CHAT_LVL);
        return;
      }
      if ((receiver != null) && (!BlockList.isBlocked(receiver, activeChar)))
      {
        if ((Config.JAIL_DISABLE_CHAT) && (receiver.isInJail()))
        {
          activeChar.sendMessage("Player in jail.");
          return;
        }

        if (!receiver.getMessageRefusal())
        {
          receiver.sendPacket(cs);
          activeChar.sendPacket(new CreatureSay(activeChar.getObjectId(), _type, "->" + receiver.getName(), _text));
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
    case 1:
      _text = _text.replaceAll("\n", "");
      if (activeChar.getLevel() < Config.SHOUT_CHAT_LVL)
      {
        activeChar.sendMessage("\u041E\u0442\u043F\u0440\u0430\u0432\u043B\u044F\u0442\u044C \u0441\u043E\u043E\u0431\u0449\u0435\u043D\u0438\u044F \u0432 shout \u0447\u0430\u0442 \u043C\u043E\u0433\u0443\u0442 \u0442\u043E\u043B\u044C\u043A\u043E \u0434\u043E\u0441\u0442\u0438\u0433\u0448\u0438\u0435 " + Config.SHOUT_CHAT_LVL);
        return;
      }
      if (Config.SHOUT_FLOOD_TIME > 0)
      {
        if ((!activeChar.isGM()) && (!FloodProtector.getInstance().tryPerformAction(activeChar.getObjectId(), 13)))
        {
          activeChar.sendMessage("You are too fast write in shout chat.");
          return;
        }
      }
      int region;
      if ((Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("on")) || ((Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("gm")) && (activeChar.isGM())))
      {
        region = MapRegionTable.getInstance().getMapRegion(activeChar.getX(), activeChar.getY());
        for (L2PcInstance player : L2World.getInstance().getAllPlayers())
        {
          if (region == MapRegionTable.getInstance().getMapRegion(player.getX(), player.getY()))
            player.sendPacket(cs);
        }
      } else {
        if (!Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("global"))
          break;
        for (L2PcInstance player : L2World.getInstance().getAllPlayers())
        {
          player.sendPacket(cs); } 
      }break;
    case 8:
      _text = _text.replaceAll("\n", "");
      if (activeChar.getLevel() < Config.TRADE_CHAT_LVL)
      {
        activeChar.sendMessage("\u041E\u0442\u043F\u0440\u0430\u0432\u043B\u044F\u0442\u044C \u0441\u043E\u043E\u0431\u0449\u0435\u043D\u0438\u044F \u0432 trade \u0447\u0430\u0442 \u043C\u043E\u0433\u0443\u0442 \u0442\u043E\u043B\u044C\u043A\u043E \u0434\u043E\u0441\u0442\u0438\u0433\u0448\u0438\u0435 " + Config.TRADE_CHAT_LVL);
        return;
      }
      if (Config.TRADE_FLOOD_TIME > 0)
      {
        if ((!activeChar.isGM()) && (!FloodProtector.getInstance().tryPerformAction(activeChar.getObjectId(), 10)))
        {
          activeChar.sendMessage("You are too fast write in trade chat.");
          return;
        }
      }
      if ((Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("on")) || ((Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("gm")) && (activeChar.isGM())))
      {
        for (L2PcInstance player : L2World.getInstance().getAllPlayers())
        {
          player.sendPacket(cs);
        }
      } else {
        if (!Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("limited"))
          break;
        region = MapRegionTable.getInstance().getMapRegion(activeChar.getX(), activeChar.getY());
        for (L2PcInstance player : L2World.getInstance().getAllPlayers())
        {
          if (region == MapRegionTable.getInstance().getMapRegion(player.getX(), player.getY()))
            player.sendPacket(cs); 
        }
      }
      break;
    case 0:
      _text = _text.replaceAll("\n", "");
      if (_text.startsWith("."))
      {
        StringTokenizer st = new StringTokenizer(_text);

        String command = "";
        String target = "";
        IVoicedCommandHandler vch;
        IVoicedCommandHandler vch;
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

        for (L2PcInstance player : activeChar.getKnownList().getKnownPlayers().values())
        {
          if ((player != null) && (activeChar.isInsideRadius(player, 1250, false, true)))
            player.sendPacket(cs);
        }
        activeChar.sendPacket(cs);
        return;
      }

      for (L2PcInstance player : activeChar.getKnownList().getKnownPlayers().values())
      {
        if ((player != null) && (activeChar.isInsideRadius(player, 1250, false, true)))
          player.sendPacket(cs);
      }
      activeChar.sendPacket(cs);

      break;
    case 4:
      _text = _text.replaceAll("\n", "");
      if (activeChar.getClan() == null) break;
      activeChar.getClan().broadcastToOnlineMembers(cs); break;
    case 9:
      if (activeChar.getClan() == null) break;
      activeChar.getClan().broadcastToOnlineAllyMembers(cs); break;
    case 3:
      if (!activeChar.isInParty()) break;
      activeChar.getParty().broadcastToPartyMembers(cs); break;
    case 6:
    case 7:
      if (!PetitionManager.getInstance().isPlayerInConsultation(activeChar))
      {
        activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_ARE_NOT_IN_PETITION_CHAT));
      }
      else
      {
        PetitionManager.getInstance().sendActivePetitionMessage(activeChar, _text);
      }break;
    case 16:
      if (!activeChar.isInParty())
        break;
      if ((!activeChar.getParty().isInCommandChannel()) || (!activeChar.getParty().isLeader(activeChar)))
        break;
      activeChar.getParty().getCommandChannel().broadcastToChannelMembers(cs); break;
    case 15:
      if (!activeChar.isInParty())
        break;
      if ((!activeChar.getParty().isInCommandChannel()) || (!activeChar.getParty().getCommandChannel().getChannelLeader().equals(activeChar))) {
        break;
      }
      activeChar.getParty().getCommandChannel().broadcastToChannelMembers(cs); break;
    case 17:
      _text = _text.replaceAll("\n", "");
      if ((!activeChar.isHero()) && (!activeChar.isGM()))
        break;
      if ((!activeChar.isGM()) && (!FloodProtector.getInstance().tryPerformAction(activeChar.getObjectId(), 4)))
      {
        return;
      }
      for (L2PcInstance player : L2World.getInstance().getAllPlayers())
        if (!BlockList.isBlocked(player, activeChar))
          player.sendPacket(cs);  case 5:
    case 10:
    case 11:
    case 12:
    case 13:
    case 14:
    }
  }

  private void checkText(L2PcInstance activeChar) {
    if (((_type == 0) || (_type == 1) || (_type == 8) || (_type == 17)) && (Config.USE_SAY_FILTER))
    {
      int _inttext = 0;
      _inttext = SayFilter.getInstance().getMatchesCnt(_text);
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
          activeChar.sendMessage("\u0412\u0430\u043C \u0434\u043E\u0431\u0430\u0432\u043B\u0435\u043D\u043E " + karm + " \u043A\u0430\u0440\u043C\u044B \u0437\u0430 \u043F\u043B\u043E\u0445\u043E\u0435 \u0441\u043B\u043E\u0432\u043E");
        }
        _text = filteredText;
      }
    }
  }

  public String getType()
  {
    return "[C] 38 Say2";
  }
}
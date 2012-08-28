package net.sf.l2j.gameserver.network.clientpackets;

import java.nio.BufferUnderflowException;
import java.util.StringTokenizer;
import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.instancemanager.PartyWaitingRoomManager.WaitingRoom;
import net.sf.l2j.gameserver.instancemanager.PetitionManager;
import net.sf.l2j.gameserver.model.BlockList;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2CommandChannel;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.PcKnownList;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.util.Log;
import net.sf.l2j.util.TimeLogger;
import scripts.commands.IVoicedCommandHandler;
import scripts.commands.VoicedCommandHandler;

public final class Say2 extends L2GameClientPacket
{
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
  public static final int PARTYROOM_WAIT = 14;
  public static final int PARTYROOM_COMMANDER = 15;
  public static final int PARTYROOM_ALL = 16;
  public static final int HERO_VOICE = 17;
  private String _text;
  private int _type;
  private String _target;

  protected void readImpl()
  {
    _text = readS();
    try {
      _type = readD();
    } catch (BufferUnderflowException e) {
      _type = 0;
    }
    _target = (_type == 2 ? readS() : null);
  }

  protected void runImpl()
  {
    if ((_type < 0) || (_type >= 18)) {
      return;
    }

    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }

    if (System.currentTimeMillis() - player.gCPH() < 300L) {
      return;
    }
    player.sCPH();

    if (player.isChatBanned()) {
      player.sendPacket(Static.CHAT_BLOCKED);
      return;
    }

    if (player.isInJail()) {
      switch (_type) {
      case 1:
      case 2:
      case 8:
      case 17:
        player.sendPacket(Static.CHAT_BLOCKED);
        return;
      }
    }

    _text = filter(_text);
    if (_text.isEmpty()) {
      return;
    }

    if ((_type == 6) && (player.isGM())) {
      _type = 7;
    }

    if ((player.isInParty()) && (player.getParty().isInCommandChannel())) {
      if ((_text.startsWith("~~")) && (player.getParty().getCommandChannel().getChannelLeader().equals(player))) {
        _type = 15;
        _text = _text.replace("~~", "");
      } else if ((_text.startsWith("~")) && (player.getParty().isLeader(player))) {
        _type = 16;
        _text = _text.replace("~", "");
      }
    }

    if ((Config.USE_CHAT_FILTER) && (!_text.startsWith("."))) {
      switch (_type) {
      case 0:
      case 1:
      case 8:
      case 17:
        String wordn = "";
        String wordnf = "";
        String newWord = "";
        String wordf = "";

        String[] tokens = _text.split("[ ]+");
        for (int i = 0; i < tokens.length; i++) {
          wordn = tokens[i];
          wordnf = replaceIdent(wordn);
          for (String pattern : Config.CHAT_FILTER_STRINGS) {
            if (wordnf.matches(".*" + pattern + ".*")) {
              newWord = wordnf.replace(pattern, Config.CHAT_FILTER_STRING);
              break;
            }
            newWord = wordn;
          }

          wordf = wordf + newWord + " ";
        }
        _text = wordf.replace("null", "");
      }

    }

    if ((Config.PROTECT_SAY) && (player.identSay(_text))) {
      return;
    }
    player.setLastSay(_text);

    CreatureSay cs = new CreatureSay(player.getObjectId(), _type, player.getName(), _text);
    int region;
    switch (_type) {
    case 2:
      L2PcInstance receiver = L2World.getInstance().getPlayer(_target);
      if ((receiver != null) && (!BlockList.isBlocked(receiver, player))) {
        if ((receiver.isChatBanned()) || (receiver.isInJail())) {
          player.sendPacket(Static.PLAYER_BLOCKED);
          return;
        }

        if ((receiver.getMessageRefusal()) || (receiver.getChatIgnore() >= player.getLevel())) {
          player.sendPacket(Static.THE_PERSON_IS_IN_MESSAGE_REFUSAL_MODE);
          return;
        }

        if (receiver.isFantome()) {
          Log.add(TimeLogger.getTime() + player.getName() + ": " + _text, "pm_bot");
        }
        if (receiver.isPartner()) {
          Log.add(TimeLogger.getTime() + player.getName() + ": " + _text, "partner_bot");
          player.sendPacket(SystemMessage.id(SystemMessageId.S1_IS_NOT_ONLINE).addString(_target));
          return;
        }

        receiver.sendPacket(cs);
        player.sendPacket(new CreatureSay(player.getObjectId(), _type, "->" + receiver.getName(), _text));
      } else {
        player.sendPacket(SystemMessage.id(SystemMessageId.S1_IS_NOT_ONLINE).addString(_target));
      }
      break;
    case 1:
      int region;
      if ((Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("on")) || ((Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("gm")) && (player.isGM()))) {
        region = MapRegionTable.getInstance().getMapRegion(player.getX(), player.getY());
        for (L2PcInstance pchar : L2World.getInstance().getAllPlayers())
          if (region == MapRegionTable.getInstance().getMapRegion(pchar.getX(), pchar.getY()))
            pchar.sendSayPacket(cs, player.getLevel());
      }
      else {
        if (!Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("global")) break;
        for (L2PcInstance pchar : L2World.getInstance().getAllPlayers())
          pchar.sendSayPacket(cs, player.getLevel()); 
      }break;
    case 8:
      if (System.currentTimeMillis() - player.gCPBH() < 5000L) {
        player.sendPacket(Static.HERO_DELAY);
        return;
      }
      player.sCPBH();
      if ((Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("on")) || ((Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("gm")) && (player.isGM())))
      {
        for (L2PcInstance pchar : L2World.getInstance().getAllPlayers())
          pchar.sendSayPacket(cs, player.getLevel());
      } else {
        if (!Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("limited")) break;
        region = MapRegionTable.getInstance().getMapRegion(player.getX(), player.getY());
        for (L2PcInstance pchar : L2World.getInstance().getAllPlayers()) {
          if (region == MapRegionTable.getInstance().getMapRegion(pchar.getX(), pchar.getY()))
            pchar.sendSayPacket(cs, player.getLevel());
        }
      }
      break;
    case 0:
      if (_text.startsWith(".")) {
        StringTokenizer st = new StringTokenizer(_text);

        String command = "";
        String target = "";

        command = _text.substring(1);
        IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
        if ((vch != null) && (!player.isInJail())) {
          vch.useVoicedCommand(command, player, target);
          return;
        }
        player.sendPacket(cs);
        FastList players = player.getKnownList().getKnownPlayersInRadius(1250);
        L2PcInstance pc = null;
        FastList.Node n = players.head(); for (FastList.Node end = players.tail(); (n = n.getNext()) != end; ) {
          pc = (L2PcInstance)n.getValue();
          if (pc == null)
          {
            continue;
          }
          pc.sendPacket(cs);
        }
        pc = null;
      } else if ((_text.startsWith("~")) && (Config.ALLOW_RUPOR)) {
        if (player.getItemCount(Config.RUPOR_ID) >= 1) {
          if (player.isInJail()) {
            return;
          }

          L2ItemInstance item = player.getInventory().getItemByItemId(Config.RUPOR_ID);

          if (item == null) {
            player.sendMessage("\u0420\u0435\u043F\u043B\u0438\u043A\u0430 \u043D\u0430 \u0432\u0435\u0441\u044C \u043C\u0438\u0440 \u0441\u0442\u043E\u0438\u0442 1 \u043C\u0438\u043A\u0440\u043E\u0444\u043E\u043D");
            return;
          }

          player.destroyItemByItemId("Say2", Config.RUPOR_ID, 1, player, false);

          _text = _text.substring(1);

          cs = new CreatureSay(player.getObjectId(), 18, player.getName(), player.getName() + ":" + _text);
          for (L2PcInstance pchar : L2World.getInstance().getAllPlayers())
            if (!pchar.isWorldIgnore())
              pchar.sendPacket(cs);
        }
        else
        {
          player.sendMessage("\u0420\u0435\u043F\u043B\u0438\u043A\u0430 \u043D\u0430 \u0432\u0435\u0441\u044C \u043C\u0438\u0440 \u0441\u0442\u043E\u0438\u0442 1 \u043C\u0438\u043A\u0440\u043E\u0444\u043E\u043D");
          return;
        }
      } else {
        player.sendPacket(cs);
        FastList players = player.getKnownList().getKnownPlayersInRadius(1250);
        L2PcInstance pc = null;
        FastList.Node n = players.head(); for (FastList.Node end = players.tail(); (n = n.getNext()) != end; ) {
          pc = (L2PcInstance)n.getValue();
          if (pc == null)
          {
            continue;
          }
          pc.sendSayPacket(cs, player.getLevel());
        }
        pc = null;
      }
      break;
    case 4:
      if (player.getClan() == null) break;
      player.getClan().broadcastToOnlineMembers(cs); break;
    case 9:
      if (player.getClan() == null) break;
      player.getClan().broadcastToOnlineAllyMembers(cs); break;
    case 3:
      if (!player.isInParty()) break;
      player.getParty().broadcastToPartyMembers(cs); break;
    case 6:
    case 7:
      if (!PetitionManager.getInstance().isPlayerInConsultation(player)) {
        player.sendPacket(Static.YOU_ARE_NOT_IN_PETITION_CHAT);
      }
      else
      {
        PetitionManager.getInstance().sendActivePetitionMessage(player, _text);
      }break;
    case 16:
      if ((!player.isInParty()) || 
        (!player.getParty().isInCommandChannel()) || (!player.getParty().isLeader(player))) break;
      player.getParty().getCommandChannel().broadcastToChannelMembers(cs); break;
    case 15:
      if ((!player.isInParty()) || 
        (!player.getParty().isInCommandChannel()) || (!player.getParty().getCommandChannel().getChannelLeader().equals(player))) break;
      player.getParty().getCommandChannel().broadcastToChannelMembers(cs); break;
    case 17:
      if ((!player.isHero()) && (!player.isGM())) break;
      if ((!player.isGM()) && (System.currentTimeMillis() - player.gCPBH() < 5000L)) {
        player.sendPacket(Static.HERO_DELAY);
        return;
      }
      player.sCPBH();

      Log.add(TimeLogger.getTime() + player.getName() + ": " + _text, "hero_chat");

      for (L2PcInstance pchar : L2World.getInstance().getAllPlayers())
        if (!BlockList.isBlocked(pchar, player))
          pchar.sendPacket(cs); 
      break;
    case 14:
      PartyWaitingRoomManager.WaitingRoom room = player.getPartyRoom();
      if (room == null) break;
      for (L2PcInstance member : room.players) {
        if (member == null)
        {
          continue;
        }
        member.sendPacket(cs); } case 5:
    case 10:
    case 11:
    case 12:
    case 13:
    }cs = null;
  }

  public static String filter(String source) {
    if (source.length() > Config.MAX_CHAT_LENGTH) {
      source = source.substring(0, Config.MAX_CHAT_LENGTH);
    }

    source = source.replaceAll("\n", "");
    source = source.replace("\n", "");
    source = source.replace("n\\", "");
    source = source.replace("\r", "");
    source = source.replace("r\\", "");

    source = ltrim(source);
    source = rtrim(source);
    source = itrim(source);
    source = lrtrim(source);
    return source;
  }

  public static String replaceIdent(String word) {
    word = word.toLowerCase();
    word = word.replace("a", "\u0430");
    word = word.replace("c", "\u0441");
    word = word.replace("s", "\u0441");
    word = word.replace("e", "\u0435");
    word = word.replace("k", "\u043A");
    word = word.replace("m", "\u043C");
    word = word.replace("o", "\u043E");
    word = word.replace("0", "\u043E");
    word = word.replace("x", "\u0445");
    word = word.replace("uy", "\u0443\u0439");
    word = word.replace("y", "\u0443");
    word = word.replace("u", "\u0443");
    word = word.replace("\u0451", "\u0435");
    word = word.replace("9", "\u044F");
    word = word.replace("3", "\u0437");
    word = word.replace("z", "\u0437");
    word = word.replace("d", "\u0434");
    word = word.replace("p", "\u043F");
    word = word.replace("i", "\u0438");
    word = word.replace("ya", "\u044F");
    word = word.replace("ja", "\u044F");
    return word;
  }

  public static String ltrim(String source) {
    return source.replaceAll("^\\s+", "");
  }

  public static String rtrim(String source) {
    return source.replaceAll("\\s+$", "");
  }

  public static String itrim(String source) {
    return source.replaceAll("\\b\\s{2,}\\b", " ");
  }

  public static String trim(String source) {
    return itrim(ltrim(rtrim(source)));
  }

  public static String lrtrim(String source) {
    return ltrim(rtrim(source));
  }
}
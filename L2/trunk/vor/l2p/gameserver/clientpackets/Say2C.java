package l2p.gameserver.clientpackets;

import com.graphbuilder.math.Expression;
import com.graphbuilder.math.ExpressionParseException;
import com.graphbuilder.math.ExpressionTree;
import com.graphbuilder.math.VarMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import l2p.gameserver.Config;
import l2p.gameserver.cache.ItemInfoCache;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2p.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2p.gameserver.instancemanager.PetitionManager;
import l2p.gameserver.model.CommandChannel;
import l2p.gameserver.model.GameObjectsStorage;
import l2p.gameserver.model.Party;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.World;
import l2p.gameserver.model.base.PlayerAccess;
import l2p.gameserver.model.entity.olympiad.OlympiadGame;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.model.matching.MatchingRoom;
import l2p.gameserver.model.pledge.Alliance;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ActionFail;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.Say2;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.serverpackets.components.ChatType;
import l2p.gameserver.serverpackets.components.CustomMessage;
import l2p.gameserver.serverpackets.components.IStaticPacket;
import l2p.gameserver.utils.AntiFlood;
import l2p.gameserver.utils.Log;
import l2p.gameserver.utils.MapUtils;
import l2p.gameserver.utils.Strings;
import l2p.gameserver.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Say2C extends L2GameClientPacket
{
  private static final Logger _log = LoggerFactory.getLogger(Say2C.class);

  private static final Pattern EX_ITEM_LINK_PATTERN = Pattern.compile("[\b]\tType=[0-9]+[\\s]+\tID=([0-9]+)[\\s]+\tColor=[0-9]+[\\s]+\tUnderline=[0-9]+[\\s]+\tTitle=\033(.[^\033]*)[^\b]");
  private static final Pattern SKIP_ITEM_LINK_PATTERN = Pattern.compile("[\b]\tType=[0-9]+(.[^\b]*)[\b]");
  private String _text;
  private ChatType _type;
  private String _target;

  protected void readImpl()
  {
    _text = readS(Config.CHAT_MESSAGE_MAX_LEN);
    _type = ((ChatType)l2p.commons.lang.ArrayUtils.valid(ChatType.VALUES, readD()));
    _target = (_type == ChatType.TELL ? readS(Config.CNAME_MAXLEN) : null);
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    if ((_type == null) || (_text == null) || (_text.length() == 0))
    {
      activeChar.sendActionFailed();
      return;
    }

    _text = _text.replaceAll("\\\\n", "\n");

    if (_text.contains("\n"))
    {
      String[] lines = _text.split("\n");
      _text = "";
      for (int i = 0; i < lines.length; i++)
      {
        lines[i] = lines[i].trim();
        if (lines[i].length() == 0)
          continue;
        if (_text.length() > 0)
          _text = new StringBuilder().append(_text).append("\n  >").toString();
        _text = new StringBuilder().append(_text).append(lines[i]).toString();
      }
    }

    if (_text.length() == 0)
    {
      activeChar.sendActionFailed();
      return;
    }

    if (_text.startsWith("."))
    {
      String fullcmd = _text.substring(1).trim();
      String command = fullcmd.split("\\s+")[0];
      String args = fullcmd.substring(command.length()).trim();

      if (command.length() > 0)
      {
        IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
        if (vch != null)
        {
          vch.useVoicedCommand(command, activeChar, args);
          return;
        }
      }
      activeChar.sendMessage(new CustomMessage("common.command404", activeChar, new Object[0]));
      return;
    }
    if (_text.startsWith("=="))
    {
      String expression = _text.substring(2);
      Expression expr = null;

      if (!expression.isEmpty())
      {
        try
        {
          expr = ExpressionTree.parse(expression);
        }
        catch (ExpressionParseException epe)
        {
        }

        if (expr != null)
        {
          try
          {
            VarMap vm = new VarMap();
            vm.setValue("adena", activeChar.getAdena());
            double result = expr.eval(vm, null);
            activeChar.sendMessage(expression);
            activeChar.sendMessage(new StringBuilder().append("=").append(Util.formatDouble(result, "NaN", false)).toString());
          }
          catch (Exception e)
          {
          }
        }

      }

      return;
    }

    if ((Config.CHATFILTER_MIN_LEVEL > 0) && (org.apache.commons.lang3.ArrayUtils.contains(Config.CHATFILTER_CHANNELS, _type.ordinal())) && (activeChar.getLevel() < Config.CHATFILTER_MIN_LEVEL))
    {
      if (Config.CHATFILTER_WORK_TYPE == 1) {
        _type = ChatType.ALL;
      } else if (Config.CHATFILTER_WORK_TYPE == 2)
      {
        activeChar.sendMessage(new CustomMessage("chat.NotHavePermission", activeChar, new Object[0]).addNumber(Config.CHATFILTER_MIN_LEVEL));
        return;
      }
    }

    boolean globalchat = (_type != ChatType.ALLIANCE) && (_type != ChatType.CLAN) && (_type != ChatType.PARTY);

    if (((globalchat) || (org.apache.commons.lang3.ArrayUtils.contains(Config.BAN_CHANNEL_LIST, _type.ordinal()))) && (activeChar.getNoChannel() != 0L))
    {
      if ((activeChar.getNoChannelRemained() > 0L) || (activeChar.getNoChannel() < 0L))
      {
        if (activeChar.getNoChannel() > 0L)
        {
          int timeRemained = Math.round((float)(activeChar.getNoChannelRemained() / 60000L));
          activeChar.sendMessage(new CustomMessage("common.ChatBanned", activeChar, new Object[0]).addNumber(timeRemained));
        }
        else {
          activeChar.sendMessage(new CustomMessage("common.ChatBannedPermanently", activeChar, new Object[0]));
        }activeChar.sendActionFailed();
        return;
      }
      activeChar.updateNoChannel(0L);
    }

    if (globalchat) {
      if (Config.ABUSEWORD_REPLACE)
      {
        if (Config.containsAbuseWord(_text))
        {
          _text = Config.ABUSEWORD_REPLACE_STRING;
          activeChar.sendActionFailed();
        }
      }
      else if ((Config.ABUSEWORD_BANCHAT) && (Config.containsAbuseWord(_text)))
      {
        activeChar.sendMessage(new CustomMessage("common.ChatBanned", activeChar, new Object[0]).addNumber(Config.ABUSEWORD_BANTIME * 60));
        Log.add(new StringBuilder().append(activeChar).append(": ").append(_text).toString(), "abuse");
        activeChar.updateNoChannel(Config.ABUSEWORD_BANTIME * 60000);
        activeChar.sendActionFailed();
        return;
      }
    }

    Matcher m = EX_ITEM_LINK_PATTERN.matcher(_text);

    while (m.find())
    {
      int objectId = Integer.parseInt(m.group(1));
      ItemInstance item = activeChar.getInventory().getItemByObjectId(objectId);

      if (item == null)
      {
        activeChar.sendActionFailed();
        break;
      }

      ItemInfoCache.getInstance().put(item);
    }

    String translit = activeChar.getVar("translit");
    if (translit != null)
    {
      m = SKIP_ITEM_LINK_PATTERN.matcher(_text);
      StringBuilder sb = new StringBuilder();
      int end = 0;
      while (m.find())
      {
        sb.append(Strings.fromTranslit(_text.substring(end, end = m.start()), translit.equals("tl") ? 1 : 2));
        sb.append(_text.substring(end, end = m.end()));
      }

      _text = sb.append(Strings.fromTranslit(_text.substring(end, _text.length()), translit.equals("tl") ? 1 : 2)).toString();
    }

    Log.LogChat(_type.name(), activeChar.getName(), _target, _text);

    Say2 cs = new Say2(activeChar.getObjectId(), _type, activeChar.getName(), _text);

    switch (1.$SwitchMap$l2p$gameserver$serverpackets$components$ChatType[_type.ordinal()])
    {
    case 1:
      Player receiver = World.getPlayer(_target);
      if ((receiver != null) && (receiver.isInOfflineMode()))
      {
        activeChar.sendMessage("The person is in offline trade mode.");
        activeChar.sendActionFailed();
      }
      else if ((receiver != null) && (!receiver.isInBlockList(activeChar)) && (!receiver.isBlockAll()))
      {
        if (!receiver.getMessageRefusal())
        {
          if (activeChar.antiFlood.canTell(receiver.getObjectId(), _text)) {
            receiver.sendPacket(cs);
          }
          cs = new Say2(activeChar.getObjectId(), _type, new StringBuilder().append("->").append(receiver.getName()).toString(), _text);
          activeChar.sendPacket(cs);
        }
        else {
          activeChar.sendPacket(Msg.THE_PERSON_IS_IN_A_MESSAGE_REFUSAL_MODE);
        }
      } else if (receiver == null) {
        activeChar.sendPacket(new IStaticPacket[] { new SystemMessage(3).addString(_target), ActionFail.STATIC });
      } else {
        activeChar.sendPacket(new IStaticPacket[] { Msg.YOU_HAVE_BEEN_BLOCKED_FROM_THE_CONTACT_YOU_SELECTED, ActionFail.STATIC });
      }break;
    case 2:
      if (activeChar.isCursedWeaponEquipped())
      {
        activeChar.sendPacket(Msg.SHOUT_AND_TRADE_CHATING_CANNOT_BE_USED_SHILE_POSSESSING_A_CURSED_WEAPON);
        return;
      }
      if (activeChar.isInObserverMode())
      {
        activeChar.sendPacket(Msg.YOU_CANNOT_CHAT_LOCALLY_WHILE_OBSERVING);
        return;
      }

      if ((!activeChar.isGM()) && (!activeChar.antiFlood.canShout(_text)))
      {
        activeChar.sendMessage("Shout chat is allowed once per 5 seconds.");
        return;
      }

      if (Config.GLOBAL_SHOUT)
        announce(activeChar, cs);
      else {
        shout(activeChar, cs);
      }
      activeChar.sendPacket(cs);
      break;
    case 3:
      if (activeChar.isCursedWeaponEquipped())
      {
        activeChar.sendPacket(Msg.SHOUT_AND_TRADE_CHATING_CANNOT_BE_USED_SHILE_POSSESSING_A_CURSED_WEAPON);
        return;
      }
      if (activeChar.isInObserverMode())
      {
        activeChar.sendPacket(Msg.YOU_CANNOT_CHAT_LOCALLY_WHILE_OBSERVING);
        return;
      }

      if ((!activeChar.isGM()) && (!activeChar.antiFlood.canTrade(_text)))
      {
        activeChar.sendMessage("Trade chat is allowed once per 5 seconds.");
        return;
      }

      if (Config.GLOBAL_TRADE_CHAT)
        announce(activeChar, cs);
      else {
        shout(activeChar, cs);
      }
      activeChar.sendPacket(cs);
      break;
    case 4:
      if (activeChar.isCursedWeaponEquipped()) {
        cs = new Say2(activeChar.getObjectId(), _type, activeChar.getTransformationName(), _text);
      }
      List list = null;

      if ((activeChar.isInObserverMode()) && (activeChar.getObserverRegion() != null) && (activeChar.getOlympiadObserveGame() != null))
      {
        OlympiadGame game = activeChar.getOlympiadObserveGame();
        if (game != null)
          list = game.getAllPlayers();
      }
      else if (activeChar.isInOlympiadMode())
      {
        OlympiadGame game = activeChar.getOlympiadGame();
        if (game != null)
          list = game.getAllPlayers();
      }
      else {
        list = World.getAroundPlayers(activeChar);
      }
      if (list != null) {
        for (Player player : list)
        {
          if ((player == activeChar) || (player.getReflection() != activeChar.getReflection()) || (player.isBlockAll()) || (player.isInBlockList(activeChar)))
            continue;
          player.sendPacket(cs);
        }
      }
      activeChar.sendPacket(cs);
      break;
    case 5:
      if (activeChar.getClan() == null) break;
      activeChar.getClan().broadcastToOnlineMembers(new L2GameServerPacket[] { cs }); break;
    case 6:
      if ((activeChar.getClan() == null) || (activeChar.getClan().getAlliance() == null)) break;
      activeChar.getClan().getAlliance().broadcastToOnlineMembers(cs); break;
    case 7:
      if (!activeChar.isInParty()) break;
      activeChar.getParty().broadCast(new IStaticPacket[] { cs }); break;
    case 8:
      MatchingRoom r = activeChar.getMatchingRoom();
      if ((r == null) || (r.getType() != MatchingRoom.PARTY_MATCHING)) break;
      r.broadCast(new IStaticPacket[] { cs }); break;
    case 9:
      if ((!activeChar.isInParty()) || (!activeChar.getParty().isInCommandChannel()))
      {
        activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_AUTHORITY_TO_USE_THE_COMMAND_CHANNEL);
        return;
      }
      if (activeChar.getParty().getCommandChannel().getChannelLeader() == activeChar)
        activeChar.getParty().getCommandChannel().broadCast(new IStaticPacket[] { cs });
      else
        activeChar.sendPacket(Msg.ONLY_CHANNEL_OPENER_CAN_GIVE_ALL_COMMAND);
      break;
    case 10:
      if ((!activeChar.isInParty()) || (!activeChar.getParty().isInCommandChannel()))
      {
        activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_AUTHORITY_TO_USE_THE_COMMAND_CHANNEL);
        return;
      }
      if (activeChar.getParty().isLeader(activeChar))
        activeChar.getParty().getCommandChannel().broadcastToChannelPartyLeaders(cs);
      else
        activeChar.sendPacket(Msg.ONLY_A_PARTY_LEADER_CAN_ACCESS_THE_COMMAND_CHANNEL);
      break;
    case 11:
      boolean PremiumHeroChat = false;
      if ((Config.PREMIUM_HEROCHAT) && (activeChar.getNetConnection().getBonus() > 1.0D)) {
        long endtime = activeChar.getNetConnection().getBonusExpire();
        if (endtime >= 0L) {
          PremiumHeroChat = true;
        }

      }

      if ((!activeChar.isHero()) && (!activeChar.getPlayerAccess().CanAnnounce) && (!PremiumHeroChat)) {
        break;
      }
      if ((!activeChar.getPlayerAccess().CanAnnounce) && 
        (!activeChar.antiFlood.canHero(_text)))
      {
        activeChar.sendMessage("Hero chat is allowed once per 10 seconds.");
        return;
      }
      for (Player player : GameObjectsStorage.getAllPlayersForIterate())
        if ((!player.isInBlockList(activeChar)) && (!player.isBlockAll()))
          player.sendPacket(cs); 
      break;
    case 12:
    case 13:
      if (!PetitionManager.getInstance().isPlayerInConsultation(activeChar))
      {
        activeChar.sendPacket(new SystemMessage(745));
        return;
      }

      PetitionManager.getInstance().sendActivePetitionMessage(activeChar, _text);
      break;
    case 14:
      if (activeChar.getBattlefieldChatId() == 0) {
        return;
      }
      for (Player player : GameObjectsStorage.getAllPlayersForIterate())
        if ((!player.isInBlockList(activeChar)) && (!player.isBlockAll()) && (player.getBattlefieldChatId() == activeChar.getBattlefieldChatId()))
          player.sendPacket(cs);
      break;
    case 15:
      MatchingRoom r2 = activeChar.getMatchingRoom();
      if ((r2 == null) || (r2.getType() != MatchingRoom.CC_MATCHING)) break;
      r2.broadCast(new IStaticPacket[] { cs }); break;
    default:
      _log.warn(new StringBuilder().append("Character ").append(activeChar.getName()).append(" used unknown chat type: ").append(_type.ordinal()).append(".").toString());
    }
  }

  private static void shout(Player activeChar, Say2 cs)
  {
    int rx = MapUtils.regionX(activeChar);
    int ry = MapUtils.regionY(activeChar);
    int offset = Config.SHOUT_OFFSET;

    for (Player player : GameObjectsStorage.getAllPlayersForIterate())
    {
      if ((player == activeChar) || (activeChar.getReflection() != player.getReflection()) || (player.isBlockAll()) || (player.isInBlockList(activeChar))) {
        continue;
      }
      int tx = MapUtils.regionX(player);
      int ty = MapUtils.regionY(player);

      if (((tx >= rx - offset) && (tx <= rx + offset) && (ty >= ry - offset) && (ty <= ry + offset)) || (activeChar.isInRangeZ(player, Config.CHAT_RANGE)))
        player.sendPacket(cs);
    }
  }

  private static void announce(Player activeChar, Say2 cs)
  {
    for (Player player : GameObjectsStorage.getAllPlayersForIterate())
    {
      if ((player == activeChar) || (activeChar.getReflection() != player.getReflection()) || (player.isBlockAll()) || (player.isInBlockList(activeChar))) {
        continue;
      }
      player.sendPacket(cs);
    }
  }
}
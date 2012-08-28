package l2p.gameserver.model.instances;

import java.util.Calendar;
import java.util.StringTokenizer;
import l2p.commons.collections.MultiValueSet;
import l2p.commons.time.cron.SchedulingPattern;
import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.data.htm.HtmCache;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.ClassId;
import l2p.gameserver.model.entity.SevenSigns;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.serverpackets.NpcHtmlMessage;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.serverpackets.SystemMessage2;
import l2p.gameserver.serverpackets.components.CustomMessage;
import l2p.gameserver.serverpackets.components.IStaticPacket;
import l2p.gameserver.tables.ClanTable;
import l2p.gameserver.templates.npc.NpcTemplate;
import l2p.gameserver.utils.ItemFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignsPriestInstance extends NpcInstance
{
  private static final Logger _log = LoggerFactory.getLogger(SignsPriestInstance.class);

  public SignsPriestInstance(int objectId, NpcTemplate template)
  {
    super(objectId, template);
  }

  private void showChatWindow(Player player, int val, String suffix, boolean isDescription)
  {
    String filename = "seven_signs/";
    filename = new StringBuilder().append(filename).append(isDescription ? new StringBuilder().append("desc_").append(val).toString() : new StringBuilder().append("signs_").append(val).toString()).toString();
    filename = new StringBuilder().append(filename).append(suffix != null ? new StringBuilder().append("_").append(suffix).append(".htm").toString() : ".htm").toString();
    showChatWindow(player, filename, new Object[0]);
  }

  private boolean getPlayerAllyHasCastle(Player player)
  {
    Clan playerClan = player.getClan();

    if (playerClan == null) {
      return false;
    }

    if (!Config.ALT_GAME_REQUIRE_CLAN_CASTLE)
    {
      int allyId = playerClan.getAllyId();

      if (allyId != 0)
      {
        Clan[] clanList = ClanTable.getInstance().getClans();

        for (Clan clan : clanList)
          if ((clan.getAllyId() == allyId) && 
            (clan.getCastle() > 0))
            return true;
      }
    }
    return playerClan.getCastle() > 0;
  }

  public void onBypassFeedback(Player player, String command)
  {
    if (!canBypassCheck(player, this)) {
      return;
    }
    if (((getNpcId() == 31113) || (getNpcId() == 31126)) && 
      (SevenSigns.getInstance().getPlayerCabal(player) == 0) && (!player.isGM())) {
      return;
    }

    super.onBypassFeedback(player, command);

    if (command.startsWith("SevenSignsDesc"))
    {
      int val = Integer.parseInt(command.substring(15));

      showChatWindow(player, val, null, true);
    }
    else if (command.startsWith("SevenSigns"))
    {
      int cabal = 0;
      int stoneType = 0;

      ItemInstance ancientAdena = player.getInventory().getItemByItemId(5575);
      long ancientAdenaAmount = ancientAdena == null ? 0L : ancientAdena.getCount();
      int val = Integer.parseInt(command.substring(11, 12).trim());

      if (command.length() > 12) {
        val = Integer.parseInt(command.substring(11, 13).trim());
      }
      if (command.length() > 13) {
        try
        {
          cabal = Integer.parseInt(command.substring(14, 15).trim());
        }
        catch (Exception e)
        {
          try
          {
            cabal = Integer.parseInt(command.substring(13, 14).trim());
          }
          catch (Exception e2)
          {
          }
        }
      }
      switch (val)
      {
      case 2:
        if (!player.getInventory().validateCapacity(1L))
        {
          player.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
          return;
        }

        if (500L > player.getAdena())
        {
          player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
          return;
        }

        player.reduceAdena(500L, true);
        player.getInventory().addItem(ItemFunctions.createItem(5707));
        player.sendPacket(SystemMessage2.obtainItems(5707, 1L, 0));

        break;
      case 3:
      case 8:
        cabal = SevenSigns.getInstance().getPriestCabal(getNpcId());
        showChatWindow(player, val, SevenSigns.getCabalShortName(cabal), false);
        break;
      case 10:
        cabal = SevenSigns.getInstance().getPriestCabal(getNpcId());
        if (SevenSigns.getInstance().isSealValidationPeriod()) {
          showChatWindow(player, val, "", false);
        }
        else {
          showChatWindow(player, val, getParameters().getString("town", "no"), false);
        }
        break;
      case 4:
        int newSeal = Integer.parseInt(command.substring(15));
        int oldCabal = SevenSigns.getInstance().getPlayerCabal(player);

        if (oldCabal != 0)
        {
          player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2SignsPriestInstance.AlreadyMember", player, new Object[0]).addString(SevenSigns.getCabalName(cabal)));
          return;
        }
        if (player.getClassId().level() == 0)
        {
          player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2SignsPriestInstance.YouAreNewbie", player, new Object[0]));
        }
        else
        {
          if ((player.getClassId().level() >= 2) && 
            (Config.ALT_GAME_REQUIRE_CASTLE_DAWN)) {
            if (getPlayerAllyHasCastle(player))
            {
              if (cabal == 1)
              {
                player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2SignsPriestInstance.CastleOwning", player, new Object[0]));
                return;
              }

            }
            else if (cabal == 2)
            {
              boolean allowJoinDawn = false;

              if (Functions.getItemCount(player, 6388) > 0L)
              {
                Functions.removeItem(player, 6388, 1L);
                allowJoinDawn = true;
              }
              else if ((Config.ALT_GAME_ALLOW_ADENA_DAWN) && (player.getAdena() >= 50000L))
              {
                player.reduceAdena(50000L, true);
                allowJoinDawn = true;
              }

              if (!allowJoinDawn)
              {
                if (Config.ALT_GAME_ALLOW_ADENA_DAWN)
                  player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2SignsPriestInstance.CastleOwningCertificate", player, new Object[0]));
                else
                  player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2SignsPriestInstance.CastleOwningCertificate2", player, new Object[0]));
                return;
              }
            }
          }
          SevenSigns.getInstance().setPlayerInfo(player.getObjectId(), cabal, newSeal);
          if (cabal == 2)
            player.sendPacket(Msg.YOU_WILL_PARTICIPATE_IN_THE_SEVEN_SIGNS_AS_A_MEMBER_OF_THE_LORDS_OF_DAWN);
          else {
            player.sendPacket(Msg.YOU_WILL_PARTICIPATE_IN_THE_SEVEN_SIGNS_AS_A_MEMBER_OF_THE_REVOLUTIONARIES_OF_DUSK);
          }

          switch (newSeal)
          {
          case 1:
            player.sendPacket(Msg.YOUVE_CHOSEN_TO_FIGHT_FOR_THE_SEAL_OF_AVARICE_DURING_THIS_QUEST_EVENT_PERIOD);
            break;
          case 2:
            player.sendPacket(Msg.YOUVE_CHOSEN_TO_FIGHT_FOR_THE_SEAL_OF_GNOSIS_DURING_THIS_QUEST_EVENT_PERIOD);
            break;
          case 3:
            player.sendPacket(Msg.YOUVE_CHOSEN_TO_FIGHT_FOR_THE_SEAL_OF_STRIFE_DURING_THIS_QUEST_EVENT_PERIOD);
          }

          showChatWindow(player, 4, SevenSigns.getCabalShortName(cabal), false);
        }break;
      case 6:
        stoneType = Integer.parseInt(command.substring(13));
        ItemInstance redStones = player.getInventory().getItemByItemId(6362);
        long redStoneCount = redStones == null ? 0L : redStones.getCount();
        ItemInstance greenStones = player.getInventory().getItemByItemId(6361);
        long greenStoneCount = greenStones == null ? 0L : greenStones.getCount();
        ItemInstance blueStones = player.getInventory().getItemByItemId(6360);
        long blueStoneCount = blueStones == null ? 0L : blueStones.getCount();
        long contribScore = SevenSigns.getInstance().getPlayerContribScore(player);
        boolean stonesFound = false;

        if (contribScore == SevenSigns.MAXIMUM_PLAYER_CONTRIB) {
          player.sendPacket(Msg.CONTRIBUTION_LEVEL_HAS_EXCEEDED_THE_LIMIT_YOU_MAY_NOT_CONTINUE);
        }
        else {
          long redContribCount = 0L;
          long greenContribCount = 0L;
          long blueContribCount = 0L;

          switch (stoneType)
          {
          case 1:
            blueContribCount = (SevenSigns.MAXIMUM_PLAYER_CONTRIB - contribScore) / 3L;
            if (blueContribCount <= blueStoneCount) break;
            blueContribCount = blueStoneCount; break;
          case 2:
            greenContribCount = (SevenSigns.MAXIMUM_PLAYER_CONTRIB - contribScore) / 5L;
            if (greenContribCount <= greenStoneCount) break;
            greenContribCount = greenStoneCount; break;
          case 3:
            redContribCount = (SevenSigns.MAXIMUM_PLAYER_CONTRIB - contribScore) / 10L;
            if (redContribCount <= redStoneCount) break;
            redContribCount = redStoneCount; break;
          case 4:
            long tempContribScore = contribScore;
            redContribCount = (SevenSigns.MAXIMUM_PLAYER_CONTRIB - tempContribScore) / 10L;
            if (redContribCount > redStoneCount)
              redContribCount = redStoneCount;
            tempContribScore += redContribCount * 10L;
            greenContribCount = (SevenSigns.MAXIMUM_PLAYER_CONTRIB - tempContribScore) / 5L;
            if (greenContribCount > greenStoneCount)
              greenContribCount = greenStoneCount;
            tempContribScore += greenContribCount * 5L;
            blueContribCount = (SevenSigns.MAXIMUM_PLAYER_CONTRIB - tempContribScore) / 3L;
            if (blueContribCount <= blueStoneCount) break;
            blueContribCount = blueStoneCount;
          }

          if ((redContribCount > 0L) && 
            (player.getInventory().destroyItemByItemId(6362, redContribCount)))
            stonesFound = true;
          if ((greenContribCount > 0L) && 
            (player.getInventory().destroyItemByItemId(6361, greenContribCount)))
            stonesFound = true;
          if (blueContribCount > 0L)
          {
            ItemInstance temp = player.getInventory().getItemByItemId(6360);
            if (player.getInventory().destroyItemByItemId(6360, blueContribCount)) {
              stonesFound = true;
            }
          }
          if (!stonesFound)
          {
            player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2SignsPriestInstance.DontHaveAnySSType", player, new Object[0]));
            return;
          }

          contribScore = SevenSigns.getInstance().addPlayerStoneContrib(player, blueContribCount, greenContribCount, redContribCount);
          SystemMessage sm = new SystemMessage(1267);
          sm.addNumber(contribScore);
          player.sendPacket(sm);

          showChatWindow(player, 6, null, false);
        }
        break;
      case 7:
        long ancientAdenaConvert = 0L;
        try
        {
          ancientAdenaConvert = Long.parseLong(command.substring(13).trim());
        }
        catch (NumberFormatException e)
        {
          player.sendMessage(new CustomMessage("common.IntegerAmount", player, new Object[0]));
          return;
        }
        catch (StringIndexOutOfBoundsException e)
        {
          player.sendMessage(new CustomMessage("common.IntegerAmount", player, new Object[0]));
          return;
        }

        if ((ancientAdenaAmount < ancientAdenaConvert) || (ancientAdenaConvert < 1L))
        {
          player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
          return;
        }

        if (!player.getInventory().destroyItemByItemId(5575, ancientAdenaConvert))
          break;
        player.addAdena(ancientAdenaConvert);
        player.sendPacket(SystemMessage2.removeItems(5575, ancientAdenaConvert));
        player.sendPacket(SystemMessage2.obtainItems(57, ancientAdenaConvert, 0)); break;
      case 9:
        int playerCabal = SevenSigns.getInstance().getPlayerCabal(player);
        int winningCabal = SevenSigns.getInstance().getCabalHighestScore();

        if ((!SevenSigns.getInstance().isSealValidationPeriod()) || (playerCabal != winningCabal))
          break;
        int ancientAdenaReward = SevenSigns.getInstance().getAncientAdenaReward(player, true);

        if (ancientAdenaReward < 3)
        {
          showChatWindow(player, 9, "b", false);
          return;
        }

        ancientAdena = ItemFunctions.createItem(5575);
        ancientAdena.setCount(ancientAdenaReward);
        player.getInventory().addItem(ancientAdena);
        player.sendPacket(SystemMessage2.obtainItems(5575, ancientAdenaReward, 0));
        showChatWindow(player, 9, "a", false);
        break;
      case 11:
        try
        {
          String portInfo = command.substring(14).trim();

          StringTokenizer st = new StringTokenizer(portInfo);
          int x = Integer.parseInt(st.nextToken());
          int y = Integer.parseInt(st.nextToken());
          int z = Integer.parseInt(st.nextToken());
          long ancientAdenaCost = Long.parseLong(st.nextToken());

          if ((ancientAdenaCost > 0L) && 
            (!player.getInventory().destroyItemByItemId(5575, ancientAdenaCost)))
          {
            player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
            return;
          }
          player.teleToLocation(x, y, z);
        }
        catch (Exception e)
        {
          _log.warn(new StringBuilder().append("SevenSigns: Error occurred while teleporting player: ").append(e).toString());
        }

      case 17:
        stoneType = Integer.parseInt(command.substring(14));
        int stoneId = 0;
        long stoneCount = 0L;
        int stoneValue = 0;
        String stoneColor = null;

        if (stoneType == 4)
        {
          ItemInstance BlueStoneInstance = player.getInventory().getItemByItemId(6360);
          long bcount = BlueStoneInstance != null ? BlueStoneInstance.getCount() : 0L;
          ItemInstance GreenStoneInstance = player.getInventory().getItemByItemId(6361);
          long gcount = GreenStoneInstance != null ? GreenStoneInstance.getCount() : 0L;
          ItemInstance RedStoneInstance = player.getInventory().getItemByItemId(6362);
          long rcount = RedStoneInstance != null ? RedStoneInstance.getCount() : 0L;
          long ancientAdenaReward = SevenSigns.calcAncientAdenaReward(bcount, gcount, rcount);
          if (ancientAdenaReward > 0L)
          {
            if (BlueStoneInstance != null)
            {
              player.getInventory().destroyItem(BlueStoneInstance, bcount);
              player.sendPacket(SystemMessage2.removeItems(6360, bcount));
            }
            if (GreenStoneInstance != null)
            {
              player.getInventory().destroyItem(GreenStoneInstance, gcount);
              player.sendPacket(SystemMessage2.removeItems(6361, gcount));
            }
            if (RedStoneInstance != null)
            {
              player.getInventory().destroyItem(RedStoneInstance, rcount);
              player.sendPacket(SystemMessage2.removeItems(6362, rcount));
            }

            ancientAdena = ItemFunctions.createItem(5575);
            ancientAdena.setCount(ancientAdenaReward);
            player.getInventory().addItem(ancientAdena);
            player.sendPacket(SystemMessage2.obtainItems(5575, ancientAdenaReward, 0));
          }
          else {
            player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2SignsPriestInstance.DontHaveAnySS", player, new Object[0]));
          }
        }
        else {
          switch (stoneType)
          {
          case 1:
            stoneColor = "blue";
            stoneId = 6360;
            stoneValue = 3;
            break;
          case 2:
            stoneColor = "green";
            stoneId = 6361;
            stoneValue = 5;
            break;
          case 3:
            stoneColor = "red";
            stoneId = 6362;
            stoneValue = 10;
          }

          ItemInstance stoneInstance = player.getInventory().getItemByItemId(stoneId);

          if (stoneInstance != null) {
            stoneCount = stoneInstance.getCount();
          }
          String path = "seven_signs/signs_17.htm";
          String content = HtmCache.getInstance().getNotNull(path, player);

          if (content != null)
          {
            content = content.replaceAll("%stoneColor%", stoneColor);
            content = content.replaceAll("%stoneValue%", String.valueOf(stoneValue));
            content = content.replaceAll("%stoneCount%", String.valueOf(stoneCount));
            content = content.replaceAll("%stoneItemId%", String.valueOf(stoneId));

            NpcHtmlMessage html = new NpcHtmlMessage(player, this);
            html.setHtml(content);
            player.sendPacket(html);
          }
          else {
            _log.warn(new StringBuilder().append("Problem with HTML text seven_signs/signs_17.htm: ").append(path).toString());
          }
        }break;
      case 18:
        int convertStoneId = Integer.parseInt(command.substring(14, 18));
        long convertCount = 0L;
        try
        {
          convertCount = Long.parseLong(command.substring(19).trim());
        }
        catch (Exception NumberFormatException)
        {
          player.sendMessage(new CustomMessage("common.IntegerAmount", player, new Object[0]));
          break;
        }

        ItemInstance convertItem = player.getInventory().getItemByItemId(convertStoneId);
        if (convertItem == null)
        {
          player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2SignsPriestInstance.DontHaveAnySSType", player, new Object[0]));
        }
        else
        {
          long totalCount = convertItem.getCount();
          long ancientAdenaReward = 0L;
          if ((convertCount <= totalCount) && (convertCount > 0L))
          {
            switch (convertStoneId)
            {
            case 6360:
              ancientAdenaReward = SevenSigns.calcAncientAdenaReward(convertCount, 0L, 0L);
              break;
            case 6361:
              ancientAdenaReward = SevenSigns.calcAncientAdenaReward(0L, convertCount, 0L);
              break;
            case 6362:
              ancientAdenaReward = SevenSigns.calcAncientAdenaReward(0L, 0L, convertCount);
            }

            if (!player.getInventory().destroyItemByItemId(convertStoneId, convertCount))
              break;
            ancientAdena = ItemFunctions.createItem(5575);
            ancientAdena.setCount(ancientAdenaReward);
            player.getInventory().addItem(ancientAdena);
            player.sendPacket(new IStaticPacket[] { SystemMessage2.removeItems(convertStoneId, convertCount), SystemMessage2.obtainItems(5575, ancientAdenaReward, 0) });
          }
          else
          {
            player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2SignsPriestInstance.DontHaveSSAmount", player, new Object[0]));
          }
        }break;
      case 19:
        int chosenSeal = Integer.parseInt(command.substring(16));
        String fileSuffix = new StringBuilder().append(SevenSigns.getSealName(chosenSeal, true)).append("_").append(SevenSigns.getCabalShortName(cabal)).toString();

        showChatWindow(player, val, fileSuffix, false);
        break;
      case 20:
        StringBuilder contentBuffer = new StringBuilder("<html><body><font color=\"LEVEL\">[Seal Status]</font><br>");

        for (int i = 1; i < 4; i++)
        {
          int sealOwner = SevenSigns.getInstance().getSealOwner(i);
          if (sealOwner != 0)
            contentBuffer.append(new StringBuilder().append("[").append(SevenSigns.getSealName(i, false)).append(": ").append(SevenSigns.getCabalName(sealOwner)).append("]<br>").toString());
          else {
            contentBuffer.append(new StringBuilder().append("[").append(SevenSigns.getSealName(i, false)).append(": Nothingness]<br>").toString());
          }
        }
        contentBuffer.append(new StringBuilder().append("<a action=\"bypass -h npc_").append(getObjectId()).append("_SevenSigns 3 ").append(cabal).append("\">Go back.</a></body></html>").toString());

        NpcHtmlMessage html2 = new NpcHtmlMessage(player, this);
        html2.setHtml(contentBuffer.toString());
        player.sendPacket(html2);
        break;
      case 21:
        if (player.getLevel() < 60)
        {
          showChatWindow(player, 20, null, false);
          return;
        }
        if (player.getVarInt("bmarketadena", 0) >= 500000)
        {
          showChatWindow(player, 21, null, false);
          return;
        }
        Calendar sh = Calendar.getInstance();
        sh.set(11, 20);
        sh.set(12, 0);
        sh.set(13, 0);
        Calendar eh = Calendar.getInstance();
        eh.set(11, 23);
        eh.set(12, 59);
        eh.set(13, 59);
        if ((System.currentTimeMillis() > sh.getTimeInMillis()) && (System.currentTimeMillis() < eh.getTimeInMillis()))
          showChatWindow(player, 23, null, false);
        else
          showChatWindow(player, 22, null, false);
        break;
      case 22:
        int tradeMult = 4;
        int limit = 500000;
        long adenaConvert;
        try { adenaConvert = Long.parseLong(command.substring(14).trim());
        }
        catch (NumberFormatException e)
        {
          player.sendMessage(new CustomMessage("common.IntegerAmount", player, new Object[0]));
          return;
        }
        catch (StringIndexOutOfBoundsException e)
        {
          player.sendMessage(new CustomMessage("common.IntegerAmount", player, new Object[0]));
          return;
        }
        long adenaAmount = ItemFunctions.getItemCount(player, 57);
        int amountLimit = player.getVarInt("bmarketadena", 0);
        long result = adenaConvert / tradeMult;
        if ((adenaAmount < adenaConvert) || (adenaConvert < tradeMult))
        {
          player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
          return;
        }
        if (result > limit - amountLimit)
        {
          player.sendMessage(new CustomMessage("common.LimitedAmount", player, new Object[0]).addNumber(500000L));
          return;
        }
        if (ItemFunctions.removeItem(player, 57, adenaConvert, true) != adenaConvert)
          break;
        SchedulingPattern reset = new SchedulingPattern("30 6 * * *");
        player.setVar("bmarketadena", player.getVarInt("bmarketadena") + result, reset.next(System.currentTimeMillis()));
        ItemFunctions.addItem(player, 5575, result, true);
        showChatWindow(player, 24, null, false);
        break;
      case 5:
      case 12:
      case 13:
      case 14:
      case 15:
      case 16:
      default:
        showChatWindow(player, val, null, false);
      }
    }
  }

  public void showChatWindow(Player player, int val, Object[] arg)
  {
    int npcId = getTemplate().npcId;

    String filename = "seven_signs/";

    int sealAvariceOwner = SevenSigns.getInstance().getSealOwner(1);
    int sealGnosisOwner = SevenSigns.getInstance().getSealOwner(2);
    int playerCabal = SevenSigns.getInstance().getPlayerCabal(player);
    boolean isSealValidationPeriod = SevenSigns.getInstance().isSealValidationPeriod();
    int compWinner = SevenSigns.getInstance().getCabalHighestScore();

    switch (npcId)
    {
    case 31078:
    case 31079:
    case 31080:
    case 31081:
    case 31082:
    case 31083:
    case 31084:
    case 31168:
    case 31692:
    case 31694:
    case 31997:
      switch (playerCabal)
      {
      case 2:
        if (isSealValidationPeriod) {
          if (compWinner == 2) {
            if (compWinner != sealGnosisOwner)
              filename = new StringBuilder().append(filename).append("dawn_priest_2c.htm").toString();
            else
              filename = new StringBuilder().append(filename).append("dawn_priest_2a.htm").toString();
          }
          else filename = new StringBuilder().append(filename).append("dawn_priest_2b.htm").toString(); 
        }
        else
          filename = new StringBuilder().append(filename).append("dawn_priest_1b.htm").toString();
        break;
      case 1:
        if (isSealValidationPeriod)
          filename = new StringBuilder().append(filename).append("dawn_priest_3b.htm").toString();
        else
          filename = new StringBuilder().append(filename).append("dawn_priest_3a.htm").toString();
        break;
      default:
        if (isSealValidationPeriod) {
          if (compWinner == 2)
            filename = new StringBuilder().append(filename).append("dawn_priest_4.htm").toString();
          else
            filename = new StringBuilder().append(filename).append("dawn_priest_2b.htm").toString();
        }
        else filename = new StringBuilder().append(filename).append("dawn_priest_1a.htm").toString(); 
      }
      break;
    case 31085:
    case 31086:
    case 31087:
    case 31088:
    case 31089:
    case 31090:
    case 31091:
    case 31169:
    case 31693:
    case 31695:
    case 31998:
      switch (playerCabal)
      {
      case 1:
        if (isSealValidationPeriod) {
          if (compWinner == 1) {
            if (compWinner != sealGnosisOwner)
              filename = new StringBuilder().append(filename).append("dusk_priest_2c.htm").toString();
            else
              filename = new StringBuilder().append(filename).append("dusk_priest_2a.htm").toString();
          }
          else filename = new StringBuilder().append(filename).append("dusk_priest_2b.htm").toString(); 
        }
        else
          filename = new StringBuilder().append(filename).append("dusk_priest_1b.htm").toString();
        break;
      case 2:
        if (isSealValidationPeriod)
          filename = new StringBuilder().append(filename).append("dusk_priest_3b.htm").toString();
        else
          filename = new StringBuilder().append(filename).append("dusk_priest_3a.htm").toString();
        break;
      default:
        if (isSealValidationPeriod) {
          if (compWinner == 1)
            filename = new StringBuilder().append(filename).append("dusk_priest_4.htm").toString();
          else
            filename = new StringBuilder().append(filename).append("dusk_priest_2b.htm").toString();
        }
        else filename = new StringBuilder().append(filename).append("dusk_priest_1a.htm").toString(); 
      }
      break;
    case 31092:
      filename = new StringBuilder().append(filename).append("blkmrkt_1.htm").toString();
      break;
    case 31113:
      if (!player.isGM()) {
        switch (compWinner)
        {
        case 2:
          if ((playerCabal == compWinner) && (playerCabal == sealAvariceOwner))
            break;
          filename = new StringBuilder().append(filename).append("mammmerch_2.htm").toString();
          return;
        case 1:
          if ((playerCabal == compWinner) && (playerCabal == sealAvariceOwner))
            break;
          filename = new StringBuilder().append(filename).append("mammmerch_2.htm").toString();
          return;
        }
      }

      filename = new StringBuilder().append(filename).append("mammmerch_1.htm").toString();
      break;
    case 31126:
      if (!player.isGM()) {
        switch (compWinner)
        {
        case 2:
          if ((playerCabal == compWinner) && (playerCabal == sealGnosisOwner))
            break;
          filename = new StringBuilder().append(filename).append("mammblack_2.htm").toString();
          return;
        case 1:
          if ((playerCabal == compWinner) && (playerCabal == sealGnosisOwner))
            break;
          filename = new StringBuilder().append(filename).append("mammblack_2.htm").toString();
          return;
        }
      }

      filename = new StringBuilder().append(filename).append("mammblack_1.htm").toString();
      break;
    default:
      filename = getHtmlPath(npcId, val, player);
    }

    player.sendPacket(new NpcHtmlMessage(player, this, filename, val));
  }
}
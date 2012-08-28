package net.sf.l2j.gameserver.model.actor.instance;

import java.util.StringTokenizer;
import java.util.logging.Logger;
import javolution.text.TextBuilder;
import javolution.util.FastTable;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.SevenSigns;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2SignsPriestInstance extends L2FolkInstance
{
  public L2SignsPriestInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    if (command.startsWith("SevenSignsDesc"))
    {
      int val = Integer.parseInt(command.substring(15));
      showChatWindow(player, val, null, true);
    }
    else
    {
      SystemMessage sm;
      if (command.startsWith("SevenSigns"))
      {
        int cabal = 0;
        int stoneType = 0;
        L2ItemInstance ancientAdena = player.getInventory().getItemByItemId(5575);

        int ancientAdenaAmount = ancientAdena == null ? 0 : ancientAdena.getCount();
        int val = Integer.parseInt(command.substring(11, 12).trim());

        if (command.length() > 12) {
          val = Integer.parseInt(command.substring(11, 13).trim());
        }
        if (command.length() > 13)
        {
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
              try
              {
                StringTokenizer st = new StringTokenizer(command.trim());
                st.nextToken();
                cabal = Integer.parseInt(st.nextToken());
              }
              catch (Exception e3)
              {
                _log.warning(new StringBuilder().append("Failed to retrieve cabal from bypass command. NpcId: ").append(getNpcId()).append("; Command: ").append(command).toString());
              }
            }
          }
        }
        InventoryUpdate iu;
        StatusUpdate su;
        switch (val)
        {
        case 2:
          if (!player.getInventory().validateCapacity(1))
          {
            player.sendPacket(Static.SLOTS_FULL);
          }
          else
          {
            L2ItemInstance adenaItem = player.getInventory().getAdenaInstance();
            if (!player.reduceAdena("SevenSigns", 500, this, true))
            {
              player.sendPacket(Static.YOU_NOT_ENOUGH_ADENA);
            }
            else {
              L2ItemInstance recordSevenSigns = player.getInventory().addItem("SevenSigns", 5707, 1, player, this);

              iu = new InventoryUpdate();
              iu.addNewItem(recordSevenSigns);
              iu.addItem(adenaItem);
              sendPacket(iu);

              su = new StatusUpdate(player.getObjectId());
              su.addAttribute(14, player.getCurrentLoad());
              sendPacket(su);
              player.sendPacket(SystemMessage.id(SystemMessageId.EARNED_ITEM).addItemName(5707));
            }
          }break;
        case 3:
        case 8:
        case 10:
          showChatWindow(player, val, SevenSigns.getCabalShortName(cabal), false);
          break;
        case 4:
          int newSeal = Integer.parseInt(command.substring(15));
          int oldCabal = SevenSigns.getInstance().getPlayerCabal(player);

          if (oldCabal != 0)
          {
            player.sendMessage(new StringBuilder().append("You are already a member of the ").append(SevenSigns.getCabalName(cabal)).append(".").toString());

            return;
          }

          if (player.getClassId().level() == 0)
          {
            player.sendMessage("You must have already completed your first class transfer.");
          }
          else {
            if (player.getClassId().level() >= 2)
            {
              if (Config.ALT_GAME_REQUIRE_CASTLE_DAWN)
              {
                if (getPlayerAllyHasCastle(player))
                {
                  if (cabal == 1)
                  {
                    player.sendMessage("You must not be a member of a castle-owning clan to join the Revolutionaries of Dusk.");
                    return;
                  }

                }
                else if (cabal == 2)
                {
                  boolean allowJoinDawn = false;

                  if (player.destroyItemByItemId("SevenSigns", 6388, 1, this, false))
                  {
                    allowJoinDawn = true;
                    player.sendPacket(SystemMessage.id(SystemMessageId.DISSAPEARED_ITEM).addNumber(1).addItemName(6388));
                  }
                  else if (player.reduceAdena("SevenSigns", 50000, this, false))
                  {
                    allowJoinDawn = true;
                    player.sendPacket(SystemMessage.id(SystemMessageId.DISSAPEARED_ADENA).addNumber(50000));
                  }

                  if (!allowJoinDawn)
                  {
                    player.sendMessage("You must be a member of a castle-owning clan, have a Certificate of Lord's Approval, or pay 50000 adena to join the Lords of Dawn.");
                    return;
                  }
                }
              }

            }

            SevenSigns.getInstance().setPlayerInfo(player, cabal, newSeal);

            if (cabal == 2) player.sendPacket(Static.SEVENSIGNS_PARTECIPATION_DAWN); else {
              player.sendPacket(Static.SEVENSIGNS_PARTECIPATION_DUSK);
            }

            switch (newSeal)
            {
            case 1:
              player.sendPacket(Static.FIGHT_FOR_AVARICE);
              break;
            case 2:
              player.sendPacket(Static.FIGHT_FOR_GNOSIS);
              break;
            case 3:
              player.sendPacket(Static.FIGHT_FOR_STRIFE);
            }

            showChatWindow(player, 4, SevenSigns.getCabalShortName(cabal), false);
          }break;
        case 6:
          stoneType = Integer.parseInt(command.substring(13));
          L2ItemInstance redStones = player.getInventory().getItemByItemId(6362);
          int redStoneCount = redStones == null ? 0 : redStones.getCount();
          L2ItemInstance greenStones = player.getInventory().getItemByItemId(6361);
          int greenStoneCount = greenStones == null ? 0 : greenStones.getCount();
          L2ItemInstance blueStones = player.getInventory().getItemByItemId(6360);
          int blueStoneCount = blueStones == null ? 0 : blueStones.getCount();
          int contribScore = SevenSigns.getInstance().getPlayerContribScore(player);
          boolean stonesFound = false;

          if (contribScore == Config.ALT_MAXIMUM_PLAYER_CONTRIB)
          {
            player.sendPacket(Static.CONTRIB_SCORE_EXCEEDED);
          }
          else
          {
            int redContribCount = 0;
            int greenContribCount = 0;
            int blueContribCount = 0;

            switch (stoneType)
            {
            case 1:
              blueContribCount = (Config.ALT_MAXIMUM_PLAYER_CONTRIB - contribScore) / 3;

              if (blueContribCount <= blueStoneCount) break;
              blueContribCount = blueStoneCount; break;
            case 2:
              greenContribCount = (Config.ALT_MAXIMUM_PLAYER_CONTRIB - contribScore) / 5;

              if (greenContribCount <= greenStoneCount) break;
              greenContribCount = greenStoneCount; break;
            case 3:
              redContribCount = (Config.ALT_MAXIMUM_PLAYER_CONTRIB - contribScore) / 10;

              if (redContribCount <= redStoneCount) break; redContribCount = redStoneCount; break;
            case 4:
              int tempContribScore = contribScore;
              redContribCount = (Config.ALT_MAXIMUM_PLAYER_CONTRIB - tempContribScore) / 10;

              if (redContribCount > redStoneCount) redContribCount = redStoneCount;
              tempContribScore += redContribCount * 10;
              greenContribCount = (Config.ALT_MAXIMUM_PLAYER_CONTRIB - tempContribScore) / 5;

              if (greenContribCount > greenStoneCount)
                greenContribCount = greenStoneCount;
              tempContribScore += greenContribCount * 5;
              blueContribCount = (Config.ALT_MAXIMUM_PLAYER_CONTRIB - tempContribScore) / 3;

              if (blueContribCount <= blueStoneCount) break;
              blueContribCount = blueStoneCount;
            }

            if (redContribCount > 0)
            {
              if (player.destroyItemByItemId("SevenSigns", 6362, redContribCount, this, false))
              {
                stonesFound = true;
              }
            }
            if (greenContribCount > 0)
            {
              if (player.destroyItemByItemId("SevenSigns", 6361, greenContribCount, this, false))
              {
                stonesFound = true;
              }
            }
            if (blueContribCount > 0)
            {
              if (player.destroyItemByItemId("SevenSigns", 6360, blueContribCount, this, false))
              {
                stonesFound = true;
              }
            }
            if (!stonesFound)
            {
              player.sendMessage("You do not have any seal stones of that type.");
            }
            else
            {
              contribScore = SevenSigns.getInstance().addPlayerStoneContrib(player, blueContribCount, greenContribCount, redContribCount);
              player.sendPacket(SystemMessage.id(SystemMessageId.CONTRIB_SCORE_INCREASED).addNumber(contribScore));
              showChatWindow(player, 6, null, false);
            }
          }
          break;
        case 7:
          int ancientAdenaConvert = 0;
          try
          {
            ancientAdenaConvert = Integer.parseInt(command.substring(13).trim());
          }
          catch (NumberFormatException e)
          {
            player.sendMessage("You must enter an integer amount.");
            break;
          }
          catch (StringIndexOutOfBoundsException e)
          {
            player.sendMessage("You must enter an amount.");
            break;
          }

          if ((ancientAdenaAmount < ancientAdenaConvert) || (ancientAdenaConvert < 1))
          {
            player.sendPacket(Static.YOU_NOT_ENOUGH_ADENA);
          }
          else
          {
            player.reduceAncientAdena("SevenSigns", ancientAdenaConvert, this, true);
            player.addAdena("SevenSigns", ancientAdenaConvert, this, true);

            iu = new InventoryUpdate();
            iu.addModifiedItem(player.getInventory().getAncientAdenaInstance());
            iu.addModifiedItem(player.getInventory().getAdenaInstance());
            player.sendPacket(iu);
          }break;
        case 9:
          int playerCabal = SevenSigns.getInstance().getPlayerCabal(player);
          int winningCabal = SevenSigns.getInstance().getCabalHighestScore();

          if ((!SevenSigns.getInstance().isSealValidationPeriod()) || (playerCabal != winningCabal))
            break;
          int ancientAdenaReward = SevenSigns.getInstance().getAncientAdenaReward(player, true);

          if (ancientAdenaReward < 3)
          {
            showChatWindow(player, 9, "b", false);
          }
          else
          {
            player.addAncientAdena("SevenSigns", ancientAdenaReward, this, true);

            iu = new InventoryUpdate();
            iu.addModifiedItem(player.getInventory().getAncientAdenaInstance());
            sendPacket(iu);

            su = new StatusUpdate(player.getObjectId());
            su.addAttribute(14, player.getCurrentLoad());
            sendPacket(su);

            showChatWindow(player, 9, "a", false);
          }break;
        case 11:
          try
          {
            String portInfo = command.substring(14).trim();

            StringTokenizer st = new StringTokenizer(portInfo);
            int x = Integer.parseInt(st.nextToken());
            int y = Integer.parseInt(st.nextToken());
            int z = Integer.parseInt(st.nextToken());
            int ancientAdenaCost = Integer.parseInt(st.nextToken());

            if ((ancientAdenaCost > 0) && 
              (!player.reduceAncientAdena("SevenSigns", ancientAdenaCost, this, true)))
            {
              break;
            }
            player.teleToLocation(x, y, z, true);
          }
          catch (Exception e)
          {
            _log.warning(new StringBuilder().append("SevenSigns: Error occurred while teleporting player: ").append(e).toString());
          }

        case 17:
          stoneType = Integer.parseInt(command.substring(14));
          int stoneId = 0;
          int stoneCount = 0;
          int stoneValue = 0;
          String stoneColor = null;

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

          L2ItemInstance stoneInstance = player.getInventory().getItemByItemId(stoneId);

          if (stoneInstance != null) stoneCount = stoneInstance.getCount();

          String path = "data/html/seven_signs/signs_17.htm";
          String content = HtmCache.getInstance().getHtm(path);

          if (content != null)
          {
            content = content.replaceAll("%stoneColor%", stoneColor);
            content = content.replaceAll("%stoneValue%", String.valueOf(stoneValue));
            content = content.replaceAll("%stoneCount%", String.valueOf(stoneCount));
            content = content.replaceAll("%stoneItemId%", String.valueOf(stoneId));
            content = content.replaceAll("%objectId%", String.valueOf(getObjectId()));

            NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());
            html.setHtml(content);
            player.sendPacket(html);
          }
          else
          {
            _log.warning(new StringBuilder().append("Problem with HTML text data/html/seven_signs/signs_17.htm: ").append(path).toString());
          }

          break;
        case 18:
          int convertStoneId = Integer.parseInt(command.substring(14, 18));
          int convertCount = 0;
          try
          {
            convertCount = Integer.parseInt(command.substring(19).trim());
          }
          catch (Exception NumberFormatException)
          {
            player.sendMessage("You must enter an integer amount.");
            break;
          }

          L2ItemInstance convertItem = player.getInventory().getItemByItemId(convertStoneId);

          if (convertItem == null)
          {
            player.sendMessage("You do not have any seal stones of that type.");
          }
          else
          {
            int totalCount = convertItem.getCount();
            int ancientAdenaReward = 0;

            if ((convertCount <= totalCount) && (convertCount > 0))
            {
              switch (convertStoneId)
              {
              case 6360:
                ancientAdenaReward = SevenSigns.calcAncientAdenaReward(convertCount, 0, 0);

                break;
              case 6361:
                ancientAdenaReward = SevenSigns.calcAncientAdenaReward(0, convertCount, 0);

                break;
              case 6362:
                ancientAdenaReward = SevenSigns.calcAncientAdenaReward(0, 0, convertCount);
              }

              if (!player.destroyItemByItemId("SevenSigns", convertStoneId, convertCount, this, true)) {
                break;
              }
              player.addAncientAdena("SevenSigns", ancientAdenaReward, this, true);

              iu = new InventoryUpdate();
              iu.addModifiedItem(player.getInventory().getAncientAdenaInstance());
              iu.addModifiedItem(convertItem);
              sendPacket(iu);

              su = new StatusUpdate(player.getObjectId());
              su.addAttribute(14, player.getCurrentLoad());
              sendPacket(su);
            }
            else
            {
              player.sendMessage("You do not have that many seal stones.");
            }
          }
          break;
        case 19:
          int chosenSeal = Integer.parseInt(command.substring(16));
          String fileSuffix = new StringBuilder().append(SevenSigns.getSealName(chosenSeal, true)).append("_").append(SevenSigns.getCabalShortName(cabal)).toString();

          showChatWindow(player, val, fileSuffix, false);
          break;
        case 20:
          TextBuilder contentBuffer = new TextBuilder("<html><body><font color=\"LEVEL\">[ Seal Status ]</font><br>");

          for (int i = 1; i < 4; i++)
          {
            int sealOwner = SevenSigns.getInstance().getSealOwner(i);

            if (sealOwner != 0) contentBuffer.append(new StringBuilder().append("[").append(SevenSigns.getSealName(i, false)).append(": ").append(SevenSigns.getCabalName(sealOwner)).append("]<br>").toString());
            else
            {
              contentBuffer.append(new StringBuilder().append("[").append(SevenSigns.getSealName(i, false)).append(": Nothingness]<br>").toString());
            }
          }

          contentBuffer.append(new StringBuilder().append("<a action=\"bypass -h npc_").append(getObjectId()).append("_SevenSigns 3 ").append(cabal).append("\">Go back.</a></body></html>").toString());

          NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());
          html.setHtml(contentBuffer.toString());
          player.sendPacket(html);
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

        sm = null;
      }
      else {
        super.onBypassFeedback(player, command);
      }
    }
  }

  private final boolean getPlayerAllyHasCastle(L2PcInstance player) {
    L2Clan playerClan = player.getClan();

    if (playerClan == null) return false;
    int allyId;
    if (!Config.ALT_GAME_REQUIRE_CLAN_CASTLE)
    {
      allyId = playerClan.getAllyId();

      if (allyId != 0)
      {
        FastTable cn = new FastTable();
        cn.addAll(ClanTable.getInstance().getClans());
        for (L2Clan clan : cn) {
          if ((clan.getAllyId() == allyId) && (clan.getHasCastle() > 0))
            return true;
        }
      }
    }
    return playerClan.getHasCastle() > 0;
  }

  private void showChatWindow(L2PcInstance player, int val, String suffix, boolean isDescription)
  {
    String filename = "data/html/seven_signs/";

    filename = new StringBuilder().append(filename).append(isDescription ? new StringBuilder().append("desc_").append(val).toString() : new StringBuilder().append("signs_").append(val).toString()).toString();
    filename = new StringBuilder().append(filename).append(suffix != null ? new StringBuilder().append("_").append(suffix).append(".htm").toString() : ".htm").toString();

    showChatWindow(player, filename);
  }
}
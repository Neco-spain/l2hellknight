package l2m.gameserver.utils;

import java.io.File;
import javolution.text.TextBuilder;
import l2m.gameserver.aConfig;
import l2m.gameserver.data.xml.holder.ResidenceHolder;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Zone.ZoneType;
import l2m.gameserver.model.base.TeamType;
import l2m.gameserver.model.entity.Reflection;
import l2m.gameserver.model.entity.events.impl.SiegeEvent;
import l2m.gameserver.model.entity.residence.Castle;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BBSUtil
{
  private static final Logger _log = LoggerFactory.getLogger(BBSUtil.class);

  public static String BuildMenu()
  {
    TextBuilder menu = new TextBuilder();
    try
    {
      String[] lines = FileUtils.readFileToString(new File("config/community_menu.txt"), "UTF-8").split("\n");
      for (String line : lines)
      {
        if (((line.contains("_bbspage:30-2")) && (!aConfig.get("CommunityShopEnable", false))) || ((!aConfig.get("CommunityBufferEnable", false)) && (line.contains("_bbsbuff"))) || ((!aConfig.get("CommunityTeleportEnable", false)) && (line.contains("_bbsteleport"))) || ((!aConfig.get("CommunityClassEnable", false)) && (line.contains("_bbsclassmaster"))))
        {
          continue;
        }

        menu.append(line);
      }
    }
    catch (Exception e)
    {
      _log.error("Error while loading config/community_menu.txt!");
    }

    return menu.toString();
  }

  public static boolean checkCondition(Player player, boolean isEnable, boolean isLastHero) {
    if (player == null)
    {
      return false;
    }

    if ((player.isInOlympiadMode()) || (!player.isRegisterOlympiad()))
    {
      player.sendMessage("\u0412\u043E \u0432\u0440\u0435\u043C\u044F \u041E\u043B\u0438\u043C\u043F\u0438\u0430\u0434\u044B \u043D\u0435\u043B\u044C\u0437\u044F \u0438\u0441\u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u044C \u0434\u0430\u043D\u043D\u0443\u044E \u0444\u0443\u043D\u043A\u0446\u0438\u044E.");
      return false;
    }

    if ((player.getReflection().getId() != 0) && (!aConfig.get("CommunityInstancesEnable", false)))
    {
      player.sendMessage("\u0414\u0430\u043D\u043D\u0430\u044F \u0444\u0443\u043D\u043A\u0446\u0438\u044F \u0434\u043E\u0441\u0442\u0443\u043F\u043D\u0430 \u0442\u043E\u043B\u044C\u043A\u043E \u0432 \u043E\u0431\u044B\u0447\u043D\u043E\u043C \u043C\u0438\u0440\u0435.");
      return false;
    }

    if (!isEnable)
    {
      player.sendMessage("\u0414\u0430\u043D\u043D\u0430\u044F \u0444\u0443\u043D\u043A\u0446\u0438\u044F \u043E\u0442\u043A\u043B\u044E\u0447\u0435\u043D\u0430.");
      return false;
    }

    if ((!aConfig.get("CommunityEventsEnable", false)) && ((player.getTeam() != TeamType.NONE) || (isLastHero)))
    {
      player.sendMessage("\u041D\u0435\u043B\u044C\u0437\u044F \u0438\u0441\u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u044C \u0434\u0430\u043D\u043D\u0443\u044E \u0444\u0443\u043D\u043A\u0446\u0438\u044E \u0432\u043E \u0432\u0440\u0435\u043C\u044F \u044D\u0432\u0435\u043D\u0442\u043E\u0432.");
      return false;
    }

    Castle castle = (Castle)ResidenceHolder.getInstance().getResidenceByObject(Castle.class, player);
    if ((!aConfig.get("CommunitySiegeEnable", false)) && ((player.isInZone(Zone.ZoneType.SIEGE)) || ((castle != null) && (castle.getSiegeEvent().isInProgress()))))
    {
      player.sendMessage("\u041D\u0435\u043B\u044C\u0437\u044F \u0438\u0441\u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u044C \u0434\u0430\u043D\u043D\u0443\u044E \u0444\u0443\u043D\u043A\u0446\u0438\u044E \u0432\u043E \u0432\u0440\u0435\u043C\u044F \u043E\u0441\u0430\u0434.");
      return false;
    }

    if ((!aConfig.get("AllowAbnormalState", false)) && ((player.isDead()) || (player.isAlikeDead()) || (player.isCastingNow()) || (player.isInCombat()) || (player.isAttackingNow()) || (player.isInOlympiadMode()) || (player.isInBoat()) || (player.isFlying()) || (player.isInFlyingTransform())))
    {
      player.sendMessage("\u0414\u0430\u043D\u043D\u0443\u044E \u0444\u0443\u043D\u043A\u0446\u0438\u044E \u043D\u0435\u0432\u043E\u0437\u043C\u043E\u0436\u043D\u043E \u0438\u0441\u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u044C \u0432 \u0434\u0430\u043D\u043D\u044B\u0445 \u0443\u0441\u043B\u043E\u0432\u0438\u044F\u0445.");
      return false;
    }

    return true;
  }
}
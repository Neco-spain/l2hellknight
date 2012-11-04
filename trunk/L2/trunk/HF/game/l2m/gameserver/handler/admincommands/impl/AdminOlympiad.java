package l2m.gameserver.handler.admincommands.impl;

import java.util.ArrayList;
import java.util.List;
import l2m.gameserver.Announcements;
import l2m.gameserver.Config;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.handler.admincommands.IAdminCommandHandler;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.World;
import l2m.gameserver.model.entity.Hero;
import l2m.gameserver.model.entity.olympiad.Olympiad;
import l2m.gameserver.model.entity.olympiad.OlympiadDatabase;
import l2m.gameserver.model.entity.olympiad.OlympiadManager;
import l2m.gameserver.templates.StatsSet;

public class AdminOlympiad
  implements IAdminCommandHandler
{
  public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    switch (1.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminOlympiad$Commands[command.ordinal()])
    {
    case 1:
      if (!Config.ENABLE_OLYMPIAD) {
        return false;
      }
      try
      {
        OlympiadDatabase.save();
      }
      catch (Exception e)
      {
      }

      activeChar.sendMessage("olympaid data saved.");
      break;
    case 2:
      if (wordList.length < 3)
      {
        activeChar.sendMessage("Command syntax: //add_oly_points <char_name> <point_to_add>");
        activeChar.sendMessage("This command can be applied only for online players.");
        return false;
      }

      Player player = World.getPlayer(wordList[1]);
      if (player == null)
      {
        activeChar.sendMessage("Character " + wordList[1] + " not found in game.");
        return false;
      }

      int pointToAdd;
      try
      {
        pointToAdd = Integer.parseInt(wordList[2]);
      }
      catch (NumberFormatException e)
      {
        activeChar.sendMessage("Please specify integer value for olympiad points.");
        return false;
      }

      int curPoints = Olympiad.getNoblePoints(player.getObjectId());
      Olympiad.manualSetNoblePoints(player.getObjectId(), curPoints + pointToAdd);
      int newPoints = Olympiad.getNoblePoints(player.getObjectId());

      activeChar.sendMessage("Added " + pointToAdd + " points to character " + player.getName());
      activeChar.sendMessage("Old points: " + curPoints + ", new points: " + newPoints);
      break;
    case 3:
      Olympiad._manager = new OlympiadManager();
      Olympiad._inCompPeriod = true;

      new Thread(Olympiad._manager).start();

      Announcements.getInstance().announceToAll(Msg.THE_OLYMPIAD_GAME_HAS_STARTED);
      break;
    case 4:
      Olympiad._inCompPeriod = false;
      Announcements.getInstance().announceToAll(Msg.THE_OLYMPIAD_GAME_HAS_ENDED);
      try
      {
        OlympiadDatabase.save();
      }
      catch (Exception e)
      {
      }

    case 5:
      if (wordList.length < 2)
      {
        activeChar.sendMessage("Command syntax: //add_hero <char_name>");
        activeChar.sendMessage("This command can be applied only for online players.");
        return false;
      }

      Player player = World.getPlayer(wordList[1]);
      if (player == null)
      {
        activeChar.sendMessage("Character " + wordList[1] + " not found in game.");
        return false;
      }

      StatsSet hero = new StatsSet();
      hero.set("class_id", player.getBaseClassId());
      hero.set("char_id", player.getObjectId());
      hero.set("char_name", player.getName());

      List heroesToBe = new ArrayList();
      heroesToBe.add(hero);

      Hero.getInstance().computeNewHeroes(heroesToBe);

      activeChar.sendMessage("Hero status added to player " + player.getName());
      break;
    }

    return true;
  }

  public Enum[] getAdminCommandEnum()
  {
    return Commands.values();
  }

  private static enum Commands
  {
    admin_oly_save, 
    admin_add_oly_points, 
    admin_oly_start, 
    admin_add_hero, 
    admin_oly_stop;
  }
}
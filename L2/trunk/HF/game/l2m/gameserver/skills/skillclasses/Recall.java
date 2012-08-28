package l2m.gameserver.skills.skillclasses;

import java.util.List;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.Zone.ZoneType;
import l2m.gameserver.model.base.PlayerAccess;
import l2m.gameserver.model.base.TeamType;
import l2m.gameserver.model.entity.Reflection;
import l2m.gameserver.model.pledge.Clan;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.network.serverpackets.components.CustomMessage;
import l2m.gameserver.network.serverpackets.components.SystemMsg;
import l2m.gameserver.templates.StatsSet;
import l2m.gameserver.utils.Location;

public class Recall extends Skill
{
  private final int _townId;
  private final boolean _clanhall;
  private final boolean _castle;
  private final boolean _fortress;
  private final Location _loc;

  public Recall(StatsSet set)
  {
    super(set);
    _townId = set.getInteger("townId", 0);
    _clanhall = set.getBool("clanhall", false);
    _castle = set.getBool("castle", false);
    _fortress = set.getBool("fortress", false);
    String[] cords = set.getString("loc", "").split(";");
    if (cords.length == 3)
      _loc = new Location(Integer.parseInt(cords[0]), Integer.parseInt(cords[1]), Integer.parseInt(cords[2]));
    else
      _loc = null;
  }

  public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
  {
    if (getHitTime() == 200)
    {
      Player player = activeChar.getPlayer();
      if (_clanhall)
      {
        if ((player.getClan() == null) || (player.getClan().getHasHideout() == 0))
        {
          activeChar.sendPacket(new SystemMessage(113).addItemName(_itemConsumeId[0]));
          return false;
        }
      }
      else if (_castle)
      {
        if ((player.getClan() == null) || (player.getClan().getCastle() == 0))
        {
          activeChar.sendPacket(new SystemMessage(113).addItemName(_itemConsumeId[0]));
          return false;
        }
      }
      else if ((_fortress) && (
        (player.getClan() == null) || (player.getClan().getHasFortress() == 0)))
      {
        activeChar.sendPacket(new SystemMessage(113).addItemName(_itemConsumeId[0]));
        return false;
      }
    }

    if (activeChar.isPlayer())
    {
      Player p = (Player)activeChar;
      if (p.getActiveWeaponFlagAttachment() != null)
      {
        activeChar.sendPacket(SystemMsg.YOU_CANNOT_TELEPORT_WHILE_IN_POSSESSION_OF_A_WARD);
        return false;
      }
      if ((p.isInDuel()) || (p.getTeam() != TeamType.NONE))
      {
        activeChar.sendMessage(new CustomMessage("common.RecallInDuel", p, new Object[0]));
        return false;
      }
      if (p.isInOlympiadMode())
      {
        activeChar.sendPacket(Msg.THIS_SKILL_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
        return false;
      }
    }

    if ((activeChar.isInZone(Zone.ZoneType.no_escape)) || ((_townId > 0) && (activeChar.getReflection() != null) && (activeChar.getReflection().getCoreLoc() != null)))
    {
      if (activeChar.isPlayer())
        activeChar.sendMessage(new CustomMessage("l2p.gameserver.skills.skillclasses.Recall.Here", (Player)activeChar, new Object[0]));
      return false;
    }

    return super.checkCondition(activeChar, target, forceUse, dontMove, first);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    for (Creature target : targets) {
      if (target != null)
      {
        Player pcTarget = target.getPlayer();
        if ((pcTarget == null) || 
          (!pcTarget.getPlayerAccess().UseTeleport))
          continue;
        if (pcTarget.getActiveWeaponFlagAttachment() != null)
        {
          activeChar.sendPacket(Msg.YOU_CANNOT_TELEPORT_WHILE_IN_POSSESSION_OF_A_WARD);
          continue;
        }
        if (pcTarget.isFestivalParticipant())
        {
          activeChar.sendMessage(new CustomMessage("l2p.gameserver.skills.skillclasses.Recall.Festival", (Player)activeChar, new Object[0]));
          continue;
        }
        if (pcTarget.isInOlympiadMode())
        {
          activeChar.sendPacket(Msg.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_CURRENTLY_PARTICIPATING_IN_THE_GRAND_OLYMPIAD);
          return;
        }
        if (pcTarget.isInObserverMode())
        {
          activeChar.sendPacket(new SystemMessage(113).addSkillName(getId(), getLevel()));
          return;
        }
        if ((pcTarget.isInDuel()) || (pcTarget.getTeam() != TeamType.NONE))
        {
          activeChar.sendMessage(new CustomMessage("common.RecallInDuel", (Player)activeChar, new Object[0]));
          return;
        }
        if (_isItemHandler)
        {
          if (_itemConsumeId[0] == 7125)
          {
            pcTarget.teleToLocation(17144, 170156, -3502, 0);
            return;
          }
          if (_itemConsumeId[0] == 7127)
          {
            pcTarget.teleToLocation(105918, 109759, -3207, 0);
            return;
          }
          if (_itemConsumeId[0] == 7130)
          {
            pcTarget.teleToLocation(85475, 16087, -3672, 0);
            return;
          }
          if (_itemConsumeId[0] == 9716)
          {
            pcTarget.teleToLocation(-120000, 44500, 352, 0);
            return;
          }
          if (_itemConsumeId[0] == 7618)
          {
            pcTarget.teleToLocation(149864, -81062, -5618, 0);
            return;
          }
          if (_itemConsumeId[0] == 7619)
          {
            pcTarget.teleToLocation(108275, -53785, -2524, 0);
            return;
          }
        }
        if (_loc != null)
        {
          pcTarget.teleToLocation(_loc);
          return;
        }

        switch (_townId)
        {
        case 1:
          pcTarget.teleToLocation(-83990, 243336, -3700, 0);
          return;
        case 2:
          pcTarget.teleToLocation(45576, 49412, -2950, 0);
          return;
        case 3:
          pcTarget.teleToLocation(12501, 16768, -4500, 0);
          return;
        case 4:
          pcTarget.teleToLocation(-44884, -115063, -80, 0);
          return;
        case 5:
          pcTarget.teleToLocation(115790, -179146, -890, 0);
          return;
        case 6:
          pcTarget.teleToLocation(-14279, 124446, -3000, 0);
          return;
        case 7:
          pcTarget.teleToLocation(-82909, 150357, -3000, 0);
          return;
        case 8:
          pcTarget.teleToLocation(19025, 145245, -3107, 0);
          return;
        case 9:
          pcTarget.teleToLocation(82272, 147801, -3350, 0);
          return;
        case 10:
          pcTarget.teleToLocation(82323, 55466, -1480, 0);
          return;
        case 11:
          pcTarget.teleToLocation(144526, 24661, -2100, 0);
          return;
        case 12:
          pcTarget.teleToLocation(117189, 78952, -2210, 0);
          return;
        case 13:
          pcTarget.teleToLocation(110768, 219824, -3624, 0);
          return;
        case 14:
          pcTarget.teleToLocation(43536, -50416, -800, 0);
          return;
        case 15:
          pcTarget.teleToLocation(148288, -58304, -2979, 0);
          return;
        case 16:
          pcTarget.teleToLocation(87776, -140384, -1536, 0);
          return;
        case 17:
          pcTarget.teleToLocation(-117081, 44171, 507, 0);
          return;
        case 18:
          pcTarget.teleToLocation(10568, -24600, -3648, 0);
          return;
        case 19:
          pcTarget.teleToLocation(19025, 145245, -3107, 0);
          return;
        case 20:
          pcTarget.teleToLocation(-16434, 208803, -3664, 0);
          return;
        case 21:
          pcTarget.teleToLocation(-184200, 243080, 1568, 0);
          return;
        case 22:
          pcTarget.teleToLocation(8976, 252416, -1928, 0);
          return;
        }
        if (_castle)
        {
          pcTarget.teleToCastle();
          return;
        }
        if (_clanhall)
        {
          pcTarget.teleToClanhall();
          return;
        }
        if (_fortress)
        {
          pcTarget.teleToFortress();
          return;
        }

        pcTarget.teleToClosestTown();
      }
    }
    if (isSSPossible())
      activeChar.unChargeShots(isMagic());
  }
}
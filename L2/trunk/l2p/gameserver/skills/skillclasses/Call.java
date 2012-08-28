package l2p.gameserver.skills.skillclasses;

import java.util.List;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.instancemanager.ReflectionManager;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Party;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.Zone.ZoneType;
import l2p.gameserver.model.base.PlayerAccess;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.templates.StatsSet;
import l2p.gameserver.utils.Location;

public class Call extends Skill
{
  final boolean _party;

  public Call(StatsSet set)
  {
    super(set);
    _party = set.getBool("party", false);
  }

  public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
  {
    if (activeChar.isPlayer())
    {
      if ((_party) && (((Player)activeChar).getParty() == null)) {
        return false;
      }
      SystemMessage msg = canSummonHere((Player)activeChar);
      if (msg != null)
      {
        activeChar.sendPacket(msg);
        return false;
      }

      if (!_party)
      {
        if (activeChar == target) {
          return false;
        }
        msg = canBeSummoned(target);
        if (msg != null)
        {
          activeChar.sendPacket(msg);
          return false;
        }
      }
    }

    return super.checkCondition(activeChar, target, forceUse, dontMove, first);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    if (!activeChar.isPlayer()) {
      return;
    }
    SystemMessage msg = canSummonHere((Player)activeChar);
    if (msg != null)
    {
      activeChar.sendPacket(msg);
      return;
    }

    if (_party)
    {
      if (((Player)activeChar).getParty() != null) {
        for (Player target : ((Player)activeChar).getParty().getPartyMembers())
          if ((!target.equals(activeChar)) && (canBeSummoned(target) == null) && (!target.isTerritoryFlagEquipped()))
          {
            target.stopMove();
            target.teleToLocation(Location.findPointToStay(activeChar, 100, 150), activeChar.getGeoIndex());
            getEffects(activeChar, target, getActivateRate() > 0, false);
          }
      }
      if (isSSPossible())
        activeChar.unChargeShots(isMagic());
      return;
    }

    for (Creature target : targets) {
      if (target != null)
      {
        if (canBeSummoned(target) != null) {
          continue;
        }
        ((Player)target).summonCharacterRequest(activeChar, Location.findAroundPosition(activeChar, 100, 150), (getId() == 1403) || (getId() == 1404) ? 1 : 0);

        getEffects(activeChar, target, getActivateRate() > 0, false);
      }
    }
    if (isSSPossible())
      activeChar.unChargeShots(isMagic());
  }

  public static SystemMessage canSummonHere(Player activeChar)
  {
    if ((activeChar.isAlikeDead()) || (activeChar.isInOlympiadMode()) || (activeChar.isInObserverMode()) || (activeChar.isFlying()) || (activeChar.isFestivalParticipant())) {
      return Msg.NOTHING_HAPPENED;
    }

    if ((activeChar.isInZoneBattle()) || (activeChar.isInZone(Zone.ZoneType.SIEGE)) || (activeChar.isInZone(Zone.ZoneType.no_restart)) || (activeChar.isInZone(Zone.ZoneType.no_summon)) || (activeChar.isInBoat()) || (activeChar.getReflection() != ReflectionManager.DEFAULT)) {
      return Msg.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION;
    }

    if ((activeChar.isInStoreMode()) || (activeChar.isProcessingRequest())) {
      return Msg.YOU_CANNOT_SUMMON_DURING_A_TRADE_OR_WHILE_USING_THE_PRIVATE_SHOPS;
    }
    return null;
  }

  public static SystemMessage canBeSummoned(Creature target)
  {
    if ((target == null) || (!target.isPlayer()) || (target.getPlayer().isTerritoryFlagEquipped()) || (target.isFlying()) || (target.isInObserverMode()) || (target.getPlayer().isFestivalParticipant()) || (!target.getPlayer().getPlayerAccess().UseTeleport)) {
      return Msg.INVALID_TARGET;
    }
    if (target.isInOlympiadMode()) {
      return Msg.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_CURRENTLY_PARTICIPATING_IN_THE_GRAND_OLYMPIAD;
    }
    if ((target.isInZoneBattle()) || (target.isInZone(Zone.ZoneType.SIEGE)) || (target.isInZone(Zone.ZoneType.no_restart)) || (target.isInZone(Zone.ZoneType.no_summon)) || (target.getReflection() != ReflectionManager.DEFAULT) || (target.isInBoat())) {
      return Msg.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING;
    }

    if (target.isAlikeDead()) {
      return new SystemMessage(1844).addString(target.getName());
    }

    if ((target.getPvpFlag() != 0) || (target.isInCombat())) {
      return new SystemMessage(1843).addString(target.getName());
    }
    Player pTarget = (Player)target;

    if ((pTarget.getPrivateStoreType() != 0) || (pTarget.isProcessingRequest())) {
      return new SystemMessage(1898).addString(target.getName());
    }
    return null;
  }
}
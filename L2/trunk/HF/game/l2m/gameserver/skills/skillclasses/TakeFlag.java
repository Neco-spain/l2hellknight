package l2m.gameserver.skills.skillclasses;

import java.util.List;
import l2m.gameserver.data.xml.holder.EventHolder;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.entity.events.EventType;
import l2m.gameserver.model.entity.events.impl.DominionSiegeEvent;
import l2m.gameserver.model.entity.events.impl.DominionSiegeRunnerEvent;
import l2m.gameserver.model.entity.events.objects.TerritoryWardObject;
import l2m.gameserver.model.entity.residence.Dominion;
import l2m.gameserver.model.instances.residences.SiegeFlagInstance;
import l2m.gameserver.model.pledge.Clan;
import l2m.gameserver.network.serverpackets.SystemMessage2;
import l2m.gameserver.network.serverpackets.components.SystemMsg;
import l2m.gameserver.templates.StatsSet;

public class TakeFlag extends Skill
{
  public TakeFlag(StatsSet set)
  {
    super(set);
  }

  public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
  {
    if (!super.checkCondition(activeChar, target, forceUse, dontMove, first)) {
      return false;
    }
    if ((activeChar == null) || (!activeChar.isPlayer())) {
      return false;
    }
    Player player = (Player)activeChar;

    if (player.getClan() == null) {
      return false;
    }
    DominionSiegeEvent siegeEvent1 = (DominionSiegeEvent)player.getEvent(DominionSiegeEvent.class);
    if (siegeEvent1 == null) {
      return false;
    }
    if (!(player.getActiveWeaponFlagAttachment() instanceof TerritoryWardObject))
    {
      activeChar.sendPacket(new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
      return false;
    }

    if (player.isMounted())
    {
      activeChar.sendPacket(new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
      return false;
    }

    if ((!(target instanceof SiegeFlagInstance)) || (target.getNpcId() != 36590) || (target.getClan() != player.getClan()))
    {
      activeChar.sendPacket(new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
      return false;
    }

    DominionSiegeEvent siegeEvent2 = (DominionSiegeEvent)target.getEvent(DominionSiegeEvent.class);
    if ((siegeEvent2 == null) || (siegeEvent1 != siegeEvent2))
    {
      activeChar.sendPacket(new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
      return false;
    }

    return true;
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    for (Creature target : targets)
      if (target != null)
      {
        Player player = (Player)activeChar;
        DominionSiegeEvent siegeEvent1 = (DominionSiegeEvent)player.getEvent(DominionSiegeEvent.class);
        if ((siegeEvent1 == null) || 
          (!(target instanceof SiegeFlagInstance)) || (target.getNpcId() != 36590) || (target.getClan() != player.getClan()) || 
          (!(player.getActiveWeaponFlagAttachment() instanceof TerritoryWardObject)))
          continue;
        DominionSiegeEvent siegeEvent2 = (DominionSiegeEvent)target.getEvent(DominionSiegeEvent.class);
        if ((siegeEvent2 == null) || (siegeEvent1 != siegeEvent2))
        {
          continue;
        }
        Dominion dominion = (Dominion)siegeEvent1.getResidence();

        TerritoryWardObject wardObject = (TerritoryWardObject)player.getActiveWeaponFlagAttachment();

        DominionSiegeEvent siegeEvent3 = wardObject.getEvent();
        Dominion dominion3 = (Dominion)siegeEvent3.getResidence();

        int wardDominionId = wardObject.getDominionId();

        wardObject.despawnObject(siegeEvent3);

        dominion3.removeFlag(wardDominionId);

        dominion.addFlag(wardDominionId);

        siegeEvent1.spawnAction("ward_" + wardDominionId, true);

        DominionSiegeRunnerEvent runnerEvent = (DominionSiegeRunnerEvent)EventHolder.getInstance().getEvent(EventType.MAIN_EVENT, 1);
        runnerEvent.broadcastTo(((SystemMessage2)new SystemMessage2(SystemMsg.CLAN_S1_HAS_SUCCEEDED_IN_CAPTURING_S2S_TERRITORY_WARD).addString(dominion.getOwner().getName())).addResidenceName(wardDominionId));
      }
  }
}
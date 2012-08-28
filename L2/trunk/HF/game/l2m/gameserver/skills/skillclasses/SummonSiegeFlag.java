package l2m.gameserver.skills.skillclasses;

import java.util.List;
import l2m.gameserver.data.xml.holder.NpcHolder;
import l2m.gameserver.geodata.GeoEngine;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.Zone.ZoneType;
import l2m.gameserver.model.entity.events.impl.DominionSiegeEvent;
import l2m.gameserver.model.entity.events.impl.SiegeEvent;
import l2m.gameserver.model.entity.events.objects.SiegeClanObject;
import l2m.gameserver.model.entity.events.objects.ZoneObject;
import l2m.gameserver.model.instances.residences.SiegeFlagInstance;
import l2m.gameserver.model.pledge.Clan;
import l2m.gameserver.network.serverpackets.SystemMessage2;
import l2m.gameserver.network.serverpackets.components.IStaticPacket;
import l2m.gameserver.network.serverpackets.components.SystemMsg;
import l2m.gameserver.skills.Stats;
import l2m.gameserver.skills.funcs.FuncMul;
import l2m.gameserver.templates.StatsSet;
import l2m.gameserver.templates.npc.NpcTemplate;

public class SummonSiegeFlag extends Skill
{
  private final FlagType _flagType;
  private final double _advancedMult;

  public SummonSiegeFlag(StatsSet set)
  {
    super(set);
    _flagType = ((FlagType)set.getEnum("flagType", FlagType.class));
    _advancedMult = set.getDouble("advancedMultiplier", 1.0D);
  }

  public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
  {
    if (!activeChar.isPlayer())
      return false;
    if (!super.checkCondition(activeChar, target, forceUse, dontMove, first)) {
      return false;
    }
    Player player = (Player)activeChar;
    if ((player.getClan() == null) || (!player.isClanLeader())) {
      return false;
    }

    switch (1.$SwitchMap$l2p$gameserver$skills$skillclasses$SummonSiegeFlag$FlagType[_flagType.ordinal()])
    {
    case 1:
      break;
    case 2:
    case 3:
    case 4:
      if (player.isInZone(Zone.ZoneType.RESIDENCE))
      {
        player.sendPacket(new IStaticPacket[] { SystemMsg.YOU_CANNOT_SET_UP_A_BASE_HERE, new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this) });
        return false;
      }

      SiegeEvent siegeEvent = (SiegeEvent)activeChar.getEvent(SiegeEvent.class);
      if (siegeEvent == null)
      {
        player.sendPacket(new IStaticPacket[] { SystemMsg.YOU_CANNOT_SET_UP_A_BASE_HERE, new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this) });
        return false;
      }

      boolean inZone = false;
      List zones = siegeEvent.getObjects("flag_zones");
      for (ZoneObject zone : zones)
      {
        if (player.isInZone(zone.getZone())) {
          inZone = true;
        }
      }
      if (!inZone)
      {
        player.sendPacket(new IStaticPacket[] { SystemMsg.YOU_CANNOT_SET_UP_A_BASE_HERE, new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this) });
        return false;
      }

      SiegeClanObject siegeClan = siegeEvent.getSiegeClan(siegeEvent.getClass() == DominionSiegeEvent.class ? "defenders" : "attackers", player.getClan());
      if (siegeClan == null)
      {
        player.sendPacket(new IStaticPacket[] { SystemMsg.YOU_CANNOT_SUMMON_THE_ENCAMPMENT_BECAUSE_YOU_ARE_NOT_A_MEMBER_OF_THE_SIEGE_CLAN_INVOLVED_IN_THE_CASTLE__FORTRESS__HIDEOUT_SIEGE, new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this) });
        return false;
      }

      if (siegeClan.getFlag() == null)
        break;
      player.sendPacket(new IStaticPacket[] { SystemMsg.AN_OUTPOST_OR_HEADQUARTERS_CANNOT_BE_BUILT_BECAUSE_ONE_ALREADY_EXISTS, new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this) });
      return false;
    }

    return true;
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    Player player = (Player)activeChar;

    Clan clan = player.getClan();
    if ((clan == null) || (!player.isClanLeader())) {
      return;
    }
    SiegeEvent siegeEvent = (SiegeEvent)activeChar.getEvent(SiegeEvent.class);
    if (siegeEvent == null) {
      return;
    }
    SiegeClanObject siegeClan = siegeEvent.getSiegeClan(siegeEvent.getClass() == DominionSiegeEvent.class ? "defenders" : "attackers", clan);
    if (siegeClan == null) {
      return;
    }
    switch (1.$SwitchMap$l2p$gameserver$skills$skillclasses$SummonSiegeFlag$FlagType[_flagType.ordinal()])
    {
    case 1:
      siegeClan.deleteFlag();
      break;
    default:
      if (siegeClan.getFlag() != null) {
        return;
      }

      SiegeFlagInstance flag = (SiegeFlagInstance)NpcHolder.getInstance().getTemplate(_flagType == FlagType.OUTPOST ? 36590 : 35062).getNewInstance();
      flag.setClan(siegeClan);
      flag.addEvent(siegeEvent);

      if (_flagType == FlagType.ADVANCED) {
        flag.addStatFunc(new FuncMul(Stats.MAX_HP, 80, flag, _advancedMult));
      }
      flag.setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp(), true);
      flag.setHeading(player.getHeading());

      int x = (int)(player.getX() + 100.0D * Math.cos(player.headingToRadians(player.getHeading() - 32768)));
      int y = (int)(player.getY() + 100.0D * Math.sin(player.headingToRadians(player.getHeading() - 32768)));
      flag.spawnMe(GeoEngine.moveCheck(player.getX(), player.getY(), player.getZ(), x, y, player.getGeoIndex()));

      siegeClan.setFlag(flag);
    }
  }

  public static enum FlagType
  {
    DESTROY, 
    NORMAL, 
    ADVANCED, 
    OUTPOST;
  }
}
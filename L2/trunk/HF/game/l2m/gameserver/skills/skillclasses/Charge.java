package l2m.gameserver.skills.skillclasses;

import java.util.List;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.Skill.SkillTargetType;
import l2m.gameserver.network.serverpackets.MagicSkillUse;
import l2m.gameserver.skills.Formulas;
import l2m.gameserver.skills.Formulas.AttackInfo;
import l2m.gameserver.templates.StatsSet;

public class Charge extends Skill
{
  public static final int MAX_CHARGE = 8;
  private int _charges;
  private boolean _fullCharge;

  public Charge(StatsSet set)
  {
    super(set);
    _charges = set.getInteger("charges", getLevel());
    _fullCharge = set.getBool("fullCharge", false);
  }

  public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
  {
    if (!activeChar.isPlayer()) {
      return false;
    }
    Player player = (Player)activeChar;

    if ((getPower() <= 0.0D) && (getId() != 2165) && (player.getIncreasedForce() >= _charges))
    {
      activeChar.sendPacket(Msg.YOUR_FORCE_HAS_REACHED_MAXIMUM_CAPACITY_);
      return false;
    }
    if (getId() == 2165) {
      player.sendPacket(new MagicSkillUse(player, player, 2165, 1, 0, 0L));
    }
    return super.checkCondition(activeChar, target, forceUse, dontMove, first);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    if (!activeChar.isPlayer()) {
      return;
    }
    boolean ss = (activeChar.getChargedSoulShot()) && (isSSPossible());
    if ((ss) && (getTargetType() != Skill.SkillTargetType.TARGET_SELF)) {
      activeChar.unChargeShots(false);
    }

    for (Creature target : targets)
    {
      if ((target.isDead()) || (target == activeChar)) {
        continue;
      }
      boolean reflected = target.checkReflectSkill(activeChar, this);
      Creature realTarget = reflected ? activeChar : target;

      if (getPower() > 0.0D)
      {
        Formulas.AttackInfo info = Formulas.calcPhysDam(activeChar, realTarget, this, false, false, ss, false);

        if (info.lethal_dmg > 0.0D) {
          realTarget.reduceCurrentHp(info.lethal_dmg, activeChar, this, true, true, false, false, false, false, false);
        }
        realTarget.reduceCurrentHp(info.damage, activeChar, this, true, true, false, true, false, false, true);
        if (!reflected) {
          realTarget.doCounterAttack(this, activeChar, false);
        }
      }
      getEffects(activeChar, target, getActivateRate() > 0, false, reflected);
    }

    chargePlayer((Player)activeChar, Integer.valueOf(getId()));
  }

  public void chargePlayer(Player player, Integer skillId)
  {
    if (player.getIncreasedForce() >= _charges)
    {
      player.sendPacket(Msg.YOUR_FORCE_HAS_REACHED_MAXIMUM_CAPACITY_);
      return;
    }
    if (_fullCharge)
      player.setIncreasedForce(_charges);
    else
      player.setIncreasedForce(player.getIncreasedForce() + 1);
  }
}
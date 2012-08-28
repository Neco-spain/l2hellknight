package l2m.gameserver.skills.skillclasses;

import java.util.List;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.instances.residences.SiegeFlagInstance;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.network.serverpackets.components.CustomMessage;
import l2m.gameserver.skills.Stats;
import l2m.gameserver.templates.StatsSet;

public class Heal extends Skill
{
  private final boolean _ignoreHpEff;
  private final boolean _staticPower;

  public Heal(StatsSet set)
  {
    super(set);
    _ignoreHpEff = set.getBool("ignoreHpEff", false);
    _staticPower = set.getBool("staticPower", isHandler());
  }

  public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
  {
    if ((target == null) || (target.isDoor()) || ((target instanceof SiegeFlagInstance))) {
      return false;
    }
    return super.checkCondition(activeChar, target, forceUse, dontMove, first);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    double hp = _power;
    if (!_staticPower) {
      hp += 0.1D * _power * Math.sqrt(activeChar.getMAtk(null, this) / 333);
    }
    int sps = (isSSPossible()) && (getHpConsume() == 0) ? activeChar.getChargedSpiritShot() : 0;

    if (sps == 2)
      hp *= 1.5D;
    else if (sps == 1) {
      hp *= 1.3D;
    }
    if ((activeChar.getSkillMastery(Integer.valueOf(getId())) == 3) && (!_staticPower))
    {
      activeChar.removeSkillMastery(Integer.valueOf(getId()));
      hp *= 3.0D;
    }

    for (Creature target : targets) {
      if (target != null)
      {
        if ((target.isHealBlocked()) || (
          (target != activeChar) && (
          ((target.isPlayer()) && (target.isCursedWeaponEquipped())) || (
          (activeChar.isPlayer()) && (activeChar.isCursedWeaponEquipped()))))) {
          continue;
        }
        double addToHp = 0.0D;
        if (_staticPower) {
          addToHp = _power;
        }
        else {
          addToHp = hp * (!_ignoreHpEff ? target.calcStat(Stats.HEAL_EFFECTIVNESS, 100.0D, activeChar, this) : 100.0D) / 100.0D;
          addToHp = activeChar.calcStat(Stats.HEAL_POWER, addToHp, target, this);
        }

        addToHp = Math.max(0.0D, Math.min(addToHp, target.calcStat(Stats.HP_LIMIT, null, null) * target.getMaxHp() / 100.0D - target.getCurrentHp()));

        if (addToHp > 0.0D)
          target.setCurrentHp(addToHp + target.getCurrentHp(), false);
        if (getId() == 4051) {
          target.sendPacket(Msg.REJUVENATING_HP);
        } else if (target.isPlayer()) {
          if (activeChar == target) {
            activeChar.sendPacket(new SystemMessage(1066).addNumber(Math.round(addToHp)));
          }
          else
            target.sendPacket(new SystemMessage(1067).addString(activeChar.getName()).addNumber(Math.round(addToHp)));
        } else if ((target.isSummon()) || (target.isPet()))
        {
          Player owner = target.getPlayer();
          if (owner != null)
            if (activeChar == target)
              owner.sendMessage(new CustomMessage("YOU_HAVE_RESTORED_S1_HP_OF_YOUR_PET", owner, new Object[0]).addNumber(Math.round(addToHp)));
            else if (owner == activeChar) {
              owner.sendMessage(new CustomMessage("YOU_HAVE_RESTORED_S1_HP_OF_YOUR_PET", owner, new Object[0]).addNumber(Math.round(addToHp)));
            }
            else
              owner.sendMessage(new CustomMessage("S1_HAS_BEEN_RESTORED_S2_HP_OF_YOUR_PET", owner, new Object[0]).addString(activeChar.getName()).addNumber(Math.round(addToHp)));
        }
        getEffects(activeChar, target, getActivateRate() > 0, false);
      }
    }
    if (isSSPossible())
      activeChar.unChargeShots(isMagic());
  }
}
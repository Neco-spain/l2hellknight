package l2m.gameserver.skills.skillclasses;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import l2p.commons.util.Rnd;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.model.EffectList;
import l2m.gameserver.model.Skill;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.skills.EffectType;
import l2m.gameserver.skills.Formulas;
import l2m.gameserver.templates.StatsSet;

public class NegateEffects extends Skill
{
  private Map<EffectType, Integer> _negateEffects = new HashMap();
  private Map<String, Integer> _negateStackType = new HashMap();
  private final boolean _onlyPhysical;
  private final boolean _negateDebuffs;

  public NegateEffects(StatsSet set)
  {
    super(set);

    String[] negateEffectsString = set.getString("negateEffects", "").split(";");
    for (int i = 0; i < negateEffectsString.length; i++) {
      if (negateEffectsString[i].isEmpty())
        continue;
      String[] entry = negateEffectsString[i].split(":");
      _negateEffects.put(Enum.valueOf(EffectType.class, entry[0]), Integer.valueOf(entry.length > 1 ? Integer.decode(entry[1]).intValue() : 2147483647));
    }

    String[] negateStackTypeString = set.getString("negateStackType", "").split(";");
    for (int i = 0; i < negateStackTypeString.length; i++) {
      if (negateStackTypeString[i].isEmpty())
        continue;
      String[] entry = negateStackTypeString[i].split(":");
      _negateStackType.put(entry[0], Integer.valueOf(entry.length > 1 ? Integer.decode(entry[1]).intValue() : 2147483647));
    }

    _onlyPhysical = set.getBool("onlyPhysical", false);
    _negateDebuffs = set.getBool("negateDebuffs", true);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    for (Creature target : targets) {
      if (target != null)
      {
        if ((!_negateDebuffs) && (!Formulas.calcSkillSuccess(activeChar, target, this, getActivateRate())))
        {
          activeChar.sendPacket(new SystemMessage(139).addString(target.getName()).addSkillName(getId(), getLevel()));
          continue;
        }

        if (!_negateEffects.isEmpty()) {
          for (Map.Entry e : _negateEffects.entrySet())
            negateEffectAtPower(target, (EffectType)e.getKey(), ((Integer)e.getValue()).intValue());
        }
        if (!_negateStackType.isEmpty()) {
          for (Map.Entry e : _negateStackType.entrySet())
            negateEffectAtPower(target, (String)e.getKey(), ((Integer)e.getValue()).intValue());
        }
        getEffects(activeChar, target, getActivateRate() > 0, false);
      }
    }
    if (isSSPossible())
      activeChar.unChargeShots(isMagic());
  }

  private void negateEffectAtPower(Creature target, EffectType type, int power)
  {
    for (Effect e : target.getEffectList().getAllEffects())
    {
      Skill skill = e.getSkill();
      if (((_onlyPhysical) && (skill.isMagic())) || (!skill.isCancelable()) || ((skill.isOffensive()) && (!_negateDebuffs)) || (
        (!skill.isOffensive()) && (skill.getMagicLevel() > getMagicLevel()) && (Rnd.chance(skill.getMagicLevel() - getMagicLevel()))))
        continue;
      if ((e.getEffectType() == type) && (e.getStackOrder() <= power))
        e.exit();
    }
  }

  private void negateEffectAtPower(Creature target, String stackType, int power)
  {
    for (Effect e : target.getEffectList().getAllEffects())
    {
      Skill skill = e.getSkill();
      if (((_onlyPhysical) && (skill.isMagic())) || (!skill.isCancelable()) || ((skill.isOffensive()) && (!_negateDebuffs)) || (
        (!skill.isOffensive()) && (skill.getMagicLevel() > getMagicLevel()) && (Rnd.chance(skill.getMagicLevel() - getMagicLevel()))))
        continue;
      if ((e.checkStackType(stackType)) && (e.getStackOrder() <= power))
        e.exit();
    }
  }
}
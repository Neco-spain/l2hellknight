package net.sf.l2j.gameserver.templates;

import java.util.List;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.Quest.QuestEventType;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.conditions.ConditionGameChance;
import net.sf.l2j.gameserver.skills.funcs.Func;
import net.sf.l2j.gameserver.skills.funcs.FuncTemplate;
import scripts.skills.ISkillHandler;
import scripts.skills.SkillHandler;

public final class L2Weapon extends L2Item
{
  private final int _soulShotCount;
  private final int _spiritShotCount;
  private final int _pDam;
  private final int _rndDam;
  private final int _critical;
  private final double _hitModifier;
  private final int _avoidModifier;
  private final int _shieldDef;
  private final double _shieldDefRate;
  private final int _atkSpeed;
  private final int _atkReuse;
  private final int _mpConsume;
  private final int _mDam;
  private L2Skill _itemSkill = null;
  private L2Skill _enchant4Skill = null;
  protected L2Skill[] _skillsOnCast;
  protected L2Skill[] _skillsOnCrit;

  public L2Weapon(L2WeaponType type, StatsSet set)
  {
    super(type, set);
    _soulShotCount = set.getInteger("soulshots");
    _spiritShotCount = set.getInteger("spiritshots");
    _pDam = set.getInteger("p_dam");
    _rndDam = set.getInteger("rnd_dam");
    _critical = set.getInteger("critical");
    _hitModifier = set.getDouble("hit_modify");
    _avoidModifier = set.getInteger("avoid_modify");
    _shieldDef = set.getInteger("shield_def");
    _shieldDefRate = set.getDouble("shield_def_rate");
    _atkSpeed = set.getInteger("atk_speed");
    _atkReuse = set.getInteger("atk_reuse", type == L2WeaponType.BOW ? 1500 : 0);
    _mpConsume = set.getInteger("mp_consume");
    _mDam = set.getInteger("m_dam");

    int sId = set.getInteger("item_skill_id");
    int sLv = set.getInteger("item_skill_lvl");
    if ((sId > 0) && (sLv > 0)) {
      _itemSkill = SkillTable.getInstance().getInfo(sId, sLv);
    }
    sId = set.getInteger("enchant4_skill_id");
    sLv = set.getInteger("enchant4_skill_lvl");
    if ((sId > 0) && (sLv > 0)) {
      _enchant4Skill = SkillTable.getInstance().getInfo(sId, sLv);
    }
    sId = set.getInteger("onCast_skill_id");
    sLv = set.getInteger("onCast_skill_lvl");
    int sCh = set.getInteger("onCast_skill_chance");
    if ((sId > 0) && (sLv > 0) && (sCh > 0))
    {
      L2Skill skill = SkillTable.getInstance().getInfo(sId, sLv);
      skill.attach(new ConditionGameChance(sCh), true);
      attachOnCast(skill);
    }

    sId = set.getInteger("onCrit_skill_id");
    sLv = set.getInteger("onCrit_skill_lvl");
    sCh = set.getInteger("onCrit_skill_chance");
    if ((sId > 0) && (sLv > 0) && (sCh > 0))
    {
      L2Skill skill = SkillTable.getInstance().getInfo(sId, sLv);
      skill.attach(new ConditionGameChance(sCh), true);
      attachOnCrit(skill);
    }
  }

  public L2WeaponType getItemType()
  {
    return (L2WeaponType)_type;
  }

  public int getItemMask()
  {
    return getItemType().mask();
  }

  public int getSoulShotCount()
  {
    return _soulShotCount;
  }

  public int getSpiritShotCount()
  {
    return _spiritShotCount;
  }

  public int getPDamage()
  {
    return _pDam;
  }

  public int getRandomDamage()
  {
    return _rndDam;
  }

  public int getAttackSpeed()
  {
    return _atkSpeed;
  }

  public int getAttackReuseDelay()
  {
    return _atkReuse;
  }

  public int getAvoidModifier()
  {
    return _avoidModifier;
  }

  public int getCritical()
  {
    return _critical;
  }

  public double getHitModifier()
  {
    return _hitModifier;
  }

  public int getMDamage()
  {
    return _mDam;
  }

  public int getMpConsume()
  {
    return _mpConsume;
  }

  public int getShieldDef()
  {
    return _shieldDef;
  }

  public double getShieldDefRate()
  {
    return _shieldDefRate;
  }

  public L2Skill getSkill()
  {
    return _itemSkill;
  }

  public L2Skill getEnchant4Skill()
  {
    return _enchant4Skill;
  }

  public Func[] getStatFuncs(L2ItemInstance instance, L2Character player)
  {
    List funcs = new FastList();
    if (_funcTemplates != null)
    {
      for (FuncTemplate t : _funcTemplates) {
        Env env = new Env();
        env.cha = player;
        env.item = instance;
        Func f = t.getFunc(env, instance);
        if (f != null)
          funcs.add(f);
      }
    }
    return (Func[])funcs.toArray(new Func[funcs.size()]);
  }

  public L2Effect[] getSkillEffects(L2Character caster, L2Character target, boolean crit)
  {
    if ((_skillsOnCrit == null) || (!crit))
      return _emptyEffectSet;
    List effects = new FastList();

    for (L2Skill skill : _skillsOnCrit)
    {
      if ((target.isRaid()) && ((skill.getSkillType() == L2Skill.SkillType.CONFUSION) || (skill.getSkillType() == L2Skill.SkillType.MUTE) || (skill.getSkillType() == L2Skill.SkillType.PARALYZE) || (skill.getSkillType() == L2Skill.SkillType.ROOT))) {
        continue;
      }
      if (!skill.checkCondition(caster, target, true)) {
        continue;
      }
      if (target.getFirstEffect(skill.getId()) != null)
        target.getFirstEffect(skill.getId()).exit();
      for (L2Effect e : skill.getEffects(caster, target))
        effects.add(e);
    }
    if (effects.size() == 0)
      return _emptyEffectSet;
    return (L2Effect[])effects.toArray(new L2Effect[effects.size()]);
  }

  public L2Effect[] getSkillEffects(L2Character caster, L2Character target, L2Skill trigger)
  {
    if (_skillsOnCast == null)
      return _emptyEffectSet;
    List effects = new FastList();

    for (L2Skill skill : _skillsOnCast)
    {
      if (trigger.isOffensive() != skill.isOffensive()) {
        continue;
      }
      if ((trigger.getId() >= 1320) && (trigger.getId() <= 1322)) {
        continue;
      }
      if ((target.isRaid()) && ((skill.getSkillType() == L2Skill.SkillType.CONFUSION) || (skill.getSkillType() == L2Skill.SkillType.MUTE) || (skill.getSkillType() == L2Skill.SkillType.PARALYZE) || (skill.getSkillType() == L2Skill.SkillType.ROOT))) {
        continue;
      }
      if ((trigger.isToggle()) && (skill.getSkillType() == L2Skill.SkillType.BUFF)) {
        continue;
      }
      if (!skill.checkCondition(caster, target, true))
      {
        continue;
      }
      try
      {
        ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());

        FastList targets = new FastList();
        targets.add(target);

        if (handler != null)
          handler.useSkill(caster, skill, targets);
        else {
          skill.useSkill(caster, targets);
        }
        if ((caster.isPlayer()) && (target.isL2Npc()))
        {
          Quest[] quests = ((L2NpcInstance)target).getTemplate().getEventQuests(Quest.QuestEventType.MOB_TARGETED_BY_SKILL);
          if (quests != null)
            for (Quest quest : quests)
              quest.notifySkillUse((L2NpcInstance)target, caster.getPlayer(), skill);
        }
      }
      catch (Exception e)
      {
      }
    }
    if (effects.size() == 0)
      return _emptyEffectSet;
    return (L2Effect[])effects.toArray(new L2Effect[effects.size()]);
  }

  public void attachOnCrit(L2Skill skill)
  {
    if (_skillsOnCrit == null)
    {
      _skillsOnCrit = new L2Skill[] { skill };
    }
    else
    {
      int len = _skillsOnCrit.length;
      L2Skill[] tmp = new L2Skill[len + 1];

      System.arraycopy(_skillsOnCrit, 0, tmp, 0, len);
      tmp[len] = skill;
      _skillsOnCrit = tmp;
    }
  }

  public void attachOnCast(L2Skill skill)
  {
    if (_skillsOnCast == null)
    {
      _skillsOnCast = new L2Skill[] { skill };
    }
    else
    {
      int len = _skillsOnCast.length;
      L2Skill[] tmp = new L2Skill[len + 1];

      System.arraycopy(_skillsOnCast, 0, tmp, 0, len);
      tmp[len] = skill;
      _skillsOnCast = tmp;
    }
  }

  public int maxOlyEnch()
  {
    return Config.OLY_MAX_WEAPON_ENCH;
  }
}
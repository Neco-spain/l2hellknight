package net.sf.l2j.gameserver.model.actor.instance;

import java.util.concurrent.Future;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.status.PcStatus;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.Rnd;

public final class L2BabyPetInstance extends L2PetInstance
{
  protected L2Skill _weakHeal;
  protected L2Skill _strongHeal;
  private Future _healingTask;

  public L2BabyPetInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, L2ItemInstance control)
  {
    super(objectId, template, owner, control);

    FastMap skills = (FastMap)getTemplate().getSkills();
    L2Skill skill1 = null;
    L2Skill skill2 = null;

    for (L2Skill skill : skills.values())
    {
      if ((skill.isActive()) && (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_OWNER_PET) && ((skill.getSkillType() == L2Skill.SkillType.HEAL) || (skill.getSkillType() == L2Skill.SkillType.HOT) || (skill.getSkillType() == L2Skill.SkillType.BALANCE_LIFE) || (skill.getSkillType() == L2Skill.SkillType.HEAL_PERCENT) || (skill.getSkillType() == L2Skill.SkillType.HEAL_STATIC) || (skill.getSkillType() == L2Skill.SkillType.COMBATPOINTHEAL) || (skill.getSkillType() == L2Skill.SkillType.CPHOT) || (skill.getSkillType() == L2Skill.SkillType.MANAHEAL) || (skill.getSkillType() == L2Skill.SkillType.MANA_BY_LEVEL) || (skill.getSkillType() == L2Skill.SkillType.MANAHEAL_PERCENT) || (skill.getSkillType() == L2Skill.SkillType.MANARECHARGE) || (skill.getSkillType() == L2Skill.SkillType.MPHOT)))
      {
        if (skill1 == null) {
          skill1 = skill;
        }
        else {
          skill2 = skill;
          break;
        }
      }
    }

    if (skill1 != null)
    {
      if (skill2 == null)
      {
        _weakHeal = skill1;
        _strongHeal = skill1;
      }
      else if (skill1.getPower() > skill2.getPower())
      {
        _weakHeal = skill2;
        _strongHeal = skill1;
      }
      else
      {
        _weakHeal = skill1;
        _strongHeal = skill2;
      }

      _healingTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new Heal(this), 0L, 1000L);
    }
  }

  public boolean doDie(L2Character killer)
  {
    if (!super.doDie(killer)) {
      return false;
    }
    if (_healingTask != null)
    {
      _healingTask.cancel(false);
      _healingTask = null;
    }
    return true;
  }

  public synchronized void unSummon(L2PcInstance owner)
  {
    super.unSummon(owner);

    if (_healingTask != null)
    {
      _healingTask.cancel(false);
      _healingTask = null;
    }
  }

  public void doRevive(boolean broadcastPacketRevive)
  {
    super.doRevive(true);
    if (_healingTask == null)
      _healingTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new Heal(this), 0L, 1000L);
  }

  private class Heal implements Runnable {
    private L2BabyPetInstance _baby;

    public Heal(L2BabyPetInstance baby) {
      _baby = baby;
    }

    public void run()
    {
      L2PcInstance owner = _baby.getOwner();

      if ((!owner.isDead()) && (!_baby.isCastingNow()) && (!_baby.isBetrayed()))
      {
        boolean previousFollowStatus = _baby.getFollowStatus();

        if ((owner.getStatus().getCurrentHp() / owner.getMaxHp() < 0.2D) && (Rnd.get(4) < 3))
          _baby.useMagic(_strongHeal, false, false);
        else if ((owner.getStatus().getCurrentHp() / owner.getMaxHp() < 0.8D) && (Rnd.get(4) < 1)) {
          _baby.useMagic(_weakHeal, false, false);
        }

        if (previousFollowStatus != _baby.getFollowStatus())
          setFollowStatus(previousFollowStatus);
      }
    }
  }
}
package l2p.gameserver.model.instances;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;
import l2p.commons.threading.RunnableImpl;
import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.ai.SummonAI;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.model.EffectList;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.skills.effects.EffectTemplate;
import l2p.gameserver.tables.PetDataTable;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.templates.npc.NpcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PetBabyInstance extends PetInstance
{
  private static final Logger _log = LoggerFactory.getLogger(PetBabyInstance.class);
  private Future<?> _actionTask;
  private boolean _buffEnabled = true;
  private static final int HealTrick = 4717;
  private static final int GreaterHealTrick = 4718;
  private static final int GreaterHeal = 5195;
  private static final int BattleHeal = 5590;
  private static final int Recharge = 5200;
  private static final int Pet_Haste = 5186;
  private static final int Pet_Vampiric_Rage = 5187;
  private static final int Pet_Regeneration = 5188;
  private static final int Pet_Blessed_Body = 5189;
  private static final int Pet_Blessed_Soul = 5190;
  private static final int Pet_Guidance = 5191;
  private static final int Pet_Wind_Walk = 5192;
  private static final int Pet_Acumen = 5193;
  private static final int Pet_Empower = 5194;
  private static final int Pet_Concentration = 5201;
  private static final int Pet_Might = 5586;
  private static final int Pet_Shield = 5587;
  private static final int Pet_Focus = 5588;
  private static final int Pet_Death_Wisper = 5589;
  private static final int WindShackle = 5196;
  private static final int Hex = 5197;
  private static final int Slow = 5198;
  private static final int CurseGloom = 5199;
  private static final Skill[][] COUGAR_BUFFS = { { SkillTable.getInstance().getInfo(5194, 3), SkillTable.getInstance().getInfo(5586, 3) }, { SkillTable.getInstance().getInfo(5194, 3), SkillTable.getInstance().getInfo(5586, 3), SkillTable.getInstance().getInfo(5587, 3), SkillTable.getInstance().getInfo(5189, 6) }, { SkillTable.getInstance().getInfo(5194, 3), SkillTable.getInstance().getInfo(5586, 3), SkillTable.getInstance().getInfo(5587, 3), SkillTable.getInstance().getInfo(5189, 6), SkillTable.getInstance().getInfo(5193, 3), SkillTable.getInstance().getInfo(5186, 2) }, { SkillTable.getInstance().getInfo(5194, 3), SkillTable.getInstance().getInfo(5586, 3), SkillTable.getInstance().getInfo(5587, 3), SkillTable.getInstance().getInfo(5189, 6), SkillTable.getInstance().getInfo(5193, 3), SkillTable.getInstance().getInfo(5186, 2), SkillTable.getInstance().getInfo(5187, 4), SkillTable.getInstance().getInfo(5588, 3) } };

  private static final Skill[][] BUFFALO_BUFFS = { { SkillTable.getInstance().getInfo(5586, 3), SkillTable.getInstance().getInfo(5189, 6) }, { SkillTable.getInstance().getInfo(5586, 3), SkillTable.getInstance().getInfo(5189, 6), SkillTable.getInstance().getInfo(5587, 3), SkillTable.getInstance().getInfo(5191, 3) }, { SkillTable.getInstance().getInfo(5586, 3), SkillTable.getInstance().getInfo(5189, 6), SkillTable.getInstance().getInfo(5587, 3), SkillTable.getInstance().getInfo(5191, 3), SkillTable.getInstance().getInfo(5187, 4), SkillTable.getInstance().getInfo(5186, 2) }, { SkillTable.getInstance().getInfo(5586, 3), SkillTable.getInstance().getInfo(5189, 6), SkillTable.getInstance().getInfo(5587, 3), SkillTable.getInstance().getInfo(5191, 3), SkillTable.getInstance().getInfo(5187, 4), SkillTable.getInstance().getInfo(5186, 2), SkillTable.getInstance().getInfo(5588, 3), SkillTable.getInstance().getInfo(5589, 3) } };

  private static final Skill[][] KOOKABURRA_BUFFS = { { SkillTable.getInstance().getInfo(5194, 3), SkillTable.getInstance().getInfo(5190, 6) }, { SkillTable.getInstance().getInfo(5194, 3), SkillTable.getInstance().getInfo(5190, 6), SkillTable.getInstance().getInfo(5189, 6), SkillTable.getInstance().getInfo(5587, 3) }, { SkillTable.getInstance().getInfo(5194, 3), SkillTable.getInstance().getInfo(5190, 6), SkillTable.getInstance().getInfo(5189, 6), SkillTable.getInstance().getInfo(5587, 3), SkillTable.getInstance().getInfo(5193, 3), SkillTable.getInstance().getInfo(5201, 6) }, { SkillTable.getInstance().getInfo(5194, 3), SkillTable.getInstance().getInfo(5190, 6), SkillTable.getInstance().getInfo(5189, 6), SkillTable.getInstance().getInfo(5587, 3), SkillTable.getInstance().getInfo(5193, 3), SkillTable.getInstance().getInfo(5201, 6) } };

  private static final Skill[][] FAIRY_PRINCESS_BUFFS = { { SkillTable.getInstance().getInfo(5194, 3), SkillTable.getInstance().getInfo(5190, 6) }, { SkillTable.getInstance().getInfo(5194, 3), SkillTable.getInstance().getInfo(5190, 6), SkillTable.getInstance().getInfo(5189, 6), SkillTable.getInstance().getInfo(5587, 3) }, { SkillTable.getInstance().getInfo(5194, 3), SkillTable.getInstance().getInfo(5190, 6), SkillTable.getInstance().getInfo(5189, 6), SkillTable.getInstance().getInfo(5587, 3), SkillTable.getInstance().getInfo(5193, 3), SkillTable.getInstance().getInfo(5201, 6) }, { SkillTable.getInstance().getInfo(5194, 3), SkillTable.getInstance().getInfo(5190, 6), SkillTable.getInstance().getInfo(5189, 6), SkillTable.getInstance().getInfo(5587, 3), SkillTable.getInstance().getInfo(5193, 3), SkillTable.getInstance().getInfo(5201, 6) } };

  public PetBabyInstance(int objectId, NpcTemplate template, Player owner, ItemInstance control, int _currentLevel, long exp)
  {
    super(objectId, template, owner, control, _currentLevel, exp);
  }

  public PetBabyInstance(int objectId, NpcTemplate template, Player owner, ItemInstance control)
  {
    super(objectId, template, owner, control);
  }

  public Skill[] getBuffs()
  {
    switch (getNpcId())
    {
    case 16036:
      return COUGAR_BUFFS[getBuffLevel()];
    case 16034:
      return BUFFALO_BUFFS[getBuffLevel()];
    case 16035:
      return KOOKABURRA_BUFFS[getBuffLevel()];
    case 16046:
      return FAIRY_PRINCESS_BUFFS[getBuffLevel()];
    }
    return Skill.EMPTY_ARRAY;
  }

  public Skill onActionTask()
  {
    try
    {
      Player owner = getPlayer();
      if ((!owner.isDead()) && (!owner.isInvul()) && (!isCastingNow()))
      {
        if (getEffectList().getEffectsCountForSkill(5753) > 0) {
          return null;
        }
        if (getEffectList().getEffectsCountForSkill(5771) > 0) {
          return null;
        }
        boolean improved = PetDataTable.isImprovedBabyPet(getNpcId());
        Skill skill = null;

        if ((!Config.ALT_PET_HEAL_BATTLE_ONLY) || (owner.isInCombat()))
        {
          double curHp = owner.getCurrentHpPercents();
          if ((curHp < 90.0D) && (Rnd.chance((100.0D - curHp) / 3.0D))) {
            if (curHp < 33.0D)
              skill = SkillTable.getInstance().getInfo(improved ? 5590 : 4718, getHealLevel());
            else if (getNpcId() != 16035) {
              skill = SkillTable.getInstance().getInfo(improved ? 5195 : 4717, getHealLevel());
            }
          }
          if ((skill == null) && (getNpcId() == 16035))
          {
            double curMp = owner.getCurrentMpPercents();
            if ((curMp < 66.0D) && (Rnd.chance((100.0D - curMp) / 2.0D))) {
              skill = SkillTable.getInstance().getInfo(5200, getRechargeLevel());
            }
          }
          if (skill != null) if (skill.checkCondition(this, owner, false, !isFollowMode(), true))
            {
              setTarget(owner);
              getAI().Cast(skill, owner, false, !isFollowMode());
              return skill;
            }
        }

        if ((!improved) || (owner.isInOfflineMode()) || (owner.getEffectList().getEffectsCountForSkill(5771) > 0)) {
          return null;
        }
        for (Skill buff : getBuffs())
        {
          if (getCurrentMp() < buff.getMpConsume2()) {
            continue;
          }
          Iterator i$ = owner.getEffectList().getAllEffects().iterator();
          while (true) if (i$.hasNext()) { Effect ef = (Effect)i$.next();
              if (checkEffect(ef, buff)) break;
              continue;
            } else {
              if (buff.checkCondition(this, owner, false, !isFollowMode(), true))
              {
                setTarget(owner);
                getAI().Cast(buff, owner, false, !isFollowMode());
                return buff;
              }
              return null;
            } 
        }
      }
    }
    catch (Throwable e)
    {
      _log.warn("Pet [#" + getNpcId() + "] a buff task error has occurred: " + e);
      _log.error("", e);
    }
    return null;
  }

  private boolean checkEffect(Effect ef, Skill skill)
  {
    if ((ef == null) || (!ef.isInUse()) || (!EffectList.checkStackType(ef.getTemplate(), skill.getEffectTemplates()[0])))
      return false;
    if (ef.getStackOrder() < skill.getEffectTemplates()[0]._stackOrder)
      return false;
    if (ef.getTimeLeft() > 10)
      return true;
    if (ef.getNext() != null)
      return checkEffect(ef.getNext(), skill);
    return false;
  }

  public synchronized void stopBuffTask()
  {
    if (_actionTask != null)
    {
      _actionTask.cancel(false);
      _actionTask = null;
    }
  }

  public synchronized void startBuffTask()
  {
    if (_actionTask != null) {
      stopBuffTask();
    }
    if ((_actionTask == null) && (!isDead()))
      _actionTask = ThreadPoolManager.getInstance().schedule(new ActionTask(), 5000L);
  }

  public boolean isBuffEnabled()
  {
    return _buffEnabled;
  }

  public void triggerBuff()
  {
    _buffEnabled = (!_buffEnabled);
  }

  protected void onDeath(Creature killer)
  {
    stopBuffTask();
    super.onDeath(killer);
  }

  public void doRevive()
  {
    super.doRevive();
    startBuffTask();
  }

  public void unSummon()
  {
    stopBuffTask();
    super.unSummon();
  }

  public int getHealLevel()
  {
    return Math.min(Math.max((getLevel() - getMinLevel()) / ((80 - getMinLevel()) / 12), 1), 12);
  }

  public int getRechargeLevel()
  {
    return Math.min(Math.max((getLevel() - getMinLevel()) / ((80 - getMinLevel()) / 8), 1), 8);
  }

  public int getBuffLevel()
  {
    if (getNpcId() == 16046)
      return Math.min(Math.max((getLevel() - getMinLevel()) / ((80 - getMinLevel()) / 3), 0), 3);
    return Math.min(Math.max((getLevel() - 55) / 5, 0), 3);
  }

  public int getSoulshotConsumeCount()
  {
    return 1;
  }

  public int getSpiritshotConsumeCount()
  {
    return 1;
  }

  class ActionTask extends RunnableImpl
  {
    ActionTask()
    {
    }

    public void runImpl()
      throws Exception
    {
      Skill skill = onActionTask();
      PetBabyInstance.access$002(PetBabyInstance.this, ThreadPoolManager.getInstance().schedule(new ActionTask(PetBabyInstance.this), skill.getHitTime() * 333 / Math.max(getMAtkSpd(), 1) - 100));
    }
  }
}
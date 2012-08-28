package net.sf.l2j.gameserver.model.actor.instance;

import java.util.Map;
import java.util.concurrent.Future;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.network.serverpackets.NpcInfo;
import net.sf.l2j.gameserver.network.serverpackets.StopMove;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.Point3D;
import net.sf.l2j.util.Rnd;

public final class L2TamedBeastInstance extends L2FeedableBeastInstance
{
  private int _foodSkillId;
  private static final int MAX_DISTANCE_FROM_HOME = 30000;
  private static final int MAX_DISTANCE_FROM_OWNER = 2000;
  private static final int MAX_DURATION = 1200000;
  private static final int DURATION_CHECK_INTERVAL = 60000;
  private static final int DURATION_INCREASE_INTERVAL = 20000;
  private static final int BUFF_INTERVAL = 5000;
  private int _remainingTime = 1200000;
  private int _homeX;
  private int _homeY;
  private int _homeZ;
  private L2PcInstance _owner;
  private Future<?> _buffTask = null;
  private Future<?> _durationCheckTask = null;

  public L2TamedBeastInstance(int objectId, L2NpcTemplate template) {
    super(objectId, template);
    setHome(this);
  }

  public L2TamedBeastInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, int foodSkillId, int x, int y, int z) {
    super(objectId, template);

    setCurrentHp(getMaxHp());
    setCurrentMp(getMaxMp());
    setOwner(owner);
    setFoodType(foodSkillId);
    setHome(x, y, z);
    spawnMe(x, y, z);
  }

  public void onReceiveFood()
  {
    _remainingTime += 20000;
    if (_remainingTime > 1200000)
      _remainingTime = 1200000;
  }

  public Point3D getHome()
  {
    return new Point3D(_homeX, _homeY, _homeZ);
  }

  public void setHome(int x, int y, int z) {
    _homeX = x;
    _homeY = y;
    _homeZ = z;
  }

  public void setHome(L2Character c) {
    setHome(c.getX(), c.getY(), c.getZ());
  }

  public int getRemainingTime() {
    return _remainingTime;
  }

  public void setRemainingTime(int duration) {
    _remainingTime = duration;
  }

  public int getFoodType() {
    return _foodSkillId;
  }

  public void setFoodType(int foodItemId) {
    if (foodItemId > 0) {
      _foodSkillId = foodItemId;

      if (_durationCheckTask != null) {
        _durationCheckTask.cancel(true);
      }
      _durationCheckTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckDuration(this), 60000L, 60000L);
    }
  }

  public boolean doDie(L2Character killer)
  {
    if (!super.doDie(killer)) {
      return false;
    }

    getAI().stopFollow();
    _buffTask.cancel(true);
    _durationCheckTask.cancel(true);

    if (_owner != null) {
      _owner.setTrainedBeast(null);
    }
    _buffTask = null;
    _durationCheckTask = null;
    _owner = null;
    _foodSkillId = 0;
    _remainingTime = 0;
    return true;
  }

  public L2PcInstance getOwner()
  {
    return _owner;
  }

  public void setOwner(L2PcInstance owner) {
    if (owner != null) {
      _owner = owner;
      setTitle(owner.getName());

      broadcastPacket(new NpcInfo(this, owner));

      owner.setTrainedBeast(this);

      getAI().startFollow(_owner, 100);

      int totalBuffsAvailable = 0;
      for (L2Skill skill : getTemplate().getSkills().values())
      {
        if (skill.getSkillType() == L2Skill.SkillType.BUFF) {
          totalBuffsAvailable++;
        }

      }

      if (_buffTask != null) {
        _buffTask.cancel(true);
      }
      _buffTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckOwnerBuffs(this, totalBuffsAvailable), 5000L, 5000L);
    } else {
      doDespawn();
    }
  }

  public boolean isTooFarFromHome() {
    return !isInsideRadius(_homeX, _homeY, _homeZ, 30000, true, true);
  }

  public void doDespawn()
  {
    getAI().stopFollow();
    _buffTask.cancel(true);
    _durationCheckTask.cancel(true);
    stopHpMpRegeneration();

    if (_owner != null) {
      _owner.setTrainedBeast(null);
    }
    setTarget(null);
    _buffTask = null;
    _durationCheckTask = null;
    _owner = null;
    _foodSkillId = 0;
    _remainingTime = 0;

    onDecay();
  }

  public void onOwnerGotAttacked(L2Character attacker)
  {
    if ((_owner == null) || (_owner.isOnline() == 0)) {
      doDespawn();
      return;
    }

    if (!_owner.isInsideRadius(this, 2000, true, true)) {
      getAI().startFollow(_owner);
      return;
    }

    if (_owner.isDead()) {
      return;
    }

    if (isCastingNow()) {
      return;
    }

    float HPRatio = (float)_owner.getCurrentHp() / _owner.getMaxHp();
    int chance;
    if (HPRatio >= 0.8D) {
      FastMap skills = (FastMap)getTemplate().getSkills();

      for (L2Skill skill : skills.values())
      {
        if ((skill.getSkillType() == L2Skill.SkillType.DEBUFF) && (Rnd.get(3) < 1) && (attacker.getFirstEffect(skill) != null)) {
          sitCastAndFollow(skill, attacker);
        }
      }

    }
    else if (HPRatio < 0.5D) {
      chance = 1;
      if (HPRatio < 0.25D) {
        chance = 2;
      }

      FastMap skills = (FastMap)getTemplate().getSkills();

      for (L2Skill skill : skills.values())
      {
        if ((Rnd.get(5) < chance) && ((skill.getSkillType() == L2Skill.SkillType.HEAL) || (skill.getSkillType() == L2Skill.SkillType.HOT) || (skill.getSkillType() == L2Skill.SkillType.BALANCE_LIFE) || (skill.getSkillType() == L2Skill.SkillType.HEAL_PERCENT) || (skill.getSkillType() == L2Skill.SkillType.HEAL_STATIC) || (skill.getSkillType() == L2Skill.SkillType.COMBATPOINTHEAL) || (skill.getSkillType() == L2Skill.SkillType.CPHOT) || (skill.getSkillType() == L2Skill.SkillType.MANAHEAL) || (skill.getSkillType() == L2Skill.SkillType.MANA_BY_LEVEL) || (skill.getSkillType() == L2Skill.SkillType.MANAHEAL_PERCENT) || (skill.getSkillType() == L2Skill.SkillType.MANARECHARGE) || (skill.getSkillType() == L2Skill.SkillType.MPHOT)))
        {
          sitCastAndFollow(skill, _owner);
          return;
        }
      }
    }
  }

  protected void sitCastAndFollow(L2Skill skill, L2Character target)
  {
    stopMove(null);
    broadcastPacket(new StopMove(this));
    getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

    setTarget(target);
    doCast(skill);
    getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, _owner);
  }

  private class CheckOwnerBuffs
    implements Runnable
  {
    private L2TamedBeastInstance _tamedBeast;
    private int _numBuffs;

    CheckOwnerBuffs(L2TamedBeastInstance tamedBeast, int numBuffs)
    {
      _tamedBeast = tamedBeast;
      _numBuffs = numBuffs;
    }

    public void run() {
      L2PcInstance owner = _tamedBeast.getOwner();

      if ((owner == null) || (owner.isOnline() == 0)) {
        doDespawn();
        return;
      }

      if (!isInsideRadius(owner, 2000, true, true)) {
        getAI().startFollow(owner);
        return;
      }

      if (owner.isDead()) {
        return;
      }

      if (isCastingNow()) {
        return;
      }

      int totalBuffsOnOwner = 0;
      int i = 0;
      int rand = Rnd.get(_numBuffs);
      L2Skill buffToGive = null;

      FastMap skills = (FastMap)_tamedBeast.getTemplate().getSkills();

      for (L2Skill skill : skills.values())
      {
        if (skill.getSkillType() == L2Skill.SkillType.BUFF) {
          if (i == rand) {
            buffToGive = skill;
          }
          i++;
          if (owner.getFirstEffect(skill) != null) {
            totalBuffsOnOwner++;
          }
        }
      }

      if (_numBuffs * 2 / 3 > totalBuffsOnOwner) {
        _tamedBeast.sitCastAndFollow(buffToGive, owner);
      }
      getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, _tamedBeast.getOwner());
    }
  }

  private static class CheckDuration
    implements Runnable
  {
    private L2TamedBeastInstance _tamedBeast;

    CheckDuration(L2TamedBeastInstance tamedBeast)
    {
      _tamedBeast = tamedBeast;
    }

    public void run() {
      int foodTypeSkillId = _tamedBeast.getFoodType();
      L2PcInstance owner = _tamedBeast.getOwner();
      _tamedBeast.setRemainingTime(_tamedBeast.getRemainingTime() - 60000);

      L2ItemInstance item = null;
      if (foodTypeSkillId == 2188)
        item = owner.getInventory().getItemByItemId(6643);
      else if (foodTypeSkillId == 2189) {
        item = owner.getInventory().getItemByItemId(6644);
      }

      if ((item != null) && (item.getCount() >= 1)) {
        L2Object oldTarget = owner.getTarget();
        owner.setTarget(_tamedBeast);
        FastList targets = new FastList();
        targets.add(_tamedBeast);

        owner.callSkill(SkillTable.getInstance().getInfo(foodTypeSkillId, 1), targets);
        owner.setTarget(oldTarget);
        targets.clear();
        targets = null;
      }
      else if (_tamedBeast.getRemainingTime() < 900000) {
        _tamedBeast.setRemainingTime(-1);
      }

      if (_tamedBeast.getRemainingTime() <= 0)
        _tamedBeast.doDespawn();
    }
  }
}
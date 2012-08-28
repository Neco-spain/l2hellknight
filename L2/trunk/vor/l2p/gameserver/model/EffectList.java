package l2p.gameserver.model;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntObjectHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import l2p.gameserver.Config;
import l2p.gameserver.skills.EffectType;
import l2p.gameserver.skills.effects.EffectTemplate;
import l2p.gameserver.skills.skillclasses.Transformation;
import l2p.gameserver.stats.Stats;
import l2p.gameserver.stats.funcs.FuncTemplate;
import org.apache.commons.lang3.ArrayUtils;

public class EffectList
{
  public static final int NONE_SLOT_TYPE = -1;
  public static final int BUFF_SLOT_TYPE = 0;
  public static final int MUSIC_SLOT_TYPE = 1;
  public static final int TRIGGER_SLOT_TYPE = 2;
  public static final int DEBUFF_SLOT_TYPE = 3;
  public static final int DEBUFF_LIMIT = 8;
  public static final int MUSIC_LIMIT = 12;
  public static final int TRIGGER_LIMIT = 12;
  private Creature _actor;
  private List<Effect> _effects;
  private Lock lock = new ReentrantLock();

  public EffectList(Creature owner)
  {
    _actor = owner;
  }

  public int getEffectsCountForSkill(int skill_id)
  {
    if (isEmpty()) {
      return 0;
    }
    int count = 0;

    for (Effect e : _effects) {
      if (e.getSkill().getId() == skill_id)
        count++;
    }
    return count;
  }

  public Effect getEffectByType(EffectType et)
  {
    if (isEmpty()) {
      return null;
    }
    for (Effect e : _effects) {
      if (e.getEffectType() == et)
        return e;
    }
    return null;
  }

  public List<Effect> getEffectsBySkill(Skill skill)
  {
    if (skill == null)
      return null;
    return getEffectsBySkillId(skill.getId());
  }

  public List<Effect> getEffectsBySkillId(int skillId)
  {
    if (isEmpty()) {
      return null;
    }
    List list = new ArrayList(2);
    for (Effect e : _effects) {
      if (e.getSkill().getId() == skillId)
        list.add(e);
    }
    return list.isEmpty() ? null : list;
  }

  public Effect getEffectByIndexAndType(int skillId, EffectType type)
  {
    if (isEmpty())
      return null;
    for (Effect e : _effects) {
      if ((e.getSkill().getId() == skillId) && (e.getEffectType() == type))
        return e;
    }
    return null;
  }

  public Effect getEffectByStackType(String type)
  {
    if (isEmpty())
      return null;
    for (Effect e : _effects) {
      if (e.getStackType().equals(type))
        return e;
    }
    return null;
  }

  public boolean containEffectFromSkills(int[] skillIds)
  {
    if (isEmpty()) {
      return false;
    }

    for (Effect e : _effects)
    {
      int skillId = e.getSkill().getId();
      if (ArrayUtils.contains(skillIds, skillId)) {
        return true;
      }
    }
    return false;
  }

  public List<Effect> getAllEffects()
  {
    if (isEmpty())
      return Collections.emptyList();
    return new ArrayList(_effects);
  }

  public boolean isEmpty()
  {
    return (_effects == null) || (_effects.isEmpty());
  }

  public Effect[] getAllFirstEffects()
  {
    if (isEmpty()) {
      return Effect.EMPTY_L2EFFECT_ARRAY;
    }
    TIntObjectHashMap map = new TIntObjectHashMap();

    for (Effect e : _effects) {
      map.put(e.getSkill().getId(), e);
    }
    return (Effect[])map.getValues(new Effect[map.size()]);
  }

  private void checkSlotLimit(Effect newEffect)
  {
    if (_effects == null) {
      return;
    }
    int slotType = getSlotType(newEffect);
    if (slotType == -1) {
      return;
    }
    int size = 0;
    TIntArrayList skillIds = new TIntArrayList();
    for (Effect e : _effects) {
      if (e.isInUse())
      {
        if (e.getSkill().equals(newEffect.getSkill())) {
          return;
        }
        if (!skillIds.contains(e.getSkill().getId()))
        {
          int subType = getSlotType(e);
          if (subType == slotType)
          {
            size++;
            skillIds.add(e.getSkill().getId());
          }
        }
      }
    }
    int limit = 0;

    switch (slotType)
    {
    case 0:
      limit = _actor.getBuffLimit();
      break;
    case 1:
      if (Config.ADD_MUSIC_LIMIT > 12)
        limit = Config.ADD_MUSIC_LIMIT;
      else
        limit = 12;
      break;
    case 3:
      if (Config.ADD_DEBUFF_LIMIT > 8)
        limit = Config.ADD_DEBUFF_LIMIT;
      else
        limit = 8;
      break;
    case 2:
      if (Config.ADD_TRIGGER_LIMIT > 12)
        limit = Config.ADD_TRIGGER_LIMIT;
      else {
        limit = 12;
      }
    }

    if (size < limit) {
      return;
    }
    int skillId = 0;
    for (Effect e : _effects) {
      if (e.isInUse())
      {
        if (getSlotType(e) == slotType)
        {
          skillId = e.getSkill().getId();
          break;
        }
      }
    }
    if (skillId != 0)
      stopEffect(skillId);
  }

  public static int getSlotType(Effect e)
  {
    if ((e.getSkill().isPassive()) || (e.getSkill().isToggle()) || ((e.getSkill() instanceof Transformation)) || (e.getStackType().equals(EffectTemplate.HP_RECOVER_CAST)) || (e.getEffectType() == EffectType.Cubic))
      return -1;
    if (e.getSkill().isOffensive())
      return 3;
    if (e.getSkill().isMusic())
      return 1;
    if (e.getSkill().isTrigger()) {
      return 2;
    }
    return 0;
  }

  public static boolean checkStackType(EffectTemplate ef1, EffectTemplate ef2)
  {
    if ((!_stackType.equals(EffectTemplate.NO_STACK)) && (_stackType.equalsIgnoreCase(ef2._stackType)))
      return true;
    if ((!_stackType.equals(EffectTemplate.NO_STACK)) && (_stackType.equalsIgnoreCase(ef2._stackType2)))
      return true;
    if ((!_stackType2.equals(EffectTemplate.NO_STACK)) && (_stackType2.equalsIgnoreCase(ef2._stackType))) {
      return true;
    }
    return (!_stackType2.equals(EffectTemplate.NO_STACK)) && (_stackType2.equalsIgnoreCase(ef2._stackType2));
  }

  public void addEffect(Effect effect)
  {
    double hp = _actor.getCurrentHp();
    double mp = _actor.getCurrentMp();
    double cp = _actor.getCurrentCp();

    String stackType = effect.getStackType();
    boolean add = false;

    lock.lock();
    try
    {
      if (_effects == null) {
        _effects = new CopyOnWriteArrayList();
      }
      if (stackType.equals(EffectTemplate.NO_STACK))
      {
        for (Effect e : _effects)
        {
          if (!e.isInUse()) {
            continue;
          }
          if ((e.getStackType().equals(EffectTemplate.NO_STACK)) && (e.getSkill().getId() == effect.getSkill().getId()) && (e.getEffectType() == effect.getEffectType()))
          {
            if (effect.getTimeLeft() > e.getTimeLeft())
              e.exit();
            else
              return;
          }
        }
      }
      else {
        for (Effect e : _effects)
        {
          if ((!e.isInUse()) || 
            (!checkStackType(e.getTemplate(), effect.getTemplate()))) {
            continue;
          }
          if ((e.getSkill().getId() == effect.getSkill().getId()) && (e.getEffectType() != effect.getEffectType()))
          {
            break;
          }
          if (e.getStackOrder() == -1)
            return;
          if (!e.maybeScheduleNext(effect))
            return;
        }
      }
      checkSlotLimit(effect);

      if ((add = _effects.add(effect)))
        effect.setInUse(true);
    }
    finally
    {
      lock.unlock();
    }

    if (!add) {
      return;
    }

    effect.start();

    for (FuncTemplate ft : effect.getTemplate().getAttachedFuncs()) {
      if (ft._stat == Stats.MAX_HP)
        _actor.setCurrentHp(hp, false);
      else if (ft._stat == Stats.MAX_MP)
        _actor.setCurrentMp(mp);
      else if (ft._stat == Stats.MAX_CP) {
        _actor.setCurrentCp(cp);
      }
    }
    _actor.updateStats();
    _actor.updateEffectIcons();
  }

  public void removeEffect(Effect effect)
  {
    if (effect == null) {
      return;
    }
    boolean remove = false;

    lock.lock();
    try
    {
      if (_effects == null)
        return;
      if (!(remove = _effects.remove(effect)))
        return;
    }
    finally {
      lock.unlock();
    }

    if (!remove) {
      return;
    }
    _actor.updateStats();
    _actor.updateEffectIcons();
  }

  public void stopAllEffects()
  {
    if (isEmpty()) {
      return;
    }
    lock.lock();
    try
    {
      for (Effect e : _effects)
        e.exit();
    }
    finally
    {
      lock.unlock();
    }

    _actor.updateStats();
    _actor.updateEffectIcons();
  }

  public void stopEffect(int skillId)
  {
    if (isEmpty()) {
      return;
    }
    for (Effect e : _effects)
      if (e.getSkill().getId() == skillId)
        e.exit();
  }

  public void stopEffect(Skill skill)
  {
    if (skill != null)
      stopEffect(skill.getId());
  }

  public void stopEffectByDisplayId(int skillId)
  {
    if (isEmpty()) {
      return;
    }
    for (Effect e : _effects)
      if (e.getSkill().getDisplayId() == skillId)
        e.exit();
  }

  public void stopEffects(EffectType type)
  {
    if (isEmpty()) {
      return;
    }
    for (Effect e : _effects)
      if (e.getEffectType() == type)
        e.exit();
  }

  public void stopAllSkillEffects(EffectType type)
  {
    if (isEmpty()) {
      return;
    }
    TIntHashSet skillIds = new TIntHashSet();

    for (Effect e : _effects) {
      if (e.getEffectType() == type)
        skillIds.add(e.getSkill().getId());
    }
    for (int skillId : skillIds.toArray())
      stopEffect(skillId);
  }
}
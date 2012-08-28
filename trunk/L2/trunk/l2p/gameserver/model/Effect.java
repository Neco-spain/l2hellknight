package l2p.gameserver.model;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import l2p.commons.threading.RunnableImpl;
import l2p.gameserver.Config;
import l2p.gameserver.listener.actor.OnAttackListener;
import l2p.gameserver.listener.actor.OnMagicUseListener;
import l2p.gameserver.model.base.PlayerAccess;
import l2p.gameserver.serverpackets.AbnormalStatusUpdate;
import l2p.gameserver.serverpackets.ExOlympiadSpelledInfo;
import l2p.gameserver.serverpackets.PartySpelled;
import l2p.gameserver.serverpackets.ShortBuffStatusUpdate;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.skills.AbnormalEffect;
import l2p.gameserver.skills.EffectType;
import l2p.gameserver.skills.effects.EffectTemplate;
import l2p.gameserver.stats.Env;
import l2p.gameserver.stats.funcs.Func;
import l2p.gameserver.stats.funcs.FuncOwner;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.taskmanager.EffectTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Effect extends RunnableImpl
  implements Comparable<Effect>, FuncOwner
{
  protected static final Logger _log = LoggerFactory.getLogger(Effect.class);

  public static final Effect[] EMPTY_L2EFFECT_ARRAY = new Effect[0];

  public static int SUSPENDED = -1;

  public static int STARTING = 0;
  public static int STARTED = 1;
  public static int ACTING = 2;
  public static int FINISHING = 3;
  public static int FINISHED = 4;
  protected final Creature _effector;
  protected final Creature _effected;
  protected final Skill _skill;
  protected final int _displayId;
  protected final int _displayLevel;
  private final double _value;
  private final AtomicInteger _state;
  private int _count;
  private long _period;
  private long _startTimeMillis;
  private long _duration;
  private boolean _inUse = false;
  private Effect _next = null;
  private boolean _active = false;
  protected final EffectTemplate _template;
  private Future<?> _effectTask;
  private ActionDispelListener _listener;

  protected Effect(Env env, EffectTemplate template)
  {
    _skill = env.skill;
    _effector = env.character;
    _effected = env.target;

    _template = template;
    _value = template._value;
    _count = template.getCount();
    _period = template.getPeriod();

    _duration = (_period * _count);

    _displayId = (template._displayId != 0 ? template._displayId : _skill.getDisplayId());
    _displayLevel = (template._displayLevel != 0 ? template._displayLevel : _skill.getDisplayLevel());

    _state = new AtomicInteger(STARTING);
  }

  public long getPeriod()
  {
    return _period;
  }

  public void setPeriod(long time)
  {
    _period = time;
    _duration = (_period * _count);
  }

  public int getCount()
  {
    return _count;
  }

  public void setCount(int count)
  {
    _count = count;
    _duration = (_period * _count);
  }

  public boolean isOneTime()
  {
    return _period == 0L;
  }

  public long getStartTime()
  {
    if (_startTimeMillis == 0L)
      return System.currentTimeMillis();
    return _startTimeMillis;
  }

  public long getTime()
  {
    return System.currentTimeMillis() - getStartTime();
  }

  public long getDuration()
  {
    return _duration;
  }

  public int getTimeLeft()
  {
    return (int)((getDuration() - getTime()) / 1000L);
  }

  public boolean isTimeLeft()
  {
    return getDuration() - getTime() > 0L;
  }

  public boolean isInUse()
  {
    return _inUse;
  }

  public void setInUse(boolean inUse)
  {
    _inUse = inUse;
  }

  public boolean isActive()
  {
    return _active;
  }

  public void setActive(boolean set)
  {
    _active = set;
  }

  public EffectTemplate getTemplate()
  {
    return _template;
  }

  public String getStackType()
  {
    return getTemplate()._stackType;
  }

  public String getStackType2()
  {
    return getTemplate()._stackType2;
  }

  public boolean checkStackType(String param)
  {
    return (getStackType().equalsIgnoreCase(param)) || (getStackType2().equalsIgnoreCase(param));
  }

  public boolean checkStackType(Effect param)
  {
    return (checkStackType(param.getStackType())) || (checkStackType(param.getStackType2()));
  }

  public int getStackOrder()
  {
    return getTemplate()._stackOrder;
  }

  public Skill getSkill()
  {
    return _skill;
  }

  public Creature getEffector()
  {
    return _effector;
  }

  public Creature getEffected()
  {
    return _effected;
  }

  public double calc()
  {
    return _value;
  }

  public boolean isEnded()
  {
    return (isFinished()) || (isFinishing());
  }

  public boolean isFinishing()
  {
    return getState() == FINISHING;
  }

  public boolean isFinished()
  {
    return getState() == FINISHED;
  }

  private int getState()
  {
    return _state.get();
  }

  private boolean setState(int oldState, int newState)
  {
    return _state.compareAndSet(oldState, newState);
  }

  public boolean checkCondition()
  {
    return true;
  }

  protected void onStart()
  {
    getEffected().addStatFuncs(getStatFuncs());
    getEffected().addTriggers(getTemplate());
    if (getTemplate()._abnormalEffect != AbnormalEffect.NULL)
      getEffected().startAbnormalEffect(getTemplate()._abnormalEffect);
    else if (getEffectType().getAbnormal() != null)
      getEffected().startAbnormalEffect(getEffectType().getAbnormal());
    if (getTemplate()._abnormalEffect2 != AbnormalEffect.NULL)
      getEffected().startAbnormalEffect(getTemplate()._abnormalEffect2);
    if (getTemplate()._abnormalEffect3 != AbnormalEffect.NULL)
      getEffected().startAbnormalEffect(getTemplate()._abnormalEffect3);
    if (_template._cancelOnAction)
      getEffected().addListener(this._listener = new ActionDispelListener(null));
    if ((getEffected().isPlayer()) && (!getSkill().canUseTeleport()))
      getEffected().getPlayer().getPlayerAccess().UseTeleport = false;
  }

  protected abstract boolean onActionTime();

  protected void onExit()
  {
    getEffected().removeStatsOwner(this);
    getEffected().removeTriggers(getTemplate());
    if (getTemplate()._abnormalEffect != AbnormalEffect.NULL)
      getEffected().stopAbnormalEffect(getTemplate()._abnormalEffect);
    else if (getEffectType().getAbnormal() != null)
      getEffected().stopAbnormalEffect(getEffectType().getAbnormal());
    if (getTemplate()._abnormalEffect2 != AbnormalEffect.NULL)
      getEffected().stopAbnormalEffect(getTemplate()._abnormalEffect2);
    if (getTemplate()._abnormalEffect3 != AbnormalEffect.NULL)
      getEffected().stopAbnormalEffect(getTemplate()._abnormalEffect3);
    if (_template._cancelOnAction)
      getEffected().removeListener(_listener);
    if ((getEffected().isPlayer()) && (getStackType().equals(EffectTemplate.HP_RECOVER_CAST)))
      getEffected().sendPacket(new ShortBuffStatusUpdate());
    if ((getEffected().isPlayer()) && (!getSkill().canUseTeleport()) && (!getEffected().getPlayer().getPlayerAccess().UseTeleport))
      getEffected().getPlayer().getPlayerAccess().UseTeleport = true;
  }

  private void stopEffectTask()
  {
    if (_effectTask != null)
      _effectTask.cancel(false);
  }

  private void startEffectTask()
  {
    if (_effectTask == null)
    {
      _startTimeMillis = System.currentTimeMillis();
      _effectTask = EffectTaskManager.getInstance().scheduleAtFixedRate(this, _period, _period);
    }
  }

  public final void schedule()
  {
    Creature effected = getEffected();
    if (effected == null) {
      return;
    }
    if (!checkCondition()) {
      return;
    }
    getEffected().getEffectList().addEffect(this);
  }

  private final void suspend()
  {
    if (setState(STARTING, SUSPENDED)) {
      startEffectTask();
    } else if ((setState(STARTED, SUSPENDED)) || (setState(ACTING, SUSPENDED)))
    {
      synchronized (this)
      {
        if (isInUse())
        {
          setInUse(false);
          setActive(false);
          onExit();
        }
      }
      getEffected().getEffectList().removeEffect(this);
    }
  }

  public final void start()
  {
    if (setState(STARTING, STARTED))
    {
      synchronized (this)
      {
        if (isInUse())
        {
          setActive(true);
          onStart();
          startEffectTask();
        }
      }
    }

    run();
  }

  public final void runImpl()
    throws Exception
  {
    if (setState(STARTED, ACTING))
    {
      if ((!getSkill().isHideStartMessage()) && (getEffected().getEffectList().getEffectsCountForSkill(getSkill().getId()) == 1)) {
        getEffected().sendPacket(new SystemMessage(110).addSkillName(_displayId, _displayLevel));
      }
      return;
    }

    if (getState() == SUSPENDED)
    {
      if (isTimeLeft())
      {
        _count -= 1;
        if (isTimeLeft()) {
          return;
        }
      }
      exit();
      return;
    }

    if ((getState() == ACTING) && 
      (isTimeLeft()))
    {
      _count -= 1;
      if (((!isActive()) || (onActionTime())) && (isTimeLeft())) {
        return;
      }
    }
    if (setState(ACTING, FINISHING)) {
      setInUse(false);
    }
    if (setState(FINISHING, FINISHED))
    {
      synchronized (this)
      {
        setActive(false);
        stopEffectTask();
        onExit();
      }

      Effect next = getNext();
      if ((next != null) && 
        (next.setState(SUSPENDED, STARTING))) {
        next.schedule();
      }
      if (getSkill().getDelayedEffect() > 0) {
        SkillTable.getInstance().getInfo(getSkill().getDelayedEffect(), 1).getEffects(_effector, _effected, false, false);
      }
      boolean msg = (!isHidden()) && (getEffected().getEffectList().getEffectsCountForSkill(getSkill().getId()) == 1);

      getEffected().getEffectList().removeEffect(this);

      if (msg)
        getEffected().sendPacket(new SystemMessage(92).addSkillName(_displayId, _displayLevel));
    }
  }

  public final void exit()
  {
    Effect next = getNext();
    if (next != null)
      next.exit();
    removeNext();

    if (setState(STARTING, FINISHED)) {
      getEffected().getEffectList().removeEffect(this);
    }
    else if (setState(SUSPENDED, FINISHED)) {
      stopEffectTask();
    } else if ((setState(STARTED, FINISHED)) || (setState(ACTING, FINISHED)))
    {
      synchronized (this)
      {
        if (isInUse())
        {
          setInUse(false);
          setActive(false);
          stopEffectTask();
          onExit();
        }
      }
      getEffected().getEffectList().removeEffect(this);
    }
  }

  private boolean scheduleNext(Effect e)
  {
    if ((e == null) || (e.isEnded())) {
      return false;
    }
    Effect next = getNext();
    if ((next != null) && (!next.maybeScheduleNext(e))) {
      return false;
    }
    _next = e;

    return true;
  }

  public Effect getNext()
  {
    return _next;
  }

  private void removeNext()
  {
    _next = null;
  }

  public boolean maybeScheduleNext(Effect newEffect)
  {
    if (newEffect.getStackOrder() < getStackOrder())
    {
      if (newEffect.getTimeLeft() > getTimeLeft())
      {
        newEffect.suspend();
        scheduleNext(newEffect);
      }

      return false;
    }

    if (newEffect.getTimeLeft() >= getTimeLeft())
    {
      if ((getNext() != null) && (getNext().getTimeLeft() > newEffect.getTimeLeft()))
      {
        newEffect.scheduleNext(getNext());

        removeNext();
      }
      exit();
    }
    else
    {
      suspend();
      newEffect.scheduleNext(this);
    }

    return true;
  }

  public Func[] getStatFuncs()
  {
    return getTemplate().getStatFuncs(this);
  }

  public void addIcon(AbnormalStatusUpdate mi)
  {
    if ((!isActive()) || (isHidden()))
      return;
    int duration = _skill.isToggle() ? -1 : getTimeLeft();
    mi.addEffect(_displayId, _displayLevel, duration);
  }

  public void addPartySpelledIcon(PartySpelled ps)
  {
    if ((!isActive()) || (isHidden()))
      return;
    int duration = _skill.isToggle() ? -1 : getTimeLeft();
    ps.addPartySpelledEffect(_displayId, _displayLevel, duration);
  }

  public void addOlympiadSpelledIcon(Player player, ExOlympiadSpelledInfo os)
  {
    if ((!isActive()) || (isHidden()))
      return;
    int duration = _skill.isToggle() ? -1 : getTimeLeft();
    os.addSpellRecivedPlayer(player);
    os.addEffect(_displayId, _displayLevel, duration);
  }

  protected int getLevel()
  {
    return _skill.getLevel();
  }

  public EffectType getEffectType()
  {
    return getTemplate()._effectType;
  }

  public boolean isHidden()
  {
    return _displayId < 0;
  }

  public int compareTo(Effect obj)
  {
    if (obj.equals(this))
      return 0;
    return 1;
  }

  public boolean isSaveable()
  {
    return (_template.isSaveable(getSkill().isSaveable())) && (getTimeLeft() >= Config.ALT_SAVE_EFFECTS_REMAINING_TIME);
  }

  public int getDisplayId()
  {
    return _displayId;
  }

  public int getDisplayLevel()
  {
    return _displayLevel;
  }

  public boolean isCancelable()
  {
    return _template.isCancelable(getSkill().isCancelable());
  }

  public String toString()
  {
    return "Skill: " + _skill + ", state: " + getState() + ", inUse: " + _inUse + ", active : " + _active;
  }

  public boolean isFuncEnabled()
  {
    return isInUse();
  }

  public boolean overrideLimits()
  {
    return false;
  }

  public boolean isOffensive()
  {
    return _template.isOffensive(getSkill().isOffensive());
  }

  private class ActionDispelListener
    implements OnAttackListener, OnMagicUseListener
  {
    private ActionDispelListener()
    {
    }

    public void onMagicUse(Creature actor, Skill skill, Creature target, boolean alt)
    {
      exit();
    }

    public void onAttack(Creature actor, Creature target)
    {
      exit();
    }
  }
}
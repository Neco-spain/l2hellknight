//L2E
package net.sf.l2j.gameserver.model;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExOlympiadSpelledInfo;
import net.sf.l2j.gameserver.network.serverpackets.MagicEffectIcons;
import net.sf.l2j.gameserver.network.serverpackets.PartySpelled;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.effects.EffectTemplate;
import net.sf.l2j.gameserver.skills.funcs.Func;
import net.sf.l2j.gameserver.skills.funcs.FuncTemplate;
import net.sf.l2j.gameserver.skills.funcs.Lambda;

public abstract class L2Effect
{
    static final Logger _log = Logger.getLogger(L2Effect.class.getName());

    public static enum EffectState 
    {
        CREATED, ACTING, FINISHING
    }

    public static enum EffectType
    {
        BUFF,
		DEBUFF,
        CHARGE,
        DMG_OVER_TIME,
        HEAL_OVER_TIME,
        COMBAT_POINT_HEAL_OVER_TIME,
        MANA_DMG_OVER_TIME,
        MANA_HEAL_OVER_TIME,
        RELAXING, STUN, ROOT,
        SLEEP,
        HATE,
        FAKE_DEATH,
        CONFUSION,
        CONFUSE_MOB_ONLY,
        MUTE,
        FEAR,
        SILENT_MOVE,
        SEED,
        PARALYZE,
        RNDTELEPORT,
        STUN_SELF,
        PSYCHICAL_MUTE,
        REMOVE_TARGET,
        TARGET_ME,
        SILENCE_MAGIC_PHYSICAL,
        BETRAY,
        NOBLESSE_BLESSING,
	    PHOENIX_BLESSING,
        PETRIFICATION,
        BLUFF,
		MEDITATION,
		CHAMELEON,
        CHARM_OF_LUCK,
		CHARMOFCOURAGE,
        INVINCIBLE,
		PROTECTION_BLESSING,
		INVISIBLE,
		DISPELL_YOU,
		SIGNET_EFFECT,
		SIGNET_GROUND,
		CLAN_GATE,
		BLOCK_BUFF
    }

    private static final Func[] _emptyFunctionSet = new Func[0];

    private final L2Character _effector;

    private final L2Character _effected;

    private final L2Skill _skill;

    private final Lambda _lambda;

    private EffectState _state;

    private final int _period;
    private int _periodStartTicks;
    private int _periodfirsttime;

    private final FuncTemplate[] _funcTemplates;

    private int _totalCount;

    private int _count;

    private int _abnormalEffect;
	private boolean _icon;

    public boolean preventExitUpdate;

    public final class EffectTask implements Runnable
    {
        protected final int _delay;
        protected final int _rate;

        EffectTask(int pDelay, int pRate)
        {
            _delay = pDelay;
            _rate = pRate;
        }

        public void run()
        {
            try
            {
                if (getPeriodfirsttime() == 0) setPeriodStartTicks(GameTimeController.getGameTicks());
                else setPeriodfirsttime(0);
                L2Effect.this.scheduleEffect();
            }
            catch (Throwable e)
            {
                _log.log(Level.SEVERE, "", e);
            }
        }
    }

	private ScheduledFuture<?> _currentFuture;
    private EffectTask _currentTask;

    private final String _stackType;

    private final float _stackOrder;

    private boolean _inUse = false;
	
	private boolean _startConditionsCorrect = true;

    protected L2Effect(Env env, EffectTemplate template)
    {
        _state = EffectState.CREATED;
        _skill = env.skill;
        _effected = env.target;
        _effector = env.player;
        _lambda = template.lambda;
        _funcTemplates = template.funcTemplates;
        _count = template.counter;
        _totalCount = _count;
        int temp = template.period;
        if (env.skillMastery)
        	temp *= 2;
        _period = temp;
        _abnormalEffect = template.abnormalEffect;
        _stackType = template.stackType;
        _stackOrder = template.stackOrder;
        _periodStartTicks = GameTimeController.getGameTicks();
        _periodfirsttime = 0;
		_icon = template.icon;
        scheduleEffect();
    }

    public int getCount()
    {
        return _count;
    }

	public boolean getShowIcon()
	{
		return _icon;
	}

    public int getTotalCount()
    {
        return _totalCount;
    }

    public void setCount(int newcount)
    {
        _count = newcount;
    }

    public void setFirstTime(int newfirsttime)
    {
        if (_currentFuture != null)
        {
            _periodStartTicks = GameTimeController.getGameTicks() - newfirsttime
                * GameTimeController.TICKS_PER_SECOND;
            _currentFuture.cancel(false);
            _currentFuture = null;
            _currentTask = null;
            _periodfirsttime = newfirsttime;
            int duration = _period - _periodfirsttime;
            _currentTask = new EffectTask(duration * 1000, -1);
            _currentFuture = ThreadPoolManager.getInstance().scheduleEffect(_currentTask,
                                                                            duration * 1000);
        }
    }

    public int getPeriod()
    {
        return _period;
    }

    public int getTime()
    {
        return (GameTimeController.getGameTicks() - _periodStartTicks)
            / GameTimeController.TICKS_PER_SECOND;
    }

    public int getTaskTime()
    {
    	if (_count == _totalCount) return 0;
    	return (Math.abs(_count-_totalCount+1)*_period) + getTime()+1;
    }

    public boolean getInUse()
    {
        return _inUse;
    }

	public void setInUse(boolean inUse)
	{
		_inUse = inUse;
		if (_inUse)
		_startConditionsCorrect = onStart();
		else onExit();
	}

    public String getStackType()
    {
        return _stackType;
    }

    public float getStackOrder()
    {
        return _stackOrder;
    }

    public final L2Skill getSkill()
    {
        return _skill;
    }

    public final L2Character getEffector()
    {
        return _effector;
    }

    public final L2Character getEffected()
    {
        return _effected;
    }
    public boolean isSelfEffect()
    {
        return _skill._effectTemplatesSelf != null;
    }

    public boolean isHerbEffect()
    {
    	if (getSkill().getName().contains("Herb"))
    		return true;

    	return false;
    }

    public final double calc()
    {
        Env env = new Env();
        env.player = _effector;
        env.target = _effected;
        env.skill = _skill;
        return _lambda.calc(env);
    }

    private synchronized void startEffectTask(int duration)
    {
        stopEffectTask();
        _currentTask = new EffectTask(duration, -1);
        _currentFuture = ThreadPoolManager.getInstance().scheduleEffect(_currentTask, duration);
        if (_state == EffectState.ACTING) _effected.addEffect(this);
    }

    private synchronized void startEffectTaskAtFixedRate(int delay, int rate)
    {
        stopEffectTask();
        _currentTask = new EffectTask(delay, rate);
        _currentFuture = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(_currentTask, delay,
                                                                                   rate);
        if (_state == EffectState.ACTING) _effected.addEffect(this);
    }

    public final void exit()
    {
        this.exit(false);
    }

    public final void exit(boolean preventUpdate)
    {
    	preventExitUpdate = preventUpdate;
    	_state = EffectState.FINISHING;
    	scheduleEffect();
    }

    public synchronized void stopEffectTask()
    {
        if (_currentFuture != null)
        {
            _currentFuture.cancel(false);
            _currentFuture = null;
            _currentTask = null;

            _effected.removeEffect(this);
        }
    }

    public abstract EffectType getEffectType();

    public boolean onStart()
    {
        if (_abnormalEffect != 0) getEffected().startAbnormalEffect(_abnormalEffect);
		return true;
    }

    public void onExit()
    {
        if (_abnormalEffect != 0) getEffected().stopAbnormalEffect(_abnormalEffect);
    }

    public abstract boolean onActionTime();

    public final void rescheduleEffect()
    {
        if (_state != EffectState.ACTING)
        {
            scheduleEffect();
        }
        else
        {
            if (_count > 1)
            {
                startEffectTaskAtFixedRate(5, _period * 1000);
                return;
            }
            if (_period > 0)
            {
                startEffectTask(_period * 1000);
                return;
            }
        }
    }

	public final void scheduleEffect()
	{
		if (_state == EffectState.CREATED)
		{
			_state = EffectState.ACTING;

			if (_skill.isPvpSkill())
			{
				SystemMessage smsg = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
				smsg.addString(_skill.getName());
				getEffected().sendPacket(smsg);
			}

			if (_count > 1)
			{
				startEffectTaskAtFixedRate(5, _period * 1000);
				return;
			}
			if (_period > 0 || _period == -1)
			{
				startEffectTask(_period * 1000);
				return;
			}
			_startConditionsCorrect = onStart();
		}

		if (_state == EffectState.ACTING)
		{
			if (_count-- > 0)
			{
				if (getInUse())
				{
					if (onActionTime() && _startConditionsCorrect) return;
				}
				else if (_count > 0)
				{
					return;
				}
			}
			_state = EffectState.FINISHING;
		}

		if (_state == EffectState.FINISHING)
		{
			if (getInUse() || !(_count > 1 || _period > 0))
				if (_startConditionsCorrect)
				onExit();

			if (_count == 0 && (getEffected() != null) && ((getEffected() instanceof L2PcInstance)))
			{
				SystemMessage smsg3 = new SystemMessage(SystemMessageId.S1_HAS_WORN_OFF);
				smsg3.addString(_skill.getName());
				getEffected().sendPacket(smsg3);
			}
			stopEffectTask();
		}
	}

    public Func[] getStatFuncs()
    {
        if (_funcTemplates == null) 
		return _emptyFunctionSet;
        List<Func> funcs = new FastList<Func>();
        for (FuncTemplate t : _funcTemplates)
        {
            Env env = new Env();
            env.player = getEffector();
            env.target = getEffected();
            env.skill = getSkill();
            Func f = t.getFunc(env, this);
            if (f != null) funcs.add(f);
        }
        if (funcs.size() == 0) 
		return _emptyFunctionSet;
        return funcs.toArray(new Func[funcs.size()]);
    }

	public final void addIcon(MagicEffectIcons mi)
	{
		EffectTask task = _currentTask;
		ScheduledFuture<?> future = _currentFuture;
		if (task == null || future == null) return;
		if (_state == EffectState.FINISHING || _state == EffectState.CREATED) return;
		L2Skill sk = getSkill();
		if (task._rate > 0)
		{
			if (sk.isPotion()) mi.addEffect(sk.getId(), getLevel(), sk.getBuffDuration() - (getTaskTime() * 1000));
			else mi.addEffect(sk.getId(), getLevel(), -1);
		}
		else mi.addEffect(sk.getId(), getLevel(), (int)future.getDelay(TimeUnit.MILLISECONDS));
	}

	public final void addPartySpelledIcon(PartySpelled ps)
	{
		EffectTask task = _currentTask;
		ScheduledFuture<?> future = _currentFuture;
		if (task == null || future == null) return;
		if (_state == EffectState.FINISHING || _state == EffectState.CREATED) return;
		L2Skill sk = getSkill();
		ps.addPartySpelledEffect(sk.getId(), getLevel(), (int)future.getDelay(TimeUnit.MILLISECONDS));
	}

	public final void addOlympiadSpelledIcon(ExOlympiadSpelledInfo os)
	{
		EffectTask task = _currentTask;
		ScheduledFuture<?> future = _currentFuture;
		if (task == null || future == null) return;
		if (_state == EffectState.FINISHING || _state == EffectState.CREATED) return;
		L2Skill sk = getSkill();
		os.addEffect(sk.getId(), getLevel(), (int)future.getDelay(TimeUnit.MILLISECONDS));
	}

    public int getLevel()
    {
        return getSkill().getLevel();
    }

    public int getPeriodfirsttime()
    {
        return _periodfirsttime;
    }

    public void setPeriodfirsttime(int periodfirsttime)
    {
        _periodfirsttime = periodfirsttime;
    }

    public int getPeriodStartTicks()
    {
        return _periodStartTicks;
    }

    public void setPeriodStartTicks(int periodStartTicks)
    {
        _periodStartTicks = periodStartTicks;
    }
}

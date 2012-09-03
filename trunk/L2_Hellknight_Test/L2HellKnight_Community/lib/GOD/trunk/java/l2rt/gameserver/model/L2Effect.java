package l2rt.gameserver.model;

import l2rt.config.ConfigSystem;
import l2rt.extensions.listeners.MethodCollection;
import l2rt.extensions.listeners.MethodInvokeListener;
import l2rt.extensions.listeners.events.MethodEvent;
import l2rt.gameserver.model.L2Skill.SkillType;
import l2rt.gameserver.network.serverpackets.AbnormalStatusUpdate;
import l2rt.gameserver.network.serverpackets.ExAbnormalStatusUpdateFromTargetPacket;
import l2rt.gameserver.network.serverpackets.ExOlympiadSpelledInfo;
import l2rt.gameserver.network.serverpackets.PartySpelled;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.skills.AbnormalEffect;
import l2rt.gameserver.skills.EffectType;
import l2rt.gameserver.skills.Env;
import l2rt.gameserver.skills.Stats;
import l2rt.gameserver.skills.effects.EffectTemplate;
import l2rt.gameserver.skills.funcs.Func;
import l2rt.gameserver.skills.funcs.FuncOwner;
import l2rt.gameserver.skills.funcs.FuncTemplate;
import l2rt.gameserver.tables.SkillTable;
import l2rt.gameserver.taskmanager.EffectTaskManager;
import l2rt.util.ArrayMap;

import java.util.logging.Logger;

public abstract class L2Effect implements Comparable<L2Effect>, FuncOwner
{
	protected static final Logger _log = Logger.getLogger(L2Effect.class.getName());

	public static enum EffectState
	{
		CREATED,
		ACTING,
		FINISHING,
		FINISHED
	}

	private static final Func[] _emptyFunctionSet = new Func[0];

	public static EffectType CLAN_GATE;

	/** Накладывающий эффект */
	protected final L2Character _effector;
	/** Тот, на кого накладывают эффект */
	protected final L2Character _effected;

	protected final L2Skill _skill;
	protected final int _displayId;
	protected final int _displayLevel;

	// the value of an update
	private final double _value;

	// the current state
	protected EffectState _state;

	// period, milliseconds
	private long _period;
	private long _periodStartTime;

	// function templates
	private final FuncTemplate[] _funcTemplates;

	private final EffectType _effectType;

	// counter
	protected int _count;

	// abnormal effects
	private AbnormalEffect _abnormalEffect;
	private AbnormalEffect _abnormalEffect2;

	/** The Identifier of the stack group */
	private final String _stackType;
	private final String _stackType2;
	private final String _stackType3;
	private final String _stackType4;
	private final String _stackType5;

	/** The position of the effect in the stack group */
	private final int _stackOrder;

	private boolean _inUse = false;
	private L2Effect _next = null;
	private boolean _active = false;

	private boolean _skillMastery = false;

	public final EffectTemplate _template;

	protected L2Effect(Env env, EffectTemplate template)
	{
		_template = template;
		_state = EffectState.CREATED;
		_skill = env.skill;
		_effector = env.character;
		_effected = env.target;
		_value = template._value;
		_funcTemplates = template._funcTemplates;
		_abnormalEffect = template._abnormalEffect;
		_abnormalEffect2 = template._abnormalEffect2;
		_stackType = template._stackType;
		_stackType2 = template._stackType2;
		_stackType3 = template._stackType3;
		_stackType4 = template._stackType4;
		_stackType5 = template._stackType5;
		_stackOrder = template._stackOrder;
		_effectType = template._effectType;

		_count = template._counter;
		_period = template.getPeriod();
		_displayId = template._displayId != 0 ? template._displayId : _skill.getDisplayId();
		_displayLevel = template._displayLevel != 0 ? template._displayLevel : _skill.getDisplayLevel();

		// Check for skill mastery duration time increase
		if(ArrayMap.get(env.arraymap, Env.SkillMastery) == 2)
		{
			if(_count > 1)
				_count *= 2;
			else
				_period *= 2;
			_skillMastery = true;
		}

		// Считаем влияние резистов
		if(!template._applyOnCaster && _skill.isOffensive() && !_skill.isIgnoreResists() && !_effector.isRaid())
		{
			double res = 0;
			if(_effectType.getResistType() != null)
				res += _effected.calcStat(_effectType.getResistType(), _effector, _skill);
			if(_effectType.getAttibuteType() != null)
				res -= _effector.calcStat(_effectType.getAttibuteType(), _effected, _skill);

			res += _effected.calcStat(Stats.DEBUFF_RECEPTIVE, _effector, _skill);

			if(res != 0)
			{
				double mod = 1 + Math.abs(0.01 * res);
				if(res > 0)
					mod = 1. / mod;

				if(_count > 1)
					_count = (int) Math.floor(Math.max(_count * mod, 1));
				else
					_period = (long) Math.floor(Math.max(_period * mod, 1));
			}
		}

		if(_skill.getSkillType() == SkillType.BUFF && _period > 119000 && _skill.getId() != 396 && _skill.getId() != 1374)
			_period *= ConfigSystem.getFloat("BuffTimeModifier");
		if(_skill.isMusic())
			_period *= ConfigSystem.getFloat("SongDanceTimeModifier");
		if(_skill.getId() >= 4342 && _skill.getId() <= 4360)
			_period *= ConfigSystem.getFloat("ClanHallBuffTimeModifier");
		_periodStartTime = System.currentTimeMillis();
	}

	public long getPeriod()
	{
		return _period;
	}

	public void setPeriod(long time)
	{
		_period = time;
	}

	public int getCount()
	{
		return _count;
	}

	public void setCount(int newcount)
	{
		_count = newcount;
	}

	public long getTime()
	{
		return System.currentTimeMillis() - _periodStartTime;
	}

	public long getPeriodStartTime()
	{
		return _periodStartTime;
	}

	/** Возвращает оставшееся время в миллисекундах. */
	public long getTimeLeft()
	{
		return getPeriod() * getCount() - getTime();
	}

	public boolean isInUse()
	{
		return _inUse;
	}

	public void setInUse(boolean inUse)
	{
		_inUse = inUse;
		if(_inUse)
			scheduleEffect();
		else if(_state != EffectState.FINISHED)
			_state = EffectState.FINISHING;
	}

	public boolean isActive()
	{
		return _active;
	}

	/**
	 * true означает что эффект зашедулен и сейчас не считается активным. Для неактивных эфектов не вызывается onActionTime.
	 */
	public void setActive(boolean set)
	{
		_active = set;
	}

	public String getStackType()
	{
		return _stackType;
	}

	public String getStackType2()
	{
		return _stackType2;
	}

	public String getStackType3()
	{
		return _stackType2;
	}
	
	public String getStackType4()
	{
		return _stackType2;
	}
	
	public String getStackType5()
	{
		return _stackType2;
	}
	
	public boolean checkStackType(String param)
	{
		return _stackType.equalsIgnoreCase(param) || _stackType2.equalsIgnoreCase(param) || _stackType3.equalsIgnoreCase(param) || _stackType4.equalsIgnoreCase(param) || _stackType5.equalsIgnoreCase(param);
	}

	public boolean checkStackType(L2Effect param)
	{
		return checkStackType(param.getStackType()) || checkStackType(param.getStackType2()) || checkStackType(param.getStackType3()) || checkStackType(param.getStackType4()) || checkStackType(param.getStackType5());
	}

	public int getStackOrder()
	{
		return _stackOrder;
	}

	public L2Skill getSkill()
	{
		return _skill;
	}

	public L2Character getEffector()
	{
		return _effector;
	}

	public L2Character getEffected()
	{
		return _effected;
	}

	public double calc()
	{
		return _value;
	}

	/**
	 * Stop the L2Effect task and send Server->Client update packet.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Cancel the effect in the the abnormal effect map of the L2Character </li>
	 * <li>Stop the task of the L2Effect, remove it and update client magic icon </li><BR><BR>
	 *
	 */
	public void exit()
	{
		if(_next != null)
			_next.exit();
		_next = null;

		if(_state == EffectState.FINISHED)
			return;
		if(_state != EffectState.CREATED)
		{
			_state = EffectState.FINISHING;
			scheduleEffect();
		}
		else
			_state = EffectState.FINISHING;
	}

	public boolean isEnded()
	{
		return _state == EffectState.FINISHED || _state == EffectState.FINISHING;
	}

	public boolean isFinishing()
	{
		return _state == EffectState.FINISHING;
	}

	public boolean isFinished()
	{
		return _state == EffectState.FINISHED;
	}

	/**
	 * Stop the task of the L2Effect, remove it and update client magic icon.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Cancel the task </li>
	 * <li>Stop and remove L2Effect from L2Character and update client magic icon </li><BR><BR>
	 *
	 */
	private synchronized void stopEffectTask()
	{
		_effected.getEffectList().removeEffect(this);
		updateEffects();
	}

	private ActionDispelListener _listener;
	private DamageDispelListener _listener2;

	private class ActionDispelListener implements MethodInvokeListener, MethodCollection
	{
		@Override
		public boolean accept(MethodEvent event)
		{
			return event.getMethodName().equals(onStartAttack) || event.getMethodName().equals(onStartCast) || event.getMethodName().equals(onStartAltCast);
		}

		@Override
		public void methodInvoked(MethodEvent e)
		{
			exit();
		}
	}

	private class DamageDispelListener implements MethodInvokeListener, MethodCollection
	{
		@Override
		public boolean accept(MethodEvent event)
		{
			return event.getMethodName().equals(OnAttacked);
		}

		@Override
		public void methodInvoked(MethodEvent e)
		{
			exit();
		}
	}

	public boolean checkCondition()
	{
		return true;
	}

	/** Notify started */
	public void onStart()
	{
		if(_abnormalEffect != AbnormalEffect.NULL)
			getEffected().startAbnormalEffect(_abnormalEffect);
		else if(getEffectType().getAbnormal() != null)
			getEffected().startAbnormalEffect(getEffectType().getAbnormal());
		if(_abnormalEffect2 != AbnormalEffect.NULL)
			getEffected().startAbnormalEffect(_abnormalEffect2);
		if(_template._cancelOnAction)
			getEffected().addMethodInvokeListener(_listener = new ActionDispelListener());
		if(_template._cancelOnDamage)
			getEffected().addMethodInvokeListener(_listener2 = new DamageDispelListener());
	}

	/**
	 * Cancel the effect in the the abnormal effect map of the effected L2Character.<BR><BR>
	 */
	public void onExit()
	{
		if(_abnormalEffect != AbnormalEffect.NULL)
			getEffected().stopAbnormalEffect(_abnormalEffect);
		else if(getEffectType().getAbnormal() != null)
			getEffected().stopAbnormalEffect(getEffectType().getAbnormal());
		if(_abnormalEffect2 != AbnormalEffect.NULL)
			getEffected().stopAbnormalEffect(_abnormalEffect2);
		if(_template._cancelOnAction)
			getEffected().removeMethodInvokeListener(_listener);
		if(_template._cancelOnDamage)
			getEffected().removeMethodInvokeListener(_listener2);
	}

	/** Return true for continuation of this effect */
	public abstract boolean onActionTime();

	public final void scheduleEffect()
	{
		L2Character effected = getEffected();
		if(effected == null)
			return;

		// Если персонаж выходит (или уже вышел) из игры, просто останавливаем эффект
		if(_state != EffectState.FINISHED && effected.isPlayer() && ((L2Player) effected).isDeleting())
		{
			_state = EffectState.FINISHED;
			_inUse = false;
			onExit();
			stopEffectTask();
			return;
		}

		if(_state == EffectState.CREATED)
		{
			if(!checkCondition())
			{
				// TODO Переделать так, чтобы проверка вызывалась до owner.addStatFuncs(newEffect.getStatFuncs()); и эффект вообще не добавлялся игроку, если условие не прошло.
				// Учесть случаи, когда эффект ставится в очередь, либо вынимается из очереди. Лучше всего делать проверку еще до постановки в очередь.
				// Но не забыть, что условия могут подойти вначале, но не подойти после вынимания из очереди.
				// Сейчас вся очередь уничтожается, если условие не подошло. Это может произойти, только если этот эффект уже сам в очереди, что конечно маловероятно :)
				exit(); // Т.к. _state CREATED, никаких действий не выполнится
				_effected.getEffectList().removeEffect(this); // Удаляем эффект у игрока
				return;
			}

			_state = EffectState.ACTING;
			onStart();

			// Fake Death и Silent Move не отображаются
			// Отображать сообщение только для первого эффекта скилла
			if(_skill.getId() != 60 && _skill.getId() != 221 && getEffected().getEffectList().getEffectsCountForSkill(getSkill().getId()) == 1)
				getEffected().sendPacket(new SystemMessage(SystemMessage.S1_S2S_EFFECT_CAN_BE_FELT).addSkillName(_displayId, _displayLevel));

			updateEffects(); // Обрабатываем отображение статов

			EffectTaskManager.getInstance().addDispelTask(this, (int) (_period / 1000));

			_periodStartTime = System.currentTimeMillis();

			return;
		}

		if(_state == EffectState.ACTING)
		{
			if(_count > 0)
			{
				_count--;
				if((!isActive() || onActionTime()) && _count > 0)
					return;
			}
			_state = EffectState.FINISHING;
		}

		if(_state == EffectState.FINISHING)
		{
			_state = EffectState.FINISHED;

			// Для ускоренной "остановки" эффекта
			_inUse = false;

			// Cancel the effect in the the abnormal effect map of the L2Character
			onExit();

			// If the time left is equal to zero, send the message
			// Отображать сообщение только для последнего оставшегося эффекта скилла
			if(_count <= 0 && getEffected().getEffectList().getEffectsCountForSkill(getSkill().getId()) == 1)
				getEffected().sendPacket(new SystemMessage(SystemMessage.S1_HAS_WORN_OFF).addSkillName(_displayId, _displayLevel));

			// Stop the task of the L2Effect, remove it and update client magic icon
			stopEffectTask();

			if(getSkill().getDelayedEffect() > 0)
				SkillTable.getInstance().getInfo(getSkill().getDelayedEffect(), 1).getEffects(_effector, _effected, false, false);
		}
	}

	public void updateEffects()
	{
		_effected.updateStats();
	}

	public Func[] getStatFuncs()
	{
		if(_funcTemplates == null)
			return _emptyFunctionSet;
		Func[] funcs = new Func[_funcTemplates.length];
		for(int i = 0; i < funcs.length; i++)
		{
			Func f = _funcTemplates[i].getFunc(this); // effect is owner
			funcs[i] = f;
		}
		return funcs;
	}

	public void addIcon(AbnormalStatusUpdate mi)
	{
		if(_state != EffectState.ACTING || _displayId < 0)
			return;
		int duration = _skill.isToggle() ? AbnormalStatusUpdate.INFINITIVE_EFFECT : (int) (getTimeLeft() / 1000);
		mi.addEffect(_displayId, _displayLevel, duration);
	}

	public void addPartySpelledIcon(PartySpelled ps)
	{
		if(_state != EffectState.ACTING || _displayId < 0)
			return;
		int duration = _skill.isToggle() ? AbnormalStatusUpdate.INFINITIVE_EFFECT : (int) (getTimeLeft() / 1000);
		ps.addPartySpelledEffect(_displayId, _displayLevel, duration);
	}	
	
	public void addSpelledIcon(ExAbnormalStatusUpdateFromTargetPacket ps)
	{
		if(_state != EffectState.ACTING || _displayId < 0)
			return;
		int duration = _skill.isToggle() ? AbnormalStatusUpdate.INFINITIVE_EFFECT : (int) (getTimeLeft() / 1000);
		ps.addSpelledEffect(_displayId, _displayLevel, duration);
	}

	public void addOlympiadSpelledIcon(L2Player player, ExOlympiadSpelledInfo os)
	{
		if(_state != EffectState.ACTING || _displayId < 0)
			return;
		int duration = _skill.isToggle() ? AbnormalStatusUpdate.INFINITIVE_EFFECT : (int) (getTimeLeft() / 1000);
		os.addSpellRecivedPlayer(player);
		os.addEffect(_displayId, _displayLevel, duration);
	}

	protected int getLevel()
	{
		return _skill.getLevel();
	}

	public boolean containsStat(Stats stat)
	{
		if(_funcTemplates != null)
			for(int i = 0; i < _funcTemplates.length; i++)
				if(_funcTemplates[i]._stat == stat)
					return true;
		return false;
	}

	public EffectType getEffectType()
	{
		return _effectType;
	}

	public boolean isSkillMasteryEffect()
	{
		return _skillMastery;
	}

	@Override
	public int compareTo(L2Effect obj)
	{
		if(obj.equals(this))
			return 0;
		return 1;
	}

	public void removeNext()
	{
		_next = null;
	}

	public void scheduleNext(L2Effect e)
	{
		if(_next != null && e != null && _next.maybeScheduleNext(e) == 0)
			return;
		_next = e;
		if(_next != null && !_next.isInUse())
			_next.setInUse(true);
	}

	public L2Effect getNext()
	{
		return _next;
	}

	/**
	 * @returns 0 - игнорировать новый эффект, 1 - использовать новый эффект
	 */
	public int maybeScheduleNext(L2Effect newEffect)
	{
		//231
		if(newEffect.getStackOrder() < getStackOrder()) // новый эффект слабее
		{
			if(newEffect.getTimeLeft() <= getTimeLeft()) // новый эффект не длинее
				return 0; // новый эффект бесполезен

			scheduleNext(newEffect); // пробуем пристроить новый эффект в очередь

			return 0; // если пристроить новый эффект не удалось - сбрасываем за ненадобностью
		}

		if(newEffect.getStackOrder() == getStackOrder()) // эффекты равны
		{
			if(newEffect.getTimeLeft() <= getTimeLeft()) // новый эффект не длиннее
				return 0; // новый эффект бесполезен

			// Чистим все ненужные зашедуленные
			L2Effect next = this, previous = this;
			while((next = next.getNext()) != null)
			{
				if(newEffect.getTimeLeft() > next.getTimeLeft())
				{
					previous.scheduleNext(next.getNext());
					next.removeNext();
					next.exit();
					next = previous;
					continue;
				}
				previous = next;
			}
			// Присоединяем зашедуленные эффекты от старого к новому
			if(getNext() != null && !getNext().isEnded())
				newEffect.scheduleNext(getNext());
			// Отсоединяем зашедуленные от текущего
			removeNext();
			// Останавливаем текущий
			exit();
			return 1;
		}

		if(newEffect.getStackOrder() > getStackOrder())
		{
			// Если старый короче то просто остановить его
			if(newEffect.getTimeLeft() > getTimeLeft())
			{
				// наследуем зашедуленый старому если есть смысл
				if(getNext() != null && getNext().getTimeLeft() > newEffect.getTimeLeft())
				{
					newEffect.scheduleNext(getNext());
					removeNext();
				}
				exit();
				return 1;
			}
			// Если новый короче то зашедулить старый
			setActive(false);
			_effected.removeStatsOwner(this);
			_effected.getEffectList().removeFromList(this);
			newEffect.scheduleNext(this);
			return 1;
		}

		// сюда дойти не может
		return 1;
	}

	public AbnormalEffect getAbnormalEffect()
	{
		return _abnormalEffect;
	}

	public AbnormalEffect getAbnormalEffect2()
	{
		return _abnormalEffect2;
	}

	public boolean isSaveable()
	{
		return getTimeLeft() >= 15000 && getSkill().isSaveable();
	}

	public int getDisplayId()
	{
		return _displayId;
	}

	public int getDisplayLevel()
	{
		return _displayLevel;
	}

	@Override
	public String toString()
	{
		return "Skill: " + _skill + ", state: " + _state.name() + ", inUse: " + _inUse;
	}

	@Override
	public boolean isFuncEnabled()
	{
		return isInUse();
	}

	@Override
	public boolean overrideLimits()
	{
		return false;
	}

	public boolean isOffensive()
	{
		Boolean template = _template.getParam().getBool("isOffensive", null);
		if(template != null)
			return template;
		return getSkill().isOffensive();
	}
}
package l2rt.gameserver.skills.effects;

import l2rt.gameserver.model.EffectList;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Effect;
import l2rt.gameserver.skills.AbnormalEffect;
import l2rt.gameserver.skills.EffectType;
import l2rt.gameserver.skills.Env;
import l2rt.gameserver.skills.conditions.Condition;
import l2rt.gameserver.skills.funcs.FuncTemplate;
import l2rt.gameserver.templates.StatsSet;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

public final class EffectTemplate
{
	static Logger _log = Logger.getLogger(EffectTemplate.class.getName());

	public static final String NO_STACK = "none".intern();

	public Condition _attachCond;
	public final double _value;
	public final int _counter;
	public final long _period; // in milliseconds
	public AbnormalEffect _abnormalEffect;
	public AbnormalEffect _abnormalEffect2;

	public FuncTemplate[] _funcTemplates;
	public final EffectType _effectType;

	public final String _stackType;
	public final String _stackType2;
	public final String _stackType3;
	public final String _stackType4;
	public final String _stackType5;
	public final int _stackOrder;
	public final int _displayId;
	public final int _displayLevel;

	public final boolean _applyOnCaster;
	public final boolean _cancelOnAction;
	public final boolean _cancelOnDamage;

	public final StatsSet _paramSet;

	public EffectTemplate(StatsSet set)
	{
		_value = set.getDouble("value");
		_counter = set.getInteger("count", 1) < 0 ? Integer.MAX_VALUE : set.getInteger("count", 1);
		_period = Math.min(Integer.MAX_VALUE, 1000 * (set.getInteger("time", 1) < 0 ? Integer.MAX_VALUE : set.getInteger("time", 1)));
		_abnormalEffect = set.getEnum("abnormal", AbnormalEffect.class);
		_abnormalEffect2 = set.getEnum("abnormal2", AbnormalEffect.class);
		_stackType = set.getString("stackType", NO_STACK);
		_stackType2 = set.getString("stackType2", NO_STACK);
		_stackType3 = set.getString("stackType3", NO_STACK);
		_stackType4 = set.getString("stackType4", NO_STACK);
		_stackType5 = set.getString("stackType5", NO_STACK);
		_stackOrder = set.getInteger("stackOrder", _stackType == NO_STACK && _stackType2 == NO_STACK && _stackType3 == NO_STACK && _stackType4 == NO_STACK && _stackType5 == NO_STACK ? 1 : 0);
		_applyOnCaster = set.getBool("applyOnCaster", false);
		_cancelOnAction = set.getBool("cancelOnAction", false);
		_cancelOnDamage = set.getBool("cancelOnDamage", false);
		_displayId = set.getInteger("displayId", 0);
		_displayLevel = set.getInteger("displayLevel", 0);
		_effectType = set.getEnum("name", EffectType.class);
		_paramSet = set;
	}

	public L2Effect getEffect(Env env)
	{
		if(_attachCond != null && !_attachCond.test(env))
			return null;
		return _effectType.makeEffect(env, this);
	}

	public void attachCond(Condition c)
	{
		_attachCond = c;
	}

	public void attachFunc(FuncTemplate f)
	{
		if(_funcTemplates == null)
			_funcTemplates = new FuncTemplate[] { f };
		else
		{
			int len = _funcTemplates.length;
			FuncTemplate[] tmp = new FuncTemplate[len + 1];
			System.arraycopy(_funcTemplates, 0, tmp, 0, len);
			tmp[len] = f;
			_funcTemplates = tmp;
		}
	}

	public long getPeriod()
	{
		return _period;
	}

	public EffectType getEffectType()
	{
		return _effectType;
	}

	public L2Effect getSameByStackType(ConcurrentLinkedQueue<L2Effect> ef_list)
	{
		for(L2Effect ef : ef_list)
			if(ef != null && EffectList.checkStackType(ef._template, this))
				return ef;
		return null;
	}

	public L2Effect getSameByStackType(EffectList ef_list)
	{
		return getSameByStackType(ef_list.getAllEffects());
	}

	public L2Effect getSameByStackType(L2Character actor)
	{
		return getSameByStackType(actor.getEffectList().getAllEffects());
	}

	public StatsSet getParam()
	{
		return _paramSet;
	}
}
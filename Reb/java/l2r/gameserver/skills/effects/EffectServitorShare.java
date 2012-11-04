package l2r.gameserver.skills.effects;

import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Effect;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.Player;
import l2r.gameserver.stats.Env;
import l2r.gameserver.stats.Stats;
import l2r.gameserver.stats.funcs.Func;

public class EffectServitorShare extends Effect
{
	public class FuncShare extends Func
	{
		public FuncShare(Stats stat, int order, Object owner, double value)
		{
			super(stat, order, owner, value);
		}

		@Override
		public void calc(Env env)
		{
			env.value += env.character.getPlayer().calcStat(stat, stat.getInit()) * value;
		}
	}

	public EffectServitorShare(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		onActionTime();
	}

	@Override
	public void onExit()
	{
		super.onExit();
	}

	@Override
	public Func[] getStatFuncs()
	{
		return new Func[] 
		{ new Func(Stats.POWER_ATTACK, 64, this)
			{
				public void calc(Env env)
				{
					Player pc = env.character.getPlayer();
					if (pc != null)
					{
						GameObject target = env.character.getTarget();
						env.value += pc.getPAtk((Creature)((target != null) && (target.isPet()) ? target : null)) * 0.5D;
					}
				}
			}
			, new Func(Stats.POWER_DEFENCE, 64, this)
			{
				public void calc(Env env)
				{
					Player pc = env.character.getPlayer();
					if (pc != null)
					{
						GameObject target = env.character.getTarget();
						env.value += pc.getPDef((Creature)((target != null) && (target.isPet()) ? target : null)) * 0.5D;
					}
				}
			}
			, new Func(Stats.MAGIC_ATTACK, 64, this)
			{
				public void calc(Env env)
				{
					Player pc = env.character.getPlayer();
					if (pc != null)
					{
						GameObject target = env.character.getTarget();
						env.value += pc.getMAtk((Creature)((target != null) && (target.isPet()) ? target : null), env.skill) * 0.25D;
					}
				}
			}
			, new Func(Stats.MAGIC_DEFENCE, 64, this)
			{
				public void calc(Env env)
				{
					Player pc = env.character.getPlayer();
					if (pc != null)
					{
						GameObject target = env.character.getTarget();
						env.value += pc.getMDef((Creature)((target != null) && (target.isPet()) ? target : null), env.skill) * 0.25D;
					}
				}
			}
			, new Func(Stats.MAX_HP, 64, this)
			{
				public void calc(Env env)
				{
					Player pc = env.character.getPlayer();
					if (pc != null)
						env.value += pc.getMaxHp() * 0.1D;
				}
			}
			, new Func(Stats.MAX_HP, 64, this)
			{
				public void calc(Env env)
				{
					Player pc = env.character.getPlayer();
					if (pc != null)
						env.value += pc.getMaxMp() * 0.1D;
				}
			}
			, new Func(Stats.CRITICAL_BASE, 64, this)
			{
				public void calc(Env env)
				{
					Player pc = env.character.getPlayer();
					if (pc != null)
					{
						GameObject target = env.character.getTarget();
						env.value += pc.getCriticalHit((Creature)((target != null) && (target.isPet()) ? target : null), env.skill) * 0.2D;
					}
				}
			}
			, new Func(Stats.POWER_ATTACK_SPEED, 64, this)
			{
				public void calc(Env env)
				{
					Player pc = env.character.getPlayer();
					if (pc != null)
						env.value += pc.getPAtkSpd() * 0.1D;
				}
			}
			, new Func(Stats.MAGIC_ATTACK_SPEED, 64, this)
			{
				public void calc(Env env)
				{
					Player pc = env.character.getPlayer();
					if (pc != null)
						env.value += pc.getMAtkSpd() * 0.03D;
				}
			}
		};



		//FuncTemplate[] funcTemplates = getTemplate().getAttachedFuncs();
		//Func[] funcs = new Func[funcTemplates.length];
		//if(_effected.getEffectList().getEffectByType(EffectType.ServitorShare) == null)
		//{
		//for(int i = 0; i < funcs.length; i++)
		//{
		//	funcs[i] = new FuncShare(funcTemplates[i]._stat, funcTemplates[i]._order, this, funcTemplates[i]._value);
		//}
		//}
		//return funcs;
	}

	@Override
	protected boolean onActionTime()
	{
		return false;
	}
}
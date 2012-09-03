package l2rt.gameserver.skills.effects;

import l2rt.gameserver.ai.CtrlIntention;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Effect;
import l2rt.gameserver.model.L2Summon;
import l2rt.gameserver.skills.Env;
import l2rt.util.GArray;
import l2rt.util.Rnd;

public class EffectDiscord extends L2Effect
{
	public EffectDiscord(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean checkCondition()
	{
		int skilldiff = _effected.getLevel() - _skill.getMagicLevel();
		int lvldiff = _effected.getLevel() - _effector.getLevel();
		if(skilldiff > 10 || skilldiff > 5 && Rnd.chance(30) || Rnd.chance(Math.abs(lvldiff) * 2))
			return false;

		boolean multitargets = _skill.isAoE();

		if(!_effected.isMonster())
		{
			if(!multitargets)
				getEffector().sendPacket(Msg.THAT_IS_THE_INCORRECT_TARGET);
			return false;
		}

		if(_effected.isFearImmune() || _effected.isRaid())
		{
			if(!multitargets)
				getEffector().sendPacket(Msg.THAT_IS_THE_INCORRECT_TARGET);
			return false;
		}

		// Discord нельзя наложить на осадных саммонов
		if(_effected instanceof L2Summon && ((L2Summon) _effected).isSiegeWeapon())
		{
			if(!multitargets)
				getEffector().sendPacket(Msg.THAT_IS_THE_INCORRECT_TARGET);
			return false;
		}

		if(_effected.isInZonePeace())
		{
			if(!multitargets)
				getEffector().sendPacket(Msg.YOU_MAY_NOT_ATTACK_IN_A_PEACEFUL_ZONE);
			return false;
		}

		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		_effected.startConfused();
		onActionTime();
	}

	@Override
	public void onExit()
	{
		super.onExit();
		_effected.stopConfused();
		_effected.setWalking();
		_effected.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
	}

	@Override
	public boolean onActionTime()
	{
		GArray<L2Character> targetList = new GArray<L2Character>();

		for(L2Character character : _effected.getAroundCharacters(900, 200))
			if(character.isNpc() && character != getEffected())
				targetList.add(character);

		// if there is no target, exit function
		if(targetList.size() == 0)
			return true;

		// Choosing randomly a new target
		L2Character target = targetList.get(Rnd.get(targetList.size()));

		// Attacking the target
		_effected.setRunning();
		_effected.getAI().Attack(target, true, false);

		return false;
	}
}
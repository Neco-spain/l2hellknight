package l2rt.gameserver.skills.effects;

import javolution.util.FastMap;
import l2rt.extensions.listeners.MethodCollection;
import l2rt.extensions.listeners.reduceHp.ReduceCurrentHpListener;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Effect;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.skills.Env;

import java.util.Map.Entry;

public final class EffectCurseOfLifeFlow extends L2Effect
{
	private CurseOfLifeFlowListener _listener;

	private FastMap<Long, Integer> _damagers = new FastMap<Long, Integer>();

	public EffectCurseOfLifeFlow(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		_listener = new CurseOfLifeFlowListener();
		_effected.addMethodInvokeListener(MethodCollection.ReduceCurrentHp, _listener);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		_effected.removeMethodInvokeListener(MethodCollection.ReduceCurrentHp, _listener);
		_listener = null;
	}

	@Override
	public boolean onActionTime()
	{
		if(_effected.isDead())
			return false;

		for(Entry<Long, Integer> entry : _damagers.entrySet())
		{
			L2Character damager = L2ObjectsStorage.getAsCharacter(entry.getKey());
			if(damager == null || damager.isDead() || damager.isCurrentHpFull())
				continue;

			Integer damage = entry.getValue();
			if(damage == null || damage <= 0)
				continue;

			double max_heal = calc();
			double heal = Math.min(damage, max_heal);
			double newHp = Math.min(damager.getCurrentHp() + heal, damager.getMaxHp());

			damager.sendPacket(new SystemMessage(SystemMessage.S1_HPS_HAVE_BEEN_RESTORED).addNumber((long) (newHp - damager.getCurrentHp())));
			damager.setCurrentHp(newHp, false);
		}

		_damagers.clear();

		return true;
	}

	private class CurseOfLifeFlowListener extends ReduceCurrentHpListener
	{
		@Override
		public void onReduceCurrentHp(L2Character actor, double damage, L2Character attacker, L2Skill skill, boolean awake, boolean standUp, boolean directHp)
		{
			if(attacker == actor || attacker == _effected)
				return;
			Integer old_damage = _damagers.get(attacker.getStoredId());
			_damagers.put(attacker.getStoredId(), old_damage == null ? (int) damage : old_damage + (int) damage);
		}
	}
}
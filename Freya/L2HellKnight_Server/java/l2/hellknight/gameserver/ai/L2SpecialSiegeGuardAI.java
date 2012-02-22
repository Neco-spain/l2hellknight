package l2.hellknight.gameserver.ai;

import java.util.List;

import javolution.util.FastList;

import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.L2Character.AIAccessor;

public final class L2SpecialSiegeGuardAI extends L2SiegeGuardAI
{
	private List<Integer> _allied;

	public L2SpecialSiegeGuardAI(AIAccessor accessor)
	{
		super(accessor);
		_allied = new FastList<Integer>();
	}
	
	public List<Integer> getAlly()
	{
		return _allied;
	}
	
	@Override
	protected boolean autoAttackCondition(L2Character target)
	{
		if(_allied.contains(target.getObjectId()))
			return false;
		
		return super.autoAttackCondition(target);
	}
}
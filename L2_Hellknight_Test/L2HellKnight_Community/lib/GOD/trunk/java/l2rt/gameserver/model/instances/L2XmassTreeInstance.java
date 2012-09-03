package l2rt.gameserver.model.instances;

import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.templates.L2NpcTemplate;

public class L2XmassTreeInstance extends L2NpcInstance
{
	public L2XmassTreeInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public boolean isAttackable(L2Character attacker)
	{
		return false;
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}

	@Override
	public boolean hasRandomWalk()
	{
		return false;
	}

	@Override
	public boolean isFearImmune()
	{
		return true;
	}

	@Override
	public boolean isParalyzeImmune()
	{
		return true;
	}

	@Override
	public boolean isLethalImmune()
	{
		return true;
	}
}
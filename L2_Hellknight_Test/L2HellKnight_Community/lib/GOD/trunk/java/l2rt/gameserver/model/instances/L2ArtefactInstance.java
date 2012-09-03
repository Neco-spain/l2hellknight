package l2rt.gameserver.model.instances;

import l2rt.gameserver.ai.L2CharacterAI;
import l2rt.gameserver.ai.L2StaticObjectAI;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.templates.L2NpcTemplate;

public final class L2ArtefactInstance extends L2NpcInstance
{
	public boolean hasChatWindow = false;

	public L2ArtefactInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public L2CharacterAI getAI()
	{
		if(_ai == null)
			_ai = new L2StaticObjectAI(this);
		return _ai;
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}

	@Override
	public boolean isAttackable(L2Character attacker)
	{
		return false;
	}

	/**
	 * Артефакт нельзя убить
	 */
	@Override
	public void doDie(L2Character killer)
	{}

	@Override
	public boolean isInvul()
	{
		return true;
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
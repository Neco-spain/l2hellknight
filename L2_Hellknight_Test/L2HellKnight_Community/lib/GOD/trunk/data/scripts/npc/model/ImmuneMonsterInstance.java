package npc.model;

import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.templates.L2NpcTemplate;

public class ImmuneMonsterInstance extends L2MonsterInstance
{
	public ImmuneMonsterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
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
package npc.model;

import l2rt.gameserver.model.instances.L2ReflectionBossInstance;
import l2rt.gameserver.templates.L2NpcTemplate;

/**
 * Данный инстанс используется босами в Hall of Suffering.
 * Босов 2, очистку рефлекшена при смерти не делаем.
 * @author SYS
 */
public class HoSBossInstance extends L2ReflectionBossInstance
{
	public HoSBossInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	protected void clearReflection()
	{}

	@Override
	public boolean canChampion()
	{
		return false;
	}
}
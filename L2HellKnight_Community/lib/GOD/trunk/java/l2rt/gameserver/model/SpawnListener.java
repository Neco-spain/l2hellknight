package l2rt.gameserver.model;

import l2rt.gameserver.model.instances.L2NpcInstance;

public interface SpawnListener
{
	public void npcSpawned(L2NpcInstance npc);

	public void npcDeSpawned(L2NpcInstance npc);
}

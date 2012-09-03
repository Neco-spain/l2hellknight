package npc.model;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.templates.L2NpcTemplate;

/**
 * Данный инстанс используется NPC 13193 в локации Seed of Destruction
 * @author SYS
 */
public class FakeObeliskInstance extends L2NpcInstance
{
	public FakeObeliskInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onAction(L2Player player, boolean shift)
	{
		player.sendActionFailed();
	}
}
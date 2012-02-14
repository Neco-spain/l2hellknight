package l2rt.gameserver.model.instances;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.templates.L2NpcTemplate;

public class L2NoTargetNpcInstance extends L2NpcInstance
{
	public L2NoTargetNpcInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onAction(L2Player player, boolean shift)
	{
		player.sendActionFailed();
		return;
	}
	
	@Override
	public boolean isInvul()
	{
		return true;
	}


}
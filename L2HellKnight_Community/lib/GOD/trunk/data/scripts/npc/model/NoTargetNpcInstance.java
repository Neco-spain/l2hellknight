package npc.model;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.templates.L2NpcTemplate;

public class NoTargetNpcInstance extends L2NpcInstance
{

    public NoTargetNpcInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }

    public void onAction(L2Player player, boolean dontMove)
    {
        player.sendActionFailed();
    }
}

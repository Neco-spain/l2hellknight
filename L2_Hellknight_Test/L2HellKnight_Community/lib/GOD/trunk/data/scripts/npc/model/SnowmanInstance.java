package npc.model;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.templates.L2NpcTemplate;

public class SnowmanInstance extends L2NpcInstance
{

    public SnowmanInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }

    public void showChatWindow(L2Player player, int val)
    {
        player.sendActionFailed();
    }
}

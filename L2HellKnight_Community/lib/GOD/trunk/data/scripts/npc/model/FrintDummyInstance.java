package npc.model;

import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.templates.L2NpcTemplate;

public class FrintDummyInstance extends L2NpcInstance
{

    public FrintDummyInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }

    public void onAction(L2Player player, boolean dontMove)
    {
        player.sendActionFailed();
    }

    public boolean isAttackable()
    {
        return false;
    }

    public boolean isAutoAttackable(L2Character attacker)
    {
        return false;
    }
}

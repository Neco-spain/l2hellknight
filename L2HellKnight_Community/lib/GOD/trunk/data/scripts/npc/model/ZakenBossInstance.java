package npc.model;

import l2rt.gameserver.model.instances.L2ReflectionBossInstance;
import l2rt.gameserver.templates.L2NpcTemplate;

public class ZakenBossInstance extends L2ReflectionBossInstance
{

    public ZakenBossInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
        fail = 0;
    }

    public boolean isDay()
    {
        return getNpcId() == 29176;
    }

    public void incFail()
    {
        fail++;
    }

    public int getFail()
    {
        return fail;
    }

    private int fail;
}

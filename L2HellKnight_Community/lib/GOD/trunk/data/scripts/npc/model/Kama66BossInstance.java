package npc.model;

import l2rt.gameserver.model.instances.L2ReflectionBossInstance;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.util.Rnd;

public class Kama66BossInstance extends L2ReflectionBossInstance
{

    public Kama66BossInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }

    protected int getMaintenanceInterval()
    {
        return Rnd.get(60000, 0x2bf20);
    }

    protected int getKilledInterval()
    {
        return Rnd.get(60000, 0x2bf20);
    }
}

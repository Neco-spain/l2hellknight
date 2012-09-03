package npc.model;

import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.templates.L2NpcTemplate;

public class L2ooInstance extends L2MonsterInstance
{

    public L2ooInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
        _ColorHandyCube = 0;
    }

    public static void setColorHandyCubik(int color, L2MonsterInstance actor)
    {
        _ColorHandyCube = color;
    }

    public static int getColorHandyCubik()
    {
        return _ColorHandyCube;
    }

    private static int _ColorHandyCube;
}

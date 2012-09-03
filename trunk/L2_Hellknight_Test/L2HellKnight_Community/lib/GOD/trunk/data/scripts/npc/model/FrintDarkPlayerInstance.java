package npc.model;

import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2DoorInstance;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.tables.DoorTable;
import l2rt.gameserver.tables.SpawnTable;
import l2rt.gameserver.templates.L2NpcTemplate;

public class FrintDarkPlayerInstance extends L2MonsterInstance
{

    public FrintDarkPlayerInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }

    public void doDie(L2Character killer)
    {
        super.doDie(killer);
        super.doDie(killer);
        getSpawn().despawnAll();
        SpawnTable.getInstance().deleteSpawn(getSpawn());
        for(int doorId = 0x17fc26d; doorId <= 0x17fc276; doorId++)
        {
            L2DoorInstance door = DoorTable.getInstance().getDoor(Integer.valueOf(doorId));
            if(door != null && !door.isOpen())
                door.openMe();
        }

    }
}

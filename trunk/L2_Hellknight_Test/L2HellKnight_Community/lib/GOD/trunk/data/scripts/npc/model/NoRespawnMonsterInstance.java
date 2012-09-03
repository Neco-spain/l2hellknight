package npc.model;

import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.tables.SpawnTable;
import l2rt.gameserver.templates.L2NpcTemplate;

public class NoRespawnMonsterInstance extends L2MonsterInstance
{

    public NoRespawnMonsterInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }

    public void doDie(L2Character killer)
    {
        super.doDie(killer);
        getSpawn().despawnAll();
        SpawnTable.getInstance().deleteSpawn(getSpawn());
    }
}

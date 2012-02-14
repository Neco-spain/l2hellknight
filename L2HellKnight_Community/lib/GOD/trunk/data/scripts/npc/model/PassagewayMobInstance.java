package npc.model;

import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.templates.L2NpcTemplate;

public final class PassagewayMobInstance extends L2MonsterInstance
{

    public PassagewayMobInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }

    public void calculateRewards(L2Character lastAttacker)
    {
        if(lastAttacker == null)
            return;
        super.calculateRewards(lastAttacker);
        if(lastAttacker.isPlayable())
            dropItem(lastAttacker.getPlayer(), 9849, 1L);
    }

    public static final int FieryDemonBloodHerb = 9849;
}

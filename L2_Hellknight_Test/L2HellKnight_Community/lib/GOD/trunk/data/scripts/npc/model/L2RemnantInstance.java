package npc.model;

import l2rt.gameserver.instancemanager.HellboundManager;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.templates.L2NpcTemplate;

public class L2RemnantInstance extends L2MonsterInstance
{

    public L2RemnantInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }

    public void doDie(L2Character killer)
    {
        if(HellboundManager.getInstance().getLevel() == 2 && isBlessed())
            decayMe();
        super.doDie(killer);
    }

    public boolean isDead()
    {
        return false;
    }

    public boolean isBlessed()
    {
        return _isBlessed;
    }

    public void setBlessed(boolean blessed)
    {
        _isBlessed = blessed;
    }

    private boolean _isBlessed;
}

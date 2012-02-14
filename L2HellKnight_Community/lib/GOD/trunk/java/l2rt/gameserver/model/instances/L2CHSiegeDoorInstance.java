package l2rt.gameserver.model.instances;

import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.templates.L2CharTemplate;

public class L2CHSiegeDoorInstance extends L2DoorInstance
{

    public L2CHSiegeDoorInstance(int objectId, L2CharTemplate template, int doorId, String name, boolean unlockable, boolean showHp)
    {
        super(objectId, template, doorId, name, unlockable, showHp);
    }

    public void doDie(L2Character killer)
    {
        super.doDie(killer);
    }

    public boolean isInvul()
    {
        return false;
    }
}

package npc.model;

import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2MinionInstance;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.util.Rnd;

public class Kama66MinionInstance extends L2MinionInstance
{

    public Kama66MinionInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template, null);
    }

    public void reduceCurrentHp(double i, L2Character attacker, boolean awake, boolean standUp, boolean directHp)
    {
        if(_lastSpeach < System.currentTimeMillis() && Rnd.chance(60))
        {
            _lastSpeach = System.currentTimeMillis() + 60000L;
            Functions.npcShout(this, "Arg! The pain is more than I can stand!");
        }
        super.reduceCurrentHp(i, attacker, null, awake, standUp, directHp, directHp);
    }

    public void doDie(L2Character killer)
    {
        if(Rnd.chance(75))
            Functions.npcShout(this, "Ahh! How did he find my weakness?");
        super.doDie(killer);
    }

    private long _lastSpeach;
}

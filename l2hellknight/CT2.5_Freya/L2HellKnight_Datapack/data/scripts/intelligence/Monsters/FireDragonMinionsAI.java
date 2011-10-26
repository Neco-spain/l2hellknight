package intelligence.Monsters;

import l2.hellknight.Config;
import l2.hellknight.bflmpsvz.a.L2AttackableAIScript;
import l2.hellknight.gameserver.model.actor.L2Npc;

public class FireDragonMinionsAI extends L2AttackableAIScript
{
    public FireDragonMinionsAI(int id, String name, String descr)
    {
        super(id, name, descr);
        registerMobs(new int[]{29029});
    }

    public final String onSpawn(L2Npc npc)
    {
    	npc.setIsInvul(true);
        return super.onSpawn(npc);
    }

	public static void main(String[] args)
	{
		new FireDragonMinionsAI(-1, "FireDragonMinionsAI", "ai");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Monster: Pushbon AI");
	}
}

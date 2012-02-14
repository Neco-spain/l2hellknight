package ai;

import bosses.FreyaManager;
import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.ai.DefaultAI;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.network.serverpackets.ExStartScenePlayer;

public class Freya extends DefaultAI 
{
	private static final int FREYA_STAND = 29179;
	private static final int ARCHERY_KNIGHT = 18855;
	private static boolean kegorSpwn = false;
	
    public Freya(L2Character actor) 
	{
        super(actor);
        AI_TASK_DELAY = 1000;
        AI_TASK_ACTIVE_DELAY = 1000;
    }

    @Override
    protected boolean thinkActive() 
	{
        return super.thinkActive() || defaultThinkBuff(10);
    }

    @Override
    protected boolean createNewTask() 
	{
        return defaultFightTask();
    }

    @Override
    public void onEvtAttacked(L2Character attacker, int damage) 
	{
        L2NpcInstance npc = getActor();
        if (npc == null)
            return;
			
		final FreyaManager.FreyaInstanceInfo world = FreyaManager.instances.get(npc.getReflection().getId());
		
		switch(npc.getNpcId())
		{
			case FREYA_STAND:
	    		if(npc.getCurrentHp() < npc.getMaxHp() * 0.45000000000000001D && !kegorSpwn)
				{
				    kegorSpwn = true;
					ThreadPoolManager.getInstance().scheduleGeneral(new FreyaManager.StartWave(102,world), 2000);
				}
				break;
			case ARCHERY_KNIGHT:
	    		if(npc.isImobilised())
				{
	    			npc.setNpcState(2);
					npc.setImobilised(false);
				}
				break;
		}
        super.onEvtAttacked(attacker, damage);
    }

    @Override
    protected void onEvtSpawn() 
	{
        getActor().setNpcState(1);
    }
	
    @Override
    protected boolean maybeMoveToHome() 
	{
        L2NpcInstance actor = getActor();
        if (actor != null && !FreyaManager.getZone().checkIfInZone(actor))
            teleportHome(true);
        return false;
    }
}

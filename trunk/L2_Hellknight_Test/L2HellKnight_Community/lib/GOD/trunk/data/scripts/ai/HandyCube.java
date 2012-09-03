package ai;

import l2rt.gameserver.ai.DefaultAI;
import l2rt.gameserver.instancemanager.GameHandyManager;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.L2Object;
import l2rt.gameserver.network.serverpackets.NpcInfo;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.common.ThreadPoolManager;


public class HandyCube extends DefaultAI
{
	private class despawn implements Runnable
	{
		public void run()
		{
			L2MonsterInstance actor = (L2MonsterInstance) getActor();
			if(actor != null)
				actor.deleteMe();
		}
	}
    public HandyCube(L2Character actor)
    {
        super(actor);
        Red = 1;
        Blue = 0;
        //actor.setName(" ");
        actor.updateAbnormalEffect();
    }
	@Override
    protected void onEvtSeeSpell(L2Skill skill, L2Character caster)
    {
		
        L2MonsterInstance actor = (L2MonsterInstance) getActor();
		//Functions.npcSay(actor, "xyu");
        int arenaId = GameHandyManager.getPlayerArena(caster.getPlayer());
		//if(skill.getId() == 5853)
        if(actor.getChampion() == 1 && skill.getId() == 5853)
        {
            //caster.getPlayer()._HandyGamePoints++;
            //L2ooInstance.setColorHandyCubik(Red, actor);
			//Functions.npcSay(actor, "red");
			actor.setChampion(2);
			actor.broadcastPacket(new NpcInfo(actor, caster.getPlayer()));
			GameHandyManager.increaseKill(1, arenaId, caster.getPlayer());			
        } else
        if(actor.getChampion() == 2 && skill.getId() == 5852)
        {
            //caster.getPlayer()._HandyGamePoints++;
            //L2ooInstance.setColorHandyCubik(Blue, actor);
			//Functions.npcSay(actor, "blue");
			actor.setChampion(1);
			actor.broadcastPacket(new NpcInfo(actor, caster.getPlayer()));
			//actor.broadcastPacket(new L2Update(actor, caster.getPlayer()));			
            GameHandyManager.increaseKill(2, arenaId, caster.getPlayer());
        }
        actor.broadcastPacket(new NpcInfo(actor, caster.getPlayer()));
		//actor.broadcastPacket(new L2Update(actor, caster.getPlayer()));
    }
    public static boolean contains(L2Object objects[], L2NpcInstance npc)
    {
        L2Object arr$[] = objects;
        int len$ = arr$.length;
        for(int i$ = 0; i$ < len$; i$++)
        {
            L2Object obj = arr$[i$];
            if(obj == npc)
                return true;
        }

        return false;
    }
	@Override
	public boolean isGlobalAI()
	{
		return true;
	}
	@Override
	protected void onEvtSpawn()
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new despawn(), 300000L);
	}	
    int Red;
    int Blue;
}
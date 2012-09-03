package ai;

import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.instancemanager.KrateisCubeManager;
import l2rt.gameserver.serverpackets.ExShowScreenMessage;
import l2rt.gameserver.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.ai.Fighter;

public class Krateis extends Fighter
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
    public Krateis(L2Character actor)
    {
        super(actor);
    }
	@Override
	public boolean isGlobalAI()
	{
		return true;
	}
	@Override
	protected void onEvtSpawn()
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new despawn(), 1200000); //test run - set event time!
	}		
	@Override	
	protected void onEvtDead(L2Character killer)
	{
		KrateisCubeManager.getInstance().addKill(killer);
		String text = killer.getName()+": You gained"+ KrateisCubeManager.getInstance().getKills(killer);
		killer.sendPacket(new ExShowScreenMessage(text, 5000, ScreenMessageAlign.TOP_CENTER, true));
		super.onEvtDead(killer);
	}	
}
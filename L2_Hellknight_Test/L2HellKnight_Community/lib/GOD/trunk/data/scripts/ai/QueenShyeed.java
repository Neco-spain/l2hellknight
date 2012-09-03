package ai;

import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.instancemanager.ZoneManager;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Zone;
import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.model.instances.L2NpcInstance;

/**
*	AI Queen Shyeed для Stakato Nest
*	Накладывает баф\дебаф в зоне. Взависимости от того жив рб или нет.
* 	Author: Drizzy
*	Date: 19.08.10
*/
public class QueenShyeed extends Fighter
{	
	private L2Zone _zone;
	private L2Zone _zone1;
	
	public QueenShyeed(L2Character actor)
	{
		super(actor);
	}

	public boolean isGlobalAI()
	{
		return true;
	}
	
	public final L2Zone getZoneBuff()
	{
		if (_zone == null)
			_zone = ZoneManager.getInstance().getZoneById(ZoneType.other, 999222, true);
		return _zone;
	}

	
	public final L2Zone getZoneDebuff()
	{
		if (_zone1 == null)
			_zone1 = ZoneManager.getInstance().getZoneById(ZoneType.damage, 999223, false);
		return _zone1;
	}
	
	@Override
	protected boolean thinkActive()
	{
		return super.thinkActive();
	}
	
	@Override
	protected void onEvtSpawn()
	{
		// при спауне запускаем таймер
		ThreadPoolManager.getInstance().scheduleGeneral(new activeZone(), 10000);
		return;
	}
	
	@Override
	protected void onEvtDead(L2Character killer)
	{
		// при смерте меняет зоны (с бафа на дебаф).
		getZoneDebuff().setActive(false);
		getZoneBuff().setActive(true);
		super.onEvtDead(killer);
	}
	
	private class activeZone implements Runnable
	{
		public void run()
		{
			L2NpcInstance actor = getActor();
			if(actor == null || actor.isDead())
				return;

			// меняем активность зоны
			getZoneBuff().setActive(false);
			getZoneDebuff().setActive(true);		
		}
	}
}
package ai;

import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.instancemanager.RaidBossSpawnManager;
import l2rt.gameserver.instancemanager.RaidBossSpawnManager.Status;
import l2rt.gameserver.instancemanager.ZoneManager;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Zone;
import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.model.instances.L2NpcInstance;

public class StakatoShyeed extends Fighter
{
	private final static int _playerDebuffZone = 100500;
	private final static int _npcBuffZone = 100501;
	private final static int _playerBuffZone = 100502;
	private static boolean _zoneDisabled = false;

	public StakatoShyeed(L2Character actor)
	{
		super(actor);
	}

	@Override
	public boolean isGlobalAI()
	{
		return true;
	}

	@Override
	protected boolean thinkActive()
	{
		L2NpcInstance actor = getActor();

		if(actor == null || actor.isDead())
			return true;

		if (!_zoneDisabled && RaidBossSpawnManager.getInstance().getRaidBossStatusId(actor.getNpcId()) == Status.ALIVE)
		{
			switchZone(false, _playerBuffZone);
			switchZone(true, _playerDebuffZone);
			switchZone(true, _npcBuffZone);
		}
		return super.thinkActive();
	}
	
	@Override
	protected void onEvtDead(L2Character killer)
	{
		switchZone(true, _playerBuffZone);
		switchZone(false, _playerDebuffZone);
		switchZone(false, _npcBuffZone);
		super.onEvtDead(killer);
	}
	
	private void switchZone(boolean zoneMode, int zoneId)
	{
		L2Zone _zone = ZoneManager.getInstance().getZoneById(ZoneType.other, zoneId, false);
		if(_zone != null && _zone.isActive() != zoneMode)
		{
			_zone.setActive(zoneMode);
			if (zoneId == _playerBuffZone)
				_zoneDisabled = !zoneMode;
		}
	}
}
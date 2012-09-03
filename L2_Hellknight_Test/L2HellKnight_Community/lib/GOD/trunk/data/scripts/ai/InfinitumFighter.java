package ai;

import javolution.util.FastMap;
import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.instancemanager.ZoneManager;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Object;
import l2rt.gameserver.model.L2Party;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Zone;
import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.util.Location;
import l2rt.util.Rnd;

/**
 * AI моба-воина для Tower of Infinitum.<br>
 * При атаке имеет небольшой шанс портануть партию вверх или вниз на один этаж.
 * @author SYS
 */
public class InfinitumFighter extends Fighter
{
	private static FastMap<Integer, L2Zone> _floorZones = null;
	private int _currentFloor;
	private static final int ZONE_OFFSET = 705000;

	public InfinitumFighter(L2Character actor)
	{
		super(actor);
		_currentFloor = getCurrentFloor(actor);
	}

	private static int getCurrentFloor(L2Object o)
	{
		if(_floorZones == null)
		{
			_floorZones = new FastMap<Integer, L2Zone>(10);
			for(int i = 1; i < 11; i++)
				_floorZones.put(i, ZoneManager.getInstance().getZoneById(ZoneType.dummy, ZONE_OFFSET + i, true));
		}
		for(L2Zone floor : _floorZones.values())
			if(floor.checkIfInZone(o))
				return floor.getId() - ZONE_OFFSET;
		return 0;
	}

	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{
		L2NpcInstance actor = getActor();
		if(actor == null || attacker == null)
			return;

		// С 5 и 10 этажей никуда не портает, и вообще там этих мобов быть не должно.
		if(_currentFloor == 5 || _currentFloor == 10)
		{
			super.onEvtAttacked(attacker, damage);
			return;
		}

		if(_currentFloor == 0)
		{
			_currentFloor = getCurrentFloor(actor);
			super.onEvtAttacked(attacker, damage);
			return;
		}

		Location teleToPoint = null;
		L2Zone floor = null;
		int chance = Rnd.get(70000);

		// Портает вниз (c 1 и 6 вниз не портает)
		if(chance <= 5 && _currentFloor != 1 && _currentFloor != 6)
			floor = _floorZones.get(_currentFloor - 1);
		// Портает вверх
		else if(chance < 20 && chance > 5)
			floor = _floorZones.get(_currentFloor + 1);

		if(floor != null)
		{
			teleToPoint = floor.getSpawn();
			L2Party party = attacker.getPlayer().getParty();

			if(teleToPoint != null)
				if(party != null)
				{
					for(L2Player member : party.getPartyMembers())
						if(member != null && _currentFloor == getCurrentFloor(member))
							member.teleToLocation(teleToPoint);
				}
				else
					attacker.teleToLocation(teleToPoint);
		}

		super.onEvtAttacked(attacker, damage);
	}
}
package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.model.zone.L2ZoneType;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class L2OlympiadStadiumZone extends L2ZoneType
{
	private int _stadiumId;

	public L2OlympiadStadiumZone(int id)
	{
		super(id);
	}

	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("stadiumId"))
		{
			_stadiumId = Integer.parseInt(value);
		}
		else super.setParameter(name, value);
	}

	@Override
	protected void onEnter(L2Character character)
	{
		character.setInsideZone(L2Character.ZONE_PVP, true);
		character.setInsideZone(L2Character.ZONE_OLY, true);
		//wyvern exploit fix
		character.setInsideZone(L2Character.ZONE_NOLANDING, false);

		if (character instanceof L2PcInstance)
		{
			((L2PcInstance)character).sendPacket(new SystemMessage(SystemMessageId.ENTERED_COMBAT_ZONE));
		}
		// Fix against exploit for Olympiad zone during oly period
		if(character instanceof L2PcInstance && Olympiad.getInstance().inCompPeriod() && !((L2PcInstance) character).isInOlympiadMode() && !((L2PcInstance) character).inObserverMode())
			{
				oustAllPlayers();
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(L2Character.ZONE_PVP, false);
		character.setInsideZone(L2Character.ZONE_OLY, false);

		if (character instanceof L2PcInstance)
		{
			((L2PcInstance)character).sendPacket(new SystemMessage(SystemMessageId.LEFT_COMBAT_ZONE));
		}
	}

	@Override
	protected void onDieInside(L2Character character) {}

	@Override
	protected void onReviveInside(L2Character character) {}
	
	public void oustAllPlayers()
		{
			if(_characterList == null)
				return;
	
			if(_characterList.isEmpty())
				return;
	
			for(L2Character character : _characterList.values())
			{
				if(character == null)
				{
					continue;
				}
	
				if(character instanceof L2PcInstance)
				{
					L2PcInstance player = (L2PcInstance) character;
					if(player.isOnline() == 1 && !player.isGM() && Olympiad.getInstance().inCompPeriod() && !player.inObserverMode() && !player.isInOlympiadMode())
					{
						player.teleToLocation(MapRegionTable.TeleportWhereType.Town);
					}
					player = null;
				}
			}
		}
	
		/**
		 * Returns this zones stadium id (if any)
		 *
		 * @return
		 */

	public int getStadiumId()
	{
		return _stadiumId;
	}
}

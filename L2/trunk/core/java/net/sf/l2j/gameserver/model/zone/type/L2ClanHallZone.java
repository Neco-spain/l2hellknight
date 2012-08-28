package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.model.zone.L2ZoneType;
import net.sf.l2j.gameserver.network.serverpackets.ClanHallDecoration;

public class L2ClanHallZone extends L2ZoneType
{
	private int _clanHallId;
	private int[] _spawnLoc;

	public L2ClanHallZone(int id)
	{
		super(id);

		_spawnLoc = new int[3];
	}

	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("clanHallId"))
		{
			_clanHallId = Integer.parseInt(value);
			ClanHallManager.getInstance().getClanHallById(_clanHallId).setZone(this);
		}
		else if (name.equals("spawnX"))
		{
			_spawnLoc[0] = Integer.parseInt(value);
		}
		else if (name.equals("spawnY"))
		{
			_spawnLoc[1] = Integer.parseInt(value);
		}
		else if (name.equals("spawnZ"))
		{
			_spawnLoc[2] = Integer.parseInt(value);
		}
		else super.setParameter(name, value);
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			character.setInsideZone(L2Character.ZONE_CLANHALL, true);

			ClanHall clanHall = ClanHallManager.getInstance().getClanHallById(_clanHallId);
			if (clanHall == null) return;

			ClanHallDecoration deco = new ClanHallDecoration(clanHall);
			((L2PcInstance)character).sendPacket(deco);

			if (clanHall.getOwnerId() != 0 && clanHall.getOwnerId() == ((L2PcInstance)character).getClanId())
				((L2PcInstance)character).sendMessage("You have entered your clan hall");
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			character.setInsideZone(L2Character.ZONE_CLANHALL, false);
			if (((L2PcInstance)character).getClanId() != 0
					&& ClanHallManager.getInstance().getClanHallById(_clanHallId).getOwnerId() == ((L2PcInstance)character).getClanId())
				((L2PcInstance)character).sendMessage("You have left your clan hall");
		}
	}

	@Override
	protected void onDieInside(L2Character character) {}

	@Override
	protected void onReviveInside(L2Character character) {}

	public void banishForeigners(int owningClanId)
	{
		for (L2Character temp : _characterList.values())
		{
			if(!(temp instanceof L2PcInstance)) continue;
			if (((L2PcInstance)temp).getClanId() == owningClanId) continue;

			((L2PcInstance)temp).teleToLocation(MapRegionTable.TeleportWhereType.Town);
		}
	}

	public Location getSpawn()
	{
		return new Location(_spawnLoc[0], _spawnLoc[1], _spawnLoc[2]);
	}
}

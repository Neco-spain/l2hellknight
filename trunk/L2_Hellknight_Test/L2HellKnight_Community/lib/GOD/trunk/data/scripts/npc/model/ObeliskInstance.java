package npc.model;

import java.util.HashMap;

import l2rt.gameserver.geodata.GeoControl;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.idfactory.IdFactory;
import l2rt.gameserver.model.L2RoundTerritory;
import l2rt.gameserver.model.L2Territory;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.templates.L2NpcTemplate;

public class ObeliskInstance extends L2MonsterInstance implements GeoControl
{
	public ObeliskInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		int id = IdFactory.getInstance().getNextId();
		L2Territory pos = new L2RoundTerritory(id, -245825, 217075, 230, -12208, -12000);
		setGeoPos(pos);
		GeoEngine.applyControl(this);
	}

	private L2Territory geoPos;
	private HashMap<Long, Byte> geoAround;

	public L2Territory getGeoPos()
	{
		return geoPos;
	}

	public void setGeoPos(L2Territory value)
	{
		geoPos = value;
	}

	public HashMap<Long, Byte> getGeoAround()
	{
		return geoAround;
	}

	public void setGeoAround(HashMap<Long, Byte> value)
	{
		geoAround = value;
	}

	@Override
	public boolean canChampion()
	{
		return false;
	}

	@Override
	public boolean isGeoCloser()
	{
		return false;
	}

	@Override
	public boolean isFearImmune()
	{
		return true;
	}

	@Override
	public boolean isParalyzeImmune()
	{
		return true;
	}

	@Override
	public boolean isLethalImmune()
	{
		return true;
	}
}
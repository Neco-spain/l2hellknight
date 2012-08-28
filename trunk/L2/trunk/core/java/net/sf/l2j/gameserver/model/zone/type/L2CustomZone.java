package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.instancemanager.VanHalterManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.zone.L2ZoneType;

public class L2CustomZone extends L2ZoneType
{
	private String _zoneName;
	private boolean _IsFlyingEnable = true;

	public L2CustomZone(int id)
	{
		super(id);
	}

	@Override
	protected void onDieInside(L2Character character) {}

	@Override
	protected void onReviveInside(L2Character character) {}

	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("name"))
		{
			_zoneName = value;
		}
		else if (name.equals("flying"))
		{
			_IsFlyingEnable = Boolean.parseBoolean(value);
		}
		else super.setParameter(name, value);
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
	        if (!((L2PcInstance)character).isGM() && ((L2PcInstance)character).isFlying() && !((L2PcInstance)character).isInJail() && !_IsFlyingEnable)
	        	((L2PcInstance)character).teleToLocation(MapRegionTable.TeleportWhereType.Town);

	        if (_zoneName.equalsIgnoreCase("AltarofSacrifice"))
	        	VanHalterManager.getInstance().intruderDetection((L2PcInstance)character);
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
		}
	}
	public String getZoneName()
	{
		return _zoneName;
	}

	public boolean isFlyingEnable()
	{
		return _IsFlyingEnable;
	}
}

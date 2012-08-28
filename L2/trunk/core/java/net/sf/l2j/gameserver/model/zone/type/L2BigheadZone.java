package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.zone.L2ZoneType;

public class L2BigheadZone extends L2ZoneType
{
	public L2BigheadZone(int id)
	{
		super(id);
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			if(character instanceof L2PcInstance)
                ((L2PcInstance)character).enterDangerArea();
			character.startAbnormalEffect(0x2000);
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			if(character instanceof L2PcInstance)
                ((L2PcInstance)character).exitDangerArea();
			character.stopAbnormalEffect((short)0x2000);
		}
	}

	@Override
	protected void onDieInside(L2Character character)
	{
		onExit(character);
	}

	@Override
	protected void onReviveInside(L2Character character)
	{
		onEnter(character);
	}

}

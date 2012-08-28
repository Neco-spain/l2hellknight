package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.zone.L2ZoneType;
import net.sf.l2j.gameserver.network.serverpackets.NpcInfo;

public class L2WaterZone extends L2ZoneType
{
	public L2WaterZone(int id)
	{
		super(id);
	}

	@Override
	protected void onEnter(L2Character character)
	{
		character.setInsideZone(L2Character.ZONE_WATER, true);

		if (character instanceof L2PcInstance)
		{
			((L2PcInstance) character).broadcastUserInfo();
		}
		else if (character instanceof L2NpcInstance)
		{
            for (L2PcInstance player : character.getKnownList().getKnownPlayers().values())
                if (player != null)
                    player.sendPacket(new NpcInfo((L2NpcInstance)character, player));
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(L2Character.ZONE_WATER, false);
		if (character instanceof L2PcInstance)
        {
		    ((L2PcInstance) character).broadcastUserInfo();
        }
        else if (character instanceof L2NpcInstance)
        {
            for (L2PcInstance player : character.getKnownList().getKnownPlayers().values())
                if (player != null)
                    player.sendPacket(new NpcInfo((L2NpcInstance)character, player));
        }
	}


	@Override
	protected void onDieInside(L2Character character) {}

	@Override
	protected void onReviveInside(L2Character character) {}

	public int getWaterZ()
	{
		return getZone().getHighZ();
	}
}

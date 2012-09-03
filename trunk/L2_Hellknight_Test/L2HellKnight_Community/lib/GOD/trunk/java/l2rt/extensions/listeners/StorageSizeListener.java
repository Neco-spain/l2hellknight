package l2rt.extensions.listeners;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.serverpackets.ExStorageMaxCount;
import l2rt.gameserver.skills.Env;
import l2rt.gameserver.skills.Stats;

public class StorageSizeListener extends StatsChangeListener
{
	public StorageSizeListener(Stats stat)
	{
		super(stat);
	}

	@Override
	public void statChanged(Double oldValue, double newValue, double baseValue, Env env)
	{
		_calculator._character.sendPacket(new ExStorageMaxCount((L2Player) _calculator._character));
	}
}
package instances;

import l2p.commons.util.Rnd;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.Reflection;
import l2p.gameserver.serverpackets.components.NpcString;
import l2p.gameserver.serverpackets.ExSendUIEvent;
import l2p.gameserver.utils.Location;
import l2p.gameserver.utils.ReflectionUtils;

/**
 * Класс контролирует дневного Закена
 *
 * @author pchayka
 */

public class ZakenDay extends Reflection
{
	private static final int Anchor = 32468;
	private static Location[] zakenTp = {new Location(55272, 219080, -2952), new Location(55272, 219080, -3224), new Location(55272, 219080, -3496),};
	private long _savedTime;

	@Override
	protected void onCreate()
	{
		super.onCreate();
		addSpawnWithoutRespawn(Anchor, zakenTp[Rnd.get(zakenTp.length)], 0);
		_savedTime = System.currentTimeMillis();
	}

	@Override
	public void onPlayerEnter(Player player)
	{
		super.onPlayerEnter(player);
		player.sendPacket(new ExSendUIEvent(player, false, true, (int) (System.currentTimeMillis() - _savedTime) / 1000, 0, NpcString.ELAPSED_TIME));
	}

	@Override
	public void onPlayerExit(Player player)
	{
		super.onPlayerExit(player);
		player.sendPacket(new ExSendUIEvent(player, true, true, 0, 0));
	}
}
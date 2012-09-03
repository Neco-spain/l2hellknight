package ai;

import l2rt.Config;
import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.ai.DefaultAI;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.network.serverpackets.MagicSkillUse;
import l2rt.gameserver.tables.NpcTable;
import l2rt.util.Location;
import l2rt.util.Rnd;

/**
 * Master Toma, телепортируется раз в 30 минут по 3м разным точкам гномьего острова.
 *
 * @author SYS
 */
public class Toma extends DefaultAI
{
	private Location[] _points = { new Location(151680, -174891, -1807, 41400), new Location(154153, -220105, -3402),
			new Location(178834, -184336, -352) };
	private static long TELEPORT_PERIOD = 30 * 60 * 1000; // 30 min
	private long _lastTeleport = System.currentTimeMillis();

	public Toma(L2Character actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		if(System.currentTimeMillis() - _lastTeleport < TELEPORT_PERIOD)
			return false;

		L2NpcInstance _thisActor = getActor();

		Location loc = _points[Rnd.get(_points.length)];
		if(_thisActor.getLoc().equals(loc))
			return false;

		_thisActor.broadcastPacketToOthers(new MagicSkillUse(_thisActor, _thisActor, 4671, 1, 1000, 0));
		ThreadPoolManager.getInstance().scheduleGeneral(new Teleport(loc), 1000);
		_lastTeleport = System.currentTimeMillis();

		return true;
	}

	public static void onLoad()
	{
		if(Config.ALT_TELEPORTING_TOMA)
		{
			for(L2NpcInstance i : L2ObjectsStorage.getAllByNpcId(30556, false))
				i.setAI(new Toma(i));
			NpcTable.getTemplate(30556).ai_type = "Toma";
		}
	}

	@Override
	public boolean isGlobalAI()
	{
		return true;
	}
}
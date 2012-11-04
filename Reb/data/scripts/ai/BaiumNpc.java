package ai;

import java.util.List;

import l2r.gameserver.ai.DefaultAI;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.Earthquake;
import l2r.gameserver.network.serverpackets.L2GameServerPacket;

public class BaiumNpc extends DefaultAI
{
	private long _wait_timeout = 0;
	private static final int BAIUM_EARTHQUAKE_TIMEOUT = 1000 * 60 * 15; // 15 мин

	public BaiumNpc(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	public boolean isGlobalAI()
	{
		return true;
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		// Пора устроить землятрясение
		if(_wait_timeout < System.currentTimeMillis())
		{
			_wait_timeout = System.currentTimeMillis() + BAIUM_EARTHQUAKE_TIMEOUT;
			L2GameServerPacket eq = new Earthquake(actor.getLoc(), 40, 10);
			List<Creature> chars = actor.getAroundCharacters(5000, 10000);
			for(Creature character : chars)
				if(character.isPlayer())
					character.sendPacket(eq);
		}
		return false;
	}

	@Override
	protected boolean randomWalk()
	{
		return false;
	}
}
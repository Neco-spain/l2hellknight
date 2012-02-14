package l2rt.gameserver.ai;

import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2World;
import l2rt.gameserver.model.instances.L2RaceManagerInstance;
import l2rt.gameserver.network.serverpackets.MonRaceInfo;
import l2rt.util.GArray;

public class RaceManager extends DefaultAI
{
	private Boolean thinking = false; // to prevent recursive thinking
	GArray<Long> _knownPlayers = new GArray<Long>();

	public RaceManager(L2Character actor)
	{
		super(actor);
		AI_TASK_DELAY = 5000;
	}

	@Override
	public void run()
	{
		onEvtThink();
	}

	@Override
	protected void onEvtThink()
	{
		L2RaceManagerInstance actor = getActor();
		if(actor == null)
			return;

		MonRaceInfo packet = actor.getPacket();
		if(packet == null)
			return;

		synchronized (thinking)
		{
			if(thinking)
				return;
			thinking = true;
		}

		try
		{
			GArray<Long> newPlayers = new GArray<Long>();
			for(L2Player player : L2World.getAroundPlayers(actor, 1200, 200))
			{
				if(player == null)
					continue;
				newPlayers.add(player.getStoredId());
				if(!_knownPlayers.contains(player.getStoredId()))
					player.sendPacket(packet);
				_knownPlayers.remove(player.getStoredId());
			}

			L2Player player;
			for(Long playerStoreId : _knownPlayers)
				if((player = L2ObjectsStorage.getAsPlayer(playerStoreId)) != null)
					actor.removeKnownPlayer(player);

			_knownPlayers = newPlayers;
		}
		finally
		{
			// Stop thinking action
			thinking = false;
		}
	}

	@Override
	public L2RaceManagerInstance getActor()
	{
		return (L2RaceManagerInstance) super.getActor();
	}
}
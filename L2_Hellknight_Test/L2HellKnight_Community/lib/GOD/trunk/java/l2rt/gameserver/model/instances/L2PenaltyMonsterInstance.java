package l2rt.gameserver.model.instances;

import l2rt.gameserver.ai.CtrlEvent;
import l2rt.gameserver.ai.CtrlIntention;
import l2rt.gameserver.network.clientpackets.Say2C;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.serverpackets.Say2;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.util.Rnd;

public class L2PenaltyMonsterInstance extends L2MonsterInstance
{
	private long ptkStoreId;

	public L2PenaltyMonsterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public L2Character getMostHated()
	{
		L2Player p = getPtk();
		L2Character p2 = super.getMostHated();
		if(p == null)
			return p2;
		if(p2 == null)
			return p;
		return getDistance3D(p) > getDistance3D(p2) ? p2 : p;
	}

	public void SetPlayerToKill(L2Player ptk)
	{
		setPtk(ptk);
		if(Rnd.chance(80))
			broadcastPacket(new Say2(getObjectId(), Say2C.ALL, getName(), "mmm your bait was delicious"));
		getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, ptk, 10);
		getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, ptk);
	}

	@Override
	public void doDie(L2Character killer)
	{
		if(Rnd.chance(75))
		{
			Say2 cs = new Say2(getObjectId(), Say2C.ALL, getName(), "I will tell fishes not to take your bait");
			broadcastPacket(cs);
		}
		super.doDie(killer);
	}

	public L2Player getPtk()
	{
		return L2ObjectsStorage.getAsPlayer(ptkStoreId);
	}

	public void setPtk(L2Player ptk)
	{
		ptkStoreId = ptk.getStoredId();
	}
}
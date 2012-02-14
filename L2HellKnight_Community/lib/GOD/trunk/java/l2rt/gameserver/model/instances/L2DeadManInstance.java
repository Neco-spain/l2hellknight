package l2rt.gameserver.model.instances;

import l2rt.gameserver.ai.L2CharacterAI;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.network.serverpackets.Die;
import l2rt.gameserver.templates.L2NpcTemplate;

public class L2DeadManInstance extends L2MonsterInstance
{
	public L2DeadManInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	/**
	 * Return the L2CharacterAI of the L2Character and if its null create a new one.<BR><BR>
	 */
	@Override
	public L2CharacterAI getAI()
	{
		if(_ai == null)
			_ai = new L2CharacterAI(this);
		return _ai;
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		setCurrentHp(0, false);
		setDead(true);
		broadcastStatusUpdate();
		broadcastPacket(new Die(this));
		setWalking();
	}

	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, L2Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect)
	{}

	@Override
	public void doDie(L2Character killer)
	{}

	@Override
	public int getAggroRange()
	{
		return 0;
	}
}
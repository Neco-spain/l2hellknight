package net.sf.l2j.gameserver.ai.special;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2AttackableAIScript;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.NpcSay;
import net.sf.l2j.util.Rnd;

public class Monastery extends L2AttackableAIScript
{
	public Monastery(int questId, String name, String descr)
	{
		super(questId, name, descr);
		int[] mobs = {22124, 22125, 22126, 22127, 22129};
		this.registerMobs(mobs);
	}

	private static boolean _isAttacked	= false;

	public String onAttack (L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (npc.getNpcId() == 22129 && _isAttacked == false && Rnd.get(100) < 50)
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), "Brother " + attacker.getName() + ", move your weapon away!!"));

		_isAttacked	= true;

		return super.onAttack (npc, attacker, damage, isPet);
	}

	public String onAggroRangeEnter(L2NpcInstance npc, L2PcInstance player, boolean isPet)
	{
		L2Character target = isPet ? player.getPet().getOwner() : player;
		if ((player.getActiveWeaponInstance() != null) && (!(player.isSilentMoving())) && (!(player.isGM())) && (!(player.getAppearance().getInvisible())))
		{
			if (npc.getNpcId() == 22129)
			{
				npc.setTarget(player);
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), "Brother " + target.getName() + ", move your weapon away!!"));
			}
			else
			{
				npc.setTarget(player);
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), "You cannot carry a weapon without authorization!"));
			((L2Attackable) npc).addDamageHate(target, 0, 999);
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
		}
		else if (npc.getMaxHp() == npc.getCurrentHp())
		{
			((L2Attackable) npc).getAggroListRP().remove(target);
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
		}
		return null;
	}
}
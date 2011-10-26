package intelligence.Monsters;

import java.util.Collection;

import l2.hellknight.Config;
import l2.hellknight.gameserver.GeoData;
import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.model.L2Object;
import l2.hellknight.gameserver.model.actor.L2Attackable;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2MonsterInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.network.clientpackets.Say2;
import l2.hellknight.gameserver.network.serverpackets.CreatureSay;

import l2.hellknight.bflmpsvz.a.L2AttackableAIScript;

public class GiantScouts extends L2AttackableAIScript
{
	final private static int _scouts[] = { 22668, 22669 };
	
	public GiantScouts(int questId, String name, String descr)
	{
		super(questId, name, descr);
		for (int id : _scouts)
			addAggroRangeEnterId(id);
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		L2Character target = isPet ? player.getPet() : player;
		
		if(GeoData.getInstance().canSeeTarget(npc, target))
		{
			if (!npc.isInCombat() && npc.getTarget() == null)
				npc.broadcastPacket(new CreatureSay(npc.getObjectId(), Say2.SHOUT, npc.getName(), "Oh Giants, an intruder has been discovered."));
			
			npc.setTarget(target);
			npc.setRunning();
			((L2Attackable) npc).addDamageHate(target, 0, 999);
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			
			// Notify clan
			Collection<L2Object> objs = npc.getKnownList().getKnownObjects().values();
			for(L2Object obj : objs)
			{
				if (obj != null)
				{
					if (obj instanceof L2MonsterInstance)
					{
						L2MonsterInstance monster = (L2MonsterInstance) obj;
						if (( npc.getClan() != null && monster.getClan() != null) && monster.getClan().equals(npc.getClan()) && GeoData.getInstance().canSeeTarget(npc, monster))
						{
							monster.setTarget(target);
							monster.setRunning();
							monster.addDamageHate(target, 0, 999);
							monster.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
						}
					}
					
				}
			}
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}
	
	public static void main(String[] args)
	{
		new GiantScouts(-1, "GiantScouts", "ai");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Monster: Giant Scout");
	}
}
package intelligence.RaidBosses;

import l2.hellknight.Config;
import l2.hellknight.bflmpsvz.a.L2AttackableAIScript;

import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.instancemanager.HellboundManager;
import l2.hellknight.gameserver.model.L2Effect;
import l2.hellknight.gameserver.model.L2Skill;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2MonsterInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.network.clientpackets.Say2;
import l2.hellknight.gameserver.network.serverpackets.NpcSay;
import l2.hellknight.gameserver.skills.SkillHolder;
import l2.hellknight.util.Rnd;

public class Amaskari extends L2AttackableAIScript
{
	private static final int AMASKARI = 22449;
	private static final int AMASKARI_PRISONER = 22450;
	
	private static final int BUFF_ID = 4632;
	private static SkillHolder[] BUFF = { new SkillHolder(BUFF_ID, 1), new SkillHolder(BUFF_ID, 2),  new SkillHolder(BUFF_ID, 3) };
	//private static SkillHolder INVINCIBILITY = new SkillHolder(5417, 1);

	private static final int[] AMASKARI_FSTRING_ID =
	{
		1000105, //I'll make everyone feel the same suffering as me!
		1800124, //Ha-ha yes, die slowly writhing in pain and agony!
		1800125, //More... need more... severe pain...
		1800127 //Something is burning inside my body!
	};

	private static final int[] MINIONS_FSTRING_ID =
	{
		1800126, //Ahh! My life is being drained out!
		1000503, //Thank you for saving me.
		1000138, //It... will... kill... everyone...
		1010451 //Eeek... I feel sick...yow...!
	};

	public Amaskari (int id, String name, String descr)
	{
		super(id,name,descr);
		
		addKillId(AMASKARI);
		addKillId(AMASKARI_PRISONER);
		addAttackId(AMASKARI);
		addSpawnId(AMASKARI_PRISONER);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("stop_toggle"))
		{
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getNpcId(), AMASKARI_FSTRING_ID[2]));
			((L2MonsterInstance) npc).clearAggroList();
			((L2MonsterInstance) npc).getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			npc.setIsInvul(false);
			//npc.doCast(INVINCIBILITY.getSkill())
		}
		
		else if (event.equalsIgnoreCase("onspawn_msg") && npc != null && !npc.isDead())
		{
			if (Rnd.get(100) > 20)
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getNpcId(), MINIONS_FSTRING_ID[2]));
			else if (Rnd.get(100) > 40)
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getNpcId(), MINIONS_FSTRING_ID[3]));
			
			startQuestTimer ("onspawn_msg", (Rnd.get(8) + 1) * 30000, npc, null);
		}
		
		return null;
	}


	@Override
	public String onAttack (L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		if (npc.getNpcId() == AMASKARI && Rnd.get(1000) < 25)
		{
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getNpcId(), AMASKARI_FSTRING_ID[0]));
			for (L2MonsterInstance minion : ((L2MonsterInstance) npc).getMinionList().getSpawnedMinions())
			{
				if (minion != null && !minion.isDead() && Rnd.get(10) == 0)
				{
					minion.broadcastPacket(new NpcSay(minion.getObjectId(), Say2.ALL, minion.getNpcId(), MINIONS_FSTRING_ID[0]));
					minion.setCurrentHp(minion.getCurrentHp() - minion.getCurrentHp() / 5); 
				}
			}
		}
		
		return super.onAttack(npc, attacker, damage, isPet, skill);
	}

	@Override
	public String onKill (L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (npc.getNpcId() == AMASKARI_PRISONER)
		{
			L2MonsterInstance master = ((L2MonsterInstance) npc).getLeader();
			if (master != null && !master.isDead())
			{
				master.broadcastPacket(new NpcSay(master.getObjectId(), Say2.ALL, master.getNpcId(), AMASKARI_FSTRING_ID[1]));
				L2Effect e = master.getFirstEffect(BUFF_ID);
				
				if (e != null && e.getAbnormalLvl() == 3 && master.isInvul())
					master.setCurrentHp(master.getCurrentHp() + master.getCurrentHp() /5);
				else
				{
					master.clearAggroList();
					master.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
					if (e == null)
						master.doCast(BUFF[0].getSkill());

					else if (e.getAbnormalLvl() < 3)
						master.doCast(BUFF[e.getAbnormalLvl()].getSkill());
						
					else
					{
						master.broadcastPacket(new NpcSay(master.getObjectId(), Say2.ALL, master.getNpcId(), AMASKARI_FSTRING_ID[3]));
						//master.doCast(INVINCIBILITY.getSkill())
						master.setIsInvul(true);
						startQuestTimer("stop_toggle", 10000, master, null);
					}
				}
			}
		}
		
		else if (npc.getNpcId() == AMASKARI)
		{
			for (L2MonsterInstance minion : ((L2MonsterInstance) npc).getMinionList().getSpawnedMinions())
			{
				if (minion != null && !minion.isDead())
				{
					if (Rnd.get(1000) > 300)
						minion.broadcastPacket(new NpcSay(minion.getObjectId(), Say2.ALL, minion.getNpcId(), MINIONS_FSTRING_ID[1]));
				
					HellboundManager.getInstance().updateTrust(30, true);
					minion.deleteMe();
				}
			}
		}
		
		return super.onKill(npc, killer, isPet);
	}
	
	@Override
	public final String onSpawn(L2Npc npc)
	{
		if (!npc.isTeleporting())
			startQuestTimer("onspawn_msg", (Rnd.get(3) + 1) * 30000, npc, null);

		return super.onSpawn(npc);
	}

	public static void main(String[] args)
	{
		new Amaskari(-1,"amaskari","ai");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded RaidBoss: Amaskari");
	}

}
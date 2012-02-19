package l2.brick.bflmpsvz.a;

import static l2.brick.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;

import l2.brick.gameserver.ai.CtrlEvent;
import l2.brick.gameserver.ai.CtrlIntention;
import l2.brick.gameserver.datatables.NpcTable;
import l2.brick.gameserver.instancemanager.DimensionalRiftManager;
import l2.brick.gameserver.model.L2Object;
import l2.brick.gameserver.model.L2Skill;
import l2.brick.gameserver.model.actor.L2Attackable;
import l2.brick.gameserver.model.actor.L2Character;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2MonsterInstance;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.actor.instance.L2RiftInvaderInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.jython.QuestJython;
import l2.brick.gameserver.templates.L2NpcTemplate;
import l2.brick.gameserver.util.Util;

public class L2AttackableAIScript extends QuestJython
{
	public void registerMobs(int[] mobs)
	{
		for (int id : mobs)
		{
			addEventId(id, QuestEventType.ON_ATTACK);
			addEventId(id, QuestEventType.ON_KILL);
			addEventId(id, QuestEventType.ON_SPAWN);
			addEventId(id, QuestEventType.ON_SPELL_FINISHED);
			addEventId(id, QuestEventType.ON_SKILL_SEE);
			addEventId(id, QuestEventType.ON_FACTION_CALL);
			addEventId(id, QuestEventType.ON_AGGRO_RANGE_ENTER);
		}
	}
	
	public void registerMobs(int[] mobs, QuestEventType... types)
	{
		for (int id : mobs)
		{
			for (QuestEventType type : types)
			{
				addEventId(id, type);
			}
		}
	}
	
	public L2AttackableAIScript (int questId, String name, String descr)
	{
		super(questId, name, descr);
	}
	
	@Override
	public String onAdvEvent (String event, L2Npc npc, L2PcInstance player)
	{
		return null;
	}
	
	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		return null;
	}
	
	@Override
	public String onSkillSee (L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if (caster == null)
		{
			return null;
		}
		if (!(npc instanceof L2Attackable))
		{
			return null;
		}
		
		L2Attackable attackable = (L2Attackable)npc;
		
		int skillAggroPoints = skill.getAggroPoints();
		
		if (caster.getPet() != null)
		{
			if (targets.length == 1 && Util.contains(targets, caster.getPet()))
				skillAggroPoints = 0;
		}
		
		if (skillAggroPoints > 0)
		{
			if ( attackable.hasAI() && (attackable.getAI().getIntention() == AI_INTENTION_ATTACK))
			{
				L2Object npcTarget = attackable.getTarget();
				for (L2Object skillTarget : targets)
				{
					if (npcTarget == skillTarget || npc == skillTarget)
					{
						L2Character originalCaster = isPet? caster.getPet(): caster;
						attackable.addDamageHate(originalCaster, 0, (skillAggroPoints*150)/(attackable.getLevel()+7));
					}
				}
			}
		}
		
		return null;
	}
	
	@Override
	public String onFactionCall (L2Npc npc, L2Npc caller, L2PcInstance attacker, boolean isPet)
	{
		if(attacker == null)
			return null;
		
		L2Character originalAttackTarget = (isPet? attacker.getPet(): attacker);
		if ( attacker.isInParty()
				&& attacker.getParty().isInDimensionalRift())
		{
			byte riftType = attacker.getParty().getDimensionalRift().getType();
			byte riftRoom = attacker.getParty().getDimensionalRift().getCurrentRoom();
			
			if (caller instanceof L2RiftInvaderInstance
					&& !DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(npc.getX(), npc.getY(), npc.getZ()))
			{
				return null;
			}
		}
		
		npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, originalAttackTarget, 1);
		
		return null;
	}
	
	@Override
	public String onAggroRangeEnter (L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if (player == null)
			return null;
		
		L2Character target = isPet ? player.getPet() : player;
		
		((L2Attackable) npc).addDamageHate(target, 0, 1);
		
		if (npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		return null;
	}
	
	@Override
	public String onSpawn (L2Npc npc)
	{
		return null;
	}
	
	@Override
	public String onAttack (L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if ((attacker != null) && (npc instanceof L2Attackable))
		{
			L2Attackable attackable = (L2Attackable)npc;
			
			L2Character originalAttacker = isPet? attacker.getPet(): attacker;
			attackable.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, originalAttacker);
			attackable.addDamageHate(originalAttacker, damage, (damage*100)/(attackable.getLevel()+7));
		}
		return null;
	}
	
	@Override
	public String onKill (L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (npc instanceof L2MonsterInstance)
		{
			final L2MonsterInstance mob = (L2MonsterInstance)npc;
			if (mob.getLeader() != null)
				mob.getLeader().getMinionList().onMinionDie(mob, -1);

			if (mob.hasMinions())
				mob.getMinionList().onMasterDie(false);
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		L2AttackableAIScript ai = new L2AttackableAIScript(-1,"L2AttackableAIScript","L2AttackableAIScript");
		// register all mobs here...
		for (int level =1; level<100; level++)
		{
			L2NpcTemplate[] templates = NpcTable.getInstance().getAllOfLevel(level);
			if ((templates != null) && (templates.length > 0))
			{
				for (L2NpcTemplate t: templates)
				{
					try
					{
						if ( L2Attackable.class.isAssignableFrom(Class.forName("l2.brick.gameserver.model.actor.instance."+t.type+"Instance")))
						{
							ai.addEventId(t.npcId, Quest.QuestEventType.ON_ATTACK);
							ai.addEventId(t.npcId, Quest.QuestEventType.ON_KILL);
							ai.addEventId(t.npcId, Quest.QuestEventType.ON_SPAWN);
							ai.addEventId(t.npcId, Quest.QuestEventType.ON_SKILL_SEE);
							ai.addEventId(t.npcId, Quest.QuestEventType.ON_FACTION_CALL);
							ai.addEventId(t.npcId, Quest.QuestEventType.ON_AGGRO_RANGE_ENTER);
						}
					}
					catch(ClassNotFoundException ex)
					{
						_log.info("Class not found "+t.type+"Instance");
					}
				}
			}
		}
	}
}
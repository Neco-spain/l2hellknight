package ai;

import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.ai.CtrlEvent;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.L2World;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.network.serverpackets.MagicSkillUse;
import l2rt.gameserver.tables.SkillTable;
import l2rt.util.GArray;
import l2rt.util.Location;
import l2rt.util.Rnd;

/**
 * @author Drizzy
 * @date 12.11.10
 * @AI for location Plains of Lizardman
 */
public class PlainsOfLizardman extends Fighter
{
	private GArray<L2Character> targets = new GArray<L2Character>();
	private boolean attack = false;
	
	public PlainsOfLizardman(L2Character actor)
	{
		super(actor);
	}

	@Override
	protected boolean randomWalk()
	{
		return false;
	}
	
	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{
		L2NpcInstance actor = getActor();
						
		L2Skill skill1 = SkillTable.getInstance().getInfo(6428, 1);
		L2Skill skill2 = SkillTable.getInstance().getInfo(6429, 1);
		L2Skill skill3 = SkillTable.getInstance().getInfo(6430, 1);
		
		targets.add(attacker);
		if(attacker.getPet() != null)
			targets.add(attacker);	

		int id = getActor().getNpcId();
		if (id == 18868)
		{
			attacker.callSkill(skill3, targets, true);
			attacker.sendPacket(new MagicSkillUse(attacker, 6430, 1, skill3.getHitTime(), 0));	
			actor.doDie(attacker);
		}
		if (id == 18865)
		{
			attacker.callSkill(skill1, targets, true);
			attacker.sendPacket(new MagicSkillUse(attacker, 6428, 1, skill1.getHitTime(), 0));	
			actor.doDie(attacker);
		}
		if (id == 18866)
		{
			attacker.callSkill(skill2, targets, true);
			attacker.sendPacket(new MagicSkillUse(attacker, 6429, 1, skill2.getHitTime(), 0));	
			actor.doDie(attacker);
		}
		if (id == 18864)
		{	
			actor.setIsInvul(true);
			actor.setImobilised(true);
			for(L2NpcInstance npc : L2World.getAroundNpc(actor, 400, 400))
			{
				if(npc.getNpcId() >= 22768 && npc.getNpcId() <= 22774)
				{
					Location sloc = actor.getSpawnedLoc();

					int x = sloc.x + Rnd.get(50) - 10;
					int y = sloc.y + Rnd.get(50) - 10;
					int z = GeoEngine.getHeight(x, y, sloc.z, actor.getReflection().getGeoIndex());

					npc.setRunning();
					npc.moveToLocation(x, y, z, 0, true);		
				}
			}
			if(attack == false)
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new Active(actor, attacker), 3000);
				attack = true;
			}			
		}
		super.onEvtAttacked(attacker, damage);
	}

	private class Active implements Runnable
	{
		private L2NpcInstance _actor;
		private L2Character _attacker;

		public Active(L2NpcInstance actor, L2Character attacker)
		{
			_actor = actor;
			_attacker = attacker;
		}
		public void run()
		{
			L2Skill skill = SkillTable.getInstance().getInfo(6427, 1);	

			int id = getActor().getNpcId();
			if(id == 18864)
			{
				for(L2NpcInstance npc : L2World.getAroundNpc(_actor, 300, 300))
				{
					if(npc.getNpcId() >= 22768 && npc.getNpcId() <= 22774)
					{
						skill.getEffects(_actor, npc, false, false);
						npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, _attacker, Rnd.get(1, 100));
					}
					attack = false;
					_actor.setIsInvul(false);
					_actor.setImobilised(false);	
				}	
				_actor.doDie(_attacker);		
			}
		}
	}
}
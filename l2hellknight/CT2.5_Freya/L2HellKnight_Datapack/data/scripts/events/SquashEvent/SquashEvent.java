/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package events.SquashEvent;

import l2.hellknight.gameserver.instancemanager.QuestManager;
import l2.hellknight.gameserver.model.L2Object;
import l2.hellknight.gameserver.model.L2Skill;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2MonsterInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.network.clientpackets.Say2;
import l2.hellknight.gameserver.network.serverpackets.CreatureSay;
import l2.hellknight.util.Rnd;

/**
 * @author Gnacik
 * @version 1.0
 * @see Warning! Mostly that event is custom!
 */
public class SquashEvent extends Quest
{
	private static final int MANAGER = 31860;
	
	private static final int NECTAR_SKILL = 2005;
	
	private static final int[] CHRONO_LIST = {
		4202,5133,5817,7058,8350
	};
	
	private static final int[] SQUASH_LIST = {
		12774,12775,12776,
		12777,12778,12779,
		13016,13017
	};
	
	private static final String[] _NOCHRONO_TEXT = {
		"You cannot kill me without Chrono",
		"Hehe...keep trying...",
		"Nice try...",
		"Tired ?",
		"Go go ! haha..."
	};
	
	private static final String[] _CHRONO_TEXT = {
		"Arghh... Chrono weapon...",
		"My end is coming...",
		"Please leave me !",
		"Heeellpppp...",
		"Somebody help me please..."
	};
	private static final String[] _NECTAR_TEXT = {
		"Yummy... Nectar...",
		"Plase give me more...",
		"Hmmm.. More.. I need more...",
		"I will like you more if you give me more...",
		"Hmmmmmmm...",
		"My favourite..."
	};
	
	private static final int[][] DROPLIST =
	{
		// must be sorted by npcId !
		// npcId, itemId, chance
		// Young Squash
		{ 12774,  1060, 100 },	// Lesser Healing potion
		{ 12774,  1062,  50 },	// Haste potion
		// High Quality Squash
		{ 12775,  1539, 100 },	// Greater Healing potion
		{ 12775,  1375,  70 },	// Greater Swift Attack Potion
		{ 12775,  1459,  50 },	// Crystal c-grade
		// Low Quality Squash
		{ 12776,  1061, 100 },	// Healing potion
		{ 12776,  1062,  70 },	// Haste potion
		{ 12776,  1458,  50 },	// Crystal d-grade
		
		// Large Young Squash
		{ 12777,  1061, 100 },	// Healing potion
		{ 12777,  1374,  50 },	// Greater Haste potion
		// High Quality Large
		{ 12778,  1539, 100 },	// Greater Healing potion		
		{ 12778,  6036,  70 },	// Greater Magic Haste Potion
		{ 12778,  1459,  40 },	// Crystal c-grade
		// Low Quality Large
		{ 12779,  6035,  70 },	// Magic Haste Potion
		{ 12779,  1458,  50 },	// Crystal d-grade
		
		// King
		{ 13016,  1540, 100 },	// Quick Healing Potion
		{ 13016,  1460,  40 },	// Crystal b-grade
		{ 13016,  5234,  20 },	// Mystery Potion
		// Emperor
		{ 13017,  1540, 100 },	// Quick Healing Potion
		{ 13017, 20004,  40 },	// Energy Ginseng
		{ 13017,  1461,  20 },	// Crystal a-grade
		{ 13017,  5234,  10 }	// Mystery Potion
	};
	
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{		
		if (contains(SQUASH_LIST,npc.getNpcId()))
		{
			if(isPet)
			{
				noChronoText(npc);
				npc.setIsInvul(true);				
				return null;
			}
			if(attacker.getActiveWeaponItem() != null && contains(CHRONO_LIST,attacker.getActiveWeaponItem().getItemId()))
			{
				ChronoText(npc);
				npc.setIsInvul(false);
				npc.getStatus().reduceHp(10, attacker);
				return null;
			}
			else
			{
				noChronoText(npc);
				npc.setIsInvul(true);
				return null;
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
        if (contains(targets,npc) && contains(SQUASH_LIST,npc.getNpcId()) && (skill.getId() == NECTAR_SKILL))
        {
        	switch(npc.getNpcId())
        	{
        		case 12774:
                	randomSpawn(12775, 12776, npc, true);
        			break;
        		case 12777:
                	randomSpawn(12778, 12779, npc, true);
        			break;
        		case 12775:
       				randomSpawn(13016, npc, true);
        			break;
        		case 12778:
       				randomSpawn(13017, npc, true);
        			break;
        	}        	
        }
        return super.onSkillSee(npc,caster,skill,targets,isPet);
	}

    public String onKill (L2Npc npc, L2PcInstance killer, boolean isPet) 
    { 
    	dropItem(npc, killer);
    	
    	return super.onKill(npc, killer, isPet);
    }

    public String onSpawn(L2Npc npc) 
    {
    	npc.setIsImmobilized(true);
    	npc.disableCoreAI(true);
    	return null;
    	// return super.onSpawn(npc);
    }
    
	private static final void dropItem(L2Npc mob, L2PcInstance player)
	{
		final int npcId = mob.getNpcId();
		final int chance = Rnd.get(100);
		for (int i = 0; i < DROPLIST.length; i++)
		{
			int[] drop = DROPLIST[i];
			if (npcId == drop[0])
			{
				if (chance < drop[2])
				{
					if(drop[1] > 20000)
						((L2MonsterInstance)mob).dropItem(player, drop[1], 2);
					else
						((L2MonsterInstance)mob).dropItem(player, drop[1], Rnd.get(2, 6));
					continue;
				}
			}
			if (npcId < drop[0])
				return; // not found
		}
	}
	
	private void randomSpawn(int lower, int higher, L2Npc npc, boolean delete)
	{
		int _random = Rnd.get(100);
		if (_random < 10)
			spawnNext(lower, npc);
		else if(_random < 30)
			spawnNext(higher, npc);
		else
			nectarText(npc);
	}
	
	private void randomSpawn(int npcId, L2Npc npc, boolean delete)
	{
		if(Rnd.get(100) < 10)
			spawnNext(npcId, npc);
		else
			nectarText(npc);
	}
	
	private void ChronoText(L2Npc npc)
	{
		if(Rnd.get(100) < 20)
			npc.broadcastPacket(new CreatureSay(npc.getObjectId(), Say2.ALL, npc.getName(), _CHRONO_TEXT[Rnd.get(_CHRONO_TEXT.length)]));		
	}
	private void noChronoText(L2Npc npc)
	{
		if(Rnd.get(100) < 20)
			npc.broadcastPacket(new CreatureSay(npc.getObjectId(), Say2.ALL, npc.getName(), _NOCHRONO_TEXT[Rnd.get(_NOCHRONO_TEXT.length)]));		
	}
	private void nectarText(L2Npc npc)
	{
		if(Rnd.get(100) < 30)
			npc.broadcastPacket(new CreatureSay(npc.getObjectId(), Say2.ALL, npc.getName(), _NECTAR_TEXT[Rnd.get(_NECTAR_TEXT.length)]));
	}
	
    private void spawnNext(int npcId, L2Npc npc)
    {
    	addSpawn(npcId, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 60000);		
    	npc.deleteMe();
    }

	public static <T> boolean contains(T[] array, T obj)
	{
		for (int i = 0; i < array.length; i++)
		{
			if (array[i] == obj)
			{
				return true;
			}
		}
		return false;
	}
	
	public static boolean contains(int[] array, int obj)
	{
		for (int i = 0; i < array.length; i++)
		{
			if (array[i] == obj)
			{
				return true;
			}
		}
		return false;
	}
	
	public SquashEvent(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		for (int mob : SQUASH_LIST)
		{
			addAttackId(mob);
			addKillId(mob);
			addSpawnId(mob);
			addSkillSeeId(mob);
		}

		addStartNpc(MANAGER);
		addFirstTalkId(MANAGER);
		addTalkId(MANAGER);
		
		addSpawn(MANAGER, 83077, 147910, -3471, 29412, false, 0);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			Quest q = QuestManager.getInstance().getQuest(getName());
			st = q.newQuestState(player);
		}
		htmltext = npc.getNpcId() + ".htm";
		return htmltext;
	}
	
    public static void main(String[] args)
    {
		new SquashEvent(-1,"SquashEvent","events");
    }
}
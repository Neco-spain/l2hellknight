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
package ai.individual;

import javolution.util.FastMap;

import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.datatables.SpawnTable;
import l2.hellknight.gameserver.model.L2CharPosition;
import l2.hellknight.gameserver.model.L2Spawn;
import l2.hellknight.gameserver.model.actor.L2Attackable;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.network.clientpackets.Say2;
import l2.hellknight.gameserver.network.serverpackets.NpcSay;
import l2.hellknight.gameserver.util.Util;
import l2.hellknight.util.Rnd;

public class FrightenedRagnaOrc extends Quest/**L2AttackableAIScript*/
{
	// NPC
	private static final int FRIGHTENED_RAGNA_ORC = 18807;
	
	// TEXT
	private static final int[] ON_KILL_SAY = {
		1800839,
		1800840,
	};
	
	// COORD
	private static final int[][] ESCAPE_COORDS = {
		// zone_vertices.sql#Den of Evil Zones
		{/*70000*/ 74313, -116888, -2218},
		{/*70001*/ 63170, -106621, -2384},
		{/*70002*/ 68030, -107150, -1152},
		{/*70003*/ 62351, -117376, -3064},
		{/*70004*/ 68100, -116006, -2171},
		{/*70005*/ 69911, -118790, -2256},
		{/*70006*/ 67520, -122211, -2910},
		{/*70007*/ 74325, -121296, -3024},
		{/*70008*/ 70788, -125554, -3016},
		{/*70009*/ 76155, -127355, -3149},
		{/*70010*/ 71543, -128820, -3360},
	};
	
	private FrightenedRagnaOrc(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addEventId(FRIGHTENED_RAGNA_ORC, Quest.QuestEventType.ON_SPAWN);
		addEventId(FRIGHTENED_RAGNA_ORC, Quest.QuestEventType.ON_ATTACK);
		addEventId(FRIGHTENED_RAGNA_ORC, Quest.QuestEventType.ON_KILL);
		
		for (L2Spawn spawn : SpawnTable.getInstance().getSpawnTable())
		{
			L2Npc npc;
			if (spawn != null && spawn.getNpcid() == FRIGHTENED_RAGNA_ORC && (npc = spawn.getLastSpawn()) != null)
				this.onSpawn(npc);
		}
	}
	
	/*******************************************************/
	// Frightened RagnaOrc Instance
	/*******************************************************/
	private class TheFrightenedRagnaOrcInstance
	{
		L2PcInstance lastAttacker;
		long lastAttackTime;
		double hp;
		boolean enabled;
	}
	private static FastMap<L2Npc,TheFrightenedRagnaOrcInstance> _frightenedRagnaOrcs = new FastMap<L2Npc,TheFrightenedRagnaOrcInstance>();
	
	private void put(L2Npc npc, L2PcInstance attacker)
	{
		synchronized (_frightenedRagnaOrcs)
		{
			TheFrightenedRagnaOrcInstance he;
			if ((he = _frightenedRagnaOrcs.get(npc)) == null)
			{
				he = new TheFrightenedRagnaOrcInstance();
				he.lastAttacker = attacker;
				he.lastAttackTime = System.currentTimeMillis();
				he.hp = npc.getCurrentHp();
				_frightenedRagnaOrcs.put(npc, he);
				startQuestTimer("1", 1000, npc, null, true);
			}
			else
			{
				he.lastAttacker = attacker;
				he.lastAttackTime = System.currentTimeMillis();
				he.hp = npc.getCurrentHp();
			}
		}
	}
	private TheFrightenedRagnaOrcInstance get(L2Npc npc)
	{
		return _frightenedRagnaOrcs.get(npc);
	}
	private void remove(L2Npc npc)
	{
		cancelQuestTimer("1", npc, null);
		_frightenedRagnaOrcs.remove(npc);
		npc.setIsImmobilized(false);
	}
	
	private void autoChat(L2Npc npc, int npcString)
	{
		npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getNpcId(), "Now it is truly the beginning of the duel!!"));
	}
	
	private void escape(L2Npc npc)
	{
		((L2Attackable)npc).clearAggroList();
		
		L2Spawn spawn = npc.getSpawn();
		int[] pos = { spawn.getLocx(), spawn.getLocy(), spawn.getLocz() };
		double m = Double.MAX_VALUE;
		for (int a[] : ESCAPE_COORDS)
		{
			double d = l2.hellknight.gameserver.util.Util.calculateDistance(a[0], a[1], spawn.getLocx(), spawn.getLocy());
			if (m > d)
			{
				m = d;
				pos = a;
			}
		}
		npc.setIsImmobilized(false);
		npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(pos[0], pos[1], pos[2], 0));
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.disableCoreAI(true);
		((L2Attackable)npc).setOnKillDelay(1000);	//Default 5000ms.
		npc.getSpawn().setRespawnDelay(60);
		return super.onSpawn(npc);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		final TheFrightenedRagnaOrcInstance he = get(npc);
		if (he == null)
		{
			put(npc, attacker);
			autoChat(npc, 1800833);
		}
		else
		{
			double previousHpPercent = he.hp / npc.getMaxHp() * 100;
			double currentHpPercent = npc.getCurrentHp() / npc.getMaxHp() * 100;
			long time = System.currentTimeMillis() - he.lastAttackTime;
			
			put(npc, attacker);
			
			if (currentHpPercent <= 50 && 50 < previousHpPercent)
			{
				he.enabled = true;
				npc.setIsImmobilized(true);
				autoChat(npc, 1800832);
			}
			else if (currentHpPercent <= 80 && time >= 5000)
			{
				int rnd = Rnd.get(100);
				if ((rnd -= 15) <= 0)
					autoChat(npc, 1800834);
				else if ((rnd -= 15) <= 0)
					autoChat(npc, 1800833);
			}
		}
		return null;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		remove(npc);
		autoChat(npc, ON_KILL_SAY[Rnd.get(ON_KILL_SAY.length)]);
		return null;
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event == "1")
		{
			final TheFrightenedRagnaOrcInstance he = get(npc);
			long time = System.currentTimeMillis() - he.lastAttackTime;
			if (he.enabled && 
			   (time > 60000
			 || time > 10000 && npc.getCurrentHp() >= he.hp) )
			{
				remove(npc);
				if (Rnd.get(100) <= 33 && Util.checkIfInRange(100, he.lastAttacker, npc, true))
				{
					final int adena;
					int say = 1800835;
					int chance = Rnd.get(100);
					if ((chance -= 1) <= 0)
					{
						adena = 10000000;
						say = 1800836;
					}
					else if ((chance -= 5) <= 0)
						adena = 5000000;
					else if ((chance -= 10) <= 0)
						adena = 1000000;
					else if ((chance -= 15) <= 0)
						adena = 500000;
					else if ((chance -= 20) <= 0)
						adena = 100000;
					else /*49%*/
						adena = 50000;
					autoChat(npc, say);
					for (int n = 0; n < 10; n++)
						((L2Attackable)npc).dropItem(he.lastAttacker, 57, adena / 10);
					
					startQuestTimer("despawn", 5000, npc, null);
				}
				else
				{
					autoChat(npc, 1800837);
					autoChat(npc, 1800838);
					escape(npc);
				}
			}
			else if (time > 60000 && npc.getCurrentHp() >= npc.getMaxHp())
			{
				// timeout.
				remove(npc);
			}
			return null;
		}
		else if (event == "despawn")
		{
			npc.getSpawn().setRespawnDelay(1800);
			npc.deleteMe();
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	// Register the new Script at the Script System
	public static void main(String[] args)
	{
		new FrightenedRagnaOrc(-1, "FrightenedRagnaOrc", "ai");
	}
}

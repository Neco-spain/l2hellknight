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
package intelligence.individual;

import intelligence.group_template.L2AttackableAIScript;

import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.datatables.SpawnTable;
import l2.hellknight.gameserver.model.L2CharPosition;
import l2.hellknight.gameserver.model.L2Spawn;
import l2.hellknight.gameserver.model.actor.L2Attackable;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;

public class Knoriks extends L2AttackableAIScript
{
	private static final int KNORIKS = 22857;
	private static int _npcMoveX = 0;
	private static int _npcMoveY = 0;
	private static int _isRunTo = 0;
	private static int _npcBlock = 0;
	private static int X = 0;
	private static int Y = 0;
	private static int Z = 0;
	private static final int[][] WALKS = {
		{147212, 112375, -3725},{148310, 112051, -3725},{149145, 113373, -3728},
		{149178, 115300, -3725},{148146, 115983, -3725},{146054, 116203, -3725},
		{144901, 115402, -3725},{145307, 113010, -3725}};
	
	private static boolean _isAttacked = false;
	private static boolean _isSpawned = false;
	
	public Knoriks (int id, String name, String descr)
	{
		super(id,name,descr);
		int[] mobs = {KNORIKS};
		registerMobs(mobs, QuestEventType.ON_ATTACK, QuestEventType.ON_KILL, QuestEventType.ON_SPAWN);
		// wait 2 minutes after Start AI
		startQuestTimer("check_ai", 5000, null, null, true);
		
		_isSpawned = false;
		_isAttacked = false;
		_isRunTo = 1;
		_npcMoveX = 0;
		_npcMoveY = 0;
		_npcBlock = 0;
	}
	
	public L2Npc findTemplate(int npcId)
	{
		L2Npc npc = null;
		for (L2Spawn spawn : SpawnTable.getInstance().getSpawnTable())
		{
			if (spawn != null && spawn.getNpcid() == npcId)
			{
				npc = spawn.getLastSpawn();
				break;
			}
		}
		return npc;
	}
	
	@Override
	public String onAdvEvent (String event, L2Npc npc, L2PcInstance player)
	{
		X = WALKS[_isRunTo-1][0];
		Y = WALKS[_isRunTo-1][1];
		Z = WALKS[_isRunTo-1][2];
		if (event.equalsIgnoreCase("time_isAttacked"))
		{
			_isAttacked = false;
			if (npc.getNpcId() == KNORIKS)
			{
				npc.setRunning();
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(X, Y, Z, 0));
			}
		}
		else if (event.equalsIgnoreCase("check_ai"))
		{
			cancelQuestTimer("check_ai", null, null);
			if (_isSpawned == false)
			{
				L2Npc knoriks_ai = findTemplate(KNORIKS);
				if (knoriks_ai != null)
				{
					_isSpawned = true;
					startQuestTimer("Start", 1000, knoriks_ai, null, true);
					return super.onAdvEvent(event, npc, player);
				}
			}
		}
		else if (event.equalsIgnoreCase("Start"))
		{
			if (npc != null && _isSpawned == true)
			{
				if (_isAttacked == true)
					return super.onAdvEvent(event, npc, player);
				if (npc.getNpcId() == KNORIKS && (npc.getX()-50) <= X && (npc.getX()+50) >= X && (npc.getY()-50) <= Y && (npc.getY()+50) >= Y)
				{
					_isRunTo++;
					if (_isRunTo > 55)
						_isRunTo = 1;
					X = WALKS[_isRunTo-1][0];
					Y = WALKS[_isRunTo-1][1];
					Z = WALKS[_isRunTo-1][2];
					npc.setRunning();
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,new L2CharPosition(X, Y, Z, 0));
				}
				
				// Test for unblock Npc
				if (npc.getX() != _npcMoveX && npc.getY() != _npcMoveY)
				{
					_npcMoveX = npc.getX();
					_npcMoveY = npc.getY();
					_npcBlock = 0;
				}
				else if (npc.getNpcId() == KNORIKS)
				{
					_npcBlock++;
					if (_npcBlock > 2)
					{
						npc.teleToLocation(X, Y, Z);
						return super.onAdvEvent(event, npc, player);
					}
					if (_npcBlock > 0)
						npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,new L2CharPosition(X, Y, Z, 0));
				}
				// End Test unblock Npc
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onSpawn (L2Npc npc)
	{
		if (npc.getNpcId() == KNORIKS && _npcBlock == 0)
		{
			_isSpawned = true;
			_isRunTo = 1;
			startQuestTimer("Start", 1000, npc, null, true);
		}
		return super.onSpawn(npc);
	}
	
	@Override
	public String onAttack (L2Npc npc, L2PcInstance player, int damage, boolean isPet)
	{
		if (npc.getNpcId() == KNORIKS)
		{
			_isAttacked = true;
			cancelQuestTimer("time_isAttacked", null, null);
			startQuestTimer("time_isAttacked", 5000, npc, null);
			if (player != null)
			{
				npc.setRunning();
				((L2Attackable)npc).addDamageHate(player, 0, 100);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
			}
		}
		return super.onAttack(npc, player, damage, isPet);
	}
	
	@Override
	public String onKill (L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (npc.getNpcId() == KNORIKS)
		{
			cancelQuestTimer("Start", null, null);
			cancelQuestTimer("time_isAttacked", null, null);
			_isSpawned = false;
		}
		return super.onKill(npc,killer,isPet);
	}
	
	public static void main(String[] args)
	{
		new Knoriks(-1,"knoriks","ai");
	}
}
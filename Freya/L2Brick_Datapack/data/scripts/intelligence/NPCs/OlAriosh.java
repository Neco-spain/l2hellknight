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
package intelligence.NPCs;

import l2.brick.bflmpsvz.a.L2AttackableAIScript;

import gnu.trove.TIntObjectHashMap;

import javolution.util.FastSet;

import l2.brick.Config;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.network.serverpackets.NpcSay;

public class OlAriosh extends L2AttackableAIScript
{
	private static final int ARIOSH = 18555;
	private static final int GUARD  = 18556;
	private static L2Npc _guard = null;
	private FastSet<Integer> _lockedSpawns = new FastSet<Integer>();
	private TIntObjectHashMap<Integer> _spawnedGuards = new TIntObjectHashMap<Integer>();
	
	public OlAriosh(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addAttackId(ARIOSH);
		addKillId(ARIOSH);
		addKillId(GUARD);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("time_to_spawn"))
		{
			final int objId = npc.getObjectId();
			if (!_spawnedGuards.contains(objId))
			{
				npc.broadcastPacket(new NpcSay(npc.getObjectId(),0,npc.getNpcId(),"What do you do? Help me more likely!"));
			    	_guard = addSpawn(GUARD, npc.getX()+100, npc.getY()+100, npc.getZ(), 0, false, 0, false, npc.getInstanceId());
			    	_lockedSpawns.remove(objId);
				_spawnedGuards.put(_guard.getObjectId(), objId);
			}
		}
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet)
	{    
		if (npc.getNpcId() == ARIOSH)
		{
			final int objId = npc.getObjectId();
			if (!_spawnedGuards.contains(objId))
			{
				if (!_lockedSpawns.contains(objId))
				{
					startQuestTimer("time_to_spawn",60000,npc,player);
					_lockedSpawns.add(objId);
				}
			}
		}
		
		return super.onAttack(npc, player, damage, isPet);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		switch (npc.getNpcId())
		{
			case GUARD:
				_spawnedGuards.remove(npc.getObjectId());
				break;
			case ARIOSH:
				_spawnedGuards.remove(_guard.getObjectId());
				_guard.decayMe();
			    cancelQuestTimer("time_to_spawn",npc,killer);
			    break;
		}

		return super.onKill(npc, killer, isPet);
	}

	public static void main(String[] args)
	{
		new OlAriosh(-1, "OlAriosh", "intelligence/NPCs");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded NPC: Ol Ariosh");
	}
}
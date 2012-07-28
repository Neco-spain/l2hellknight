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
package intelligence.group_template;

import l2.brick.gameserver.ThreadPoolManager;
import l2.brick.gameserver.datatables.SkillTable;
import l2.brick.gameserver.instancemanager.ZoneManager;
import l2.brick.gameserver.model.L2Skill;
import l2.brick.gameserver.model.Location;
import l2.brick.gameserver.model.actor.L2Character;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.L2Playable;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.zone.type.L2EffectZone;
import l2.brick.gameserver.network.SystemMessageId;
import l2.brick.gameserver.network.serverpackets.SystemMessage;
import l2.brick.gameserver.util.Util;
import l2.brick.util.Rnd;

/**
 ** @author Gnacik
 **
 ** Dummy AI for spawns/respawns only for testing.
 */
public class DenOfEvil extends L2AttackableAIScript
{
	// private static final int _buffer_id = 32656;
	
	private static final int[] _eye_ids = { 18812, 18813, 18814 };
	private static final int _skill_id = 6150; // others +2
	
	private static final Location[] _eye_spawn =
	{
		new Location(71544, -129400, -3360, 16472),
		new Location(70954, -128854, -3360,    16),
		new Location(72145, -128847, -3368, 32832),
		new Location(76147, -128372, -3144, 16152),
		new Location(71573, -128309, -3360, 49152),
		new Location(75211, -127441, -3152,     0),
		new Location(77005, -127406, -3144, 32784),
		new Location(75965, -126486, -3144, 49120),
		new Location(70972, -126429, -3016, 19208),
		new Location(69916, -125838, -3024,  2840),
		new Location(71658, -125459, -3016, 35136),
		new Location(70605, -124646, -3040, 52104),
		new Location(67283, -123237, -2912, 12376),
		new Location(68383, -122754, -2912, 27904),
		new Location(74137, -122733, -3024, 13272),
		new Location(66736, -122007, -2896, 60576),
		new Location(73289, -121769, -3024,  1024),
		new Location(67894, -121491, -2912, 43872),
		new Location(75530, -121477, -3008, 34424),
		new Location(74117, -120459, -3024, 52344),
		new Location(69608, -119855, -2534, 17251),
		new Location(71014, -119027, -2520, 31904),
		new Location(68944, -118964, -2527, 59874),
		new Location(62261, -118263, -3072, 12888),
		new Location(70300, -117942, -2528, 46208),
		new Location(74312, -117583, -2272, 15280),
		new Location(63276, -117409, -3064, 24760),
		new Location(68104, -117192, -2168, 15888),
		new Location(73758, -116945, -2216,     0),
		new Location(74944, -116858, -2220, 30892),
		new Location(61715, -116623, -3064, 59888),
		new Location(69140, -116464, -2168, 28952),
		new Location(67311, -116374, -2152,  1280),
		new Location(62459, -116370, -3064, 48624),
		new Location(74475, -116260, -2216, 47456),
		new Location(68333, -115015, -2168, 45136),
		new Location(68280, -108129, -1160, 17992),
		new Location(62983, -107259, -2384, 12552),
		new Location(67062, -107125, -1144, 64008),
		new Location(68893, -106954, -1160, 36704),
		new Location(63848, -106771, -2384, 32784),
		new Location(62372, -106514, -2384,     0),
		new Location(67838, -106143, -1160, 51232),
		new Location(62905, -106109, -2384, 51288)
	};
	
	private int getSkillIdByNpcId(int npcId)
	{
		int diff = npcId - _eye_ids[0];
		diff *= 2;
		return _skill_id + diff;
	}
	
	public DenOfEvil(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		registerMobs(_eye_ids, QuestEventType.ON_KILL, QuestEventType.ON_SPAWN);
		
		spawnEyes();
	}
	
	public static void main(String[] args)
	{
		new DenOfEvil(-1, "DenOfEvil", "ai");
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		if (Util.contains(_eye_ids, npc.getNpcId()))
		{
			npc.disableCoreAI(true);
			npc.setIsImmobilized(true);
			L2EffectZone zone = ZoneManager.getInstance().getZone(npc, L2EffectZone.class);
			if (zone == null)
			{
				_log.warning("NPC "+npc+" spawned outside of L2EffectZone, check your zone coords! X:"+npc.getX()+" Y:"+npc.getY()+" Z:"+npc.getZ());
				return null;
			}
			int skillId = getSkillIdByNpcId(npc.getNpcId());
			int skillLevel = zone.getSkillLevel(skillId);
			zone.addSkill(skillId, skillLevel+1);
			if (skillLevel == 3) // 3+1=4
			{
				ThreadPoolManager.getInstance().scheduleAi(new KashaDestruction(zone), 2*60*1000l);
				zone.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.KASHA_EYE_PITCHES_TOSSES_EXPLODE));
			}
			else if (skillLevel == 2) // 2+1=3
				zone.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.I_CAN_FEEL_ENERGY_KASHA_EYE_GETTING_STRONGER_RAPIDLY));
		}
		return null;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (Util.contains(_eye_ids, npc.getNpcId()))
		{
			ThreadPoolManager.getInstance().scheduleAi(new RespawnNewEye(npc.getLocation()), 15000);
			L2EffectZone zone = ZoneManager.getInstance().getZone(npc, L2EffectZone.class);
			if (zone == null)
			{
				_log.warning("NPC "+npc+" killed outside of L2EffectZone, check your zone coords! X:"+npc.getX()+" Y:"+npc.getY()+" Z:"+npc.getZ());
				return null;
			}
			int skillId = getSkillIdByNpcId(npc.getNpcId());
			int skillLevel = zone.getSkillLevel(skillId);
			zone.addSkill(skillId, skillLevel - 1);
		}
		return null;
	}
	
	private void spawnEyes()
	{
		for(Location loc : _eye_spawn)
			addSpawn(_eye_ids[Rnd.get(_eye_ids.length)], loc, false, 0);
	}
	
	private class RespawnNewEye implements Runnable
	{
		private final Location _loc;
		
		public RespawnNewEye(Location loc) 
		{
			_loc = loc;
		}
		
		@Override
		public void run()
		{
			addSpawn(_eye_ids[Rnd.get(_eye_ids.length)], _loc, false, 0);
		}
	}
	
	private class KashaDestruction implements Runnable
	{
		L2EffectZone _zone;
		
		public KashaDestruction(L2EffectZone zone)
		{
			_zone = zone;
		}
		
		@Override
		public void run()
		{
			for (int i = _skill_id; i <= _skill_id + 4; i = i + 2 )
			{
				// test 3 skills if some is lvl 4
				if (_zone.getSkillLevel(i) > 3)
				{
					destroyZone();
					break;
				}
			}
		}
		
		private void destroyZone()
		{
			for (L2Character character : _zone.getCharactersInsideArray())
			{
				if (character == null)
					continue;
				if (character instanceof L2Playable)
				{
					L2Skill skill = SkillTable.getInstance().getInfo(6149, 1);
					skill.getEffects(character, character); // apply effect
				}
				else
				{
					if (character.doDie(null)) // mobs die
					{
						if (character instanceof L2Npc)
						{
							// respawn eye
							L2Npc npc = (L2Npc) character;
							if (Util.contains(_eye_ids, npc.getNpcId()))
								ThreadPoolManager.getInstance().scheduleAi(new RespawnNewEye(npc.getLocation()), 15000);
						}
					}
				}
			}
			for (int i = _skill_id; i <= _skill_id + 4; i = i + 2 )
			{
				_zone.removeSkill(i);
			}
		}
	}
}
package intelligence.Monsters;

import l2.brick.bflmpsvz.a.L2AttackableAIScript;

import l2.brick.Config;
import l2.brick.gameserver.ThreadPoolManager;
import l2.brick.gameserver.datatables.SkillTable;
import l2.brick.gameserver.instancemanager.ZoneManager;
import l2.brick.gameserver.model.L2Skill;
import l2.brick.gameserver.model.actor.L2Character;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.L2Playable;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.zone.type.L2EffectZone;
import l2.brick.gameserver.network.SystemMessageId;
import l2.brick.gameserver.network.serverpackets.SystemMessage;
import l2.brick.gameserver.util.Util;
import l2.brick.util.Rnd;

public class DenOfEvil extends L2AttackableAIScript
{
	
	private static final int[] _eye_ids = { 18812, 18813, 18814 };
	private static final int _skill_id = 6150;
	
	private static final int[][] _eye_spawn =
	{
		{ 71544, -129400, -3360, 16472 },
		{ 70954, -128854, -3360,    16 },
		{ 72145, -128847, -3368, 32832 },
		{ 76147, -128372, -3144, 16152 },
		{ 71573, -128309, -3360, 49152 },
		{ 75211, -127441, -3152,     0 },
		{ 77005, -127406, -3144, 32784 },
		{ 75965, -126486, -3144, 49120 },
		{ 70972, -126429, -3016, 19208 },
		{ 69916, -125838, -3024,  2840 },
		{ 71658, -125459, -3016, 35136 },
		{ 70605, -124646, -3040, 52104 },
		{ 67283, -123237, -2912, 12376 },
		{ 68383, -122754, -2912, 27904 },
		{ 74137, -122733, -3024, 13272 },
		{ 66736, -122007, -2896, 60576 },
		{ 73289, -121769, -3024,  1024 },
		{ 67894, -121491, -2912, 43872 },
		{ 75530, -121477, -3008, 34424 },
		{ 74117, -120459, -3024, 52344 },
		{ 69608, -119855, -2534, 17251 },
		{ 71014, -119027, -2520, 31904 },
		{ 68944, -118964, -2527, 59874 },
		{ 62261, -118263, -3072, 12888 },
		{ 70300, -117942, -2528, 46208 },
		{ 74312, -117583, -2272, 15280 },
		{ 63276, -117409, -3064, 24760 },
		{ 68104, -117192, -2168, 15888 },
		{ 73758, -116945, -2216,     0 },
		{ 74944, -116858, -2220, 30892 },
		{ 61715, -116623, -3064, 59888 },
		{ 69140, -116464, -2168, 28952 },
		{ 67311, -116374, -2152,  1280 },
		{ 62459, -116370, -3064, 48624 },
		{ 74475, -116260, -2216, 47456 },
		{ 68333, -115015, -2168, 45136 },
		{ 68280, -108129, -1160, 17992 },
		{ 62983, -107259, -2384, 12552 },
		{ 67062, -107125, -1144, 64008 },
		{ 68893, -106954, -1160, 36704 },
		{ 63848, -106771, -2384, 32784 },
		{ 62372, -106514, -2384,     0 },
		{ 67838, -106143, -1160, 51232 },
		{ 62905, -106109, -2384, 51288 }
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
				return null;
			}
			int skillId = getSkillIdByNpcId(npc.getNpcId());
			int skillLevel = zone.getSkillLevel(skillId);
			zone.addSkill(skillId, skillLevel+1);
			if (skillLevel == 3)
			{
				ThreadPoolManager.getInstance().scheduleAi(new KashaDestruction(zone), 2*60*1000l);
				zone.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.KASHA_EYE_PITCHES_TOSSES_EXPLODE));
			}
			else if (skillLevel == 2)
				zone.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.I_CAN_FEEL_ENERGY_KASHA_EYE_GETTING_STRONGER_RAPIDLY));
		}
		return null;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (Util.contains(_eye_ids, npc.getNpcId()))
		{
			ThreadPoolManager.getInstance().scheduleAi(new RespawnNewEye(npc.getX(), npc.getY(), npc.getZ(), npc.getHeading()), 15000);
			L2EffectZone zone = ZoneManager.getInstance().getZone(npc, L2EffectZone.class);
			if (zone == null)
			{
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
		for(int[] _spawn : _eye_spawn)
			addSpawn(_eye_ids[Rnd.get(0, _eye_ids.length-1)], _spawn[0], _spawn[1], _spawn[2], _spawn[3], false, 0);
	}
	
	private class RespawnNewEye implements Runnable
	{
		private int _x, _y, _z, _h;
		
		public RespawnNewEye(int x, int y, int z, int h) {
			_x = x;
			_y = y;
			_z = z;
			_h = h;
		}
		
		public void run()
		{
			addSpawn(_eye_ids[Rnd.get(0, _eye_ids.length-1)], _x, _y, _z, _h, false, 0);
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
					if (character.doDie(null))
					{
						if (character instanceof L2Npc)
						{
							L2Npc npc = (L2Npc) character;
							if (Util.contains(_eye_ids, npc.getNpcId()))
								ThreadPoolManager.getInstance().scheduleAi(new RespawnNewEye(npc.getX(), npc.getY(), npc.getZ(), npc.getHeading()), 15000);
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

	public static void main(String[] args)
	{
		new DenOfEvil(-1, "DenOfEvil", "ai");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Monster: Den of Evil");
	}
}
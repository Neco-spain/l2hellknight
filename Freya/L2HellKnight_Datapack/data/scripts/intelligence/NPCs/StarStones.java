package intelligence.NPCs;

import l2.hellknight.Config;
import l2.hellknight.gameserver.model.L2Object;
import l2.hellknight.gameserver.model.L2Skill;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;
import l2.hellknight.gameserver.util.Util;
import l2.hellknight.util.Rnd;

import l2.hellknight.bflmpsvz.a.L2AttackableAIScript;

public class StarStones extends L2AttackableAIScript
{
	private static final int[] mobs = {18684, 18685, 18686, 18687, 18688, 18689, 18690, 18691, 18692};
	private static final int RATE = 1;
	
	public StarStones(int questId, String name, String descr)
	{
		super(questId, name, descr);
		this.registerMobs(mobs, QuestEventType.ON_SKILL_SEE);
	}
	
	@Override
	public String onSkillSee (L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if (Util.contains(targets, npc) && skill.getId() == 932)
		{
			int itemId = 0;
			
			switch(npc.getNpcId())
			{
				case 18684:
				case 18685:
				case 18686:
					// give Red item
					itemId = 14009;
					break;
				case 18687:
				case 18688:
				case 18689:
					// give Blue item
					itemId = 14010;
					break;
				case 18690:
				case 18691:
				case 18692:
					// give Green item
					itemId = 14011;
					break;
				default:
					// unknown npc!
					return super.onSkillSee(npc, caster, skill, targets, isPet);
			}
			if (Rnd.get(100) < 33)
			{
				caster.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_COLLECTION_HAS_SUCCEEDED));
				caster.addItem("StarStone", itemId, Rnd.get(RATE + 1, 2 * RATE), null, true);
			}
			else if ((skill.getLevel() == 1 && Rnd.get(100) < 15) ||
					(skill.getLevel() == 2 && Rnd.get(100) < 50) ||
					(skill.getLevel() == 3 && Rnd.get(100) < 75))
			{
				caster.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_COLLECTION_HAS_SUCCEEDED));
				caster.addItem("StarStone", itemId, Rnd.get(1, RATE), null, true);
			}
			else
				caster.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_COLLECTION_HAS_FAILED));
			npc.deleteMe();
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}
	
	public static void main(String[] args)
	{
		new StarStones(-1, "starstones", "ai");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded NPC: Star Stones");
	}
}

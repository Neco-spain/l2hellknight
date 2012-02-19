package intelligence.Monsters;

import l2.brick.Config;
import l2.brick.gameserver.ai.CtrlIntention;
import l2.brick.gameserver.model.L2Object;
import l2.brick.gameserver.model.L2Skill;
import l2.brick.gameserver.model.actor.L2Character;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2ChestInstance;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.util.Util;
import l2.brick.util.Rnd;

import l2.brick.bflmpsvz.a.L2AttackableAIScript;

public class Chests extends L2AttackableAIScript
{
	
	private static final int SKILL_DELUXE_KEY = 2229;
	
	//Base chance for BOX to be opened
	private static final int BASE_CHANCE = 100;
	
	// Percent to decrease base chance when grade of DELUXE key not match
	private static final int LEVEL_DECREASE = 40;
	
	// Chance for a chest to actually be a BOX (as opposed to being a mimic).
	private static final int IS_BOX = 40;
	
	private static final int[] NPC_IDS = { 18265,18266,18267,18268,18269,18270,18271,
		18272,18273,18274,18275,18276,18277,18278,18279,18280,18281,
		18282,18283,18284,18285,18286,18287,18288,18289,18290,18291,
		18292,18293,18294,18295,18296,18297,18298,21671,21694,21717,
		21740,21763,21786,21801,21802,21803,21804,21805,21806,21807,
		21808,21809,21810,21811,21812,21813,21814,21815,21816,21817,
		21818,21819,21820,21821,21822 };
	
	public Chests(int questId, String name, String descr)
	{
		// firstly, don't forget to call the parent constructor to prepare the event triggering
		// mechanisms etc.
		super(questId, name, descr);
		this.registerMobs(NPC_IDS, QuestEventType.ON_ATTACK, QuestEventType.ON_SKILL_SEE);
	}
	
	@Override
	public String onSkillSee (L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if (npc instanceof L2ChestInstance)
		{
			// this behavior is only run when the target of skill is the passed npc (chest)
			// i.e. when the player is attempting to open the chest using a skill
			if (!Util.contains(targets,npc))
			{
				return super.onSkillSee(npc,caster,skill,targets,isPet);
			}
			L2ChestInstance chest = ((L2ChestInstance)npc);
			int npcId = chest.getNpcId();
			int skillId = skill.getId();
			int skillLevel= skill.getLevel();
			
			// check if the chest and skills used are valid for this script.  Exit if invalid.
			if (!Util.contains(NPC_IDS,npcId))
			{
				return super.onSkillSee(npc,caster,skill,targets,isPet);
			}
			// if this has already been interacted, no further ai decisions are needed
			// if it's the first interaction, check if this is a box or mimic
			if (!chest.isInteracted())
			{
				chest.setInteracted();
				if (Rnd.get(100) < IS_BOX)
				{
					// if it's a box, either it will be successfully openned by a proper key, or instantly disappear
					if (skillId == SKILL_DELUXE_KEY)
					{
						// check the chance to open the box
						int keyLevelNeeded = chest.getLevel()/10;
						keyLevelNeeded -= skillLevel;
						if (keyLevelNeeded < 0)
							keyLevelNeeded *= -1;
						int chance = BASE_CHANCE - keyLevelNeeded * LEVEL_DECREASE;
						
						// success, pretend-death with rewards:  chest.reduceCurrentHp(99999999, player)
						if (Rnd.get(100) < chance)
						{
							chest.setMustRewardExpSp(false);
							chest.setSpecialDrop();
							chest.reduceCurrentHp(99999999, caster, null);
							return null;
						}
					}
					// used a skill other than chest-key, or used a chest-key but failed to open: disappear with no rewards
					chest.deleteMe();
				}
				else
				{
					L2Character originalCaster = isPet? caster.getPet(): caster;
					chest.setRunning();
					chest.addDamageHate(originalCaster,0,999);
					chest.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, originalCaster);
				}
			}
		}
		return super.onSkillSee(npc,caster,skill,targets,isPet);
	}
	
	@Override
	public String onAttack (L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (npc instanceof L2ChestInstance)
		{
			L2ChestInstance chest = ((L2ChestInstance)npc);
			int npcId = chest.getNpcId();
			// check if the chest and skills used are valid for this script.  Exit if invalid.
			if (!Util.contains(NPC_IDS, npcId))
			{
				return super.onAttack(npc,attacker,damage,isPet);
			}
			
			// if this was a mimic, set the target, start the skills and become agro
			if (!chest.isInteracted())
			{
				chest.setInteracted();
				if (Rnd.get(100) < IS_BOX)
				{
					chest.deleteMe();
				}
				else
				{
					// if this weren't a box, upon interaction start the mimic behaviors...
					// todo: perhaps a self-buff (skill id 4245) with random chance goes here?
					L2Character originalAttacker = isPet? attacker.getPet(): attacker;
					chest.setRunning();
					chest.addDamageHate(originalAttacker,0,(damage*100)/(chest.getLevel()+7));
					chest.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, originalAttacker);
				}
			}
		}
		return super.onAttack(npc,attacker,damage,isPet);
	}
	
	public static void main(String[] args)
	{
		// now call the constructor (starts up the ai)
		new Chests(-1,"chests","ai");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Monster: Chests");
	}
}
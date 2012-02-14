package services;

import static l2rt.gameserver.tables.SkillTable.SubclassSkills.EmergentAbilityAttack;
import static l2rt.gameserver.tables.SkillTable.SubclassSkills.EmergentAbilityDefense;
import static l2rt.gameserver.tables.SkillTable.SubclassSkills.EmergentAbilityEmpower;
import static l2rt.gameserver.tables.SkillTable.SubclassSkills.EmergentAbilityMagicDefense;
import static l2rt.gameserver.tables.SkillTable.SubclassSkills.EnchanterAbilityBarrier;
import static l2rt.gameserver.tables.SkillTable.SubclassSkills.EnchanterAbilityBoostMana;
import static l2rt.gameserver.tables.SkillTable.SubclassSkills.EnchanterAbilityManaRecycle;
import static l2rt.gameserver.tables.SkillTable.SubclassSkills.HealerAbilityDivineProtection;
import static l2rt.gameserver.tables.SkillTable.SubclassSkills.HealerAbilityHeal;
import static l2rt.gameserver.tables.SkillTable.SubclassSkills.HealerAbilityPrayer;
import static l2rt.gameserver.tables.SkillTable.SubclassSkills.KnightAbilityBoostHP;
import static l2rt.gameserver.tables.SkillTable.SubclassSkills.KnightAbilityDefense;
import static l2rt.gameserver.tables.SkillTable.SubclassSkills.KnightAbilityResistCritical;
import static l2rt.gameserver.tables.SkillTable.SubclassSkills.MasterAbilityAttack;
import static l2rt.gameserver.tables.SkillTable.SubclassSkills.MasterAbilityCasting;
import static l2rt.gameserver.tables.SkillTable.SubclassSkills.MasterAbilityDefense;
import static l2rt.gameserver.tables.SkillTable.SubclassSkills.MasterAbilityEmpower;
import static l2rt.gameserver.tables.SkillTable.SubclassSkills.MasterAbilityFocus;
import static l2rt.gameserver.tables.SkillTable.SubclassSkills.MasterAbilityMagicDefense;
import static l2rt.gameserver.tables.SkillTable.SubclassSkills.RogueAbilityCriticalChance;
import static l2rt.gameserver.tables.SkillTable.SubclassSkills.RogueAbilityEvasion;
import static l2rt.gameserver.tables.SkillTable.SubclassSkills.RogueAbilityLongShot;
import static l2rt.gameserver.tables.SkillTable.SubclassSkills.SummonerAbilityBoostHPMP;
import static l2rt.gameserver.tables.SkillTable.SubclassSkills.SummonerAbilityResistAttribute;
import static l2rt.gameserver.tables.SkillTable.SubclassSkills.SummonerAbilitySpirit;
import static l2rt.gameserver.tables.SkillTable.SubclassSkills.TransformDivineEnchanter;
import static l2rt.gameserver.tables.SkillTable.SubclassSkills.TransformDivineHealer;
import static l2rt.gameserver.tables.SkillTable.SubclassSkills.TransformDivineKnight;
import static l2rt.gameserver.tables.SkillTable.SubclassSkills.TransformDivineRogue;
import static l2rt.gameserver.tables.SkillTable.SubclassSkills.TransformDivineSummoner;
import static l2rt.gameserver.tables.SkillTable.SubclassSkills.TransformDivineWarrior;
import static l2rt.gameserver.tables.SkillTable.SubclassSkills.TransformDivineWizard;
import static l2rt.gameserver.tables.SkillTable.SubclassSkills.WarriorAbilityBoostCP;
import static l2rt.gameserver.tables.SkillTable.SubclassSkills.WarriorAbilityHaste;
import static l2rt.gameserver.tables.SkillTable.SubclassSkills.WarriorAbilityResistTrait;
import static l2rt.gameserver.tables.SkillTable.SubclassSkills.WizardAbilityAntimagic;
import static l2rt.gameserver.tables.SkillTable.SubclassSkills.WizardAbilityManaGain;
import static l2rt.gameserver.tables.SkillTable.SubclassSkills.WizardAbilityManaSteal;

import java.util.HashMap;

import l2rt.config.ConfigSystem;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.L2SubClass;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.network.serverpackets.AcquireSkillList;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.tables.SkillTable;
import l2rt.util.GArray;

public class SubclassSkills extends Functions implements ScriptFile
{
	// --- Сертификаты ---

	private final static int CertificateEmergentAbility = 1; // 10280;
	private final static int CertificateEmergentAbility2 = 2; // Затычка

	private final static int CertificateWarriorAbility = 31; // 10281;
	private final static int CertificateKnightAbility = 32; // 10282;
	private final static int CertificateRogueAbility = 33; // 10283;
	private final static int CertificateWizardAbility = 34; // 10284;
	private final static int CertificateHealerAbility = 35; // 10285;
	private final static int CertificateSummonerAbility = 36; // 10286;
	private final static int CertificateEnchanterAbility = 37; // 10287;

	private final static int TransformSealbookDivineWarrior = 41; // 10289;
	private final static int TransformSealbookDivineKnight = 42; // 10288;
	private final static int TransformSealbookDivineRogue = 43; // 10290;
	private final static int TransformSealbookDivineWizard = 44; // 10292;
	private final static int TransformSealbookDivineHealer = 45; // 10291;
	private final static int TransformSealbookDivineSummoner = 46; // 10294;
	private final static int TransformSealbookDivineEnchanter = 47; // 10293;

	private static int[][] skills = new int[][] {
			// Скилл - сертификат - минимальный уровень отображения

			{ EmergentAbilityAttack.getId(), CertificateEmergentAbility, 65 },
			{ EmergentAbilityDefense.getId(), CertificateEmergentAbility, 65 },
			{ EmergentAbilityEmpower.getId(), CertificateEmergentAbility, 65 },
			{ EmergentAbilityMagicDefense.getId(), CertificateEmergentAbility, 65 },

			{ MasterAbilityAttack.getId(), CertificateEmergentAbility2, 75 },
			{ MasterAbilityEmpower.getId(), CertificateEmergentAbility2, 75 },
			{ MasterAbilityCasting.getId(), CertificateEmergentAbility2, 75 },
			{ MasterAbilityFocus.getId(), CertificateEmergentAbility2, 75 },
			{ MasterAbilityDefense.getId(), CertificateEmergentAbility2, 75 },
			{ MasterAbilityMagicDefense.getId(), CertificateEmergentAbility2, 75 },

			{ KnightAbilityBoostHP.getId(), CertificateKnightAbility, 75 },
			{ KnightAbilityDefense.getId(), CertificateKnightAbility, 75 },
			{ KnightAbilityResistCritical.getId(), CertificateKnightAbility, 75 },

			{ EnchanterAbilityBoostMana.getId(), CertificateEnchanterAbility, 75 },
			{ EnchanterAbilityManaRecycle.getId(), CertificateEnchanterAbility, 75 },
			{ EnchanterAbilityBarrier.getId(), CertificateEnchanterAbility, 75 },

			{ SummonerAbilityBoostHPMP.getId(), CertificateSummonerAbility, 75 },
			{ SummonerAbilityResistAttribute.getId(), CertificateSummonerAbility, 75 },
			{ SummonerAbilitySpirit.getId(), CertificateSummonerAbility, 75 },

			{ RogueAbilityEvasion.getId(), CertificateRogueAbility, 75 },
			{ RogueAbilityLongShot.getId(), CertificateRogueAbility, 75 },
			{ RogueAbilityCriticalChance.getId(), CertificateRogueAbility, 75 },

			{ WizardAbilityManaGain.getId(), CertificateWizardAbility, 75 },
			{ WizardAbilityManaSteal.getId(), CertificateWizardAbility, 75 },
			{ WizardAbilityAntimagic.getId(), CertificateWizardAbility, 75 },

			{ HealerAbilityPrayer.getId(), CertificateHealerAbility, 75 },
			{ HealerAbilityHeal.getId(), CertificateHealerAbility, 75 },
			{ HealerAbilityDivineProtection.getId(), CertificateHealerAbility, 75 },

			{ WarriorAbilityResistTrait.getId(), CertificateWarriorAbility, 75 },
			{ WarriorAbilityHaste.getId(), CertificateWarriorAbility, 75 },
			{ WarriorAbilityBoostCP.getId(), CertificateWarriorAbility, 75 },

			{ TransformDivineWarrior.getId(), TransformSealbookDivineWarrior, 80 },
			{ TransformDivineKnight.getId(), TransformSealbookDivineKnight, 80 },
			{ TransformDivineRogue.getId(), TransformSealbookDivineRogue, 80 },
			{ TransformDivineWizard.getId(), TransformSealbookDivineWizard, 80 },
			{ TransformDivineSummoner.getId(), TransformSealbookDivineSummoner, 80 },
			{ TransformDivineHealer.getId(), TransformSealbookDivineHealer, 80 },
			{ TransformDivineEnchanter.getId(), TransformSealbookDivineEnchanter, 80 } };

	private static int[][] classes = new int[][] {
			// Класс - второй сертификат (75) - третий сертификат (80)

			// --- Warriors ---

			// Gladiator
			{ 2, CertificateWarriorAbility, TransformSealbookDivineWarrior },
			{ 88, CertificateWarriorAbility, TransformSealbookDivineWarrior },

			// Warlord
			{ 3, CertificateWarriorAbility, TransformSealbookDivineWarrior },
			{ 89, CertificateWarriorAbility, TransformSealbookDivineWarrior },

			// Bounty Hunter
			{ 55, CertificateWarriorAbility, TransformSealbookDivineWarrior },
			{ 117, CertificateWarriorAbility, TransformSealbookDivineWarrior },

			// Tyrant
			{ 48, CertificateWarriorAbility, TransformSealbookDivineWarrior },
			{ 114, CertificateWarriorAbility, TransformSealbookDivineWarrior },

			// Destroyer
			{ 46, CertificateWarriorAbility, TransformSealbookDivineWarrior },
			{ 113, CertificateWarriorAbility, TransformSealbookDivineWarrior },

			// Soul Breaker
			{ 128, CertificateWarriorAbility, TransformSealbookDivineWarrior },
			{ 132, CertificateWarriorAbility, TransformSealbookDivineWarrior },

			// Soul Breaker
			{ 129, CertificateWarriorAbility, TransformSealbookDivineWarrior },
			{ 133, CertificateWarriorAbility, TransformSealbookDivineWarrior },

			// Berserker
			{ 127, CertificateWarriorAbility, TransformSealbookDivineWarrior },
			{ 131, CertificateWarriorAbility, TransformSealbookDivineWarrior },

			// --- Knights ---

			// Paladin
			{ 5, CertificateKnightAbility, TransformSealbookDivineKnight },
			{ 90, CertificateKnightAbility, TransformSealbookDivineKnight },

			// Dark Avenger
			{ 6, CertificateKnightAbility, TransformSealbookDivineKnight },
			{ 91, CertificateKnightAbility, TransformSealbookDivineKnight },

			// Temple Knight
			{ 20, CertificateKnightAbility, TransformSealbookDivineKnight },
			{ 99, CertificateKnightAbility, TransformSealbookDivineKnight },

			// Shillien Knight
			{ 33, CertificateKnightAbility, TransformSealbookDivineKnight },
			{ 106, CertificateKnightAbility, TransformSealbookDivineKnight },

			// --- Rogues ---

			// Hawkeye
			{ 9, CertificateRogueAbility, TransformSealbookDivineRogue },
			{ 92, CertificateRogueAbility, TransformSealbookDivineRogue },

			// Silver Ranger
			{ 24, CertificateRogueAbility, TransformSealbookDivineRogue },
			{ 102, CertificateRogueAbility, TransformSealbookDivineRogue },

			// Phantom Ranger
			{ 37, CertificateRogueAbility, TransformSealbookDivineRogue },
			{ 109, CertificateRogueAbility, TransformSealbookDivineRogue },

			// Treasure Hunter
			{ 8, CertificateRogueAbility, TransformSealbookDivineRogue },
			{ 93, CertificateRogueAbility, TransformSealbookDivineRogue },

			// Plains Walker
			{ 23, CertificateRogueAbility, TransformSealbookDivineRogue },
			{ 101, CertificateRogueAbility, TransformSealbookDivineRogue },

			// Abyss Walker
			{ 36, CertificateRogueAbility, TransformSealbookDivineRogue },
			{ 108, CertificateRogueAbility, TransformSealbookDivineRogue },

			// Arbalester
			{ 130, CertificateRogueAbility, TransformSealbookDivineRogue },
			{ 134, CertificateRogueAbility, TransformSealbookDivineRogue },

			// --- Wizards ---

			// Sorcecer
			{ 12, CertificateWizardAbility, TransformSealbookDivineWizard },
			{ 94, CertificateWizardAbility, TransformSealbookDivineWizard },

			// Spellsinger
			{ 27, CertificateWizardAbility, TransformSealbookDivineWizard },
			{ 103, CertificateWizardAbility, TransformSealbookDivineWizard },

			// Spellhowler
			{ 40, CertificateWizardAbility, TransformSealbookDivineWizard },
			{ 110, CertificateWizardAbility, TransformSealbookDivineWizard },

			// Necromancer
			{ 13, CertificateWizardAbility, TransformSealbookDivineWizard },
			{ 95, CertificateWizardAbility, TransformSealbookDivineWizard },

			// --- Healers ---

			// Shillien Elder
			{ 43, CertificateHealerAbility, TransformSealbookDivineHealer },
			{ 112, CertificateHealerAbility, TransformSealbookDivineHealer },

			// Elder
			{ 30, CertificateHealerAbility, TransformSealbookDivineHealer },
			{ 105, CertificateHealerAbility, TransformSealbookDivineHealer },

			// Bishop
			{ 16, CertificateHealerAbility, TransformSealbookDivineHealer },
			{ 97, CertificateHealerAbility, TransformSealbookDivineHealer },

			// --- Summoners ---

			// Warlock
			{ 14, CertificateSummonerAbility, TransformSealbookDivineSummoner },
			{ 96, CertificateSummonerAbility, TransformSealbookDivineSummoner },

			// Elemental Summoner
			{ 28, CertificateSummonerAbility, TransformSealbookDivineSummoner },
			{ 104, CertificateSummonerAbility, TransformSealbookDivineSummoner },

			// Phantom Summoner
			{ 41, CertificateSummonerAbility, TransformSealbookDivineSummoner },
			{ 111, CertificateSummonerAbility, TransformSealbookDivineSummoner },

			// --- Enchanters ---

			// Prophet
			{ 17, CertificateEnchanterAbility, TransformSealbookDivineEnchanter },
			{ 98, CertificateEnchanterAbility, TransformSealbookDivineEnchanter },

			// Warcryer
			{ 52, CertificateEnchanterAbility, TransformSealbookDivineEnchanter },
			{ 116, CertificateEnchanterAbility, TransformSealbookDivineEnchanter },

			// Inspector
			{ 135, CertificateEnchanterAbility, TransformSealbookDivineEnchanter },
			{ 136, CertificateEnchanterAbility, TransformSealbookDivineEnchanter },

			// Sword Singer
			{ 21, CertificateEnchanterAbility, TransformSealbookDivineEnchanter },
			{ 100, CertificateEnchanterAbility, TransformSealbookDivineEnchanter },

			// Bladedancer
			{ 34, CertificateEnchanterAbility, TransformSealbookDivineEnchanter },
			{ 107, CertificateEnchanterAbility, TransformSealbookDivineEnchanter } };

	public void showList()
	{
		L2Player player = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		if(!ConfigSystem.getBoolean("AllowLearnTransSkillsWOQuest"))
		{
			if(!player.isQuestCompleted("_136_MoreThanMeetsTheEye"))
			{
				show("You must complete the “More Than Meets the Eye” transformation quest in order to receive the subclass certification and to properly acquire the skill as your main class.", player, npc);
				return;
			}
		}
		AcquireSkillList asl = new AcquireSkillList(AcquireSkillList.TRANSFORMATION);
		GArray<L2Skill> skills = getAvailableSkills(player);
		if(skills.isEmpty())
		{
			show("You've learned all skills available for your Subclass.", player, npc);
			return;
		}
		for(L2Skill skill : skills)
			asl.addSkill(skill.getId(), skill.getLevel(), skill.getLevel(), 0, 1);
		player.sendPacket(asl);
	}

	public GArray<L2Skill> getAvailableSkills(L2Player player)
	{
		GArray<L2Skill> result = new GArray<L2Skill>();
		if(!player.isSubClassActive() || player.getLevel() < 65)
			return result;
		GArray<Integer> certificates = getPossibleCertificates(player);
		for(int[] tmp : skills)
			if(tmp[2] <= player.getLevel() && certificates.contains(tmp[1]) && canLearn(player, tmp[0]))
			{
				L2Skill skill = SkillTable.getInstance().getInfo(tmp[0], getSumLevel(player, tmp[0]) + 1);
				if(skill == null)
					System.out.println("getAvailableSkills: skill is null! id: " + tmp[0] + ", level: " + (getSumLevel(player, tmp[0]) + 1));
				else
					result.add(skill);
			}
		return result;
	}

	public GArray<Integer> getPossibleCertificates(L2Player player)
	{
		GArray<Integer> certificates = new GArray<Integer>();
		if(!player.isSubClassActive() || player.getLevel() < 65)
			return certificates;
		int class_id = player.getClassId().getId();
		certificates.add(CertificateEmergentAbility);
		if(player.getLevel() >= 75)
			certificates.add(CertificateEmergentAbility2);
		for(int[] tmp : classes)
			if(tmp[0] == class_id)
			{
				if(player.getLevel() >= 75 && !certificates.contains(tmp[1]))
					certificates.add(tmp[1]);
				if(player.getLevel() >= 80 && !certificates.contains(tmp[2]))
					certificates.add(tmp[2]);
			}
		return certificates;
	}

	public static int getSumLevel(L2Player player, int skill_id)
	{
		int sum_level = 0;
		for(L2SubClass sb : player.getSubClasses().values())
			for(L2Skill skill : sb.getSkillsList())
				if(skill != null && skill.getId() == skill_id)
					sum_level += skill.getLevel();
		return sum_level;
	}

	public static boolean canLearn(L2Player player, int skill_id)
	{
		int max_level = SkillTable.getInstance().getMaxLevel(skill_id);
		if(getSumLevel(player, skill_id) >= max_level)
			return false;
		for(int[] tmp : skills)
			if(tmp[0] == skill_id)
			{
				if(tmp[2] > player.getLevel())
					return false;
				int count = 0;
				for(L2Skill skill : player.getActiveClass().getSkillsList())
				{
					if(tmp[2] == 75 && is75skill(skill.getId()))
						return false;
					if(getCertificateForSkill(skill.getId()) == tmp[1])
						count += skill.getLevel();
				}
				if(count > 0)
				{
					if(tmp[1] == CertificateEmergentAbility)
						return player.getLevel() >= 70 && count < 2;
					return false;
				}
				break;
			}
		return true;
	}

	public static int getCertificateForSkill(int skill_id)
	{
		for(int[] tmp : skills)
			if(tmp[0] == skill_id)
				return tmp[1];
		return -1;
	}

	public static void learnSkill(L2Player player, Integer skill_id)
	{
		if(!player.isSubClassActive() || player.getLevel() < 65 || !canLearn(player, skill_id))
		{
			player.sendActionFailed();
			return;
		}

		/**
		int certificate_id = getCertificateForSkill(skill_id);
		L2ItemInstance certificate = player.getInventory().findItemByItemId(certificate_id);
		if(certificate == null || certificate.getIntegerLimitedCount() < 1)
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ITEMS_TO_LEARN_SKILLS);
			return;
		}
		L2ItemInstance ri = player.getInventory().destroyItem(certificate, 1, true);
		player.sendPacket(new SystemMessage(SystemMessage.S1_HAS_DISAPPEARED).addItemName(ri.getItemId()));
		*/

		L2Skill old_skill = null;
		if(player.getActiveClass().getSkillsList().contains(skill_id))
			old_skill = player.getActiveClass().getSkillsList().get(skill_id);
		int new_level = old_skill == null ? 1 : old_skill.getLevel() + 1;

		player.getActiveClass().setSkills(player.getActiveClass().getSkills() + (player.getActiveClass().getSkills().isEmpty() ? "" : ";") + skill_id);
		player.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EARNED_S1).addSkillName(skill_id, new_level));
		HashMap<String, Object> variables = new HashMap<String, Object>();
		variables.put("self", player.getStoredId());
		callScripts("services.SubclassSkills", "showList", new Object[0], variables);
	}

	public void deleteSkills()
	{
		L2Player player = (L2Player) getSelf();
		if(!player.isSubClassActive() || player.getLevel() < 65)
		{
			player.sendActionFailed();
			return;
		}
		if(player.getInventory().getAdena() < 10000000)
		{
			player.sendActionFailed();
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}
		player.getInventory().reduceAdena(10000000);
		player.getActiveClass().setSkills("");
		show(new CustomMessage("scripts.services.SubclassSkills.SkillsDeleted", player), player);
	}

	private static boolean is75skill(int skill_id)
	{
		for(int[] tmp : skills)
			if(tmp[0] == skill_id && tmp[2] == 75)
				return true;
		return false;
	}

	/*
	10280	Certificate - Emergent Ability		a,Certificate needed to learn an Emergent Ability. Only main classes can learn this skill. This item cannot be exchanged, dropped, destroyed, or stored in a warehouse.\0	-1	a,	a,	a,	a,	0	0	0	a,	1
	10281	Certificate - Warrior Ability		a,Certificate needed to learn a Warrior Ability. Only main classes can learn this skill. This item cannot be exchanged, dropped, destroyed, or stored in a warehouse.\0	-1	a,	a,	a,	a,	0	0	0	a,	1
	10282	Certificate - Knight Ability		a,Certificate needed to learn a Knight Ability. Only main classes can learn this skill. This item cannot be exchanged, dropped, destroyed, or stored in a warehouse.\0	-1	a,	a,	a,	a,	0	0	0	a,	1
	10283	Certificate - Rogue Ability		a,Certificate needed to learn a Rogue Ability. Only main classes can learn this skill. This item cannot be exchanged, dropped, destroyed, or stored in a warehouse.\0	-1	a,	a,	a,	a,	0	0	0	a,	1
	10284	Certificate - Wizard Ability		a,Certificate needed to learn a Wizard Ability. Only main classes can learn this skill. This item cannot be exchanged, dropped, destroyed, or stored in a warehouse.\0	-1	a,	a,	a,	a,	0	0	0	a,	1
	10285	Certificate - Healer Ability		a,Certificate needed to learn a Healer Ability. Only main classes can learn this skill. This item cannot be exchanged, dropped, destroyed, or stored in a warehouse.\0	-1	a,	a,	a,	a,	0	0	0	a,	1
	10286	Certificate - Summoner Ability		a,Certificate needed to learn a Summoner Ability. Only main classes can learn this skill. This item cannot be exchanged, dropped, destroyed, or stored in a warehouse.\0	-1	a,	a,	a,	a,	0	0	0	a,	1
	10287	Certificate - Enchanter Ability		a,Certificate needed to learn an Enchanter Ability. Only main classes can learn this skill. This item cannot be exchanged, dropped, destroyed, or stored in a warehouse.\0	-1	a,	a,	a,	a,	0	0	0	a,	1
	10288	Transform Sealbook - Divine Knight		a,Allows for tranformation into a highly specialized form, by transferring the powerful energy of the subclass to the main class. This item cannot be exchanged, dropped, destroyed, or stored in a warehouse.\0	-1	a,	a,	a,	a,	0	0	0	a,	1
	10289	Transform Sealbook - Divine Warrior		a,Allows for tranformation into a highly specialized form, by transferring the powerful energy of the subclass to the main class. This item cannot be exchanged, dropped, destroyed, or stored in a warehouse.\0	-1	a,	a,	a,	a,	0	0	0	a,	1
	10290	Transform Sealbook - Divine Rogue		a,Allows for tranformation into a highly specialized form, by transferring the powerful energy of the subclass to the main class. This item cannot be exchanged, dropped, destroyed, or stored in a warehouse.\0	-1	a,	a,	a,	a,	0	0	0	a,	1
	10291	Transform Sealbook - Divine Healer		a,Allows for tranformation into a highly specialized form, by transferring the powerful energy of the subclass to the main class. This item cannot be exchanged, dropped, destroyed, or stored in a warehouse.\0	-1	a,	a,	a,	a,	0	0	0	a,	1
	10292	Transform Sealbook - Divine Wizard		a,Allows for tranformation into a highly specialized form, by transferring the powerful energy of the subclass to the main class. This item cannot be exchanged, dropped, destroyed, or stored in a warehouse.\0	-1	a,	a,	a,	a,	0	0	0	a,	1
	10293	Transform Sealbook - Divine Enchanter		a,Allows for tranformation into a highly specialized form, by transferring the powerful energy of the subclass to the main class. This item cannot be exchanged, dropped, destroyed, or stored in a warehouse.\0	-1	a,	a,	a,	a,	0	0	0	a,	1
	10294	Transform Sealbook - Divine Summoner		a,Allows for tranformation into a highly specialized form, by transferring the powerful energy of the subclass to the main class. This item cannot be exchanged, dropped, destroyed, or stored in a warehouse.\0	-1	a,	a,	a,	a,	0	0	0	a,	1
	*/

	public void onLoad()
	{
		System.out.println("Loaded Service: Subclass Skills");
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}
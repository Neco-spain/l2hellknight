package actions;

import commands.admin.AdminEditChar;
import l2rt.Config;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.cache.InfoCache;
import l2rt.gameserver.model.L2Effect;
import l2rt.gameserver.model.L2Object;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.base.Experience;
import l2rt.gameserver.model.instances.*;
import l2rt.gameserver.model.instances.L2NpcInstance.AggroInfo;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestEventType;
import l2rt.gameserver.skills.Stats;
import l2rt.util.DropList;
import l2rt.util.Files;
import l2rt.util.Util;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;

public class OnActionShift extends Functions implements ScriptFile
{
	public void onLoad()
	{
		System.out.println("OnActionShift Loaded");
	}

	public void onReload()
	{}

	public void onShutdown()
	{}

	public boolean OnActionShift_L2NpcInstance(L2Player player, L2Object object)
	{
		if(player == null || object == null)
			return false;
		if(!Config.ALLOW_NPC_SHIFTCLICK && !player.isGM())
		{
			if(Config.ALT_GAME_SHOW_DROPLIST && object.isNpc())
			{
				L2NpcInstance npc = (L2NpcInstance) object;
				if(npc.isDead())
					return false;
				droplist(player, npc);
			}
			return false;
		}
		if(object.isNpc())
		{
			L2NpcInstance npc = (L2NpcInstance) object;

			// Для мертвых мобов не показываем табличку, иначе спойлеры плачут
			if(npc.isDead())
				return false;

			String dialog;

			if(Config.ALT_FULL_NPC_STATS_PAGE)
			{
				dialog = Files.read("data/scripts/actions/player.L2NpcInstance.onActionShift.full.htm", player);
				dialog = dialog.replaceFirst("%class%", String.valueOf(npc.getClass().getSimpleName().replaceFirst("L2", "").replaceFirst("Instance", "")));
				dialog = dialog.replaceFirst("%id%", String.valueOf(npc.getNpcId()));
				dialog = dialog.replaceFirst("%respawn%", String.valueOf(npc.getSpawn() != null ? Util.formatTime(npc.getSpawn().getRespawnDelay()) : "0"));
				dialog = dialog.replaceFirst("%walkSpeed%", String.valueOf(npc.getWalkSpeed()));
				dialog = dialog.replaceFirst("%evs%", String.valueOf(npc.getEvasionRate(null)));
				dialog = dialog.replaceFirst("%acc%", String.valueOf(npc.getAccuracy()));
				dialog = dialog.replaceFirst("%crt%", String.valueOf(npc.getCriticalHit(null, null)));
				dialog = dialog.replaceFirst("%aspd%", String.valueOf(npc.getPAtkSpd()));
				dialog = dialog.replaceFirst("%cspd%", String.valueOf(npc.getMAtkSpd()));
				dialog = dialog.replaceFirst("%loc%", String.valueOf(npc.getSpawn() != null ? npc.getSpawn().getLocation() : "0"));
				dialog = dialog.replaceFirst("%dist%", String.valueOf((int) npc.getDistance3D(player)));
				dialog = dialog.replaceFirst("%killed%", String.valueOf(npc.getTemplate().killscount));
				dialog = dialog.replaceFirst("%spReward%", String.valueOf(npc.getSpReward()));
				dialog = dialog.replaceFirst("%xyz%", npc.getLoc().x + " " + npc.getLoc().y + " " + npc.getLoc().z);
				dialog = dialog.replaceFirst("%ai_type%", npc.getAI().getL2ClassShortName());
				dialog = dialog.replaceFirst("%direction%", player.getDirectionTo(npc, true).toString().toLowerCase());
			}
			else
				dialog = Files.read("data/scripts/actions/player.L2NpcInstance.onActionShift.htm", player);

			dialog = dialog.replaceFirst("%name%", npc.getName());
			dialog = dialog.replaceFirst("%level%", String.valueOf(npc.getLevel()));
			dialog = dialog.replaceFirst("%factionId%", npc.getFactionId().equals("") ? "none" : npc.getFactionId());
			dialog = dialog.replaceFirst("%aggro%", String.valueOf(npc.getAggroRange()));
			dialog = dialog.replaceFirst("%maxHp%", String.valueOf(npc.getMaxHp()));
			dialog = dialog.replaceFirst("%maxMp%", String.valueOf(npc.getMaxMp()));
			dialog = dialog.replaceFirst("%pDef%", String.valueOf(npc.getPDef(null)));
			dialog = dialog.replaceFirst("%mDef%", String.valueOf(npc.getMDef(null, null)));
			dialog = dialog.replaceFirst("%pAtk%", String.valueOf(npc.getPAtk(null)));
			dialog = dialog.replaceFirst("%mAtk%", String.valueOf(npc.getMAtk(null, null)));
			dialog = dialog.replaceFirst("%expReward%", String.valueOf(npc.getExpReward()));
			dialog = dialog.replaceFirst("%runSpeed%", String.valueOf(npc.getRunSpeed()));

			// Дополнительная инфа для ГМов
			if(player.isGM())
				dialog = dialog.replaceFirst("%AI%", String.valueOf(npc.getAI()) + ",<br1>active: " + npc.getAI().isActive() + ",<br1>intention: " + npc.getAI().getIntention());
			else
				dialog = dialog.replaceFirst("%AI%", "");

			show(dialog, player, npc);
		}
		return true;
	}

	public String getNpcRaceById(int raceId)
	{
		switch(raceId)
		{
			case 1:
				return "Undead";
			case 2:
				return "Magic Creatures";
			case 3:
				return "Beasts";
			case 4:
				return "Animals";
			case 5:
				return "Plants";
			case 6:
				return "Humanoids";
			case 7:
				return "Spirits";
			case 8:
				return "Angels";
			case 9:
				return "Demons";
			case 10:
				return "Dragons";
			case 11:
				return "Giants";
			case 12:
				return "Bugs";
			case 13:
				return "Fairies";
			case 14:
				return "Humans";
			case 15:
				return "Elves";
			case 16:
				return "Dark Elves";
			case 17:
				return "Orcs";
			case 18:
				return "Dwarves";
			case 19:
				return "Others";
			case 20:
				return "Non-living Beings";
			case 21:
				return "Siege Weapons";
			case 22:
				return "Defending Army";
			case 23:
				return "Mercenaries";
			case 24:
				return "Unknown Creature";
			case 25:
				return "Kamael";
			default:
				return "Not defined";
		}
	}

	public void droplist()
	{
		L2Player player = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		droplist(player, npc);
	}

	public void droplist(L2Player player, L2NpcInstance npc)
	{
		if(player == null || npc == null)
			return;
		if(!Config.ALT_GAME_GEN_DROPLIST_ON_DEMAND)
			show(InfoCache.getFromDroplistCache(npc.getNpcId()), player, npc);
		else
		{
			int diff = npc.calculateLevelDiffForDrop(player.isInParty() ? player.getParty().getLevel() : player.getLevel());
			double mult = 1;
			if(diff > 0)
				mult = Experience.penaltyModifier(diff, 9);
			mult = npc.calcStat(Stats.DROP, mult, null, null);
			show(DropList.generateDroplist(npc.getTemplate(), npc.isMonster() ? (L2MonsterInstance) npc : null, mult, player), player, npc);
		}
	}

	public void quests()
	{
		L2Player player = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		StringBuilder dialog = new StringBuilder("<html><body><center><font color=\"LEVEL\">");
		dialog.append(npc.getName()).append("<br></font></center><br>");

		Quest[] list = npc.getTemplate().getEventQuests(QuestEventType.MOBKILLED);
		if(list != null && list.length != 0)
		{
			dialog.append("On kill:<br>");
			for(Quest q : list)
				dialog.append(q.getDescr(player)).append("<br1>");
		}

		dialog.append("</body></html>");
		show(dialog.toString(), player, npc);
	}

	public void skills()
	{
		L2Player player = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		StringBuilder dialog = new StringBuilder("<html><body><center><font color=\"LEVEL\">");
		dialog.append(npc.getName()).append("<br></font></center>");

		L2Skill[] list = npc.getAllSkills();
		//if(list != null && !list.isEmpty())
		//{
			dialog.append("<br>Active:<br>");
			for(L2Skill s : list)
				if(s.isActive())
					dialog.append(s.getName()).append("<br1>");
			dialog.append("<br>Passive:<br>");
			for(L2Skill s : list)
				if(!s.isActive())
					dialog.append(s.getName()).append("<br1>");
		//}

		dialog.append("</body></html>");
		show(dialog.toString(), player, npc);
	}

	public void effects()
	{
		L2Player player = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		StringBuilder dialog = new StringBuilder("<html><body><center><font color=\"LEVEL\">");
		dialog.append(npc.getName()).append("<br></font></center><br>");

		ConcurrentLinkedQueue<L2Effect> list = npc.getEffectList().getAllEffects();
		if(list != null && !list.isEmpty())
			for(L2Effect e : list)
				dialog.append(e.getSkill().getName()).append("<br1>");

		dialog.append("<br><center><button value=\"");
		dialog.append(player.isLangRus() ? "Обновить" : "Refresh");
		dialog.append("\" action=\"bypass -h scripts_actions.OnActionShift:effects\" width=100 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" /></center></body></html>");

		show(dialog.toString(), player, npc);
	}

	public void stats()
	{
		L2Player player = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		String dialog = Files.read("data/scripts/actions/player.L2NpcInstance.stats.htm", player);
		dialog = dialog.replaceFirst("%name%", npc.getName());
		dialog = dialog.replaceFirst("%level%", String.valueOf(npc.getLevel()));
		dialog = dialog.replaceFirst("%factionId%", npc.getFactionId());
		dialog = dialog.replaceFirst("%aggro%", String.valueOf(npc.getAggroRange()));
		dialog = dialog.replaceFirst("%race%", getNpcRaceById(npc.getTemplate().getRace()));
		dialog = dialog.replaceFirst("%herbs%", String.valueOf(npc.getTemplate().isDropHerbs));
		dialog = dialog.replaceFirst("%maxHp%", String.valueOf(npc.getMaxHp()));
		dialog = dialog.replaceFirst("%maxMp%", String.valueOf(npc.getMaxMp()));
		dialog = dialog.replaceFirst("%pDef%", String.valueOf(npc.getPDef(null)));
		dialog = dialog.replaceFirst("%mDef%", String.valueOf(npc.getMDef(null, null)));
		dialog = dialog.replaceFirst("%pAtk%", String.valueOf(npc.getPAtk(null)));
		dialog = dialog.replaceFirst("%mAtk%", String.valueOf(npc.getMAtk(null, null)));
		dialog = dialog.replaceFirst("%accuracy%", String.valueOf(npc.getAccuracy()));
		dialog = dialog.replaceFirst("%evasionRate%", String.valueOf(npc.getEvasionRate(null)));
		dialog = dialog.replaceFirst("%criticalHit%", String.valueOf(npc.getCriticalHit(null, null)));
		dialog = dialog.replaceFirst("%runSpeed%", String.valueOf(npc.getRunSpeed()));
		dialog = dialog.replaceFirst("%walkSpeed%", String.valueOf(npc.getWalkSpeed()));
		dialog = dialog.replaceFirst("%pAtkSpd%", String.valueOf(npc.getPAtkSpd()));
		dialog = dialog.replaceFirst("%mAtkSpd%", String.valueOf(npc.getMAtkSpd()));
		show(dialog, player, npc);
	}

	public void resists()
	{
		L2Player player = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		StringBuilder dialog = new StringBuilder("<html><body><center><font color=\"LEVEL\">");
		dialog.append(npc.getName()).append("<br></font></center><table width=\"80%\">");

		int FIRE_RECEPTIVE = (int) npc.calcStat(Stats.FIRE_RECEPTIVE, 0, null, null);
		if(FIRE_RECEPTIVE != 0)
			dialog.append("<tr><td>Fire</td><td>").append(-FIRE_RECEPTIVE).append("</td></tr>");

		int WIND_RECEPTIVE = (int) npc.calcStat(Stats.WIND_RECEPTIVE, 0, null, null);
		if(WIND_RECEPTIVE != 0)
			dialog.append("<tr><td>Wind</td><td>").append(-WIND_RECEPTIVE).append("</td></tr>");

		int WATER_RECEPTIVE = (int) npc.calcStat(Stats.WATER_RECEPTIVE, 0, null, null);
		if(WATER_RECEPTIVE != 0)
			dialog.append("<tr><td>Water</td><td>").append(-WATER_RECEPTIVE).append("</td></tr>");

		int EARTH_RECEPTIVE = (int) npc.calcStat(Stats.EARTH_RECEPTIVE, 0, null, null);
		if(EARTH_RECEPTIVE != 0)
			dialog.append("<tr><td>Earth</td><td>").append(-EARTH_RECEPTIVE).append("</td></tr>");

		int SACRED_RECEPTIVE = (int) npc.calcStat(Stats.SACRED_RECEPTIVE, 0, null, null);
		if(SACRED_RECEPTIVE != 0)
			dialog.append("<tr><td>Light</td><td>").append(-SACRED_RECEPTIVE).append("</td></tr>");

		int UNHOLY_RECEPTIVE = (int) npc.calcStat(Stats.UNHOLY_RECEPTIVE, 0, null, null);
		if(UNHOLY_RECEPTIVE != 0)
			dialog.append("<tr><td>Darkness</td><td>").append(-UNHOLY_RECEPTIVE).append("</td></tr>");

		int BLEED_RECEPTIVE = (int) npc.calcStat(Stats.BLEED_RECEPTIVE, null, null);
		if(BLEED_RECEPTIVE != 0)
			dialog.append("<tr><td>Bleed</td><td>").append(BLEED_RECEPTIVE).append("</td></tr>");

		int POISON_RECEPTIVE = (int) npc.calcStat(Stats.POISON_RECEPTIVE, null, null);
		if(POISON_RECEPTIVE != 0)
			dialog.append("<tr><td>Poison</td><td>").append(POISON_RECEPTIVE).append("</td></tr>");

		int STUN_RECEPTIVE = (int) npc.calcStat(Stats.STUN_RECEPTIVE, null, null);
		if(STUN_RECEPTIVE != 0)
			dialog.append("<tr><td>Stun</td><td>").append(STUN_RECEPTIVE).append("</td></tr>");

		int ROOT_RECEPTIVE = (int) npc.calcStat(Stats.ROOT_RECEPTIVE, null, null);
		if(ROOT_RECEPTIVE != 0)
			dialog.append("<tr><td>Root</td><td>").append(ROOT_RECEPTIVE).append("</td></tr>");

		int SLEEP_RECEPTIVE = (int) npc.calcStat(Stats.SLEEP_RECEPTIVE, null, null);
		if(SLEEP_RECEPTIVE != 0)
			dialog.append("<tr><td>Sleep</td><td>").append(SLEEP_RECEPTIVE).append("</td></tr>");

		int PARALYZE_RECEPTIVE = (int) npc.calcStat(Stats.PARALYZE_RECEPTIVE, null, null);
		if(PARALYZE_RECEPTIVE != 0)
			dialog.append("<tr><td>Paralyze</td><td>").append(PARALYZE_RECEPTIVE).append("</td></tr>");

		int MENTAL_RECEPTIVE = (int) npc.calcStat(Stats.MENTAL_RECEPTIVE, null, null);
		if(MENTAL_RECEPTIVE != 0)
			dialog.append("<tr><td>Mental</td><td>").append(MENTAL_RECEPTIVE).append("</td></tr>");

		int DEBUFF_RECEPTIVE = (int) npc.calcStat(Stats.DEBUFF_RECEPTIVE, null, null);
		if(DEBUFF_RECEPTIVE != 0)
			dialog.append("<tr><td>Debuff</td><td>").append(DEBUFF_RECEPTIVE).append("</td></tr>");

		int CANCEL_RECEPTIVE = (int) npc.calcStat(Stats.CANCEL_RECEPTIVE, null, null);
		if(CANCEL_RECEPTIVE != 0)
			dialog.append("<tr><td>Cancel</td><td>").append(CANCEL_RECEPTIVE).append("</td></tr>");

		int SWORD_WPN_RECEPTIVE = 100 - (int) npc.calcStat(Stats.SWORD_WPN_RECEPTIVE, null, null);
		if(SWORD_WPN_RECEPTIVE != 0)
			dialog.append("<tr><td>Sword</td><td>").append(SWORD_WPN_RECEPTIVE).append("%</td></tr>");

		int DUAL_WPN_RECEPTIVE = 100 - (int) npc.calcStat(Stats.DUAL_WPN_RECEPTIVE, null, null);
		if(DUAL_WPN_RECEPTIVE != 0)
			dialog.append("<tr><td>Dual Sword</td><td>").append(DUAL_WPN_RECEPTIVE).append("%</td></tr>");

		int BLUNT_WPN_RECEPTIVE = 100 - (int) npc.calcStat(Stats.BLUNT_WPN_RECEPTIVE, null, null);
		if(BLUNT_WPN_RECEPTIVE != 0)
			dialog.append("<tr><td>Blunt</td><td>").append(BLUNT_WPN_RECEPTIVE).append("%</td></tr>");

		int DAGGER_WPN_RECEPTIVE = 100 - (int) npc.calcStat(Stats.DAGGER_WPN_RECEPTIVE, null, null);
		if(DAGGER_WPN_RECEPTIVE != 0)
			dialog.append("<tr><td>Dagger/rapier</td><td>").append(DAGGER_WPN_RECEPTIVE).append("%</td></tr>");

		int BOW_WPN_RECEPTIVE = 100 - (int) npc.calcStat(Stats.BOW_WPN_RECEPTIVE, null, null);
		if(BOW_WPN_RECEPTIVE != 0)
			dialog.append("<tr><td>Bow</td><td>").append(BOW_WPN_RECEPTIVE).append("%</td></tr>");

		int XBOW_WPN_RECEPTIVE = 100 - (int) npc.calcStat(Stats.CROSSBOW_WPN_RECEPTIVE, null, null);
		if(XBOW_WPN_RECEPTIVE != 0)
			dialog.append("<tr><td>Crossbow</td><td>").append(XBOW_WPN_RECEPTIVE).append("%</td></tr>");

		int POLE_WPN_RECEPTIVE = 100 - (int) npc.calcStat(Stats.POLE_WPN_RECEPTIVE, null, null);
		if(POLE_WPN_RECEPTIVE != 0)
			dialog.append("<tr><td>Polearm</td><td>").append(POLE_WPN_RECEPTIVE).append("%</td></tr>");

		int FIST_WPN_RECEPTIVE = 100 - (int) npc.calcStat(Stats.FIST_WPN_RECEPTIVE, null, null);
		if(FIST_WPN_RECEPTIVE != 0)
			dialog.append("<tr><td>Fist weapons</td><td>").append(FIST_WPN_RECEPTIVE).append("%</td></tr>");

		if(FIRE_RECEPTIVE == 0 && WIND_RECEPTIVE == 0 && WATER_RECEPTIVE == 0 && EARTH_RECEPTIVE == 0 && UNHOLY_RECEPTIVE == 0 && SACRED_RECEPTIVE // primary elements
		== 0 && BLEED_RECEPTIVE == 0 && STUN_RECEPTIVE // phys debuff
		== 0 && POISON_RECEPTIVE == 0 && ROOT_RECEPTIVE == 0 && SLEEP_RECEPTIVE == 0 && PARALYZE_RECEPTIVE == 0 && MENTAL_RECEPTIVE == 0 && DEBUFF_RECEPTIVE == 0 && CANCEL_RECEPTIVE // mag debuff
		== 0 && SWORD_WPN_RECEPTIVE == 0 && DUAL_WPN_RECEPTIVE == 0 && BLUNT_WPN_RECEPTIVE == 0 && DAGGER_WPN_RECEPTIVE == 0 && BOW_WPN_RECEPTIVE == 0 && POLE_WPN_RECEPTIVE == 0 && FIST_WPN_RECEPTIVE == 0// weapons
		)
			dialog.append("</table>No resists</body></html>");
		else
			dialog.append("</table></body></html>");
		show(dialog.toString(), player, npc);
	}

	public void aggro()
	{
		L2Player player = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		StringBuilder dialog = new StringBuilder("<html><body><table width=\"80%\"><tr><td>Attacker</td><td>Damage</td><td>Hate</td></tr>");

		// Сортировка аггролиста по хейту
		TreeSet<AggroInfo> aggroList = new TreeSet<AggroInfo>(new Comparator<AggroInfo>(){
			@Override
			public int compare(AggroInfo o1, AggroInfo o2)
			{
				int hateDiff = o1.hate - o2.hate;
				if(hateDiff != 0)
					return hateDiff;
				return o1.damage - o2.damage;
			}
		});
		aggroList.addAll(npc.getAggroList());

		// Вывод результата
		for(AggroInfo aggroInfo : aggroList.descendingSet())
			if(aggroInfo.attacker != null && (aggroInfo.attacker.isPlayer() || aggroInfo.attacker.isSummon() || aggroInfo.attacker.isPet()))
				dialog.append("<tr><td>" + aggroInfo.attacker.getName() + "</td><td>" + aggroInfo.damage + "</td><td>" + aggroInfo.hate + "</td></tr>");

		dialog.append("</table><br><center><button value=\"");
		dialog.append(player.isLangRus() ? "Обновить" : "Refresh");
		dialog.append("\" action=\"bypass -h scripts_actions.OnActionShift:aggro\" width=100 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" /></center></body></html>");
		show(dialog.toString(), player, npc);
	}

	public boolean OnActionShift_L2DoorInstance(L2Player player, L2Object object)
	{
		if(player == null || object == null || !player.getPlayerAccess().Door || !object.isDoor())
			return false;

		String dialog;
		L2DoorInstance door = (L2DoorInstance) object;
		dialog = Files.read("data/scripts/actions/admin.L2DoorInstance.onActionShift.htm", player);
		dialog = dialog.replaceFirst("%CurrentHp%", String.valueOf((int) door.getCurrentHp()));
		dialog = dialog.replaceFirst("%MaxHp%", String.valueOf(door.getMaxHp()));
		dialog = dialog.replaceFirst("%ObjectId%", String.valueOf(door.getObjectId()));
		dialog = dialog.replaceFirst("%doorId%", String.valueOf(door.getDoorId()));
		dialog = dialog.replaceFirst("%pdef%", String.valueOf(door.getPDef(null)));
		dialog = dialog.replaceFirst("%mdef%", String.valueOf(door.getMDef(null, null)));
		dialog = dialog.replaceFirst("%siege%", door.isSiegeWeaponOnlyAttackable() ? "Siege weapon only attackable." : "");

		dialog = dialog.replaceFirst("bypass -h admin_open", "bypass -h admin_open " + door.getDoorId());
		dialog = dialog.replaceFirst("bypass -h admin_close", "bypass -h admin_close " + door.getDoorId());

		show(dialog, player);
		return true;
	}

	public boolean OnActionShift_L2Player(L2Player player, L2Object object)
	{
		if(player == null || object == null || !player.getPlayerAccess().CanViewChar)
			return false;
		if(object.isPlayer())
			AdminEditChar.showCharacterList(player, (L2Player) object);
		return true;
	}

	public boolean OnActionShift_L2SummonInstance(L2Player player, L2Object object)
	{
		if(player == null || object == null || !player.getPlayerAccess().CanViewChar)
			return false;
		if(object.isSummon())
		{
			String dialog;
			L2SummonInstance summon = (L2SummonInstance) object;
			dialog = Files.read("data/scripts/actions/admin.L2SummonInstance.onActionShift.htm");
			dialog = dialog.replaceFirst("%name%", String.valueOf(summon.getName()));
			dialog = dialog.replaceFirst("%level%", String.valueOf(summon.getLevel()));
			dialog = dialog.replaceFirst("%class%", String.valueOf(summon.getClass().getSimpleName().replaceFirst("L2", "").replaceFirst("Instance", "")));
			dialog = dialog.replaceFirst("%xyz%", summon.getLoc().x + " " + summon.getLoc().y + " " + summon.getLoc().z);
			dialog = dialog.replaceFirst("%heading%", String.valueOf(summon.getLoc().h));

			dialog = dialog.replaceFirst("%owner%", String.valueOf(summon.getPlayer().getName()));
			dialog = dialog.replaceFirst("%ownerId%", String.valueOf(summon.getPlayer().getObjectId()));

			dialog = dialog.replaceFirst("%npcId%", String.valueOf(summon.getNpcId()));
			dialog = dialog.replaceFirst("%expPenalty%", String.valueOf(summon.getExpPenalty()));

			dialog = dialog.replaceFirst("%maxHp%", String.valueOf(summon.getMaxHp()));
			dialog = dialog.replaceFirst("%maxMp%", String.valueOf(summon.getMaxMp()));
			dialog = dialog.replaceFirst("%currHp%", String.valueOf((int) summon.getCurrentHp()));
			dialog = dialog.replaceFirst("%currMp%", String.valueOf((int) summon.getCurrentMp()));

			dialog = dialog.replaceFirst("%pDef%", String.valueOf(summon.getPDef(null)));
			dialog = dialog.replaceFirst("%mDef%", String.valueOf(summon.getMDef(null, null)));
			dialog = dialog.replaceFirst("%pAtk%", String.valueOf(summon.getPAtk(null)));
			dialog = dialog.replaceFirst("%mAtk%", String.valueOf(summon.getMAtk(null, null)));
			dialog = dialog.replaceFirst("%accuracy%", String.valueOf(summon.getAccuracy()));
			dialog = dialog.replaceFirst("%evasionRate%", String.valueOf(summon.getEvasionRate(null)));
			dialog = dialog.replaceFirst("%crt%", String.valueOf(summon.getCriticalHit(null, null)));
			dialog = dialog.replaceFirst("%runSpeed%", String.valueOf(summon.getRunSpeed()));
			dialog = dialog.replaceFirst("%walkSpeed%", String.valueOf(summon.getWalkSpeed()));
			dialog = dialog.replaceFirst("%pAtkSpd%", String.valueOf(summon.getPAtkSpd()));
			dialog = dialog.replaceFirst("%mAtkSpd%", String.valueOf(summon.getMAtkSpd()));
			dialog = dialog.replaceFirst("%dist%", String.valueOf((int) summon.getRealDistance(player)));

			dialog = dialog.replaceFirst("%STR%", String.valueOf(summon.getSTR()));
			dialog = dialog.replaceFirst("%DEX%", String.valueOf(summon.getDEX()));
			dialog = dialog.replaceFirst("%CON%", String.valueOf(summon.getCON()));
			dialog = dialog.replaceFirst("%INT%", String.valueOf(summon.getINT()));
			dialog = dialog.replaceFirst("%WIT%", String.valueOf(summon.getWIT()));
			dialog = dialog.replaceFirst("%MEN%", String.valueOf(summon.getMEN()));

			show(dialog, player);
		}
		return false;
	}

	public boolean OnActionShift_L2PetInstance(L2Player player, L2Object object)
	{
		if(player == null || object == null || !player.getPlayerAccess().CanViewChar)
			return false;
		if(object.isPet())
		{
			L2PetInstance pet = (L2PetInstance) object;
			
			String dialog;
			
			dialog = Files.read("data/scripts/actions/admin.L2PetInstance.onActionShift.htm");
			dialog = dialog.replaceFirst("%name%", String.valueOf(pet.getName()));
			dialog = dialog.replaceFirst("%level%", String.valueOf(pet.getLevel()));
			dialog = dialog.replaceFirst("%class%", String.valueOf(pet.getClass().getSimpleName().replaceFirst("L2", "").replaceFirst("Instance", "")));
			dialog = dialog.replaceFirst("%xyz%", pet.getLoc().x + " " + pet.getLoc().y + " " + pet.getLoc().z);
			dialog = dialog.replaceFirst("%heading%", String.valueOf(pet.getLoc().h));

			dialog = dialog.replaceFirst("%owner%", String.valueOf(pet.getPlayer().getName()));
			dialog = dialog.replaceFirst("%ownerId%", String.valueOf(pet.getPlayer().getObjectId()));
			dialog = dialog.replaceFirst("%npcId%", String.valueOf(pet.getNpcId()));
			dialog = dialog.replaceFirst("%controlItemId%", String.valueOf(pet.getControlItem().getItemId()));

			dialog = dialog.replaceFirst("%exp%", String.valueOf(pet.getExp()));
			dialog = dialog.replaceFirst("%sp%", String.valueOf(pet.getSp()));

			dialog = dialog.replaceFirst("%maxHp%", String.valueOf(pet.getMaxHp()));
			dialog = dialog.replaceFirst("%maxMp%", String.valueOf(pet.getMaxMp()));
			dialog = dialog.replaceFirst("%currHp%", String.valueOf((int) pet.getCurrentHp()));
			dialog = dialog.replaceFirst("%currMp%", String.valueOf((int) pet.getCurrentMp()));

			dialog = dialog.replaceFirst("%pDef%", String.valueOf(pet.getPDef(null)));
			dialog = dialog.replaceFirst("%mDef%", String.valueOf(pet.getMDef(null, null)));
			dialog = dialog.replaceFirst("%pAtk%", String.valueOf(pet.getPAtk(null)));
			dialog = dialog.replaceFirst("%mAtk%", String.valueOf(pet.getMAtk(null, null)));
			dialog = dialog.replaceFirst("%accuracy%", String.valueOf(pet.getAccuracy()));
			dialog = dialog.replaceFirst("%evasionRate%", String.valueOf(pet.getEvasionRate(null)));
			dialog = dialog.replaceFirst("%crt%", String.valueOf(pet.getCriticalHit(null, null)));
			dialog = dialog.replaceFirst("%runSpeed%", String.valueOf(pet.getRunSpeed()));
			dialog = dialog.replaceFirst("%walkSpeed%", String.valueOf(pet.getWalkSpeed()));
			dialog = dialog.replaceFirst("%pAtkSpd%", String.valueOf(pet.getPAtkSpd()));
			dialog = dialog.replaceFirst("%mAtkSpd%", String.valueOf(pet.getMAtkSpd()));
			dialog = dialog.replaceFirst("%dist%", String.valueOf((int) pet.getRealDistance(player)));

			dialog = dialog.replaceFirst("%STR%", String.valueOf(pet.getSTR()));
			dialog = dialog.replaceFirst("%DEX%", String.valueOf(pet.getDEX()));
			dialog = dialog.replaceFirst("%CON%", String.valueOf(pet.getCON()));
			dialog = dialog.replaceFirst("%INT%", String.valueOf(pet.getINT()));
			dialog = dialog.replaceFirst("%WIT%", String.valueOf(pet.getWIT()));
			dialog = dialog.replaceFirst("%MEN%", String.valueOf(pet.getMEN()));

			show(dialog, player);
		}
		return false;
	}

	public boolean OnActionShift_L2ItemInstance(L2Player player, L2Object object)
	{
		if(player == null || object == null || !player.getPlayerAccess().CanViewChar)
			return false;
			
			String dialog;
			L2ItemInstance item = (L2ItemInstance) object;
			dialog = Files.read("data/scripts/actions/admin.L2ItemInstance.onActionShift.htm");
			dialog = dialog.replaceFirst("%name%", String.valueOf(item.getItem().getName()));
			dialog = dialog.replaceFirst("%objId%", String.valueOf(item.getObjectId()));
			dialog = dialog.replaceFirst("%itemId%", String.valueOf(item.getItemId()));
			dialog = dialog.replaceFirst("%grade%", String.valueOf(item.getCrystalType()));
			dialog = dialog.replaceFirst("%count%", String.valueOf(item.getCount()));

			dialog = dialog.replaceFirst("%owner%", String.valueOf(item.getPlayer()));
			dialog = dialog.replaceFirst("%ownerId%", String.valueOf(item.getPlayer().getObjectId()));
            dialog = dialog.replaceFirst("%attrElement%", String.valueOf(item.getAttackElement()));
            dialog = dialog.replaceFirst("%attrValue%", String.valueOf(item.getAttackElementAndValue()));
			dialog = dialog.replaceFirst("%enchLevel%", String.valueOf(item.getEnchantLevel()));
			dialog = dialog.replaceFirst("%type%", String.valueOf(item.getItemType()));

			dialog = dialog.replaceFirst("%dropTime%", String.valueOf((int) item.getDropTimeOwner()));
			dialog = dialog.replaceFirst("%dropOwner%", String.valueOf(item.getItemDropOwner()));
			dialog = dialog.replaceFirst("%dropOwnerId%", String.valueOf(item.getItemDropOwner().getObjectId()));

			show(dialog, player);
		return false;
	}
}
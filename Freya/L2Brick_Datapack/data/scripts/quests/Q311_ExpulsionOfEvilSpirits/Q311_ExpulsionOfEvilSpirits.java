package quests.Q311_ExpulsionOfEvilSpirits;

import java.util.HashMap;
import l2.brick.Config;
import l2.brick.gameserver.ai.CtrlIntention;
import l2.brick.gameserver.model.Location;
import l2.brick.gameserver.model.actor.L2Attackable;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.actor.position.CharPosition;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.State;
import l2.brick.gameserver.model.quest.QuestState;
import l2.brick.gameserver.model.quest.jython.QuestJython;
import l2.brick.gameserver.util.Util;


public class Q311_ExpulsionOfEvilSpirits extends QuestJython
{
	private static final String qn = "311_ExpulsionOfEvilSpirits";
	
	private static final int CHAIREN = 32655;
	
	private static final int RAGNA_ORCS_AMULET = 14882;		//Ragna Orc's Amulet
	private static final int SOUL_CORE = 14881;			//Soul Core Containing Evil Spirit
	private static final int SOUL_PENDANT = 14848;			//Protection Soul's Pendant
	
	private static boolean SPAWN_VARANGKA = false;
	
	private static final HashMap<L2Npc, L2Npc> PAGES = new HashMap<L2Npc, L2Npc>();
	private static final HashMap<L2Npc, L2Npc> RETAINERS = new HashMap<L2Npc, L2Npc>();
	
	private static final int SHAMAN_VARANGKA = 18808;
	private static final int VARANGKA_RETAINER = 18809;
	private static final int VARANGKA_PAGE = 18810;
	private static final int VARANGKA_GUARDIAN = 22700;
	private static final int GUARDIAN_OF_THE_ALTAR = 18811;
	
	private static L2Npc ALTAR;
	private static L2Npc VARANGKA;
	
	
	private static final Location ALTAR_LOCATION = new Location(74120, -101920, -960, 32760);
	
	private static final int[] MOBS = new int[]
	{
		22691,		//Ragna Orc (Spirit Infested)
		22692,		//Ragna Orc Warrior (Spirit Infested)
		22693,		//Ragna Orc Hero (Spirit Infested)
		22694,		//Ragna Orc Commander (Spirit Infested)
		22695,		//Ragna Orc Healer (Spirit Infested)
		22696,		//Ragna Orc Shaman (Spirit Infested)
		22697,		//Ragna Orc Seer (Spirit Infested)
		22698,		//Ragna Orc Archer (Spirit Infested)
		22699,		//Ragna Orc Sniper (Spirit Infested)
		22700,		//Varangka's Guardian
		22701,		//Varangka's Dre Vanul
		22702		//Varangka's Destroyer
	};
	
	private Q311_ExpulsionOfEvilSpirits(int questId, String name, String descr)
	{
		super(questId, name, descr);
		questItemIds = new int[] { SOUL_CORE, RAGNA_ORCS_AMULET };
		addStartNpc(CHAIREN);
		addTalkId(CHAIREN);
		for(int mob : MOBS)
			addKillId(mob);
		
		ALTAR = addSpawn(GUARDIAN_OF_THE_ALTAR, ALTAR_LOCATION.getX(), ALTAR_LOCATION.getY(), ALTAR_LOCATION.getZ(), ALTAR_LOCATION.getHeading(), false, 0);
		ALTAR.setIsImmobilized(true);
		ALTAR.setIsMortal(false);
		
		addAttackId(GUARDIAN_OF_THE_ALTAR);
		addAttackId(SHAMAN_VARANGKA);
		addKillId(SHAMAN_VARANGKA);
		addKillId(VARANGKA_PAGE);
		addKillId(VARANGKA_RETAINER);
	}
	
	private L2Npc addMinion(int npcID, int x, int y, int z, int heading)
	{
		L2Npc varangkaMinion = addSpawn(npcID, x, y, z, heading, false, 0);
		varangkaMinion.setRunning();
		((L2Attackable)varangkaMinion).addDamageHate(VARANGKA.getTarget().getActingPlayer(), 1, 99999);
		varangkaMinion.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, VARANGKA.getTarget().getActingPlayer());
		return varangkaMinion;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		QuestState st = attacker.getQuestState(qn);
		if (st == null) return "";
		
		int npcID = npc.getNpcId();
		if (npcID == GUARDIAN_OF_THE_ALTAR && st.getQuestItemsCount(SOUL_PENDANT) > 0 && !SPAWN_VARANGKA)
			if (st.getRandom(100) < 10)
			{
				SPAWN_VARANGKA = true;
				st.takeItems(SOUL_PENDANT, 1);
				
				VARANGKA = addSpawn(SHAMAN_VARANGKA, 74945, -101924, -967, ALTAR_LOCATION.getHeading(), false, 0);
				((L2Attackable)VARANGKA).addDamageHate(attacker, 1, 99999);
				VARANGKA.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
				
				addMinion(VARANGKA_PAGE, 74722, -101651, -967, ALTAR_LOCATION.getHeading());
				addMinion(VARANGKA_RETAINER, 74659, -101618, -967, ALTAR_LOCATION.getHeading());
			}
		
		if (npcID == SHAMAN_VARANGKA)
			if (st.getRandom(100) < 10)
			{
				if (st.getRandom(100) < 5)
				{
					CharPosition position = npc.getPosition();
					L2Npc page = addMinion(VARANGKA_PAGE, position.getX() - 80, position.getY(), position.getZ(), position.getHeading());					
					L2Npc retainer = addMinion(VARANGKA_RETAINER, position.getX() - 80, position.getY(), position.getZ(), position.getHeading());
					PAGES.put(page, retainer);
					RETAINERS.put(retainer, page);
				}
				
				int newX = st.getRandom(600); 
				int newY = 600 - newX;
				newX = st.getRandom(100) < 50 ? -newX : newX;
				newY = st.getRandom(100) < 50 ? -newY : newY;
				
				npc.teleToLocation(ALTAR_LOCATION.getX() - newX, ALTAR_LOCATION.getY() - newY, ALTAR_LOCATION.getZ(), ALTAR_LOCATION.getHeading(), false);
				((L2Attackable)VARANGKA).addDamageHate(attacker, 1, 99999);
				VARANGKA.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
			}
		return "";
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null) return Quest.getNoQuestMsg(player);
		
		if (event.equalsIgnoreCase("32655-yes.htm"))
		{
			st.set("cond","1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("32655-14.htm"))
		{
			long count = st.getQuestItemsCount(SOUL_CORE);
			if (count >= 10)
			{
				st.takeItems(SOUL_CORE, 10);
				st.giveItems(SOUL_PENDANT,1);				
				st.playSound("ItemSound.quest_finish");
			}
			else
				htmltext = "32655-14no.htm";		
		}
		else if (event.equalsIgnoreCase("32655-quit.htm"))
		{
			st.unset("cond");
			st.exitQuest(true);
			st.playSound("ItemSound.quest_finish");
		}
		else if (event.equalsIgnoreCase("respawn"))
		{
			ALTAR = addSpawn(GUARDIAN_OF_THE_ALTAR, ALTAR_LOCATION.getX(), ALTAR_LOCATION.getY(), ALTAR_LOCATION.getZ(), ALTAR_LOCATION.getHeading(), false, 0);
			ALTAR.setIsImmobilized(true);
			ALTAR.setIsMortal(false);
		}	
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)	
			return null;
		
		if (st.getInt("cond") == 1 )
		{
			if (Util.contains(MOBS, npc.getNpcId())) 
			{
				int chance = (int) (65 * Config.RATE_QUEST_DROP);
				int numItems = (int) (chance / 100);
				chance = chance % 100;
				if (st.getRandom(100) < chance)
					numItems++;
				if (numItems > 0)
				{
					st.playSound("ItemSound.quest_itemget");
					st.giveItems(RAGNA_ORCS_AMULET, numItems);
				}
				int chance2 = (int) (2);
				int numItems2 = (int) (chance2 / 100);
				chance2 = chance2 % 100;
				if (st.getRandom(100) < chance2)
					numItems2++;
				if (numItems2 > 0)
				{
						st.playSound("ItemSound.quest_itemget");
						st.giveItems(SOUL_CORE, numItems2);
				}
			}
			else if (npc.getNpcId() == SHAMAN_VARANGKA)
			{
				ALTAR.setIsMortal(true);
				ALTAR.doDie(player);
				ALTAR.setIsMortal(false);
				SPAWN_VARANGKA = false;
				startQuestTimer("respawn", 600000, ALTAR, player);
			}
			else if (npc.getNpcId() == VARANGKA_PAGE)
			{
				if (PAGES.containsKey(npc) && PAGES.get(npc).isDead())
				{
					RETAINERS.remove(PAGES.get(npc));
					PAGES.remove(npc);
					addSpawn(VARANGKA_GUARDIAN, VARANGKA.getX(), VARANGKA.getY(), VARANGKA.getZ(), VARANGKA.getHeading(), false, 0).setTarget(player);
				}
			}
			else if (npc.getNpcId() == VARANGKA_RETAINER)
			{
				if (RETAINERS.containsKey(npc) && RETAINERS.get(npc).isDead())
				{
					PAGES.remove(RETAINERS.get(npc));
					RETAINERS.remove(npc);
					addSpawn(VARANGKA_GUARDIAN, VARANGKA.getX(), VARANGKA.getY(), VARANGKA.getZ(), VARANGKA.getHeading(), false, 0).setTarget(player);
				}
			}
		}
		return null;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		String htmlText = Quest.getNoQuestMsg(player);
		
		if (st == null)	return htmlText;
		
		int npcID = npc.getNpcId();
		int cond = st.getInt("cond");
		
		if (npcID == CHAIREN)
		{
			if (cond == 0)
				if (player.getLevel() >= 80)
					htmlText = "32655-01.htm";
				else
				{
					htmlText = "32655-lvl.htm";
					st.exitQuest(true);
				}
			else if (cond == 1)
				if (st.getQuestItemsCount(SOUL_CORE) > 0 || st.getQuestItemsCount(RAGNA_ORCS_AMULET) > 0)
					htmlText = "32655-12.htm";
				else
					htmlText = "32655-10.htm";
		}		
		return htmlText;
	}
	
	public static void main(String[] args)
	{
		new Q311_ExpulsionOfEvilSpirits(311, qn, "Expulsion of Evil Spirits");
				if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Quest: Expulsion of Evil Spirits!");
	}
}

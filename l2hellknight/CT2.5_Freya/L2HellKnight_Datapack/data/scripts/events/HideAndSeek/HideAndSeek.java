package events.HideAndSeek;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import javolution.util.FastList;

import l2.hellknight.Config;
import l2.hellknight.L2DatabaseFactory;
import l2.hellknight.gameserver.ThreadPoolManager;
import l2.hellknight.gameserver.instancemanager.QuestManager;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.entity.DM;
import l2.hellknight.gameserver.model.entity.LMEvent;
import l2.hellknight.gameserver.model.entity.TvTEvent;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.network.clientpackets.Say2;
import l2.hellknight.gameserver.network.serverpackets.CreatureSay;
import l2.hellknight.gameserver.templates.StatsSet;
import l2.hellknight.util.Rnd;

public class HideAndSeek extends Quest {

	private static final String qn = "HideAndSeek";
	
	public static final boolean ENABLE_EVENT = Config.ALT_HAS_ENABLE;
	protected static final String EVENT_HOURS = "Config.ALT_HAS_TIME";
	protected static final int REGISTRATION_MINS_DURATION = Config.ALT_HAS_TIME_REG;
	protected static final int EVENT_MINS_DURATION = Config.ALT_HAS_TIME_EVENT;
	protected static final int REG_NPC = Config.ALT_HAS_NPC;
	protected static final int[] REG_NPC_COORDS = { 82698, 148638, -3473, 1 };
	protected static boolean PK_PLAYERS_CAN_JOIN = Config.ALT_HAS_PKJOIN;
	//true = Npcs from hide_and_seek table will be used in the order you added them
	//false = Npcs will be token randomly
	protected static boolean SECUENTIAL_NPC = Config.ALT_HAS_SECUENTIAL;
	// Min and Max levels and players amount.
	// Specify a value or put -1 to disable that config
	// (E.g.: MIN_LEVEL -1: Theres no min level to join the event)
	protected static int MIN_LEVEL = Config.ALT_HAS_MINLEVEL;
	protected static int MAX_LEVEL = Config.ALT_HAS_MAXLEVEL;
	protected static int MIN_PLAYERS = Config.ALT_HAS_MINPLAYERS;
	protected static int MAX_PLAYERS = Config.ALT_HAS_MAXPLAYERS;
	
	public static boolean _canRegister = false;
	public static boolean _active = false;
		
	private static int HIDE_NPC = 0;
	public static final Engine _event = new Engine();
	
	private static int lastNpc = 0;
	private static int nextNpc = 0;
	
	private static final List<StatsSet> hideNpcs = new FastList<StatsSet>();
	protected static final  FastList<L2PcInstance> _players = new FastList<L2PcInstance>();
	
	
	public HideAndSeek(int questId, String name, String descr) {
		super(questId, name, descr);
		addStartNpc(REG_NPC);
		addFirstTalkId(REG_NPC);
		addTalkId(REG_NPC);
		loadHolders();
		for(StatsSet holder : hideNpcs) {
			if(holder != null)
				addFirstTalkId(holder.getInteger("npc"));
			else
				_log.config("Null holder!");
		}
		scheduleEventStart();
	}
	
	public String onFirstTalk(L2Npc npc, L2PcInstance player) {
		if(!_active)
			return null;
		if(npc == null || player == null)
			return null;
		
		String htmltext = "";
		int id = npc.getNpcId();
		
		if(id == REG_NPC) {
			QuestState st = player.getQuestState(getName());
			if (st == null) {
				Quest q = QuestManager.getInstance().getQuest(getName());
				st = q.newQuestState(player);
			}
			htmltext = "registration_main.htm";
		}
		else if(id == HIDE_NPC) {
			if(_players.contains(player)) {
				_event.endGame(player);
				htmltext = "winner.htm";
			}
		}
		return htmltext;
	}
	
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player) {
		if(!_active || !_canRegister)
			return null;
		if(npc == null || player == null)
			return null;

		String htmltext = "";
		
		if(event.equalsIgnoreCase("register")) {
			if(addPlayer(player))
				htmltext = "registered.htm";
		}
		else if(event.equalsIgnoreCase("unregister")) {
			if(removePlayer(player))
				htmltext = "unregistered.htm";
		}
		return htmltext;
	}
	
	private static boolean addPlayer(L2PcInstance player) {
		int level = player.getLevel();		
		if(player.isInOlympiadMode())
			player.sendPacket(message("Cannot join while in olympiad"));
		else if(HideAndSeek.PK_PLAYERS_CAN_JOIN && (player.getKarma() > 0 || player.isCursedWeaponEquipped()))
			player.sendPacket(message("Cannot join while holding karma or cursed weapon"));
		else if(TvTEvent.isPlayerParticipant(player.getObjectId()))
			player.sendPacket(message("Cannot join while in TvT Event"));
		else if(LMEvent.isPlayerParticipant(player.getObjectId()))
			player.sendPacket(message("Cannot join while in LM Event"));
		else if(DM.isPlayerParticipant(player.getObjectId()))
			player.sendPacket(message("Cannot join while in DM Event"));
		else if(_players.contains(player))
			player.sendPacket(message("You are alredy in the waiting list for the event!"));
		else if(HideAndSeek.MIN_LEVEL != -1 && level < HideAndSeek.MIN_LEVEL) 
			player.sendPacket(message("Your level is too low, min level is "+HideAndSeek.MIN_LEVEL));
		else if(HideAndSeek.MAX_LEVEL != -1 && level > HideAndSeek.MAX_LEVEL)
			player.sendPacket(message("Your level is too high, max level is "+HideAndSeek.MAX_LEVEL));
		else if(HideAndSeek.MAX_PLAYERS != -1 && _players.size() == HideAndSeek.MAX_PLAYERS)
			player.sendPacket(message("Max amount of player for event reached, you cannot join."));
		else {
			_players.add(player);
			player.sendPacket(message("You have been added for the Hide and Seek Event!"));
			return true;
		}
		return false;
	}

	private static boolean removePlayer(L2PcInstance player) {
		if(!_players.contains(player))
			return false;
		_players.remove(player);
		player.sendPacket(message("You have been removed from the event!"));
		return true;
	}
	
	private static final CreatureSay message(String message){
		final CreatureSay say = new CreatureSay(0, Say2.TELL, "Hide & Seek", message);
		return say;
	}
	
	protected static final void clue(String message) {
		final CreatureSay say = new CreatureSay(0, Say2.PARTY, "Hide & Seek", message);
		for(L2PcInstance plr : _players) {
			if(plr != null)
				plr.sendPacket(say);
		}
	}
	
	private static final void loadHolders() {
		Connection con = null;
		try {
			read();
			
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM hide_and_seek");
			ResultSet rset = statement.executeQuery();
			
			while(rset.next()) {
				StatsSet set = new StatsSet();
				
				set.set("npc", rset.getInt("npc"));
				
				set.set("x", rset.getInt("x"));
				set.set("y", rset.getInt("y"));
				set.set("z", rset.getInt("z"));
				
				set.set("0_clue", rset.getString("first_clue"));
				set.set("1_clue", rset.getString("second_clue"));
				set.set("2_clue", rset.getString("third_clue"));
				
				set.set("rewards", rset.getString("rewards"));

				hideNpcs.add(set);
			}
			rset.close();
			statement.close();
		} catch(Exception e) {
			System.out.println("Couldnt Load Hided npc stats!");
			e.printStackTrace();
		} finally {
			try {
				if(con != null)
					con.close();
			} catch(Exception e) {
				if(Config.DEBUG)
					e.printStackTrace();
			}
		}
	}
	
	protected static StatsSet getNextNpc() {
		if(nextNpc == hideNpcs.size())
			nextNpc = 0;
		StatsSet holder = hideNpcs.get(nextNpc);
		while(holder.getInteger("npc") == lastNpc) {
			nextNpc++;
			if(nextNpc == hideNpcs.size())
				nextNpc = 0;
			holder = hideNpcs.get(nextNpc);
		}
		nextNpc++;
		lastNpc = holder.getInteger("npc");
		save();
		return holder;
	}
	
	/**
	 * Get a random holder which contains a different npc
	 * id than <b>lastNpc</b>
	 * @return InfoHolder
	 */
	protected static StatsSet getRandomNpc() {
		int size = hideNpcs.size();
		int pos = Rnd.get(size);
		StatsSet holder = hideNpcs.get(pos);
		// Keep getting a new holder while hodler.getNpc() equals lastNpc
		while(holder.getInteger("npc") == lastNpc) {
			pos = Rnd.get(size);
			holder = hideNpcs.get(pos);
		}
		lastNpc = holder.getInteger("npc");
		save();
		return holder;	
	}
		
	private static final void read() {
		try {
			File file = new File(Config.DATAPACK_ROOT, "data/scripts/events/HideAndSeek/npc_save.properties");
			FileInputStream reader = new FileInputStream(file);
			Properties prop = new Properties();
			prop.load(reader);
			lastNpc = Integer.parseInt(prop.getProperty("lastNpc", "0"));
			nextNpc = Integer.parseInt(prop.getProperty("nextNpc", "0"));
		} catch (IOException ioe) {
			System.out.println("Hide and Seek: Error while reading last and next npc from npc_save!");
			ioe.printStackTrace();
		}
	}
	
	private static final void save() {
		try {
			File file = new File(Config.DATAPACK_ROOT, "data/scripts/events/HideAndSeek/npc_save.properties");
			FileInputStream reader = new FileInputStream(file);
			Properties prop = new Properties();
			prop.load(reader);
			prop.setProperty("lastNpc", String.valueOf(lastNpc));
			prop.setProperty("nextNpc", String.valueOf(nextNpc));
			prop.store(new FileOutputStream(file), "");
		} catch (IOException ioe) {
			System.out.println("Hide and Seek: Error while saving last and next npc to npc_save!");
			ioe.printStackTrace();
		}
	}
	
	private static void scheduleEventStart() {
		try {
			Calendar currentTime = Calendar.getInstance();
			Calendar nextStartTime = null;
			Calendar testStartTime = null;
			for (String timeOfDay : EVENT_HOURS.split(",")) {
				// Creating a Calendar object from the specified interval value
				testStartTime = Calendar.getInstance();
				testStartTime.setLenient(true);
				String[] splitTimeOfDay = timeOfDay.split(":");
				testStartTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splitTimeOfDay[0]));
				testStartTime.set(Calendar.MINUTE, Integer.parseInt(splitTimeOfDay[1]));
				if (testStartTime.getTimeInMillis() < currentTime.getTimeInMillis()) {
					testStartTime.add(Calendar.DAY_OF_MONTH, 1);
				}
				if (nextStartTime == null || testStartTime.getTimeInMillis() < nextStartTime.getTimeInMillis()) {
					nextStartTime = testStartTime;
				}
			}
			long delay = nextStartTime.getTimeInMillis() - currentTime.getTimeInMillis();
			ThreadPoolManager.getInstance().scheduleGeneral(new ScheduledEventStart(), delay);
		} catch (Exception e) {
			_log.warning("Couldnt Initialize Hide and Seek Event");
			e.printStackTrace();
		}
	}
		
	static class ScheduledEventStart implements Runnable {
		@Override
		public void run() {
			if(_active)
				return;
			
			_canRegister = true;
			_active = true;
			StatsSet holder = null;
			
				holder = SECUENTIAL_NPC? getNextNpc() : getRandomNpc();
			
			if(holder == null) {
				System.out.println("Hide and Seek: Stopped due null InfoHolder");
				return;
			}
			HIDE_NPC = holder.getInteger("npc");
			_event.setHolder(holder);
			_event.launchGame();
		}
	}
	
	public static void main(String...args) {
		if(ENABLE_EVENT) {
			new HideAndSeek(-1, qn, "events");
			_log.config("Hide and Seek event: Sucessfully loaded!");
		}
	}
}
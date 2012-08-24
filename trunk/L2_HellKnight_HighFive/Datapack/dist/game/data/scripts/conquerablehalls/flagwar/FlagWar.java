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
package conquerablehalls.flagwar;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import l2.hellknight.L2DatabaseFactory;
import l2.hellknight.gameserver.Announcements;
import l2.hellknight.gameserver.ThreadPoolManager;
import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.ai.L2SpecialSiegeGuardAI;
import l2.hellknight.gameserver.cache.HtmCache;
import l2.hellknight.gameserver.datatables.ClanTable;
import l2.hellknight.gameserver.datatables.NpcTable;
import l2.hellknight.gameserver.instancemanager.MapRegionManager.TeleportWhereType;
import l2.hellknight.gameserver.model.L2CharPosition;
import l2.hellknight.gameserver.model.L2Clan;
import l2.hellknight.gameserver.model.L2ClanMember;
import l2.hellknight.gameserver.model.L2SiegeClan;
import l2.hellknight.gameserver.model.L2SiegeClan.SiegeClanType;
import l2.hellknight.gameserver.model.L2Spawn;
import l2.hellknight.gameserver.model.L2World;
import l2.hellknight.gameserver.model.Location;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.actor.templates.L2NpcTemplate;
import l2.hellknight.gameserver.model.entity.Siegable;
import l2.hellknight.gameserver.model.entity.clanhall.ClanHallSiegeEngine;
import l2.hellknight.gameserver.model.entity.clanhall.SiegeStatus;
import l2.hellknight.gameserver.model.zone.type.L2ResidenceHallTeleportZone;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.NpcHtmlMessage;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;

/**
 * @author BiggBoss
 */
public abstract class FlagWar extends ClanHallSiegeEngine
{
	protected static String qn;
	
	private static final String SQL_LOAD_ATTACKERS			= "SELECT * FROM siegable_hall_flagwar_attackers WHERE hall_id = ?";
	private static final String SQL_SAVE_ATTACKER 			= "INSERT INTO siegable_hall_flagwar_attackers_members VALUES (?,?,?)";
	private static final String SQL_LOAD_MEMEBERS			= "SELECT object_id FROM siegable_hall_flagwar_attackers_members WHERE clan_id = ?";
	private static final String SQL_SAVE_CLAN 				= "INSERT INTO siegable_hall_flagwar_attackers VALUES(?,?,?,?)";
	private static final String SQL_SAVE_NPC				= "UPDATE siegable_hall_flagwar_attackers SET npc = ? WHERE clan_id = ?";
	private static final String SQL_CLEAR_CLAN 				= "DELETE FROM siegable_hall_flagwar_attackers WHERE hall_id = ?";
	private static final String SQL_CLEAR_CLAN_ATTACKERS 	= "DELETE FROM siegable_hall_flagwar_attackers_members WHERE hall_id = ?";

	protected static int ROYAL_FLAG;
	protected static int FLAG_RED;
	protected static int FLAG_YELLOW;
	protected static int FLAG_GREEN;
	protected static int FLAG_BLUE;
	protected static int FLAG_PURPLE;
	
	protected static int ALLY_1;
	protected static int ALLY_2;
	protected static int ALLY_3;
	protected static int ALLY_4;
	protected static int ALLY_5;
	
	protected static int TELEPORT_1;
	
	protected static int MESSENGER;
	
	protected static int[] OUTTER_DOORS_TO_OPEN = new int[2];
	protected static int[] INNER_DOORS_TO_OPEN = new int[2];
	protected static Location[] FLAG_COORDS = new Location[7];
	
	protected static L2ResidenceHallTeleportZone[] TELE_ZONES = new L2ResidenceHallTeleportZone[6];
	
	protected static int QUEST_REWARD;
	
	protected static L2CharPosition CENTER;
	
	protected TIntObjectHashMap<ClanData> _data =  new TIntObjectHashMap<>(6);
	protected L2Clan _winner;
	private boolean _firstPhase;
	
	public FlagWar(int questId, String name, String descr, int hallId)
	{
		super(questId, name, descr, hallId);
		
		addStartNpc(MESSENGER);
		addFirstTalkId(MESSENGER);
		addTalkId(MESSENGER);
		
		for(int i = 0; i < 6; i++)
			addFirstTalkId(TELEPORT_1 + i);
		
		addKillId(ALLY_1);
		addKillId(ALLY_2);
		addKillId(ALLY_3);
		addKillId(ALLY_4);
		addKillId(ALLY_5);
		
		addSpawnId(ALLY_1);
		addSpawnId(ALLY_2);
		addSpawnId(ALLY_3);
		addSpawnId(ALLY_4);
		addSpawnId(ALLY_5);

		// If siege ends w/ more than 1 flag alive, winner is old owner
		_winner = ClanTable.getInstance().getClan(_hall.getOwnerId());
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		String html = null;
		/*
		if(player.getQuestState(qn) == null)
			newQuestState(player);
		*/
		if(npc.getNpcId() == MESSENGER)
		{
			if(!checkIsAttacker(player.getClan()))
			{
				L2Clan clan = ClanTable.getInstance().getClan(_hall.getOwnerId());
				String content = HtmCache.getInstance().getHtm(null, "data/scripts/conquerablehalls/flagwar/"+qn+"/messenger_initial.htm");
				content = content.replaceAll("%clanName%", clan == null? "no owner" : clan.getName());
				content = content.replaceAll("%objectId%", String.valueOf(npc.getObjectId()));
				html = content;
			}
			else
				html = "messenger_initial.htm";
		}
		else
		{
			int index = npc.getNpcId() - TELEPORT_1;
			if(index == 0 && _firstPhase)
				html = "teleporter_notyet.htm";
			else
			{
				TELE_ZONES[index].checkTeleporTask();
				html = "teleporter.htm";
			}
		}
		return html;
	}
	
	@Override
	public synchronized String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String html = event;
		L2Clan clan = player.getClan();
				
		if(event.startsWith("register_clan")) // Register the clan for the siege
		{
			if(!_hall.isRegistering())
			{
				if(_hall.isInSiege())
					html = "messenger_registrationpassed.htm";
				else
				{
					sendRegistrationPageDate(player);
					return null;
				}
			}
			else if(clan == null || !player.isClanLeader())
				html = "messenger_notclannotleader.htm";
			else if(getAttackers().size() >= 5)
				html = "messenger_attackersqueuefull.htm";
			else if(checkIsAttacker(clan))
				html = "messenger_clanalreadyregistered.htm";
			else if(_hall.getOwnerId() == clan.getClanId())
				html = "messenger_curownermessage.htm";
			else
			{
				String[] arg = event.split(" ");
				if(arg.length >= 2)
				{
					// Register passing the quest
					if(arg[1].equals("wQuest"))
					{
						if(player.destroyItemByItemId(_hall.getName()+" Siege", QUEST_REWARD, 1, npc, false)) // Quest passed
						{
							registerClan(clan);
							html = getFlagHtml(_data.get(clan.getClanId()).flag);
						}
						else // Quest not accomplished, try by paying
							html = "messenger_noquest.htm";
					}
					// Register paying the fee
					else if(arg[1].equals("wFee") && canPayRegistration())
					{
						if(player.reduceAdena(qn+" Siege", 200000, npc, false)) // Fee payed
						{
							registerClan(clan);
							html = getFlagHtml(_data.get(clan.getClanId()).flag);
						}
						else // Fee couldnt be payed, try with quest
							html = "messenger_nomoney.htm";
					}
				}
			}
		}
		// Select the flag to defend
		else if(event.startsWith("select_clan_npc"))
		{
			if(!player.isClanLeader())
				html = "messenger_onlyleaderselectally.htm";
			else if(!_data.containsKey(clan.getClanId()))
				html = "messenger_clannotregistered.htm";
			else
			{
				String[] var = event.split(" ");
				if(var.length >= 2)
				{
					int id = 0;
					try { id = Integer.parseInt(var[1]); }
					catch(Exception e)
					{
						_log.warning(qn+"->select_clan_npc->Wrong mahum warrior id: "+var[1]);
					}
					if(id > 0 && (html = getAllyHtml(id)) != null)
					{
						_data.get(clan.getClanId()).npc = id;
						saveNpc(id, clan.getClanId());
					}
				}
				else
					_log.warning(qn+" Siege: Not enough parameters to save clan npc for clan: "+clan.getName());
			}
		}
		// View (and change ? ) the current selected mahum warrior
		else if(event.startsWith("view_clan_npc"))
		{
			ClanData cd = null;
			if(clan == null)
				html = "messenger_clannotregistered.htm";
			else if((cd = _data.get(clan.getClanId())) == null)
				html = "messenger_notclannotleader.htm";
			else if(cd.npc == 0)
				html = "messenger_leaderdidnotchooseyet.htm";
			else
				html = getAllyHtml(cd.npc);
		}
		// Register a clan member for the fight
		else if(event.equals("register_member"))
		{
			if(clan == null)
				html = "messenger_clannotregistered.htm";
			else if(!_hall.isRegistering())
				html = "messenger_registrationpassed.htm";
			else if(!_data.containsKey(clan.getClanId()))
				html = "messenger_notclannotleader.htm";
			else if(_data.get(clan.getClanId()).players.size() >= 18)
				html = "messenger_clanqueuefull.htm";
			else
			{
				ClanData data = _data.get(clan.getClanId());
				data.players.add(player.getObjectId());
				saveMember(clan.getClanId(), player.getObjectId());
				if(data.npc == 0)
					html = "messenger_leaderdidnotchooseyet.htm";
				else
					html = "messenger_clanregistered.htm";
			}
		}
		// Show cur attacker list
		else if(event.equals("view_attacker_list"))
		{
			if(_hall.isRegistering())
				sendRegistrationPageDate(player);
			else
			{
				html = HtmCache.getInstance().getHtm(null, "data/scripts/conquerablehalls/flagwar/"+qn+"/messenger_registeredclans.htm");
				for(int i = 0; i < _data.size(); i++)
				{
					L2Clan attacker = ClanTable.getInstance().getClan(_data.keys()[i]);
					if(attacker == null)
						continue;
					html = html.replaceAll("%clan"+i+"%", clan.getName());
					html = html.replaceAll("%clanMem"+i+"%", String.valueOf(_data.values()[i].players.size()));
				}
				if(_data.size() < 5)
				{
					for(int i = _data.size(); i < 5; i++)
					{
						html = html.replaceAll("%clan"+i+"%", "Empty pos. ");
						html = html.replaceAll("%clanMem"+i+"%", "Empty pos. ");
					}
				}
			}
		}
		
		return html;
	}
	
	@Override
	public synchronized String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if(_hall.isInSiege())
		{
			final int npcId = npc.getNpcId();
			for(int keys : _data.keys())
				if(_data.get(keys).npc == npcId)
					removeParticipant(keys, true);
			_data.trimToSize();
			
			synchronized(this)
			{
				if(_firstPhase)
				{
					// Siege ends if just 1 flag is alive
					if((_data.size() == 1 && _hall.getOwnerId() <= 0)	// Hall was free before battle
						|| _data.values()[0].npc == 0) 					// or owner didnt set the ally npc
					{
						_missionAccomplished = true;
						_winner = ClanTable.getInstance().getClan(_data.keys()[0]);
						removeParticipant(_data.keys()[0], false);
						cancelSiegeTask();
						endSiege();
					}
					else if(_data.size() == 2 && _hall.getOwnerId() > 0) // Hall has defender (owner)
					{
						cancelSiegeTask();	// No time limit now
						_firstPhase = false;
						_hall.getSiegeZone().setIsActive(false);
						for(int doorId : INNER_DOORS_TO_OPEN)
							_hall.openCloseDoor(doorId, true);
						
						for(ClanData data : _data.values())
							doUnSpawns(data);
						
						ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
						{
							@Override
							public void run()
							{
								for(int doorId : INNER_DOORS_TO_OPEN)
									_hall.openCloseDoor(doorId, false);
								
								for(int i = 0; i< _data.size(); i++)
									doSpawns(_data.keys()[i], _data.values()[i]);
								
								_hall.getSiegeZone().setIsActive(true);
							}
						}, 300000);
					}
				}
				else
				{
					_missionAccomplished = true;
					_winner = ClanTable.getInstance().getClan(_data.keys()[0]);
					removeParticipant(_data.keys()[0], false);
					endSiege();
				}
			}
		}
		return null;
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CENTER);
		return null;
	}
	
	@Override
	public L2Clan getWinner()
	{
		return _winner;
	}
	
	@Override
	public void prepareOwner()
	{
		if(_hall.getOwnerId() > 0)
			registerClan(ClanTable.getInstance().getClan(_hall.getOwnerId()));
		
		_hall.banishForeigners();
		SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.REGISTRATION_TERM_FOR_S1_ENDED);
		msg.addString(getName());
		Announcements.getInstance().announceToAll(msg);
		_hall.updateSiegeStatus(SiegeStatus.WAITING_BATTLE);
		
		_siegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new SiegeStarts(), 3600000);
	}
	
	@Override
	public void startSiege()
	{
		if(getAttackers().size() < 2)
		{
			onSiegeEnds();
			getAttackers().clear();
			_hall.updateNextSiege();
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST);
			sm.addString(_hall.getName());
			Announcements.getInstance().announceToAll(sm);
			return;
		}
		
		// Open doors for challengers
		for(int door : OUTTER_DOORS_TO_OPEN)
			_hall.openCloseDoor(door, true);
		
		// Teleport owner inside
		if(_hall.getOwnerId() > 0)
		{
			L2Clan owner = ClanTable.getInstance().getClan(_hall.getOwnerId());
			final Location loc = _hall.getZone().getSpawns().get(0); // Owner restart point
			for(L2ClanMember pc : owner.getMembers())
			{
				if(pc != null)
				{
					final L2PcInstance player = pc.getPlayerInstance();
					if(player != null && player.isOnline())
						player.teleToLocation(loc, false);
				}
			}
		}
		
		// Schedule open doors closement and siege start in 2 minutes
		ThreadPoolManager.getInstance().scheduleGeneral(new CloseOutterDoorsTask(FlagWar.super), 300000);
	}
	
	/**
	 * Runnable class to schedule doors closing and siege start.
	 * @author Zoey76
	 */
	protected class CloseOutterDoorsTask implements Runnable
	{
		private Siegable _siegable;
		
		protected CloseOutterDoorsTask(Siegable clanHallSiege)
		{
			_siegable = clanHallSiege;
		}
		
		@Override
		public void run()
		{
			for(int door : OUTTER_DOORS_TO_OPEN)
			{
				_hall.openCloseDoor(door, false);
			}
			
			_hall.getZone().banishNonSiegeParticipants();
			
			_siegable.startSiege();
		}
	}
	
	@Override
	public void onSiegeStarts()
	{
		for(int i = 1; i < _data.size(); i++)
		{
			// Spawns challengers flags and npcs
			try
			{
				ClanData data = _data.values()[i];				
				doSpawns(_data.keys()[i], data);
				fillPlayerList(data);
			}
			catch(Exception e)
			{
				endSiege();
				_log.warning(qn+": Problems in siege initialization!");
				e.printStackTrace();
			}
		}	
	}
	
	@Override
	public void endSiege()
	{
		if(_hall.getOwnerId() > 0)
		{
			L2Clan clan = ClanTable.getInstance().getClan(_hall.getOwnerId());
			clan.setHideoutId(0);
			_hall.free();
		}
		super.endSiege();
	}
	
	@Override
	public void onSiegeEnds()
	{
		if(_data.size() > 0)
		{
			for(int clanId : _data.keys())
			{
				if(_hall.getOwnerId() == clanId)
					removeParticipant(clanId, false);
				else
					removeParticipant(clanId, true);
			}
		}
		clearTables();
	}
	
	@Override
	public final Location getInnerSpawnLoc(final L2PcInstance player)
	{
		Location loc = null;
		if(player.getClanId() == _hall.getOwnerId())
			loc = _hall.getZone().getSpawns().get(0);
		else
		{
			ClanData cd = _data.get(player.getClanId());
			if(cd != null)
			{
				int index = cd.flag - FLAG_RED;
				if(index >= 0 && index <= 4)
					loc = _hall.getZone().getChallengerSpawns().get(index);
				else 
					throw new ArrayIndexOutOfBoundsException();
			}
		}
		return loc;
	}
	
	@Override
	public final boolean canPlantFlag()
	{
		return false;
	}
	
	@Override
	public final boolean doorIsAutoAttackable()
	{
		return false;
	}
	
	void doSpawns(int clanId, ClanData data)
	{
		try
		{
			L2NpcTemplate mahumTemplate = NpcTable.getInstance().getTemplate(data.npc);
			L2NpcTemplate flagTemplate = NpcTable.getInstance().getTemplate(data.flag);
			
			if(flagTemplate == null)
			{
				_log.warning(qn+": Flag L2NpcTemplate["+data.flag+"] does not exist!");
				throw new NullPointerException();
			}
			else if(mahumTemplate == null)
			{
				_log.warning(qn+": Ally L2NpcTemplate["+data.npc+"] does not exist!");
				throw new NullPointerException();
			}
	
			int index = 0;
			if(_firstPhase)
				index = data.flag - FLAG_RED;
			else
				index = clanId == _hall.getOwnerId()? 5 : 6;
			Location loc = FLAG_COORDS[index];		
			
			data.flagInstance = new L2Spawn(flagTemplate);			
			data.flagInstance.setLocation(loc);
			data.flagInstance.setRespawnDelay(10000);
			data.flagInstance.setAmount(1);
			data.flagInstance.init();
			
			data.warrior = new L2Spawn(mahumTemplate);
			data.warrior.setLocation(loc);
			data.warrior.setRespawnDelay(10000);
			data.warrior.setAmount(1);
			data.warrior.init();
			((L2SpecialSiegeGuardAI)data.warrior.getLastSpawn().getAI()).getAlly().addAll(data.players);
		}
		catch(Exception e)
		{
			_log.warning(qn+": Couldnt make clan spawns: "+e.getMessage());
			e.printStackTrace();
		}
	}
	
	private void fillPlayerList(ClanData data)
	{
		for(int objId : data.players)
		{
			L2PcInstance plr = L2World.getInstance().getPlayer(objId);
			if(plr != null)
			{
				data.playersInstance.add(plr);
			}
		}			
	}
	
	private void registerClan(L2Clan clan)
	{
		final int clanId = clan.getClanId();
		
		L2SiegeClan sc = new L2SiegeClan(clanId, SiegeClanType.ATTACKER);
		getAttackers().put(clanId, sc);
		
		ClanData data = new ClanData();
		data.flag = ROYAL_FLAG + _data.size();
		data.players.add(clan.getLeaderId());
		_data.put(clanId, data);
		
		saveClan(clanId, data.flag);
		saveMember(clanId, clan.getLeaderId());
	}
	
	private final void doUnSpawns(ClanData data)
	{
		if(data.flagInstance != null)
		{
			data.flagInstance.stopRespawn();
			data.flagInstance.getLastSpawn().deleteMe();
		}
		if(data.warrior != null)
		{
			data.warrior.stopRespawn();
			data.warrior.getLastSpawn().deleteMe();
		}
	}
	
	private final void removeParticipant(int clanId, boolean teleport)
	{
		ClanData dat = _data.remove(clanId);
		
		if(dat != null)
		{	
			// Destroy clan flag
			if(dat.flagInstance != null)
			{
				dat.flagInstance.stopRespawn();
				if(dat.flagInstance.getLastSpawn() != null)
					dat.flagInstance.getLastSpawn().deleteMe();
			}
		
			if(dat.warrior != null)
			{
				// Destroy clan warrior
				dat.warrior.stopRespawn();
				if(dat.warrior.getLastSpawn() != null)
					dat.warrior.getLastSpawn().deleteMe();
			}
		
			dat.players.clear();
			
			if(teleport)
			{
				// Teleport players outside
				for(L2PcInstance pc : dat.playersInstance)
					if(pc != null)
						pc.teleToLocation(TeleportWhereType.Town);
			}
			
			dat.playersInstance.clear();
		}
	}
	
	public boolean canPayRegistration()
	{
		return true;
	}
	
	private void sendRegistrationPageDate(L2PcInstance player)
	{
		NpcHtmlMessage msg = new NpcHtmlMessage(5);
		msg.setFile(null, "data/scripts/conquerablehalls/flagwar/"+qn+"/siege_date.htm");
		msg.replace("%nextSiege%", _hall.getSiegeDate().getTime().toString());
		player.sendPacket(msg);
	}
	
	public abstract String getFlagHtml(int flag);	
	public abstract String getAllyHtml(int ally);
	
	@Override
	public final void loadAttackers()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(SQL_LOAD_ATTACKERS);
			statement.setInt(1, _hall.getId());
			ResultSet rset = statement.executeQuery();
			while(rset.next())
			{
				final int clanId = rset.getInt("clan_id");
				
				if(ClanTable.getInstance().getClan(clanId) == null)
				{
					_log.warning(qn+": Loaded an unexistent clan as attacker! Clan Id: "+clanId);
					continue;
				}
				
				ClanData data = new ClanData();
				data.flag = rset.getInt("flag");
				data.npc = rset.getInt("npc");

				_data.put(clanId, data);
				loadAttackerMembers(clanId);
			}
			rset.close();
			statement.close();
		}
		catch(Exception e)
		{
			_log.warning(qn+".loadAttackers()->"+e.getMessage());
			e.printStackTrace();
		}
	}
	
	private final void loadAttackerMembers(int clanId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			ArrayList<Integer> listInstance = _data.get(clanId).players;
			
			if(listInstance == null)
			{
				_log.warning(qn+": Tried to load unregistered clan: "+clanId+"[clan Id]");
				return;
			}
			
			PreparedStatement statement = con.prepareStatement(SQL_LOAD_MEMEBERS);
			statement.setInt(1, clanId);
			ResultSet rset = statement.executeQuery();
			while(rset.next())
			{
				listInstance.add(rset.getInt("object_id"));
				
			}
			rset.close();
			statement.close();
		}
		catch(Exception e)
		{
			_log.warning(qn+".loadAttackerMembers()->"+e.getMessage());
			e.printStackTrace();
		}
	}
	
	private final void saveClan(int clanId, int flag)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(SQL_SAVE_CLAN);
			statement.setInt(1, _hall.getId());
			statement.setInt(2, flag);
			statement.setInt(3, 0);
			statement.setInt(4, clanId);
			statement.execute();
			statement.close();
		}
		catch(Exception e)
		{
			_log.warning(qn+".saveClan()->"+e.getMessage());
			e.printStackTrace();
		}
	}
	
	private final void saveNpc(int npc, int clanId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(SQL_SAVE_NPC);
			statement.setInt(1, npc);
			statement.setInt(2, clanId);
			statement.execute();
			statement.close();
		}
		catch(Exception e)
		{
			_log.warning(qn+".saveNpc()->"+e.getMessage());
			e.printStackTrace();
		}
	}
	
	private final void saveMember(int clanId, int objectId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(SQL_SAVE_ATTACKER);
			statement.setInt(1, _hall.getId());
			statement.setInt(2, clanId);
			statement.setInt(3, objectId);
			statement.execute();
			statement.close();
		}
		catch(Exception e)
		{
			_log.warning(qn+".saveMember()->"+e.getMessage());
			e.printStackTrace();
		}
	}
	
	private void clearTables()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement stat1 = con.prepareStatement(SQL_CLEAR_CLAN);
			stat1.setInt(1, _hall.getId());
			stat1.execute();
			stat1.close();
			
			PreparedStatement stat2 = con.prepareStatement(SQL_CLEAR_CLAN_ATTACKERS);
			stat2.setInt(1, _hall.getId());
			stat2.execute();
			stat2.close();
		}
		catch(Exception e)
		{
			_log.warning(qn+".clearTables()->"+e.getMessage());
		}
	}
	
	class ClanData
	{
		int flag = 0;
		int npc = 0;
		ArrayList<Integer> players = new ArrayList<>(18);
		ArrayList<L2PcInstance> playersInstance = new ArrayList<>(18);
		L2Spawn warrior = null;
		L2Spawn flagInstance = null;
	}
}

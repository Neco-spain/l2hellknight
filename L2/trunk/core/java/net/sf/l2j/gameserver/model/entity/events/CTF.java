package net.sf.l2j.gameserver.model.entity.events;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import javolution.text.TextBuilder;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Radar;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.RadarControl;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.Rnd;

public class CTF
{   
    private final static Logger _log = Logger.getLogger(CTF.class.getName());    
	private static int _FlagNPC = 35062, _FLAG_IN_HAND_ITEM_ID = 6718;
    public static String _eventName = new String(),
                         _eventDesc = new String(),
                         _topTeam = new String(),
                         _joiningLocationName = new String();
    public static Vector<String> _teams = new Vector<String>(),
                                 _savePlayers = new Vector<String>(),
                                 _savePlayerTeams = new Vector<String>();
    public static List<L2PcInstance> _players = new FastList<L2PcInstance>(),
                                       _playersShuffle = new Vector<L2PcInstance>();
    public static Vector<Integer> _teamPlayersCount = new Vector<Integer>(),
                                  _teamColors = new Vector<Integer>(),
                                  _teamsX = new Vector<Integer>(),
                                  _teamsY = new Vector<Integer>(),
                                  _teamsZ = new Vector<Integer>();
    public static boolean _joining = false,
                          _teleport = false,
                          _started = false,
                          _sitForced = false;
    public static L2Spawn _npcSpawn;
    public static int _npcId = 0,
                      _npcX = 0,
                      _npcY = 0,
                      _npcZ = 0,
                      _npcHeading = 0,
                      _rewardId = 0,
                      _rewardAmount = 0,
                      _minlvl = 0,
                      _maxlvl = 0,
                      _joinTime = 0,
                      _eventTime = 0,
                      _minPlayers = 0,
                      _maxPlayers = 0;
    public static Vector<Integer> _teamPointsCount = new Vector<Integer>();
    public static Vector<Integer> 	_flagIds = new Vector<Integer>(),
			  						_flagsX = new Vector<Integer>(),
			  						_flagsY = new Vector<Integer>(),
			  						_flagsZ = new Vector<Integer>();
	public static Vector<L2Spawn> _flagSpawns = new Vector<L2Spawn>(),
								  _throneSpawns = new Vector<L2Spawn>();
	public static Vector<Boolean> _flagsTaken = new Vector<Boolean>();
	public static int 	_topScore = 0,
						eventCenterX=0,
						eventCenterY=0,
						eventCenterZ=0,
						eventOffset=0;

	public static void showFlagHtml(L2PcInstance eventPlayer, String objectId, String teamName)
    {
    	if (eventPlayer == null)
    		return;
    	
        try
        {
            NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

            TextBuilder replyMSG = new TextBuilder("<html><head><body><center>");
            replyMSG.append("CTF Flag<br><br>");
            replyMSG.append("<font color=\"00FF00\">" + teamName + "'s Flag</font><br1>");
            if (eventPlayer._teamNameCTF!=null && eventPlayer._teamNameCTF.equals(teamName))
            	replyMSG.append("<font color=\"LEVEL\">This is your Flag</font><br1>");
            else 
            	replyMSG.append("<font color=\"LEVEL\">Enemy Flag!</font><br1>");
            if (_started){
            	processInFlagRange(eventPlayer);            
            }
            else 
            	replyMSG.append("CTF match is not in progress yet.<br>Wait for a GM to start the event<br>");            	
            replyMSG.append("</center></body></html>");
            adminReply.setHtml(replyMSG.toString());
            eventPlayer.sendPacket(adminReply);
        }
        catch (Exception e)
        {
            System.out.println(""+"CTF Engine[showEventHtlm(" + eventPlayer.getName() + ", " + objectId + ")]: exception: " + e.getStackTrace());
        }
    }

	public static void CheckRestoreFlags(){
		Vector<Integer> teamsTakenFlag = new Vector<Integer>();
		try{
		for (L2PcInstance player : _players)
		{
			if (player != null){
				if (player.isOnline() == 0 && player._haveFlagCTF){
					Announcements(_eventName + "(CTF): " + player.getName() + " logged off with a CTF flag!");
					player._haveFlagCTF = false;
					if (_teams.indexOf(player._teamNameHaveFlagCTF)>=0)
						if (_flagsTaken.get(_teams.indexOf(player._teamNameHaveFlagCTF))){
							_flagsTaken.set(_teams.indexOf(player._teamNameHaveFlagCTF), false);
							spawnFlag(player._teamNameHaveFlagCTF);
							Announcements(_eventName + "(CTF): " + player._teamNameHaveFlagCTF + " flag now returned to place.");
						}
					removeFlagFromPlayer(player);
					player._teamNameHaveFlagCTF = null;
    				return;
				}
				else if (player._haveFlagCTF)    			
					teamsTakenFlag.add(_teams.indexOf(player._teamNameHaveFlagCTF));
			}
		}
		for (String team : _teams)
		{
			if (team == null) continue;
			int index = _teams.indexOf(team);
			if (!teamsTakenFlag.contains(index)){
				if (_flagsTaken.get(index)){
					_flagsTaken.set(index, false);
					spawnFlag(team);
					Announcements(_eventName + "(CTF): " + team + " flag returned due to player error.");
				}
			}
		}
		for (L2PcInstance player : _players)
			if (player!=null && player._haveFlagCTF){
				if (isOutsideCTFArea(player)){
					Announcements(_eventName + "(CTF): " + player.getName() + " escaped from the event holding a flag!");
					player._haveFlagCTF = false;
					if (_teams.indexOf(player._teamNameHaveFlagCTF)>=0)
						if (_flagsTaken.get(_teams.indexOf(player._teamNameHaveFlagCTF))){
							_flagsTaken.set(_teams.indexOf(player._teamNameHaveFlagCTF), false);
							spawnFlag(player._teamNameHaveFlagCTF);
							Announcements(_eventName + "(CTF): " + player._teamNameHaveFlagCTF + " flag now returned to place.");
						}
					removeFlagFromPlayer(player);
					player._teamNameHaveFlagCTF = null;
					player.teleToLocation(_teamsX.get(_teams.indexOf(player._teamNameCTF)), _teamsY.get(_teams.indexOf(player._teamNameCTF)), _teamsZ.get(_teams.indexOf(player._teamNameCTF)));
					player.sendMessage("You have been returned to your team spawn");
    				return;
				}
			}
		}catch(Exception e){
			_log.info("CTF.restoreFlags() Error:"+e.toString());
			return;}
	}
	
	public static void Announcements(String announce){
		CreatureSay cs = new CreatureSay(0, 18, "", "Announcements: "+announce);
			if (!_started && !_teleport)
				for (L2PcInstance player: L2World.getInstance().getAllPlayers()){
					if (player != null)
						if (player.isOnline()!=0)
							player.sendPacket(cs);
				}
			else{
				if (_players!=null && !_players.isEmpty())
					for (L2PcInstance player: _players){
						if (player != null)
							if (player.isOnline()!=0)
								player.sendPacket(cs);
					}
			}
	}
		
	public static void Started(L2PcInstance player){
		player._teamNameHaveFlagCTF=null;
		player._haveFlagCTF = false;
	}
	
	public static void StartEvent(){
		for (L2PcInstance player : _players)
    		if (player != null){
    			player._teamNameHaveFlagCTF=null;
    			player._haveFlagCTF = false;
    		}
    	Announcements(_eventName + "(CTF): Started. Go Capture the Flags!");
	}
	
	public static void addFlagToPlayer(L2PcInstance _player){
		L2ItemInstance wpn = _player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
        if (wpn == null){ 
        	wpn = _player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LRHAND);
        	if (wpn!=null)
        		_player.getInventory().unEquipItemInBodySlotAndRecord(Inventory.PAPERDOLL_LRHAND);
        }
        else{
        	_player.getInventory().unEquipItemInBodySlotAndRecord(Inventory.PAPERDOLL_RHAND);
        	wpn = _player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
        	if (wpn!=null)
        		_player.getInventory().unEquipItemInBodySlotAndRecord(Inventory.PAPERDOLL_LHAND);
        }
		_player.getInventory().equipItem(ItemTable.getInstance().createItem("",CTF._FLAG_IN_HAND_ITEM_ID,1,_player,null));
		_player.broadcastPacket(new SocialAction(_player.getObjectId(), 16));
		_player._haveFlagCTF = true;
		_player.broadcastUserInfo();
    	CreatureSay cs = new CreatureSay(_player.getObjectId(), 15, ":", "You got it! Run back! ::");
    	_player.sendPacket(cs);
	}
	
	public static void removeFlagFromPlayer(L2PcInstance player)
	{
		L2ItemInstance wpn = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LRHAND);
		player._haveFlagCTF = false;
		if (wpn != null)
		{
			L2ItemInstance[] unequiped = player.getInventory().unEquipItemInBodySlotAndRecord(wpn.getItem().getBodyPart());
			player.getInventory().destroyItemByItemId("", CTF._FLAG_IN_HAND_ITEM_ID, 1, player, null);
			InventoryUpdate iu = new InventoryUpdate();
            for (L2ItemInstance element : unequiped)
				iu.addModifiedItem(element);
			player.sendPacket(iu);
			player.sendPacket(new ItemList(player, true));
            player.abortAttack();
            player.broadcastUserInfo();
		}
		else{
			player.getInventory().destroyItemByItemId("", CTF._FLAG_IN_HAND_ITEM_ID, 1, player, null);
			player.sendPacket(new ItemList(player, true));
            player.abortAttack();
            player.broadcastUserInfo();
		}
			
	}
	
	
	
	public static void setTeamFlag(String teamName, L2PcInstance activeChar)
    {
        int index = _teams.indexOf(teamName);
        
        if (index == -1)
            return;
        addOrSet(_teams.indexOf(teamName),null,false,_FlagNPC,activeChar.getX(),activeChar.getY(),activeChar.getZ());        
    }
    
    
    public static void spawnAllFlags()
    {
    	while (_flagSpawns.size()<_teams.size())
    		_flagSpawns.add(null);
    	while (_throneSpawns.size()<_teams.size())
    		_throneSpawns.add(null);
    	for (String team : _teams)
    	{
    		int index = _teams.indexOf(team);
    		L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(_flagIds.get(index));
    		L2NpcTemplate throne = NpcTable.getInstance().getTemplate(32027);
    		try
    		{
    			//spawn throne
    			_throneSpawns.set(index, new L2Spawn(throne));
    			_throneSpawns.get(index).setLocx(_flagsX.get(index));
    			_throneSpawns.get(index).setLocy(_flagsY.get(index));
    			_throneSpawns.get(index).setLocz(_flagsZ.get(index)-10);
    			_throneSpawns.get(index).setAmount(1);
    			_throneSpawns.get(index).setHeading(0);
    			_throneSpawns.get(index).setRespawnDelay(1);
    			SpawnTable.getInstance().addNewSpawn(_throneSpawns.get(index), false);
    			_throneSpawns.get(index).init();
    			_throneSpawns.get(index).getLastSpawn().getStatus().setCurrentHp(999999999);
    			_throneSpawns.get(index).getLastSpawn().decayMe();
    			_throneSpawns.get(index).getLastSpawn().spawnMe(_throneSpawns.get(index).getLastSpawn().getX(), _throneSpawns.get(index).getLastSpawn().getY(), _throneSpawns.get(index).getLastSpawn().getZ());
    			_throneSpawns.get(index).getLastSpawn().setTitle(team+" Throne");
    			_throneSpawns.get(index).getLastSpawn().broadcastPacket(new MagicSkillUser(_throneSpawns.get(index).getLastSpawn(), _throneSpawns.get(index).getLastSpawn(), 1036, 1, 5500, 1));
    			_throneSpawns.get(index).getLastSpawn()._isCTF_throneSpawn=true;
    			//spawn flag
    			_flagSpawns.set(index, new L2Spawn(tmpl));
    			_flagSpawns.get(index).setLocx(_flagsX.get(index));
    			_flagSpawns.get(index).setLocy(_flagsY.get(index));
    			_flagSpawns.get(index).setLocz(_flagsZ.get(index));
    			_flagSpawns.get(index).setAmount(1);
    			_flagSpawns.get(index).setHeading(0);
    			_flagSpawns.get(index).setRespawnDelay(1);
    			SpawnTable.getInstance().addNewSpawn(_flagSpawns.get(index), false);
    			_flagSpawns.get(index).init();
    			_flagSpawns.get(index).getLastSpawn().getStatus().setCurrentHp(999999999);
    			_flagSpawns.get(index).getLastSpawn().setTitle(team+"'s Flag");
    			_flagSpawns.get(index).getLastSpawn()._CTF_FlagTeamName = team;
    			_flagSpawns.get(index).getLastSpawn().decayMe();
    			_flagSpawns.get(index).getLastSpawn().spawnMe(_flagSpawns.get(index).getLastSpawn().getX(), _flagSpawns.get(index).getLastSpawn().getY(), _flagSpawns.get(index).getLastSpawn().getZ());
    			_flagSpawns.get(index).getLastSpawn()._isCTF_Flag = true;
    			calculateOutSideOfCTF(); // sets event boundaries so players don't run with the flag.
    		}
    		catch(Exception e)
    		{
				System.out.println("CTF Engine[spawnAllFlags()]: exception: " + e.getMessage());
			}
    	}
    }
  
    public static void processTopTeam()
	{
		for (L2PcInstance disconnectPlayer : _players)
		{
			if (disconnectPlayer.isOnline() == 0)
			{
				_players.remove(disconnectPlayer);
				_log.info("[CTF DEBUG] processTopTeam() remove disconnect player " + disconnectPlayer.getName());
			}
		}
    	_topTeam = null;
    	for (String team : _teams){
            if (teamPointsCount(team) == _topScore && _topScore > 0)
            	_topTeam = null;
    		if (teamPointsCount(team) > _topScore){
       			_topTeam = team;
       			_topScore = teamPointsCount(team);
       		}
    	}
    	if (_topScore <= 0){
    		Announcements(_eventName + "("+"CTF): No flags taken).");
    	}
    	else{
    		if (_topTeam == null) 
    			Announcements(_eventName + "(CTF): Maximum flags taken : " + _topScore + " flags! No one won.");
    		else{
    			Announcements(_eventName + "(CTF): Team " + _topTeam + " wins the match, with " + _topScore + " flags taken!");
   				rewardTeam(_topTeam);
    		}
    	}
    	teleportFinish();
    }
  
    
    public static void unspawnAllFlags()
    {
    	try{
    	if (_throneSpawns == null || _flagSpawns == null || _teams == null)
    		return;
    	for (String team : _teams)
    	{
    		int index = _teams.indexOf(team);
    		if (_throneSpawns.get(index) != null){
    			_throneSpawns.get(index).getLastSpawn().deleteMe();
    			_throneSpawns.get(index).stopRespawn();
    			SpawnTable.getInstance().deleteSpawn(_throneSpawns.get(index), true);
    		}
    		if (_flagSpawns.get(index) != null){
    			_flagSpawns.get(index).getLastSpawn().deleteMe();
    			_flagSpawns.get(index).stopRespawn();
    			SpawnTable.getInstance().deleteSpawn(_flagSpawns.get(index), true);
    		}
    	}
		_throneSpawns.removeAllElements();
    	}catch(Throwable t){
    		return;
    	}
    }
  
    
    private static void unspawnFlag(String teamName)
    {
    	int index = _teams.indexOf(teamName);

		_flagSpawns.get(index).getLastSpawn().deleteMe();
		_flagSpawns.get(index).stopRespawn();
		SpawnTable.getInstance().deleteSpawn(_flagSpawns.get(index), true);
    }
    
    public static void spawnFlag(String teamName)
    {
    	int index = _teams.indexOf(teamName);
		L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(_flagIds.get(index));

		try
		{
			_flagSpawns.set(index, new L2Spawn(tmpl));

			_flagSpawns.get(index).setLocx(_flagsX.get(index));
			_flagSpawns.get(index).setLocy(_flagsY.get(index));
			_flagSpawns.get(index).setLocz(_flagsZ.get(index));
			_flagSpawns.get(index).setAmount(1);
			_flagSpawns.get(index).setHeading(0);
			_flagSpawns.get(index).setRespawnDelay(1);

			SpawnTable.getInstance().addNewSpawn(_flagSpawns.get(index), false);

			_flagSpawns.get(index).init();
			_flagSpawns.get(index).getLastSpawn().getStatus().setCurrentHp(999999999);
			_flagSpawns.get(index).getLastSpawn().setTitle(teamName+"'s Flag");
			_flagSpawns.get(index).getLastSpawn()._CTF_FlagTeamName = teamName;
			_flagSpawns.get(index).getLastSpawn()._isCTF_Flag = true;    			
			_flagSpawns.get(index).getLastSpawn().decayMe();
			_flagSpawns.get(index).getLastSpawn().spawnMe(_flagSpawns.get(index).getLastSpawn().getX(), _flagSpawns.get(index).getLastSpawn().getY(), _flagSpawns.get(index).getLastSpawn().getZ());
		}
		catch(Exception e)
		{
			System.out.println("CTF Engine[spawnFlag(" + teamName + ")]: exception: " + e.getStackTrace());
		}    	
    }
  
   
    public static boolean InRangeOfFlag(L2PcInstance _player, int flagIndex, int offset){
    	if (_player.getX() > CTF._flagsX.get(flagIndex)-offset && _player.getX() < CTF._flagsX.get(flagIndex)+offset &&
    		_player.getY() > CTF._flagsY.get(flagIndex)-offset && _player.getY() < CTF._flagsY.get(flagIndex)+offset &&
    		_player.getZ() > CTF._flagsZ.get(flagIndex)-offset && _player.getZ() < CTF._flagsZ.get(flagIndex)+offset)
    		return true;
    	return false;
    }
    	
    public static void processInFlagRange(L2PcInstance _player){	
		try{
			CheckRestoreFlags();
			for (String team : _teams)
    		{
    			if (team.equals(_player._teamNameCTF))
    			{
    				int indexOwn = _teams.indexOf(_player._teamNameCTF);
					
    				if (InRangeOfFlag(_player,indexOwn,100) && !_flagsTaken.get(indexOwn) && _player._haveFlagCTF){
    					int indexEnemy = _teams.indexOf(_player._teamNameHaveFlagCTF);
    					_flagsTaken.set(indexEnemy, false);
   						spawnFlag(_player._teamNameHaveFlagCTF);
   						_player.broadcastPacket(new SocialAction(_player.getObjectId(), 16));
   						_player.broadcastUserInfo();
   						_player.broadcastPacket(new SocialAction(_player.getObjectId(), 3));
   						_player.broadcastUserInfo();
   						removeFlagFromPlayer(_player);
   						_teamPointsCount.set(indexOwn, teamPointsCount(team)+1);
   						Announcements(_eventName + "(CTF): " + _player.getName() + " scores for " + _player._teamNameCTF + ".");
   					}
    			}
    			else
    			{
    				int indexEnemy = _teams.indexOf(team);
    				if (InRangeOfFlag(_player, indexEnemy,100) && !_flagsTaken.get(indexEnemy) && !_player._haveFlagCTF && !_player.isDead())
    				{
    					_flagsTaken.set(indexEnemy, true);
    					unspawnFlag(team);
    					_player._teamNameHaveFlagCTF = team;
    					addFlagToPlayer(_player);
    					_player.broadcastUserInfo();
    					_player._haveFlagCTF = true;
    					Announcements(_eventName + "(CTF): " + team + " flag taken by " + _player.getName()+"...");
    					pointTeamTo(_player,team);
    					break;
    				}
    			}
			}}catch(Exception e){return;}
    	}
    
    public static void pointTeamTo(L2PcInstance hasFlag, String ourFlag){
    	try{
    	for (L2PcInstance player : _players){
    		if (player!=null && player.isOnline()!=0){
    			if (player._teamNameCTF.equals(ourFlag)){
    				player.sendMessage(hasFlag.getName()+" took your flag!");
    				if (player._haveFlagCTF){
    					player.sendMessage("You can not return the flag to headquarters, until your flag is returned to it's place.");
    					player.sendPacket(new RadarControl(1, 1, player.getX(), player.getY(), player.getZ()));
    				}
    				else{
    					player.sendPacket(new RadarControl(0, 1, hasFlag.getX(), hasFlag.getY(), hasFlag.getZ()));
    					L2Radar rdr = new L2Radar(player);
    					L2Radar.RadarOnPlayer radar = rdr.new RadarOnPlayer(hasFlag,player);
    					ThreadPoolManager.getInstance().scheduleGeneral(radar, 10000+Rnd.get(30000));
    				}
    			}
    		}
    	}
    	}catch(Throwable t){}
    }
    
    public static int teamPointsCount(String teamName)
    {
        int index = _teams.indexOf(teamName);
        
        if (index == -1)
            return -1;

        return _teamPointsCount.get(index);
    }
    
    public static void setTeamPointsCount(String teamName, int teamPointCount)
    {
        int index = _teams.indexOf(teamName);
        
        if (index == -1)
            return;

        _teamPointsCount.set(index, teamPointCount);
    }
    
    public static int teamPlayersCount(String teamName)
    {
        int index = _teams.indexOf(teamName);
        
        if (index == -1)
            return -1;

        return _teamPlayersCount.get(index);
    }
    
    public static void setTeamPlayersCount(String teamName, int teamPlayersCount)
    {
        int index = _teams.indexOf(teamName);
        
        if (index == -1)
            return;
        
        _teamPlayersCount.set(index, teamPlayersCount);
    }
    
    public static void setNpcPos(L2PcInstance activeChar)
    {
        _npcX = activeChar.getX();
        _npcY = activeChar.getY();
        _npcZ = activeChar.getZ();
        _npcHeading = activeChar.getHeading();
    }

    public static void setNpcPos(int x,int y,int z)
    {
        _npcX = x;
        _npcY = y;
        _npcZ = z;
    }
    
    public static void addTeam(String teamName)
    {
        if (!checkTeamOk())
        {
            return;
        }
        
        if (teamName.equals(" "))
            return;

        _teams.add(teamName);
        _teamPlayersCount.add(0);
        _teamColors.add(0);
        _teamsX.add(0);
        _teamsY.add(0);
        _teamsZ.add(0);
    	_teamPointsCount.add(0);
    	addOrSet(_teams.indexOf(teamName),null,false,_FlagNPC,0,0,0);
    }
    
    private static void addOrSet(int listSize, L2Spawn flagSpawn, boolean flagsTaken, int flagId,int flagX, int flagY, int flagZ){
    	while (_flagsX.size() <= listSize){
        	_flagSpawns.add(null);
        	_flagsTaken.add(false);
        	_flagIds.add(_FlagNPC);
        	_flagsX.add(0);
        	_flagsY.add(0);
        	_flagsZ.add(0);    		
    	}
    	_flagSpawns.set(listSize,flagSpawn);
    	_flagsTaken.set(listSize,flagsTaken);
    	_flagIds.set(listSize,flagId);
    	_flagsX.set(listSize,flagX);
    	_flagsY.set(listSize,flagY);
    	_flagsZ.set(listSize,flagZ);
    }
    
    public static boolean checkMaxLevel(int maxlvl)
    {
        if (_minlvl >= maxlvl)
            return false;
        
        return true;
    }
    
    public static boolean checkMinLevel(int minlvl)
    {
        if (_maxlvl <= minlvl)
            return false;
        
        return true;
    }
    
    public static boolean checkMinPlayers(int players)
    {
        if (_minPlayers <= players)
            return true;
        
        return false;
    }
    public static boolean checkMaxPlayers(int players)
    {
        if (_maxPlayers > players)
            return true;
        
        return false;
    }
    
    public static void removeTeam(String teamName)
    {
        if (!checkTeamOk() || _teams.isEmpty())
        {
            return;
        }
        
        if (teamPlayersCount(teamName) > 0)
        {
            return;
        }
        
        int index = _teams.indexOf(teamName);
        
        if (index == -1)
            return;

        _teamsZ.remove(index);
        _teamsY.remove(index);
        _teamsX.remove(index);
        _teamColors.remove(index);
        _teamPointsCount.remove(index);
        _teamPlayersCount.remove(index);
        _teams.remove(index);
    	_flagSpawns.remove(index);
    	_flagsTaken.remove(index);
    	_flagIds.remove(index);
    	_flagsX.remove(index);
    	_flagsY.remove(index);
    	_flagsZ.remove(index);
    }
    
    public static void setTeamPos(String teamName, L2PcInstance activeChar)
    {
        int index = _teams.indexOf(teamName);
        
        if (index == -1)
            return;
        
        _teamsX.set(index, activeChar.getX());
        _teamsY.set(index, activeChar.getY());
        _teamsZ.set(index, activeChar.getZ());
    }

    public static void setTeamPos(String teamName, int x,int y,int z)
    {
        int index = _teams.indexOf(teamName);
        
        if (index == -1)
            return;
        
        _teamsX.set(index, x);
        _teamsY.set(index, y);
        _teamsZ.set(index, z);
    }
    
    public static void setTeamColor(String teamName, int color)
    {
        if (!checkTeamOk())
            return;

        int index = _teams.indexOf(teamName);
        
        if (index == -1)
            return;

        _teamColors.set(index, color);
    }
    
    public static boolean checkTeamOk()
    {
        if (_started || _teleport || _joining)
            return false;
        
        return true;
    }
    
    public static void startJoin(L2PcInstance activeChar)
    {
        if (!startJoinOk())
        {
            activeChar.sendMessage("Event not setted propertly.");
                return;
        }
        
        _joining = true;
        spawnEventNpc(activeChar);
        Announcements(_eventName + "(CTF): Joinable in " + _joiningLocationName + "!");
    }

    public static void startJoin()
    {
        if (!startJoinOk())
        {
            _log.warning("Event not setted propertly.");
                return;
        }
        
        _joining = true;
        spawnEventNpc();
        Announcements(_eventName + "(CTF): Joinable in " + _joiningLocationName + "!");
    }
    
    public static boolean startAutoJoin()
    {
        if (!startJoinOk())
        {
                return false;
        }
        
        _joining = true;
        spawnEventNpc();
        Announcements(_eventName + "(CTF): Joinable in " + _joiningLocationName + "!");
        return true;
    }
    
    public static boolean startJoinOk()
    {
        if (_started || _teleport || _joining || _teams.size() < 2 || _eventName.equals("") ||
            _joiningLocationName.equals("") || _eventDesc.equals("") || _npcId == 0 ||
            _npcX == 0 || _npcY == 0 || _npcZ == 0 || _rewardId == 0 || _rewardAmount == 0 ||
            _teamsX.contains(0) || _teamsY.contains(0) || _teamsZ.contains(0))
            return false;
        try {
        	if (_flagsX.contains(0) || _flagsY.contains(0) || _flagsZ.contains(0) || _flagIds.contains(0))
        		return false;
        	if (_flagsX.size() < _teams.size() ||
        		_flagsY.size() < _teams.size() ||
        		_flagsZ.size() < _teams.size() ||
        		_flagIds.size() < _teams.size())
        		return false;
        } 
        catch (ArrayIndexOutOfBoundsException e){
        	return false;
        }        
        return true;
    }
    
    private static void spawnEventNpc(L2PcInstance activeChar)
    {
        L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(_npcId);

        try
        {
            _npcSpawn = new L2Spawn(tmpl);

            _npcSpawn.setLocx(_npcX);
            _npcSpawn.setLocy(_npcY);
            _npcSpawn.setLocz(_npcZ);
            _npcSpawn.setAmount(1);
            _npcSpawn.setHeading(_npcHeading);
            _npcSpawn.setRespawnDelay(1);

            SpawnTable.getInstance().addNewSpawn(_npcSpawn, false);

            _npcSpawn.init();
            _npcSpawn.getLastSpawn().getStatus().setCurrentHp(999999999);
            _npcSpawn.getLastSpawn().setTitle(_eventName);
            _npcSpawn.getLastSpawn()._isEventMobCTF = true;
            _npcSpawn.getLastSpawn().isAggressive();
            _npcSpawn.getLastSpawn().decayMe();
            _npcSpawn.getLastSpawn().spawnMe(_npcSpawn.getLastSpawn().getX(), _npcSpawn.getLastSpawn().getY(), _npcSpawn.getLastSpawn().getZ());

            _npcSpawn.getLastSpawn().broadcastPacket(new MagicSkillUser(_npcSpawn.getLastSpawn(), _npcSpawn.getLastSpawn(), 1034, 1, 1, 1));
        }
        catch (Exception e)
        {
            _log.severe("CTF Engine[spawnEventNpc(" + activeChar.getName() + ")]: exception: " + e.getMessage());
        }
    }

    private static void spawnEventNpc()
    {
        L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(_npcId);

        try
        {
            _npcSpawn = new L2Spawn(tmpl);

            _npcSpawn.setLocx(_npcX);
            _npcSpawn.setLocy(_npcY);
            _npcSpawn.setLocz(_npcZ);
            _npcSpawn.setAmount(1);
            _npcSpawn.setHeading(_npcHeading);
            _npcSpawn.setRespawnDelay(1);

            SpawnTable.getInstance().addNewSpawn(_npcSpawn, false);

            _npcSpawn.init();
            _npcSpawn.getLastSpawn().getStatus().setCurrentHp(999999999);
            _npcSpawn.getLastSpawn().setTitle(_eventName);
            _npcSpawn.getLastSpawn()._isEventMobCTF = true;
            _npcSpawn.getLastSpawn().isAggressive();
            _npcSpawn.getLastSpawn().decayMe();
            _npcSpawn.getLastSpawn().spawnMe(_npcSpawn.getLastSpawn().getX(), _npcSpawn.getLastSpawn().getY(), _npcSpawn.getLastSpawn().getZ());

            _npcSpawn.getLastSpawn().broadcastPacket(new MagicSkillUser(_npcSpawn.getLastSpawn(), _npcSpawn.getLastSpawn(), 1034, 1, 1, 1));
        }
        catch (Exception e)
        {
            _log.severe("CTF Engine[spawnEventNpc(exception: " + e.getMessage());
        }
    }
    
    public static void teleportStart()
    {
        if (!_joining || _started || _teleport)
            return;
        
        if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE") && checkMinPlayers(_playersShuffle.size()))
        {
        	removeOfflinePlayers();
        	shuffleTeams();
        }
        else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE") && !checkMinPlayers(_playersShuffle.size()))
        {
            Announcements("Not enough players for event. Min Requested : " + _minPlayers +", Participating : " + _playersShuffle.size());
            return;
        }
        
        _joining = false;
        Announcements(_eventName + "(CTF): Teleport to team spot in 20 seconds!");

        setUserData();
        ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
        {
            public void run()
            {
                CTF.sit();
                CTF.spawnAllFlags();
                for (L2PcInstance player : _players)
                {
                    if (player !=  null)
                    {
                        if (Config.CTF_ON_START_UNSUMMON_PET)
                        {
                            if (player.getPet() != null)
                            {
                                L2Summon summon = player.getPet();
                                for (L2Effect e : summon.getAllEffects())
                                    if (e != null) e.exit();

                                if (summon instanceof L2PetInstance)
                                    summon.unSummon(player);
                            }
                        }

                        if (Config.CTF_ON_START_REMOVE_ALL_EFFECTS)
                        {
                            for (L2Effect e : player.getAllEffects())
                            {
                                if (e != null) e.exit();
                            }
                        }

                        if (player.getParty() != null)
                        {
                            L2Party party = player.getParty();
                            party.removePartyMember(player);
                        }
						player.teleToLocation(_teamsX.get(_teams.indexOf(player._teamNameCTF)) + Rnd.get(Config.CTF_RND_SPAWNXMIN, Config.CTF_RND_SPAWNXMAX), _teamsY.get(_teams.indexOf(player._teamNameCTF)) + Rnd.get(Config.CTF_RND_SPAWNYMIN, Config.CTF_RND_SPAWNYMAX), _teamsZ.get(_teams.indexOf(player._teamNameCTF)) + Config.CTF_SPAWN_Z);
                    }
                }
            }
        }, 20000);
        _teleport = true;
    }

    public static boolean teleportAutoStart()
    {
        if (!_joining || _started || _teleport)
            return false;
        
        if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE") && checkMinPlayers(_playersShuffle.size()))
        {
        	removeOfflinePlayers();
			shuffleTeams();
        }
        else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE") && !checkMinPlayers(_playersShuffle.size()))
        {
        	Announcements("Not enough players for event. Min Requested : " + _minPlayers +", Participating : " + _playersShuffle.size());
            return false;
        }
        
        _joining = false;
        Announcements(_eventName + "(CTF): Teleport to team spot in 20 seconds!");

        setUserData();
        ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
        {
            public void run()
            {
                sit();
                spawnAllFlags();

                for (L2PcInstance player : _players)
                {
                    if (player !=  null)
                    {
                        if (Config.CTF_ON_START_UNSUMMON_PET)
                        {
                            if (player.getPet() != null)
                            {
                                L2Summon summon = player.getPet();
                                for (L2Effect e : summon.getAllEffects())
                                    if (e != null) e.exit();

                                if (summon instanceof L2PetInstance)
                                    summon.unSummon(player);
                            }
                        }

                        if (Config.CTF_ON_START_REMOVE_ALL_EFFECTS)
                        {
                            for (L2Effect e : player.getAllEffects())
                            {
                                if (e != null) e.exit();
                            }
                        }

                        if (player.getParty() != null)
                        {
                            L2Party party = player.getParty();
                            party.removePartyMember(player);
                        }
						player.teleToLocation(_teamsX.get(_teams.indexOf(player._teamNameCTF)) + Rnd.get(Config.CTF_RND_SPAWNXMIN, Config.CTF_RND_SPAWNXMAX), _teamsY.get(_teams.indexOf(player._teamNameCTF)) + Rnd.get(Config.CTF_RND_SPAWNYMIN, Config.CTF_RND_SPAWNYMAX), _teamsZ.get(_teams.indexOf(player._teamNameCTF)));
                    }
                }
            }
        }, 20000);
        _teleport = true;
        return true;
    }
    
    public static void startEvent(L2PcInstance activeChar)
    {
        if (!startEventOk())
        {
            return;
        }
        
        _teleport = false;
        sit();
        _started = true;
        StartEvent();
    }
    
    public static void setJoinTime(int time)
    {
        _joinTime = time;
    }

    public static void setEventTime(int time)
    {
        _eventTime = time;
    }
    
    public static boolean startAutoEvent()
    {
        if (!startEventOk())
        {
            return false;
        }
        
        _teleport = false;
        sit();
        Announcements(_eventName + "(CTF): Started. Go Capture the Flags!");
        _started = true;
        return true;
    }
    
    public static void autoEvent()
    {
    	if(startAutoJoin())
    	{
    		if(_joinTime > 0) waiter(_joinTime * 60 * 1000);
    		else if(_joinTime <= 0)
    		{
    			abortEvent();
    			return;
    		}
    		if(teleportAutoStart())
    		{
    			waiter(1 * 60 * 1000);
    			if(startAutoEvent())
    			{
    				waiter(_eventTime * 60 * 1000);
    				finishEvent();
    			}
    		}
    		else if (!teleportAutoStart())
    		{
    			abortEvent();
    		}
    	}
    }
    
    private static void waiter(long interval)
	{
		long startWaiterTime = System.currentTimeMillis();
		int seconds = (int)(interval / 1000);

		while (startWaiterTime + interval > System.currentTimeMillis())
		{
			seconds--;
			
			if (_joining || _started || _teleport)
			{
				switch (seconds)
				{
					case 3600: // 1 hour left
						if (_joining)
						{
							Announcements(_eventName + "(CTF): Joinable in " + _joiningLocationName + "!");
							Announcements("CTF Event: " + seconds / 60 / 60 + " hour(s) till registration close!");
						}
						else if (_started)
							Announcements("CTF Event: " + seconds / 60 / 60 + " hour(s) till event finish!");

						break;
					case 1800: // 30 minutes left
					case 900: // 15 minutes left
					case 600: //  10 minutes left 
					case 300: // 5 minutes left
					case 240: // 4 minutes left
					case 180: // 3 minutes left
					case 120: // 2 minutes left
					case 60: // 1 minute left
						if (_joining)
						{
							removeOfflinePlayers();
							Announcements(_eventName + "(CTF): Joinable in " + _joiningLocationName + "!");
							Announcements("CTF Event: " + seconds / 60 + " minute(s) till registration close!");
						}
						else if (_started)
							Announcements("CTF Event: " + seconds / 60 + " minute(s) till event finish!");
						
						break;
					case 30: // 30 seconds left
					case 15: // 15 seconds left
					case 10: // 10 seconds left
					case 5: // 5 seconds left
					case 4: // 4 seconds left
					case 3: // 3 seconds left
					case 2: // 2 seconds left
					case 1: // 1 seconds left
						if (_joining)
							Announcements("CTF Event: " + seconds + " second(s) till registration close!");
						else if (_teleport)
							Announcements("CTF Event: " + seconds + " seconds(s) till start fight!");
						else if (_started)
							Announcements("CTF Event: " + seconds + " second(s) till event finish!");
						
						break;
				}
			}

			long startOneSecondWaiterStartTime = System.currentTimeMillis();

			while (startOneSecondWaiterStartTime + 1000 > System.currentTimeMillis())
			{
				try
				{
					Thread.sleep(1);
				}
				catch (InterruptedException ie)
				{}
			}
		}
	}

    private static boolean startEventOk()
    {
        if (_joining || !_teleport || _started)
            return false;
        
        if (Config.CTF_EVEN_TEAMS.equals("NO") || Config.CTF_EVEN_TEAMS.equals("BALANCE"))
        {
            if (_teamPlayersCount.contains(0))
                return false;
        }
        else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE"))
        {
            Vector<L2PcInstance> playersShuffleTemp = new Vector<L2PcInstance>();
            int loopCount = 0;
            
            loopCount = _playersShuffle.size();

            for (int i=0;i<loopCount;i++)
            {
                if (_playersShuffle != null)
                    playersShuffleTemp.add(_playersShuffle.get(i));
            }
            
            _playersShuffle = playersShuffleTemp; 
            playersShuffleTemp.clear();
            
          //  if (_playersShuffle.size() < (_teams.size()*2)){
          //      return false;
          //  }
          }
        
        return true;
    }
    
    public static void shuffleTeams()
    {
        int teamCount = 0,
            playersCount = 0;

        for (;;)
        {
            if (_playersShuffle.isEmpty())
                break;

            int playerToAddIndex = Rnd.nextInt(_playersShuffle.size());
            L2PcInstance player=null;
            player = _playersShuffle.get(playerToAddIndex);
            player._originalKarmaCTF = player.getKarma();
			
            _players.add(player);
            _players.get(playersCount)._teamNameCTF = _teams.get(teamCount);
            _savePlayers.add(_players.get(playersCount).getName());
            _savePlayerTeams.add(_teams.get(teamCount));
            playersCount++;

            if (teamCount == _teams.size()-1)
                teamCount = 0;
            else
                teamCount++;
            
            _playersShuffle.remove(playerToAddIndex);
        }
    }
    
    public static void setUserData()
    {
        for (L2PcInstance player : _players)
        {
            player.setTeam(_teams.indexOf(player._teamNameCTF) + 1);
            player.setKarma(0);
            player.broadcastUserInfo();
        }
    }
    
    public static void finishEvent()
    {
        if (!finishEventOk())
        {
            return;
        }

        _started = false;
        unspawnEventNpc();
        unspawnAllFlags();
        processTopTeam();

        if (_topScore != 0)
            playKneelAnimation(_topTeam);
        
		Announcements(_eventName + " Team Statistics:");
		for (String team : _teams)
		{
			int _flags_ = teamFlagCount(team);
			Announcements("Team: " + team + " - Flags taken: " + _flags_);
		}

        teleportFinish();
    }
    public static void playKneelAnimation(String teamName)
    {
    	for (L2PcInstance player : _players)
        {
            if (player != null)
            {
                if (!player._teamNameCTF.equals(teamName))
                {
                	player.broadcastPacket(new SocialAction(player.getObjectId(), 7));
                }
                else if (player._teamNameCTF.equals(teamName))
                {
                	player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
                }
            }
        }
    }
    
    private static boolean finishEventOk()
    {
        if (!_started)
            return false;
        
        return true;
    }
    
    public static void rewardTeam(String teamName)
    {
        for (L2PcInstance player : _players)
        {
            if (player != null)
            {
                if (player._teamNameCTF.equals(teamName))
                {
                    player.addItem("CTF Event: " + _eventName, _rewardId, _rewardAmount, null, true);

                    NpcHtmlMessage nhm = new NpcHtmlMessage(5);
                    TextBuilder replyMSG = new TextBuilder("");

                    replyMSG.append("<html><body>Your team wins the event. Look in your inventory for the reward.</body></html>");

                    nhm.setHtml(replyMSG.toString());
                    player.sendPacket(nhm);
                    player.sendPacket( new ActionFailed() );
                }
            }
        }
    }
    
    public static void abortEvent()
    {
        if (!_joining && !_teleport && !_started)
            return;
        if (_joining && !_teleport && !_started){
        	unspawnEventNpc();
        	cleanCTF();
        	_joining = false;
        	Announcements(_eventName + "(CTF): Match aborted!");
        	return;
        }
        _joining = false;
        _teleport = false;
        _started = false;
        unspawnEventNpc();
        unspawnAllFlags();
        Announcements(_eventName + "(CTF): Match aborted!");
        teleportFinish();
    }
    
    public static void sit()
    {
        if (_sitForced)
            _sitForced = false;
        else
            _sitForced = true;
        
        for (L2PcInstance player : _players)
        {
            if (player != null)
            {
                if (_sitForced)
                {
                    player.stopMove(null, false);
                    player.abortAttack();
                    player.abortCast();
                    
                    if (!player.isSitting())
                        player.sitDown();
                }
                else
                {
                    if (player.isSitting())
                        player.standUp();
                }
            }
        }        
    }
    
    public static void dumpData()
    {
        _log.info("");
        _log.info("");
        
        if (!_joining && !_teleport && !_started)
        {
            _log.info("<<---------------------------------->>");
            _log.info(">> CTF Engine infos dump (INACTIVE) <<");
            _log.info("<<--^----^^-----^----^^------^^----->>");
        }
        else if (_joining && !_teleport && !_started)
        {
            _log.info("<<--------------------------------->>");
            _log.info(">> CTF Engine infos dump (JOINING) <<");
            _log.info("<<--^----^^-----^----^^------^----->>");
        }
        else if (!_joining && _teleport && !_started)
        {
            _log.info("<<---------------------------------->>");
            _log.info(">> CTF Engine infos dump (TELEPORT) <<");
            _log.info("<<--^----^^-----^----^^------^^----->>");
        }
        else if (!_joining && !_teleport && _started)
        {
            _log.info("<<--------------------------------->>");
            _log.info(">> CTF Engine infos dump (STARTED) <<");
            _log.info("<<--^----^^-----^----^^------^----->>");
        }

        _log.info("Name: " + _eventName);
        _log.info("Desc: " + _eventDesc);
        _log.info("Join location: " + _joiningLocationName);
        _log.info("Min lvl: " + _minlvl);
        _log.info("Max lvl: " + _maxlvl);
        _log.info("");
        _log.info("##########################");
        _log.info("# _teams(Vector<String>) #");
        _log.info("##########################");
        
        for (String team : _teams)
           _log.info(team + " Flags Taken :" + _teamPointsCount.get(_teams.indexOf(team)));

        if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE"))
        {
            _log.info("");
            _log.info("#########################################");
            _log.info("# _playersShuffle(Vector<L2PcInstance>) #");
            _log.info("#########################################");
        
            for (L2PcInstance player : _playersShuffle)
            {
                if (player != null)
                    _log.info("Name: " + player.getName());
            }
        }
        
        _log.info("");
        _log.info("##################################");
        _log.info("# _players(Vector<L2PcInstance>) #");
        _log.info("##################################");
        
        for (L2PcInstance player : _players)
        {
            if (player != null)
                _log.info("Name: " + player.getName() + "   Team: " + player._teamNameCTF + "  Flags :" + player._countCTFflags);
        }
        
        _log.info("");
        _log.info("#####################################################################");
        _log.info("# _savePlayers(Vector<String>) and _savePlayerTeams(Vector<String>) #");
        _log.info("#####################################################################");
        
        for (String player : _savePlayers)
            _log.info("Name: " + player + "    Team: " + _savePlayerTeams.get(_savePlayers.indexOf(player)));
        
        _log.info("");
        _log.info("");
    	System.out.println("**********==CTF==************");
    	System.out.println("CTF._teamPointsCount:"+_teamPointsCount.toString());
    	System.out.println("CTF._flagIds:"+_flagIds.toString());
    	System.out.println("CTF._flagSpawns:"+_flagSpawns.toString());
    	System.out.println("CTF._throneSpawns:"+_throneSpawns.toString());
    	System.out.println("CTF._flagsTaken:"+_flagsTaken.toString());
    	System.out.println("CTF._flagsX:"+_flagsX.toString());
    	System.out.println("CTF._flagsY:"+_flagsY.toString());
    	System.out.println("CTF._flagsZ:"+_flagsZ.toString());
        System.out.println("************EOF**************\n");
        System.out.println("");
    }
    
    public static void loadData()
    {
        _eventName = new String();
        _eventDesc = new String();
        _topTeam = new String();
        _joiningLocationName = new String();
        _teams = new Vector<String>();
        _savePlayers = new Vector<String>();
        _savePlayerTeams = new Vector<String>();
        _players = new FastList<L2PcInstance>();
        _playersShuffle = new Vector<L2PcInstance>();
        _teamPlayersCount = new Vector<Integer>();
        _teamPointsCount = new Vector<Integer>();
        _teamColors = new Vector<Integer>();
        _teamsX = new Vector<Integer>();
        _teamsY = new Vector<Integer>();
        _teamsZ = new Vector<Integer>();
    	
        _throneSpawns = new Vector<L2Spawn>();
        _flagSpawns = new Vector<L2Spawn>();
    	_flagsTaken = new Vector<Boolean>();
    	_flagIds = new Vector<Integer>();
    	_flagsX = new Vector<Integer>();
    	_flagsY = new Vector<Integer>();
    	_flagsZ = new Vector<Integer>();
        
        _joining = false;
        _teleport = false;
        _started = false;
        _sitForced = false;
        _npcId = 0;
        _npcX = 0;
        _npcY = 0;
        _npcZ = 0;
        _npcHeading = 0;
        _rewardId = 0;
        _rewardAmount = 0;
        _topScore = 0;
        _minlvl = 0;
        _maxlvl = 0;
        _joinTime = 0;
        _eventTime = 0;
        _minPlayers = 0;
        _maxPlayers = 0;
        
        java.sql.Connection con = null;
        try
        {
            PreparedStatement statement;
            ResultSet rs;

            con = L2DatabaseFactory.getInstance().getConnection();

            statement = con.prepareStatement("Select * from ctf");
            rs = statement.executeQuery();
            
            int teams =0;
            
            while (rs.next())
            {
                _eventName = rs.getString("eventName");
                _eventDesc = rs.getString("eventDesc");
                _joiningLocationName = rs.getString("joiningLocation");
                _minlvl = rs.getInt("minlvl");
                _maxlvl = rs.getInt("maxlvl");
                _npcId = rs.getInt("npcId");
                _npcX = rs.getInt("npcX");
                _npcY = rs.getInt("npcY");
                _npcZ = rs.getInt("npcZ");
                _npcHeading = rs.getInt("npcHeading");
                _rewardId = rs.getInt("rewardId");
                _rewardAmount = rs.getInt("rewardAmount"); 
                teams = rs.getInt("teamsCount");
                _joinTime = rs.getInt("joinTime");
                _eventTime = rs.getInt("eventTime");
                _minPlayers = rs.getInt("minPlayers");
                _maxPlayers = rs.getInt("maxPlayers");
            }
            statement.close();
            
            int index = -1;
            if (teams > 0)
                index = 0;    
            while (index < teams && index > -1)
            { 
                statement = con.prepareStatement("Select * from ctf_teams where teamId = ?");
                statement.setInt(1, index);
                rs = statement.executeQuery(); 
                while (rs.next())
                {
                    _teams.add(rs.getString("teamName"));
                    _teamPlayersCount.add(0);
                    _teamPointsCount.add(0); 
                    _teamColors.add(0);
                    _teamsX.add(0);
                    _teamsY.add(0);
                    _teamsZ.add(0);
                    _teamsX.set(index, rs.getInt("teamX"));
                    _teamsY.set(index, rs.getInt("teamY"));
                    _teamsZ.set(index, rs.getInt("teamZ"));
                    _teamColors.set(index, rs.getInt("teamColor"));
                    _flagsX.add(0);
                    _flagsY.add(0);
                    _flagsZ.add(0);
                    _flagsX.set(index, rs.getInt("flagX"));
                    _flagsY.set(index, rs.getInt("flagY"));
                    _flagsZ.set(index, rs.getInt("flagZ"));
                    _flagSpawns.add(null);
                    _flagIds.add(_FlagNPC);
                	_flagsTaken.add(false);
                    
                }
                index ++;
                statement.close();
            }
        }
        catch (Exception e)
        {
            _log.severe("Exception: CTF.loadData(): " + e.getMessage());
        }
        finally {try { con.close(); } catch (Exception e) {}}
    }
    
    public static void saveData()
    {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;
            
            statement = con.prepareStatement("Delete from ctf");
            statement.execute();
            statement.close();

            statement = con.prepareStatement("INSERT INTO ctf (eventName, eventDesc, joiningLocation, minlvl, maxlvl, npcId, npcX, npcY, npcZ, npcHeading, rewardId, rewardAmount, teamsCount, joinTime, eventTime, minPlayers, maxPlayers) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");  
            statement.setString(1, _eventName);
            statement.setString(2, _eventDesc);
            statement.setString(3, _joiningLocationName);
            statement.setInt(4, _minlvl);
            statement.setInt(5, _maxlvl);
            statement.setInt(6, _npcId);
            statement.setInt(7, _npcX);
            statement.setInt(8, _npcY);
            statement.setInt(9, _npcZ);
            statement.setInt(10, _npcHeading);
            statement.setInt(11, _rewardId);
            statement.setInt(12, _rewardAmount);
            statement.setInt(13, _teams.size());
            statement.setInt(14, _joinTime);
            statement.setInt(15, _eventTime);
            statement.setInt(16, _minPlayers);
            statement.setInt(17, _maxPlayers);
            statement.execute();
            statement.close();
            
            statement = con.prepareStatement("Delete from ctf_teams");
            statement.execute();
            statement.close();
            
            for (String teamName : _teams)
            { 
                int index = _teams.indexOf(teamName);
                
                if (index == -1)
                    return;
                statement = con.prepareStatement("INSERT INTO ctf_teams (teamId ,teamName, teamX, teamY, teamZ, teamColor, flagX, flagY, flagZ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");  
                statement.setInt(1 , index);
                statement.setString(2, teamName);
                statement.setInt(3, _teamsX.get(index));
                statement.setInt(4, _teamsY.get(index));
                statement.setInt(5, _teamsZ.get(index));
                statement.setInt(6, _teamColors.get(index));
                statement.setInt(7, _flagsX.get(index));
                statement.setInt(8, _flagsY.get(index));
                statement.setInt(9, _flagsZ.get(index));
                statement.execute();
                statement.close();
            }
        }
        catch (Exception e)
        {
            _log.severe("Exception: CTF.saveData(): " + e.getMessage());
        }        
        finally {try { con.close(); } catch (Exception e) {}}
    }

    public static void showEventHtml(L2PcInstance eventPlayer, String objectId)
    {
        try
        {
            NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

            TextBuilder replyMSG = new TextBuilder("<html><body>");
            replyMSG.append("CTF event registration<br><br><br>");

            if (!_started && !_joining)
                replyMSG.append("<center>CTF match is not in progress yet.Wait for a GM to start the event<br>.</center>");
            else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE") && !checkMaxPlayers(_playersShuffle.size()))
            {
                if (!CTF._started)
                {
                    replyMSG.append("Players : <font color=\"00FF00\">" + _playersShuffle.size() +".</font><br>");
                    replyMSG.append("Max players : <font color=\"00FF00\">" + _maxPlayers + "</font><br><br>");
                    replyMSG.append("<font color=\"FFFF00\">The event has reached its maximum capacity.</font><br>");
                }
            }
            else if (eventPlayer.isCursedWeaponEquiped())
            {
                replyMSG.append("<font color=\"FFFF00\">You can't participate in this event with a cursed Weapon.</font><br>");
            }
            else if (!_started && _joining && eventPlayer.getLevel()>=_minlvl && eventPlayer.getLevel()<_maxlvl)
            {
                if (_players.contains(eventPlayer) || _playersShuffle.contains(eventPlayer) || checkShufflePlayers(eventPlayer))
                {
                    if (Config.CTF_EVEN_TEAMS.equals("NO") || Config.CTF_EVEN_TEAMS.equals("BALANCE"))
                        replyMSG.append("You are already participating in team <font color=\"LEVEL\">" + eventPlayer._teamNameCTF + "</font><br><br>");
                    else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE"))
                        replyMSG.append("You are already participating!<br><br>");

                    replyMSG.append("<table border=\"0\"><tr>");
                    replyMSG.append("<td width=\"200\">Wait till event start or</td><br>");
                    replyMSG.append("<td width=\"60\"><center><button value=\"Leave\" action=\"bypass -h npc_" + objectId + "_ctf_player_leave\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></td>");
                    replyMSG.append("</tr></table>");
                }
                else
                {
                    replyMSG.append("You want to participate in Event?<br><br>");
                    replyMSG.append("<td width=\"200\">Min level : <font color=\"00FF00\">" + _minlvl + "</font></td><br>");
                    replyMSG.append("<td width=\"200\">Max level : <font color=\"00FF00\">" + _maxlvl + "</font></td><br><br>");
                    
                    if (Config.CTF_EVEN_TEAMS.equals("NO") || Config.CTF_EVEN_TEAMS.equals("BALANCE"))
                    {
                        replyMSG.append("<center><table border=\"0\">");
                    
                        for (String team : _teams)
                        {
							replyMSG.append("<tr><td width=\"100\"><font color=\"LEVEL\">" + team + "</font>&nbsp;(" + teamPlayersCount(team) + " ��������)</td></tr><br>");
                            replyMSG.append("<center><button value=\"Join\" action=\"bypass -h npc_" + objectId + "_ctf_player_join " + team + "\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
                        }
                    
                        replyMSG.append("</table></center>");
                    }
                    else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE"))
                    {
                        replyMSG.append("<center><table border=\"0\">");
                        
                        for (String team : _teams)
                            replyMSG.append("<tr><td width=\"100\"><font color=\"LEVEL\">" + team + "</font></td>");
                    
                        replyMSG.append("</table></center><br>");
                        
                        replyMSG.append("<button value=\"Join\" action=\"bypass -h npc_" + objectId + "_ctf_player_join eventShuffle\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
                    }
                }
            }
            else if (_started && !_joining)
                replyMSG.append("<center>CTF match is in progress.</center>");
            else if (eventPlayer.getLevel()<_minlvl || eventPlayer.getLevel()>_maxlvl )
            {
                replyMSG.append("You level : <font color=\"00FF00\">" + eventPlayer.getLevel() +"</font><br>");
                replyMSG.append("Min level : <font color=\"00FF00\">" + _minlvl + "</font><br>");
                replyMSG.append("Max level : <font color=\"00FF00\">" + _maxlvl + "</font><br><br>");
                replyMSG.append("<font color=\"FFFF00\">You can't participatein this event.</font><br>");
            }
            
            replyMSG.append("</body></html>");
            adminReply.setHtml(replyMSG.toString());
            eventPlayer.sendPacket(adminReply);
            eventPlayer.sendPacket( new ActionFailed() );
        }
        catch (Exception e)
        {
            _log.severe("CTF Engine[showEventHtlm(" + eventPlayer.getName() + ", " + objectId + ")]: exception" + e.getMessage());
        }
    }

    public static void addPlayer(L2PcInstance player, String teamName)
    {
        if (!addPlayerOk(teamName, player))
            return;
        
        if (Config.CTF_EVEN_TEAMS.equals("NO") || Config.CTF_EVEN_TEAMS.equals("BALANCE"))
        {
            player._teamNameCTF = teamName;
            _players.add(player);
            setTeamPlayersCount(teamName, teamPlayersCount(teamName)+1);
        }
        else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE"))
            _playersShuffle.add(player);
        
        player._inEventCTF = true;
        player._countCTFflags = 0;
    }
    
    public static void removeOfflinePlayers()
    {
    	try
    	{
    		if(_playersShuffle== null || _playersShuffle.isEmpty())
    			return;
    		if(_playersShuffle!= null && !_playersShuffle.isEmpty())
    		{
    			for(L2PcInstance player: _playersShuffle)
    			{
    				if(player==null || player.isOnline()==0)
    					_playersShuffle.remove(player);
    			}
    		}
    	}
    	catch (Exception e)
    	{return;}
    }
    public static boolean checkShufflePlayers(L2PcInstance eventPlayer)
    {
    	try
    	{
    		for(L2PcInstance player: _playersShuffle)
    		{
    			if(player==null)
    			{
    				_playersShuffle.remove(player);
    				continue;
    			}
    			else if(player.getObjectId()==eventPlayer.getObjectId())
    			{
    				eventPlayer._inEventCTF = true;
    				eventPlayer._countCTFflags = 0;
    				return true;
    			}
    			else if(player.getName().equals(eventPlayer.getName()))
    			{
    				_playersShuffle.remove(player);
    				_playersShuffle.add(eventPlayer);
    				eventPlayer._inEventCTF = true;
    				eventPlayer._countCTFflags = 0;
    				return true;
    			}
    		}
    	}
    	catch (Exception e)
		{}
    	return false;
    }
    
    public static boolean addPlayerOk(String teamName, L2PcInstance eventPlayer)
    {
    	if (checkShufflePlayers(eventPlayer) || eventPlayer._inEventCTF)
    	{
    		eventPlayer.sendMessage("You already participated in the event!");
    		return false;
    	}
    	
    	for(L2PcInstance player: _players)
    	{
    		if(player.getObjectId()==eventPlayer.getObjectId())
        	{
                eventPlayer.sendMessage("You already participated in the event!"); 
                return false;
        	}
    		else if(player.getName()==eventPlayer.getName())
        	{
                eventPlayer.sendMessage("You already participated in the event!"); 
                return false;
        	}
    	}
    	if(_players.contains(eventPlayer))
    	{
            eventPlayer.sendMessage("You already participated in the event!"); 
            return false;
    	}

        if (Config.CTF_EVEN_TEAMS.equals("NO"))
            return true;
        else if (Config.CTF_EVEN_TEAMS.equals("BALANCE"))
        {
            boolean allTeamsEqual = true;
            int countBefore = -1;
        
            for (int playersCount : _teamPlayersCount)
            {
                if (countBefore == -1)
                    countBefore = playersCount;
            
                if (countBefore != playersCount)
                {
                    allTeamsEqual = false;
                    break;
                }
            
                countBefore = playersCount;
            }
        
            if (allTeamsEqual)
                return true;

            countBefore = Integer.MAX_VALUE;
        
            for (int teamPlayerCount : _teamPlayersCount)
            {
                if (teamPlayerCount < countBefore)
                    countBefore = teamPlayerCount;
            }

            Vector<String> joinableTeams = new Vector<String>();
        
            for (String team : _teams)
            {
                if (teamPlayersCount(team) == countBefore)
                    joinableTeams.add(team);
            }
        
            if (joinableTeams.contains(teamName))
                return true;
        }
        else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE"))
            return true;

        eventPlayer.sendMessage("Too many players in team \"" + teamName + "\"");
        return false;
    }

    public static synchronized void addDisconnectedPlayer(L2PcInstance player)
    {
    	if ((Config.CTF_EVEN_TEAMS.equals("SHUFFLE") && (_teleport || _started)) || (Config.CTF_EVEN_TEAMS.equals("NO") || Config.CTF_EVEN_TEAMS.equals("BALANCE")  && (_teleport || _started)))
        {
            if (Config.CTF_ON_START_REMOVE_ALL_EFFECTS)
            {
                for (L2Effect e : player.getAllEffects())
                {
                    if (e != null)
                        e.exit();
                }
            }

			for (L2PcInstance disconnectPlayer : _players)
			{
				if (player.getName().equals(disconnectPlayer.getName()))
				{
					_players.remove(disconnectPlayer);
					_log.info("[CTF DEBUG] Remove old data for player " + disconnectPlayer.getName());
				}
			}

            player._teamNameCTF = _savePlayerTeams.get(_savePlayers.indexOf(player.getName()));

			if(!_players.contains(player))
			{
                _players.add(player);
				_log.info("[CTF DEBUG] Add new data for player " + player.getName());
			}
			
            player._originalKarmaCTF = player.getKarma();
            player._inEventCTF = true;
            player._countCTFflags = 0;

			player.setTeam(_teams.indexOf(player._teamNameCTF) + 1);
            player.setKarma(0);
            player.broadcastUserInfo();
            player.teleToLocation(_teamsX.get(_teams.indexOf(player._teamNameCTF)), _teamsY.get(_teams.indexOf(player._teamNameCTF)), _teamsZ.get(_teams.indexOf(player._teamNameCTF)));
            Started(player);
            CheckRestoreFlags();
        }
    }
    
    public static void removePlayer(L2PcInstance player)
    {
    	if(player._inEventCTF)
    	{
    		if(!_joining)
    		{
				player.setTeam(0);
    			player.setKarma(player._originalKarmaCTF);
    			player.broadcastUserInfo();
    		}
    		player._teamNameCTF = new String();
    		player._countCTFflags = 0;
    		player._inEventCTF = false;
    		
    		if ((Config.CTF_EVEN_TEAMS.equals("NO") || Config.CTF_EVEN_TEAMS.equals("BALANCE")) && _players.contains(player))
    		{
    			setTeamPlayersCount(player._teamNameCTF, teamPlayersCount(player._teamNameCTF)-1);
    			_players.remove(player);
    		}
    		else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE") && (!_playersShuffle.isEmpty() && _playersShuffle.contains(player)))
    			_playersShuffle.remove(player);
    	}
    }
    
    public static void cleanCTF()
    {
    	_log.info("CTF : Cleaning players.");
    	for (L2PcInstance player : _players)
    	{
    		if(player != null)
    		{
    			if (player._haveFlagCTF)
    				removeFlagFromPlayer(player);
    			else 
    				player.getInventory().destroyItemByItemId("", CTF._FLAG_IN_HAND_ITEM_ID, 1, player, null);
    			player._haveFlagCTF = false;
    			removePlayer(player);
    			if(_savePlayers.contains(player.getName()))
    				_savePlayers.remove(player.getName());
    			player._inEventCTF = false;
    		}
    	}
    	if (_playersShuffle != null && !_playersShuffle.isEmpty())
    	{
    		for (L2PcInstance player : _playersShuffle)
            {
        		if(player != null)
        			player._inEventCTF = false;
            }
    	}
    	_log.info("CTF : Cleaning teams and flags.");
    	for (String team : _teams)
    	{
    		int index = _teams.indexOf(team);
    		_teamPointsCount.set(index,0);
    		_flagSpawns.set(index,null);
    		_flagsTaken.set(index,false);
    		_teamPlayersCount.set(index, 0);
    		_teamPointsCount.set(index, 0);
    	}
    	_topScore = 0;
        _topTeam = new String();
        _players = new FastList<L2PcInstance>();
        _playersShuffle = new Vector<L2PcInstance>();
        _savePlayers = new Vector<String>();
        _savePlayerTeams = new Vector<String>();
        _teamPointsCount = new Vector<Integer>();
        _flagSpawns = new Vector<L2Spawn>();
        _flagsTaken = new Vector<Boolean>();
        _teamPlayersCount = new Vector<Integer>();
        _log.info("Cleaning CTF done.");
        _log.info("Loading new data from MySql");
        loadData();
    }
    
    public static void unspawnEventNpc()
    {
        if (_npcSpawn == null)
            return;

        _npcSpawn.getLastSpawn().deleteMe();
        _npcSpawn.stopRespawn();
        SpawnTable.getInstance().deleteSpawn(_npcSpawn, true);
    }
    
    public static void teleportFinish()
    {
        Announcements(_eventName + "(CTF): Teleport back to participation NPC!");

        ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
        {
            public void run()
            {
                for (L2PcInstance player : _players)
                {
                    if (player !=  null)
                        player.teleToLocation(_npcX, _npcY, _npcZ);
                }
                cleanCTF();
            }
        }, 3000);
    }

    public static int teamFlagCount(String teamName)
    {
        int index = _teams.indexOf(teamName);
        
        if (index == -1)
            return -1;

        return _teamPointsCount.get(index);
    }
    
    public static void setTeamFlagCount(String teamName, int teamFlagCount)
    {
        int index = _teams.indexOf(teamName);
        
        if (index == -1)
            return;

        _teamPointsCount.set(index, teamFlagCount);
    }
    
    private static void calculateOutSideOfCTF(){
    	if (_teams==null || _flagSpawns==null || _teamsX==null || _teamsY==null || _teamsZ==null)
    		return;
    	int division = _teams.size()*2,pos=0;
    	int[] locX = new int[division], locY = new int[division], locZ = new int[division];
    	for (L2Spawn flag : _flagSpawns){    	
    		locX[pos]=flag.getLocx();
    		locY[pos]=flag.getLocy();
    		locZ[pos]=flag.getLocz();
    		pos++;
    		if(pos>division/2)
    			break;
    	}
    	for (int x=0; x<_teams.size() ; x++){
    		locX[pos]=_teamsX.get(x);
    		locY[pos]=_teamsY.get(x);
    		locZ[pos]=_teamsZ.get(x);
    		pos++;
    		if(pos>division)
    			break;
    	}
    	int centerX=0, centerY=0, centerZ=0;
    	for (int x=0; x<pos ; x++){
    		centerX+=(locX[x]/division);
    		centerY+=(locY[x]/division);
    		centerZ+=(locZ[x]/division);
    	}
    	int maxX = 0, maxY = 0,maxZ = 0;
    	for (int x=0; x<pos ; x++){
    		if (maxX<2*Math.abs(centerX-locX[x])) maxX = (int)(2*Math.abs(centerX-locX[x]));
    		if (maxY<2*Math.abs(centerY-locY[x])) maxY = (int)(2*Math.abs(centerY-locY[x]));
    		if (maxZ<2*Math.abs(centerZ-locZ[x])) maxZ = (int)(2*Math.abs(centerZ-locZ[x]));
    	}

    	eventCenterX = centerX;
    	eventCenterY = centerY;
    	eventCenterZ = centerZ;
    	eventOffset  = maxX;
    	if (eventOffset<maxY)  eventOffset = maxY;
    	if (eventOffset<maxZ)  eventOffset = maxZ;
    }
    
    public static boolean isOutsideCTFArea(L2PcInstance _player){
    	if (_player == null || _player.isOnline() == 0) return true;
    	if (!(_player.getX() > eventCenterX-eventOffset && _player.getX() < eventCenterX+eventOffset &&
    		_player.getY() > eventCenterY-eventOffset && _player.getY() < eventCenterY+eventOffset &&
    		_player.getZ() > eventCenterZ-eventOffset && _player.getZ() < eventCenterZ+eventOffset))
    		return true;
    	return false;
    }
}
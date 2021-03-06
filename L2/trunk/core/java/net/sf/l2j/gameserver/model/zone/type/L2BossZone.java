package net.sf.l2j.gameserver.model.zone.type;

import java.util.Map;

import javolution.util.FastMap;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.instancemanager.VanHalterManager;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.zone.L2ZoneType;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.util.L2FastList;

public class L2BossZone extends L2ZoneType
{
    private String _zoneName;
    private int _timeInvade;
    private boolean _enabled = true;
    private int _InvadeTimeAfterRestart = 0;
    private boolean _IsFlyingEnable = false;
    private String _QuestName = null;
    private FastMap<Integer, Long> _playerAllowedReEntryTimes;
    private L2FastList<Integer> _playersAllowed;
    private int[] _oustLoc = {0,0,0};
    
    protected L2FastList<L2Character> _raidList= new L2FastList<L2Character>();
    
    public L2BossZone(int id)
    {
        super(id);
        _playerAllowedReEntryTimes = new FastMap<Integer, Long>();
        _playersAllowed = new L2FastList<Integer>();
        _oustLoc = new int[3];
    }
    
    @Override
    public void setParameter(String name, String value)
    {
        if (name.equals("name"))
        {
            _zoneName = value;
        }
        
        else if (name.equals("InvadeTime"))
        {
            _timeInvade = Integer.parseInt(value);
        }
        else if (name.equals("InvadeTimeAfterRestart"))
        {
        	_InvadeTimeAfterRestart = Integer.parseInt(value);
        }
        else if (name.equals("Flying"))
        {
			_IsFlyingEnable = Boolean.parseBoolean(value);
		}
        else if (name.equals("QuestName"))
        {
        	_QuestName = value;
		}
        else if (name.equals("EnabledByDefault"))
        {
        	_enabled = Boolean.parseBoolean(value);
        }
		else if (name.equals("oustX"))
		{
			_oustLoc[0] = Integer.parseInt(value);
		}
		else if (name.equals("oustY"))
		{
			_oustLoc[1] = Integer.parseInt(value);
		}
		else if (name.equals("oustZ"))
		{
			_oustLoc[2] = Integer.parseInt(value);
		}
        else
        {
            super.setParameter(name, value);
        }
    }
    
    public void setZoneEnabled(boolean flag)
    {
    	if (_enabled != flag)
    		oustAllPlayers();
    	
    	_enabled = flag;
    }
    
    public String getZoneName()
    {
        return _zoneName;
    }
    
    public int getTimeInvade()
    {
        return _timeInvade;
    }

    public void setAllowedPlayers(L2FastList<Integer> players)
    {
        if (players != null)
            _playersAllowed = players;
    }

    public L2FastList<Integer> getAllowedPlayers()
    {
        return _playersAllowed;
    }
    
    public boolean isPlayerAllowed(L2PcInstance player)
    {
        if (player.isGM())
            return true;
        else if (_playersAllowed.contains(player.getObjectId()))
            return true;
        else
        {
        	if (_oustLoc[0] != 0 && _oustLoc[1] != 0 && _oustLoc[2] != 0)
            	player.teleToLocation(_oustLoc[0], _oustLoc[1], _oustLoc[2]);
            else
            	player.teleToLocation(MapRegionTable.TeleportWhereType.Town);
            return false;
        }
     }
    
    public void oustAllPlayers()
    {
        if (_characterList == null) return;
        if (_characterList.isEmpty()) return;
        for (L2Character character : _characterList.values())
        {
            if (character == null) continue;
            if (character instanceof L2PcInstance)
            {
                L2PcInstance player = (L2PcInstance) character;
                if (player.isOnline() == 1)
                	if (_oustLoc[0] != 0 && _oustLoc[1] != 0 && _oustLoc[2] != 0)
    	            	player.teleToLocation(_oustLoc[0], _oustLoc[1], _oustLoc[2]);
    	            else
    	            	player.teleToLocation(MapRegionTable.TeleportWhereType.Town);
            }
        }
        _playerAllowedReEntryTimes.clear();
        _playersAllowed.clear();
    }
    
    public void allowPlayerEntry(L2PcInstance player, int durationInSec)
    {
        if (!player.isGM())
        {
            _playersAllowed.add(player.getObjectId());
            _playerAllowedReEntryTimes.put(player.getObjectId(), System.currentTimeMillis() + durationInSec*1000);

        }
    }
    
    @Override
    protected void onDieInside(L2Character character)
    {
    }
    
    @Override
    protected void onReviveInside(L2Character character)
    {
    }

    public int getInvadeTimeAfterRestart()
    {
    	return _InvadeTimeAfterRestart;
    }

	public boolean isFlyingEnable()
	{
		return _IsFlyingEnable;
	}

    public String getQuestName()
    {
    	return _QuestName;
    }

	protected void onEnter(L2Character character)
    {
		character.setInsideZone(L2Character.ZONE_BOSS, true);
		character.setInsideZone(L2Character.ZONE_NOSUMMONFRIEND, true);
		if (character instanceof L2PcInstance)
		{
            L2PcInstance player = (L2PcInstance) character;

            if (player.isGM())
            {
				player.sendMessage("[DEBUG] You entered to zone" + _zoneName);

				if(_QuestName != null)
				{
					if(player.getQuestState(_QuestName) == null)
					{
						player.sendMessage("[DEBUG] "+ _QuestName);
					}
					else
					{
						player.sendMessage("[DEBUG] "+ _QuestName);
					}
				}
            }

	        if (!player.isGM() && player.isFlying() && !player.isInJail() && !_IsFlyingEnable)
		    {
	        	player.teleToLocation(MapRegionTable.TeleportWhereType.Town);
	        	return;
	        }

	        if(!player.isGM() && _QuestName != null && player.getQuestState(_QuestName) == null)
	        {
				player.sendMessage("You tried illegal entry!");
				player.setInJail(true,1440);
	        	return;
	        }

	        /*
	        if (player.getQuestState("baium") == null && player.isInsideZone(12007))
	        {
	        	player.sendMessage("?????????? ?????? ????? ???????");
	        	player.teleToLocation(MapRegionTable.TeleportWhereType.Town);
	        	return;
	        }
	        */
	        
	        if (_zoneName.equalsIgnoreCase("AltarofSacrifice"))
	        	VanHalterManager.getInstance().intruderDetection((L2PcInstance)character);

	        
		}
    }

    protected void onExit(L2Character character)
    {
    	character.setInsideZone(L2Character.ZONE_BOSS, false);
    	character.setInsideZone(L2Character.ZONE_NOSUMMONFRIEND, false);
    	
        if (character instanceof L2PcInstance)
        {
            L2PcInstance player = (L2PcInstance) character;
            if (player.isGM())
            {
                player.sendMessage("[DEBUG] You leave zone" + _zoneName);
                return;
            }

            if(player.isOnline() == 1 && _QuestName != null && player.getQuestState(_QuestName) != null)
            {
            	player.getQuestState(_QuestName).exitQuest(true);
            }
        }
        if (character instanceof L2MonsterInstance && character.isRaid()) 
        {
        	((L2MonsterInstance) character).returnHome();
        }
        if (character instanceof L2PlayableInstance)
        {
        	if (getCharactersInside() != null && getCharactersInside().size() > 0)
        	{
        		_raidList.clear();
        		int count = 0;
        		for (L2Character obj : getCharactersInside().values())
        		{
        			if (obj == null)
        				continue;
        			if (obj instanceof L2PlayableInstance)
        				count++;
        			else if (obj instanceof L2Attackable && obj.isRaid())
        			{
        				_raidList.add(obj);
        			}
        		}
        		// if inside zone isnt any player, force all boss instance return to its spawn points
        		if (count == 0 && !_raidList.isEmpty())
        		{
        			for (int i = 0; i < _raidList.size(); i++)
        			{
        				L2Attackable raid = (L2Attackable) _raidList.get(i);
        				if (!raid.isInsideRadius(raid.getSpawn().getLocx(), raid.getSpawn().getLocy(), 150, false))
        					raid.returnHome();
        				
        			}
        		}
        	}
        }//3.1.3 3.1.4
        if ((character instanceof L2Attackable && ((L2Attackable) character).getNpcId() == 29001 ))
        {
        	((L2Attackable) character).teleToHome();
        }
        
    }	
    
	public void movePlayersTo(int x, int y, int z)
	{
		if (_characterList == null)
			return;
		if (_characterList.isEmpty())
			return;
		for (L2Character character : _characterList.values())
		{
			if (character == null)
				continue;
			if (character instanceof L2PcInstance)
			{
				L2PcInstance player = (L2PcInstance) character;
				if (player.isOnline() == 1)
					player.teleToLocation(x, y, z);
			}
		}
	}

	public void broadcastPacket(L2GameServerPacket packet)
	  {
	    if ((this._characterList == null) || (this._characterList.isEmpty()))
	      return;
	    for (L2Character character : this._characterList.values())
	    {
	      if (character == null)
	        continue;
	      if ((character instanceof L2PcInstance))
	      {
	        L2PcInstance player = (L2PcInstance)character;
	        if (player.isOnline() == 1)
	          player.sendPacket(packet);
	      }
	    }
	  }

	public void updateKnownList(L2NpcInstance npc)
	  {
	    if ((this._characterList == null) || (this._characterList.isEmpty()))
	      return;
	    Map<Integer, L2PcInstance> npcKnownPlayers = npc.getKnownList().getKnownPlayers();
	    for (L2Character character : this._characterList.values())
	    {
	      if (character == null)
	        continue;
	      if ((character instanceof L2PcInstance))
	      {
	        L2PcInstance player = (L2PcInstance)character;
	        if (player.isOnline() == 1)
	          npcKnownPlayers.put(Integer.valueOf(player.getObjectId()), player);
	      }
	    }
	  }
}
package l2rt.gameserver.instancemanager;

import gnu.trove.map.hash.TIntObjectHashMap;

import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.PartyRoom;
import l2rt.gameserver.network.serverpackets.ExClosePartyRoom;
import l2rt.gameserver.network.serverpackets.PartyMatchDetail;
import l2rt.util.GArray;

import java.util.ArrayList;
import java.util.logging.Logger;

public class PartyRoomManager
{
	protected static Logger _log = Logger.getLogger(PartyRoomManager.class.getName());

	private static PartyRoomManager _instance;

	private int _lastId = 1;
	private ArrayList<Long> _waitingList = new ArrayList<Long>();
	private TIntObjectHashMap<PartyRoom> _rooms = new TIntObjectHashMap<PartyRoom>();

	public PartyRoomManager()
	{
		_instance = this;
	}

	public static PartyRoomManager getInstance()
	{
		if(_instance == null)
			_instance = new PartyRoomManager();
		return _instance;
	}

	public void addToWaitingList(L2Player player)
	{
		synchronized (_waitingList)
		{
			if(!_waitingList.contains(player.getStoredId()))
				_waitingList.add(player.getStoredId());
		}
	}

	public void removeFromWaitingList(L2Player player)
	{
		synchronized (_waitingList)
		{
			_waitingList.remove(player.getStoredId());
		}
	}

	public GArray<L2Player> getWaitingList()
	{
		synchronized (_waitingList)
		{
			GArray<L2Player> result = new GArray<L2Player>(_waitingList.size());
			L2Player player;
			for(int i = _waitingList.size() - 1; i >= 0; i--)
				if((player = L2ObjectsStorage.getAsPlayer(_waitingList.get(i))) == null)
					_waitingList.remove(i);
				else
					result.add(player);
			return result;
		}
	}

	public ArrayList<L2Player> getWaitingList(int minLevel, int maxLevel)
	{
		ArrayList<L2Player> res = new ArrayList<L2Player>();
		for(L2Player pc : getWaitingList())
			if(pc.getLevel() >= minLevel && pc.getLevel() <= maxLevel) //&& !pc.isGM())
				res.add(pc);
		return res;
	}

	public PartyRoom addRoom(int minLevel, int maxLevel, int maxMembers, int lootDist, String title, L2Player leader)
	{
		PartyRoom room = new PartyRoom(_lastId, minLevel, maxLevel, maxMembers, lootDist, title, leader);
		_rooms.put(_lastId, room);
		removeFromWaitingList(leader);
		_lastId++;
		return room;
	}

	public PartyRoom changeRoom(Integer id, int minLevel, int maxLevel, int maxMembers, int lootDist, String title)
	{
		PartyRoom room = _rooms.get(id);
		room.setMinLevel(minLevel);
		room.setMaxLevel(maxLevel);
		room.setMaxMembers(maxMembers);
		room.setLootDist(lootDist);
		room.setTitle(title);
		return room;
	}

	public PartyRoom getRoom(Integer id)
	{
		return _rooms.get(id);
	}

	public void removeRoom(Integer id)
	{
		PartyRoom room = _rooms.get(id);
		_rooms.remove(id);
		L2Player member;
		for(Long storedId : room.getMembers())
			if((member = L2ObjectsStorage.getAsPlayer(storedId)) != null)
			{
				member.sendPacket(new ExClosePartyRoom(), new PartyMatchDetail(member));
				member.setPartyRoom(0);
				member.broadcastUserInfo(true);
				addToWaitingList(member);
			}
		if(id == _lastId - 1)
			_lastId--;
		if(_lastId < 1)
			_lastId = 1;
	}

	public TIntObjectHashMap<PartyRoom> getRooms()
	{
		return _rooms;
	}

	public GArray<PartyRoom> getRooms(int region, int lvlRst, L2Player activeChar)
	{
		GArray<PartyRoom> res = new GArray<PartyRoom>();
		for(PartyRoom room : getRooms().values())
		{
			if(region > 0 && room.getLocation() != region)
				continue;
			else if(region == -2 && room.getLocation() != PartyRoomManager.getInstance().getLocation(activeChar))
				continue;
			if(lvlRst == 0 && (room.getMinLevel() > activeChar.getLevel() || room.getMaxLevel() < activeChar.getLevel()))
				continue;
			res.add(room);
		}
		return res;
	}

	public boolean isLeader(L2Player player)
	{
		PartyRoom room = _rooms.get(player.getPartyRoom());
		if(room == null || room.getLeader() == null)
			return false;
		return room.getLeader().equals(player);
	}

	public void joinPartyRoom(L2Player player, Integer roomId)
	{
		PartyRoom room = _rooms.get(roomId);
		if(room == null)
		{
			player.sendPacket(Msg.YOU_ARE_NOT_ALLOWED_TO_ENTER_THE_PARTY_ROOM);
			return;
		}
		if(player.getLevel() < room.getMinLevel() || player.getLevel() > room.getMaxLevel())
		{
			player.sendPacket(Msg.SINCE_YOU_DO_NOT_MEET_THE_REQUIREMENTS_YOU_ARE_NOT_ALLOWED_TO_ENTER_THE_PARTY_ROOM);
			return;
		}
		if(room.getMembersSize() >= room.getMaxMembers())
		{
			player.sendPacket(Msg.PARTY_IS_FULL);
			return;
		}
		room.addMember(player);
	}

	public int getLocation(L2Player player)
	{
		if(player == null)
			return 0;
		int loc = 0;
		int town = TownManager.getInstance().getClosestTownNumber(player);
		switch(town)
		{
			case 1: //Near Talking Island Village
				loc = 1; //Talking Island
				break;
			case 2: // Near Elven village
				loc = 4; // Elven Territory
				break;
			case 3: //Near Dark Elven village
				loc = 3; //Dark Elven Ter.
				break;
			case 6: //Near Gludio
				loc = 2; //Gludio
				break;
			case 8: //Near Dion
				loc = 5; //Dion
				break;
			case 9: //Near Giran
				loc = 6; //Giran
				break;
			case 10: //Near Oren
				loc = 10; //Oren
				break;
			case 11: //Near Aden
				loc = 13; //Aden
				break;
			case 12: //Near Hunters Village
				loc = 11; //Hunters Village
				break;
			case 14: // Near Rune village
				loc = 14; //Rune
				break;
			case 15: //Near Goddard
				loc = 15; //Goddard
				break;
			case 16: //Near Schuttgart
				loc = 9; //Schuttgart
				break;
			case 4: //Orc village
			case 5: //Dwarven village
			case 7: //Near Gludin village
			case 13: //Near Heine
			case 17: //Near Kamael village
			case 18: //Near Primeval Isle
			case 19: //Near Fantasy Isle
				loc = 7; //Neutral Zone
				break;
		}
		return loc;
	}
}
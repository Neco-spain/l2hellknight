package l2rt.gameserver.model;

import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.instancemanager.PartyRoomManager;
import l2rt.gameserver.network.serverpackets.*;

import java.util.Vector;

public class PartyRoom
{
	private final int _id;
	private int _minLevel, _maxLevel, _lootDist, _maxMembers;
	private String _title;
	private final Vector<Long> members_list = new Vector<Long>();

	public PartyRoom(int id, int minLevel, int maxLevel, int maxMembers, int lootDist, String title, L2Player leader)
	{
		_id = id;
		_minLevel = minLevel;
		_maxLevel = maxLevel;
		_maxMembers = maxMembers;
		_lootDist = lootDist;
		_title = title;
		members_list.add(leader.getStoredId());
		leader.setPartyRoom(_id);
	}

	public void addMember(L2Player member)
	{
		if(members_list.contains(member.getStoredId()))
			return;

		members_list.add(member.getStoredId());
		L2Player player;
		member.setPartyRoom(_id);
		for(Long storedId : members_list)
			if((player = L2ObjectsStorage.getAsPlayer(storedId)) != null)
			{
				player.sendPacket(new PartyMatchList(this));
				player.sendPacket(new ExPartyRoomMember(this, player));
				player.sendPacket(new SystemMessage(SystemMessage.S1_HAS_ENTERED_THE_PARTY_ROOM).addString(member.getName()));
			}
		PartyRoomManager.getInstance().removeFromWaitingList(member);
		member.broadcastUserInfo(true);
	}

	public void removeMember(L2Player member, boolean oust)
	{
		members_list.remove(member.getStoredId());
		member.setPartyRoom(0);
		if(members_list.isEmpty())
			PartyRoomManager.getInstance().removeRoom(getId());
		else
		{
			L2Player player;
			for(Long storedId : members_list)
				if((player = L2ObjectsStorage.getAsPlayer(storedId)) != null)
					player.sendPacket(new PartyMatchList(this), new ExPartyRoomMember(this, player), new SystemMessage(oust ? SystemMessage.S1_HAS_BEEN_OUSTED_FROM_THE_PARTY_ROOM : SystemMessage.S1_HAS_LEFT_THE_PARTY_ROOM).addString(member.getName()));
		}

		member.sendPacket(new ExClosePartyRoom(), new PartyMatchDetail(member), oust ? Msg.YOU_HAVE_BEEN_OUSTED_FROM_THE_PARTY_ROOM : Msg.YOU_HAVE_EXITED_FROM_THE_PARTY_ROOM);
		PartyRoomManager.getInstance().addToWaitingList(member);
		member.broadcastUserInfo(true);
	}

	public void broadcastPacket(L2GameServerPacket packet)
	{
		L2Player player;
		for(Long storedId : members_list)
			if((player = L2ObjectsStorage.getAsPlayer(storedId)) != null)
				player.sendPacket(packet);
	}

	public void updateInfo()
	{
		L2Player player;
		for(Long storedId : members_list)
			if((player = L2ObjectsStorage.getAsPlayer(storedId)) != null)
			{
				player.sendPacket(new PartyMatchList(this));
				player.sendPacket(new ExPartyRoomMember(this, player));
			}
	}

	public Vector<Long> getMembers()
	{
		return members_list;
	}

	public int getMembersSize()
	{
		return members_list.size();
	}

	public int getId()
	{
		return _id;
	}

	public int getMinLevel()
	{
		return _minLevel;
	}

	public int getMaxLevel()
	{
		return _maxLevel;
	}

	public int getMaxMembers()
	{
		return _maxMembers;
	}

	public int getLootDist()
	{
		return _lootDist;
	}

	public String getTitle()
	{
		return _title;
	}

	public L2Player getLeader()
	{
		return members_list.isEmpty() ? null : L2ObjectsStorage.getAsPlayer(members_list.get(0));
	}

	public void setMinLevel(int minLevel)
	{
		_minLevel = minLevel;
	}

	public void setMaxLevel(int maxLevel)
	{
		_maxLevel = maxLevel;
	}

	public void setMaxMembers(int maxMembers)
	{
		_maxMembers = maxMembers;
	}

	public void setLootDist(int lootDist)
	{
		_lootDist = lootDist;
	}

	public void setTitle(String title)
	{
		_title = title;
	}

	public int getLocation()
	{
		return PartyRoomManager.getInstance().getLocation(getLeader());
	}
}
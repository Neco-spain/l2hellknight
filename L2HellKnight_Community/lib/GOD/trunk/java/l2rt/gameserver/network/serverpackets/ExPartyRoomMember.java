package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.instancemanager.PartyRoomManager;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.PartyRoom;
import l2rt.util.GArray;

/**
 * Format:(ch) d d [dsdddd]
 */
public class ExPartyRoomMember extends L2GameServerPacket
{
	private boolean isLeader;
	private GArray<PartyRoomMemberInfo> members_list = null;

	public ExPartyRoomMember(PartyRoom room, L2Player activeChar)
	{
		L2Player leader = room.getLeader();
		if(leader == null)
			return;

		isLeader = activeChar.equals(leader);
		members_list = new GArray<PartyRoomMemberInfo>(room.getMembersSize());
		L2Player member;
		for(Long storedId : room.getMembers())
			if((member = L2ObjectsStorage.getAsPlayer(storedId)) != null)
				members_list.add(new PartyRoomMemberInfo(member, leader));
	}

	@Override
	protected final void writeImpl()
	{
		if(members_list == null)
			return;

		writeC(EXTENDED_PACKET);
		writeH(0x08);
		writeD(isLeader ? 0x01 : 0x00);
		writeD(members_list.size());
		for(PartyRoomMemberInfo member_info : members_list)
		{
			writeD(member_info.objectId);
			writeS(member_info.name);
			writeD(member_info.classId);
			writeD(member_info.level);
			writeD(member_info.location);
			writeD(member_info.memberType);//1-leader     2-party member    0-not party member
		}
	}

	static class PartyRoomMemberInfo
	{
		public final int objectId, classId, level, location, memberType;
		public final String name;

		public PartyRoomMemberInfo(L2Player member, L2Player leader)
		{
			objectId = member.getObjectId();
			name = member.getName();
			classId = member.getClassId().ordinal();
			level = member.getLevel();
			location = PartyRoomManager.getInstance().getLocation(member);
			memberType = member.equals(leader) ? 0x01 : member.getParty() != null && leader.getParty() == member.getParty() ? 0x02 : 0x00;
		}
	}
}
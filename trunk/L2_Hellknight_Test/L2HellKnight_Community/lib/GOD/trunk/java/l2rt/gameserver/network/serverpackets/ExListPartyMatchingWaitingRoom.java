package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.instancemanager.PartyRoomManager;
import l2rt.gameserver.model.L2Player;
import l2rt.util.GArray;

import java.util.ArrayList;

/**
 * Format:(ch) d [sdd]
 */
public class ExListPartyMatchingWaitingRoom extends L2GameServerPacket
{
	private final GArray<PartyMatchingWaitingInfo> waiting_list;
	private final int _fullSize;

	public ExListPartyMatchingWaitingRoom(L2Player searcher, int minLevel, int maxLevel, int page)
	{
		int first = (page - 1) * 64;
		int firstNot = page * 64;
		int i = 0;

		ArrayList<L2Player> temp = PartyRoomManager.getInstance().getWaitingList(minLevel, maxLevel);
		_fullSize = temp.size();

		waiting_list = new GArray<PartyMatchingWaitingInfo>(_fullSize);
		for(L2Player pc : temp)
		{
			if(i < first || i >= firstNot)
				continue;
			waiting_list.add(new PartyMatchingWaitingInfo(pc));
			i++;
		}
	}

	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x36);

		writeD(_fullSize);
		writeD(waiting_list.size());
		for(PartyMatchingWaitingInfo waiting_info : waiting_list)
		{
			writeS(waiting_info.name);
			writeD(waiting_info.classId);
			writeD(waiting_info.level);
		}
	}

	static class PartyMatchingWaitingInfo
	{
		public int classId, level;
		public final String name;

		public PartyMatchingWaitingInfo(L2Player member)
		{
			name = member.getName();
			classId = member.getClassId().getId();
			level = member.getLevel();
			if (member.isAwaking())
				classId = member.getAwakingId();
		}
	}
}
package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.instancemanager.PartyRoomManager;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.PartyRoom;

/**
 * Format: (ch) dd
 */
public class RequestDismissPartyRoom extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int _roomId, _data2;

	@Override
	public void readImpl()
	{
		_roomId = readD(); //room id
		_data2 = readD(); //unknown
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		PartyRoom room = PartyRoomManager.getInstance().getRoom(_roomId);
		if(room.getLeader() == null || room.getLeader().equals(activeChar))
			PartyRoomManager.getInstance().removeRoom(_roomId);
		else
			PartyRoomManager.getInstance().getRoom(_roomId).removeMember(activeChar, false);
	}
}
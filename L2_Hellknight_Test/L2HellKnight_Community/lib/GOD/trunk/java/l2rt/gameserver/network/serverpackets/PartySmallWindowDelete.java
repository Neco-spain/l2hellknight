package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.L2Player;

public class PartySmallWindowDelete extends L2GameServerPacket
{
	private int member_obj_id;
	private String member_name;

	public PartySmallWindowDelete(L2Player member)
	{
		member_obj_id = member.getObjectId();
		member_name = member.getName();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x51);
		writeD(member_obj_id);
		writeS(member_name);
	}
}
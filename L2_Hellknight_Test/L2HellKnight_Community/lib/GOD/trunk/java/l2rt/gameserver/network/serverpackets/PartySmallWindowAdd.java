package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.L2Player;

public class PartySmallWindowAdd extends L2GameServerPacket
{
	//dddSdddddddddddd{ddSddddd}
	private final PartySmallWindowAll.PartySmallWindowMemberInfo member;

	public PartySmallWindowAdd(L2Player _member)
	{
		member = new PartySmallWindowAll.PartySmallWindowMemberInfo(_member);
	}

	@Override
	protected final void writeImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;

		writeC(0x4F);
		writeD(player.getObjectId()); // c3
		writeD(0);//writeD(0x04); ?? //c3

		writeD(member._id);
		writeS(member._name);
		writeD(member.curCp);
		writeD(member.maxCp);
		writeD(member.vitality);
		writeD(member.curHp);
		writeD(member.maxHp);
		writeD(member.curMp);
		writeD(member.maxMp);
		writeD(member.level);
		writeD(member.class_id);
		writeD(0);//writeD(0x01); ??
		writeD(member.race_id);
		writeD(1); // Hide Name
		writeD(0); // unk
		writeD(0); // Идет поиск замены данному игроку.
	}
}
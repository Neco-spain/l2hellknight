package l2rt.gameserver.network.serverpackets;

public class ExShowQuestMark extends L2GameServerPacket
{
	private int _questId;

	public ExShowQuestMark(int questId)
	{
		_questId = questId;
	}

	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x21);
		writeD(_questId);
	}
}
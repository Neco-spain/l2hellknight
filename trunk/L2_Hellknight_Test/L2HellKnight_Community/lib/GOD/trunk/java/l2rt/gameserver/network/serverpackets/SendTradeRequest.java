package l2rt.gameserver.network.serverpackets;

public class SendTradeRequest extends L2GameServerPacket
{
	private int _senderID;

	public SendTradeRequest(int senderID)
	{
		_senderID = senderID;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x70);
		writeD(_senderID);
	}
}
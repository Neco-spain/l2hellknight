package l2rt.gameserver.network.serverpackets;

public class ExPutItemResultForVariationCancel extends L2GameServerPacket
{
	private int _itemObjId;
	private int _price;

	public ExPutItemResultForVariationCancel(int itemObjId, int price)
	{
		_itemObjId = itemObjId;
		_price = price;
	}

	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x57);
		writeD(0x40A97712);
		writeD(_itemObjId);
		writeD(0x27);
		writeD(0x2006);
		writeQ(_price);
		writeD(0x01);
	}
}
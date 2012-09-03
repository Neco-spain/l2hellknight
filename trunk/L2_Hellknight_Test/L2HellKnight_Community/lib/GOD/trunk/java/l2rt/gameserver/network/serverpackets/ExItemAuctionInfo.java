package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.L2ItemInstance;

/**
 * cdQddQhhdhhhh hhhhhhhh
 */
public class ExItemAuctionInfo extends L2GameServerPacket
{
	private final L2Player _cha;
	private final L2ItemInstance _item;
	private final long _currectPrice;
	private final int _remainTime;

	public ExItemAuctionInfo(L2Player cha, L2ItemInstance item, long currectPrice, int remainTime)
	{
		_cha = cha;
		_item = item;
		_currectPrice = currectPrice;
		_remainTime = remainTime;
	}

	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x68);

		writeC(0x00); // TODO ?
		writeD(_remainTime);
		writeQ(_cha.getAdena());
		writeD(_item.getObjectId());
		writeD(_item.getItemId());
		writeQ(_currectPrice);
		writeH(_item.getItem().getType2ForPackets());
		writeH(_item.getCustomType1());
		writeD(_item.getBodyPart());
		writeH(_item.getEnchantLevel());
		writeH(_item.getCustomType2());

		writeH(0x00); //?? GraciaEpilogue возможно после getAugmentationId

		// Вместо 2H
		writeH(_item.getAugmentationId());
		writeH(0x00);

		writeItemElements(_item); // hhhhhhhh
		writeEnchantEffect(_item);
		writeD(0x00);//Visible itemID

		//FIXME GraciaEpilogue точно такая же структура что и выше (QddQhhdhhhhdhhhhhhhh hhh) еще раз 
	}
}
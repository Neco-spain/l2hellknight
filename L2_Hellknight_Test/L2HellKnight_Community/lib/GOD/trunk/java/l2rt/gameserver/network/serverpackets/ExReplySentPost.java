package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.network.clientpackets.RequestExCancelSentPost;
import l2rt.gameserver.network.clientpackets.RequestExRequestSentPost;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.TradeItem;
import l2rt.gameserver.model.items.MailParcelController;
import l2rt.gameserver.model.items.MailParcelController.Letter;
import l2rt.gameserver.templates.L2Item;

/**
 * Просмотр собственного отправленного письма. Шлется в ответ на {@link RequestExRequestSentPost}.
 * При нажатии на кнопку Cancel клиент шлет {@link RequestExCancelSentPost}.
 * @see ExReplyReceivedPost
 */
public class ExReplySentPost extends L2GameServerPacket
{
	private Letter _letter;

	public ExReplySentPost(L2Player cha, int post)
	{
		_letter = MailParcelController.getInstance().getLetter(post);

		if(_letter == null)
		{
			_letter = new Letter();
			cha.sendPacket(new ExShowSentPostList(cha));
		}
	}

	// ddSSS dx[hddQdddhhhhhhhhhh] Qd
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0xAD);

		writeD(_letter.id); // id письма
		writeD(_letter.price > 0 ? 1 : 0); // 1 - письмо с запросом оплаты, 0 - просто письмо

		writeS(_letter.receiverName); // кому
		writeS(_letter.topic); // топик
		writeS(_letter.body); // тело

		writeD(_letter.attached.size()); // количество приложенных вещей
		for(TradeItem temp : _letter.attached)
		{
			L2Item item = temp.getItem();
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
            writeD(temp.getEquipSlot());
			writeQ(temp.getCount());
            writeH(item.getType2ForPackets());
            writeH(temp.getCustomType1());
            writeH(0x00);//equiped?
            writeD(item.getBodyPart());
            writeH(temp.getEnchantLevel());
            writeH(temp.getCustomType2());
            writeH(0x00);// Augmentation
            writeH(0x00);// Augmentation
            writeD(-1);// Mana
            writeD(0x00);// Temp Life time
			writeItemElements(temp);
			writeEnchantEffect(temp);
			writeD(0x00);//Visible itemID
            writeD(0x00);//unknown
		}

		writeQ(_letter.price); // для писем с оплатой - цена
		writeD(_letter.attachments > 0 ? 1 : 0);
        writeD(_letter.system);
	}
}
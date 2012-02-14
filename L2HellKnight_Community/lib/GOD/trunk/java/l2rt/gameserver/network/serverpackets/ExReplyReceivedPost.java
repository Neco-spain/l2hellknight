package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.network.clientpackets.RequestExReceivePost;
import l2rt.gameserver.network.clientpackets.RequestExRejectPost;
import l2rt.gameserver.network.clientpackets.RequestExRequestReceivedPost;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.TradeItem;
import l2rt.gameserver.model.items.MailParcelController;
import l2rt.gameserver.model.items.MailParcelController.Letter;
import l2rt.gameserver.templates.L2Item;

/**
 * Просмотр полученного письма. Шлется в ответ на {@link RequestExRequestReceivedPost}.
 * При попытке забрать приложенные вещи клиент шлет {@link RequestExReceivePost}.
 * При возврате письма клиент шлет {@link RequestExRejectPost}.
 * @see ExReplySentPost
 */
public class ExReplyReceivedPost extends L2GameServerPacket
{
	private Letter _letter;

	public ExReplyReceivedPost(L2Player cha, int post)
	{
		_letter = MailParcelController.getInstance().getLetter(post);

		if(_letter == null)
			_letter = new Letter();
		else if(_letter.unread > 0)
		{
			MailParcelController.getInstance().markMailRead(post);
			_letter.unread = 0;
		}

		cha.sendPacket(new ExShowReceivedPostList(cha));
	}

	// dddSSS dx[hddQdddhhhhhhhhhh] Qdd
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0xAB);
		writeD(0);	
		writeD(_letter.id); // id письма
		writeD(_letter.price > 0 ? 1 : 0); // 1 - письмо с запросом оплаты, 0 - просто письмо
		writeD(0); // для писем с флагом "от news informer" в отправителе значится "****", всегда оверрайдит тип на просто письмо
		writeS(_letter.senderName); // от кого
		writeS(_letter.topic); // топик
		writeS(_letter.body); // тело

		writeD(_letter.attached.size()); // количество приложенных вещей		
		L2Item item;
		for(TradeItem temp : _letter.attached)
		{
			item = temp.getItem();
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
            writeD(temp.getEquipSlot());
			writeQ(temp.getCount());
            writeH(item.getType2());
            writeH(temp.getCustomType1());
            writeH(0x00);// equiped?
            writeD(temp.getItem().getBodyPart());
			writeH(temp.getEnchantLevel());
            writeH(temp.getCustomType2());
            writeH(0x00);
            writeH(0x00);
            writeD(-1);
            writeD(0x00);
			writeH(1); // при 0 итем красный(заблокирован) 
			writeItemElements(temp);
			writeEnchantEffect(temp);
			writeD(0x00);//Visible itemID
			writeD(0x00);
		}

		writeQ(_letter.price); // для писем с оплатой - цена
		writeD(_letter.attachments > 0 ? 1 : 0); // 1 - письмо можно вернуть
		writeD(_letter.system); // 1 - на письмо нельзя отвечать, его нельзя вернуть, в отправителе значится news informer (или "****" если установлен флаг в начале пакета)
	}
}
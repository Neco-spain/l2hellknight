package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.network.clientpackets.RequestExDeleteReceivedPost;
import l2rt.gameserver.network.clientpackets.RequestExPostItemList;
import l2rt.gameserver.network.clientpackets.RequestExRequestReceivedPost;
import l2rt.gameserver.network.clientpackets.RequestExRequestReceivedPostList;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.MailParcelController;
import l2rt.gameserver.model.items.MailParcelController.Letter;
import l2rt.util.GArray;

/**
 * Появляется при нажатии на кнопку "почта" или "received mail", входящие письма
 * <br> Ответ на {@link RequestExRequestReceivedPostList}.
 * <br> При нажатии на письмо в списке шлется {@link RequestExRequestReceivedPost} а в ответ {@link ExReplyReceivedPost}.
 * <br> При попытке удалить письмо шлется {@link RequestExDeleteReceivedPost}.
 * <br> При нажатии кнопки send mail шлется {@link RequestExPostItemList}.
 * @see ExShowSentPostList аналогичный список отправленной почты
 */
public class ExShowReceivedPostList extends L2GameServerPacket
{
	private static final GArray<Letter> EMPTY_LETTERS = new GArray<Letter>(0);
	private GArray<Letter> letters;

	public ExShowReceivedPostList(L2Player cha)
	{
		letters = MailParcelController.getInstance().getReceived(cha.getObjectId());

		if(letters == null)
			letters = EMPTY_LETTERS;
	}

	// d dx[dSSddddddd]
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0xAA);

		//20477743   PUSH Engine.205A0360                      ASCII "dd"
		//204777FC   PUSH Engine.205AC548                      ASCII "dSSddddddd"
		//204778A7   PUSH Engine.205AC53C                      ASCII "ddSSddddddd"
		writeD(1); // unknown: каждый раз разное
		writeD(letters.size()); // количество писем		
		writeD(0x01); //TODO: Unk
		for(Letter letter : letters)
		{
			writeD(0);
			writeD(letter.id); // уникальный id письма
			writeS(letter.topic); // топик
			writeS(letter.senderName); // отправитель
			writeD(letter.price > 0 ? 1 : 0); // если тут 1 то письмо требует оплаты
			writeD(letter.validtime - (int) (System.currentTimeMillis() / 1000)); // время действительности письма в секундах
			writeD(letter.unread); // письмо не прочитано - его нельзя удалить и оно выделяется ярким цветом
			writeD(0); // ?
			writeD(letter.attachments); // 1 - письмо с приложением, 0 - просто письмо
			writeD(0); // если тут 1 и следующий параметр 1 то отправителем будет "****", если тут 2 то следующий параметр игнорируется
			writeD(letter.system); // 1 - отправителем значится "**News Informer**"
		}
	}
}
package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.MailParcelController;
import l2rt.gameserver.network.serverpackets.ExReplyReceivedPost;
import l2rt.gameserver.network.serverpackets.ExShowReceivedPostList;

/**
 * Шлется клиентом как запрос на отказ принять письмо из {@link ExReplyReceivedPost}. Если к письму приложены вещи то их надо вернуть отправителю.
 */
public class RequestExRejectPost extends L2GameClientPacket
{
	private int postId;

	/**
	 * format: d
	 */
	@Override
	public void readImpl()
	{
		postId = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player cha = getClient().getActiveChar();
		if(cha == null)
			return;

		MailParcelController.getInstance().returnLetter(postId);

		cha.sendPacket(new ExShowReceivedPostList(cha));
	}
}
package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.network.clientpackets.RequestExSendPost;

/**
 * Запрос на отправку нового письма. Шлется в ответ на {@link RequestExSendPost}.
 */
public class ExReplyWritePost extends L2GameServerPacket
{
	private int _reply;

	/**
	 * @param i если 1 окно создания письма закрывается
	 */
	public ExReplyWritePost(int i)
	{
		_reply = i;
	}

	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0xB4);
		writeD(_reply); // 1 - закрыть окно письма, иное - не закрывать
	}
}
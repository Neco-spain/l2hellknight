package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.serverpackets.ExUISetting;

/**
 * format: (ch)db
 */
public class RequestSaveKeyMapping extends L2GameClientPacket
{
	private byte[] _data;

	@Override
	protected void readImpl()
	{
		int length = readD();
		if(length > _buf.remaining() || length > Short.MAX_VALUE || length < 0)
		{
			_data = null;
			return;
		}
		_data = new byte[length];
		readB(_data);
	}

	@Override
	protected void runImpl()
	{
		if(_data == null)
			return;
		L2Player player = getClient().getActiveChar();

		if(player != null)
		{
			player.setKeyBindings(_data);
			player.sendPacket(new ExUISetting(_data));
		}
	}
}
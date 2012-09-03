package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.network.L2GameClient;

public class ReplyGameGuardQuery extends L2GameClientPacket
{
	// Format: cdddd
	public byte[] _data = new byte[72];

	@Override
	protected void readImpl()
	{
		ccpGuard.Protection.doReadReplyGameGuard(getClient(), _buf, _data);
	}

	@Override
	protected void runImpl()
	{
		L2GameClient client = this.getClient();
		if(client != null)
		{
			getClient().setGameGuardOk(true);
			ccpGuard.Protection.doReplyGameGuard(client, _data);
		}
	}

}
package l2rt.gameserver.network.serverpackets;

public class PledgeReceiveUpdatePower extends L2GameServerPacket
{
	private int _privs;

	public PledgeReceiveUpdatePower(int privs)
	{
		_privs = privs;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x42);
		writeD(_privs); //Filler??????
	}
}
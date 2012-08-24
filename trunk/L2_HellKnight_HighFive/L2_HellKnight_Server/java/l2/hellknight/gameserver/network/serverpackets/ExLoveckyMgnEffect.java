package l2.hellknight.gameserver.network.serverpackets;

public class ExLoveckyMgnEffect extends L2GameServerPacket
{
	private int _cas;
	
	public ExLoveckyMgnEffect(int cas)
	{
		_cas = cas;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xE0);
		writeD(_cas);
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:E0 ExLoveckyMgnEffect".intern();
	}
	
}
package l2.hellknight.gameserver.network.serverpackets;

import l2.hellknight.gameserver.datatables.LovecTable;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;

public class ExLoveckyMgnPointInfo extends L2GameServerPacket
{
	private int _bonus;
	
	public ExLoveckyMgnPointInfo(L2PcInstance player)
	{
		_bonus = LovecTable.getInstance().getAdventPoints(player.getObjectId());
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xDF);
		writeD(_bonus);
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:DF ExLoveckyMgnPointInfo".intern();
	}
}
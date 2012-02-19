package l2.brick.gameserver.network.serverpackets;

//import l2.brick.gameserver.instancemanager.KrateisCubeManager;
import l2.brick.gameserver.instancemanager.KrateisCubeManager.CCPlayer;
//import l2.brick.gameserver.model.actor.instance.L2PcInstance;

public class ExPVPMatchCCRecord extends L2GameServerPacket
{
	private static final String _S__FE_89_EXPVPMATCHCCRECORD = "[S] FE:89 ExPVPMatchCCRecord";
	
	private final int _state;
	private final CCPlayer[] _players;
	
	
	
	public ExPVPMatchCCRecord(int state, CCPlayer[] players)
	{
		_state = state;
		_players = players;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x89);

		writeD(_state); // 0x01 - in progress, 0x02 - finished
		writeD(_players.length);
		
		for (CCPlayer ccp : _players)
		{
			writeS(ccp.getName());
			writeD(ccp.getPoints());
		}
	}

	@Override
	public String getType()
	{
		return _S__FE_89_EXPVPMATCHCCRECORD;
	}
}
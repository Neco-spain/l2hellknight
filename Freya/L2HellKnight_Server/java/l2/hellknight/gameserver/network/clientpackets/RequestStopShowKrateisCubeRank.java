package l2.hellknight.gameserver.network.clientpackets;

public class RequestStopShowKrateisCubeRank extends L2GameClientPacket
{
	
  private static final String _C__51_REQUESTSTOPSHOWKRATEISCUBERANK = "[C] 51 RequestStopShowKrateisCubeRank";
	
  @Override
	protected void readImpl()
	{
		// trigger packet
	}

	@Override
	protected void runImpl()
	{
	}
	
	@Override
	public String getType()
	{
		return _C__51_REQUESTSTOPSHOWKRATEISCUBERANK;
	}
}
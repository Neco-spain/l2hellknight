package l2m.gameserver.network.serverpackets;

public class AskJoinPledge extends L2GameServerPacket
{
  private int _requestorId;
  private String _pledgeName;

  public AskJoinPledge(int requestorId, String pledgeName)
  {
    _requestorId = requestorId;
    _pledgeName = pledgeName;
  }

  protected final void writeImpl()
  {
    writeC(44);
    writeD(_requestorId);
    writeS(_pledgeName);
  }
}
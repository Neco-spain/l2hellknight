package l2m.gameserver.network.serverpackets;

public class JoinPledge extends L2GameServerPacket
{
  private int _pledgeId;

  public JoinPledge(int pledgeId)
  {
    _pledgeId = pledgeId;
  }

  protected final void writeImpl()
  {
    writeC(45);

    writeD(_pledgeId);
  }
}
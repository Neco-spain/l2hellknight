package net.sf.l2j.gameserver.network.serverpackets;

public class JoinPledge extends L2GameServerPacket
{
  private static final String _S__45_JOINPLEDGE = "[S] 33 JoinPledge";
  private int _pledgeId;

  public JoinPledge(int pledgeId)
  {
    _pledgeId = pledgeId;
  }

  protected final void writeImpl()
  {
    writeC(51);

    writeD(_pledgeId);
  }

  public String getType()
  {
    return "[S] 33 JoinPledge";
  }
}
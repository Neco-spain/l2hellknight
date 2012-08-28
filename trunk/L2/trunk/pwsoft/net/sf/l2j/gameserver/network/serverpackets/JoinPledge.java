package net.sf.l2j.gameserver.network.serverpackets;

public class JoinPledge extends L2GameServerPacket
{
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
}
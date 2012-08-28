package net.sf.l2j.gameserver.network.serverpackets;

public class AskJoinPledge extends L2GameServerPacket
{
  private int _requestorObjId;
  private String _pledgeName;

  public AskJoinPledge(int requestorObjId, String pledgeName)
  {
    _requestorObjId = requestorObjId;
    _pledgeName = pledgeName;
  }

  protected final void writeImpl()
  {
    writeC(50);
    writeD(_requestorObjId);
    writeS(_pledgeName);
  }
}
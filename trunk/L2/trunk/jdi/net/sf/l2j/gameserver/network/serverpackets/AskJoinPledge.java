package net.sf.l2j.gameserver.network.serverpackets;

public class AskJoinPledge extends L2GameServerPacket
{
  private static final String _S__44_ASKJOINPLEDGE = "[S] 32 AskJoinPledge";
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

  public String getType()
  {
    return "[S] 32 AskJoinPledge";
  }
}
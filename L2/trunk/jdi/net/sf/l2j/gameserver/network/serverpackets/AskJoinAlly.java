package net.sf.l2j.gameserver.network.serverpackets;

public class AskJoinAlly extends L2GameServerPacket
{
  private static final String _S__A8_ASKJOINALLY_0XA8 = "[S] a8 AskJoinAlly 0xa8";
  private String _requestorName;
  private int _requestorObjId;

  public AskJoinAlly(int requestorObjId, String requestorName)
  {
    _requestorName = requestorName;
    _requestorObjId = requestorObjId;
  }

  protected final void writeImpl()
  {
    writeC(168);
    writeD(_requestorObjId);
    writeS(_requestorName);
  }

  public String getType()
  {
    return "[S] a8 AskJoinAlly 0xa8";
  }
}
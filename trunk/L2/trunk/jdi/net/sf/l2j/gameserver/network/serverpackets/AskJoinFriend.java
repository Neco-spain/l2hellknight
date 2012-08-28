package net.sf.l2j.gameserver.network.serverpackets;

public class AskJoinFriend extends L2GameServerPacket
{
  private static final String _S__7d_ASKJoinFriend_0X7d = "[S] 7d AskJoinFriend 0x7d";
  private String _requestorName;

  public AskJoinFriend(String requestorName)
  {
    _requestorName = requestorName;
  }

  protected final void writeImpl()
  {
    writeC(125);
    writeS(_requestorName);
    writeD(0);
  }

  public String getType()
  {
    return "[S] 7d AskJoinFriend 0x7d";
  }
}
package net.sf.l2j.gameserver.network.serverpackets;

public class AskJoinFriend extends L2GameServerPacket
{
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
}
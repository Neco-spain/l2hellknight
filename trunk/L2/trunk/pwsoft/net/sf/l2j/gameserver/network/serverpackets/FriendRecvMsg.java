package net.sf.l2j.gameserver.network.serverpackets;

public class FriendRecvMsg extends L2GameServerPacket
{
  private String _sender;
  private String _receiver;
  private String _message;

  public FriendRecvMsg(String sender, String reciever, String message)
  {
    _sender = sender;
    _receiver = reciever;
    _message = message;
  }

  protected final void writeImpl()
  {
    writeC(253);

    writeD(0);
    writeS(_receiver);
    writeS(_sender);
    writeS(_message);
  }
}
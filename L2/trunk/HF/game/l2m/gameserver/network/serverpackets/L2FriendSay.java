package l2m.gameserver.network.serverpackets;

public class L2FriendSay extends L2GameServerPacket
{
  private String _sender;
  private String _receiver;
  private String _message;

  public L2FriendSay(String sender, String reciever, String message)
  {
    _sender = sender;
    _receiver = reciever;
    _message = message;
  }

  protected final void writeImpl()
  {
    writeC(120);
    writeD(0);
    writeS(_receiver);
    writeS(_sender);
    writeS(_message);
  }
}
package l2p.gameserver.serverpackets;

public class ExReplyWritePost extends L2GameServerPacket
{
  public static final L2GameServerPacket STATIC_TRUE = new ExReplyWritePost(1);
  public static final L2GameServerPacket STATIC_FALSE = new ExReplyWritePost(0);
  private int _reply;

  public ExReplyWritePost(int i)
  {
    _reply = i;
  }

  protected void writeImpl()
  {
    writeEx(180);
    writeD(_reply);
  }
}
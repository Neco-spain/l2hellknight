package l2p.gameserver.serverpackets;

public class ExNoticePostArrived extends L2GameServerPacket
{
  public static final L2GameServerPacket STATIC_TRUE = new ExNoticePostArrived(1);
  public static final L2GameServerPacket STATIC_FALSE = new ExNoticePostArrived(0);
  private int _anim;

  public ExNoticePostArrived(int useAnim)
  {
    _anim = useAnim;
  }

  protected void writeImpl()
  {
    writeEx(169);

    writeD(_anim);
  }
}
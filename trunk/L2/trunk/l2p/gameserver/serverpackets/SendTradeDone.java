package l2p.gameserver.serverpackets;

public class SendTradeDone extends L2GameServerPacket
{
  public static final L2GameServerPacket SUCCESS = new SendTradeDone(1);
  public static final L2GameServerPacket FAIL = new SendTradeDone(0);
  private int _response;

  private SendTradeDone(int num)
  {
    _response = num;
  }

  protected final void writeImpl()
  {
    writeC(28);
    writeD(_response);
  }
}
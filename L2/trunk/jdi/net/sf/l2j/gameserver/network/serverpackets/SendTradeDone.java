package net.sf.l2j.gameserver.network.serverpackets;

public class SendTradeDone extends L2GameServerPacket
{
  private static final String _S__32_SENDTRADEDONE = "[S] 22 SendTradeDone";
  private int _num;

  public SendTradeDone(int num)
  {
    _num = num;
  }

  protected final void writeImpl()
  {
    writeC(34);
    writeD(_num);
  }

  public String getType()
  {
    return "[S] 22 SendTradeDone";
  }
}
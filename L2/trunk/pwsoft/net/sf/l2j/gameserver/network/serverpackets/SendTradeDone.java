package net.sf.l2j.gameserver.network.serverpackets;

public class SendTradeDone extends L2GameServerPacket
{
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
    return "S.SendTradeDone";
  }
}
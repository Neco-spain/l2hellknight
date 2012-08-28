package l2p.gameserver.serverpackets;

public class ShowCalc extends L2GameServerPacket
{
  private int _calculatorId;

  public ShowCalc(int calculatorId)
  {
    _calculatorId = calculatorId;
  }

  protected final void writeImpl()
  {
    writeC(226);
    writeD(_calculatorId);
  }
}
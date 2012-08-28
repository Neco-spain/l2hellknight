package net.sf.l2j.gameserver.network.serverpackets;

public class ShowCalculator extends L2GameServerPacket
{
  private int _calculatorId;

  public ShowCalculator(int calculatorId)
  {
    _calculatorId = calculatorId;
  }

  protected final void writeImpl()
  {
    writeC(220);
    writeD(_calculatorId);
  }

  public String getType()
  {
    return "S.ShowCalculator";
  }
}
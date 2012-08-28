package net.sf.l2j.gameserver.network.serverpackets;

public class ExVariationCancelResult extends L2GameServerPacket
{
  private static final String _S__FE_57_EXVARIATIONCANCELRESULT = "[S] FE:57 ExVariationCancelResult";
  private int _closeWindow;
  private int _unk1;

  public ExVariationCancelResult(int result)
  {
    _closeWindow = 1;
    _unk1 = result;
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(87);
    writeD(_closeWindow);
    writeD(_unk1);
  }

  public String getType()
  {
    return "[S] FE:57 ExVariationCancelResult";
  }
}
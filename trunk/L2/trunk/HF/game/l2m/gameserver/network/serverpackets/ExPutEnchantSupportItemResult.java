package l2m.gameserver.network.serverpackets;

public class ExPutEnchantSupportItemResult extends L2GameServerPacket
{
  private int _result;

  public ExPutEnchantSupportItemResult(int result)
  {
    _result = result;
  }

  protected void writeImpl()
  {
    writeEx(130);
    writeD(_result);
  }
}
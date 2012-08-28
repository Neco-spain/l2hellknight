package l2p.gameserver.serverpackets;

public class ExPutEnchantTargetItemResult extends L2GameServerPacket
{
  public static final L2GameServerPacket FAIL = new ExPutEnchantTargetItemResult(0);
  public static final L2GameServerPacket SUCCESS = new ExPutEnchantTargetItemResult(1);
  private int _result;

  public ExPutEnchantTargetItemResult(int result)
  {
    _result = result;
  }

  protected void writeImpl()
  {
    writeEx(129);
    writeD(_result);
  }
}
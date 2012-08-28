package l2m.gameserver.network.serverpackets;

public class ExAttributeEnchantResult extends L2GameServerPacket
{
  private int _result;

  public ExAttributeEnchantResult(int unknown)
  {
    _result = unknown;
  }

  protected final void writeImpl()
  {
    writeEx(97);
    writeD(_result);
  }
}
package net.sf.l2j.gameserver.network.serverpackets;

public class EnchantResult extends L2GameServerPacket
{
  private static final String _S__81_ENCHANTRESULT = "[S] 81 EnchantResult";
  private int _unknown;

  public EnchantResult(int unknown)
  {
    _unknown = unknown;
  }

  protected final void writeImpl()
  {
    writeC(129);
    writeD(_unknown);
  }

  public String getType()
  {
    return "[S] 81 EnchantResult";
  }
}
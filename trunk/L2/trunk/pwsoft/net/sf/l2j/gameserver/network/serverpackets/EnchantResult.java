package net.sf.l2j.gameserver.network.serverpackets;

public class EnchantResult extends L2GameServerPacket
{
  private int _unknown;
  private boolean _fromServer = false;

  public EnchantResult(int unknown, boolean flag)
  {
    _unknown = unknown;
    _fromServer = flag;
  }

  protected final void writeImpl()
  {
    if (!_fromServer) {
      return;
    }
    writeC(129);
    writeD(_unknown);
  }
}
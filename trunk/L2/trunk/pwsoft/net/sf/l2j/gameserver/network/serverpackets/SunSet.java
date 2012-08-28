package net.sf.l2j.gameserver.network.serverpackets;

public class SunSet extends L2GameServerPacket
{
  protected final void writeImpl()
  {
    writeC(29);
  }

  public String getType()
  {
    return "S.SunSet";
  }
}
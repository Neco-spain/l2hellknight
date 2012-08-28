package net.sf.l2j.gameserver.network.serverpackets;

public class CharDeleteOk extends L2GameServerPacket
{
  private static final String _S__33_CHARDELETEOK = "[S] 23 CharDeleteOk";

  protected final void writeImpl()
  {
    writeC(35);
  }

  public String getType()
  {
    return "[S] 23 CharDeleteOk";
  }
}
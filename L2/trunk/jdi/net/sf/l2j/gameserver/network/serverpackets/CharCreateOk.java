package net.sf.l2j.gameserver.network.serverpackets;

public class CharCreateOk extends L2GameServerPacket
{
  private static final String _S__25_CHARCREATEOK = "[S] 19 CharCreateOk";

  protected final void writeImpl()
  {
    writeC(25);
    writeD(1);
  }

  public String getType()
  {
    return "[S] 19 CharCreateOk";
  }
}
package net.sf.l2j.gameserver.network.serverpackets;

public class ExOrcMove extends L2GameServerPacket
{
  private static final String _S__FE_44_EXORCMOVE = "[S] FE:44 ExOrcMove";

  protected void writeImpl()
  {
    writeC(254);
    writeH(68);
  }

  public String getType()
  {
    return "[S] FE:44 ExOrcMove";
  }
}
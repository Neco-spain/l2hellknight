package net.sf.l2j.gameserver.network.serverpackets;

public class ExRequestHackShield extends L2GameServerPacket
{
  private static final String _S__FE_48_EXREQUESTHACKSHIELD = "[S] FE:48 ExRequestHackShield";

  protected void writeImpl()
  {
    writeC(254);
    writeH(72);
  }

  public String getType()
  {
    return "[S] FE:48 ExRequestHackShield";
  }
}
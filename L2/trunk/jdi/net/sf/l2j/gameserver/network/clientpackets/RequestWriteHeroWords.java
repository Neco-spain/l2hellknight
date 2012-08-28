package net.sf.l2j.gameserver.network.clientpackets;

public final class RequestWriteHeroWords extends L2GameClientPacket
{
  private static final String _C__FE_0C_REQUESTWRITEHEROWORDS = "[C] D0:0C RequestWriteHeroWords";
  private String _heroWords;

  protected void readImpl()
  {
    _heroWords = readS();
  }

  protected void runImpl()
  {
  }

  public String getType()
  {
    return "[C] D0:0C RequestWriteHeroWords";
  }
}
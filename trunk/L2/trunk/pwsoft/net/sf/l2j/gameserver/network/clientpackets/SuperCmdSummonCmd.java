package net.sf.l2j.gameserver.network.clientpackets;

public class SuperCmdSummonCmd extends L2GameClientPacket
{
  private String _summonName;

  protected void readImpl()
  {
    _summonName = readS();
  }

  protected void runImpl()
  {
  }

  public String getType()
  {
    return "[C] SuperCmdSummonCmd";
  }
}
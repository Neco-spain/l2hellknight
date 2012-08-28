package net.sf.l2j.gameserver.network.clientpackets;

public class SuperCmdSummonCmd extends L2GameClientPacket
{
  private static final String _C__39_01_SUPERCMDSUMMONCMD = "[C] 39:01 SuperCmdSummonCmd";
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
    return "[C] 39:01 SuperCmdSummonCmd";
  }
}
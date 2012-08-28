package l2p.gameserver.clientpackets;

class SuperCmdSummonCmd extends L2GameClientPacket
{
  private String _summonName;

  protected void readImpl()
  {
    _summonName = readS();
  }

  protected void runImpl()
  {
  }
}
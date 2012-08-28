package net.sf.l2j.gameserver.network.serverpackets;

public class ExQuestInfo extends L2GameServerPacket
{
  private boolean _fromServer = false;

  public ExQuestInfo(boolean flag)
  {
    _fromServer = flag;
  }

  protected void writeImpl()
  {
    if (!_fromServer) {
      return;
    }
    writeC(254);
    writeH(25);
  }
}
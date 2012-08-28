package l2p.gameserver.serverpackets;

public class TutorialEnableClientEvent extends L2GameServerPacket
{
  private int _event = 0;

  public TutorialEnableClientEvent(int event)
  {
    _event = event;
  }

  protected final void writeImpl()
  {
    writeC(168);
    writeD(_event);
  }
}
package net.sf.l2j.gameserver.network.serverpackets;

public class TutorialEnableClientEvent extends L2GameServerPacket
{
  private static final String _S__a2_TUTORIALENABLECLIENTEVENT = "[S] a2 TutorialEnableClientEvent";
  private int _eventId = 0;

  public TutorialEnableClientEvent(int event)
  {
    _eventId = event;
  }

  protected void writeImpl()
  {
    writeC(162);
    writeD(_eventId);
  }

  public String getType()
  {
    return "[S] a2 TutorialEnableClientEvent";
  }
}
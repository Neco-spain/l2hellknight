package l2m.gameserver.network.serverpackets;

public class EventTrigger extends L2GameServerPacket
{
  private int _trapId;
  private boolean _active;

  public EventTrigger(int trapId, boolean active)
  {
    _trapId = trapId;
    _active = active;
  }

  protected final void writeImpl()
  {
    writeC(207);
    writeD(_trapId);
    writeC(_active ? 1 : 0);
  }
}
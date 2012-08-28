package net.sf.l2j.gameserver.network.serverpackets;

public class EventTrigger extends L2GameServerPacket
{
  private int id;
  private int on;

  public EventTrigger(int id, int on)
  {
    this.id = id;
    this.on = on;
  }

  protected final void writeImpl()
  {
    writeC(207);
    writeD(id);
    writeC(on);
  }
}
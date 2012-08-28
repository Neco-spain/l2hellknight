package l2m.gameserver.network.serverpackets;

public class GMHide extends L2GameServerPacket
{
  private final int obj_id;

  public GMHide(int id)
  {
    obj_id = id;
  }

  protected void writeImpl()
  {
    writeC(147);
    writeD(obj_id);
  }
}
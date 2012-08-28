package l2m.gameserver.network.serverpackets;

public class ExClosePartyRoom extends L2GameServerPacket
{
  public static L2GameServerPacket STATIC = new ExClosePartyRoom();

  protected void writeImpl()
  {
    writeEx(9);
  }
}
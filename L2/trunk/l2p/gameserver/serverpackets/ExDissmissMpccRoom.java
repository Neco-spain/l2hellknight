package l2p.gameserver.serverpackets;

public class ExDissmissMpccRoom extends L2GameServerPacket
{
  public static final L2GameServerPacket STATIC = new ExDissmissMpccRoom();

  protected void writeImpl()
  {
    writeEx(157);
  }
}
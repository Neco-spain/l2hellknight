package l2m.gameserver.network.serverpackets;

public class ClientAction extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeC(143);
  }
}
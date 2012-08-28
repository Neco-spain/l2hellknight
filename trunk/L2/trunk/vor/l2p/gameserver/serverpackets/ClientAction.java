package l2p.gameserver.serverpackets;

public class ClientAction extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeC(143);
  }
}
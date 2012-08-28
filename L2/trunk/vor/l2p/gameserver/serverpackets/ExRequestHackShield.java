package l2p.gameserver.serverpackets;

public class ExRequestHackShield extends L2GameServerPacket
{
  protected final void writeImpl()
  {
    writeEx(73);
  }
}
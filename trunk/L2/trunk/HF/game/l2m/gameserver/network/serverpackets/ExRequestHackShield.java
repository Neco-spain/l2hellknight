package l2m.gameserver.network.serverpackets;

public class ExRequestHackShield extends L2GameServerPacket
{
  protected final void writeImpl()
  {
    writeEx(73);
  }
}
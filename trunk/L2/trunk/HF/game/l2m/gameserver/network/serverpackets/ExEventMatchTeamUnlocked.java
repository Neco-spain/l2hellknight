package l2m.gameserver.network.serverpackets;

public class ExEventMatchTeamUnlocked extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(6);
  }
}
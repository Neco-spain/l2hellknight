package l2p.gameserver.serverpackets;

public class ExEventMatchTeamUnlocked extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(6);
  }
}
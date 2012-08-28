package l2m.gameserver.network.serverpackets;

public class ExShowPetitionHtml extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(177);
  }
}
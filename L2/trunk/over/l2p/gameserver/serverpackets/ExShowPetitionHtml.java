package l2p.gameserver.serverpackets;

public class ExShowPetitionHtml extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(177);
  }
}
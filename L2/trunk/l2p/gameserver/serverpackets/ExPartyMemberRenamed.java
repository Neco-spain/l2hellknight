package l2p.gameserver.serverpackets;

public class ExPartyMemberRenamed extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(166);
  }
}
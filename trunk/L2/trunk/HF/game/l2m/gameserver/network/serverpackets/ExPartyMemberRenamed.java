package l2m.gameserver.network.serverpackets;

public class ExPartyMemberRenamed extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(166);
  }
}
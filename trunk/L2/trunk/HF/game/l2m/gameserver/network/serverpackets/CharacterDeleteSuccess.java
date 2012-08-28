package l2m.gameserver.network.serverpackets;

public class CharacterDeleteSuccess extends L2GameServerPacket
{
  protected final void writeImpl()
  {
    writeC(29);
  }
}
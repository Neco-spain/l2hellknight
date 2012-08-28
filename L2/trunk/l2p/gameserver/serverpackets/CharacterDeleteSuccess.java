package l2p.gameserver.serverpackets;

public class CharacterDeleteSuccess extends L2GameServerPacket
{
  protected final void writeImpl()
  {
    writeC(29);
  }
}
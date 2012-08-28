package l2p.gameserver.serverpackets;

public class CharacterCreateSuccess extends L2GameServerPacket
{
  public static final L2GameServerPacket STATIC = new CharacterCreateSuccess();

  protected final void writeImpl()
  {
    writeC(15);
    writeD(1);
  }
}
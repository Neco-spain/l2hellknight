package l2m.gameserver.network.serverpackets;

public class CharacterCreateFail extends L2GameServerPacket
{
  public static final L2GameServerPacket REASON_TOO_MANY_CHARACTERS = new CharacterCreateFail(1);
  public static final L2GameServerPacket REASON_NAME_ALREADY_EXISTS = new CharacterCreateFail(2);
  public static final L2GameServerPacket REASON_16_ENG_CHARS = new CharacterCreateFail(3);
  private int _error;

  private CharacterCreateFail(int errorCode)
  {
    _error = errorCode;
  }

  protected final void writeImpl()
  {
    writeC(16);
    writeD(_error);
  }
}
package l2p.gameserver.serverpackets;

import l2p.gameserver.utils.Location;

public class PlaySound extends L2GameServerPacket
{
  public static final L2GameServerPacket SIEGE_VICTORY = new PlaySound("Siege_Victory");
  public static final L2GameServerPacket B04_S01 = new PlaySound("B04_S01");
  public static final L2GameServerPacket HB01 = new PlaySound(Type.MUSIC, "HB01", 0, 0, 0, 0, 0);
  private Type _type;
  private String _soundFile;
  private int _hasCenterObject;
  private int _objectId;
  private int _x;
  private int _y;
  private int _z;

  public PlaySound(String soundFile)
  {
    this(Type.SOUND, soundFile, 0, 0, 0, 0, 0);
  }

  public PlaySound(Type type, String soundFile, int c, int objectId, Location loc)
  {
    this(type, soundFile, c, objectId, loc == null ? 0 : loc.x, loc == null ? 0 : loc.y, loc == null ? 0 : loc.z);
  }

  public PlaySound(Type type, String soundFile, int c, int objectId, int x, int y, int z)
  {
    _type = type;
    _soundFile = soundFile;
    _hasCenterObject = c;
    _objectId = objectId;
    _x = x;
    _y = y;
    _z = z;
  }

  protected final void writeImpl()
  {
    writeC(158);

    writeD(_type.ordinal());
    writeS(_soundFile);
    writeD(_hasCenterObject);
    writeD(_objectId);
    writeD(_x);
    writeD(_y);
    writeD(_z);
  }

  public static enum Type
  {
    SOUND, 
    MUSIC, 
    VOICE;
  }
}
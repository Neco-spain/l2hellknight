package l2m.gameserver.serverpackets;

public class SpecialCamera extends L2GameServerPacket
{
  private int _id;
  private int _dist;
  private int _yaw;
  private int _pitch;
  private int _time;
  private int _duration;
  private final int _turn;
  private final int _rise;
  private final int _widescreen;
  private final int _unknown;

  public SpecialCamera(int id, int dist, int yaw, int pitch, int time, int duration)
  {
    _id = id;
    _dist = dist;
    _yaw = yaw;
    _pitch = pitch;
    _time = time;
    _duration = duration;
    _turn = 0;
    _rise = 0;
    _widescreen = 0;
    _unknown = 0;
  }

  public SpecialCamera(int id, int dist, int yaw, int pitch, int time, int duration, int turn, int rise, int widescreen, int unk)
  {
    _id = id;
    _dist = dist;
    _yaw = yaw;
    _pitch = pitch;
    _time = time;
    _duration = duration;
    _turn = turn;
    _rise = rise;
    _widescreen = widescreen;
    _unknown = unk;
  }

  protected final void writeImpl()
  {
    writeC(214);

    writeD(_id);
    writeD(_dist);
    writeD(_yaw);
    writeD(_pitch);
    writeD(_time);
    writeD(_duration);
    writeD(_turn);
    writeD(_rise);
    writeD(_widescreen);
    writeD(_unknown);
  }
}
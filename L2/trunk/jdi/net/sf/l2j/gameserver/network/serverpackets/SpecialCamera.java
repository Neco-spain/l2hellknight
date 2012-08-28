package net.sf.l2j.gameserver.network.serverpackets;

public class SpecialCamera extends L2GameServerPacket
{
  private static final String _S__C7_SPECIALCAMERA = "[S] C7 SpecialCamera";
  private int _id;
  private int _dist;
  private int _yaw;
  private int _pitch;
  private int _time;
  private int _duration;

  public SpecialCamera(int id, int dist, int yaw, int pitch, int time, int duration)
  {
    _id = id;
    _dist = dist;
    _yaw = yaw;
    _pitch = pitch;
    _time = time;
    _duration = duration;
  }

  public void writeImpl()
  {
    writeC(199);
    writeD(_id);
    writeD(_dist);
    writeD(_yaw);
    writeD(_pitch);
    writeD(_time);
    writeD(_duration);
  }

  public String getType()
  {
    return "[S] C7 SpecialCamera";
  }
}
package l2m.loginserver.serverpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GGAuth extends L2LoginServerPacket
{
  static Logger _log = LoggerFactory.getLogger(GGAuth.class);
  public static int SKIP_GG_AUTH_REQUEST = 11;
  private int _response;

  public GGAuth(int response)
  {
    _response = response;
  }

  protected void writeImpl()
  {
    writeC(11);
    writeD(_response);
    writeD(0);
    writeD(0);
    writeD(0);
    writeD(0);
  }
}
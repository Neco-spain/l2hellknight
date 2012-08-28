package net.sf.l2j.loginserver.serverpackets;

import java.util.logging.Logger;
import net.sf.l2j.Config;

public final class GGAuth extends L2LoginServerPacket
{
  static final Logger _log = Logger.getLogger(GGAuth.class.getName());
  public static final int SKIP_GG_AUTH_REQUEST = 11;
  private int _response;

  public GGAuth(int response)
  {
    _response = response;
    if (Config.DEBUG)
    {
      _log.warning("Reason Hex: " + Integer.toHexString(response));
    }
  }

  protected void write()
  {
    writeC(11);
    writeD(_response);
    writeD(0);
    writeD(0);
    writeD(0);
    writeD(0);
  }
}
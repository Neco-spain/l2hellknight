package l2m.gameserver.loginservercon;

import java.nio.ByteBuffer;
import l2m.gameserver.loginservercon.lspackets.AuthResponse;
import l2m.gameserver.loginservercon.lspackets.GetAccountInfo;
import l2m.gameserver.loginservercon.lspackets.KickPlayer;
import l2m.gameserver.loginservercon.lspackets.LoginServerFail;
import l2m.gameserver.loginservercon.lspackets.PingRequest;
import l2m.gameserver.loginservercon.lspackets.PlayerAuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketHandler
{
  private static final Logger _log = LoggerFactory.getLogger(PacketHandler.class);

  public static ReceivablePacket handlePacket(ByteBuffer buf)
  {
    ReceivablePacket packet = null;

    int id = buf.get() & 0xFF;

    switch (id)
    {
    case 0:
      packet = new AuthResponse();
      break;
    case 1:
      packet = new LoginServerFail();
      break;
    case 2:
      packet = new PlayerAuthResponse();
      break;
    case 3:
      packet = new KickPlayer();
      break;
    case 4:
      packet = new GetAccountInfo();
      break;
    case 255:
      packet = new PingRequest();
      break;
    default:
      _log.error("Received unknown packet: " + Integer.toHexString(id));
    }

    return packet;
  }
}
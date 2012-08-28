package l2m.loginserver.gameservercon;

import java.nio.ByteBuffer;
import l2m.loginserver.gameservercon.gspackets.AuthRequest;
import l2m.loginserver.gameservercon.gspackets.BonusRequest;
import l2m.loginserver.gameservercon.gspackets.ChangeAccessLevel;
import l2m.loginserver.gameservercon.gspackets.ChangePassword;
import l2m.loginserver.gameservercon.gspackets.GetAccountPoint;
import l2m.loginserver.gameservercon.gspackets.OnlineStatus;
import l2m.loginserver.gameservercon.gspackets.PingResponse;
import l2m.loginserver.gameservercon.gspackets.PlayerAuthRequest;
import l2m.loginserver.gameservercon.gspackets.PlayerInGame;
import l2m.loginserver.gameservercon.gspackets.PlayerLogout;
import l2m.loginserver.gameservercon.gspackets.RemoveAccountPoint;
import l2m.loginserver.gameservercon.gspackets.SetAccountInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketHandler
{
  private static Logger _log = LoggerFactory.getLogger(PacketHandler.class);

  public static ReceivablePacket handlePacket(GameServer gs, ByteBuffer buf)
  {
    ReceivablePacket packet = null;

    int id = buf.get() & 0xFF;

    if (!gs.isAuthed())
      switch (id)
      {
      case 0:
        packet = new AuthRequest();
        break;
      default:
        _log.error("Received unknown packet: " + Integer.toHexString(id)); break;
      }
    else {
      switch (id)
      {
      case 1:
        packet = new OnlineStatus();
        break;
      case 2:
        packet = new PlayerAuthRequest();
        break;
      case 3:
        packet = new PlayerInGame();
        break;
      case 4:
        packet = new PlayerLogout();
        break;
      case 5:
        packet = new SetAccountInfo();
        break;
      case 8:
        packet = new ChangePassword();
        break;
      case 16:
        packet = new BonusRequest();
        break;
      case 11:
        packet = new ChangeAccessLevel();
        break;
      case 12:
        packet = new GetAccountPoint();
        break;
      case 13:
        packet = new RemoveAccountPoint();
        break;
      case 255:
        packet = new PingResponse();
        break;
      default:
        _log.error("Received unknown packet: " + Integer.toHexString(id));
      }
    }
    return packet;
  }
}
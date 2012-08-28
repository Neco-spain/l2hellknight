package l2m.loginserver.gameservercon.gspackets;

import l2m.loginserver.GameServerManager;
import l2m.loginserver.gameservercon.GameServer;
import l2m.loginserver.gameservercon.GameServerConnection;
import l2m.loginserver.gameservercon.ReceivablePacket;
import l2m.loginserver.gameservercon.lspackets.AuthResponse;
import l2m.loginserver.gameservercon.lspackets.LoginServerFail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthRequest extends ReceivablePacket
{
  private static final Logger _log = LoggerFactory.getLogger(AuthRequest.class);
  private int _protocolVersion;
  private int requestId;
  private boolean acceptAlternateID;
  private String externalIp;
  private String internalIp;
  private int maxOnline;
  private int _serverType;
  private int _ageLimit;
  private boolean _gmOnly;
  private boolean _brackets;
  private boolean _pvp;
  private int[] ports;

  protected void readImpl()
  {
    _protocolVersion = readD();
    requestId = readC();
    acceptAlternateID = (readC() == 1);
    _serverType = readD();
    _ageLimit = readD();
    _gmOnly = (readC() == 1);
    _brackets = (readC() == 1);
    _pvp = (readC() == 1);
    externalIp = readS();
    internalIp = readS();
    ports = new int[readH()];
    for (int i = 0; i < ports.length; i++)
      ports[i] = readH();
    maxOnline = readD();
  }

  protected void runImpl()
  {
    _log.info("Trying to register gameserver: " + requestId + " [" + getGameServer().getConnection().getIpAddress() + "]");

    int failReason = 0;

    GameServer gs = getGameServer();
    if (GameServerManager.getInstance().registerGameServer(requestId, gs))
    {
      gs.setPorts(ports);
      gs.setExternalHost(externalIp);
      gs.setInternalHost(internalIp);
      gs.setMaxPlayers(maxOnline);
      gs.setPvp(_pvp);
      gs.setServerType(_serverType);
      gs.setShowingBrackets(_brackets);
      gs.setGmOnly(_gmOnly);
      gs.setAgeLimit(_ageLimit);
      gs.setProtocol(_protocolVersion);
      gs.setAuthed(true);
      gs.getConnection().startPingTask();
    }
    else if (acceptAlternateID)
    {
      if (GameServerManager.getInstance().registerGameServer(gs = getGameServer()))
      {
        gs.setPorts(ports);
        gs.setExternalHost(externalIp);
        gs.setInternalHost(internalIp);
        gs.setMaxPlayers(maxOnline);
        gs.setPvp(_pvp);
        gs.setServerType(_serverType);
        gs.setShowingBrackets(_brackets);
        gs.setGmOnly(_gmOnly);
        gs.setAgeLimit(_ageLimit);
        gs.setProtocol(_protocolVersion);
        gs.setAuthed(true);
        gs.getConnection().startPingTask();
      }
      else {
        failReason = 5;
      }
    } else {
      failReason = 4;
    }
    if (failReason != 0)
    {
      _log.info("Gameserver registration failed.");
      sendPacket(new LoginServerFail(failReason));
      return;
    }

    _log.info("Gameserver registration successful.");
    sendPacket(new AuthResponse(gs));
  }
}
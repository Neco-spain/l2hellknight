package net.sf.l2j.loginserver.gameserverpackets;

import java.util.logging.Logger;
import net.sf.l2j.loginserver.clientpackets.ClientBasePacket;

public class GameServerAuth extends ClientBasePacket
{
  protected static Logger _log = Logger.getLogger(GameServerAuth.class.getName());
  private byte[] _hexId;
  private int _desiredId;
  private boolean _hostReserved;
  private boolean _acceptAlternativeId;
  private int _maxPlayers;
  private int _port;
  private String _externalHost;
  private String _internalHost;

  public GameServerAuth(byte[] decrypt)
  {
    super(decrypt);
    _desiredId = readC();
    _acceptAlternativeId = (readC() != 0);
    _hostReserved = (readC() != 0);
    _externalHost = readS();
    _internalHost = readS();
    _port = readH();
    _maxPlayers = readD();
    int size = readD();
    _hexId = readB(size);
  }

  public byte[] getHexID()
  {
    return _hexId;
  }

  public boolean getHostReserved()
  {
    return _hostReserved;
  }

  public int getDesiredID()
  {
    return _desiredId;
  }

  public boolean acceptAlternateID()
  {
    return _acceptAlternativeId;
  }

  public int getMaxPlayers()
  {
    return _maxPlayers;
  }

  public String getExternalHost()
  {
    return _externalHost;
  }

  public String getInternalHost()
  {
    return _internalHost;
  }

  public int getPort()
  {
    return _port;
  }
}
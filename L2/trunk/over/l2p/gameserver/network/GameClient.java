package l2p.gameserver.network;

import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import l2p.commons.dbutils.DbUtils;
import l2p.commons.net.nio.impl.MMOClient;
import l2p.commons.net.nio.impl.MMOConnection;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.dao.CharacterDAO;
import l2p.gameserver.database.DatabaseFactory;
import l2p.gameserver.loginservercon.LoginServerCommunication;
import l2p.gameserver.loginservercon.SessionKey;
import l2p.gameserver.loginservercon.gspackets.PlayerLogout;
import l2p.gameserver.model.CharSelectInfoPackage;
import l2p.gameserver.model.GameObjectsStorage;
import l2p.gameserver.model.Player;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GameClient extends MMOClient<MMOConnection<GameClient>>
{
  private static final Logger _log = LoggerFactory.getLogger(GameClient.class);
  private static final String NO_IP = "?.?.?.?";
  public GameCrypt _crypt = null;
  public GameClientState _state;
  private String _login;
  private double _bonus = 1.0D;
  private int _bonusExpire;
  private Player _activeChar;
  private SessionKey _sessionKey;
  private String _ip = "?.?.?.?";
  private int revision = 0;

  private List<Integer> _charSlotMapping = new ArrayList();

  private int _failedPackets = 0;
  private int _unknownPackets = 0;

  public GameClient(MMOConnection<GameClient> con)
  {
    super(con);

    _state = GameClientState.CONNECTED;
    _crypt = new GameCrypt();
    _ip = con.getSocket().getInetAddress().getHostAddress();
  }

  protected void onDisconnection()
  {
    setState(GameClientState.DISCONNECTED);
    Player player = getActiveChar();
    setActiveChar(null);

    if (player != null)
    {
      player.setNetConnection(null);
      player.scheduleDelete();
    }

    if (getSessionKey() != null)
    {
      if (isAuthed())
      {
        LoginServerCommunication.getInstance().removeAuthedClient(getLogin());
        LoginServerCommunication.getInstance().sendPacket(new PlayerLogout(getLogin()));
      }
      else
      {
        LoginServerCommunication.getInstance().removeWaitingClient(getLogin());
      }
    }
  }

  protected void onForcedDisconnection()
  {
  }

  public void markRestoredChar(int charslot)
    throws Exception
  {
    int objid = getObjectIdForSlot(charslot);
    if (objid < 0) {
      return;
    }
    if ((_activeChar != null) && (_activeChar.getObjectId() == objid)) {
      _activeChar.setDeleteTimer(0);
    }
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE characters SET deletetime=0 WHERE obj_id=?");
      statement.setInt(1, objid);
      statement.execute();
    }
    catch (Exception e)
    {
      _log.error("", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  public void markToDeleteChar(int charslot) throws Exception
  {
    int objid = getObjectIdForSlot(charslot);
    if (objid < 0) {
      return;
    }
    if ((_activeChar != null) && (_activeChar.getObjectId() == objid)) {
      _activeChar.setDeleteTimer((int)(System.currentTimeMillis() / 1000L));
    }
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE characters SET deletetime=? WHERE obj_id=?");
      statement.setLong(1, (int)(System.currentTimeMillis() / 1000L));
      statement.setInt(2, objid);
      statement.execute();
    }
    catch (Exception e)
    {
      _log.error("data error on update deletime char:", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  public void deleteChar(int charslot)
    throws Exception
  {
    if (_activeChar != null) {
      return;
    }
    int objid = getObjectIdForSlot(charslot);
    if (objid == -1) {
      return;
    }
    CharacterDAO.getInstance().deleteCharByObjId(objid);
  }

  public Player loadCharFromDisk(int charslot)
  {
    int objectId = getObjectIdForSlot(charslot);
    if (objectId == -1) {
      return null;
    }
    Player character = null;
    Player oldPlayer = GameObjectsStorage.getPlayer(objectId);

    if (oldPlayer != null) {
      if ((oldPlayer.isInOfflineMode()) || (oldPlayer.isLogoutStarted()))
      {
        oldPlayer.kick();
        return null;
      }

      oldPlayer.sendPacket(Msg.ANOTHER_PERSON_HAS_LOGGED_IN_WITH_THE_SAME_ACCOUNT);

      GameClient oldClient = oldPlayer.getNetConnection();
      if (oldClient != null)
      {
        oldClient.setActiveChar(null);
        oldClient.closeNow(false);
      }
      oldPlayer.setNetConnection(this);
      character = oldPlayer;
    }

    if (character == null) {
      character = Player.restore(objectId);
    }
    if (character != null)
      setActiveChar(character);
    else {
      _log.warn(new StringBuilder().append("could not restore obj_id: ").append(objectId).append(" in slot:").append(charslot).toString());
    }
    return character;
  }

  public int getObjectIdForSlot(int charslot)
  {
    if ((charslot < 0) || (charslot >= _charSlotMapping.size()))
    {
      _log.warn(new StringBuilder().append(getLogin()).append(" tried to modify Character in slot ").append(charslot).append(" but no characters exits at that slot.").toString());
      return -1;
    }
    return ((Integer)_charSlotMapping.get(charslot)).intValue();
  }

  public Player getActiveChar()
  {
    return _activeChar;
  }

  public SessionKey getSessionKey()
  {
    return _sessionKey;
  }

  public String getLogin()
  {
    return _login;
  }

  public void setLoginName(String loginName)
  {
    _login = loginName;
  }

  public void setActiveChar(Player player)
  {
    _activeChar = player;
    if (player != null)
      player.setNetConnection(this);
  }

  public void setSessionId(SessionKey sessionKey)
  {
    _sessionKey = sessionKey;
  }

  public void setCharSelection(CharSelectInfoPackage[] chars)
  {
    _charSlotMapping.clear();

    for (CharSelectInfoPackage element : chars)
    {
      int objectId = element.getObjectId();
      _charSlotMapping.add(Integer.valueOf(objectId));
    }
  }

  public void setCharSelection(int c)
  {
    _charSlotMapping.clear();
    _charSlotMapping.add(Integer.valueOf(c));
  }

  public int getRevision()
  {
    return revision;
  }

  public void setRevision(int revision)
  {
    this.revision = revision;
  }

  public boolean encrypt(ByteBuffer buf, int size)
  {
    _crypt.encrypt(buf.array(), buf.position(), size);
    buf.position(buf.position() + size);
    return true;
  }

  public boolean decrypt(ByteBuffer buf, int size)
  {
    boolean ret = _crypt.decrypt(buf.array(), buf.position(), size);

    return ret;
  }

  public void sendPacket(L2GameServerPacket gsp)
  {
    if (isConnected())
      getConnection().sendPacket(gsp);
  }

  public void sendPacket(L2GameServerPacket[] gsp)
  {
    if (isConnected())
      getConnection().sendPacket(gsp);
  }

  public void sendPackets(List<L2GameServerPacket> gsp)
  {
    if (isConnected())
      getConnection().sendPackets(gsp);
  }

  public void close(L2GameServerPacket gsp)
  {
    if (isConnected())
      getConnection().close(gsp);
  }

  public String getIpAddr()
  {
    return _ip;
  }

  public byte[] enableCrypt()
  {
    byte[] key = BlowFishKeygen.getRandomKey();
    _crypt.setKey(key);
    return key;
  }

  public double getBonus()
  {
    return _bonus;
  }

  public int getBonusExpire()
  {
    return _bonusExpire;
  }

  public void setBonus(double bonus)
  {
    _bonus = bonus;
  }

  public void setBonusExpire(int bonusExpire)
  {
    _bonusExpire = bonusExpire;
  }

  public GameClientState getState()
  {
    return _state;
  }

  public void setState(GameClientState state)
  {
    _state = state;
  }

  public void onPacketReadFail()
  {
    if (_failedPackets++ >= 10)
    {
      _log.warn(new StringBuilder().append("Too many client packet fails, connection closed : ").append(this).toString());
      closeNow(true);
    }
  }

  public void onUnknownPacket()
  {
    if (_unknownPackets++ >= 10)
    {
      _log.warn(new StringBuilder().append("Too many client unknown packets, connection closed : ").append(this).toString());
      closeNow(true);
    }
  }

  public String toString()
  {
    return new StringBuilder().append(_state).append(" IP: ").append(getIpAddr()).append(_login == null ? "" : new StringBuilder().append(" Account: ").append(_login).toString()).append(_activeChar == null ? "" : new StringBuilder().append(" Player : ").append(_activeChar).toString()).toString();
  }

  public static enum GameClientState
  {
    CONNECTED, 
    AUTHED, 
    IN_GAME, 
    DISCONNECTED;
  }
}
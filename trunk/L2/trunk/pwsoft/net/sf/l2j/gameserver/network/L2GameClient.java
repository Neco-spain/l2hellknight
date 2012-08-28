package net.sf.l2j.gameserver.network;

import com.lameguard.session.LameClientV195;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.LoginServerThread;
import net.sf.l2j.gameserver.LoginServerThread.SessionKey;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.CharSelectInfoPackage;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.L2Event;
import net.sf.l2j.gameserver.model.entity.olympiad.Olympiad;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.network.serverpackets.ServerClose;
import net.sf.l2j.gameserver.network.serverpackets.ServerCloseSocket;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;
import net.sf.l2j.gameserver.util.protection.GameGuard;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import net.sf.l2j.util.EventData;
import net.sf.l2j.util.Log;
import net.sf.l2j.util.TimeLogger;
import org.mmocore.network.ISocket;
import org.mmocore.network.MMOClient;
import org.mmocore.network.MMOConnection;

public final class L2GameClient extends MMOClient<MMOConnection<L2GameClient>>
  implements LameClientV195
{
  protected static final Logger _log = Logger.getLogger(L2GameClient.class.getName());
  public GameClientState state;
  public String accountName;
  public LoginServerThread.SessionKey sessionId;
  public L2PcInstance activeChar;
  private ReentrantLock _activeCharLock = new ReentrantLock();
  private boolean _isAuthedGG;
  private long _connectionStartTime;
  private List<Integer> _charSlotMapping = new FastList();
  protected ScheduledFuture<?> _autoSaveInDB;
  protected ScheduledFuture<?> _cleanupTask = null;
  public GameCrypt crypt;
  private int _upTryes = 0;
  private long _upLastConnection = 0L;

  private String _lastSendedPacket = "";

  private String _hwid = "none";
  private String _myhwid = "none";
  private boolean _protected;
  private int _patchVersion;
  private int _instCount;
  private boolean _hasEmail = false;

  public L2GameClient(MMOConnection<L2GameClient> con)
  {
    super(con);
    state = GameClientState.CONNECTED;
    _connectionStartTime = System.currentTimeMillis();
    crypt = new GameCrypt();

    _autoSaveInDB = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AutoSaveTask(), 300000L, 900000L);
  }

  public L2GameClient(MMOConnection<L2GameClient> con, boolean offline) {
    super(con);
    state = GameClientState.IN_GAME;
  }

  public byte[] enableCrypt() {
    byte[] key = BlowFishKeygen.getRandomKey();
    crypt.setKey(key);
    return key;
  }

  public GameClientState getState() {
    return state;
  }

  public void setState(GameClientState pState) {
    state = pState;
  }

  public long getConnectionStartTime() {
    return _connectionStartTime;
  }

  public boolean decrypt(ByteBuffer buf, int size)
  {
    crypt.decrypt(buf.array(), buf.position(), size);
    return true;
  }

  public boolean encrypt(ByteBuffer buf, int size)
  {
    crypt.encrypt(buf.array(), buf.position(), size);
    buf.position(buf.position() + size);
    return true;
  }

  public L2PcInstance getActiveChar() {
    return activeChar;
  }

  public void setActiveChar(L2PcInstance pActiveChar) {
    activeChar = pActiveChar;
    if (activeChar != null)
      L2World.getInstance().storeObject(getActiveChar());
  }

  public ReentrantLock getActiveCharLock()
  {
    return _activeCharLock;
  }

  public void setGameGuardOk(boolean val) {
    _isAuthedGG = val;
  }

  public void setAccountName(String pAccountName) {
    accountName = pAccountName;
  }

  public String getAccountName() {
    return accountName;
  }

  public void setSessionId(LoginServerThread.SessionKey sk) {
    sessionId = sk;
  }

  public LoginServerThread.SessionKey getSessionId() {
    return sessionId;
  }

  public void sendPacket(L2GameServerPacket gsp) {
    if (getConnection() == null) {
      return;
    }

    if (gsp == null) {
      return;
    }
    getConnection().sendPacket(gsp);
    gsp.runImpl();
  }

  public String getIpAddr() {
    try {
      return getConnection().getSocket().getInetAddress().getHostAddress();
    } catch (NullPointerException e) {
    }
    return "Disconnected";
  }

  public L2PcInstance markToDeleteChar(int charslot)
    throws Exception
  {
    int objid = getObjectIdForSlot(charslot);
    if (objid < 0) {
      return null;
    }

    L2PcInstance character = L2PcInstance.load(objid);
    if (character.getClanId() != 0) {
      return character;
    }

    Connect con = null;
    PreparedStatement statement = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE characters SET deletetime=? WHERE obj_id=?");
      statement.setLong(1, System.currentTimeMillis() + Config.DELETE_DAYS * 86400000L);
      statement.setInt(2, objid);
      statement.execute();
    } catch (Exception e) {
      _log.warning(new StringBuilder().append("Data error on update delete time of char: ").append(e).toString());
    } finally {
      Close.CS(con, statement);
    }
    return null;
  }

  public L2PcInstance deleteChar(int charslot)
    throws Exception
  {
    int objid = getObjectIdForSlot(charslot);
    if (objid < 0) {
      return null;
    }

    L2PcInstance character = L2PcInstance.load(objid);
    if (character.getClanId() != 0) {
      return character;
    }

    deleteCharByObjId(objid);
    return null;
  }

  public void markRestoredChar(int charslot)
    throws Exception
  {
    int objid = getObjectIdForSlot(charslot);

    if (objid < 0) {
      return;
    }

    Connect con = null;
    PreparedStatement statement = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE characters SET deletetime=0 WHERE obj_id=?");
      statement.setInt(1, objid);
      statement.execute();
    } catch (Exception e) {
      _log.severe(new StringBuilder().append("Data error on restoring char: ").append(e).toString());
    } finally {
      Close.CS(con, statement);
    }
  }

  public static void deleteCharByObjId(int objid) {
    if (objid < 0) {
      return;
    }

    Connect con = null;
    PreparedStatement statement = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      statement = con.prepareStatement("DELETE FROM character_friends WHERE char_id=? OR friend_id=?");
      statement.setInt(1, objid);
      statement.setInt(2, objid);
      statement.execute();
      Close.S(statement);

      statement = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=?");
      statement.setInt(1, objid);
      statement.execute();
      Close.S(statement);

      statement = con.prepareStatement("DELETE FROM character_macroses WHERE char_obj_id=?");
      statement.setInt(1, objid);
      statement.execute();
      Close.S(statement);

      statement = con.prepareStatement("DELETE FROM character_quests WHERE char_id=?");
      statement.setInt(1, objid);
      statement.execute();
      Close.S(statement);

      statement = con.prepareStatement("DELETE FROM character_recipebook WHERE char_id=?");
      statement.setInt(1, objid);
      statement.execute();
      Close.S(statement);

      statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE char_obj_id=?");
      statement.setInt(1, objid);
      statement.execute();
      Close.S(statement);

      statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=?");
      statement.setInt(1, objid);
      statement.execute();
      Close.S(statement);

      statement = con.prepareStatement("DELETE FROM character_buffs WHERE char_obj_id=?");
      statement.setInt(1, objid);
      statement.execute();
      Close.S(statement);

      statement = con.prepareStatement("DELETE FROM character_subclasses WHERE char_obj_id=?");
      statement.setInt(1, objid);
      statement.execute();
      Close.S(statement);

      statement = con.prepareStatement("DELETE FROM heroes WHERE char_id=?");
      statement.setInt(1, objid);
      statement.execute();
      Close.S(statement);

      statement = con.prepareStatement("DELETE FROM olympiad_nobles WHERE char_id=?");
      statement.setInt(1, objid);
      statement.execute();
      Close.S(statement);

      statement = con.prepareStatement("DELETE FROM seven_signs WHERE char_obj_id=?");
      statement.setInt(1, objid);
      statement.execute();
      Close.S(statement);

      statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id IN (SELECT object_id FROM items WHERE items.owner_id=?)");
      statement.setInt(1, objid);
      statement.execute();
      Close.S(statement);

      statement = con.prepareStatement("DELETE FROM items WHERE owner_id=?");
      statement.setInt(1, objid);
      statement.execute();
      Close.S(statement);

      statement = con.prepareStatement("DELETE FROM merchant_lease WHERE player_id=?");
      statement.setInt(1, objid);
      statement.execute();
      Close.S(statement);

      statement = con.prepareStatement("DELETE FROM character_settings WHERE char_obj_id=?");
      statement.setInt(1, objid);
      statement.execute();
      Close.S(statement);

      statement = con.prepareStatement("DELETE FROM characters WHERE obj_Id=?");
      statement.setInt(1, objid);
      statement.execute();
      Close.S(statement);
    } catch (Exception e) {
      _log.warning(new StringBuilder().append("Data error on deleting char: ").append(e).toString());
    } finally {
      Close.CS(con, statement);
    }
  }

  public L2PcInstance loadCharFromDisk(int charslot) {
    Integer objectId = Integer.valueOf(getObjectIdForSlot(charslot));
    if (objectId.intValue() == -1) {
      return null;
    }

    L2Object object = L2World.getInstance().findObject(objectId.intValue());
    if (object != null) {
      object.kick();
    }

    L2PcInstance character = L2PcInstance.load(getObjectIdForSlot(charslot));

    if (character != null)
    {
      character.setRunning();
      character.standUp();

      character.refreshOverloaded();
      character.refreshExpertisePenalty();
      character.sendPacket(new UserInfo(character));
      character.broadcastKarma();
      character.setOnlineStatus(true);
    } else {
      _log.severe(new StringBuilder().append("could not restore in slot: ").append(charslot).toString());
    }

    return character;
  }

  public void setCharSelection(CharSelectInfoPackage[] chars)
  {
    _charSlotMapping.clear();

    for (int i = 0; i < chars.length; i++) {
      int objectId = chars[i].getObjectId();
      _charSlotMapping.add(Integer.valueOf(objectId));
    }
  }

  public void setCharSelection(int c) {
    _charSlotMapping.clear();
    _charSlotMapping.add(Integer.valueOf(c));
  }

  public void close(L2GameServerPacket gsp) {
    getConnection().close(gsp);
  }

  private int getObjectIdForSlot(int charslot)
  {
    if ((charslot < 0) || (charslot >= _charSlotMapping.size())) {
      _log.warning(new StringBuilder().append(toString()).append(" tried to delete Character in slot ").append(charslot).append(" but no characters exits at that slot.").toString());
      return -1;
    }
    Integer objectId = (Integer)_charSlotMapping.get(charslot);
    return objectId.intValue();
  }

  protected void onForcedDisconnection()
  {
  }

  protected void onDisconnection()
  {
    if ((getAccountName() == null) || (getAccountName().equals("")) || ((state != GameClientState.IN_GAME) && (state != GameClientState.AUTHED))) {
      return;
    }
    try
    {
      _autoSaveInDB.cancel(true);

      L2PcInstance player = getActiveChar();

      if (player != null)
      {
        if (player.getActiveTradeList() != null)
        {
          player.cancelActiveTrade();
          if (player.getTransactionRequester() != null) {
            player.getTransactionRequester().setTransactionRequester(null);
          }
          player.setTransactionRequester(null);
        }

        if (player.isInOfflineMode())
        {
          return;
        }

        if (player.atEvent) {
          EventData data = new EventData(player.eventX, player.eventY, player.eventZ, player.eventkarma, player.eventpvpkills, player.eventpkkills, player.eventTitle, player.kills, player.eventSitForced);
          L2Event.connectionLossData.put(player.getName(), data);
        }

        if (player.isCastingNow()) {
          player.abortCast();
        }

        if ((Olympiad.isRegisteredInComp(player)) || (player.isInOlympiadMode()) || (player.getOlympiadGameId() > -1)) {
          Olympiad.logoutPlayer(player);
        }

        if (player.isFlying()) {
          player.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
        }

        if (player.getPet() != null) {
          player.getPet().unSummon(player);
        }

        saveCharToDisk(player);

        player.deleteMe();

        if (player.getClient() != null) {
          player.closeNetConnection();
          player.setClient(null);
        }

        player.gc();
        player = null;
      }
      setActiveChar(null);
    } catch (Exception e1) {
      _log.log(Level.WARNING, "error while disconnecting client", e1);
    } finally {
      LoginServerThread.getInstance().sendLogout(getAccountName());
    }

    state = GameClientState.DISCONNECTED;
    closeSession();
  }

  public static void saveCharToDisk(L2PcInstance cha)
  {
    try
    {
      cha.getInventory().updateDatabase(true);
      cha.store();
    } catch (Exception e) {
      _log.severe(new StringBuilder().append("Error saving player character ").append(cha.getName()).append(": ").append(e).toString());
    }
  }

  public String toString()
  {
    try
    {
      InetAddress address = getConnection().getSocket().getInetAddress();
      switch (1.$SwitchMap$net$sf$l2j$gameserver$network$L2GameClient$GameClientState[getState().ordinal()]) {
      case 1:
        return new StringBuilder().append("[IP: ").append(address == null ? "disconnected" : address.getHostAddress()).append("]").toString();
      case 2:
        return new StringBuilder().append("[Account: ").append(getAccountName()).append(" - IP: ").append(address == null ? "disconnected" : address.getHostAddress()).append("]").toString();
      case 3:
        return new StringBuilder().append("[Character: ").append(getActiveChar() == null ? "disconnected" : getActiveChar().getName()).append(" - Account: ").append(getAccountName()).append(" - IP: ").append(address == null ? "disconnected" : address.getHostAddress()).append("]").toString();
      case 4:
        return "[Disconnected]";
      }
      throw new IllegalStateException("Missing state on switch");
    }
    catch (NullPointerException e) {
      e.printStackTrace();
    }return "[Character read failed due to disconnect]";
  }

  public void addUPTryes()
  {
    if ((_upLastConnection != 0L) && (System.currentTimeMillis() - _upLastConnection > 700L)) {
      _upTryes = 0;
    }
    _upTryes += 1;
    _upLastConnection = System.currentTimeMillis();
  }

  public int getUPTryes() {
    if ((_upLastConnection != 0L) && (System.currentTimeMillis() - _upLastConnection > 700L)) {
      _upTryes = 0;
      _upLastConnection = System.currentTimeMillis();
    }
    return _upTryes;
  }

  public void setLastSendedPacket(String packet)
  {
    _lastSendedPacket = packet;
  }

  public String getLastSendedPacket() {
    return _lastSendedPacket;
  }

  public void close() {
    L2PcInstance player = getActiveChar();
    if (player != null) {
      player.sendPacket(Static.YOU_HAVE_BEEN_DISCONNECTED);
      player.sendPacket(new ServerClose());
      player.sendPacket(new ServerCloseSocket());
    }
    closeNow();
  }

  public void kick(String account) {
    L2PcInstance player = getActiveChar();
    if (player != null) {
      if (player.isInOfflineMode()) {
        player.kick();
        return;
      }
      player.incAccKickCount();
      player.sendMessage("\u041A\u0442\u043E-\u0442\u043E \u043F\u044B\u0442\u0430\u0435\u0442\u0441\u044F \u0437\u0430\u0439\u0442\u0438 \u0437\u0430 \u0432\u0430\u0448\u0435\u0433\u043E \u043F\u0435\u0440\u0441\u043E\u043D\u0430\u0436\u0430!");

      if (Config.KICK_USED_ACCOUNT)
      {
        player.kick();
        return;
      }

      if (player.getAccKickCount() > 2) {
        player.kick();
      }

      return;
    }

    closeNow();
    LoginServerThread.getInstance().sendLogout(account);
  }

  public void setHWID(String hwid)
  {
    _hwid = hwid;
  }

  public String getHWID()
  {
    return _hwid;
  }

  public boolean acceptHWID(String hwid, boolean pw) {
    if ((hwid.equalsIgnoreCase("none")) || (pw)) {
      return true;
    }

    if (_hwid.equalsIgnoreCase(hwid)) {
      _myhwid = hwid;
      return true;
    }
    return false;
  }

  public String getMyHWID() {
    return _myhwid;
  }

  public void setMyHWID(String hwid) {
    _myhwid = hwid;
  }

  public void setProtected(boolean f)
  {
    _protected = f;
  }

  public boolean isProtected()
  {
    return _protected;
  }

  public void setInstanceCount(int instCount)
  {
    _instCount = instCount;
  }

  public int getInstanceCount()
  {
    return _instCount;
  }

  public void setPatchVersion(int patchVersion)
  {
    _patchVersion = patchVersion;
  }

  public int getPatchVersion()
  {
    return _patchVersion;
  }

  public boolean isAuthedGG() {
    return _isAuthedGG;
  }

  public boolean checkGameGuardReply(int[] reply)
  {
    return GameGuard.getInstance().checkGameGuardReply(this, reply);
  }

  public void startSession() {
    GameGuard.getInstance().startSession(this);
  }

  public void closeSession() {
    GameGuard.getInstance().closeSession(this);
  }

  public void sendGameGuardRequest() {
    sendPacket(Static.GAME_GUARD);
  }

  public void punishClient() {
    _log.warning(new StringBuilder().append(TimeLogger.getTime()).append("Game Guard [WARNING]").append(toString()).append(" kicked.").toString());
    Log.add(new StringBuilder().append(TimeLogger.getTime()).append(toString()).toString(), "game_guard");
    switch (Config.GAMEGUARD_PUNISH) {
    case 1:
      close();

      break;
    case 2:
      getActiveChar().setInJail(true);
      break;
    case 3:
      LoginServerThread.getInstance().sendAccessLevel(getAccountName(), -1);
      close();

      break;
    case 4:
      close();
    }

    closeSession();
  }

  public void setHasEmail(boolean hasEmail)
  {
    _hasEmail = hasEmail;
  }

  public boolean hasEmail() {
    return _hasEmail;
  }

  class AutoSaveTask
    implements Runnable
  {
    AutoSaveTask()
    {
    }

    public void run()
    {
      try
      {
        L2PcInstance player = getActiveChar();
        if (player != null)
          L2GameClient.saveCharToDisk(player);
      }
      catch (Throwable e) {
        L2GameClient._log.severe(e.toString());
      }
    }
  }

  class DisconnectTask
    implements Runnable
  {
    DisconnectTask()
    {
    }

    public void run()
    {
      try
      {
        _autoSaveInDB.cancel(true);

        L2PcInstance player = getActiveChar();
        if (player != null)
        {
          if (player.getActiveTradeList() != null)
          {
            player.cancelActiveTrade();
            player.setTransactionRequester(null);
            player.getTransactionRequester().setTransactionRequester(null);
          }

          if (player.atEvent) {
            EventData data = new EventData(player.eventX, player.eventY, player.eventZ, player.eventkarma, player.eventpvpkills, player.eventpkkills, player.eventTitle, player.kills, player.eventSitForced);
            L2Event.connectionLossData.put(player.getName(), data);
          }
          if (player.isFlying()) {
            player.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
          }

          if (player.getPet() != null) {
            player.getPet().unSummon(player);
          }

          player.deleteMe();
          try
          {
            L2GameClient.saveCharToDisk(player); } catch (Exception e2) {
          }
          player.gc();
          player = null;
        }
        setActiveChar(null);
      } catch (Exception e1) {
        L2GameClient._log.log(Level.WARNING, "error while disconnecting client", e1);
      } finally {
        LoginServerThread.getInstance().sendLogout(getAccountName());
      }
    }
  }

  public static enum GameClientState
  {
    CONNECTED, AUTHED, IN_GAME, DISCONNECTED;
  }
}
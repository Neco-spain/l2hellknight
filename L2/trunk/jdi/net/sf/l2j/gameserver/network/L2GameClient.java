package net.sf.l2j.gameserver.network;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
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
import net.sf.l2j.gameserver.communitybbs.Manager.RegionBBSManager;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.CharSelectInfoPackage;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.L2Event;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;
import net.sf.l2j.gameserver.util.FloodProtector;
import net.sf.l2j.util.EventData;
import net.sf.protection.nProtect;
import org.mmocore.network.ISocket;
import org.mmocore.network.MMOClient;
import org.mmocore.network.MMOConnection;

public final class L2GameClient extends MMOClient<MMOConnection<L2GameClient>>
{
  protected static final Logger _log = Logger.getLogger(L2GameClient.class.getName());
  public GameClientState state;
  public String accountName;
  public LoginServerThread.SessionKey sessionId;
  public L2PcInstance activeChar;
  private ReentrantLock _activeCharLock = new ReentrantLock();
  private long _connectionStartTime;
  private List<Integer> _charSlotMapping = new FastList();
  protected ScheduledFuture _autoSaveInDB;
  protected ScheduledFuture<?> _cleanupTask = null;
  private ScheduledFuture<?> _guardCheckTask = null;
  public GameCrypt crypt;
  private boolean _isAuthedGG;
  public byte packetsSentInSec = 0;
  public int packetsSentStartTick = 0;

  private boolean _isDetached = false;

  private boolean NotEnterWorld = true;

  private int _packetCount = 0;

  public boolean getEnterWorld()
  {
    return NotEnterWorld;
  }

  public void setEnterWorld(boolean value) {
    NotEnterWorld = value;
  }

  public L2GameClient(MMOConnection<L2GameClient> con)
  {
    super(con);
    state = GameClientState.CONNECTED;
    _connectionStartTime = System.currentTimeMillis();
    crypt = new GameCrypt();
    _guardCheckTask = nProtect.getInstance().startTask(this);
    _autoSaveInDB = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AutoSaveTask(), 300000L, 900000L);
  }

  public byte[] enableCrypt()
  {
    byte[] key = BlowFishKeygen.getRandomKey();
    crypt.setKey(key);
    return key;
  }

  public GameClientState getState()
  {
    return state;
  }

  public void setState(GameClientState pState)
  {
    state = pState;
  }

  public long getConnectionStartTime()
  {
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

  public L2PcInstance getActiveChar()
  {
    return activeChar;
  }

  public void setActiveChar(L2PcInstance pActiveChar)
  {
    activeChar = pActiveChar;
    if (activeChar != null)
    {
      L2World.getInstance().storeObject(getActiveChar());
    }
  }

  public ReentrantLock getActiveCharLock()
  {
    return _activeCharLock;
  }

  public boolean isAuthedGG()
  {
    return _isAuthedGG;
  }

  public void setGameGuardOk(boolean val)
  {
    _isAuthedGG = val;
  }

  public void setAccountName(String pAccountName)
  {
    accountName = pAccountName;
  }

  public String getAccountName()
  {
    return accountName;
  }

  public void setSessionId(LoginServerThread.SessionKey sk)
  {
    sessionId = sk;
  }

  public LoginServerThread.SessionKey getSessionId()
  {
    return sessionId;
  }

  public void sendPacket(L2GameServerPacket gsp)
  {
    if (_isDetached)
      return;
    if (getConnection() == null) {
      return;
    }
    getConnection().sendPacket(gsp);
    gsp.runImpl();
  }

  public boolean isDetached()
  {
    return _isDetached;
  }

  public void isDetached(boolean b)
  {
    _isDetached = b;
  }

  public L2PcInstance markToDeleteChar(int charslot) throws Exception
  {
    int objid = getObjectIdForSlot(charslot);
    if (objid < 0) {
      return null;
    }
    L2PcInstance character = L2PcInstance.load(objid);
    if (character.getClanId() != 0) {
      return character;
    }
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("UPDATE characters SET deletetime=? WHERE obj_id=?");
      statement.setLong(1, System.currentTimeMillis() + Config.DELETE_DAYS * 86400000L);
      statement.setInt(2, objid);
      statement.execute();
      statement.close();
    }
    catch (Exception e)
    {
      _log.warning(new StringBuilder().append("Data error on update delete time of char: ").append(e).toString());
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
    return null;
  }

  public L2PcInstance deleteChar(int charslot) throws Exception
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

  public static void saveCharToDisk(L2PcInstance cha)
  {
    try
    {
      cha.store();
    }
    catch (Exception e)
    {
      _log.severe(new StringBuilder().append("Error saving player character: ").append(e).toString());
    }
  }

  public void markRestoredChar(int charslot) throws Exception
  {
    int objid = getObjectIdForSlot(charslot);
    if (objid < 0)
      return;
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("UPDATE characters SET deletetime=0 WHERE obj_id=?");
      statement.setInt(1, objid);
      statement.execute();
      statement.close();
    }
    catch (Exception e)
    {
      _log.severe(new StringBuilder().append("Data error on restoring char: ").append(e).toString());
    }
    finally {
      try {
        con.close();
      }
      catch (Exception e)
      {
      }
    }
  }

  public static void deleteCharByObjId(int objid) {
    if (objid < 0) {
      return;
    }
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("DELETE FROM character_friends WHERE char_id=? OR friend_id=?");
      statement.setInt(1, objid);
      statement.setInt(2, objid);
      statement.execute();
      statement.close();

      statement = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=?");
      statement.setInt(1, objid);
      statement.execute();
      statement.close();

      statement = con.prepareStatement("DELETE FROM character_macroses WHERE char_obj_id=?");
      statement.setInt(1, objid);
      statement.execute();
      statement.close();

      statement = con.prepareStatement("DELETE FROM character_quests WHERE char_id=?");
      statement.setInt(1, objid);
      statement.execute();
      statement.close();

      statement = con.prepareStatement("DELETE FROM character_recipebook WHERE char_id=?");
      statement.setInt(1, objid);
      statement.execute();
      statement.close();

      statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE char_obj_id=?");
      statement.setInt(1, objid);
      statement.execute();
      statement.close();

      statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=?");
      statement.setInt(1, objid);
      statement.execute();
      statement.close();

      statement = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=?");
      statement.setInt(1, objid);
      statement.execute();
      statement.close();

      statement = con.prepareStatement("DELETE FROM character_subclasses WHERE char_obj_id=?");
      statement.setInt(1, objid);
      statement.execute();
      statement.close();

      statement = con.prepareStatement("DELETE FROM heroes WHERE char_id=?");
      statement.setInt(1, objid);
      statement.execute();
      statement.close();

      statement = con.prepareStatement("DELETE FROM olympiad_nobles WHERE char_id=?");
      statement.setInt(1, objid);
      statement.execute();
      statement.close();

      statement = con.prepareStatement("DELETE FROM seven_signs WHERE char_obj_id=?");
      statement.setInt(1, objid);
      statement.execute();
      statement.close();

      statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id IN (SELECT object_id FROM items WHERE items.owner_id=?)");
      statement.setInt(1, objid);
      statement.execute();
      statement.close();

      statement = con.prepareStatement("DELETE FROM augmentations WHERE item_id IN (SELECT object_id FROM items WHERE items.owner_id=?)");
      statement.setInt(1, objid);
      statement.execute();
      statement.close();

      statement = con.prepareStatement("DELETE FROM items WHERE owner_id=?");
      statement.setInt(1, objid);
      statement.execute();
      statement.close();

      statement = con.prepareStatement("DELETE FROM merchant_lease WHERE player_id=?");
      statement.setInt(1, objid);
      statement.execute();
      statement.close();

      statement = con.prepareStatement("DELETE FROM characters WHERE obj_Id=?");
      statement.setInt(1, objid);
      statement.execute();
      statement.close();
    }
    catch (Exception e)
    {
      _log.warning(new StringBuilder().append("Data error on deleting char: ").append(e).toString());
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  public L2PcInstance loadCharFromDisk(int charslot) {
    int objId = getObjectIdForSlot(charslot);
    if (objId < 0)
      return null;
    L2PcInstance ch = L2World.getInstance().getPlayer(objId);
    if (ch != null)
    {
      _log.severe(new StringBuilder().append("Attempt of double login: ").append(ch.getName()).append("(").append(objId).append(") ").append(getAccountName()).toString());
      if (ch.getClient() != null)
        ch.getClient().closeNow();
      else
        ch.deleteMe();
      return null;
    }

    L2PcInstance character = L2PcInstance.load(objId);

    if (character != null)
    {
      character.setRunning();
      character.standUp();

      character.refreshOverloaded();
      character.refreshExpertisePenalty();
      character.sendPacket(new UserInfo(character));
      character.broadcastKarma();
      character.setOnlineStatus(true);
    }
    else
    {
      _log.severe(new StringBuilder().append("could not restore in slot: ").append(charslot).toString());
    }

    return character;
  }

  public void setCharSelection(CharSelectInfoPackage[] chars)
  {
    _charSlotMapping.clear();

    for (int i = 0; i < chars.length; i++)
    {
      int objectId = chars[i].getObjectId();
      _charSlotMapping.add(new Integer(objectId));
    }
  }

  public void setCharSelection(Integer charId)
  {
    _charSlotMapping.clear();
    _charSlotMapping.add(charId);
  }

  public void close(L2GameServerPacket gsp)
  {
    if (getConnection() == null) {
      return;
    }
    getConnection().close(gsp);
  }

  private int getObjectIdForSlot(int charslot)
  {
    if ((charslot < 0) || (charslot >= _charSlotMapping.size()))
    {
      _log.warning(new StringBuilder().append(toString()).append(" tried to delete Character in slot ").append(charslot).append(" but no characters exits at that slot.").toString());
      return -1;
    }
    Integer objectId = (Integer)_charSlotMapping.get(charslot);
    return objectId.intValue();
  }

  public void MyOnForcedDisconnection()
  {
    onForcedDisconnection();
  }

  protected void onForcedDisconnection()
  {
    _log.info(new StringBuilder().append("Client ").append(toString()).append(" disconnected abnormally.").toString());
    L2PcInstance player = null;
    if ((player = getActiveChar()) != null)
    {
      _log.log(Level.WARNING, new StringBuilder().append("Character disconnected at Loc X:").append(getActiveChar().getX()).append(" Y:").append(getActiveChar().getY()).append(" Z:").append(getActiveChar().getZ()).toString());

      _log.log(Level.WARNING, new StringBuilder().append("Character disconnected in (closest) zone: ").append(MapRegionTable.getInstance().getClosestTownName(getActiveChar())).toString());

      if (player.isInParty())
      {
        player.getParty().removePartyMember(player);
      }
      else if (Olympiad.getInstance().isRegistered(player))
      {
        Olympiad.getInstance().unRegisterNoble(player);
      }

      player.deleteMe();
      try
      {
        player.store();
      }
      catch (Exception e)
      {
      }

      L2World.getInstance().removeFromAllPlayers(player);
      setActiveChar(null);
      LoginServerThread.getInstance().sendLogout(getAccountName());
    }
    stopGuardTask();
    nProtect.getInstance().closeSession(this);
  }

  public void stopGuardTask()
  {
    if (_guardCheckTask != null)
    {
      _guardCheckTask.cancel(true);
      _guardCheckTask = null;
    }
  }

  protected void onDisconnection()
  {
    try
    {
      ThreadPoolManager.getInstance().executeTask(new DisconnectTask());
    }
    catch (RejectedExecutionException e)
    {
    }
  }

  public void disconnectOffline()
  {
    if (getActiveChar() == null)
    {
      _log.info(new StringBuilder().append("No activechar! Account: ").append(getAccountName()).toString());
      return;
    }
    try
    {
      ThreadPoolManager.getInstance().scheduleGeneral(new DisconnectTask(), 1L);
    }
    catch (RejectedExecutionException e)
    {
    }
  }

  public String toString() {
    if (getConnection() == null)
      return "";
    try
    {
      InetAddress address = getConnection().getSocket().getInetAddress();
      switch (1.$SwitchMap$net$sf$l2j$gameserver$network$L2GameClient$GameClientState[getState().ordinal()])
      {
      case 1:
        return new StringBuilder().append("[IP: ").append(address == null ? "disconnected" : address.getHostAddress()).append("]").toString();
      case 2:
        return new StringBuilder().append("[Account: ").append(getAccountName()).append(" - IP: ").append(address == null ? "disconnected" : address.getHostAddress()).append("]").toString();
      case 3:
        return new StringBuilder().append("[Character: ").append(getActiveChar() == null ? "disconnected" : getActiveChar().getName()).append(" - Account: ").append(getAccountName()).append(" - IP: ").append(address == null ? "disconnected" : address.getHostAddress()).append("]").toString();
      }
      throw new IllegalStateException("Missing state on switch");
    }
    catch (NullPointerException e)
    {
    }
    return "[Character read failed due to disconnect]";
  }

  public void cleanMe(boolean fast)
  {
    try
    {
      synchronized (this)
      {
        if (_cleanupTask == null)
        {
          _cleanupTask = ThreadPoolManager.getInstance().scheduleGeneral(new CleanupTask(), fast ? 5L : 15000L);
        }
      }
    }
    catch (Exception e1)
    {
      _log.log(Level.WARNING, "Error during cleanup.", e1);
    }
  }

  public void stopAutoSave()
  {
    _autoSaveInDB.cancel(true);
  }

  public boolean isSendingTooManyUnknownPackets()
  {
    if (getActiveChar() == null) {
      return false;
    }
    if (!FloodProtector.getInstance().tryPerformAction(getActiveChar().getObjectId(), 11))
    {
      _packetCount += 1;

      return _packetCount >= Config.MAX_UNKNOWN_PACKETS;
    }

    _packetCount = 0;
    return false;
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
        {
          L2GameClient.saveCharToDisk(player);
        }
      }
      catch (Throwable e)
      {
        L2GameClient._log.severe(e.toString());
      }
    }
  }

  class CleanupTask
    implements Runnable
  {
    CleanupTask()
    {
    }

    public void run()
    {
      try
      {
        try
        {
          RegionBBSManager.getInstance().changeCommunityBoard();
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }

        if (_autoSaveInDB != null)
        {
          _autoSaveInDB.cancel(true);
        }

        L2PcInstance player = getActiveChar();

        if (player != null)
        {
          if (player.atEvent)
          {
            EventData data = new EventData(player.eventX, player.eventY, player.eventZ, player.eventkarma, player.eventpvpkills, player.eventpkkills, player.eventTitle, player.kills, player.eventSitForced);
            L2Event.connectionLossData.put(player.getName(), data);
          }
          if (player.isFlying())
          {
            player.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
          }
          isDetached(false);
          player.deleteMe();
          try
          {
            L2GameClient.saveCharToDisk(player);
          } catch (Exception e2) {
          }
        }
        setActiveChar(null);
      }
      catch (Exception e1)
      {
        L2GameClient._log.log(Level.WARNING, "Error while cleanup client.", e1);
      }
      finally
      {
        LoginServerThread.getInstance().sendLogout(getAccountName());
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
      boolean fast = true;
      try
      {
        isDetached(true);
        L2PcInstance player = getActiveChar();
        if (player != null)
        {
          if ((!player.isInOlympiadMode()) && (!player.isFestivalParticipant()) && (!TvTEvent.isPlayerParticipant(player.getObjectId())) && (!player.isInJail()) && (!player.isInFunEvent()))
          {
            if (player.isInCombat())
            {
              fast = false;
            }
          }
          if (((!Olympiad.getInstance().isRegistered(player)) && (!player.isInOlympiadMode()) && (!player.isInFunEvent()) && (player.isOffline()) && (player.isInStoreMode()) && (Config.OFFLINE_TRADE_ENABLE)) || ((player.isInCraftMode()) && (Config.OFFLINE_CRAFT_ENABLE)))
          {
            player.leaveParty();
            player.store();
            player.startAbnormalEffect(128);

            if (player.getOfflineStartTime() == 0L) {
              player.setOfflineStartTime(System.currentTimeMillis());
            }

            return;
          }

          if (Olympiad.getInstance().isRegistered(player)) {
            Olympiad.getInstance().unRegisterNoble(player);
          }

        }

        cleanMe(fast);
      }
      catch (Exception e1)
      {
        L2GameClient._log.log(Level.WARNING, "Error while disconnecting client.", e1);
      }
    }
  }

  public static enum GameClientState
  {
    CONNECTED, AUTHED, IN_GAME;
  }
}
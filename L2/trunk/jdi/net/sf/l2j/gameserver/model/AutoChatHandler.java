package net.sf.l2j.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.SevenSigns;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SiegeGuardInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.NpcKnownList;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.util.Rnd;

public class AutoChatHandler
  implements SpawnListener
{
  protected static final Logger _log = Logger.getLogger(AutoChatHandler.class.getName());
  private static AutoChatHandler _instance;
  private static final long DEFAULT_CHAT_DELAY = 30000L;
  protected Map<Integer, AutoChatInstance> _registeredChats;

  protected AutoChatHandler()
  {
    _registeredChats = new FastMap();
    restoreChatData();
    L2Spawn.addSpawnListener(this);
  }

  private void restoreChatData()
  {
    int numLoaded = 0;
    Connection con = null;
    PreparedStatement statement = null;
    PreparedStatement statement2 = null;
    ResultSet rs = null;
    ResultSet rs2 = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      statement = con.prepareStatement("SELECT * FROM auto_chat ORDER BY groupId ASC");
      rs = statement.executeQuery();

      while (rs.next())
      {
        numLoaded++;

        statement2 = con.prepareStatement("SELECT * FROM auto_chat_text WHERE groupId=?");
        statement2.setInt(1, rs.getInt("groupId"));
        rs2 = statement2.executeQuery();

        rs2.last();
        String[] chatTexts = new String[rs2.getRow()];
        int i = 0;
        rs2.first();

        while (rs2.next())
        {
          chatTexts[i] = rs2.getString("chatText");
          i++;
        }

        registerGlobalChat(rs.getInt("npcId"), chatTexts, rs.getLong("chatDelay"));

        statement2.close();
      }

      statement.close();

      if (Config.DEBUG)
        _log.config("AutoChatHandler: Loaded " + numLoaded + " chat group(s) from the database.");
    }
    catch (Exception e)
    {
      _log.warning("AutoSpawnHandler: Could not restore chat data: " + e);
    }
    finally
    {
      try
      {
        con.close();
      }
      catch (Exception e)
      {
      }
    }
  }

  public static AutoChatHandler getInstance()
  {
    if (_instance == null) _instance = new AutoChatHandler();

    return _instance;
  }

  public int size()
  {
    return _registeredChats.size();
  }

  public AutoChatInstance registerGlobalChat(int npcId, String[] chatTexts, long chatDelay)
  {
    return registerChat(npcId, null, chatTexts, chatDelay);
  }

  public AutoChatInstance registerChat(L2NpcInstance npcInst, String[] chatTexts, long chatDelay)
  {
    return registerChat(npcInst.getNpcId(), npcInst, chatTexts, chatDelay);
  }

  private final AutoChatInstance registerChat(int npcId, L2NpcInstance npcInst, String[] chatTexts, long chatDelay)
  {
    AutoChatInstance chatInst = null;

    if (chatDelay < 0L) chatDelay = 30000L;

    if (_registeredChats.containsKey(Integer.valueOf(npcId))) chatInst = (AutoChatInstance)_registeredChats.get(Integer.valueOf(npcId)); else {
      chatInst = new AutoChatInstance(npcId, chatTexts, chatDelay, npcInst == null);
    }
    if (npcInst != null) chatInst.addChatDefinition(npcInst);

    _registeredChats.put(Integer.valueOf(npcId), chatInst);

    return chatInst;
  }

  public boolean removeChat(int npcId)
  {
    AutoChatInstance chatInst = (AutoChatInstance)_registeredChats.get(Integer.valueOf(npcId));

    return removeChat(chatInst);
  }

  public boolean removeChat(AutoChatInstance chatInst)
  {
    if (chatInst == null) return false;

    _registeredChats.remove(chatInst);
    chatInst.setActive(false);

    if (Config.DEBUG) {
      _log.config("AutoChatHandler: Removed auto chat for NPC ID " + chatInst.getNPCId());
    }
    return true;
  }

  public AutoChatInstance getAutoChatInstance(int id, boolean byObjectId)
  {
    if (!byObjectId) return (AutoChatInstance)_registeredChats.get(Integer.valueOf(id));
    for (AutoChatInstance chatInst : _registeredChats.values()) {
      if (chatInst.getChatDefinition(id) != null) return chatInst;
    }
    return null;
  }

  public void setAutoChatActive(boolean isActive)
  {
    for (AutoChatInstance chatInst : _registeredChats.values())
      chatInst.setActive(isActive);
  }

  public void npcSpawned(L2NpcInstance npc)
  {
    synchronized (_registeredChats)
    {
      if (npc == null) return;

      int npcId = npc.getNpcId();

      if (_registeredChats.containsKey(Integer.valueOf(npcId)))
      {
        AutoChatInstance chatInst = (AutoChatInstance)_registeredChats.get(Integer.valueOf(npcId));

        if ((chatInst != null) && (chatInst.isGlobal())) chatInst.addChatDefinition(npc);
      }
    }
  }

  public class AutoChatInstance
  {
    protected int _npcId;
    private long _defaultDelay = 30000L;
    private String[] _defaultTexts;
    private boolean _defaultRandom = false;

    private boolean _globalChat = false;
    private boolean _isActive;
    private Map<Integer, AutoChatDefinition> _chatDefinitions = new FastMap();
    protected ScheduledFuture _chatTask;

    protected AutoChatInstance(int npcId, String[] chatTexts, long chatDelay, boolean isGlobal)
    {
      _defaultTexts = chatTexts;
      _npcId = npcId;
      _defaultDelay = chatDelay;
      _globalChat = isGlobal;

      if (Config.DEBUG) {
        AutoChatHandler._log.config("AutoChatHandler: Registered auto chat for NPC ID " + _npcId + " (Global Chat = " + _globalChat + ").");
      }

      setActive(true);
    }

    protected AutoChatDefinition getChatDefinition(int objectId)
    {
      return (AutoChatDefinition)_chatDefinitions.get(Integer.valueOf(objectId));
    }

    protected AutoChatDefinition[] getChatDefinitions()
    {
      return (AutoChatDefinition[])_chatDefinitions.values().toArray(new AutoChatDefinition[_chatDefinitions.values().size()]);
    }

    public int addChatDefinition(L2NpcInstance npcInst)
    {
      return addChatDefinition(npcInst, null, 0L);
    }

    public int addChatDefinition(L2NpcInstance npcInst, String[] chatTexts, long chatDelay)
    {
      int objectId = npcInst.getObjectId();
      AutoChatDefinition chatDef = new AutoChatDefinition(this, npcInst, chatTexts, chatDelay);
      if ((npcInst instanceof L2SiegeGuardInstance))
        chatDef.setRandomChat(true);
      _chatDefinitions.put(Integer.valueOf(objectId), chatDef);
      return objectId;
    }

    public boolean removeChatDefinition(int objectId)
    {
      if (!_chatDefinitions.containsKey(Integer.valueOf(objectId))) return false;

      AutoChatDefinition chatDefinition = (AutoChatDefinition)_chatDefinitions.get(Integer.valueOf(objectId));
      chatDefinition.setActive(false);

      _chatDefinitions.remove(Integer.valueOf(objectId));

      return true;
    }

    public boolean isActive()
    {
      return _isActive;
    }

    public boolean isGlobal()
    {
      return _globalChat;
    }

    public boolean isDefaultRandom()
    {
      return _defaultRandom;
    }

    public boolean isRandomChat(int objectId)
    {
      if (!_chatDefinitions.containsKey(Integer.valueOf(objectId))) return false;

      return ((AutoChatDefinition)_chatDefinitions.get(Integer.valueOf(objectId))).isRandomChat();
    }

    public int getNPCId()
    {
      return _npcId;
    }

    public int getDefinitionCount()
    {
      return _chatDefinitions.size();
    }

    public L2NpcInstance[] getNPCInstanceList()
    {
      List npcInsts = new FastList();

      for (AutoChatDefinition chatDefinition : _chatDefinitions.values()) {
        npcInsts.add(chatDefinition._npcInstance);
      }
      return (L2NpcInstance[])npcInsts.toArray(new L2NpcInstance[npcInsts.size()]);
    }

    public long getDefaultDelay()
    {
      return _defaultDelay;
    }

    public String[] getDefaultTexts()
    {
      return _defaultTexts;
    }

    public void setDefaultChatDelay(long delayValue)
    {
      _defaultDelay = delayValue;
    }

    public void setDefaultChatTexts(String[] textsValue)
    {
      _defaultTexts = textsValue;
    }

    public void setDefaultRandom(boolean randValue)
    {
      _defaultRandom = randValue;
    }

    public void setChatDelay(int objectId, long delayValue)
    {
      AutoChatDefinition chatDef = getChatDefinition(objectId);

      if (chatDef != null) chatDef.setChatDelay(delayValue);
    }

    public void setChatTexts(int objectId, String[] textsValue)
    {
      AutoChatDefinition chatDef = getChatDefinition(objectId);

      if (chatDef != null) chatDef.setChatTexts(textsValue);
    }

    public void setRandomChat(int objectId, boolean randValue)
    {
      AutoChatDefinition chatDef = getChatDefinition(objectId);

      if (chatDef != null) chatDef.setRandomChat(randValue);
    }

    public void setActive(boolean activeValue)
    {
      if (_isActive == activeValue) return;

      _isActive = activeValue;

      if (!isGlobal())
      {
        for (AutoChatDefinition chatDefinition : _chatDefinitions.values()) {
          chatDefinition.setActive(activeValue);
        }
        return;
      }

      if (isActive())
      {
        AutoChatRunner acr = new AutoChatRunner(_npcId, -1);
        _chatTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(acr, _defaultDelay, _defaultDelay);
      }
      else
      {
        _chatTask.cancel(false);
      }
    }

    private class AutoChatRunner
      implements Runnable
    {
      private int _runnerNpcId;
      private int _objectId;

      protected AutoChatRunner(int pNpcId, int pObjectId)
      {
        _runnerNpcId = pNpcId;
        _objectId = pObjectId;
      }

      public synchronized void run()
      {
        AutoChatHandler.AutoChatInstance chatInst = (AutoChatHandler.AutoChatInstance)_registeredChats.get(Integer.valueOf(_runnerNpcId));
        AutoChatHandler.AutoChatInstance.AutoChatDefinition[] chatDefinitions;
        AutoChatHandler.AutoChatInstance.AutoChatDefinition[] chatDefinitions;
        if (chatInst.isGlobal())
        {
          chatDefinitions = chatInst.getChatDefinitions();
        }
        else
        {
          AutoChatHandler.AutoChatInstance.AutoChatDefinition chatDef = chatInst.getChatDefinition(_objectId);

          if (chatDef == null)
          {
            AutoChatHandler._log.warning("AutoChatHandler: Auto chat definition is NULL for NPC ID " + _npcId + ".");

            return;
          }

          chatDefinitions = new AutoChatHandler.AutoChatInstance.AutoChatDefinition[] { chatDef };
        }

        if (Config.DEBUG) {
          AutoChatHandler._log.info("AutoChatHandler: Running auto chat for " + chatDefinitions.length + " instances of NPC ID " + _npcId + "." + " (Global Chat = " + chatInst.isGlobal() + ")");
        }

        for (AutoChatHandler.AutoChatInstance.AutoChatDefinition chatDef : chatDefinitions)
        {
          try
          {
            L2NpcInstance chatNpc = chatDef._npcInstance;
            List nearbyPlayers = new FastList();
            List nearbyGMs = new FastList();

            for (L2Character player : chatNpc.getKnownList().getKnownCharactersInRadius(1500L))
            {
              if (!(player instanceof L2PcInstance))
                continue;
              if (((L2PcInstance)player).isGM()) nearbyGMs.add((L2PcInstance)player); else {
                nearbyPlayers.add((L2PcInstance)player);
              }
            }
            int maxIndex = chatDef.getChatTexts().length;
            int lastIndex = Rnd.nextInt(maxIndex);

            String creatureName = chatNpc.getName();

            if (!chatDef.isRandomChat())
            {
              lastIndex = chatDef._chatIndex;
              lastIndex++;

              if (lastIndex == maxIndex) lastIndex = 0;

              chatDef._chatIndex = lastIndex;
            }

            String text = chatDef.getChatTexts()[lastIndex];

            if (text == null) return;
            int losingCabal;
            if (!nearbyPlayers.isEmpty())
            {
              int randomPlayerIndex = Rnd.nextInt(nearbyPlayers.size());

              L2PcInstance randomPlayer = (L2PcInstance)nearbyPlayers.get(randomPlayerIndex);

              int winningCabal = SevenSigns.getInstance().getCabalHighestScore();
              losingCabal = 0;

              if (winningCabal == 2) losingCabal = 1;
              else if (winningCabal == 1) {
                losingCabal = 2;
              }
              if (text.indexOf("%player_random%") > -1) {
                text = text.replaceAll("%player_random%", randomPlayer.getName());
              }
              if (text.indexOf("%player_cabal_winner%") > -1)
              {
                for (L2PcInstance nearbyPlayer : nearbyPlayers)
                {
                  if (SevenSigns.getInstance().getPlayerCabal(nearbyPlayer) == winningCabal)
                  {
                    text = text.replaceAll("%player_cabal_winner%", nearbyPlayer.getName());

                    break;
                  }
                }
              }

              if (text.indexOf("%player_cabal_loser%") > -1)
              {
                for (L2PcInstance nearbyPlayer : nearbyPlayers)
                {
                  if (SevenSigns.getInstance().getPlayerCabal(nearbyPlayer) == losingCabal)
                  {
                    text = text.replaceAll("%player_cabal_loser%", nearbyPlayer.getName());

                    break;
                  }
                }
              }
            }

            if (text == null) return;

            if ((text.contains("%player_cabal_loser%")) || (text.contains("%player_cabal_winner%")) || (text.contains("%player_random%")))
            {
              return;
            }
            CreatureSay cs = new CreatureSay(chatNpc.getObjectId(), 0, creatureName, text);

            for (L2PcInstance nearbyPlayer : nearbyPlayers)
              nearbyPlayer.sendPacket(cs);
            for (L2PcInstance nearbyGM : nearbyGMs) {
              nearbyGM.sendPacket(cs);
            }
            if (Config.DEBUG) {
              AutoChatHandler._log.fine("AutoChatHandler: Chat propogation for object ID " + chatNpc.getObjectId() + " (" + creatureName + ") with text '" + text + "' sent to " + nearbyPlayers.size() + " nearby players.");
            }

          }
          catch (Exception e)
          {
            e.printStackTrace();
            return;
          }
        }
      }
    }

    private class AutoChatDefinition
    {
      protected int _chatIndex = 0;
      protected L2NpcInstance _npcInstance;
      protected AutoChatHandler.AutoChatInstance _chatInstance;
      private long _chatDelay = 0L;
      private String[] _chatTexts = null;
      private boolean _isActiveDefinition;
      private boolean _randomChat;

      protected AutoChatDefinition(AutoChatHandler.AutoChatInstance chatInst, L2NpcInstance npcInst, String[] chatTexts, long chatDelay)
      {
        _npcInstance = npcInst;

        _chatInstance = chatInst;
        _randomChat = chatInst.isDefaultRandom();

        _chatDelay = chatDelay;
        _chatTexts = chatTexts;

        if (Config.DEBUG) {
          AutoChatHandler._log.info("AutoChatHandler: Chat definition added for NPC ID " + _npcInstance.getNpcId() + " (Object ID = " + _npcInstance.getObjectId() + ").");
        }

        if (!chatInst.isGlobal()) setActive(true);
      }

      protected AutoChatDefinition(AutoChatHandler.AutoChatInstance chatInst, L2NpcInstance npcInst)
      {
        this(chatInst, npcInst, null, -1L);
      }

      protected String[] getChatTexts()
      {
        if (_chatTexts != null) return _chatTexts;
        return _chatInstance.getDefaultTexts();
      }

      private long getChatDelay()
      {
        if (_chatDelay > 0L) return _chatDelay;
        return _chatInstance.getDefaultDelay();
      }

      private boolean isActive()
      {
        return _isActiveDefinition;
      }

      boolean isRandomChat()
      {
        return _randomChat;
      }

      void setRandomChat(boolean randValue)
      {
        _randomChat = randValue;
      }

      void setChatDelay(long delayValue)
      {
        _chatDelay = delayValue;
      }

      void setChatTexts(String[] textsValue)
      {
        _chatTexts = textsValue;
      }

      void setActive(boolean activeValue)
      {
        if (isActive() == activeValue) return;

        if (activeValue)
        {
          AutoChatHandler.AutoChatInstance.AutoChatRunner acr = new AutoChatHandler.AutoChatInstance.AutoChatRunner(this$1, this$1._npcId, _npcInstance.getObjectId());
          if (getChatDelay() == 0L)
          {
            this$1._chatTask = ThreadPoolManager.getInstance().scheduleGeneral(acr, 5L);
          }
          else this$1._chatTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(acr, getChatDelay(), getChatDelay());
        }
        else
        {
          this$1._chatTask.cancel(false);
        }

        _isActiveDefinition = activeValue;
      }
    }
  }
}
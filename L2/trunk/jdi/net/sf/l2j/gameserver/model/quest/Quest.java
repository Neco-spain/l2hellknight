package net.sf.l2j.gameserver.model.quest;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.instancemanager.QuestManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.scripting.ManagedScript;
import net.sf.l2j.gameserver.scripting.ScriptManager;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.Rnd;

public class Quest extends ManagedScript
{
  protected static final Logger _log = Logger.getLogger(Quest.class.getName());
  private static Map<String, Quest> _allEventsS = new FastMap();
  private Map<String, FastList<QuestTimer>> _allEventTimers = new FastMap();
  private final ReentrantReadWriteLock _rwLock = new ReentrantReadWriteLock();
  private final int _questId;
  private final String _name;
  private final String _descr;
  private final byte _initialState = 0;
  public int[] questItemIds = null;

  public static Collection<Quest> findAllEvents() {
    return _allEventsS.values();
  }

  public Quest(int questId, String name, String descr) {
    _questId = questId;
    _name = name;
    _descr = descr;

    if (questId != 0)
    {
      QuestManager.getInstance().addQuest(this);
    }
    else
    {
      _allEventsS.put(name, this);
    }
    init_LoadGlobalData();
  }

  protected void init_LoadGlobalData()
  {
  }

  public void saveGlobalData()
  {
  }

  public int getQuestIntId()
  {
    return _questId;
  }

  public QuestState newQuestState(L2PcInstance player)
  {
    QuestState qs = new QuestState(this, player, getInitialState());
    createQuestInDb(qs);
    return qs;
  }

  public byte getInitialState()
  {
    return 0;
  }

  public String getName()
  {
    return _name;
  }

  public String getDescr()
  {
    return _descr;
  }

  public void startQuestTimer(String name, long time, L2NpcInstance npc, L2PcInstance player)
  {
    startQuestTimer(name, time, npc, player, false);
  }

  public void startQuestTimer(String name, long time, L2NpcInstance npc, L2PcInstance player, boolean repeating)
  {
    FastList timers = getQuestTimers(name);
    if (timers == null)
    {
      timers = new FastList();
      timers.add(new QuestTimer(this, name, time, npc, player, repeating));
      _allEventTimers.put(name, timers);
    }
    else if (getQuestTimer(name, npc, player) == null)
    {
      try
      {
        _rwLock.writeLock().lock();
        timers.add(new QuestTimer(this, name, time, npc, player, repeating));
      }
      finally
      {
        _rwLock.writeLock().unlock();
      }
    }
  }

  public QuestTimer getQuestTimer(String name, L2NpcInstance npc, L2PcInstance player)
  {
    FastList qt = getQuestTimers(name);

    if ((qt == null) || (qt.isEmpty()))
      return null;
    try
    {
      _rwLock.readLock().lock();
      for (QuestTimer timer : qt)
      {
        if (timer != null)
        {
          if (timer.isMatch(this, name, npc, player)) {
            QuestTimer localQuestTimer1 = timer;
            return localQuestTimer1;
          }
        }
      } } finally { _rwLock.readLock().unlock();
    }
    return null;
  }

  private FastList<QuestTimer> getQuestTimers(String name)
  {
    return (FastList)_allEventTimers.get(name);
  }

  public void cancelQuestTimers(String name)
  {
    FastList timers = getQuestTimers(name);
    if (timers == null)
      return;
    try
    {
      _rwLock.writeLock().lock();
      for (QuestTimer timer : timers)
      {
        if (timer != null)
        {
          timer.cancel();
        }
      }
    }
    finally
    {
      _rwLock.writeLock().unlock();
    }
  }

  public void cancelQuestTimer(String name, L2NpcInstance npc, L2PcInstance player)
  {
    QuestTimer timer = getQuestTimer(name, npc, player);
    if (timer != null)
      timer.cancel();
  }

  public void removeQuestTimer(QuestTimer timer)
  {
    if (timer == null)
      return;
    FastList timers = getQuestTimers(timer.getName());
    if (timers == null)
      return;
    try
    {
      _rwLock.writeLock().lock();
      timers.remove(timer);
    }
    finally
    {
      _rwLock.writeLock().unlock();
    }
  }

  public final boolean notifyAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
  {
    String res = null;
    try
    {
      res = onAttack(npc, attacker, damage, isPet);
    }
    catch (Exception e)
    {
      return showError(attacker, e);
    }
    return showResult(attacker, res);
  }

  public final boolean notifyDeath(L2Character killer, L2Character victim, QuestState qs)
  {
    String res = null;
    try
    {
      res = onDeath(killer, victim, qs);
    }
    catch (Exception e)
    {
      return showError(qs.getPlayer(), e);
    }
    return showResult(qs.getPlayer(), res);
  }

  public final boolean notifySpellFinished(L2NpcInstance instance, L2PcInstance player, L2Skill skill)
  {
    String res = null;
    try
    {
      res = onSpellFinished(instance, player, skill);
    }
    catch (Exception e)
    {
      return showError(player, e);
    }
    return showResult(player, res);
  }

  public final boolean notifySpawn(L2NpcInstance npc)
  {
    try
    {
      onSpawn(npc);
    }
    catch (Exception e)
    {
      _log.log(Level.WARNING, "", e);
      return true;
    }
    return false;
  }

  public final boolean notifyEvent(String event, L2NpcInstance npc, L2PcInstance player)
  {
    String res = null;
    try
    {
      res = onAdvEvent(event, npc, player);
    }
    catch (Exception e)
    {
      return showError(player, e);
    }
    return showResult(player, res);
  }

  public final boolean notifyKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet)
  {
    String res = null;
    try
    {
      res = onKill(npc, killer, isPet);
    }
    catch (Exception e)
    {
      return showError(killer, e);
    }
    return showResult(killer, res);
  }

  public final boolean notifyTalk(L2NpcInstance npc, QuestState qs)
  {
    String res = null;
    try
    {
      res = onTalk(npc, qs.getPlayer());
    }
    catch (Exception e)
    {
      return showError(qs.getPlayer(), e);
    }
    qs.getPlayer().setLastQuestNpcObject(npc.getObjectId());
    return showResult(qs.getPlayer(), res);
  }

  public final boolean notifyFirstTalk(L2NpcInstance npc, L2PcInstance player)
  {
    String res = null;
    try
    {
      res = onFirstTalk(npc, player);
    }
    catch (Exception e)
    {
      return showError(player, e);
    }

    if ((res != null) && (res.length() > 0)) {
      return showResult(player, res);
    }

    player.sendPacket(new ActionFailed());

    return true;
  }

  public final boolean notifySkillSee(L2NpcInstance npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
  {
    ThreadPoolManager.getInstance().executeAi(new tmpOnSkillSee(npc, caster, skill, targets, isPet));
    return true;
  }

  public final boolean notifyFactionCall(L2NpcInstance npc, L2NpcInstance caller, L2PcInstance attacker, boolean isPet)
  {
    String res = null;
    try
    {
      res = onFactionCall(npc, caller, attacker, isPet);
    }
    catch (Exception e)
    {
      return showError(attacker, e);
    }
    return showResult(attacker, res);
  }

  public final boolean notifyAggroRangeEnter(L2NpcInstance npc, L2PcInstance player, boolean isPet)
  {
    ThreadPoolManager.getInstance().executeAi(new tmpOnAggroEnter(npc, player, isPet));
    return true;
  }

  public String onAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
  {
    return null;
  }

  public String onDeath(L2Character killer, L2Character victim, QuestState qs)
  {
    if ((killer instanceof L2NpcInstance)) {
      return onAdvEvent("", (L2NpcInstance)killer, qs.getPlayer());
    }
    return onAdvEvent("", null, qs.getPlayer());
  }

  public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
  {
    if (player != null)
    {
      QuestState qs = player.getQuestState(getName());
      if (qs != null) {
        return onEvent(event, qs);
      }
    }
    return null;
  }

  public String onEvent(String event, QuestState qs)
  {
    return null;
  }

  public String onKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet)
  {
    return null;
  }

  public String onTalk(L2NpcInstance npc, L2PcInstance talker)
  {
    return null;
  }

  public String onFirstTalk(L2NpcInstance npc, L2PcInstance player)
  {
    return null;
  }

  public String onSkillSee(L2NpcInstance npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
  {
    return null;
  }

  public String onSpellFinished(L2NpcInstance npc, L2PcInstance player, L2Skill skill)
  {
    return null;
  }

  public String onSpawn(L2NpcInstance npc)
  {
    return null;
  }

  public String onFactionCall(L2NpcInstance npc, L2NpcInstance caller, L2PcInstance attacker, boolean isPet)
  {
    return null;
  }

  public String onAggroRangeEnter(L2NpcInstance npc, L2PcInstance player, boolean isPet)
  {
    return null;
  }

  public boolean showError(L2PcInstance player, Throwable t)
  {
    _log.log(Level.WARNING, getScriptFile().getAbsolutePath(), t);
    if (player.isGM())
    {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      t.printStackTrace(pw);
      pw.close();
      String res = "<html><body><title>Script error</title>" + sw.toString() + "</body></html>";
      return showResult(player, res);
    }
    return false;
  }

  public boolean showResult(L2PcInstance player, String res)
  {
    if ((res == null) || (res.equals("")))
      return true;
    if (res.endsWith(".htm"))
    {
      showHtmlFile(player, res);
    }
    else if (res.startsWith("<html>"))
    {
      NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
      npcReply.setHtml(res);
      npcReply.replace("%playername%", player.getName());
      player.sendPacket(npcReply);
      player.sendPacket(new ActionFailed());
    }
    else
    {
      player.sendMessage(res);
    }
    return false;
  }

  public static final void playerEnter(L2PcInstance player)
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement invalidQuestData = con.prepareStatement("DELETE FROM character_quests WHERE char_Id=? and name=?");
      PreparedStatement invalidQuestDataVar = con.prepareStatement("delete FROM character_quests WHERE char_Id=? and name=? and var=?");

      PreparedStatement statement = con.prepareStatement("SELECT name,value FROM character_quests WHERE char_Id=? AND var=?");
      statement.setInt(1, player.getObjectId());
      statement.setString(2, "<state>");
      ResultSet rs = statement.executeQuery();
      while (rs.next())
      {
        String questId = rs.getString("name");
        String statename = rs.getString("value");

        Quest q = QuestManager.getInstance().getQuest(questId);
        if (q == null)
        {
          _log.finer("Unknown quest " + questId + " for player " + player.getName());
          if (!Config.AUTODELETE_INVALID_QUEST_DATA)
            continue;
          invalidQuestData.setInt(1, player.getObjectId());
          invalidQuestData.setString(2, questId);
          invalidQuestData.executeUpdate(); continue;
        }

        new QuestState(q, player, State.getStateId(statename));
      }
      rs.close();
      invalidQuestData.close();
      statement.close();

      statement = con.prepareStatement("SELECT name,var,value FROM character_quests WHERE char_Id=? AND var<>?");
      statement.setInt(1, player.getObjectId());
      statement.setString(2, "<state>");
      rs = statement.executeQuery();
      while (rs.next())
      {
        String questId = rs.getString("name");
        String var = rs.getString("var");
        String value = rs.getString("value");
        QuestState qs = player.getQuestState(questId);
        if (qs == null)
        {
          _log.finer("Lost variable " + var + " in quest " + questId + " for player " + player.getName());
          if (!Config.AUTODELETE_INVALID_QUEST_DATA)
            continue;
          invalidQuestDataVar.setInt(1, player.getObjectId());
          invalidQuestDataVar.setString(2, questId);
          invalidQuestDataVar.setString(3, var);
          invalidQuestDataVar.executeUpdate(); continue;
        }

        qs.setInternal(var, value);
      }
      rs.close();
      invalidQuestDataVar.close();
      statement.close();
    }
    catch (Exception e)
    {
      _log.log(Level.WARNING, "could not insert char quest:", e);
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

    for (String name : _allEventsS.keySet())
    {
      player.processQuestEvent(name, "enter");
    }
  }

  public final void saveGlobalQuestVar(String var, String value)
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("REPLACE INTO quest_global_data (quest_name,var,value) VALUES (?,?,?)");
      statement.setString(1, getName());
      statement.setString(2, var);
      statement.setString(3, value);
      statement.executeUpdate();
      statement.close();
    }
    catch (Exception e)
    {
      _log.log(Level.WARNING, "could not insert global quest variable:", e);
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

  public final String loadGlobalQuestVar(String var)
  {
    String result = "";
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("SELECT value FROM quest_global_data WHERE quest_name = ? AND var = ?");
      statement.setString(1, getName());
      statement.setString(2, var);
      ResultSet rs = statement.executeQuery();
      if (rs.first())
        result = rs.getString(1);
      rs.close();
      statement.close();
    }
    catch (Exception e)
    {
      _log.log(Level.WARNING, "could not load global quest variable:", e);
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
    return result;
  }

  public final void deleteGlobalQuestVar(String var)
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("DELETE FROM quest_global_data WHERE quest_name = ? AND var = ?");
      statement.setString(1, getName());
      statement.setString(2, var);
      statement.executeUpdate();
      statement.close();
    }
    catch (Exception e)
    {
      _log.log(Level.WARNING, "could not delete global quest variable:", e);
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

  public final void deleteAllGlobalQuestVars()
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("DELETE FROM quest_global_data WHERE quest_name = ?");
      statement.setString(1, getName());
      statement.executeUpdate();
      statement.close();
    }
    catch (Exception e)
    {
      _log.log(Level.WARNING, "could not delete global quest variables:", e);
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

  public static void createQuestVarInDb(QuestState qs, String var, String value)
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("INSERT INTO character_quests (char_Id,name,var,value) VALUES (?,?,?,?)");
      statement.setInt(1, qs.getPlayer().getObjectId());
      statement.setString(2, qs.getQuestName());
      statement.setString(3, var);
      statement.setString(4, value);
      statement.executeUpdate();
      statement.close();
    }
    catch (Exception e)
    {
      _log.log(Level.WARNING, "could not insert char quest:", e);
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

  public static void updateQuestVarInDb(QuestState qs, String var, String value)
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("UPDATE character_quests SET value=? WHERE char_Id=? AND name=? AND var = ?");
      statement.setString(1, value);
      statement.setInt(2, qs.getPlayer().getObjectId());
      statement.setString(3, qs.getQuestName());
      statement.setString(4, var);
      statement.executeUpdate();
      statement.close();
    }
    catch (Exception e)
    {
      _log.log(Level.WARNING, "could not update char quest:", e);
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

  public static void deleteQuestVarInDb(QuestState qs, String var)
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("DELETE FROM character_quests WHERE char_Id=? AND name=? AND var=?");
      statement.setInt(1, qs.getPlayer().getObjectId());
      statement.setString(2, qs.getQuestName());
      statement.setString(3, var);
      statement.executeUpdate();
      statement.close();
    }
    catch (Exception e)
    {
      _log.log(Level.WARNING, "could not delete char quest:", e);
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

  public static void deleteQuestInDb(QuestState qs)
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("DELETE FROM character_quests WHERE char_Id=? AND name=?");
      statement.setInt(1, qs.getPlayer().getObjectId());
      statement.setString(2, qs.getQuestName());
      statement.executeUpdate();
      statement.close();
    }
    catch (Exception e)
    {
      _log.log(Level.WARNING, "could not delete char quest:", e);
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

  public static void createQuestInDb(QuestState qs)
  {
    createQuestVarInDb(qs, "<state>", State.getStateName(qs.getState()));
  }

  public static void updateQuestInDb(QuestState qs)
  {
    String val = State.getStateName(qs.getState());
    updateQuestVarInDb(qs, "<state>", val);
  }

  public L2NpcTemplate addEventId(int npcId, QuestEventType eventType)
  {
    try
    {
      L2NpcTemplate t = NpcTable.getInstance().getTemplate(npcId);
      if (t != null)
      {
        t.addQuestEvent(eventType, this);
      }
      return t;
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }return null;
  }

  public L2NpcTemplate addStartNpc(int npcId)
  {
    return addEventId(npcId, QuestEventType.QUEST_START);
  }

  public L2NpcTemplate addFirstTalkId(int npcId)
  {
    return addEventId(npcId, QuestEventType.ON_FIRST_TALK);
  }

  public L2NpcTemplate addAttackId(int attackId)
  {
    return addEventId(attackId, QuestEventType.ON_ATTACK);
  }

  public L2NpcTemplate addKillId(int killId)
  {
    return addEventId(killId, QuestEventType.ON_KILL);
  }

  public L2NpcTemplate addTalkId(int talkId)
  {
    return addEventId(talkId, QuestEventType.ON_TALK);
  }

  public L2NpcTemplate addSpawnId(int npcId)
  {
    return addEventId(npcId, QuestEventType.ON_SPAWN);
  }

  public L2NpcTemplate addSkillSeeId(int npcId)
  {
    return addEventId(npcId, QuestEventType.ON_SKILL_SEE);
  }

  public L2NpcTemplate addSpellFinishedId(int npcId)
  {
    return addEventId(npcId, QuestEventType.ON_SPELL_FINISHED);
  }

  public L2NpcTemplate addFactionCallId(int npcId)
  {
    return addEventId(npcId, QuestEventType.ON_FACTION_CALL);
  }

  public L2NpcTemplate addAggroRangeEnterId(int npcId)
  {
    return addEventId(npcId, QuestEventType.ON_AGGRO_RANGE_ENTER);
  }

  public L2PcInstance getRandomPartyMember(L2PcInstance player)
  {
    if (player == null)
      return null;
    if ((player.getParty() == null) || (player.getParty().getPartyMembers().size() == 0))
      return player;
    L2Party party = player.getParty();
    return (L2PcInstance)party.getPartyMembers().get(Rnd.get(party.getPartyMembers().size()));
  }

  public L2PcInstance getRandomPartyMember(L2PcInstance player, String value)
  {
    return getRandomPartyMember(player, "cond", value);
  }

  public L2PcInstance getRandomPartyMember(L2PcInstance player, String var, String value)
  {
    if (player == null) {
      return null;
    }

    if (var == null) {
      return getRandomPartyMember(player);
    }

    QuestState temp = null;
    L2Party party = player.getParty();

    if ((party == null) || (party.getPartyMembers().size() == 0))
    {
      temp = player.getQuestState(getName());
      if ((temp != null) && (temp.get(var) != null) && (((String)temp.get(var)).equalsIgnoreCase(value))) {
        return player;
      }
      return null;
    }

    FastList candidates = new FastList();

    L2Object target = player.getTarget();
    if (target == null) {
      target = player;
    }
    for (L2PcInstance partyMember : party.getPartyMembers())
    {
      temp = partyMember.getQuestState(getName());
      if ((temp != null) && (temp.get(var) != null) && (((String)temp.get(var)).equalsIgnoreCase(value)) && (partyMember.isInsideRadius(target, 1500, true, false))) {
        candidates.add(partyMember);
      }
    }
    if (candidates.size() == 0) {
      return null;
    }

    return (L2PcInstance)candidates.get(Rnd.get(candidates.size()));
  }

  public L2PcInstance getRandomPartyMemberState(L2PcInstance player, byte state)
  {
    if (player == null) {
      return null;
    }

    QuestState temp = null;
    L2Party party = player.getParty();

    if ((party == null) || (party.getPartyMembers().size() == 0))
    {
      temp = player.getQuestState(getName());
      if ((temp != null) && (temp.getState() == state)) {
        return player;
      }
      return null;
    }

    FastList candidates = new FastList();

    L2Object target = player.getTarget();
    if (target == null) {
      target = player;
    }
    for (L2PcInstance partyMember : party.getPartyMembers())
    {
      temp = partyMember.getQuestState(getName());
      if ((temp != null) && (temp.getState() == state) && (partyMember.isInsideRadius(target, 1500, true, false))) {
        candidates.add(partyMember);
      }
    }
    if (candidates.size() == 0) {
      return null;
    }

    return (L2PcInstance)candidates.get(Rnd.get(candidates.size()));
  }

  public String showHtmlFile(L2PcInstance player, String fileName)
  {
    String questId = getName();

    String directory = getDescr().toLowerCase();
    String content = HtmCache.getInstance().getHtm("data/scripts/" + directory + "/" + questId + "/" + fileName);

    if (content == null) {
      content = HtmCache.getInstance().getHtmForce("data/scripts/quests/" + questId + "/" + fileName);
    }
    if ((player != null) && (player.getTarget() != null)) {
      content = content.replaceAll("%objectId%", String.valueOf(player.getTarget().getObjectId()));
    }

    if (content != null)
    {
      NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
      npcReply.setHtml(content);
      npcReply.replace("%playername%", player.getName());
      player.sendPacket(npcReply);
      player.sendPacket(new ActionFailed());
    }

    return content;
  }

  public L2NpcInstance addSpawn(int npcId, L2Character cha)
  {
    return addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), false, 0, false);
  }

  public L2NpcInstance addSpawn(int npcId, L2Character cha, boolean isSummonSpawn)
  {
    return addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), false, 0, isSummonSpawn);
  }

  public L2NpcInstance addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffSet, int despawnDelay)
  {
    return addSpawn(npcId, x, y, z, heading, randomOffSet, despawnDelay, false);
  }

  public L2NpcInstance addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, int despawnDelay, boolean isSummonSpawn)
  {
    L2NpcInstance result = null;
    try
    {
      L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
      if (template != null)
      {
        if ((x == 0) && (y == 0))
        {
          _log.log(Level.SEVERE, "Failed to adjust bad locks for quest spawn!  Spawn aborted!");
          return null;
        }
        if (randomOffset)
        {
          int offset = Rnd.get(2);
          if (offset == 0)
          {
            offset = -1;
          }
          offset *= Rnd.get(50, 100);
          x += offset;

          offset = Rnd.get(2);
          if (offset == 0)
          {
            offset = -1;
          }
          offset *= Rnd.get(50, 100);
          y += offset;
        }
        L2Spawn spawn = new L2Spawn(template);
        spawn.setHeading(heading);
        spawn.setLocx(x);
        spawn.setLocy(y);
        spawn.setLocz(z + 20);
        spawn.stopRespawn();
        result = spawn.spawnOne(true);

        if (despawnDelay > 0) {
          ThreadPoolManager.getInstance().scheduleGeneral(new DeSpawnScheduleTimerTask(result), despawnDelay);
        }
        return result;
      }
    }
    catch (Exception e1)
    {
      _log.warning("Could not spawn Npc " + npcId);
    }

    return null;
  }

  public int[] getRegisteredItemIds()
  {
    return questItemIds;
  }

  public String getScriptName()
  {
    return getName();
  }

  public void setActive(boolean status)
  {
  }

  public boolean reload()
  {
    unload();
    return super.reload();
  }

  public boolean unload()
  {
    saveGlobalData();

    for (FastList timers : _allEventTimers.values())
      for (QuestTimer timer : timers)
        timer.cancel();
    _allEventTimers.clear();
    return QuestManager.getInstance().removeQuest(this);
  }

  public ScriptManager<?> getScriptManager()
  {
    return QuestManager.getInstance();
  }

  public String onSkillUse(L2NpcInstance npc, L2PcInstance caster, L2Skill skill)
  {
    return null;
  }

  public final boolean notifySkillUse(L2NpcInstance npc, L2PcInstance caster, L2Skill skill) {
    String res = null;
    try { res = onSkillUse(npc, caster, skill); } catch (Exception e) { return showError(caster, e); }
    return showResult(caster, res);
  }

  public class DeSpawnScheduleTimerTask
    implements Runnable
  {
    L2NpcInstance _npc = null;

    public DeSpawnScheduleTimerTask(L2NpcInstance npc)
    {
      _npc = npc;
    }

    public void run()
    {
      _npc.onDecay();
    }
  }

  public class tmpOnAggroEnter
    implements Runnable
  {
    private L2NpcInstance _npc;
    private L2PcInstance _pc;
    private boolean _isPet;

    public tmpOnAggroEnter(L2NpcInstance npc, L2PcInstance pc, boolean isPet)
    {
      _npc = npc;
      _pc = pc;
      _isPet = isPet;
    }

    public void run()
    {
      String res = null;
      try
      {
        res = onAggroRangeEnter(_npc, _pc, _isPet);
      }
      catch (Exception e)
      {
        showError(_pc, e);
      }
      showResult(_pc, res);
    }
  }

  public class tmpOnSkillSee
    implements Runnable
  {
    private L2NpcInstance _npc;
    private L2PcInstance _caster;
    private L2Skill _skill;
    private L2Object[] _targets;
    private boolean _isPet;

    public tmpOnSkillSee(L2NpcInstance npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
    {
      _npc = npc;
      _caster = caster;
      _skill = skill;
      _targets = targets;
      _isPet = isPet;
    }

    public void run()
    {
      String res = null;
      try
      {
        res = onSkillSee(_npc, _caster, _skill, _targets, _isPet);
      }
      catch (Exception e)
      {
        showError(_caster, e);
      }
      showResult(_caster, res);
    }
  }

  public static enum QuestEventType
  {
    ON_FIRST_TALK(false), 
    QUEST_START(true), 
    ON_TALK(true), 
    ON_ATTACK(true), 
    ON_KILL(true), 
    ON_SPAWN(true), 
    ON_SKILL_SEE(true), 
    ON_FACTION_CALL(true), 
    ON_AGGRO_RANGE_ENTER(true), 
    ON_SPELL_FINISHED(true);

    private boolean _allowMultipleRegistration;

    private QuestEventType(boolean allowMultipleRegistration) {
      _allowMultipleRegistration = allowMultipleRegistration;
    }

    public boolean isMultipleRegistrationAllowed()
    {
      return _allowMultipleRegistration;
    }
  }
}
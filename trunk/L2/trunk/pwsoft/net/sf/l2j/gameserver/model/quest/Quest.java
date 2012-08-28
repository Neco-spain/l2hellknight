package net.sf.l2j.gameserver.model.quest;

import java.io.PrintStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastTable;
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
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import net.sf.l2j.util.Rnd;
import net.sf.l2j.util.log.AbstractLogger;

public abstract class Quest
{
  protected static final Logger _log = AbstractLogger.getLogger(Quest.class.getName());

  private static Map<String, Quest> _allEventsS = new FastMap();

  private static Map<String, FastList<QuestTimer>> _allEventTimers = new FastMap();
  public static String SOUND_ITEMGET = "ItemSound.quest_itemget";
  public static String SOUND_ACCEPT = "ItemSound.quest_accept";
  public static String SOUND_MIDDLE = "ItemSound.quest_middle";
  public static String SOUND_FINISH = "ItemSound.quest_finish";
  public static String SOUND_GIVEUP = "ItemSound.quest_giveup";
  public static String SOUND_TUTORIAL = "ItemSound.quest_tutorial";
  public static String SOUND_JACKPOT = "ItemSound.quest_jackpot";
  public static String SOUND_HORROR2 = "SkillSound5.horror_02";
  public static String SOUND_BEFORE_BATTLE = "Itemsound.quest_before_battle";
  public static String SOUND_FANFARE_MIDDLE = "ItemSound.quest_fanfare_middle";
  public static String SOUND_FANFARE2 = "ItemSound.quest_fanfare_2";
  public static String SOUND_BROKEN_KEY = "ItemSound2.broken_key";
  public static String SOUND_ENCHANT_SUCESS = "ItemSound3.sys_enchant_sucess";
  public static String SOUND_ENCHANT_FAILED = "ItemSound3.sys_enchant_failed";
  public static String SOUND_ED_CHIMES05 = "AmdSound.ed_chimes_05";
  public static String SOUND_ARMOR_WOOD_3 = "ItemSound.armor_wood_3";
  public static String SOUND_ITEM_DROP_EQUIP_ARMOR_CLOTH = "ItemSound.item_drop_equip_armor_cloth";
  private final int _questId;
  private final String _name;
  private final String _prefixPath;
  private final String _descr;
  private State _initialState;
  private Map<String, State> _states;
  private FastList<Integer> _questItemIds;

  public static Collection<Quest> findAllEvents()
  {
    return _allEventsS.values();
  }

  public Quest(int questId, String name, String descr)
  {
    _questId = questId;
    _name = name;
    _descr = descr;
    _states = new FastMap();

    StringBuffer temp = new StringBuffer(getClass().getCanonicalName());
    temp.delete(0, temp.indexOf(".jscript.") + 9);
    temp.delete(temp.indexOf(getClass().getSimpleName()), temp.length());
    _prefixPath = temp.toString();
    if (questId != 0)
      QuestManager.getInstance().addQuest(this);
    else {
      _allEventsS.put(name, this);
    }
    init_LoadGlobalData();
  }

  public Quest(int questId, String name, String descr, int ex) {
    _questId = questId;
    _name = name;
    _descr = descr;
    _states = new FastMap();
    _prefixPath = "no";

    QuestManager.getInstance().addQuest(this);
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

  public void setInitialState(State state)
  {
    _initialState = state;
  }

  public QuestState newQuestState(L2PcInstance player)
  {
    QuestState qs = new QuestState(this, player, getInitialState(), false);
    createQuestInDb(qs);
    return qs;
  }

  public State getInitialState()
  {
    return _initialState;
  }

  public String getName()
  {
    return _name;
  }

  public String getPrefixPath()
  {
    return _prefixPath;
  }

  public String getDescr()
  {
    return _descr;
  }

  public State addState(State state)
  {
    _states.put(state.getName(), state);
    return state;
  }

  public void startQuestTimer(String name, long time, L2NpcInstance npc, L2PcInstance player)
  {
    FastList timers = getQuestTimers(name);

    if (timers == null) {
      timers = new FastList();
      timers.add(new QuestTimer(this, name, time, npc, player));
      _allEventTimers.put(name, timers);
    }
    else if (getQuestTimer(name, npc, player) == null) {
      timers.add(new QuestTimer(this, name, time, npc, player));
    }
  }

  public QuestTimer getQuestTimer(String name, L2NpcInstance npc, L2PcInstance player)
  {
    if (_allEventTimers.get(name) == null) {
      return null;
    }
    for (QuestTimer timer : (FastList)_allEventTimers.get(name)) {
      if (timer.isMatch(this, name, npc, player)) {
        return timer;
      }
    }
    return null;
  }

  public FastList<QuestTimer> getQuestTimers(String name) {
    return (FastList)_allEventTimers.get(name);
  }

  public void cancelQuestTimer(String name, L2NpcInstance npc, L2PcInstance player) {
    QuestTimer timer = getQuestTimer(name, npc, player);
    if (timer != null)
      timer.cancel();
  }

  public void removeQuestTimer(QuestTimer timer)
  {
    if (timer == null) {
      return;
    }
    FastList timers = getQuestTimers(timer.getName());
    if (timers == null) {
      return;
    }
    timers.remove(timer);
  }

  public final boolean notifySpawn(L2NpcInstance npc)
  {
    try {
      onSpawn(npc);
    } catch (Exception e) {
      System.out.println("((((((" + e);
    }
    return true;
  }

  public final boolean notifyFocus(L2NpcInstance npc, L2PcInstance attacker) {
    try {
      onFocus(npc, attacker);
    } catch (Exception e) {
      System.out.println("((((" + e);
    }
    return true;
  }

  public final boolean notifyAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet) {
    String res = null;
    try {
      res = onAttack(npc, attacker, damage, isPet); } catch (Exception e) {
    }
    return showResult(attacker, res);
  }

  public final boolean notifyDeath(L2Character killer, L2Character victim, QuestState qs) {
    String res = null;
    try {
      res = onDeath(killer, victim, qs);
    } catch (Exception e) {
      return showError(qs.getPlayer(), e);
    }
    return showResult(qs.getPlayer(), res);
  }

  public final boolean notifyEvent(String event, L2NpcInstance npc, L2PcInstance player) {
    String res = null;
    try {
      res = onAdvEvent(event, npc, player);
    }
    catch (Exception e) {
      return false;
    }
    return showResult(player, res);
  }

  public final boolean notifyKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet) {
    String res = null;
    try {
      res = onKill(npc, killer, isPet);
    } catch (Exception e) {
      return showError(killer, e);
    }
    return showResult(killer, res);
  }

  public final boolean notifyTalk(L2NpcInstance npc, QuestState qs) {
    String res = null;
    try {
      if ((!Config.ALLOW_CURSED_QUESTS) && (qs.getPlayer().isCursedWeaponEquiped())) {
        return showResult(qs.getPlayer(), "<html><body>\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u043F\u0440\u043E\u0434\u043E\u043B\u0436\u0430\u0442\u044C \u043A\u0432\u0435\u0441\u0442 \u0441 \u043F\u0440\u043E\u043A\u043B\u044F\u0442\u044B\u043C \u043E\u0440\u0443\u0436\u0438\u0435\u043C.</body></html>");
      }

      res = onTalk(npc, qs.getPlayer());
    } catch (Exception e) {
      return showError(qs.getPlayer(), e);
    }
    qs.getPlayer().setLastQuestNpcObject(npc.getObjectId());
    return showResult(qs.getPlayer(), res);
  }

  public final boolean notifyFirstTalk(L2NpcInstance npc, L2PcInstance player)
  {
    if ((!Config.ALLOW_CURSED_QUESTS) && (player.isCursedWeaponEquiped())) {
      return showResult(player, "<html><body>\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u043F\u0440\u043E\u0434\u043E\u043B\u0436\u0430\u0442\u044C \u043A\u0432\u0435\u0441\u0442 \u0441 \u043F\u0440\u043E\u043A\u043B\u044F\u0442\u044B\u043C \u043E\u0440\u0443\u0436\u0438\u0435\u043C.</body></html>");
    }

    String res = null;
    try {
      res = onFirstTalk(npc, player);
    } catch (Exception e) {
      return showError(player, e);
    }
    player.setLastQuestNpcObject(npc.getObjectId());

    if ((res != null) && (res.length() > 0)) {
      return showResult(player, res);
    }
    npc.showChatWindow(player);
    return true;
  }

  public final boolean notifySkillUse(L2NpcInstance npc, L2PcInstance caster, L2Skill skill) {
    String res = null;
    try {
      res = onSkillUse(npc, caster, skill);
    } catch (Exception e) {
      return showError(caster, e);
    }
    return showResult(caster, res);
  }

  public String onAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
  {
    return null;
  }

  public String onSpawn(L2NpcInstance npc) {
    return null;
  }

  public String onFocus(L2NpcInstance npc, L2PcInstance attacker) {
    return null;
  }

  public String onDeath(L2Character killer, L2Character victim, QuestState qs) {
    if (killer.isL2Npc()) {
      return onAdvEvent("", (L2NpcInstance)killer, qs.getPlayer());
    }
    return onAdvEvent("", null, qs.getPlayer());
  }

  public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
  {
    QuestState qs = player.getQuestState(getName());
    if (qs != null) {
      return onEvent(event, qs);
    }

    return null;
  }

  public String onEvent(String event, QuestState qs) {
    return null;
  }

  public String onKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet) {
    return null;
  }

  public String onTalk(L2NpcInstance npc, L2PcInstance talker) {
    return null;
  }

  public String onFirstTalk(L2NpcInstance npc, L2PcInstance player) {
    return null;
  }

  public String onSkillUse(L2NpcInstance npc, L2PcInstance caster, L2Skill skill) {
    return null;
  }

  private boolean showError(L2PcInstance player, Throwable t)
  {
    return false;
  }

  private boolean showResult(L2PcInstance player, String res)
  {
    if (res == null) {
      return true;
    }
    if (res.endsWith(".htm")) {
      showHtmlFile(player, res);
    } else if (res.startsWith("<html>")) {
      NpcHtmlMessage npcReply = NpcHtmlMessage.id(5);
      npcReply.setHtml(res);
      player.sendPacket(npcReply);
    } else {
      player.sendPacket(SystemMessage.id(SystemMessageId.S1_S2).addString(res));
    }
    return false;
  }

  public static final void playerEnter(L2PcInstance player)
  {
    Connect con = null;
    PreparedStatement statement = null;
    PreparedStatement invalidQuestData = null;
    PreparedStatement invalidQuestDataVar = null;
    ResultSet rs = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      invalidQuestData = con.prepareStatement("DELETE FROM character_quests WHERE char_id=? and name=?");
      invalidQuestDataVar = con.prepareStatement("delete FROM character_quests WHERE char_id=? and name=? and var=?");

      statement = con.prepareStatement("SELECT name,value FROM character_quests WHERE char_id=? AND var=?");
      statement.setInt(1, player.getObjectId());
      statement.setString(2, "<state>");
      rs = statement.executeQuery();
      while (rs.next())
      {
        String questId = rs.getString("name");
        String stateId = rs.getString("value");

        Quest q = QuestManager.getInstance().getQuest(questId);
        if (q == null) {
          _log.finer("Unknown quest " + questId + " for player " + player.getName());
          if (Config.AUTODELETE_INVALID_QUEST_DATA) {
            invalidQuestData.setInt(1, player.getObjectId());
            invalidQuestData.setString(2, questId);
            invalidQuestData.executeUpdate(); continue;
          }

        }

        boolean completed = false;
        if (stateId.equals("Completed")) {
          completed = true;
        }

        State state = (State)q._states.get(stateId);
        if (state == null) {
          _log.finer("Unknown state in quest " + questId + " for player " + player.getName());
          if (Config.AUTODELETE_INVALID_QUEST_DATA) {
            invalidQuestData.setInt(1, player.getObjectId());
            invalidQuestData.setString(2, questId);
            invalidQuestData.executeUpdate(); continue;
          }

        }

        new QuestState(q, player, state, completed);
      }
      Close.SR(statement, rs);
      Close.S(invalidQuestData);

      statement = con.prepareStatement("SELECT name,var,value FROM character_quests WHERE char_id=?");
      statement.setInt(1, player.getObjectId());
      rs = statement.executeQuery();
      while (rs.next()) {
        String questId = rs.getString("name");
        String var = rs.getString("var");
        String value = rs.getString("value");

        QuestState qs = player.getQuestState(questId);
        if (qs == null) {
          _log.finer("Lost variable " + var + " in quest " + questId + " for player " + player.getName());
          if (Config.AUTODELETE_INVALID_QUEST_DATA) {
            invalidQuestDataVar.setInt(1, player.getObjectId());
            invalidQuestDataVar.setString(2, questId);
            invalidQuestDataVar.setString(3, var);
            invalidQuestDataVar.executeUpdate(); continue;
          }

        }

        qs.setInternal(var, value);
      }
      Close.SR(statement, rs);
      Close.S(invalidQuestDataVar);
    }
    catch (Exception e) {
      _log.log(Level.WARNING, "could not insert char quest:", e);
    } finally {
      Close.S(invalidQuestData);
      Close.S(invalidQuestDataVar);
      Close.CSR(con, statement, rs);
    }

    for (String name : _allEventsS.keySet())
      player.processQuestEvent(name, "enter");
  }

  public final void saveGlobalQuestVar(String var, String value)
  {
    Connect con = null;
    PreparedStatement st = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      st = con.prepareStatement("REPLACE INTO quest_global_data (quest_name,var,value) VALUES (?,?,?)");
      st.setString(1, getName());
      st.setString(2, var);
      st.setString(3, value);
      st.executeUpdate();
    } catch (Exception e) {
      _log.log(Level.WARNING, "could not insert global quest variable:", e);
    } finally {
      Close.CS(con, st);
    }
  }

  public final String loadGlobalQuestVar(String var)
  {
    String result = "";
    Connect con = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      st = con.prepareStatement("SELECT value FROM quest_global_data WHERE quest_name = ? AND var = ?");
      st.setString(1, getName());
      st.setString(2, var);
      rs = st.executeQuery();
      if (rs.first())
        result = rs.getString(1);
    }
    catch (Exception e) {
      _log.log(Level.WARNING, "could not load global quest variable:", e);
    } finally {
      Close.CSR(con, st, rs);
    }
    return result;
  }

  public final void deleteGlobalQuestVar(String var)
  {
    Connect con = null;
    PreparedStatement st = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      st = con.prepareStatement("DELETE FROM quest_global_data WHERE quest_name = ? AND var = ?");
      st.setString(1, getName());
      st.setString(2, var);
      st.executeUpdate();
    } catch (Exception e) {
      _log.log(Level.WARNING, "could not delete global quest variable:", e);
    } finally {
      Close.CS(con, st);
    }
  }

  public final void deleteAllGlobalQuestVars()
  {
    Connect con = null;
    PreparedStatement st = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      st = con.prepareStatement("DELETE FROM quest_global_data WHERE quest_name = ?");
      st.setString(1, getName());
      st.executeUpdate();
    } catch (Exception e) {
      _log.log(Level.WARNING, "could not delete global quest variables:", e);
    } finally {
      Close.CS(con, st);
    }
  }

  public static void createQuestVarInDb(QuestState qs, String var, String value)
  {
    Connect con = null;
    PreparedStatement st = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      st = con.prepareStatement("REPLACE INTO character_quests (char_id,name,var,value) VALUES (?,?,?,?)");
      st.setInt(1, qs.getPlayer().getObjectId());
      st.setString(2, qs.getQuestName());
      st.setString(3, var);
      st.setString(4, value);
      st.executeUpdate();
    } catch (Exception e) {
      _log.log(Level.WARNING, "could not insert char quest:", e);
    } finally {
      Close.CS(con, st);
    }
  }

  public static void updateQuestVarInDb(QuestState qs, String var, String value)
  {
    Connect con = null;
    PreparedStatement st = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      st = con.prepareStatement("UPDATE character_quests SET value=? WHERE char_id=? AND name=? AND var = ?");
      st.setString(1, value);
      st.setInt(2, qs.getPlayer().getObjectId());
      st.setString(3, qs.getQuestName());
      st.setString(4, var);
      st.executeUpdate();
    } catch (Exception e) {
      _log.log(Level.WARNING, "could not update char quest:", e);
    } finally {
      Close.CS(con, st);
    }
  }

  public static void deleteQuestVarInDb(QuestState qs, String var)
  {
    Connect con = null;
    PreparedStatement st = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      st = con.prepareStatement("DELETE FROM character_quests WHERE char_id=? AND name=? AND var=?");
      st.setInt(1, qs.getPlayer().getObjectId());
      st.setString(2, qs.getQuestName());
      st.setString(3, var);
      st.executeUpdate();
    } catch (Exception e) {
      _log.log(Level.WARNING, "could not delete char quest:", e);
    } finally {
      Close.CS(con, st);
    }
  }

  public static void deleteQuestInDb(QuestState qs)
  {
    Connect con = null;
    PreparedStatement st = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      st = con.prepareStatement("DELETE FROM character_quests WHERE char_id=? AND name=?");
      st.setInt(1, qs.getPlayer().getObjectId());
      st.setString(2, qs.getQuestName());
      st.executeUpdate();
    } catch (Exception e) {
      _log.log(Level.WARNING, "could not delete char quest:", e);
    } finally {
      Close.CS(con, st);
    }
  }

  public static void createQuestInDb(QuestState qs)
  {
    createQuestVarInDb(qs, "<state>", qs.getStateId());
  }

  public static void updateQuestInDb(QuestState qs)
  {
    String val = qs.getStateId();

    updateQuestVarInDb(qs, "<state>", val);
  }

  public L2NpcTemplate addEventId(int npcId, QuestEventType eventType)
  {
    try
    {
      L2NpcTemplate t = NpcTable.getInstance().getTemplate(npcId);
      if (t != null) {
        t.addQuestEvent(eventType, this);
      }
      return t;
    } catch (Exception e) {
      e.printStackTrace();
    }return null;
  }

  public L2NpcTemplate addStartNpc(int npcId)
  {
    return addEventId(npcId, QuestEventType.QUEST_START);
  }

  public L2NpcTemplate addFirstTalkId(int npcId)
  {
    return addEventId(npcId, QuestEventType.NPC_FIRST_TALK);
  }

  public L2NpcTemplate addAttackId(int attackId)
  {
    return addEventId(attackId, QuestEventType.MOBGOTATTACKED);
  }

  public L2NpcTemplate addSpawnId(int attackId)
  {
    return addEventId(attackId, QuestEventType.ONSPAWN);
  }

  public L2NpcTemplate addFocusId(int attackId)
  {
    return addEventId(attackId, QuestEventType.ONFOCUS);
  }

  public L2NpcTemplate addKillId(int killId)
  {
    return addEventId(killId, QuestEventType.MOBKILLED);
  }

  public L2NpcTemplate addTalkId(int talkId)
  {
    return addEventId(talkId, QuestEventType.QUEST_TALK);
  }

  public L2NpcTemplate addSkillUseId(int npcId)
  {
    return addEventId(npcId, QuestEventType.MOB_TARGETED_BY_SKILL);
  }

  public L2PcInstance getRandomPartyMember(L2PcInstance player)
  {
    if (player == null) {
      return null;
    }
    if ((player.getParty() == null) || (player.getParty().getPartyMembers().size() == 0)) {
      return player;
    }
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

    if ((party == null) || (party.getPartyMembers().size() == 0)) {
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

    for (L2PcInstance partyMember : party.getPartyMembers()) {
      temp = partyMember.getQuestState(getName());
      if ((temp != null) && (temp.get(var) != null) && (((String)temp.get(var)).equalsIgnoreCase(value)) && (partyMember.isInsideRadius(target, 1500, true, false)))
      {
        candidates.add(partyMember);
      }
    }

    if (candidates.size() == 0) {
      return null;
    }

    return (L2PcInstance)candidates.get(Rnd.get(candidates.size()));
  }

  public L2PcInstance getRandomPartyMemberState(L2PcInstance player, State state)
  {
    if (player == null) {
      return null;
    }

    if (state == null) {
      return getRandomPartyMember(player);
    }

    QuestState temp = null;
    L2Party party = player.getParty();

    if ((party == null) || (party.getPartyMembers().size() == 0)) {
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

    for (L2PcInstance partyMember : party.getPartyMembers()) {
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
    if (player == null) {
      return null;
    }

    String questId = getName();

    String directory = getDescr().toLowerCase();
    String content = HtmCache.getInstance().getHtm("data/jscript/" + directory + "/" + questId + "/" + fileName);

    if (content == null) {
      content = HtmCache.getInstance().getHtm("data/jscript/quests/" + questId + "/" + fileName);
    }

    if (content == null) {
      content = HtmCache.getInstance().getHtm("data/scripts/" + directory + "/" + questId + "/" + fileName);
    }

    if (content == null) {
      content = HtmCache.getInstance().getHtm("data/scripts/quests/" + questId + "/" + fileName);
    }

    if ((content != null) && (player.getTarget() != null)) {
      content = content.replaceAll("%objectId%", String.valueOf(player.getTarget().getObjectId()));
      NpcHtmlMessage npcReply = NpcHtmlMessage.id(5);
      npcReply.setHtml(content);
      player.sendPacket(npcReply);
    }

    return content;
  }

  public L2NpcInstance addSpawn(int npcId, L2Character cha)
  {
    return addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), false, 0);
  }

  public L2NpcInstance addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, int despawnDelay) {
    L2NpcInstance result = null;
    try {
      L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
      if (template != null)
      {
        if ((x == 0) && (y == 0)) {
          _log.log(Level.SEVERE, "Failed to adjust bad locks for quest spawn!  Spawn aborted!");
          return null;
        }
        if (randomOffset)
        {
          int offset = Rnd.get(2);
          if (offset == 0) {
            offset = -1;
          }
          offset *= Rnd.get(50, 100);
          x += offset;

          offset = Rnd.get(2);
          if (offset == 0) {
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
        result = spawn.spawnOne();

        if (despawnDelay > 0) {
          ThreadPoolManager.getInstance().scheduleGeneral(new DeSpawnScheduleTimerTask(result), despawnDelay);
        }

        return result;
      }
    } catch (Exception e1) {
      _log.warning("Could not spawn Npc " + npcId);
    }

    return null;
  }

  public void registerItem(int itemId) {
    if (_questItemIds == null) {
      _questItemIds = new FastList();
    }
    _questItemIds.add(Integer.valueOf(itemId));
  }

  public FastList<Integer> getRegisteredItemIds() {
    return _questItemIds;
  }

  public static class DeSpawnScheduleTimerTask
    implements Runnable
  {
    L2NpcInstance _npc = null;

    public DeSpawnScheduleTimerTask(L2NpcInstance npc) {
      _npc = npc;
    }

    public void run() {
      _npc.onDecay();
    }
  }

  public static enum QuestEventType
  {
    NPC_FIRST_TALK(false), 
    QUEST_START(true), 
    QUEST_TALK(true), 
    MOBGOTATTACKED(true), 
    MOBKILLED(true), 
    MOB_TARGETED_BY_SKILL(true), 
    ONSPAWN(true), 
    ONFOCUS(true);

    private boolean _allowMultipleRegistration;

    private QuestEventType(boolean allowMultipleRegistration) {
      _allowMultipleRegistration = allowMultipleRegistration;
    }

    public boolean isMultipleRegistrationAllowed() {
      return _allowMultipleRegistration;
    }
  }
}
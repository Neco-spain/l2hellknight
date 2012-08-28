package l2p.gameserver.model.quest;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.data.htm.HtmCache;
import l2p.gameserver.data.xml.holder.ItemHolder;
import l2p.gameserver.instancemanager.SpawnManager;
import l2p.gameserver.listener.actor.OnDeathListener;
import l2p.gameserver.listener.actor.OnKillListener;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.GameObject;
import l2p.gameserver.model.GameObjectsStorage;
import l2p.gameserver.model.Party;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Spawner;
import l2p.gameserver.model.Summon;
import l2p.gameserver.model.actor.instances.player.Bonus;
import l2p.gameserver.model.base.Element;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.model.items.Warehouse;
import l2p.gameserver.serverpackets.ExShowQuestMark;
import l2p.gameserver.serverpackets.PlaySound;
import l2p.gameserver.serverpackets.PlaySound.Type;
import l2p.gameserver.serverpackets.QuestList;
import l2p.gameserver.serverpackets.SystemMessage2;
import l2p.gameserver.serverpackets.TutorialEnableClientEvent;
import l2p.gameserver.serverpackets.TutorialShowHtml;
import l2p.gameserver.serverpackets.TutorialShowQuestionMark;
import l2p.gameserver.templates.item.ItemTemplate;
import l2p.gameserver.templates.spawn.PeriodOfDay;
import l2p.gameserver.utils.ItemFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class QuestState
{
  private static final Logger _log = LoggerFactory.getLogger(QuestState.class);
  public static final int RESTART_HOUR = 6;
  public static final int RESTART_MINUTES = 30;
  public static final String VAR_COND = "cond";
  public static final QuestState[] EMPTY_ARRAY = new QuestState[0];
  private final Player _player;
  private Quest _quest;
  private int _state;
  private Integer _cond = null;
  private Map<String, String> _vars = new ConcurrentHashMap();
  private Map<String, QuestTimer> _timers = new ConcurrentHashMap();
  private OnKillListener _onKillListener = null;

  public QuestState(Quest quest, Player player, int state)
  {
    _quest = quest;
    _player = player;

    player.setQuestState(this);

    _state = state;
    quest.notifyCreate(this);
  }

  public void addExpAndSp(long exp, long sp)
  {
    Player player = getPlayer();
    if (player == null)
      return;
    if (exp > 0L)
      player.addExpAndSp(()(exp * getRateQuestsReward()), 0L);
    if (sp > 0L)
      player.addExpAndSp(0L, ()(sp * getRateQuestsReward()));
  }

  public void addNotifyOfDeath(Player player, boolean withPet)
  {
    OnDeathListenerImpl listener = new OnDeathListenerImpl();
    player.addListener(listener);
    if (withPet)
    {
      Summon summon = player.getPet();
      if (summon != null)
        summon.addListener(listener);
    }
  }

  public void addPlayerOnKillListener()
  {
    if (_onKillListener != null) {
      throw new IllegalArgumentException("Cant add twice kill listener to player");
    }
    _onKillListener = new PlayerOnKillListenerImpl();
    _player.addListener(_onKillListener);
  }

  public void removePlayerOnKillListener()
  {
    if (_onKillListener != null)
      _player.removeListener(_onKillListener);
  }

  public void addRadar(int x, int y, int z)
  {
    Player player = getPlayer();
    if (player != null)
      player.addRadar(x, y, z);
  }

  public void addRadarWithMap(int x, int y, int z)
  {
    Player player = getPlayer();
    if (player != null)
      player.addRadarWithMap(x, y, z);
  }

  public void exitCurrentQuest(Quest quest)
  {
    Player player = getPlayer();
    exitCurrentQuest(true);
    quest.newQuestState(player, 4);
    QuestState qs = player.getQuestState(quest.getClass());
    qs.setRestartTime();
  }

  public QuestState exitCurrentQuest(boolean repeatable)
  {
    Player player = getPlayer();
    if (player == null) {
      return this;
    }
    removePlayerOnKillListener();

    for (int itemId : _quest.getItems())
    {
      ItemInstance item = player.getInventory().getItemByItemId(itemId);
      if ((item == null) || (itemId == 57))
        continue;
      long count = item.getCount();

      player.getInventory().destroyItemByItemId(itemId, count);
      player.getWarehouse().destroyItemByItemId(itemId, count);
    }

    if (repeatable)
    {
      player.removeQuestState(_quest.getName());
      Quest.deleteQuestInDb(this);
      _vars.clear();
    }
    else
    {
      for (String var : _vars.keySet())
        if (var != null)
          unset(var);
      setState(3);
      Quest.updateQuestInDb(this);
    }
    player.sendPacket(new QuestList(player));
    return this;
  }

  public void abortQuest()
  {
    _quest.onAbort(this);
    exitCurrentQuest(true);
  }

  public String get(String var)
  {
    return (String)_vars.get(var);
  }

  public Map<String, String> getVars()
  {
    return _vars;
  }

  public int getInt(String var)
  {
    int varint = 0;
    try
    {
      String val = get(var);
      if (val == null)
        return 0;
      varint = Integer.parseInt(val);
    }
    catch (Exception e)
    {
      _log.error(getPlayer().getName() + ": variable " + var + " isn't an integer: " + varint, e);
    }
    return varint;
  }

  public int getItemEquipped(int loc)
  {
    return getPlayer().getInventory().getPaperdollItemId(loc);
  }

  public Player getPlayer()
  {
    return _player;
  }

  public Quest getQuest()
  {
    return _quest;
  }

  public boolean checkQuestItemsCount(int[] itemIds)
  {
    Player player = getPlayer();
    if (player == null)
      return false;
    for (int itemId : itemIds)
      if (player.getInventory().getCountOf(itemId) <= 0L)
        return false;
    return true;
  }

  public long getSumQuestItemsCount(int[] itemIds)
  {
    Player player = getPlayer();
    if (player == null)
      return 0L;
    long count = 0L;
    for (int itemId : itemIds)
      count += player.getInventory().getCountOf(itemId);
    return count;
  }

  public long getQuestItemsCount(int itemId)
  {
    Player player = getPlayer();
    return player == null ? 0L : player.getInventory().getCountOf(itemId);
  }

  public long getQuestItemsCount(int[] itemsIds)
  {
    long result = 0L;
    for (int id : itemsIds)
      result += getQuestItemsCount(id);
    return result;
  }

  public boolean haveQuestItem(int itemId, int count)
  {
    return getQuestItemsCount(itemId) >= count;
  }

  public boolean haveQuestItem(int itemId)
  {
    return haveQuestItem(itemId, 1);
  }

  public int getState()
  {
    return _state == 4 ? 1 : _state;
  }

  public String getStateName()
  {
    return Quest.getStateName(_state);
  }

  public void giveItems(int itemId, long count)
  {
    if (itemId == 57)
      giveItems(itemId, count, true);
    else
      giveItems(itemId, count, false);
  }

  public void giveItems(int itemId, long count, boolean rate)
  {
    Player player = getPlayer();
    if (player == null) {
      return;
    }
    if (count <= 0L) {
      count = 1L;
    }
    if (rate) {
      count = ()(count * getRateQuestsReward());
    }
    ItemFunctions.addItem(player, itemId, count, true);
    player.sendChanges();
  }

  public void giveItems(int itemId, long count, Element element, int power)
  {
    Player player = getPlayer();
    if (player == null) {
      return;
    }
    if (count <= 0L) {
      count = 1L;
    }

    ItemTemplate template = ItemHolder.getInstance().getTemplate(itemId);
    if (template == null) {
      return;
    }
    for (int i = 0; i < count; i++)
    {
      ItemInstance item = ItemFunctions.createItem(itemId);

      if (element != Element.NONE) {
        item.setAttributeElement(element, power);
      }

      player.getInventory().addItem(item);
    }

    player.sendPacket(SystemMessage2.obtainItems(template.getItemId(), count, 0));
    player.sendChanges();
  }

  public void dropItem(NpcInstance npc, int itemId, long count)
  {
    Player player = getPlayer();
    if (player == null) {
      return;
    }
    ItemInstance item = ItemFunctions.createItem(itemId);
    item.setCount(count);
    item.dropToTheGround(player, npc);
  }

  public int rollDrop(int count, double calcChance)
  {
    if ((calcChance <= 0.0D) || (count <= 0))
      return 0;
    return rollDrop(count, count, calcChance);
  }

  public int rollDrop(int min, int max, double calcChance)
  {
    if ((calcChance <= 0.0D) || (min <= 0) || (max <= 0))
      return 0;
    int dropmult = 1;
    calcChance *= getRateQuestsDrop();
    if (getQuest().getParty() > 0)
    {
      Player player = getPlayer();
      if (player.getParty() != null)
        calcChance *= Config.ALT_PARTY_BONUS[(player.getParty().getMemberCountInRange(player, Config.ALT_PARTY_DISTRIBUTION_RANGE) - 1)];
    }
    if (calcChance > 100.0D)
    {
      if ((int)Math.ceil(calcChance / 100.0D) <= calcChance / 100.0D)
        calcChance = Math.nextUp(calcChance);
      dropmult = (int)Math.ceil(calcChance / 100.0D);
      calcChance /= dropmult;
    }
    return Rnd.chance(calcChance) ? Rnd.get(min * dropmult, max * dropmult) : 0;
  }

  public double getRateQuestsDrop()
  {
    Player player = getPlayer();
    double Bonus = player == null ? 1.0D : player.getBonus().getQuestDropRate();
    return Config.RATE_QUESTS_DROP * Bonus;
  }

  public double getRateQuestsReward()
  {
    Player player = getPlayer();
    double Bonus = player == null ? 1.0D : player.getBonus().getQuestRewardRate();
    return Config.RATE_QUESTS_REWARD * Bonus;
  }

  public boolean rollAndGive(int itemId, int min, int max, int limit, double calcChance)
  {
    if ((calcChance <= 0.0D) || (min <= 0) || (max <= 0) || (limit <= 0) || (itemId <= 0))
      return false;
    long count = rollDrop(min, max, calcChance);
    if (count > 0L)
    {
      long alreadyCount = getQuestItemsCount(itemId);
      if (alreadyCount + count > limit)
        count = limit - alreadyCount;
      if (count > 0L)
      {
        giveItems(itemId, count, false);
        if (count + alreadyCount < limit) {
          playSound("ItemSound.quest_itemget");
        }
        else {
          playSound("ItemSound.quest_middle");
          return true;
        }
      }
    }
    return false;
  }

  public void rollAndGive(int itemId, int min, int max, double calcChance)
  {
    if ((calcChance <= 0.0D) || (min <= 0) || (max <= 0) || (itemId <= 0))
      return;
    int count = rollDrop(min, max, calcChance);
    if (count > 0)
    {
      giveItems(itemId, count, false);
      playSound("ItemSound.quest_itemget");
    }
  }

  public boolean rollAndGive(int itemId, int count, double calcChance)
  {
    if ((calcChance <= 0.0D) || (count <= 0) || (itemId <= 0))
      return false;
    int countToDrop = rollDrop(count, calcChance);
    if (countToDrop > 0)
    {
      giveItems(itemId, countToDrop, false);
      playSound("ItemSound.quest_itemget");
      return true;
    }
    return false;
  }

  public boolean isCompleted()
  {
    return getState() == 3;
  }

  public boolean isStarted()
  {
    return getState() == 2;
  }

  public boolean isCreated()
  {
    return getState() == 1;
  }

  public void killNpcByObjectId(int _objId)
  {
    NpcInstance npc = GameObjectsStorage.getNpc(_objId);
    if (npc != null)
      npc.doDie(null);
    else
      _log.warn("Attemp to kill object that is not npc in quest " + getQuest().getQuestIntId());
  }

  public String set(String var, String val)
  {
    return set(var, val, true);
  }

  public String set(String var, int intval)
  {
    return set(var, String.valueOf(intval), true);
  }

  public String set(String var, String val, boolean store)
  {
    if (val == null) {
      val = "";
    }
    _vars.put(var, val);

    if (store) {
      Quest.updateQuestVarInDb(this, var, val);
    }
    return val;
  }

  public Object setState(int state)
  {
    Player player = getPlayer();
    if (player == null) {
      return null;
    }
    _state = state;

    if ((getQuest().isVisible()) && (isStarted())) {
      player.sendPacket(new ExShowQuestMark(getQuest().getQuestIntId()));
    }
    Quest.updateQuestInDb(this);
    player.sendPacket(new QuestList(player));
    return Integer.valueOf(state);
  }

  public Object setStateAndNotSave(int state)
  {
    Player player = getPlayer();
    if (player == null) {
      return null;
    }
    _state = state;

    if ((getQuest().isVisible()) && (isStarted())) {
      player.sendPacket(new ExShowQuestMark(getQuest().getQuestIntId()));
    }
    player.sendPacket(new QuestList(player));
    return Integer.valueOf(state);
  }

  public void playSound(String sound)
  {
    Player player = getPlayer();
    if (player != null)
      player.sendPacket(new PlaySound(sound));
  }

  public void playTutorialVoice(String voice)
  {
    Player player = getPlayer();
    if (player != null)
      player.sendPacket(new PlaySound(PlaySound.Type.VOICE, voice, 0, 0, player.getLoc()));
  }

  public void onTutorialClientEvent(int number)
  {
    Player player = getPlayer();
    if (player != null)
      player.sendPacket(new TutorialEnableClientEvent(number));
  }

  public void showQuestionMark(int number)
  {
    Player player = getPlayer();
    if (player != null)
      player.sendPacket(new TutorialShowQuestionMark(number));
  }

  public void showTutorialHTML(String html)
  {
    Player player = getPlayer();
    if (player == null)
      return;
    String text = HtmCache.getInstance().getNotNull("quests/_255_Tutorial/" + html, player);
    player.sendPacket(new TutorialShowHtml(text));
  }

  public void startQuestTimer(String name, long time)
  {
    startQuestTimer(name, time, null);
  }

  public void startQuestTimer(String name, long time, NpcInstance npc)
  {
    QuestTimer timer = new QuestTimer(name, time, npc);
    timer.setQuestState(this);
    QuestTimer oldTimer = (QuestTimer)getTimers().put(name, timer);
    if (oldTimer != null)
      oldTimer.stop();
    timer.start();
  }

  public boolean isRunningQuestTimer(String name)
  {
    return getTimers().get(name) != null;
  }

  public boolean cancelQuestTimer(String name)
  {
    QuestTimer timer = removeQuestTimer(name);
    if (timer != null)
      timer.stop();
    return timer != null;
  }

  QuestTimer removeQuestTimer(String name)
  {
    QuestTimer timer = (QuestTimer)getTimers().remove(name);
    if (timer != null)
      timer.setQuestState(null);
    return timer;
  }

  public void pauseQuestTimers()
  {
    getQuest().pauseQuestTimers(this);
  }

  public void stopQuestTimers()
  {
    for (QuestTimer timer : getTimers().values())
    {
      timer.setQuestState(null);
      timer.stop();
    }
    _timers.clear();
  }

  public void resumeQuestTimers()
  {
    getQuest().resumeQuestTimers(this);
  }

  Map<String, QuestTimer> getTimers()
  {
    return _timers;
  }

  public long takeItems(int itemId, long count)
  {
    Player player = getPlayer();
    if (player == null) {
      return 0L;
    }

    ItemInstance item = player.getInventory().getItemByItemId(itemId);
    if (item == null) {
      return 0L;
    }
    if ((count < 0L) || (count > item.getCount())) {
      count = item.getCount();
    }

    player.getInventory().destroyItemByItemId(itemId, count);

    player.sendPacket(SystemMessage2.removeItems(itemId, count));

    return count;
  }

  public long takeAllItems(int itemId)
  {
    return takeItems(itemId, -1L);
  }

  public long takeAllItems(int[] itemsIds)
  {
    long result = 0L;
    for (int id : itemsIds)
      result += takeAllItems(id);
    return result;
  }

  public long takeAllItems(Collection<Integer> itemsIds)
  {
    long result = 0L;
    for (Iterator i$ = itemsIds.iterator(); i$.hasNext(); ) { int id = ((Integer)i$.next()).intValue();
      result += takeAllItems(id); }
    return result;
  }

  public String unset(String var)
  {
    if (var == null)
      return null;
    String old = (String)_vars.remove(var);
    if (old != null)
      Quest.deleteQuestVarInDb(this, var);
    return old;
  }

  private boolean checkPartyMember(Player member, int state, int maxrange, GameObject rangefrom)
  {
    if (member == null)
      return false;
    if ((rangefrom != null) && (maxrange > 0) && (!member.isInRange(rangefrom, maxrange)))
      return false;
    QuestState qs = member.getQuestState(getQuest().getName());

    return (qs != null) && (qs.getState() == state);
  }

  public List<Player> getPartyMembers(int state, int maxrange, GameObject rangefrom)
  {
    List result = new ArrayList();
    Party party = getPlayer().getParty();
    if (party == null)
    {
      if (checkPartyMember(getPlayer(), state, maxrange, rangefrom))
        result.add(getPlayer());
      return result;
    }

    for (Player _member : party.getPartyMembers()) {
      if (checkPartyMember(_member, state, maxrange, rangefrom))
        result.add(getPlayer());
    }
    return result;
  }

  public Player getRandomPartyMember(int state, int maxrangefromplayer)
  {
    return getRandomPartyMember(state, maxrangefromplayer, getPlayer());
  }

  public Player getRandomPartyMember(int state, int maxrange, GameObject rangefrom)
  {
    List list = getPartyMembers(state, maxrange, rangefrom);
    if (list.size() == 0)
      return null;
    return (Player)list.get(Rnd.get(list.size()));
  }

  public NpcInstance addSpawn(int npcId)
  {
    return addSpawn(npcId, getPlayer().getX(), getPlayer().getY(), getPlayer().getZ(), 0, 0, 0);
  }

  public NpcInstance addSpawn(int npcId, int despawnDelay)
  {
    return addSpawn(npcId, getPlayer().getX(), getPlayer().getY(), getPlayer().getZ(), 0, 0, despawnDelay);
  }

  public NpcInstance addSpawn(int npcId, int x, int y, int z)
  {
    return addSpawn(npcId, x, y, z, 0, 0, 0);
  }

  public NpcInstance addSpawn(int npcId, int x, int y, int z, int despawnDelay)
  {
    return addSpawn(npcId, x, y, z, 0, 0, despawnDelay);
  }

  public NpcInstance addSpawn(int npcId, int x, int y, int z, int heading, int randomOffset, int despawnDelay)
  {
    return getQuest().addSpawn(npcId, x, y, z, heading, randomOffset, despawnDelay);
  }

  public NpcInstance findTemplate(int npcId)
  {
    for (Spawner spawn : SpawnManager.getInstance().getSpawners(PeriodOfDay.NONE.name()))
      if ((spawn != null) && (spawn.getCurrentNpcId() == npcId))
        return spawn.getLastSpawn();
    return null;
  }

  public int calculateLevelDiffForDrop(int mobLevel, int player)
  {
    if (!Config.DEEPBLUE_DROP_RULES)
      return 0;
    return Math.max(player - mobLevel - Config.DEEPBLUE_DROP_MAXDIFF, 0);
  }

  public int getCond()
  {
    if (_cond == null)
    {
      int val = getInt("cond");
      if ((val & 0x80000000) != 0)
      {
        val &= 2147483647;
        for (int i = 1; i < 32; i++)
        {
          val >>= 1;
          if (val != 0)
            continue;
          val = i;
          break;
        }
      }

      _cond = Integer.valueOf(val);
    }

    return _cond.intValue();
  }

  public String setCond(int newCond)
  {
    return setCond(newCond, true);
  }

  public String setCond(int newCond, boolean store)
  {
    if (newCond == getCond()) {
      return String.valueOf(newCond);
    }
    int oldCond = getInt("cond");
    _cond = Integer.valueOf(newCond);

    if ((oldCond & 0x80000000) != 0)
    {
      if (newCond > 2)
      {
        oldCond &= (0x80000001 | (1 << newCond) - 1);
        newCond = oldCond | 1 << newCond - 1;
      }

    }
    else if (newCond > 2) {
      newCond = 0x80000001 | 1 << newCond - 1 | (1 << oldCond) - 1;
    }

    String sVal = String.valueOf(newCond);
    String result = set("cond", sVal, false);
    if (store) {
      Quest.updateQuestVarInDb(this, "cond", sVal);
    }
    Player player = getPlayer();
    if (player != null)
    {
      player.sendPacket(new QuestList(player));
      if ((newCond != 0) && (getQuest().isVisible()) && (isStarted()))
        player.sendPacket(new ExShowQuestMark(getQuest().getQuestIntId()));
    }
    return result;
  }

  public void setRestartTime()
  {
    Calendar reDo = Calendar.getInstance();
    if (reDo.get(11) >= 6)
      reDo.add(5, 1);
    reDo.set(11, 6);
    reDo.set(12, 30);
    set("restartTime", String.valueOf(reDo.getTimeInMillis()));
  }

  public boolean isNowAvailable()
  {
    String val = get("restartTime");
    if (val == null) {
      return true;
    }
    long restartTime = Long.parseLong(val);
    return restartTime <= System.currentTimeMillis();
  }

  public class PlayerOnKillListenerImpl
    implements OnKillListener
  {
    public PlayerOnKillListenerImpl()
    {
    }

    public void onKill(Creature actor, Creature victim)
    {
      if (!victim.isPlayer()) {
        return;
      }
      Player actorPlayer = (Player)actor;
      List players = null;
      switch (_quest.getParty())
      {
      case 0:
        players = Collections.singletonList(actorPlayer);
        break;
      case 2:
        if (actorPlayer.getParty() == null) {
          players = Collections.singletonList(actorPlayer);
        }
        else {
          players = new ArrayList(actorPlayer.getParty().getMemberCount());
          for (Player $member : actorPlayer.getParty().getPartyMembers())
            if ($member.isInRange(actorPlayer, 200L))
              players.add($member);
        }
        break;
      default:
        players = Collections.emptyList();
      }

      for (Player player : players)
      {
        QuestState questState = player.getQuestState(_quest.getClass());
        if ((questState != null) && (!questState.isCompleted()))
          _quest.notifyKill((Player)victim, questState);
      }
    }

    public boolean ignorePetOrSummon()
    {
      return true;
    }
  }

  public class OnDeathListenerImpl
    implements OnDeathListener
  {
    public OnDeathListenerImpl()
    {
    }

    public void onDeath(Creature actor, Creature killer)
    {
      Player player = actor.getPlayer();
      if (player == null) {
        return;
      }
      player.removeListener(this);

      _quest.notifyDeath(killer, actor, QuestState.this);
    }
  }
}
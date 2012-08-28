package net.sf.l2j.gameserver.model.quest;

import java.util.Map;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastList.Node;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.instancemanager.QuestManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Radar;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExShowQuestMark;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.QuestList;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.util.Rnd;
import net.sf.l2j.util.log.AbstractLogger;

public final class QuestState
{
  protected static final Logger _log = AbstractLogger.getLogger(Quest.class.getName());
  private final String _questName;
  private final L2PcInstance _player;
  private State _state;
  private boolean _isCompleted;
  private Map<String, String> _vars;
  private boolean _isExitQuestOnCleanUp = false;

  QuestState(Quest quest, L2PcInstance player, State state, boolean completed)
  {
    _questName = quest.getName();
    _player = player;

    getPlayer().setQuestState(this);

    _isCompleted = completed;

    _state = state;
  }

  public String getQuestName()
  {
    return _questName;
  }

  public Quest getQuest()
  {
    return QuestManager.getInstance().getQuest(_questName);
  }

  public L2PcInstance getPlayer()
  {
    return _player;
  }

  public State getState()
  {
    return _state;
  }

  public boolean isCompleted()
  {
    return _isCompleted;
  }

  public boolean isStarted()
  {
    return (!getStateId().equals("Start")) && (!getStateId().equals("Completed"));
  }

  public Object setState(State state)
  {
    _state = state;

    if (state == null) return null;

    if (getStateId().equals("Completed")) _isCompleted = true; else {
      _isCompleted = false;
    }
    Quest.updateQuestInDb(this);
    QuestList ql = new QuestList();

    getPlayer().sendPacket(ql);
    return state;
  }

  public String getStateId()
  {
    return getState().getName();
  }

  String setInternal(String var, String val)
  {
    if (_vars == null) {
      _vars = new FastMap();
    }
    if (val == null) {
      val = "";
    }
    _vars.put(var, val);
    return val;
  }

  public String set(String var, String val)
  {
    if (_vars == null) {
      _vars = new FastMap();
    }
    if (val == null) {
      val = "";
    }

    String old = (String)_vars.put(var, val);

    if (old != null)
      Quest.updateQuestVarInDb(this, var, val);
    else {
      Quest.createQuestVarInDb(this, var, val);
    }
    if (var.equals("cond"))
    {
      try
      {
        int previousVal = 0;
        try
        {
          previousVal = Integer.parseInt(old);
        }
        catch (Exception ex)
        {
          previousVal = 0;
        }
        setCond(Integer.parseInt(val), previousVal);
      }
      catch (Exception e)
      {
        _log.finer(getPlayer().getName() + ", " + getQuestName() + " cond [" + val + "] is not an integer.  Value stored, but no packet was sent: " + e);
      }
    }

    return val;
  }

  private void setCond(int cond, int old)
  {
    int completedStateFlags = 0;

    if (cond == old) {
      return;
    }

    if ((cond < 3) || (cond > 31))
    {
      unset("__compltdStateFlags");
    }
    else {
      completedStateFlags = getInt("__compltdStateFlags");
    }

    if (completedStateFlags == 0)
    {
      if (cond > old + 1)
      {
        completedStateFlags = -2147483647;

        completedStateFlags |= (1 << old) - 1;

        completedStateFlags |= 1 << cond - 1;
        set("__compltdStateFlags", String.valueOf(completedStateFlags));
      }

    }
    else if (cond < old)
    {
      completedStateFlags &= (1 << cond) - 1;

      if (completedStateFlags == (1 << cond) - 1) {
        unset("__compltdStateFlags");
      }
      else
      {
        completedStateFlags |= -2147483647;
        set("__compltdStateFlags", String.valueOf(completedStateFlags));
      }

    }
    else
    {
      completedStateFlags |= 1 << cond - 1;
      set("__compltdStateFlags", String.valueOf(completedStateFlags));
    }

    QuestList ql = new QuestList();
    getPlayer().sendPacket(ql);

    int questId = getQuest().getQuestIntId();
    if ((questId > 0) && (questId < 999) && (cond > 0))
      getPlayer().sendPacket(new ExShowQuestMark(questId));
  }

  public String unset(String var)
  {
    if (_vars == null) {
      return null;
    }
    String old = (String)_vars.remove(var);

    if (old != null) {
      Quest.deleteQuestVarInDb(this, var);
    }
    return old;
  }

  public Object get(String var)
  {
    if (_vars == null) {
      return null;
    }
    return _vars.get(var);
  }

  public int getInt(String var)
  {
    int varint = 0;
    try
    {
      varint = Integer.parseInt((String)_vars.get(var));
    }
    catch (Exception e)
    {
      _log.finer(getPlayer().getName() + ": variable " + var + " isn't an integer: " + varint + e);
    }

    return varint;
  }

  public void addNotifyOfDeath(L2Character character)
  {
    if (character == null) {
      return;
    }
    character.addNotifyQuestOfDeath(this);
  }

  public int getQuestItemsCount(int itemId)
  {
    int count = 0;

    for (L2ItemInstance item : getPlayer().getInventory().getItems()) {
      if (item.getItemId() == itemId)
        count += item.getCount();
    }
    return count;
  }

  public int getEnchantLevel(int itemId)
  {
    L2ItemInstance enchanteditem = getPlayer().getInventory().getItemByItemId(itemId);

    if (enchanteditem == null) {
      return 0;
    }
    return enchanteditem.getEnchantLevel();
  }

  public void giveItems(int itemId, int count)
  {
    giveItems(itemId, count, 0);
  }

  public void giveItems(int itemId, int count, int enchantlevel)
  {
    if (count <= 0) {
      return;
    }

    int questId = getQuest().getQuestIntId();

    if ((itemId == 57) && ((questId < 217) || (questId > 233)) && ((questId < 401) || (questId > 418))) {
      count = (int)(count * Config.RATE_QUESTS_REWARD);
    }

    L2ItemInstance item = getPlayer().getInventory().addItem("Quest", itemId, count, getPlayer(), getPlayer().getTarget());

    if (item == null)
      return;
    if (enchantlevel > 0) {
      item.setEnchantLevel(enchantlevel);
    }

    if (itemId == 57) {
      getPlayer().sendPacket(SystemMessage.id(SystemMessageId.EARNED_ADENA).addNumber(count));
    }
    else
    {
      SystemMessage smsg = null;
      if (count > 1)
        smsg = SystemMessage.id(SystemMessageId.EARNED_S2_S1_S).addItemName(item.getItemId()).addNumber(count);
      else {
        smsg = SystemMessage.id(SystemMessageId.EARNED_ITEM).addItemName(item.getItemId());
      }
      getPlayer().sendPacket(smsg);
      smsg = null;
    }
    getPlayer().sendItems(false);

    getPlayer().sendChanges();
  }

  public boolean dropQuestItems(int itemId, int count, int neededCount, int dropChance, boolean sound)
  {
    return dropQuestItems(itemId, count, count, neededCount, dropChance, sound);
  }

  public boolean dropQuestItems(int itemId, int minCount, int maxCount, int neededCount, int dropChance, boolean sound)
  {
    dropChance = (int)(dropChance * (Config.RATE_DROP_QUEST / (getPlayer().getParty() != null ? getPlayer().getParty().getMemberCount() : 1)));
    int currentCount = getQuestItemsCount(itemId);

    if ((neededCount > 0) && (currentCount >= neededCount)) {
      return true;
    }
    if (currentCount >= neededCount) {
      return true;
    }
    int itemCount = 0;
    int random = Rnd.get(1000000);

    while (random < dropChance)
    {
      if (minCount < maxCount)
        itemCount += Rnd.get(minCount, maxCount);
      else if (minCount == maxCount)
        itemCount += minCount;
      else {
        itemCount++;
      }

      dropChance -= 1000000;
    }

    if (itemCount > 0)
    {
      if ((neededCount > 0) && (currentCount + itemCount > neededCount)) {
        itemCount = neededCount - currentCount;
      }

      if (!getPlayer().getInventory().validateCapacityByItemId(itemId)) {
        return false;
      }

      getPlayer().addItem("Quest", itemId, itemCount, getPlayer().getTarget(), true);

      if (sound) {
        playSound(currentCount + itemCount < neededCount ? "Itemsound.quest_itemget" : "Itemsound.quest_middle");
      }
    }
    return (neededCount > 0) && (currentCount + itemCount >= neededCount);
  }

  public void addRadar(int x, int y, int z)
  {
    getPlayer().getRadar().addMarker(x, y, z);
  }

  public void removeRadar(int x, int y, int z)
  {
    getPlayer().getRadar().removeMarker(x, y, z);
  }

  public void clearRadar()
  {
    getPlayer().getRadar().removeAllMarkers();
  }

  public void takeItems(int itemId, int count)
  {
    L2ItemInstance item = getPlayer().getInventory().getItemByItemId(itemId);

    if (item == null) {
      return;
    }

    if ((count < 0) || (count > item.getCount())) {
      count = item.getCount();
    }

    if (itemId == 57)
      getPlayer().reduceAdena("Quest", count, getPlayer(), true);
    else
      getPlayer().destroyItemByItemId("Quest", itemId, count, getPlayer(), true);
  }

  public void playSound(String sound)
  {
    getPlayer().sendPacket(new PlaySound(sound));
  }

  public void addExpAndSp(int exp, int sp)
  {
    getPlayer().addExpAndSp((int)getPlayer().calcStat(Stats.EXPSP_RATE, exp * Config.RATE_QUESTS_REWARD, null, null), (int)getPlayer().calcStat(Stats.EXPSP_RATE, sp * Config.RATE_QUESTS_REWARD, null, null));
  }

  public int getRandom(int max)
  {
    return Rnd.get(max);
  }

  public int getItemEquipped(int loc)
  {
    return getPlayer().getInventory().getPaperdollItemId(loc);
  }

  public int getGameTicks()
  {
    return GameTimeController.getGameTicks();
  }

  public final boolean isExitQuestOnCleanUp()
  {
    return _isExitQuestOnCleanUp;
  }

  public void setIsExitQuestOnCleanUp(boolean isExitQuestOnCleanUp)
  {
    _isExitQuestOnCleanUp = isExitQuestOnCleanUp;
  }

  public void startQuestTimer(String name, long time)
  {
    getQuest().startQuestTimer(name, time, null, getPlayer());
  }

  public void startQuestTimer(String name, long time, L2NpcInstance npc)
  {
    getQuest().startQuestTimer(name, time, npc, getPlayer());
  }

  public final QuestTimer getQuestTimer(String name)
  {
    return getQuest().getQuestTimer(name, null, getPlayer());
  }

  public L2NpcInstance addSpawn(int npcId)
  {
    return addSpawn(npcId, getPlayer().getX(), getPlayer().getY(), getPlayer().getZ(), 0, false, 0);
  }

  public L2NpcInstance addSpawn(int npcId, int despawnDelay)
  {
    return addSpawn(npcId, getPlayer().getX(), getPlayer().getY(), getPlayer().getZ(), 0, false, despawnDelay);
  }

  public L2NpcInstance addSpawn(int npcId, int x, int y, int z)
  {
    return addSpawn(npcId, x, y, z, 0, false, 0);
  }

  public L2NpcInstance addSpawn(int npcId, L2Character cha)
  {
    return addSpawn(npcId, cha, true, 0);
  }

  public L2NpcInstance addSpawn(int npcId, L2Character cha, int despawnDelay)
  {
    return addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), true, despawnDelay);
  }

  public L2NpcInstance addSpawn(int npcId, int x, int y, int z, int despawnDelay)
  {
    return addSpawn(npcId, x, y, z, 0, false, despawnDelay);
  }

  public L2NpcInstance addSpawn(int npcId, L2Character cha, boolean randomOffset, int despawnDelay)
  {
    return addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), randomOffset, despawnDelay);
  }

  public L2NpcInstance addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, int despawnDelay)
  {
    return getQuest().addSpawn(npcId, x, y, z, heading, randomOffset, despawnDelay);
  }

  public String showHtmlFile(String fileName)
  {
    return getQuest().showHtmlFile(getPlayer(), fileName);
  }

  public QuestState exitQuest(boolean repeatable)
  {
    if (isCompleted()) {
      return this;
    }

    _isCompleted = true;

    FastList itemIdList = getQuest().getRegisteredItemIds();
    FastList.Node n;
    if (itemIdList != null)
    {
      n = itemIdList.head(); for (FastList.Node end = itemIdList.tail(); (n = n.getNext()) != end; )
      {
        takeItems(((Integer)n.getValue()).intValue(), -1);
      }

    }

    if (repeatable)
    {
      getPlayer().delQuestState(getQuestName());
      Quest.deleteQuestInDb(this);

      _vars = null;
    }
    else
    {
      if (_vars != null) {
        for (String var : _vars.keySet())
          unset(var);
      }
      Quest.updateQuestInDb(this);
    }

    return this;
  }
}
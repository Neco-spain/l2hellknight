package l2p.gameserver.model.instances;

import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntObjectIterator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import l2p.commons.collections.MultiValueSet;
import l2p.commons.lang.reference.HardReference;
import l2p.commons.threading.RunnableImpl;
import l2p.gameserver.Config;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.ai.CharacterAI;
import l2p.gameserver.ai.CtrlEvent;
import l2p.gameserver.ai.CtrlIntention;
import l2p.gameserver.ai.PlayerAI;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.data.htm.HtmCache;
import l2p.gameserver.data.xml.holder.ItemHolder;
import l2p.gameserver.data.xml.holder.MultiSellHolder;
import l2p.gameserver.data.xml.holder.ResidenceHolder;
import l2p.gameserver.data.xml.holder.SkillAcquireHolder;
import l2p.gameserver.geodata.GeoEngine;
import l2p.gameserver.idfactory.IdFactory;
import l2p.gameserver.instancemanager.DimensionalRiftManager;
import l2p.gameserver.instancemanager.QuestManager;
import l2p.gameserver.instancemanager.ReflectionManager;
import l2p.gameserver.listener.NpcListener;
import l2p.gameserver.model.AggroList;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.GameObjectTasks.NotifyAITask;
import l2p.gameserver.model.GameObjectsStorage;
import l2p.gameserver.model.MinionList;
import l2p.gameserver.model.Party;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.SkillLearn;
import l2p.gameserver.model.Spawner;
import l2p.gameserver.model.TeleportLocation;
import l2p.gameserver.model.Territory;
import l2p.gameserver.model.World;
import l2p.gameserver.model.WorldRegion;
import l2p.gameserver.model.Zone.ZoneType;
import l2p.gameserver.model.actor.listener.NpcListenerList;
import l2p.gameserver.model.actor.recorder.NpcStatsChangeRecorder;
import l2p.gameserver.model.base.AcquireType;
import l2p.gameserver.model.base.ClassId;
import l2p.gameserver.model.base.PlayerAccess;
import l2p.gameserver.model.entity.DimensionalRift;
import l2p.gameserver.model.entity.Reflection;
import l2p.gameserver.model.entity.SevenSigns;
import l2p.gameserver.model.entity.events.GlobalEvent;
import l2p.gameserver.model.entity.events.objects.TerritoryWardObject;
import l2p.gameserver.model.entity.residence.Castle;
import l2p.gameserver.model.entity.residence.ClanHall;
import l2p.gameserver.model.entity.residence.Dominion;
import l2p.gameserver.model.entity.residence.Fortress;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.model.pledge.SubUnit;
import l2p.gameserver.model.quest.Quest;
import l2p.gameserver.model.quest.QuestEventType;
import l2p.gameserver.model.quest.QuestState;
import l2p.gameserver.scripts.Events;
import l2p.gameserver.serverpackets.AcquireSkillDone;
import l2p.gameserver.serverpackets.AcquireSkillList;
import l2p.gameserver.serverpackets.ActionFail;
import l2p.gameserver.serverpackets.AutoAttackStart;
import l2p.gameserver.serverpackets.ExChangeNpcState;
import l2p.gameserver.serverpackets.ExShowBaseAttributeCancelWindow;
import l2p.gameserver.serverpackets.ExShowVariationCancelWindow;
import l2p.gameserver.serverpackets.ExShowVariationMakeWindow;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.MyTargetSelected;
import l2p.gameserver.serverpackets.NpcHtmlMessage;
import l2p.gameserver.serverpackets.NpcInfo;
import l2p.gameserver.serverpackets.RadarControl;
import l2p.gameserver.serverpackets.SocialAction;
import l2p.gameserver.serverpackets.SystemMessage2;
import l2p.gameserver.serverpackets.ValidateLocation;
import l2p.gameserver.serverpackets.components.CustomMessage;
import l2p.gameserver.serverpackets.components.IStaticPacket;
import l2p.gameserver.serverpackets.components.NpcString;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.stats.Stats;
import l2p.gameserver.tables.ClanTable;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.taskmanager.DecayTaskManager;
import l2p.gameserver.taskmanager.LazyPrecisionTaskManager;
import l2p.gameserver.templates.StatsSet;
import l2p.gameserver.templates.item.ItemTemplate;
import l2p.gameserver.templates.item.WeaponTemplate;
import l2p.gameserver.templates.npc.Faction;
import l2p.gameserver.templates.npc.NpcTemplate;
import l2p.gameserver.templates.spawn.SpawnRange;
import l2p.gameserver.utils.CertificationFunctions;
import l2p.gameserver.utils.HtmlUtils;
import l2p.gameserver.utils.ItemFunctions;
import l2p.gameserver.utils.Location;
import l2p.gameserver.utils.ReflectionUtils;
import l2p.gameserver.utils.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NpcInstance extends Creature
{
  public static final long serialVersionUID = 1L;
  public static final String NO_CHAT_WINDOW = "noChatWindow";
  public static final String NO_RANDOM_WALK = "noRandomWalk";
  public static final String NO_RANDOM_ANIMATION = "noRandomAnimation";
  public static final String TARGETABLE = "TargetEnabled";
  public static final String SHOW_NAME = "showName";
  private static final Logger _log = LoggerFactory.getLogger(NpcInstance.class);

  private int _personalAggroRange = -1;
  private int _level = 0;

  private long _dieTime = 0L;

  protected int _spawnAnimation = 2;
  private int _currentLHandId;
  private int _currentRHandId;
  private double _currentCollisionRadius;
  private double _currentCollisionHeight;
  private int npcState = 0;
  protected boolean _hasRandomAnimation;
  protected boolean _hasRandomWalk;
  protected boolean _hasChatWindow;
  private Future<?> _decayTask;
  private Future<?> _animationTask;
  private AggroList _aggroList;
  private boolean _isTargetable;
  private boolean _showName;
  private Castle _nearestCastle;
  private Fortress _nearestFortress;
  private ClanHall _nearestClanHall;
  private Dominion _nearestDominion;
  private NpcString _nameNpcString = NpcString.NONE;
  private NpcString _titleNpcString = NpcString.NONE;
  private Spawner _spawn;
  private Location _spawnedLoc = new Location();
  private SpawnRange _spawnRange;
  private MultiValueSet<String> _parameters = StatsSet.EMPTY;

  protected boolean _unAggred = false;

  private int _displayId = 0;
  private ScheduledFuture<?> _broadcastCharInfoTask;
  protected long _lastSocialAction;
  private boolean _isBusy;
  private String _busyMessage = "";

  private boolean _isUnderground = false;

  public NpcInstance(int objectId, NpcTemplate template)
  {
    super(objectId, template);

    if (template == null) {
      throw new NullPointerException("No template for Npc. Please check your datapack is setup correctly.");
    }
    setParameters(template.getAIParams());

    _hasRandomAnimation = ((!getParameter("noRandomAnimation", false)) && (Config.MAX_NPC_ANIMATION > 0));
    _hasRandomWalk = (!getParameter("noRandomWalk", false));
    setHasChatWindow(!getParameter("noChatWindow", false));
    setTargetable(getParameter("TargetEnabled", true));
    setShowName(getParameter("showName", true));
    TIntObjectIterator iterator;
    if (template.getSkills().size() > 0) {
      for (iterator = template.getSkills().iterator(); iterator.hasNext(); )
      {
        iterator.advance();
        addSkill((Skill)iterator.value());
      }
    }
    setName(template.name);
    setTitle(template.title);

    setLHandId(getTemplate().lhand);
    setRHandId(getTemplate().rhand);

    setCollisionHeight(getTemplate().collisionHeight);
    setCollisionRadius(getTemplate().collisionRadius);

    _aggroList = new AggroList(this);

    setFlying(getParameter("isFlying", false));
  }

  public HardReference<NpcInstance> getRef()
  {
    return super.getRef();
  }

  public CharacterAI getAI()
  {
    if (_ai == null) {
      synchronized (this)
      {
        if (_ai == null)
          _ai = getTemplate().getNewAI(this);
      }
    }
    return _ai;
  }

  public Location getSpawnedLoc()
  {
    return _spawnedLoc;
  }

  public void setSpawnedLoc(Location loc)
  {
    _spawnedLoc = loc;
  }

  public int getRightHandItem()
  {
    return _currentRHandId;
  }

  public int getLeftHandItem()
  {
    return _currentLHandId;
  }

  public void setLHandId(int newWeaponId)
  {
    _currentLHandId = newWeaponId;
  }

  public void setRHandId(int newWeaponId)
  {
    _currentRHandId = newWeaponId;
  }

  public double getCollisionHeight()
  {
    return _currentCollisionHeight;
  }

  public void setCollisionHeight(double offset)
  {
    _currentCollisionHeight = offset;
  }

  public double getCollisionRadius()
  {
    return _currentCollisionRadius;
  }

  public void setCollisionRadius(double collisionRadius)
  {
    _currentCollisionRadius = collisionRadius;
  }

  protected void onReduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp)
  {
    if (attacker.isPlayable()) {
      getAggroList().addDamageHate(attacker, (int)damage, 0);
    }
    super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
  }

  protected void onDeath(Creature killer)
  {
    _dieTime = System.currentTimeMillis();

    if ((isMonster()) && ((((MonsterInstance)this).isSeeded()) || (((MonsterInstance)this).isSpoiled())))
      startDecay(20000L);
    else if (isBoss())
      startDecay(20000L);
    else if (isFlying())
      startDecay(4500L);
    else {
      startDecay(8500L);
    }

    setLHandId(getTemplate().lhand);
    setRHandId(getTemplate().rhand);
    setCollisionHeight(getTemplate().collisionHeight);
    setCollisionRadius(getTemplate().collisionRadius);

    getAI().stopAITask();
    stopRandomAnimation();

    super.onDeath(killer);
  }

  public long getDeadTime()
  {
    if (_dieTime <= 0L)
      return 0L;
    return System.currentTimeMillis() - _dieTime;
  }

  public AggroList getAggroList()
  {
    return _aggroList;
  }

  public MinionList getMinionList()
  {
    return null;
  }

  public boolean hasMinions()
  {
    return false;
  }

  public void dropItem(Player lastAttacker, int itemId, long itemCount)
  {
    if ((itemCount == 0L) || (lastAttacker == null)) {
      return;
    }

    for (long i = 0L; i < itemCount; i += 1L)
    {
      ItemInstance item = ItemFunctions.createItem(itemId);
      for (GlobalEvent e : getEvents()) {
        item.addEvent(e);
      }

      if (item.isStackable())
      {
        i = itemCount;
        item.setCount(itemCount);
      }

      if ((isRaid()) || ((this instanceof ReflectionBossInstance)))
      {
        SystemMessage2 sm;
        if (itemId == 57)
        {
          SystemMessage2 sm = new SystemMessage2(SystemMsg.C1_HAS_DIED_AND_DROPPED_S2_ADENA);
          sm.addName(this);
          sm.addLong(item.getCount());
        }
        else
        {
          sm = new SystemMessage2(SystemMsg.C1_DIED_AND_DROPPED_S3_S2);
          sm.addName(this);
          sm.addItemName(itemId);
          sm.addLong(item.getCount());
        }
        broadcastPacket(new L2GameServerPacket[] { sm });
      }

      lastAttacker.doAutoLootOrDrop(item, this);
    }
  }

  public void dropItem(Player lastAttacker, ItemInstance item)
  {
    if (item.getCount() == 0L) {
      return;
    }
    if ((isRaid()) || ((this instanceof ReflectionBossInstance)))
    {
      SystemMessage2 sm;
      if (item.getItemId() == 57)
      {
        SystemMessage2 sm = new SystemMessage2(SystemMsg.C1_HAS_DIED_AND_DROPPED_S2_ADENA);
        sm.addName(this);
        sm.addLong(item.getCount());
      }
      else
      {
        sm = new SystemMessage2(SystemMsg.C1_DIED_AND_DROPPED_S3_S2);
        sm.addName(this);
        sm.addItemName(item.getItemId());
        sm.addLong(item.getCount());
      }
      broadcastPacket(new L2GameServerPacket[] { sm });
    }

    lastAttacker.doAutoLootOrDrop(item, this);
  }

  public boolean isAttackable(Creature attacker)
  {
    return true;
  }

  public boolean isAutoAttackable(Creature attacker)
  {
    return false;
  }

  protected void onSpawn()
  {
    super.onSpawn();

    _dieTime = 0L;
    _spawnAnimation = 0;

    if ((getAI().isGlobalAI()) || ((getCurrentRegion() != null) && (getCurrentRegion().isActive())))
    {
      getAI().startAITask();
      startRandomAnimation();
    }

    ThreadPoolManager.getInstance().execute(new GameObjectTasks.NotifyAITask(this, CtrlEvent.EVT_SPAWN));

    getListeners().onSpawn();
  }

  protected void onDespawn()
  {
    getAggroList().clear();

    getAI().onEvtDeSpawn();
    getAI().stopAITask();
    getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
    stopRandomAnimation();

    super.onDespawn();
  }

  public NpcTemplate getTemplate()
  {
    return (NpcTemplate)_template;
  }

  public int getNpcId()
  {
    return getTemplate().npcId;
  }

  public void setUnAggred(boolean state)
  {
    _unAggred = state;
  }

  public boolean isAggressive()
  {
    return getAggroRange() > 0;
  }

  public int getAggroRange()
  {
    if (_unAggred) {
      return 0;
    }
    if (_personalAggroRange >= 0) {
      return _personalAggroRange;
    }
    return getTemplate().aggroRange;
  }

  public void setAggroRange(int aggroRange)
  {
    _personalAggroRange = aggroRange;
  }

  public Faction getFaction()
  {
    return getTemplate().getFaction();
  }

  public boolean isInFaction(NpcInstance npc)
  {
    return (getFaction().equals(npc.getFaction())) && (!getFaction().isIgnoreNpcId(npc.getNpcId()));
  }

  public int getMAtk(Creature target, Skill skill)
  {
    return (int)(super.getMAtk(target, skill) * Config.ALT_NPC_MATK_MODIFIER);
  }

  public int getPAtk(Creature target)
  {
    return (int)(super.getPAtk(target) * Config.ALT_NPC_PATK_MODIFIER);
  }

  public int getMaxHp()
  {
    return (int)(super.getMaxHp() * Config.ALT_NPC_MAXHP_MODIFIER);
  }

  public int getMaxMp()
  {
    return (int)(super.getMaxMp() * Config.ALT_NPC_MAXMP_MODIFIER);
  }

  public long getExpReward()
  {
    return ()calcStat(Stats.EXP, getTemplate().rewardExp, null, null);
  }

  public long getSpReward()
  {
    return ()calcStat(Stats.SP, getTemplate().rewardSp, null, null);
  }

  protected void onDelete()
  {
    stopDecay();
    if (_spawn != null)
      _spawn.stopRespawn();
    setSpawn(null);

    super.onDelete();
  }

  public Spawner getSpawn()
  {
    return _spawn;
  }

  public void setSpawn(Spawner spawn)
  {
    _spawn = spawn;
  }

  protected void onDecay()
  {
    super.onDecay();

    _spawnAnimation = 2;

    if (_spawn != null)
      _spawn.decreaseCount(this);
    else
      deleteMe();
  }

  protected void startDecay(long delay)
  {
    stopDecay();
    _decayTask = DecayTaskManager.getInstance().addDecayTask(this, delay);
  }

  public void stopDecay()
  {
    if (_decayTask != null)
    {
      _decayTask.cancel(false);
      _decayTask = null;
    }
  }

  public void endDecayTask()
  {
    if (_decayTask != null)
    {
      _decayTask.cancel(false);
      _decayTask = null;
    }
    doDecay();
  }

  public boolean isUndead()
  {
    return getTemplate().isUndead();
  }

  public void setLevel(int level)
  {
    _level = level;
  }

  public int getLevel()
  {
    return _level == 0 ? getTemplate().level : _level;
  }

  public void setDisplayId(int displayId)
  {
    _displayId = displayId;
  }

  public int getDisplayId()
  {
    return _displayId > 0 ? _displayId : getTemplate().displayId;
  }

  public ItemInstance getActiveWeaponInstance()
  {
    return null;
  }

  public WeaponTemplate getActiveWeaponItem()
  {
    int weaponId = getTemplate().rhand;

    if (weaponId < 1) {
      return null;
    }

    ItemTemplate item = ItemHolder.getInstance().getTemplate(getTemplate().rhand);

    if (!(item instanceof WeaponTemplate)) {
      return null;
    }
    return (WeaponTemplate)item;
  }

  public ItemInstance getSecondaryWeaponInstance()
  {
    return null;
  }

  public WeaponTemplate getSecondaryWeaponItem()
  {
    int weaponId = getTemplate().lhand;

    if (weaponId < 1) {
      return null;
    }

    ItemTemplate item = ItemHolder.getInstance().getTemplate(getTemplate().lhand);

    if (!(item instanceof WeaponTemplate)) {
      return null;
    }
    return (WeaponTemplate)item;
  }

  public void sendChanges()
  {
    if (isFlying())
      return;
    super.sendChanges();
  }

  public void broadcastCharInfo()
  {
    if (!isVisible()) {
      return;
    }
    if (_broadcastCharInfoTask != null) {
      return;
    }
    _broadcastCharInfoTask = ThreadPoolManager.getInstance().schedule(new BroadcastCharInfoTask(), Config.BROADCAST_CHAR_INFO_INTERVAL);
  }

  public void broadcastCharInfoImpl()
  {
    for (Player player : World.getAroundPlayers(this))
      player.sendPacket(new NpcInfo(this, player).update());
  }

  public void onRandomAnimation()
  {
    if (System.currentTimeMillis() - _lastSocialAction > 10000L)
    {
      broadcastPacket(new L2GameServerPacket[] { new SocialAction(getObjectId(), 2) });
      _lastSocialAction = System.currentTimeMillis();
    }
  }

  public void startRandomAnimation()
  {
    if (!hasRandomAnimation())
      return;
    _animationTask = LazyPrecisionTaskManager.getInstance().addNpcAnimationTask(this);
  }

  public void stopRandomAnimation()
  {
    if (_animationTask != null)
    {
      _animationTask.cancel(false);
      _animationTask = null;
    }
  }

  public boolean hasRandomAnimation()
  {
    return _hasRandomAnimation;
  }

  public boolean hasRandomWalk()
  {
    return _hasRandomWalk;
  }

  public Castle getCastle()
  {
    if ((getReflection() == ReflectionManager.PARNASSUS) && (Config.SERVICES_PARNASSUS_NOTAX))
      return null;
    if ((Config.SERVICES_OFFSHORE_NO_CASTLE_TAX) && (getReflection() == ReflectionManager.GIRAN_HARBOR))
      return null;
    if ((Config.SERVICES_OFFSHORE_NO_CASTLE_TAX) && (getReflection() == ReflectionManager.PARNASSUS))
      return null;
    if ((Config.SERVICES_OFFSHORE_NO_CASTLE_TAX) && (isInZone(Zone.ZoneType.offshore)))
      return null;
    if (_nearestCastle == null)
      _nearestCastle = ((Castle)ResidenceHolder.getInstance().getResidence(getTemplate().getCastleId()));
    return _nearestCastle;
  }

  public Castle getCastle(Player player)
  {
    return getCastle();
  }

  public Fortress getFortress()
  {
    if (_nearestFortress == null) {
      _nearestFortress = ((Fortress)ResidenceHolder.getInstance().findNearestResidence(Fortress.class, getX(), getY(), getZ(), getReflection(), 32768));
    }
    return _nearestFortress;
  }

  public ClanHall getClanHall()
  {
    if (_nearestClanHall == null) {
      _nearestClanHall = ((ClanHall)ResidenceHolder.getInstance().findNearestResidence(ClanHall.class, getX(), getY(), getZ(), getReflection(), 32768));
    }
    return _nearestClanHall;
  }

  public Dominion getDominion()
  {
    if (getReflection() != ReflectionManager.DEFAULT) {
      return null;
    }
    if (_nearestDominion == null)
    {
      if (getTemplate().getCastleId() == 0) {
        return null;
      }
      Castle castle = (Castle)ResidenceHolder.getInstance().getResidence(getTemplate().getCastleId());
      _nearestDominion = castle.getDominion();
    }

    return _nearestDominion;
  }

  public void onAction(Player player, boolean shift)
  {
    if (!isTargetable())
    {
      player.sendActionFailed();
      return;
    }

    if (player.getTarget() != this)
    {
      player.setTarget(this);
      if (player.getTarget() == this) {
        player.sendPacket(new IStaticPacket[] { new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()), makeStatusUpdate(new int[] { 9, 10 }) });
      }
      player.sendPacket(new IStaticPacket[] { new ValidateLocation(this), ActionFail.STATIC });
      return;
    }

    if (Events.onAction(player, this, shift))
    {
      player.sendActionFailed();
      return;
    }

    if (isAutoAttackable(player))
    {
      player.getAI().Attack(this, false, shift);
      return;
    }

    if (!isInRange(player, 200L))
    {
      if (player.getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT)
        player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
      return;
    }

    if ((!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP) && (player.getKarma() > 0) && (!player.isGM()) && (!(this instanceof WarehouseInstance)))
    {
      player.sendActionFailed();
      return;
    }

    if (((!Config.ALLOW_TALK_WHILE_SITTING) && (player.isSitting())) || (player.isAlikeDead())) {
      return;
    }
    if (hasRandomAnimation()) {
      onRandomAnimation();
    }
    player.sendActionFailed();
    player.stopMove(false);

    if (_isBusy) {
      showBusyWindow(player);
    } else if (isHasChatWindow())
    {
      boolean flag = false;
      Quest[] qlst = getTemplate().getEventQuests(QuestEventType.NPC_FIRST_TALK);
      if ((qlst != null) && (qlst.length > 0))
        for (Quest element : qlst)
        {
          QuestState qs = player.getQuestState(element.getName());
          if (((qs == null) || (!qs.isCompleted())) && (element.notifyFirstTalk(this, player)))
            flag = true;
        }
      if (!flag)
        showChatWindow(player, 0, new Object[0]);
    }
  }

  public void showQuestWindow(Player player, String questId)
  {
    if (!player.isQuestContinuationPossible(true)) {
      return;
    }
    int count = 0;
    for (QuestState quest : player.getAllQuestsStates()) {
      if ((quest != null) && (quest.getQuest().isVisible()) && (quest.isStarted()) && (quest.getCond() > 0))
        count++;
    }
    if (count > 40)
    {
      showChatWindow(player, "quest-limit.htm", new Object[0]);
      return;
    }

    try
    {
      QuestState qs = player.getQuestState(questId);
      if (qs != null)
      {
        if (qs.isCompleted())
        {
          showChatWindow(player, "completed-quest.htm", new Object[0]);
          return;
        }
        if (qs.getQuest().notifyTalk(this, qs))
          return;
      }
      else
      {
        Quest q = QuestManager.getQuest(questId);
        if (q != null)
        {
          Quest[] qlst = getTemplate().getEventQuests(QuestEventType.QUEST_START);
          if ((qlst != null) && (qlst.length > 0)) {
            for (Quest element : qlst) {
              if (element != q)
                continue;
              qs = q.newQuestState(player, 1);
              if (!qs.getQuest().notifyTalk(this, qs)) break;
              return;
            }
          }
        }
      }

      showChatWindow(player, "no-quest.htm", new Object[0]);
    }
    catch (Exception e)
    {
      _log.warn(new StringBuilder().append("problem with npc text(questId: ").append(questId).append(") ").append(e).toString());
      _log.error("", e);
    }

    player.sendActionFailed();
  }

  public static boolean canBypassCheck(Player player, NpcInstance npc)
  {
    if ((npc == null) || (player.isActionsDisabled()) || ((!Config.ALLOW_TALK_WHILE_SITTING) && (player.isSitting())) || (!npc.isInRange(player, 200L)))
    {
      player.sendActionFailed();
      return false;
    }
    return true;
  }

  public void onBypassFeedback(Player player, String command)
  {
    if (!canBypassCheck(player, this)) {
      return;
    }
    if ((getTemplate().getTeleportList().size() > 0) && (checkForDominionWard(player))) {
      return;
    }
    try
    {
      if (command.equalsIgnoreCase("TerritoryStatus"))
      {
        NpcHtmlMessage html = new NpcHtmlMessage(player, this);
        html.setFile("merchant/territorystatus.htm");
        html.replace("%npcname%", getName());

        Castle castle = getCastle(player);
        if ((castle != null) && (castle.getId() > 0))
        {
          html.replace("%castlename%", HtmlUtils.htmlResidenceName(castle.getId()));
          html.replace("%taxpercent%", String.valueOf(castle.getTaxPercent()));

          if (castle.getOwnerId() > 0)
          {
            Clan clan = ClanTable.getInstance().getClan(castle.getOwnerId());
            if (clan != null)
            {
              html.replace("%clanname%", clan.getName());
              html.replace("%clanleadername%", clan.getLeaderName());
            }
            else
            {
              html.replace("%clanname%", "unexistant clan");
              html.replace("%clanleadername%", "None");
            }
          }
          else
          {
            html.replace("%clanname%", "NPC");
            html.replace("%clanleadername%", "None");
          }
        }
        else
        {
          html.replace("%castlename%", "Open");
          html.replace("%taxpercent%", "0");

          html.replace("%clanname%", "No");
          html.replace("%clanleadername%", getName());
        }

        player.sendPacket(html);
      }
      else if (command.startsWith("Quest"))
      {
        String quest = command.substring(5).trim();
        if (quest.length() == 0)
          showQuestWindow(player);
        else
          showQuestWindow(player, quest);
      }
      else if (command.startsWith("Chat"))
      {
        try {
          int val = Integer.parseInt(command.substring(5));
          showChatWindow(player, val, new Object[0]);
        }
        catch (NumberFormatException nfe)
        {
          String filename = command.substring(5).trim();
          if (filename.length() == 0)
            showChatWindow(player, "npcdefault.htm", new Object[0]);
          else
            showChatWindow(player, filename, new Object[0]);
        }
      } else if (command.startsWith("AttributeCancel")) {
        player.sendPacket(new ExShowBaseAttributeCancelWindow(player));
      } else if (command.startsWith("NpcLocationInfo"))
      {
        int val = Integer.parseInt(command.substring(16));
        NpcInstance npc = GameObjectsStorage.getByNpcId(val);
        if (npc != null)
        {
          player.sendPacket(new RadarControl(2, 2, npc.getLoc()));

          player.sendPacket(new RadarControl(0, 1, npc.getLoc()));
        }
      }
      else if ((command.startsWith("Multisell")) || (command.startsWith("multisell")))
      {
        String listId = command.substring(9).trim();
        Castle castle = getCastle(player);
        MultiSellHolder.getInstance().SeparateAndSend(Integer.parseInt(listId), player, castle != null ? castle.getTaxRate() : 0.0D);
      }
      else if (command.startsWith("EnterRift"))
      {
        if (checkForDominionWard(player)) {
          return;
        }
        StringTokenizer st = new StringTokenizer(command);
        st.nextToken();

        Integer b1 = Integer.valueOf(Integer.parseInt(st.nextToken()));

        DimensionalRiftManager.getInstance().start(player, b1.intValue(), this);
      }
      else if (command.startsWith("ChangeRiftRoom"))
      {
        if ((player.isInParty()) && (player.getParty().isInReflection()) && ((player.getParty().getReflection() instanceof DimensionalRift)))
          ((DimensionalRift)player.getParty().getReflection()).manualTeleport(player, this);
        else
          DimensionalRiftManager.getInstance().teleportToWaitingRoom(player);
      }
      else if (command.startsWith("ExitRift"))
      {
        if ((player.isInParty()) && (player.getParty().isInReflection()) && ((player.getParty().getReflection() instanceof DimensionalRift)))
          ((DimensionalRift)player.getParty().getReflection()).manualExitRift(player, this);
        else
          DimensionalRiftManager.getInstance().teleportToWaitingRoom(player);
      }
      else if (command.equalsIgnoreCase("SkillList")) {
        showSkillList(player);
      } else if (command.equalsIgnoreCase("ClanSkillList")) {
        showClanSkillList(player);
      } else if (command.startsWith("SubUnitSkillList")) {
        showSubUnitSkillList(player);
      } else if (command.equalsIgnoreCase("TransformationSkillList")) {
        showTransformationSkillList(player, AcquireType.TRANSFORMATION);
      } else if (command.equalsIgnoreCase("CertificationSkillList")) {
        showTransformationSkillList(player, AcquireType.CERTIFICATION);
      } else if (command.equalsIgnoreCase("CollectionSkillList")) {
        showCollectionSkillList(player);
      } else if (command.equalsIgnoreCase("BuyTransformation")) {
        showTransformationMultisell(player);
      } else if (command.startsWith("Augment"))
      {
        int cmdChoice = Integer.parseInt(command.substring(8, 9).trim());
        if (cmdChoice == 1)
          player.sendPacket(new IStaticPacket[] { Msg.SELECT_THE_ITEM_TO_BE_AUGMENTED, ExShowVariationMakeWindow.STATIC });
        else if (cmdChoice == 2)
          player.sendPacket(new IStaticPacket[] { Msg.SELECT_THE_ITEM_FROM_WHICH_YOU_WISH_TO_REMOVE_AUGMENTATION, ExShowVariationCancelWindow.STATIC });
      }
      else if (command.startsWith("Link")) {
        showChatWindow(player, command.substring(5), new Object[0]);
      } else if (command.startsWith("Teleport"))
      {
        int cmdChoice = Integer.parseInt(command.substring(9, 10).trim());
        TeleportLocation[] list = getTemplate().getTeleportList(cmdChoice);
        if (list != null)
          showTeleportList(player, list);
        else
          player.sendMessage("\u0421\u0441\u044B\u043B\u043A\u0430 \u043D\u0435\u0438\u0441\u043F\u0440\u0430\u0432\u043D\u0430, \u0441\u043E\u043E\u0431\u0449\u0438\u0442\u0435 \u0430\u0434\u043C\u0438\u043D\u0438\u0441\u0442\u0440\u0430\u0442\u043E\u0440\u0443.");
      }
      else if (command.startsWith("Tele20Lvl"))
      {
        int cmdChoice = Integer.parseInt(command.substring(10, 11).trim());
        TeleportLocation[] list = getTemplate().getTeleportList(cmdChoice);
        if (player.getLevel() > 20)
          showChatWindow(player, new StringBuilder().append("teleporter/").append(getNpcId()).append("-no.htm").toString(), new Object[0]);
        else if (list != null)
          showTeleportList(player, list);
        else
          player.sendMessage("\u0421\u0441\u044B\u043B\u043A\u0430 \u043D\u0435\u0438\u0441\u043F\u0440\u0430\u0432\u043D\u0430, \u0441\u043E\u043E\u0431\u0449\u0438\u0442\u0435 \u0430\u0434\u043C\u0438\u043D\u0438\u0441\u0442\u0440\u0430\u0442\u043E\u0440\u0443.");
      }
      else if (command.startsWith("open_gate"))
      {
        int val = Integer.parseInt(command.substring(10));
        ReflectionUtils.getDoor(val).openMe();
        player.sendActionFailed();
      }
      else if (command.equalsIgnoreCase("TransferSkillList")) {
        showTransferSkillList(player);
      } else if (command.equalsIgnoreCase("CertificationCancel")) {
        CertificationFunctions.cancelCertification(this, player);
      } else if (command.startsWith("RemoveTransferSkill"))
      {
        AcquireType type = AcquireType.transferType(player.getActiveClassId());
        if (type == null) {
          return;
        }
        Collection skills = SkillAcquireHolder.getInstance().getAvailableSkills(null, type);
        if (skills.isEmpty())
        {
          player.sendActionFailed();
          return;
        }

        boolean reset = false;
        for (SkillLearn skill : skills) {
          if (player.getKnownSkill(skill.getId()) != null)
          {
            reset = true;
            break;
          }
        }
        if (!reset)
        {
          player.sendActionFailed();
          return;
        }

        if (!player.reduceAdena(10000000L, true))
        {
          showChatWindow(player, "common/skill_share_healer_no_adena.htm", new Object[0]);
          return;
        }

        for (SkillLearn skill : skills)
          if (player.removeSkill(skill.getId(), true) != null)
            ItemFunctions.addItem(player, skill.getItemId(), skill.getItemCount(), true);
      }
      else if (command.startsWith("ExitFromQuestInstance"))
      {
        Reflection r = player.getReflection();
        r.startCollapseTimer(60000L);
        player.teleToLocation(r.getReturnLoc(), 0);
        if (command.length() > 22)
          try
          {
            int val = Integer.parseInt(command.substring(22));
            showChatWindow(player, val, new Object[0]);
          }
          catch (NumberFormatException nfe)
          {
            String filename = command.substring(22).trim();
            if (filename.length() > 0)
              showChatWindow(player, filename, new Object[0]);
          }
      }
    }
    catch (StringIndexOutOfBoundsException sioobe)
    {
      _log.info(new StringBuilder().append("Incorrect htm bypass! npcId=").append(getTemplate().npcId).append(" command=[").append(command).append("]").toString());
    }
    catch (NumberFormatException nfe)
    {
      _log.info(new StringBuilder().append("Invalid bypass to Server command parameter! npcId=").append(getTemplate().npcId).append(" command=[").append(command).append("]").toString());
    }
  }

  public void showTeleportList(Player player, TeleportLocation[] list)
  {
    StringBuilder sb = new StringBuilder();

    sb.append("&$556;").append("<br><br>");

    if ((list != null) && (player.getPlayerAccess().UseTeleport))
    {
      for (TeleportLocation tl : list)
        if (tl.getItem().getItemId() == 57)
        {
          double pricemod = player.getLevel() <= Config.GATEKEEPER_FREE ? 0.0D : Config.GATEKEEPER_MODIFIER;
          if ((tl.getPrice() > 0L) && (pricemod > 0.0D))
          {
            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(7);
            int hour = Calendar.getInstance().get(11);
            if (((day == 1) || (day == 7)) && (hour >= 20) && (hour <= 12))
              pricemod /= 2.0D;
          }
          sb.append("[scripts_Util:Gatekeeper ").append(tl.getX()).append(" ").append(tl.getY()).append(" ").append(tl.getZ());
          if (tl.getCastleId() != 0)
            sb.append(" ").append(tl.getCastleId());
          sb.append(" ").append(()(tl.getPrice() * pricemod)).append(" @811;F;").append(tl.getName()).append("|").append(HtmlUtils.htmlNpcString(tl.getName(), new Object[0]));
          if (tl.getPrice() * pricemod > 0.0D)
            sb.append(" - ").append(()(tl.getPrice() * pricemod)).append(" ").append(HtmlUtils.htmlItemName(57));
          sb.append("]<br1>\n");
        }
        else {
          sb.append("[scripts_Util:QuestGatekeeper ").append(tl.getX()).append(" ").append(tl.getY()).append(" ").append(tl.getZ()).append(" ").append(tl.getPrice()).append(" ").append(tl.getItem().getItemId()).append(" @811;F;").append("|").append(HtmlUtils.htmlNpcString(tl.getName(), new Object[0])).append(" - ").append(tl.getPrice()).append(" ").append(HtmlUtils.htmlItemName(tl.getItem().getItemId())).append("]<br1>\n");
        }
    }
    else sb.append("No teleports available for you.");

    NpcHtmlMessage html = new NpcHtmlMessage(player, this);
    html.setHtml(Strings.bbParse(sb.toString()));
    player.sendPacket(html);
  }

  public void showQuestWindow(Player player)
  {
    List options = new ArrayList();

    List awaits = player.getQuestsForEvent(this, QuestEventType.QUEST_TALK);
    Quest[] starts = getTemplate().getEventQuests(QuestEventType.QUEST_START);

    if (awaits != null) {
      for (QuestState x : awaits)
        if ((!options.contains(x.getQuest())) && 
          (x.getQuest().getQuestIntId() > 0))
          options.add(x.getQuest());
    }
    if (starts != null) {
      for (Quest x : starts) {
        if ((options.contains(x)) || 
          (x.getQuestIntId() <= 0)) continue;
        options.add(x);
      }
    }
    if (options.size() > 1)
      showQuestChooseWindow(player, (Quest[])options.toArray(new Quest[options.size()]));
    else if (options.size() == 1)
      showQuestWindow(player, ((Quest)options.get(0)).getName());
    else
      showQuestWindow(player, "");
  }

  public void showQuestChooseWindow(Player player, Quest[] quests)
  {
    StringBuilder sb = new StringBuilder();

    sb.append("<html><body><title>Talk about:</title><br>");

    for (Quest q : quests)
    {
      if (!q.isVisible()) {
        continue;
      }
      sb.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Quest ").append(q.getName()).append("\">[").append(q.getDescr(player)).append("]</a><br>");
    }

    sb.append("</body></html>");

    NpcHtmlMessage html = new NpcHtmlMessage(player, this);
    html.setHtml(sb.toString());
    player.sendPacket(html);
  }

  public void showChatWindow(Player player, int val, Object[] replace)
  {
    if ((getTemplate().getTeleportList().size() > 0) && (checkForDominionWard(player))) {
      return;
    }
    String filename = "seven_signs/";
    int npcId = getNpcId();
    switch (npcId)
    {
    case 31111:
      int sealAvariceOwner = SevenSigns.getInstance().getSealOwner(1);
      int playerCabal = SevenSigns.getInstance().getPlayerCabal(player);
      int compWinner = SevenSigns.getInstance().getCabalHighestScore();
      if ((playerCabal == sealAvariceOwner) && (playerCabal == compWinner)) {
        switch (sealAvariceOwner)
        {
        case 2:
          filename = new StringBuilder().append(filename).append("spirit_dawn.htm").toString();
          break;
        case 1:
          filename = new StringBuilder().append(filename).append("spirit_dusk.htm").toString();
          break;
        case 0:
          filename = new StringBuilder().append(filename).append("spirit_null.htm").toString();
        }
      }
      else
        filename = new StringBuilder().append(filename).append("spirit_null.htm").toString();
      break;
    case 31112:
      filename = new StringBuilder().append(filename).append("spirit_exit.htm").toString();
      break;
    case 30298:
      if (player.getPledgeType() == -1)
        filename = getHtmlPath(npcId, 1, player);
      else
        filename = getHtmlPath(npcId, 0, player);
      break;
    default:
      if (((npcId >= 31093) && (npcId <= 31094)) || ((npcId >= 31172) && (npcId <= 31201)) || ((npcId >= 31239) && (npcId <= 31254))) {
        return;
      }
      filename = getHtmlPath(npcId, val, player);
    }

    NpcHtmlMessage packet = new NpcHtmlMessage(player, this, filename, val);
    if (replace.length % 2 == 0)
      for (int i = 0; i < replace.length; i += 2)
        packet.replace(String.valueOf(replace[i]), String.valueOf(replace[(i + 1)]));
    player.sendPacket(packet);
  }

  public void showChatWindow(Player player, String filename, Object[] replace)
  {
    NpcHtmlMessage packet = new NpcHtmlMessage(player, this, filename, 0);
    if (replace.length % 2 == 0)
      for (int i = 0; i < replace.length; i += 2)
        packet.replace(String.valueOf(replace[i]), String.valueOf(replace[(i + 1)]));
    player.sendPacket(packet);
  }

  public String getHtmlPath(int npcId, int val, Player player)
  {
    String pom;
    String pom;
    if (val == 0)
      pom = new StringBuilder().append("").append(npcId).toString();
    else {
      pom = new StringBuilder().append(npcId).append("-").append(val).toString();
    }
    if (getTemplate().getHtmRoot() != null) {
      return new StringBuilder().append(getTemplate().getHtmRoot()).append(pom).append(".htm").toString();
    }
    String temp = new StringBuilder().append("default/").append(pom).append(".htm").toString();
    if (HtmCache.getInstance().getNullable(temp, player) != null) {
      return temp;
    }
    temp = new StringBuilder().append("trainer/").append(pom).append(".htm").toString();
    if (HtmCache.getInstance().getNullable(temp, player) != null) {
      return temp;
    }

    return "npcdefault.htm";
  }

  public final boolean isBusy()
  {
    return _isBusy;
  }

  public void setBusy(boolean isBusy)
  {
    _isBusy = isBusy;
  }

  public final String getBusyMessage()
  {
    return _busyMessage;
  }

  public void setBusyMessage(String message)
  {
    _busyMessage = message;
  }

  public void showBusyWindow(Player player)
  {
    NpcHtmlMessage html = new NpcHtmlMessage(player, this);
    html.setFile("npcbusy.htm");
    html.replace("%npcname%", getName());
    html.replace("%playername%", player.getName());
    html.replace("%busymessage%", _busyMessage);
    player.sendPacket(html);
  }

  public void showSkillList(Player player)
  {
    ClassId classId = player.getClassId();

    if (classId == null) {
      return;
    }
    int npcId = getTemplate().npcId;

    if (getTemplate().getTeachInfo().isEmpty())
    {
      NpcHtmlMessage html = new NpcHtmlMessage(player, this);
      StringBuilder sb = new StringBuilder();
      sb.append("<html><head><body>");
      if (player.getVar("lang@").equalsIgnoreCase("en"))
        sb.append(new StringBuilder().append("I cannot teach you. My class list is empty.<br> Ask admin to fix it. <br>NpcId:").append(npcId).append(", Your classId:").append(player.getClassId().getId()).append("<br>").toString());
      else
        sb.append(new StringBuilder().append("\u042F \u043D\u0435 \u043C\u043E\u0433\u0443 \u043E\u0431\u0443\u0447\u0438\u0442\u044C \u0442\u0435\u0431\u044F. \u0414\u043B\u044F \u0442\u0432\u043E\u0435\u0433\u043E \u043A\u043B\u0430\u0441\u0441\u0430 \u043C\u043E\u0439 \u0441\u043F\u0438\u0441\u043E\u043A \u043F\u0443\u0441\u0442.<br> \u0421\u0432\u044F\u0436\u0438\u0441\u044C \u0441 \u0430\u0434\u043C\u0438\u043D\u043E\u043C \u0434\u043B\u044F \u0444\u0438\u043A\u0441\u0430 \u044D\u0442\u043E\u0433\u043E. <br>NpcId:").append(npcId).append(", \u0442\u0432\u043E\u0439 classId:").append(player.getClassId().getId()).append("<br>").toString());
      sb.append("</body></html>");
      html.setHtml(sb.toString());
      player.sendPacket(html);

      return;
    }

    if ((!getTemplate().canTeach(classId)) && (!getTemplate().canTeach(classId.getParent(player.getSex()))))
    {
      if ((this instanceof WarehouseInstance)) {
        showChatWindow(player, new StringBuilder().append("warehouse/").append(getNpcId()).append("-noteach.htm").toString(), new Object[0]);
      } else if ((this instanceof TrainerInstance)) {
        showChatWindow(player, new StringBuilder().append("trainer/").append(getNpcId()).append("-noteach.htm").toString(), new Object[0]);
      }
      else {
        NpcHtmlMessage html = new NpcHtmlMessage(player, this);
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><body>");
        sb.append(new CustomMessage("l2p.gameserver.model.instances.L2NpcInstance.WrongTeacherClass", player, new Object[0]));
        sb.append("</body></html>");
        html.setHtml(sb.toString());
        player.sendPacket(html);
      }
      return;
    }

    Collection skills = SkillAcquireHolder.getInstance().getAvailableSkills(player, AcquireType.NORMAL);

    AcquireSkillList asl = new AcquireSkillList(AcquireType.NORMAL, skills.size());
    int counts = 0;

    for (SkillLearn s : skills)
    {
      if (s.isClicked()) {
        continue;
      }
      Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
      if ((sk == null) || (!sk.getCanLearn(player.getClassId())) || (!sk.canTeachBy(npcId))) {
        continue;
      }
      counts++;

      asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), s.getCost(), 0);
    }

    if (counts == 0)
    {
      int minlevel = SkillAcquireHolder.getInstance().getMinLevelForNewSkill(player, AcquireType.NORMAL);

      if (minlevel > 0)
      {
        SystemMessage2 sm = new SystemMessage2(SystemMsg.YOU_DO_NOT_HAVE_ANY_FURTHER_SKILLS_TO_LEARN__COME_BACK_WHEN_YOU_HAVE_REACHED_LEVEL_S1);
        sm.addInteger(minlevel);
        player.sendPacket(sm);
      }
      else {
        player.sendPacket(SystemMsg.THERE_ARE_NO_OTHER_SKILLS_TO_LEARN);
      }player.sendPacket(AcquireSkillDone.STATIC);
    }
    else {
      player.sendPacket(asl);
    }
    player.sendActionFailed();
  }

  public void showTransferSkillList(Player player)
  {
    ClassId classId = player.getClassId();
    if (classId == null) {
      return;
    }
    if ((player.getLevel() < 76) || (classId.getLevel() < 4))
    {
      NpcHtmlMessage html = new NpcHtmlMessage(player, this);
      StringBuilder sb = new StringBuilder();
      sb.append("<html><head><body>");
      sb.append("You must have 3rd class change quest completed.");
      sb.append("</body></html>");
      html.setHtml(sb.toString());
      player.sendPacket(html);
      return;
    }

    AcquireType type = AcquireType.transferType(player.getActiveClassId());
    if (type == null) {
      return;
    }
    showAcquireList(type, player);
  }

  public static void showCollectionSkillList(Player player)
  {
    showAcquireList(AcquireType.COLLECTION, player);
  }

  public void showTransformationMultisell(Player player)
  {
    if ((!Config.ALLOW_LEARN_TRANS_SKILLS_WO_QUEST) && 
      (!player.isQuestCompleted("_136_MoreThanMeetsTheEye")))
    {
      showChatWindow(player, new StringBuilder().append("trainer/").append(getNpcId()).append("-nobuy.htm").toString(), new Object[0]);
      return;
    }

    Castle castle = getCastle(player);
    MultiSellHolder.getInstance().SeparateAndSend(32323, player, castle != null ? castle.getTaxRate() : 0.0D);
    player.sendActionFailed();
  }

  public void showTransformationSkillList(Player player, AcquireType type)
  {
    if ((!Config.ALLOW_LEARN_TRANS_SKILLS_WO_QUEST) && 
      (!player.isQuestCompleted("_136_MoreThanMeetsTheEye")))
    {
      showChatWindow(player, new StringBuilder().append("trainer/").append(getNpcId()).append("-noquest.htm").toString(), new Object[0]);
      return;
    }

    showAcquireList(type, player);
  }

  public static void showFishingSkillList(Player player)
  {
    showAcquireList(AcquireType.FISHING, player);
  }

  public static void showClanSkillList(Player player)
  {
    if ((player.getClan() == null) || (!player.isClanLeader()))
    {
      player.sendPacket(SystemMsg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
      player.sendActionFailed();
      return;
    }

    showAcquireList(AcquireType.CLAN, player);
  }

  public static void showAcquireList(AcquireType t, Player player)
  {
    Collection skills = SkillAcquireHolder.getInstance().getAvailableSkills(player, t);

    AcquireSkillList asl = new AcquireSkillList(t, skills.size());

    for (SkillLearn s : skills) {
      asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), s.getCost(), 0);
    }
    if (skills.size() == 0)
    {
      player.sendPacket(AcquireSkillDone.STATIC);
      player.sendPacket(SystemMsg.THERE_ARE_NO_OTHER_SKILLS_TO_LEARN);
    }
    else {
      player.sendPacket(asl);
    }
    player.sendActionFailed();
  }

  public static void showSubUnitSkillList(Player player)
  {
    Clan clan = player.getClan();
    if (clan == null) {
      return;
    }
    if ((player.getClanPrivileges() & 0x200) != 512)
    {
      player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
      return;
    }

    Set learns = new TreeSet();
    for (SubUnit sub : player.getClan().getAllSubUnits()) {
      learns.addAll(SkillAcquireHolder.getInstance().getAvailableSkills(player, AcquireType.SUB_UNIT, sub));
    }
    AcquireSkillList asl = new AcquireSkillList(AcquireType.SUB_UNIT, learns.size());

    for (SkillLearn s : learns) {
      asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), s.getCost(), 1, 2002);
    }
    if (learns.size() == 0)
    {
      player.sendPacket(AcquireSkillDone.STATIC);
      player.sendPacket(SystemMsg.THERE_ARE_NO_OTHER_SKILLS_TO_LEARN);
    }
    else {
      player.sendPacket(asl);
    }
    player.sendActionFailed();
  }

  public int getSpawnAnimation()
  {
    return _spawnAnimation;
  }

  public double getColRadius()
  {
    return getCollisionRadius();
  }

  public double getColHeight()
  {
    return getCollisionHeight();
  }

  public int calculateLevelDiffForDrop(int charLevel)
  {
    if (!Config.DEEPBLUE_DROP_RULES) {
      return 0;
    }
    int mobLevel = getLevel();

    int deepblue_maxdiff = (this instanceof RaidBossInstance) ? Config.DEEPBLUE_DROP_RAID_MAXDIFF : Config.DEEPBLUE_DROP_MAXDIFF;

    return Math.max(charLevel - mobLevel - deepblue_maxdiff, 0);
  }

  public boolean isSevenSignsMonster()
  {
    return getFaction().getName().equalsIgnoreCase("c_dungeon_clan");
  }

  public String toString()
  {
    return new StringBuilder().append(getNpcId()).append(" ").append(getName()).toString();
  }

  public void refreshID()
  {
    objectId = IdFactory.getInstance().getNextId();
    _storedId = Long.valueOf(GameObjectsStorage.refreshId(this));
  }

  public void setUnderground(boolean b)
  {
    _isUnderground = b;
  }

  public boolean isUnderground()
  {
    return _isUnderground;
  }

  public boolean isTargetable()
  {
    return _isTargetable;
  }

  public void setTargetable(boolean value)
  {
    _isTargetable = value;
  }

  public boolean isShowName()
  {
    return _showName;
  }

  public void setShowName(boolean value)
  {
    _showName = value;
  }

  public NpcListenerList getListeners()
  {
    if (listeners == null) {
      synchronized (this)
      {
        if (listeners == null)
          listeners = new NpcListenerList(this);
      }
    }
    return (NpcListenerList)listeners;
  }

  public <T extends NpcListener> boolean addListener(T listener)
  {
    return getListeners().add(listener);
  }

  public <T extends NpcListener> boolean removeListener(T listener)
  {
    return getListeners().remove(listener);
  }

  public NpcStatsChangeRecorder getStatsRecorder()
  {
    if (_statsRecorder == null) {
      synchronized (this)
      {
        if (_statsRecorder == null)
          _statsRecorder = new NpcStatsChangeRecorder(this);
      }
    }
    return (NpcStatsChangeRecorder)_statsRecorder;
  }

  public void setNpcState(int stateId)
  {
    broadcastPacket(new L2GameServerPacket[] { new ExChangeNpcState(getObjectId(), stateId) });
    npcState = stateId;
  }

  public int getNpcState()
  {
    return npcState;
  }

  public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper)
  {
    List list = new ArrayList(3);
    list.add(new NpcInfo(this, forPlayer));

    if (isInCombat()) {
      list.add(new AutoAttackStart(getObjectId()));
    }
    if ((isMoving) || (isFollow)) {
      list.add(movePacket());
    }
    return list;
  }

  public boolean isNpc()
  {
    return true;
  }

  public int getGeoZ(Location loc)
  {
    if ((isFlying()) || (isInWater()) || (isInBoat()) || (isBoat()) || (isDoor()))
      return loc.z;
    if (isNpc())
    {
      if ((_spawnRange instanceof Territory))
        return GeoEngine.getHeight(loc, getGeoIndex());
      return loc.z;
    }

    return super.getGeoZ(loc);
  }

  public Clan getClan()
  {
    Dominion dominion = getDominion();
    if (dominion == null)
      return null;
    int lordObjectId = dominion.getLordObjectId();
    return lordObjectId == 0 ? null : dominion.getOwner();
  }

  public NpcString getNameNpcString()
  {
    return _nameNpcString;
  }

  public NpcString getTitleNpcString()
  {
    return _titleNpcString;
  }

  public void setNameNpcString(NpcString nameNpcString)
  {
    _nameNpcString = nameNpcString;
  }

  public void setTitleNpcString(NpcString titleNpcString)
  {
    _titleNpcString = titleNpcString;
  }

  public boolean isMerchantNpc()
  {
    return false;
  }

  public SpawnRange getSpawnRange()
  {
    return _spawnRange;
  }

  public void setSpawnRange(SpawnRange spawnRange)
  {
    _spawnRange = spawnRange;
  }

  public boolean checkForDominionWard(Player player)
  {
    ItemInstance item = getActiveWeaponInstance();
    if ((item != null) && ((item.getAttachment() instanceof TerritoryWardObject)))
    {
      showChatWindow(player, "flagman.htm", new Object[0]);
      return true;
    }
    return false;
  }

  public void setParameter(String str, Object val)
  {
    if (_parameters == StatsSet.EMPTY) {
      _parameters = new StatsSet();
    }
    _parameters.set(str, val);
  }

  public void setParameters(MultiValueSet<String> set)
  {
    if (set.isEmpty()) {
      return;
    }
    if (_parameters == StatsSet.EMPTY) {
      _parameters = new MultiValueSet(set.size());
    }
    _parameters.putAll(set);
  }

  public int getParameter(String str, int val)
  {
    return _parameters.getInteger(str, val);
  }

  public long getParameter(String str, long val)
  {
    return _parameters.getLong(str, val);
  }

  public boolean getParameter(String str, boolean val)
  {
    return _parameters.getBool(str, val);
  }

  public String getParameter(String str, String val)
  {
    return _parameters.getString(str, val);
  }

  public MultiValueSet<String> getParameters()
  {
    return _parameters;
  }

  public boolean isInvul()
  {
    return true;
  }

  public boolean isHasChatWindow()
  {
    return _hasChatWindow;
  }

  public void setHasChatWindow(boolean hasChatWindow)
  {
    _hasChatWindow = hasChatWindow;
  }

  public class BroadcastCharInfoTask extends RunnableImpl
  {
    public BroadcastCharInfoTask()
    {
    }

    public void runImpl()
      throws Exception
    {
      broadcastCharInfoImpl();
      NpcInstance.access$002(NpcInstance.this, null);
    }
  }
}
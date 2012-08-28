package l2p.gameserver.model.instances;

import gnu.trove.TIntHashSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import l2p.commons.threading.RunnableImpl;
import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.instancemanager.CursedWeaponsManager;
import l2p.gameserver.model.AggroList;
import l2p.gameserver.model.AggroList.HateInfo;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.model.EffectList;
import l2p.gameserver.model.Manor;
import l2p.gameserver.model.MinionList;
import l2p.gameserver.model.Party;
import l2p.gameserver.model.Playable;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.base.Experience;
import l2p.gameserver.model.base.TeamType;
import l2p.gameserver.model.entity.Reflection;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.model.quest.Quest;
import l2p.gameserver.model.quest.QuestEventType;
import l2p.gameserver.model.quest.QuestState;
import l2p.gameserver.model.reward.RewardItem;
import l2p.gameserver.model.reward.RewardList;
import l2p.gameserver.model.reward.RewardType;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.SocialAction;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.serverpackets.components.IStaticPacket;
import l2p.gameserver.stats.Stats;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.templates.npc.Faction;
import l2p.gameserver.templates.npc.NpcTemplate;
import l2p.gameserver.utils.Location;

public class MonsterInstance extends NpcInstance
{
  public static final long serialVersionUID = 1L;
  private static final int MONSTER_MAINTENANCE_INTERVAL = 1000;
  private ScheduledFuture<?> minionMaintainTask;
  private MinionList minionList;
  private boolean _isSeeded;
  private int _seederId;
  private boolean _altSeed;
  private RewardItem _harvestItem;
  private final Lock harvestLock = new ReentrantLock();
  private int overhitAttackerId;
  private double _overhitDamage;
  private TIntHashSet _absorbersIds;
  private final Lock absorbLock = new ReentrantLock();
  private boolean _isSpoiled;
  private int spoilerId;
  private List<RewardItem> _sweepItems;
  private final Lock sweepLock = new ReentrantLock();
  private int _isChampion;
  private final double MIN_DISTANCE_FOR_USE_UD = 200.0D;
  private final double MIN_DISTANCE_FOR_CANCEL_UD = 50.0D;
  private final double UD_USE_CHANCE = 30.0D;

  public MonsterInstance(int objectId, NpcTemplate template)
  {
    super(objectId, template);

    minionList = new MinionList(this);
  }

  public boolean isMovementDisabled()
  {
    return (getNpcId() == 18344) || (getNpcId() == 18345) || (super.isMovementDisabled());
  }

  public boolean isLethalImmune()
  {
    return (_isChampion > 0) || (getNpcId() == 22215) || (getNpcId() == 22216) || (getNpcId() == 22217) || (getMaxHp() >= Config.LETHAL_IMMUNE_HP);
  }

  public boolean isFearImmune()
  {
    return (_isChampion > 0) || (super.isFearImmune());
  }

  public boolean isParalyzeImmune()
  {
    return (_isChampion > 0) || (super.isParalyzeImmune());
  }

  public boolean isAutoAttackable(Creature attacker)
  {
    return !attacker.isMonster();
  }

  public int getChampion()
  {
    return _isChampion;
  }

  public void setChampion()
  {
    if ((getReflection().canChampions()) && (canChampion()))
    {
      double random = Rnd.nextDouble();
      if (Config.ALT_CHAMPION_CHANCE2 / 100.0D >= random)
        setChampion(2);
      else if ((Config.ALT_CHAMPION_CHANCE1 + Config.ALT_CHAMPION_CHANCE2) / 100.0D >= random)
        setChampion(1);
      else
        setChampion(0);
    }
    else {
      setChampion(0);
    }
  }

  public void setChampion(int level) {
    if (level == 0)
    {
      removeSkillById(Integer.valueOf(4407));
      _isChampion = 0;
    }
    else
    {
      addSkill(SkillTable.getInstance().getInfo(4407, level));
      _isChampion = level;
    }
  }

  public boolean canChampion()
  {
    return (getTemplate().rewardExp > 0L) && (getTemplate().level <= Config.ALT_CHAMPION_TOP_LEVEL);
  }

  public TeamType getTeam()
  {
    return getChampion() == 1 ? TeamType.BLUE : getChampion() == 2 ? TeamType.RED : TeamType.NONE;
  }

  protected void onSpawn()
  {
    super.onSpawn();

    setCurrentHpMp(getMaxHp(), getMaxMp(), true);

    if (getMinionList().hasMinions())
    {
      if (minionMaintainTask != null)
      {
        minionMaintainTask.cancel(false);
        minionMaintainTask = null;
      }
      minionMaintainTask = ThreadPoolManager.getInstance().schedule(new MinionMaintainTask(), 1000L);
    }
  }

  protected void onDespawn()
  {
    setOverhitDamage(0.0D);
    setOverhitAttacker(null);
    clearSweep();
    clearHarvest();
    clearAbsorbers();

    super.onDespawn();
  }

  public MinionList getMinionList()
  {
    return minionList;
  }

  public Location getMinionPosition()
  {
    return Location.findPointToStay(this, 100, 150);
  }

  public void notifyMinionDied(MinionInstance minion)
  {
  }

  public void spawnMinion(MonsterInstance minion)
  {
    minion.setReflection(getReflection());
    if (getChampion() == 2)
      minion.setChampion(1);
    else
      minion.setChampion(0);
    minion.setHeading(getHeading());
    minion.setCurrentHpMp(minion.getMaxHp(), minion.getMaxMp(), true);
    minion.spawnMe(getMinionPosition());
  }

  public boolean hasMinions()
  {
    return getMinionList().hasMinions();
  }

  public void setReflection(Reflection reflection)
  {
    super.setReflection(reflection);

    if (hasMinions())
      for (MinionInstance m : getMinionList().getAliveMinions())
        m.setReflection(reflection);
  }

  protected void onDelete()
  {
    if (minionMaintainTask != null)
    {
      minionMaintainTask.cancel(false);
      minionMaintainTask = null;
    }

    getMinionList().deleteMinions();

    super.onDelete();
  }

  protected void onDeath(Creature killer)
  {
    if (minionMaintainTask != null)
    {
      minionMaintainTask.cancel(false);
      minionMaintainTask = null;
    }

    calculateRewards(killer);

    super.onDeath(killer);
  }

  protected void onReduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp)
  {
    if ((skill != null) && (skill.isOverhit()))
    {
      double overhitDmg = (getCurrentHp() - damage) * -1.0D;
      if (overhitDmg <= 0.0D)
      {
        setOverhitDamage(0.0D);
        setOverhitAttacker(null);
      }
      else
      {
        setOverhitDamage(overhitDmg);
        setOverhitAttacker(attacker);
      }
    }

    super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
  }

  public void calculateRewards(Creature lastAttacker)
  {
    Creature topDamager = getAggroList().getTopDamager();
    if ((lastAttacker == null) || (!lastAttacker.isPlayable())) {
      lastAttacker = topDamager;
    }
    if ((lastAttacker == null) || (!lastAttacker.isPlayable())) {
      return;
    }
    Player killer = lastAttacker.getPlayer();
    if (killer == null) {
      return;
    }
    Map aggroMap = getAggroList().getPlayableMap();

    Quest[] quests = getTemplate().getEventQuests(QuestEventType.MOB_KILLED_WITH_QUEST);
    if ((quests != null) && (quests.length > 0))
    {
      List players = null;
      if ((isRaid()) && (Config.ALT_NO_LASTHIT))
      {
        players = new ArrayList();
        for (Playable pl : aggroMap.keySet())
          if ((!pl.isDead()) && ((isInRangeZ(pl, Config.ALT_PARTY_DISTRIBUTION_RANGE)) || (killer.isInRangeZ(pl, Config.ALT_PARTY_DISTRIBUTION_RANGE))) && 
            (!players.contains(pl.getPlayer())))
            players.add(pl.getPlayer());
      }
      else if (killer.getParty() != null)
      {
        players = new ArrayList(killer.getParty().getMemberCount());
        for (Player pl : killer.getParty().getPartyMembers()) {
          if ((!pl.isDead()) && ((isInRangeZ(pl, Config.ALT_PARTY_DISTRIBUTION_RANGE)) || (killer.isInRangeZ(pl, Config.ALT_PARTY_DISTRIBUTION_RANGE))))
            players.add(pl);
        }
      }
      for (Quest quest : quests)
      {
        Player toReward = killer;
        if ((quest.getParty() != 0) && (players != null)) {
          if ((isRaid()) || (quest.getParty() == 2))
          {
            for (Player pl : players)
            {
              QuestState qs = pl.getQuestState(quest.getName());
              if ((qs != null) && (!qs.isCompleted()))
                quest.notifyKill(this, qs);
            }
            toReward = null;
          }
          else
          {
            List interested = new ArrayList(players.size());
            for (Player pl : players)
            {
              QuestState qs = pl.getQuestState(quest.getName());
              if ((qs != null) && (!qs.isCompleted())) {
                interested.add(pl);
              }
            }
            if (interested.isEmpty()) {
              continue;
            }
            toReward = (Player)interested.get(Rnd.get(interested.size()));
            if (toReward == null)
              toReward = killer;
          }
        }
        if (toReward == null)
          continue;
        QuestState qs = toReward.getQuestState(quest.getName());
        if ((qs != null) && (!qs.isCompleted())) {
          quest.notifyKill(this, qs);
        }
      }
    }

    Map rewards = new HashMap();
    for (AggroList.HateInfo info : aggroMap.values())
    {
      if (info.damage <= 1)
        continue;
      Playable attacker = (Playable)info.attacker;
      Player player = attacker.getPlayer();
      RewardInfo reward = (RewardInfo)rewards.get(player);
      if (reward == null)
        rewards.put(player, new RewardInfo(player, info.damage));
      else {
        reward.addDamage(info.damage);
      }
    }
    Player[] attackers = (Player[])rewards.keySet().toArray(new Player[rewards.size()]);
    double[] xpsp = new double[2];

    for (Player attacker : attackers)
    {
      if (attacker.isDead()) {
        continue;
      }
      RewardInfo reward = (RewardInfo)rewards.get(attacker);

      if (reward == null) {
        continue;
      }
      Party party = attacker.getParty();
      int maxHp = getMaxHp();

      xpsp[0] = 0.0D;
      xpsp[1] = 0.0D;

      if (party == null)
      {
        int damage = Math.min(reward._dmg, maxHp);
        if (damage > 0)
        {
          if (isInRangeZ(attacker, Config.ALT_PARTY_DISTRIBUTION_RANGE)) {
            xpsp = calculateExpAndSp(attacker.getLevel(), damage);
          }
          xpsp[0] = applyOverhit(killer, xpsp[0]);

          attacker.addExpAndCheckBonus(this, ()xpsp[0], ()xpsp[1], 1.0D);
        }
        rewards.remove(attacker);
      }
      else
      {
        int partyDmg = 0;
        int partylevel = 1;
        List rewardedMembers = new ArrayList();
        for (Player partyMember : party.getPartyMembers())
        {
          RewardInfo ai = (RewardInfo)rewards.remove(partyMember);
          if ((partyMember.isDead()) || (!isInRangeZ(partyMember, Config.ALT_PARTY_DISTRIBUTION_RANGE)))
            continue;
          if (ai != null) {
            partyDmg += ai._dmg;
          }
          rewardedMembers.add(partyMember);
          if (partyMember.getLevel() > partylevel)
            partylevel = partyMember.getLevel();
        }
        partyDmg = Math.min(partyDmg, maxHp);
        if (partyDmg <= 0)
          continue;
        xpsp = calculateExpAndSp(partylevel, partyDmg);
        double partyMul = partyDmg / maxHp;
        xpsp[0] *= partyMul;
        xpsp[1] *= partyMul;
        xpsp[0] = applyOverhit(killer, xpsp[0]);
        party.distributeXpAndSp(xpsp[0], xpsp[1], rewardedMembers, lastAttacker, this);
      }

    }

    CursedWeaponsManager.getInstance().dropAttackable(this, killer);

    if ((topDamager == null) || (!topDamager.isPlayable())) {
      return;
    }
    for (Map.Entry entry : getTemplate().getRewards().entrySet())
      rollRewards(entry, lastAttacker, topDamager);
  }

  public void onRandomAnimation()
  {
    if (System.currentTimeMillis() - _lastSocialAction > 10000L)
    {
      broadcastPacket(new L2GameServerPacket[] { new SocialAction(getObjectId(), 1) });
      _lastSocialAction = System.currentTimeMillis();
    }
  }

  public void startRandomAnimation()
  {
  }

  public int getKarma()
  {
    return 0;
  }

  public void addAbsorber(Player attacker)
  {
    if (attacker == null) {
      return;
    }
    if (getCurrentHpPercents() > 50.0D) {
      return;
    }
    absorbLock.lock();
    try
    {
      if (_absorbersIds == null) {
        _absorbersIds = new TIntHashSet();
      }
      _absorbersIds.add(attacker.getObjectId());
    }
    finally
    {
      absorbLock.unlock();
    }
  }

  public boolean isAbsorbed(Player player)
  {
    absorbLock.lock();
    try
    {
      int i;
      if (_absorbersIds == null) {
        i = 0;
        return i;
      }
      if (!_absorbersIds.contains(player.getObjectId())) {
        i = 0;
        return i;
      } } finally { absorbLock.unlock();
    }
    return true;
  }

  public void clearAbsorbers()
  {
    absorbLock.lock();
    try
    {
      if (_absorbersIds != null)
        _absorbersIds.clear();
    }
    finally
    {
      absorbLock.unlock();
    }
  }

  public RewardItem takeHarvest()
  {
    harvestLock.lock();
    try
    {
      RewardItem harvest = _harvestItem;
      clearHarvest();
      RewardItem localRewardItem1 = harvest;
      return localRewardItem1; } finally { harvestLock.unlock(); } throw localObject;
  }

  public void clearHarvest()
  {
    harvestLock.lock();
    try
    {
      _harvestItem = null;
      _altSeed = false;
      _seederId = 0;
      _isSeeded = false;
    }
    finally
    {
      harvestLock.unlock();
    }
  }

  public boolean setSeeded(Player player, int seedId, boolean altSeed)
  {
    harvestLock.lock();
    try
    {
      if (isSeeded()) {
        int i = 0;
        return i;
      }
      _isSeeded = true;
      _altSeed = altSeed;
      _seederId = player.getObjectId();
      _harvestItem = new RewardItem(Manor.getInstance().getCropType(seedId));

      if (getTemplate().rateHp > 1.0D)
        _harvestItem.count = Rnd.get(Math.round(getTemplate().rateHp), Math.round(1.5D * getTemplate().rateHp));
    }
    finally
    {
      harvestLock.unlock();
    }

    return true;
  }

  public boolean isSeeded(Player player)
  {
    return (isSeeded()) && (_seederId == player.getObjectId()) && (getDeadTime() < 20000L);
  }

  public boolean isSeeded()
  {
    return _isSeeded;
  }

  public boolean isSpoiled()
  {
    return _isSpoiled;
  }

  public boolean isSpoiled(Player player)
  {
    if (!isSpoiled()) {
      return false;
    }

    if ((player.getObjectId() == spoilerId) && (getDeadTime() < 20000L)) {
      return true;
    }
    if (player.isInParty()) {
      for (Player pm : player.getParty().getPartyMembers())
        if ((pm.getObjectId() == spoilerId) && (getDistance(pm) < Config.ALT_PARTY_DISTRIBUTION_RANGE))
          return true;
    }
    return false;
  }

  public boolean setSpoiled(Player player)
  {
    sweepLock.lock();
    try
    {
      if (isSpoiled()) {
        int i = 0;
        return i;
      }
      _isSpoiled = true;
      spoilerId = player.getObjectId();
    }
    finally
    {
      sweepLock.unlock();
    }
    return true;
  }

  public boolean isSweepActive()
  {
    sweepLock.lock();
    try
    {
      int i = (_sweepItems != null) && (_sweepItems.size() > 0) ? 1 : 0;
      return i; } finally { sweepLock.unlock(); } throw localObject;
  }

  public List<RewardItem> takeSweep()
  {
    sweepLock.lock();
    try
    {
      List sweep = _sweepItems;
      clearSweep();
      List localList1 = sweep;
      return localList1; } finally { sweepLock.unlock(); } throw localObject;
  }

  public void clearSweep()
  {
    sweepLock.lock();
    try
    {
      _isSpoiled = false;
      spoilerId = 0;
      _sweepItems = null;
    }
    finally
    {
      sweepLock.unlock();
    }
  }

  public void rollRewards(Map.Entry<RewardType, RewardList> entry, Creature lastAttacker, Creature topDamager)
  {
    RewardType type = (RewardType)entry.getKey();
    RewardList list = (RewardList)entry.getValue();

    if ((type == RewardType.SWEEP) && (!isSpoiled())) {
      return;
    }
    Creature activeChar = type == RewardType.SWEEP ? lastAttacker : topDamager;
    Player activePlayer = activeChar.getPlayer();

    if (activePlayer == null) {
      return;
    }
    int diff = calculateLevelDiffForDrop(topDamager.getLevel());
    double mod = calcStat(Stats.REWARD_MULTIPLIER, 1.0D, activeChar, null);
    mod *= Experience.penaltyModifier(diff, 9.0D);

    List rewardItems = list.roll(activePlayer, mod, this instanceof RaidBossInstance);
    switch (1.$SwitchMap$l2p$gameserver$model$reward$RewardType[type.ordinal()])
    {
    case 1:
      _sweepItems = rewardItems;
      break;
    default:
      for (RewardItem drop : rewardItems)
      {
        if ((isSeeded()) && (!_altSeed) && (!drop.isAdena))
          continue;
        dropItem(activePlayer, drop.itemId, drop.count);
      }
    }
  }

  private double[] calculateExpAndSp(int level, long damage)
  {
    int diff = level - getLevel();
    if ((level > 77) && (diff > 3) && (diff <= 5)) {
      diff += 3;
    }
    double xp = getExpReward() * damage / getMaxHp();
    double sp = getSpReward() * damage / getMaxHp();

    if (diff > 5)
    {
      double mod = Math.pow(0.83D, diff - 5);
      xp *= mod;
      sp *= mod;
    }

    xp = Math.max(0.0D, xp);
    sp = Math.max(0.0D, sp);

    return new double[] { xp, sp };
  }

  private double applyOverhit(Player killer, double xp)
  {
    if ((xp > 0.0D) && (killer.getObjectId() == overhitAttackerId))
    {
      int overHitExp = calculateOverhitExp(xp);
      killer.sendPacket(new IStaticPacket[] { Msg.OVER_HIT, new SystemMessage(362).addNumber(overHitExp) });
      xp += overHitExp;
    }
    return xp;
  }

  public void setOverhitAttacker(Creature attacker)
  {
    overhitAttackerId = (attacker == null ? 0 : attacker.getObjectId());
  }

  public double getOverhitDamage()
  {
    return _overhitDamage;
  }

  public void setOverhitDamage(double damage)
  {
    _overhitDamage = damage;
  }

  public int calculateOverhitExp(double normalExp)
  {
    double overhitPercentage = getOverhitDamage() * 100.0D / getMaxHp();
    if (overhitPercentage > 25.0D)
      overhitPercentage = 25.0D;
    double overhitExp = overhitPercentage / 100.0D * normalExp;
    setOverhitAttacker(null);
    setOverhitDamage(0.0D);
    return (int)Math.round(overhitExp);
  }

  public boolean isAggressive()
  {
    return ((Config.ALT_CHAMPION_CAN_BE_AGGRO) || (getChampion() == 0)) && (super.isAggressive());
  }

  public Faction getFaction()
  {
    return (Config.ALT_CHAMPION_CAN_BE_SOCIAL) || (getChampion() == 0) ? super.getFaction() : Faction.NONE;
  }

  public void reduceCurrentHp(double i, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect, boolean transferDamage, boolean isDot, boolean sendMessage)
  {
    checkUD(attacker, i);
    super.reduceCurrentHp(i, attacker, skill, awake, standUp, directHp, canReflect, transferDamage, isDot, sendMessage);
  }

  private void checkUD(Creature attacker, double damage)
  {
    if ((getTemplate().baseAtkRange > 200.0D) || (getLevel() < 20) || (getLevel() > 78) || (attacker.getLevel() - getLevel() > 9) || (getLevel() - attacker.getLevel() > 9)) {
      return;
    }
    if ((isMinion()) || (getMinionList() != null) || (isRaid()) || ((this instanceof ReflectionBossInstance)) || ((this instanceof ChestInstance)) || (getChampion() > 0)) {
      return;
    }
    int skillId = 5044;
    int skillLvl = 1;
    if ((getLevel() >= 41) || (getLevel() <= 60))
      skillLvl = 2;
    else if (getLevel() > 60) {
      skillLvl = 3;
    }
    double distance = getDistance(attacker);
    if (distance <= 50.0D)
    {
      if ((getEffectList() != null) && (getEffectList().getEffectsBySkillId(skillId) != null))
        for (Effect e : getEffectList().getEffectsBySkillId(skillId))
          e.exit();
    }
    else if (distance >= 200.0D)
    {
      double chance = 30.0D / (getMaxHp() / damage);
      if (Rnd.chance(chance))
      {
        Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
        if (skill != null)
          skill.getEffects(this, this, false, false);
      }
    }
  }

  public boolean isMonster()
  {
    return true;
  }

  public Clan getClan()
  {
    return null;
  }

  public boolean isInvul()
  {
    return _isInvul;
  }

  public class MinionMaintainTask extends RunnableImpl
  {
    public MinionMaintainTask()
    {
    }

    public void runImpl()
      throws Exception
    {
      if (isDead())
        return;
      getMinionList().spawnMinions();
    }
  }

  protected static final class RewardInfo
  {
    protected Creature _attacker;
    protected int _dmg = 0;

    public RewardInfo(Creature attacker, int dmg)
    {
      _attacker = attacker;
      _dmg = dmg;
    }

    public void addDamage(int dmg)
    {
      if (dmg < 0) {
        dmg = 0;
      }
      _dmg += dmg;
    }

    public int hashCode()
    {
      return _attacker.getObjectId();
    }
  }
}
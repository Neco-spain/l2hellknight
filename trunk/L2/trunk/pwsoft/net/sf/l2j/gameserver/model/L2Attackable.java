package net.sf.l2j.gameserver.model;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastMap.Entry;
import javolution.util.FastTable;
import net.sf.l2j.Config;
import net.sf.l2j.Config.EventReward;
import net.sf.l2j.gameserver.GeoData;
import net.sf.l2j.gameserver.ItemsAutoDestroy;
import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2AttackableAI;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.ai.L2SiegeGuardAI;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.CustomServerData;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.instancemanager.CursedWeaponsManager;
import net.sf.l2j.gameserver.model.actor.instance.L2BossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2GrandBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MinionInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2RaidBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SummonInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.AttackableKnownList;
import net.sf.l2j.gameserver.model.actor.knownlist.CharKnownList;
import net.sf.l2j.gameserver.model.base.SoulCrystal;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.Quest.QuestEventType;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.templates.L2EtcItemType;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.L2NpcTemplate.AbsorbCrystalType;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.util.Rnd;

public class L2Attackable extends L2NpcInstance
{
  private ConcurrentHashMap<L2Character, AggroInfo> _aggroList = new ConcurrentHashMap();

  private boolean _isReturningToSpawnPoint = false;

  private long _lastMove = 0L;
  private boolean _canReturnToSpawnPoint = true;
  private RewardItem[] _sweepItems;
  private RewardItem[] _harvestItems;
  private boolean _seeded;
  private int _seedType = 0;
  private L2PcInstance _seeder = null;
  private boolean _overhit;
  private double _overhitDamage;
  private L2Character _overhitAttacker;
  private boolean _absorbed;
  private FastMap<L2PcInstance, AbsorberInfo> _absorbersList = new FastMap().shared("L2Attackable._absorbersList");
  private boolean _mustGiveExpSp;

  public final ConcurrentHashMap<L2Character, AggroInfo> getAggroList()
  {
    return _aggroList;
  }

  public boolean isReturningToSpawnPoint()
  {
    return _isReturningToSpawnPoint;
  }

  public final void setisReturningToSpawnPoint(boolean value) {
    _isReturningToSpawnPoint = value;
  }

  public final boolean canReturnToSpawnPoint()
  {
    if (System.currentTimeMillis() - _lastMove < 45000L)
    {
      return false;
    }

    if (Rnd.get(100) > 13) {
      return false;
    }

    _lastMove = System.currentTimeMillis();
    return _canReturnToSpawnPoint;
  }

  public final void setCanReturnToSpawnPoint(boolean value) {
    _canReturnToSpawnPoint = value;
  }

  public L2Attackable(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
    getKnownList();
    _mustGiveExpSp = true;
  }

  public AttackableKnownList getKnownList()
  {
    if ((super.getKnownList() == null) || (!(super.getKnownList() instanceof AttackableKnownList))) {
      setKnownList(new AttackableKnownList(this));
    }
    return (AttackableKnownList)super.getKnownList();
  }

  public L2CharacterAI getAI()
  {
    if (_ai == null) {
      _ai = new L2AttackableAI(new L2Character.AIAccessor(this));
    }

    return _ai;
  }

  @Deprecated
  public boolean getCondition2(L2Character target)
  {
    if ((target.isL2Folk()) || (target.isL2Door())) {
      return false;
    }

    if ((target.isAlikeDead()) || (!isInsideRadius(target, getAggroRange(), false, false)) || (Math.abs(getZ() - target.getZ()) > 100))
    {
      return false;
    }

    return !target.isInvul();
  }

  public void reduceCurrentHp(double damage, L2Character attacker)
  {
    reduceCurrentHp(damage, attacker, true);
  }

  public void reduceCurrentHp(double damage, L2Character attacker, boolean awake)
  {
    if (isEventMob) {
      return;
    }

    if (attacker != null) {
      addDamage(attacker, (int)damage);
    }

    if (isL2Monster()) {
      L2MonsterInstance master = (L2MonsterInstance)this;
      if ((this instanceof L2MinionInstance)) {
        master = ((L2MinionInstance)this).getLeader();
        if ((!master.isInCombat()) && (!master.isDead())) {
          master.addDamage(attacker, 1);
        }
      }
      if (master.hasMinions()) {
        master.callMinionsToAssist(attacker);
      }

    }

    super.reduceCurrentHp(damage, attacker, awake);
  }

  public synchronized void setMustRewardExpSp(boolean value) {
    _mustGiveExpSp = value;
  }

  public synchronized boolean getMustRewardExpSP() {
    return _mustGiveExpSp;
  }

  public boolean doDie(L2Character killer)
  {
    if (!super.doDie(killer)) {
      return false;
    }

    try
    {
      if (killer.isPlayer())
        levelSoulCrystals(killer);
    }
    catch (Exception e) {
      _log.log(Level.SEVERE, "", e);
    }

    try
    {
      L2PcInstance player = killer.getPlayer();
      if ((player != null) && 
        (getTemplate().getEventQuests(Quest.QuestEventType.MOBKILLED) != null)) {
        for (Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.MOBKILLED))
          quest.notifyKill(this, player, killer instanceof L2Summon);
      }
    }
    catch (Exception e)
    {
      _log.log(Level.SEVERE, "", e);
    }

    if (killer.isPlayer()) {
      doNpcChat(2, killer.getName());
    }

    setChampion(false);
    _aggroList.clear();
    _absorbersList.clear();
    getKnownList().gc();
    return true;
  }

  protected void calculateRewards(L2Character lastAttacker)
  {
    FastMap rewards = new FastMap().shared("L2Attackable.rewards");
    try {
      if ((_aggroList == null) || (_aggroList.isEmpty()))
      {
        return;
      }
      doItemDrop(lastAttacker);

      doEventDrop(lastAttacker);

      if (!getMustRewardExpSP()) {
        return;
      }
      int rewardCount = 0;

      L2Character cha = null;
      AggroInfo info = null;
      for (Map.Entry entry : _aggroList.entrySet()) {
        cha = (L2Character)entry.getKey();
        info = (AggroInfo)entry.getValue();
        if ((cha == null) || (info == null))
        {
          continue;
        }

        L2Character attacker = info._attacker;

        int damage = info._damage;

        if (damage > 1)
        {
          L2Character ddealer;
          L2Character ddealer;
          if ((attacker.isSummon()) || ((attacker.isPet()) && (((L2PetInstance)attacker).getPetData().getOwnerExpTaken() > 0.0F)))
            ddealer = ((L2Summon)attacker).getOwner();
          else {
            ddealer = info._attacker;
          }

          if (!Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, ddealer, true))
          {
            continue;
          }

          RewardInfo reward = (RewardInfo)rewards.get(ddealer);

          if (reward == null) {
            reward = new RewardInfo(ddealer, damage);
            rewardCount++;
          } else {
            reward.addDamage(damage);
          }
          rewards.put(ddealer, reward);
        }
      }
      if (!rewards.isEmpty())
      {
        entry = rewards.head(); for (FastMap.Entry end = rewards.tail(); (entry = entry.getNext()) != end; ) {
          RewardInfo reward = (RewardInfo)entry.getValue();
          if (reward == null)
          {
            continue;
          }

          float penalty = 0.0F;

          L2Character attacker = reward._attacker;

          int damage = reward._dmg;
          L2Party attackerParty;
          if (attacker.isPet()) {
            attackerParty = attacker.getParty();
          }
          else
          {
            L2Party attackerParty;
            if (attacker.isPlayer())
              attackerParty = attacker.getParty();
            else
              return;
          }
          L2Party attackerParty;
          if ((attacker.isPlayer()) && (attacker.getPet() != null) && (attacker.getPet().isSummon())) {
            penalty = ((L2SummonInstance)attacker.getPet()).getExpPenalty();
          }

          if (damage > getMaxHp()) {
            damage = getMaxHp();
          }

          if (attackerParty == null)
          {
            if (!attacker.getKnownList().knowsObject(this))
            {
              continue;
            }

            int levelDiff = attacker.getLevel() - getLevel();

            int[] tmp = calculateExpAndSp(levelDiff, damage);
            long exp = tmp[0];
            exp = ()((float)exp * (1.0F - penalty));
            int sp = tmp[1];

            if ((Config.L2JMOD_CHAMPION_ENABLE) && (isChampion())) {
              exp *= Config.L2JMOD_CHAMPION_REWARDS;
              sp *= Config.L2JMOD_CHAMPION_REWARDS;
            }

            if ((attacker.isPlayer()) && 
              (isOverhit()) && (attacker == getOverhitAttacker())) {
              attacker.sendUserPacket(Static.OVER_HIT);
              exp += calculateOverhitExp(exp);
            }

            if (attacker.canExp()) {
              attacker.addExpAndSp(Math.round(attacker.calcStat(Stats.EXPSP_RATE, exp, null, null)), (int)attacker.calcStat(Stats.EXPSP_RATE, sp, null, null)); continue;
            }

          }

          int partyDmg = 0;
          float partyMul = 1.0F;
          int partyLvl = 0;

          FastTable rewardedMembers = new FastTable();
          List groupMembers;
          List groupMembers;
          if (attackerParty.isInCommandChannel())
            groupMembers = attackerParty.getCommandChannel().getMembers();
          else {
            groupMembers = attackerParty.getPartyMembers();
          }

          for (L2PcInstance pl : groupMembers) {
            if ((pl == null) || (pl.isDead()))
            {
              continue;
            }

            RewardInfo reward2 = (RewardInfo)rewards.get(pl);

            if (reward2 != null) {
              if (Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, pl, true)) {
                partyDmg += reward2._dmg;
                rewardedMembers.add(pl);
                if (pl.getLevel() > partyLvl) {
                  if (attackerParty.isInCommandChannel())
                    partyLvl = attackerParty.getCommandChannel().getLevel();
                  else {
                    partyLvl = pl.getLevel();
                  }
                }
              }
              rewards.remove(pl);
            }
            else if (Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, pl, true)) {
              rewardedMembers.add(pl);
              if (pl.getLevel() > partyLvl) {
                if (attackerParty.isInCommandChannel())
                  partyLvl = attackerParty.getCommandChannel().getLevel();
                else {
                  partyLvl = pl.getLevel();
                }
              }
            }

            L2PlayableInstance summon = pl.getPet();
            if ((summon != null) && (summon.isPet())) {
              reward2 = (RewardInfo)rewards.get(summon);
              if (reward2 != null)
              {
                if (Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, summon, true)) {
                  partyDmg += reward2._dmg;
                  rewardedMembers.add(summon);
                  if (summon.getLevel() > partyLvl) {
                    partyLvl = summon.getLevel();
                  }
                }
                rewards.remove(summon);
              }
            }

          }

          if (partyDmg < getMaxHp()) {
            partyMul = partyDmg / getMaxHp();
          }

          if (partyDmg > getMaxHp()) {
            partyDmg = getMaxHp();
          }

          int levelDiff = partyLvl - getLevel();

          int[] tmp = calculateExpAndSp(levelDiff, partyDmg);
          long exp = tmp[0];
          int sp = tmp[1];

          if ((Config.L2JMOD_CHAMPION_ENABLE) && (isChampion())) {
            exp *= Config.L2JMOD_CHAMPION_REWARDS;
            sp *= Config.L2JMOD_CHAMPION_REWARDS;
          }

          exp = ()((float)exp * partyMul);
          sp = (int)(sp * partyMul);

          if ((attacker.isPlayer()) && 
            (isOverhit()) && (attacker == getOverhitAttacker())) {
            attacker.sendUserPacket(Static.OVER_HIT);
            exp += calculateOverhitExp(exp);
          }

          if (partyDmg > 0) {
            attackerParty.distributeXpAndSp(exp, sp, rewardedMembers, partyLvl);
          }
          rewardedMembers.clear();
        }
      }
    }
    catch (Exception e)
    {
      FastMap.Entry entry;
      _log.log(Level.SEVERE, "", e);
    } finally {
      rewards.clear();
    }
  }

  public void addDamage(L2Character attacker, int damage)
  {
    addDamageHate(attacker, damage, damage);
  }

  public void addDamageHate(L2Character attacker, int damage, int aggro)
  {
    if (attacker == null) {
      return;
    }
    if (_aggroList == null) {
      _aggroList = new ConcurrentHashMap();
    }

    AggroInfo ai = (AggroInfo)_aggroList.get(attacker);
    if (ai == null) {
      ai = new AggroInfo(attacker);
      ai._damage = 0;
      ai._hate = 0;
      _aggroList.put(attacker, ai);
    }

    if (aggro < 0) {
      ai._hate -= aggro * 150 / (getLevel() + 7);
      aggro = -aggro;
    }
    else if (damage == 0) {
      ai._hate += aggro;
    }
    else {
      ai._hate += aggro * 100 / (getLevel() + 7);
    }

    ai._damage += damage;

    if ((aggro > 0) && (getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)) {
      getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
    }

    if (damage > 0) {
      getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, attacker);
      try
      {
        L2PcInstance player = attacker.getPlayer();
        if (player != null)
        {
          if (getTemplate().getEventQuests(Quest.QuestEventType.MOBGOTATTACKED) != null)
            for (Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.MOBGOTATTACKED))
              quest.notifyAttack(this, player, damage, attacker.isL2Summon());
        }
      }
      catch (Exception e)
      {
        _log.log(Level.SEVERE, "", e);
      }
    }
  }

  public void reduceHate(L2Character target, int amount) {
    if ((getAI() instanceof L2SiegeGuardAI))
    {
      stopHating(target);
      setTarget(null);
      getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
      return;
    }
    if (target == null)
    {
      L2Character mostHated = getMostHated();
      if (mostHated == null)
      {
        ((L2AttackableAI)getAI()).setGlobalAggro(-25);
        return;
      }
      for (Map.Entry entry : _aggroList.entrySet()) {
        L2Character cha = (L2Character)entry.getKey();
        AggroInfo info = (AggroInfo)entry.getValue();
        if ((cha == null) || (info == null)) {
          return;
        }

        info._hate -= amount;
      }

      amount = getHating(mostHated);
      if (amount <= 0) {
        ((L2AttackableAI)getAI()).setGlobalAggro(-25);
        clearAggroList();
        getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
        setWalking();
      }
      return;
    }
    AggroInfo ai = (AggroInfo)_aggroList.get(target);
    if (ai == null) {
      return;
    }
    ai._hate -= amount;

    if ((ai._hate <= 0) && 
      (getMostHated() == null)) {
      ((L2AttackableAI)getAI()).setGlobalAggro(-25);
      clearAggroList();
      getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
      setWalking();
    }
  }

  public void stopHating(L2Character target)
  {
    if (target == null) {
      return;
    }
    AggroInfo ai = (AggroInfo)_aggroList.get(target);
    if (ai == null) {
      return;
    }
    ai._hate = 0;
  }

  public L2Character getMostHated()
  {
    if ((_aggroList == null) || (_aggroList.isEmpty()) || (isAlikeDead())) {
      return null;
    }

    L2Character mostHated = null;
    int maxHate = 0;

    for (Map.Entry entry : _aggroList.entrySet()) {
      L2Character cha = (L2Character)entry.getKey();
      AggroInfo info = (AggroInfo)entry.getValue();
      if ((cha == null) || (info == null))
      {
        continue;
      }
      if ((info._attacker.isAlikeDead()) || (!getKnownList().knowsObject(info._attacker)) || (!info._attacker.isVisible())) {
        info._hate = 0;
      }

      if (info._hate > maxHate) {
        mostHated = info._attacker;
        maxHate = info._hate;
      }
    }
    return mostHated;
  }

  public int getHating(L2Character target)
  {
    if (target == null) {
      return 0;
    }
    if ((_aggroList == null) || (_aggroList.isEmpty())) {
      return 0;
    }

    AggroInfo ai = (AggroInfo)_aggroList.get(target);
    if (ai == null) {
      return 0;
    }

    if ((ai._attacker.isPlayer()) && ((ai._attacker.getPlayer().isInvisible()) || (ai._attacker.isInvul()))) {
      _aggroList.remove(target);
      return 0;
    }
    if (!ai._attacker.isVisible()) {
      _aggroList.remove(target);
      return 0;
    }
    if (ai._attacker.isAlikeDead()) {
      ai._hate = 0;
      return 0;
    }
    return ai._hate;
  }

  public int getDropChance(int dropChance, L2PcInstance lastAttacker, int itemId)
  {
    switch (itemId) {
    case 57:
      return dropChance * (int)Config.RATE_DROP_ADENA;
    case 6360:
    case 6361:
    case 6362:
      return dropChance * (int)Config.RATE_DROP_SEAL_STONE;
    }
    if (isGrandRaid())
      return dropChance * (int)Config.RATE_DROP_ITEMS_BY_GRANDRAID;
    if (isRaid()) {
      return dropChance * (int)Config.RATE_DROP_ITEMS_BY_RAID;
    }

    if ((Config.L2JMOD_CHAMPION_ENABLE) && (isChampion())) {
      dropChance *= Config.L2JMOD_CHAMPION_REWARDS;
    }

    if ((Config.PREMIUM_ENABLE) && (lastAttacker.isPremium())) {
      dropChance = (int)(dropChance * Config.PREMIUM_ITEMDROP);
    }

    return dropChance * (int)Config.RATE_DROP_ITEMS;
  }

  public int getDropCount(int itemCount, L2PcInstance lastAttacker, int itemId, int min, int max)
  {
    if (isEpicJewerly(itemId)) {
      return 1;
    }

    if (isProtectedPremium(itemId)) {
      return Rnd.get(min, max);
    }

    switch (itemId) {
    case 57:
      if ((Config.L2JMOD_CHAMPION_ENABLE) && (isChampion())) {
        itemCount *= Config.L2JMOD_CHAMPION_ADENAS_REWARDS;
      }

      if ((Config.PREMIUM_ENABLE) && (lastAttacker.isPremium())) {
        itemCount = (int)(itemCount * Config.PREMIUM_ADENAMUL);
      }

      if (Config.RATE_DROP_ADENAMUL > 1.0F) {
        itemCount = (int)(itemCount * Config.RATE_DROP_ADENAMUL);
      }

      return itemCount;
    case 6360:
    case 6361:
    case 6362:
      return itemCount * (int)Config.RATE_MUL_SEAL_STONE;
    }
    if (isGrandRaid())
      return itemCount * (int)Config.RATE_DROP_ITEMSGRANDMUL;
    if (isRaid()) {
      return itemCount * (int)Config.RATE_DROP_ITEMSRAIDMUL;
    }

    if ((Config.PREMIUM_ENABLE) && (lastAttacker.isPremium())) {
      itemCount = (int)(itemCount * Config.PREMIUM_ITEMDROPMUL);
    }

    return itemCount;
  }

  private RewardItem calculateRewardItem(L2PcInstance lastAttacker, L2DropData drop, int levelModifier, boolean isSweep)
  {
    float dropChance = drop.getChance();

    int deepBlueDrop = 1;
    if ((Config.DEEPBLUE_DROP_RULES) && 
      (levelModifier > 0))
    {
      deepBlueDrop = 3;
      if (drop.getItemId() == 57) {
        deepBlueDrop = getDropChance(deepBlueDrop, lastAttacker, 57);
      }

    }

    if (deepBlueDrop == 0)
    {
      deepBlueDrop = 1;
    }

    if (Config.DEEPBLUE_DROP_RULES) {
      dropChance = (drop.getChance() - drop.getChance() * levelModifier / 100) / deepBlueDrop;
    }

    if (isSweep)
      dropChance *= Config.RATE_DROP_SPOIL;
    else {
      dropChance = getDropChance((int)dropChance, lastAttacker, drop.getItemId());
    }

    dropChance = Math.round(dropChance);

    if (dropChance < 1.0F) {
      dropChance = 1.0F;
    }

    int minCount = drop.getMinDrop();
    int maxCount = drop.getMaxDrop();
    int itemCount = 0;

    if ((dropChance > 1000000.0F) && (!Config.PRECISE_DROP_CALCULATION)) {
      int multiplier = (int)dropChance / 1000000;
      if (minCount < maxCount)
        itemCount += Rnd.get(minCount * multiplier, maxCount * multiplier);
      else if (minCount == maxCount)
        itemCount += minCount * multiplier;
      else {
        itemCount += multiplier;
      }

      dropChance %= 1000000.0F;
    }

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

      dropChance -= 1000000.0F;
    }

    itemCount = getDropCount(itemCount, lastAttacker, drop.getItemId(), minCount, maxCount);
    if (itemCount > 0) {
      return new RewardItem(drop.getItemId(), itemCount);
    }
    return null;
  }

  private RewardItem calculateCategorizedRewardItem(L2PcInstance lastAttacker, L2DropCategory categoryDrops, int levelModifier)
  {
    if (categoryDrops == null) {
      return null;
    }

    int basecategoryDropChance = categoryDrops.getCategoryChance();
    int categoryDropChance = basecategoryDropChance;

    int deepBlueDrop = 1;
    if ((Config.DEEPBLUE_DROP_RULES) && 
      (levelModifier > 0))
    {
      deepBlueDrop = 3;
    }

    if (deepBlueDrop == 0)
    {
      deepBlueDrop = 1;
    }

    if (Config.DEEPBLUE_DROP_RULES) {
      categoryDropChance = (categoryDropChance - categoryDropChance * levelModifier / 100) / deepBlueDrop;
    }

    categoryDropChance = getDropChance(categoryDropChance, lastAttacker, 0);

    categoryDropChance = Math.round(categoryDropChance);

    if (categoryDropChance < 1) {
      categoryDropChance = 1;
    }

    if (Rnd.get(1000000) < categoryDropChance) {
      L2DropData drop = categoryDrops.dropOne(isRaid());
      if (drop == null) {
        return null;
      }

      int dropChance = drop.getChance();
      dropChance = getDropChance(dropChance, lastAttacker, drop.getItemId());
      dropChance = Math.round(dropChance);

      if (dropChance < 1000000) {
        dropChance = 1000000;
      }

      int min = drop.getMinDrop();
      int max = drop.getMaxDrop();

      int itemCount = 0;

      if ((dropChance > 1000000) && (!Config.PRECISE_DROP_CALCULATION)) {
        int multiplier = dropChance / 1000000;
        if (min < max)
          itemCount += Rnd.get(min * multiplier, max * multiplier);
        else if (min == max)
          itemCount += min * multiplier;
        else {
          itemCount += multiplier;
        }

        dropChance %= 1000000;
      }

      int random = Rnd.get(1000000);
      while (random < dropChance)
      {
        if (min < max)
          itemCount += Rnd.get(min, max);
        else if (min == max)
          itemCount += min;
        else {
          itemCount++;
        }

        dropChance -= 1000000;
      }

      itemCount = getDropCount(itemCount, lastAttacker, drop.getItemId(), min, max);
      if (itemCount > 0) {
        return new RewardItem(drop.getItemId(), itemCount);
      }
    }
    return null;
  }

  private int calculateLevelModifierForDrop(L2PcInstance lastAttacker)
  {
    if (Config.DEEPBLUE_DROP_RULES) {
      int highestLevel = lastAttacker.getLevel();

      if ((getAttackByList() != null) && (!getAttackByList().isEmpty())) {
        for (L2Character atkChar : getAttackByList()) {
          if ((atkChar != null) && (atkChar.getLevel() > highestLevel)) {
            highestLevel = atkChar.getLevel();
          }
        }

      }

      if (highestLevel - 9 >= getLevel()) {
        return (highestLevel - (getLevel() + 8)) * 9;
      }
    }

    return 0;
  }

  public void doItemDrop(L2Character lastAttacker) {
    doItemDrop(getTemplate(), lastAttacker);
  }

  public void doItemDrop(L2NpcTemplate npcTemplate, L2Character lastAttacker)
  {
    L2PcInstance player = lastAttacker.getPlayer();
    if (player == null) {
      return;
    }
    int levelModifier = calculateLevelModifierForDrop(player);

    if ((levelModifier == 0) && (player.getLevel() > 20))
    {
      CursedWeaponsManager.getInstance().checkDrop(this, player);
    }

    for (L2DropCategory cat : npcTemplate.getDropData()) {
      RewardItem item = null;
      if (cat.isSweep())
      {
        if (isSpoil()) {
          FastList sweepList = new FastList();

          for (L2DropData drop : cat.getAllDrops()) {
            item = calculateRewardItem(player, drop, levelModifier, true);
            if (item == null)
            {
              continue;
            }

            sweepList.add(item);
          }

          if (!sweepList.isEmpty())
            _sweepItems = ((RewardItem[])sweepList.toArray(new RewardItem[sweepList.size()]));
        }
      }
      else {
        if (isSeeded()) {
          L2DropData drop = cat.dropSeedAllowedDropsOnly();
          if (drop == null)
          {
            continue;
          }
          item = calculateRewardItem(player, drop, levelModifier, false);
        } else {
          item = calculateCategorizedRewardItem(player, cat, levelModifier);
        }

        if (item != null)
        {
          if (((this instanceof L2RaidBossInstance)) || ((this instanceof L2GrandBossInstance))) {
            if ((Config.ALT_EPIC_JEWERLY) && (isEpicJewerly(item.getItemId())))
            {
              continue;
            }
            broadcastPacket(SystemMessage.id(SystemMessageId.S1_DIED_DROPPED_S3_S2).addString(getName()).addItemName(item.getItemId()).addNumber(item.getCount()));
          }

          if ((Config.VS_AUTOLOOT) && (!player.getAutoLoot())) {
            dropItem(player, item);
            continue;
          }

          if ((Config.AUTO_LOOT_RAID) && (isRaid()))
            player.doAutoLoot(this, item);
          else if ((Config.AUTO_LOOT) && (!isRaid()))
            player.doAutoLoot(this, item);
          else {
            dropItem(player, item);
          }
        }
      }

    }

    if ((Config.L2JMOD_CHAMPION_ENABLE) && (isChampion()) && (player.getLevel() <= getLevel()) && (Config.L2JMOD_CHAMPION_REWARD > 0) && (Rnd.get(100) < Config.L2JMOD_CHAMPION_REWARD)) {
      int champqty = Rnd.get(Config.L2JMOD_CHAMPION_REWARD_QTY);
      champqty++;

      RewardItem item = new RewardItem(Config.L2JMOD_CHAMPION_REWARD_ID, champqty);
      if (Config.AUTO_LOOT)
        player.addItem("ChampionLoot", item.getItemId(), item.getCount(), this, true);
      else
        dropItem(player, item);
    }
  }

  private boolean isEpicJewerly(int itemId)
  {
    switch (itemId) {
    case 6656:
    case 6657:
    case 6658:
    case 6659:
    case 6660:
    case 6661:
    case 6662:
    case 8191:
      return true;
    }
    return false;
  }

  private boolean isProtectedPremium(int itemId)
  {
    return Config.PREMIUM_PROTECTED_ITEMS.contains(Integer.valueOf(itemId));
  }

  public void doEventDrop(L2Character lastAttacker)
  {
    L2PcInstance player = lastAttacker.getPlayer();
    if (player == null) {
      return;
    }
    if (player.getLevel() - getLevel() > 9) {
      return;
    }

    if (Config.ALLOW_XM_SPAWN)
    {
      for (Config.EventReward reward : Config.XM_DROP) {
        if ((reward != null) && (Rnd.get(100) < reward.chance)) {
          player.addItem("XM.drop", reward.id, Rnd.get(1, reward.count), player, true);
        }
      }
    }

    if (Config.ALLOW_MEDAL_EVENT)
    {
      for (Config.EventReward reward : Config.MEDAL_EVENT_DROP) {
        if ((reward != null) && (Rnd.get(100) < reward.chance)) {
          player.addItem("Medal.drop", reward.id, Rnd.get(1, reward.count), player, true);
        }
      }
    }

    if (Config.EVENT_SPECIAL_DROP)
      CustomServerData.getInstance().manageSpecialDrop(player, this);
  }

  public L2ItemInstance dropItem(L2PcInstance lastAttacker, RewardItem item)
  {
    int randDropLim = 70;
    L2ItemInstance ditem = null;
    for (int i = 0; i < item.getCount(); i++)
    {
      int newX = getX() + Rnd.get(randDropLim * 2 + 1) - randDropLim;
      int newY = getY() + Rnd.get(randDropLim * 2 + 1) - randDropLim;
      int newZ = GeoData.getInstance().getSpawnHeight(newX, newY, getZ(), getZ());

      ditem = ItemTable.getInstance().createItem("Loot", item.getItemId(), item.getCount(), lastAttacker, this);
      ditem.dropMe(this, newX, newY, newZ);

      if ((!Config.LIST_PROTECTED_ITEMS.contains(Integer.valueOf(item.getItemId()))) && (
        ((Config.AUTODESTROY_ITEM_AFTER > 0) && (ditem.getItemType() != L2EtcItemType.HERB)) || ((Config.HERB_AUTO_DESTROY_TIME > 0) && (ditem.getItemType() == L2EtcItemType.HERB))))
      {
        ItemsAutoDestroy.getInstance().addItem(ditem);
      }

      ditem.setProtected(false);
      ditem.setPickuper(lastAttacker);

      if ((ditem.isStackable()) || (!Config.MULTIPLE_ITEM_DROP)) {
        break;
      }
    }
    return ditem;
  }

  public L2ItemInstance dropItem(L2PcInstance lastAttacker, int itemId, int itemCount)
  {
    return dropItem(lastAttacker, new RewardItem(itemId, itemCount));
  }

  public L2ItemInstance getActiveWeapon()
  {
    return null;
  }

  public boolean noTarget()
  {
    return _aggroList.isEmpty();
  }

  public boolean containsTarget(L2Character player)
  {
    return _aggroList.containsKey(player);
  }

  public void clearAggroList()
  {
    _aggroList.clear();
  }

  public boolean isSweepActive()
  {
    return _sweepItems != null;
  }

  public synchronized RewardItem[] takeSweep()
  {
    RewardItem[] sweep = _sweepItems;

    _sweepItems = null;

    return sweep;
  }

  public RewardItem[] takeHarvest()
  {
    RewardItem[] harvest = _harvestItems;
    _harvestItems = null;
    return harvest;
  }

  public void overhitEnabled(boolean status)
  {
    _overhit = status;
  }

  public void setOverhitValues(L2Character attacker, double damage)
  {
    double overhitDmg = (getCurrentHp() - damage) * -1.0D;
    if (overhitDmg < 0.0D)
    {
      overhitEnabled(false);
      _overhitDamage = 0.0D;
      _overhitAttacker = null;
      return;
    }
    overhitEnabled(true);
    _overhitDamage = overhitDmg;
    _overhitAttacker = attacker;
  }

  public L2Character getOverhitAttacker()
  {
    return _overhitAttacker;
  }

  public double getOverhitDamage()
  {
    return _overhitDamage;
  }

  public boolean isOverhit()
  {
    return _overhit;
  }

  public void absorbSoul()
  {
    _absorbed = true;
  }

  public boolean isAbsorbed()
  {
    return _absorbed;
  }

  public void addAbsorber(L2PcInstance attacker, int crystalId)
  {
    if (!isL2Monster()) {
      return;
    }

    if (attacker == null) {
      return;
    }

    if (getAbsorbLevel() == 0) {
      return;
    }

    AbsorberInfo ai = (AbsorberInfo)_absorbersList.get(attacker);

    if (ai == null) {
      ai = new AbsorberInfo(attacker, crystalId, getCurrentHp());
      _absorbersList.put(attacker, ai);
    } else {
      ai._absorber = attacker;
      ai._crystalId = crystalId;
      ai._absorbedHP = getCurrentHp();
    }

    absorbSoul();
  }

  private void levelSoulCrystals(L2Character attacker)
  {
    if ((!attacker.isPlayer()) && (!attacker.isL2Summon())) {
      resetAbsorbList();
      return;
    }

    int maxAbsorbLevel = getAbsorbLevel();
    int minAbsorbLevel = 0;

    if (maxAbsorbLevel == 0) {
      resetAbsorbList();
      return;
    }

    if (maxAbsorbLevel > 10) {
      minAbsorbLevel = maxAbsorbLevel > 12 ? 12 : 10;
    }

    boolean isSuccess = true;
    boolean doLevelup = true;
    boolean isBossMob = maxAbsorbLevel > 10;

    L2NpcTemplate.AbsorbCrystalType absorbType = getTemplate().absorbType;

    L2PcInstance killer = attacker.getPlayer();

    if (!isBossMob)
    {
      if (!isAbsorbed()) {
        resetAbsorbList();
        return;
      }

      AbsorberInfo ai = (AbsorberInfo)_absorbersList.get(killer);
      if ((ai == null) || (ai._absorber.getObjectId() != killer.getObjectId())) {
        isSuccess = false;
      }

      if ((ai != null) && (ai._absorbedHP > getMaxHp() / 2.0D)) {
        isSuccess = false;
      }

      if (!isSuccess) {
        resetAbsorbList();
        return;
      }

    }

    String[] crystalNFO = null;
    String crystalNME = "";

    int dice = Rnd.get(100);
    int crystalQTY = 0;
    int crystalLVL = 0;
    int crystalOLD = 0;
    int crystalNEW = 0;

    List players = new FastList();

    if ((absorbType == L2NpcTemplate.AbsorbCrystalType.FULL_PARTY) && (killer.isInParty()))
      players = killer.getParty().getPartyMembers();
    else if ((absorbType == L2NpcTemplate.AbsorbCrystalType.PARTY_ONE_RANDOM) && (killer.isInParty()))
    {
      players.add(killer.getParty().getPartyMembers().get(Rnd.get(killer.getParty().getMemberCount())));
    }
    else players.add(killer);

    for (L2PcInstance player : players) {
      if (player == null) {
        continue;
      }
      crystalQTY = 0;

      L2ItemInstance[] inv = player.getInventory().getItems();
      for (L2ItemInstance item : inv) {
        int itemId = item.getItemId();
        for (int id : SoulCrystal.SoulCrystalTable)
        {
          if (id == itemId) {
            crystalQTY++;

            if (crystalQTY > 1) {
              isSuccess = false;
              break;
            }

            if ((id != 4629) && (id != 4640) && (id != 4651))
            {
              try
              {
                if (item.getItem().getName().contains("Grade"))
                {
                  crystalNFO = item.getItem().getName().trim().replace(" Grade ", "-").split("-");

                  crystalLVL = 13;

                  crystalNME = crystalNFO[0].toLowerCase();
                }
                else {
                  crystalNFO = item.getItem().getName().trim().replace(" Stage ", "").split("-");

                  crystalLVL = Integer.parseInt(crystalNFO[1].trim());

                  crystalNME = crystalNFO[0].toLowerCase();
                }

                if (crystalLVL > 9)
                  for (int i = 0; i < SoulCrystal.HighSoulConvert.length; i++)
                  {
                    if (id == SoulCrystal.HighSoulConvert[i][0]) {
                      crystalNEW = SoulCrystal.HighSoulConvert[i][1];
                      break;
                    }
                  }
                else
                  crystalNEW = id + 1;
              }
              catch (NumberFormatException nfe) {
                _log.log(Level.WARNING, "An attempt to identify a soul crystal failed, verify the names have not changed in etcitem table.", nfe);

                player.sendMessage("There has been an error handling your soul crystal. Please notify your server admin.");

                isSuccess = false;
                break;
              } catch (Exception e) {
                e.printStackTrace();
                isSuccess = false;
                break;
              }
            } else {
              crystalNME = item.getItem().getName().toLowerCase().trim();
              crystalNEW = id + 1;
            }

            crystalOLD = id;
            break;
          }
        }
        if (!isSuccess)
        {
          break;
        }
      }

      if ((crystalLVL < minAbsorbLevel) || (crystalLVL >= maxAbsorbLevel)) {
        doLevelup = false;
      }

      if ((crystalQTY < 1) || (crystalQTY > 1) || (!isSuccess) || (!doLevelup))
      {
        if (crystalQTY > 1) {
          player.sendPacket(Static.SOUL_CRYSTAL_ABSORBING_FAILED_RESONATION);
        }
        else if (!doLevelup) {
          player.sendPacket(Static.SOUL_CRYSTAL_ABSORBING_REFUSED);
        }

        crystalQTY = 0;
        continue;
      }

      int chanceLevelUp = isBossMob ? 70 : 32;

      if (((absorbType == L2NpcTemplate.AbsorbCrystalType.FULL_PARTY) && (doLevelup)) || (dice <= chanceLevelUp))
      {
        exchangeCrystal(player, crystalOLD, crystalNEW, false);
      }
      else if ((!isBossMob) && (dice >= 90.0D))
      {
        if (crystalNME.startsWith("red"))
          exchangeCrystal(player, crystalOLD, 4662, true);
        else if (crystalNME.startsWith("gre"))
          exchangeCrystal(player, crystalOLD, 4663, true);
        else if (crystalNME.startsWith("blu")) {
          exchangeCrystal(player, crystalOLD, 4664, true);
        }
        resetAbsorbList();
      } else {
        player.sendPacket(Static.SOUL_CRYSTAL_ABSORBING_FAILED);
      }
    }
  }

  private void exchangeCrystal(L2PcInstance player, int takeid, int giveid, boolean broke) {
    L2ItemInstance Item = player.getInventory().destroyItemByItemId("SoulCrystal", takeid, 1, player, this);
    if (Item != null)
    {
      InventoryUpdate playerIU = new InventoryUpdate();
      playerIU.addRemovedItem(Item);

      Item = player.getInventory().addItem("SoulCrystal", giveid, 1, player, this);
      playerIU.addItem(Item);

      if (broke)
        player.sendPacket(Static.SOUL_CRYSTAL_BROKE);
      else {
        player.sendPacket(Static.SOUL_CRYSTAL_ABSORBING_SUCCEEDED);
      }

      player.sendPacket(SystemMessage.id(SystemMessageId.EARNED_ITEM).addItemName(giveid));

      player.sendPacket(playerIU);
    }
  }

  private void resetAbsorbList() {
    _absorbed = false;
    _absorbersList.clear();
  }

  private int[] calculateExpAndSp(int diff, int damage)
  {
    if (diff < -5) {
      diff = -5;
    }
    double xp = getExpReward() * damage / getMaxHp();
    if (Config.ALT_GAME_EXPONENT_XP != 0.0F) {
      xp *= Math.pow(2.0D, -diff / Config.ALT_GAME_EXPONENT_XP);
    }

    double sp = getSpReward() * damage / getMaxHp();
    if (Config.ALT_GAME_EXPONENT_SP != 0.0F) {
      sp *= Math.pow(2.0D, -diff / Config.ALT_GAME_EXPONENT_SP);
    }

    if ((Config.ALT_GAME_EXPONENT_XP == 0.0F) && (Config.ALT_GAME_EXPONENT_SP == 0.0F)) {
      if (diff > 5)
      {
        double pow = Math.pow(0.8333333333333334D, diff - 5);
        xp *= pow;
        sp *= pow;
      }

      if (xp <= 0.0D) {
        xp = 0.0D;
        sp = 0.0D;
      } else if (sp <= 0.0D) {
        sp = 0.0D;
      }
    }

    int[] tmp = { (int)xp, (int)sp };

    return tmp;
  }

  public long calculateOverhitExp(long normalExp)
  {
    double overhitPercentage = getOverhitDamage() * 100.0D / getMaxHp();

    if (overhitPercentage > 25.0D) {
      overhitPercentage = 25.0D;
    }

    double overhitExp = overhitPercentage / 100.0D * normalExp;

    long bonusOverhit = Math.round(overhitExp);
    return bonusOverhit;
  }

  public boolean isAttackable()
  {
    return true;
  }

  private boolean canBeChamp() {
    if (isRaid()) {
      return false;
    }

    if (!isL2Monster()) {
      return false;
    }

    if (isL2Chest()) {
      return false;
    }

    if ((Config.L2JMOD_CHAMPION_FREQUENCY == 0) || (getLevel() < Config.L2JMOD_CHAMP_MIN_LVL) || (getLevel() > Config.L2JMOD_CHAMP_MAX_LVL)) {
      return false;
    }

    switch (getNpcId()) {
    case 29002:
    case 29003:
    case 29004:
    case 29069:
    case 29070:
      return false;
    }

    return Rnd.get(101) <= Config.L2JMOD_CHAMPION_FREQUENCY;
  }

  public void onSpawn()
  {
    if ((Config.L2JMOD_CHAMPION_ENABLE) && (canBeChamp())) {
      setChampion(true);
    }

    super.onSpawn();

    setSpoil(false);

    clearAggroList();

    _harvestItems = null;

    setSeeded(false);

    _sweepItems = null;
    resetAbsorbList();

    setWalking();

    if (!isInActiveRegion().booleanValue()) {
      if (isL2SiegeGuard())
        ((L2SiegeGuardAI)getAI()).stopAITask();
      else {
        ((L2AttackableAI)getAI()).stopAITask();
      }
    }

    try
    {
      if (getTemplate().getEventQuests(Quest.QuestEventType.ONSPAWN) != null)
        for (Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.ONSPAWN))
          quest.notifySpawn(this);
    }
    catch (Exception e)
    {
      _log.log(Level.SEVERE, "", e);
    }

    doNpcChat(0, "");
  }

  public void setSeeded()
  {
    if ((_seedType != 0) && (_seeder != null))
      setSeeded(_seedType, _seeder.getLevel());
  }

  public void setSeeded(int id, L2PcInstance seeder)
  {
    if (!_seeded) {
      _seedType = id;
      _seeder = seeder;
    }
  }

  public void setSeeded(int id, int seederLvl) {
    _seeded = true;
    _seedType = id;
    int count = 1;

    Map skills = getTemplate().getSkills();
    Iterator i$;
    if (skills != null) {
      for (i$ = skills.keySet().iterator(); i$.hasNext(); ) { int skillId = ((Integer)i$.next()).intValue();
        switch (skillId) {
        case 4303:
          count *= 2;
          break;
        case 4304:
          count *= 3;
          break;
        case 4305:
          count *= 4;
          break;
        case 4306:
          count *= 5;
          break;
        case 4307:
          count *= 6;
          break;
        case 4308:
          count *= 7;
          break;
        case 4309:
          count *= 8;
          break;
        case 4310:
          count *= 9;
        }
      }

    }

    int diff = getLevel() - (L2Manor.getInstance().getSeedLevel(_seedType) - 5);

    if (diff > 0) {
      count += diff;
    }

    FastList harvested = new FastList();

    harvested.add(new RewardItem(L2Manor.getInstance().getCropType(_seedType), count * Config.RATE_DROP_MANOR));

    _harvestItems = ((RewardItem[])harvested.toArray(new RewardItem[harvested.size()]));
  }

  public void setSeeded(boolean seeded) {
    _seeded = seeded;
  }

  public L2PcInstance getSeeder() {
    return _seeder;
  }

  public int getSeedType() {
    return _seedType;
  }

  public boolean isSeeded() {
    return _seeded;
  }

  private int getAbsorbLevel() {
    return getTemplate().absorbLevel;
  }

  public boolean hasRandomAnimation()
  {
    return (Config.MAX_MONSTER_ANIMATION > 0) && (!(this instanceof L2BossInstance));
  }

  public boolean isMob()
  {
    return true;
  }

  public boolean isL2Attackable()
  {
    return true;
  }

  public static final class RewardItem
  {
    protected int _itemId;
    protected int _count;

    public RewardItem(int itemId, int count)
    {
      _itemId = itemId;
      _count = count;
    }

    public int getItemId() {
      return _itemId;
    }

    public int getCount() {
      return _count;
    }
  }

  public static final class AbsorberInfo
  {
    protected L2PcInstance _absorber;
    protected int _crystalId;
    protected double _absorbedHP;

    AbsorberInfo(L2PcInstance attacker, int pCrystalId, double pAbsorbedHP)
    {
      _absorber = attacker;
      _crystalId = pCrystalId;
      _absorbedHP = pAbsorbedHP;
    }

    public boolean equals(Object obj)
    {
      if (this == obj) {
        return true;
      }
      if ((obj instanceof AbsorberInfo)) {
        return ((AbsorberInfo)obj)._absorber == _absorber;
      }
      return false;
    }

    public int hashCode()
    {
      return _absorber.getObjectId();
    }
  }

  protected static final class RewardInfo
  {
    protected L2Character _attacker;
    protected int _dmg = 0;

    public RewardInfo(L2Character pAttacker, int pDmg) {
      _attacker = pAttacker;
      _dmg = pDmg;
    }

    public void addDamage(int pDmg) {
      _dmg += pDmg;
    }

    public boolean equals(Object obj)
    {
      if (this == obj) {
        return true;
      }
      if ((obj instanceof RewardInfo)) {
        return ((RewardInfo)obj)._attacker == _attacker;
      }
      return false;
    }

    public int hashCode()
    {
      return _attacker.getObjectId();
    }
  }

  public static final class AggroInfo
  {
    protected L2Character _attacker;
    protected int _hate;
    protected int _damage;

    AggroInfo(L2Character pAttacker)
    {
      _attacker = pAttacker;
    }

    public boolean equals(Object obj)
    {
      if (this == obj) {
        return true;
      }
      if ((obj instanceof AggroInfo)) {
        return ((AggroInfo)obj)._attacker == _attacker;
      }
      return false;
    }

    public int hashCode()
    {
      return _attacker.getObjectId();
    }
  }
}
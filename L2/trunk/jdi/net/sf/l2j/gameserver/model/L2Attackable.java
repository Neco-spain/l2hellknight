package net.sf.l2j.gameserver.model;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastMap.Entry;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ItemsAutoDestroy;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2AttackableAI;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.ai.L2SiegeGuardAI;
import net.sf.l2j.gameserver.datatables.EventDroplist;
import net.sf.l2j.gameserver.datatables.EventDroplist.DateDrop;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.instancemanager.CursedWeaponsManager;
import net.sf.l2j.gameserver.model.actor.appearance.PcAppearance;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2FolkInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2GrandBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MinionInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2RaidBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SiegeGuardInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SummonInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.AttackableKnownList;
import net.sf.l2j.gameserver.model.actor.knownlist.CharKnownList;
import net.sf.l2j.gameserver.model.actor.stat.NpcStat;
import net.sf.l2j.gameserver.model.base.SoulCrystal;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.Quest.QuestEventType;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
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
  private FastMap<L2Character, AggroInfo> _aggroList = new FastMap().setShared(true);

  private boolean _isReturningToSpawnPoint = false;

  private boolean _canReturnToSpawnPoint = true;
  private RewardItem[] _sweepItems;
  private RewardItem[] _harvestItems;
  private boolean _seeded;
  private int _seedType = 0;
  private L2PcInstance _seeder = null;
  private boolean _overhit;
  private double _overhitDamage;
  private L2Character _overhitAttacker;
  private L2CommandChannel _firstCommandChannelAttacked = null;
  private CommandChannelTimer _commandChannelTimer = null;
  private boolean _absorbed;
  private FastMap<L2PcInstance, AbsorberInfo> _absorbersList = new FastMap().setShared(true);
  private boolean _mustGiveExpSp;

  public final FastMap<L2Character, AggroInfo> getAggroListRP()
  {
    return _aggroList;
  }

  public final FastMap<L2Character, AggroInfo> getAggroList()
  {
    return _aggroList;
  }

  public final boolean isReturningToSpawnPoint() {
    return _isReturningToSpawnPoint; } 
  public final void setisReturningToSpawnPoint(boolean value) { _isReturningToSpawnPoint = value; }

  public final boolean canReturnToSpawnPoint() {
    return _canReturnToSpawnPoint; } 
  public final void setCanReturnToSpawnPoint(boolean value) { _canReturnToSpawnPoint = value;
  }

  public L2Attackable(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
    getKnownList();
    _mustGiveExpSp = true;
  }

  public AttackableKnownList getKnownList()
  {
    if ((super.getKnownList() == null) || (!(super.getKnownList() instanceof AttackableKnownList)))
      setKnownList(new AttackableKnownList(this));
    return (AttackableKnownList)super.getKnownList();
  }

  public L2CharacterAI getAI()
  {
    if (_ai == null)
    {
      synchronized (this)
      {
        if (_ai == null)
          _ai = new L2AttackableAI(new L2Character.AIAccessor(this));
      }
    }
    return _ai;
  }

  @Deprecated
  public boolean getCondition2(L2Character target)
  {
    if (((target instanceof L2FolkInstance)) || ((target instanceof L2DoorInstance))) {
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
    if ((_commandChannelTimer == null) && (isRaid()))
    {
      if ((attacker != null) && (attacker.getParty() != null) && (attacker.getParty().isInCommandChannel()) && (attacker.getParty().getCommandChannel().meetRaidWarCondition(this)))
      {
        _firstCommandChannelAttacked = attacker.getParty().getCommandChannel();
        _commandChannelTimer = new CommandChannelTimer(this, attacker.getParty().getCommandChannel());
        ThreadPoolManager.getInstance().scheduleGeneral(_commandChannelTimer, 300000L);
        _firstCommandChannelAttacked.broadcastToChannelMembers(new CreatureSay(0, 16, "", "You have looting rights!"));
      }
    }

    if (isEventMob) return;

    if (attacker != null) addDamage(attacker, (int)damage);

    if ((this instanceof L2MonsterInstance))
    {
      L2MonsterInstance master = (L2MonsterInstance)this;
      if ((this instanceof L2MinionInstance))
      {
        master = ((L2MinionInstance)this).getLeader();
        if ((!master.isInCombat()) && (!master.isDead())) master.addDamage(attacker, 1);
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
    if (!super.doDie(killer))
      return false;
    try
    {
      if ((killer instanceof L2PcInstance))
      {
        levelSoulCrystals(killer);
      }
    } catch (Exception e) {
      _log.log(Level.SEVERE, "", e);
    }
    try {
      if (((killer instanceof L2PcInstance)) || ((killer instanceof L2Summon)))
      {
        L2PcInstance player = (killer instanceof L2PcInstance) ? (L2PcInstance)killer : ((L2Summon)killer).getOwner();

        if (getTemplate().getEventQuests(Quest.QuestEventType.ON_KILL) != null)
          for (Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.ON_KILL))
            quest.notifyKill(this, player, killer instanceof L2Summon);
      }
    } catch (Exception e) {
      _log.log(Level.SEVERE, "", e);
    }return true;
  }

  protected void calculateRewards(L2Character lastAttacker)
  {
    FastMap rewards = new FastMap().setShared(true);
    try
    {
      if (getAggroListRP().isEmpty()) return;
      doItemDrop(lastAttacker);
      doEventDrop(lastAttacker);
      if (!getMustRewardExpSP()) return;
      int rewardCount = 0;

      synchronized (getAggroList())
      {
        for (AggroInfo info : getAggroListRP().values())
        {
          if (info == null) {
            continue;
          }
          L2Character attacker = info._attacker;

          int damage = info._damage;

          if (damage > 1)
          {
            L2Character ddealer;
            L2Character ddealer;
            if (((attacker instanceof L2SummonInstance)) || (((attacker instanceof L2PetInstance)) && (((L2PetInstance)attacker).getPetData().getOwnerExpTaken() > 0.0F)))
            {
              ddealer = ((L2Summon)attacker).getOwner();
            }
            else ddealer = info._attacker;

            if (!Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, ddealer, true)) {
              continue;
            }
            RewardInfo reward = (RewardInfo)rewards.get(ddealer);

            if (reward == null)
            {
              reward = new RewardInfo(ddealer, damage);
              rewardCount++;
            }
            else
            {
              reward.addDamage(damage);
            }
            rewards.put(ddealer, reward);
          }
        }
      }
      FastMap.Entry entry;
      if (!rewards.isEmpty())
      {
        entry = rewards.head(); for (FastMap.Entry end = rewards.tail(); (entry = entry.getNext()) != end; )
        {
          if (entry == null)
            continue;
          RewardInfo reward = (RewardInfo)entry.getValue();
          if (reward == null) {
            continue;
          }
          float penalty = 0.0F;

          L2Character attacker = reward._attacker;

          int damage = reward._dmg;
          L2Party attackerParty;
          if ((attacker instanceof L2PetInstance)) {
            attackerParty = ((L2PetInstance)attacker).getParty();
          }
          else
          {
            L2Party attackerParty;
            if ((attacker instanceof L2PcInstance))
              attackerParty = ((L2PcInstance)attacker).getParty();
            else
              return;
          }
          L2Party attackerParty;
          if (((attacker instanceof L2PcInstance)) && ((((L2PcInstance)attacker).getPet() instanceof L2SummonInstance)))
          {
            penalty = ((L2SummonInstance)((L2PcInstance)attacker).getPet()).getExpPenalty();
          }

          if (damage > getMaxHp()) damage = getMaxHp();

          if (attackerParty == null)
          {
            if (!attacker.getKnownList().knowsObject(this))
            {
              continue;
            }

            int levelDiff = attacker.getLevel() - getLevel();

            int[] tmp = calculateExpAndSp(levelDiff, damage, attacker.getPremiumService());
            long exp = tmp[0];
            L2PcInstance player2;
            if ((attacker instanceof L2PcInstance))
            {
              player2 = (L2PcInstance)attacker;
            }

            exp = ()((float)exp * (1.0F - penalty));
            int sp = tmp[1];
            L2PcInstance player2;
            if ((attacker instanceof L2PcInstance))
            {
              player2 = (L2PcInstance)attacker;
            }

            if ((Config.CHAMPION_ENABLE) && (isChampion()))
            {
              exp *= Config.CHAMPION_REWARDS;
              sp *= Config.CHAMPION_REWARDS;
            }

            if ((attacker instanceof L2PcInstance))
            {
              L2PcInstance player = (L2PcInstance)attacker;
              if ((isOverhit()) && (attacker == getOverhitAttacker()))
              {
                player.sendPacket(new SystemMessage(SystemMessageId.OVER_HIT));
                exp += calculateOverhitExp(exp);
              }
            }

            if (attacker.isDead())
              continue;
            long addexp = Math.round(attacker.calcStat(Stats.EXPSP_RATE, exp, null, null));
            int addsp = (int)attacker.calcStat(Stats.EXPSP_RATE, sp, null, null);
            attacker.addExpAndSp(addexp, addsp);
            continue;
          }

          int partyDmg = 0;
          float partyMul = 1.0F;
          int partyLvl = 0;

          List rewardedMembers = new FastList();
          List groupMembers;
          List groupMembers;
          if (attackerParty.isInCommandChannel())
            groupMembers = attackerParty.getCommandChannel().getMembers();
          else {
            groupMembers = attackerParty.getPartyMembers();
          }
          for (L2PcInstance pl : groupMembers)
          {
            if ((pl == null) || (pl.isDead()))
              continue;
            RewardInfo reward2 = (RewardInfo)rewards.get(pl);

            if (reward2 != null)
            {
              if (Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, pl, true))
              {
                partyDmg += reward2._dmg;
                rewardedMembers.add(pl);
                if (pl.getLevel() > partyLvl)
                {
                  if (attackerParty.isInCommandChannel())
                    partyLvl = attackerParty.getCommandChannel().getLevel();
                  else
                    partyLvl = pl.getLevel();
                }
              }
              rewards.remove(pl);
            }
            else if (Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, pl, true))
            {
              rewardedMembers.add(pl);
              if (pl.getLevel() > partyLvl)
              {
                if (attackerParty.isInCommandChannel())
                  partyLvl = attackerParty.getCommandChannel().getLevel();
                else {
                  partyLvl = pl.getLevel();
                }
              }
            }
            L2PlayableInstance summon = pl.getPet();
            if ((summon != null) && ((summon instanceof L2PetInstance)))
            {
              reward2 = (RewardInfo)rewards.get(summon);
              if (reward2 != null)
              {
                if (Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, summon, true))
                {
                  partyDmg += reward2._dmg;
                  rewardedMembers.add(summon);
                  if (summon.getLevel() > partyLvl)
                    partyLvl = summon.getLevel();
                }
                rewards.remove(summon);
              }
            }

          }

          if (partyDmg < getMaxHp()) partyMul = partyDmg / getMaxHp();

          if (partyDmg > getMaxHp()) partyDmg = getMaxHp();

          int levelDiff = partyLvl - getLevel();

          int[] tmp = calculateExpAndSp(levelDiff, partyDmg, 1);
          long exp_premium = tmp[0];
          int sp_premium = tmp[1];
          tmp = calculateExpAndSp(levelDiff, partyDmg, 0);
          long exp = tmp[0];
          int sp = tmp[1];
          L2PcInstance player2 = (L2PcInstance)attacker;

          if ((Config.CHAMPION_ENABLE) && (isChampion()))
          {
            exp *= Config.CHAMPION_REWARDS;
            sp *= Config.CHAMPION_REWARDS;
          }

          exp = ()((float)exp * partyMul);
          sp = (int)(sp * partyMul);

          if ((attacker instanceof L2PcInstance))
          {
            L2PcInstance player = (L2PcInstance)attacker;
            if ((isOverhit()) && (attacker == getOverhitAttacker()))
            {
              player.sendPacket(new SystemMessage(SystemMessageId.OVER_HIT));
              exp += calculateOverhitExp(exp);
            }

          }

          if (partyDmg > 0) attackerParty.distributeXpAndSp(exp, sp, rewardedMembers, partyLvl);
        }

      }

      rewards = null;
    }
    catch (Exception e)
    {
      _log.log(Level.SEVERE, "", e);
    }
  }

  public void addDamage(L2Character attacker, int damage)
  {
    if (damage > 0)
      try
      {
        if (((attacker instanceof L2PcInstance)) || ((attacker instanceof L2Summon)))
        {
          L2PcInstance player = (attacker instanceof L2PcInstance) ? (L2PcInstance)attacker : ((L2Summon)attacker).getOwner();

          if (getTemplate().getEventQuests(Quest.QuestEventType.ON_ATTACK) != null)
            for (Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.ON_ATTACK))
              quest.notifyAttack(this, player, damage, attacker instanceof L2Summon);
        }
      } catch (Exception e) {
        _log.log(Level.SEVERE, "", e);
      }
  }

  public void addDamageHate(L2Character attacker, int damage, int aggro)
  {
    if (attacker == null) return;

    AggroInfo ai = (AggroInfo)getAggroListRP().get(attacker);
    if (ai == null)
    {
      ai = new AggroInfo(attacker);
      ai._damage = 0;
      ai._hate = 0;
      getAggroListRP().put(attacker, ai);
      if ((((attacker instanceof L2PcInstance)) || ((attacker instanceof L2Summon))) && (!attacker.isAlikeDead())) {
        L2PcInstance targetPlayer = (attacker instanceof L2PcInstance) ? (L2PcInstance)attacker : ((L2Summon)attacker).getOwner();
        if (getTemplate().getEventQuests(Quest.QuestEventType.ON_AGGRO_RANGE_ENTER) != null) {
          for (Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.ON_AGGRO_RANGE_ENTER)) {
            quest.notifyAggroRangeEnter(this, targetPlayer, attacker instanceof L2Summon);
          }
        }
      }
    }
    ai._hate += aggro;
    ai._damage += damage;

    if ((aggro > 0) && (getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE))
      getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
  }

  public void reduceHate(L2Character target, int amount)
  {
    if ((getAI() instanceof L2SiegeGuardAI))
    {
      stopHating(target);
      setTarget(null);
      getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
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

      for (L2Character aggroed : getAggroListRP().keySet())
      {
        AggroInfo ai = (AggroInfo)getAggroListRP().get(aggroed);
        if (ai == null) return;
        ai._hate -= amount;
      }

      amount = getHating(mostHated);
      if (amount <= 0)
      {
        ((L2AttackableAI)getAI()).setGlobalAggro(-25);
        clearAggroList();
        getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
        setWalking();
      }
      return;
    }
    AggroInfo ai = (AggroInfo)getAggroListRP().get(target);
    if (ai == null) return;
    ai._hate -= amount;

    if (ai._hate <= 0)
    {
      if (getMostHated() == null)
      {
        ((L2AttackableAI)getAI()).setGlobalAggro(-25);
        clearAggroList();
        getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
        setWalking();
      }
    }
  }

  public void stopHating(L2Character target)
  {
    if (target == null) return;
    AggroInfo ai = (AggroInfo)getAggroListRP().get(target);
    if (ai == null) return;
    ai._hate = 0;
  }

  public L2Character getMostHated()
  {
    if ((getAggroListRP().isEmpty()) || (isAlikeDead())) return null;

    L2Character mostHated = null;
    int maxHate = 0;

    synchronized (getAggroList())
    {
      for (AggroInfo ai : getAggroListRP().values())
      {
        if (ai != null) {
          if ((ai._attacker.isAlikeDead()) || (!getKnownList().knowsObject(ai._attacker)) || (!ai._attacker.isVisible()))
          {
            ai._hate = 0;
          }if (ai._hate > maxHate)
          {
            mostHated = ai._attacker;
            maxHate = ai._hate;
          }
        }
      }
    }
    return mostHated;
  }

  public List<L2Character> get2MostHated()
  {
    if ((getAggroListRP().isEmpty()) || (isAlikeDead())) return null;

    L2Character mostHated = null;
    L2Character secondMostHated = null;
    int maxHate = 0;
    List result = new FastList();

    synchronized (getAggroList())
    {
      for (AggroInfo ai : getAggroListRP().values())
      {
        if (ai != null) {
          if ((ai._attacker.isAlikeDead()) || (!getKnownList().knowsObject(ai._attacker)) || (!ai._attacker.isVisible()))
          {
            ai._hate = 0;
          }if (ai._hate > maxHate)
          {
            secondMostHated = mostHated;
            mostHated = ai._attacker;
            maxHate = ai._hate;
          }
        }
      }
    }
    result.add(mostHated);
    if (getAttackByList().contains(secondMostHated))
      result.add(secondMostHated);
    else result.add(null);
    return result;
  }

  public int getHating(L2Character target)
  {
    if (getAggroListRP().isEmpty()) return 0;

    AggroInfo ai = (AggroInfo)getAggroListRP().get(target);
    if (ai == null) return 0;
    if (((ai._attacker instanceof L2PcInstance)) && ((((L2PcInstance)ai._attacker).getAppearance().getInvisible()) || (ai._attacker.isInvul())))
    {
      getAggroList().remove(target);
      return 0;
    }
    if (!ai._attacker.isVisible())
    {
      getAggroList().remove(target);
      return 0;
    }
    if (ai._attacker.isAlikeDead())
    {
      ai._hate = 0;
      return 0;
    }
    return ai._hate;
  }

  private RewardItem calculateRewardItem(L2PcInstance lastAttacker, L2DropData drop, int levelModifier, boolean isSweep)
  {
    float dropChance = drop.getChance();

    int deepBlueDrop = 1;
    if (Config.DEEPBLUE_DROP_RULES)
    {
      if (levelModifier > 0)
      {
        deepBlueDrop = 3;
        if (drop.getItemId() == 57)
        {
          if (((this instanceof L2GrandBossInstance)) && (!isRaidMinion()))
          {
            deepBlueDrop *= (int)Config.RATE_DROP_ITEMS_BY_GRAND;
          }
          else if (((this instanceof L2RaidBossInstance)) && (!isRaidMinion()))
          {
            deepBlueDrop *= (int)Config.RATE_DROP_ITEMS_BY_RAID;
          }
          else if ((Config.CHAMPION_ENABLE) && (isChampion()))
          {
            deepBlueDrop = (int)(deepBlueDrop * (Config.RATE_DROP_ITEMS * Config.CHAMPION_REWARD));
          }
          else if (lastAttacker.getPremiumService() == 1)
          {
            deepBlueDrop = (int)(deepBlueDrop * ((int)Config.RATE_DROP_ITEMS * Config.PREMIUM_RATE_DROP_ITEMS));
          }
          else
          {
            deepBlueDrop *= (int)Config.RATE_DROP_ITEMS;
          }
        }

      }

    }

    if (deepBlueDrop == 0)
      deepBlueDrop = 1;
    if (Config.DEEPBLUE_DROP_RULES) dropChance = (drop.getChance() - drop.getChance() * levelModifier / 100) / deepBlueDrop;

    if (drop.getItemId() == 57)
    {
      if ((Config.CHAMPION_ENABLE) && (isChampion()))
      {
        dropChance *= Config.CHAMPION_ADENAS_REWARDS * Config.RATE_DROP_ADENA;
      }
      else if (lastAttacker.getPremiumService() == 1)
      {
        dropChance *= Config.RATE_DROP_ADENA * Config.PREMIUM_RATE_DROP_ADENA;
      }
      else
      {
        dropChance *= Config.RATE_DROP_ADENA;
      }
    }

    if (isSweep)
    {
      if (lastAttacker.getPremiumService() == 1)
      {
        dropChance *= Config.RATE_DROP_SPOIL * Config.PREMIUM_RATE_DROP_SPOIL;
      }
      else
      {
        dropChance *= Config.RATE_DROP_SPOIL;
      }
    }
    else if ((drop.getItemId() == 6360) || (drop.getItemId() == 6361) || (drop.getItemId() == 6362))
    {
      if ((Config.CHAMPION_ENABLE) && (isChampion()))
      {
        dropChance *= Config.CHAMPION_REWARD * Config.RATE_DROP_STONE;
      }
      else
      {
        dropChance *= Config.RATE_DROP_STONE;
      }

    }
    else if (lastAttacker.getPremiumService() == 1)
      dropChance *= ((isRaid()) && (!isRaidMinion()) ? Config.PREMIUM_RATE_DROP_ITEMS_BY_RAID : Config.PREMIUM_RATE_DROP_ITEMS);
    else {
      dropChance *= ((isRaid()) && (!isRaidMinion()) ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS);
    }
    dropChance = Math.round(dropChance);

    if (dropChance < 1.0F) dropChance = 1.0F;
    int minCount = drop.getMinDrop();
    int maxCount = drop.getMaxDrop();
    int itemCount = 0;

    if ((dropChance > 1000000.0F) && (!Config.PRECISE_DROP_CALCULATION))
    {
      int multiplier = (int)dropChance / 1000000;
      if (minCount < maxCount) itemCount += Rnd.get(minCount * multiplier, maxCount * multiplier);
      else if (minCount == maxCount) itemCount += minCount * multiplier; else {
        itemCount += multiplier;
      }
      dropChance %= 1000000.0F;
    }

    int random = Rnd.get(1000000);
    while (random < dropChance)
    {
      if (minCount < maxCount) itemCount += Rnd.get(minCount, maxCount);
      else if (minCount == maxCount) itemCount += minCount; else
        itemCount++;
      dropChance -= 1000000.0F;
    }

    if (itemCount > 0) return new RewardItem(drop.getItemId(), itemCount);
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
    if (Config.DEEPBLUE_DROP_RULES)
    {
      if (levelModifier > 0)
      {
        deepBlueDrop = 3;
      }
    }

    if (deepBlueDrop == 0)
      deepBlueDrop = 1;
    if (Config.DEEPBLUE_DROP_RULES) categoryDropChance = (categoryDropChance - categoryDropChance * levelModifier / 100) / deepBlueDrop;
    L2DropData drop = categoryDrops.dropOne();
    if ((drop.getItemId() == 6360) || (drop.getItemId() == 6361) || (drop.getItemId() == 6362))
    {
      if ((Config.CHAMPION_ENABLE) && (isChampion()))
      {
        categoryDropChance = (int)(categoryDropChance * (Config.CHAMPION_REWARD * Config.RATE_DROP_STONE));
      }
      else
      {
        categoryDropChance = (int)(categoryDropChance * Config.RATE_DROP_STONE);
      }

    }
    else if (((this instanceof L2RaidBossInstance)) && (!isRaidMinion()))
    {
      categoryDropChance = (int)(categoryDropChance * Config.RATE_DROP_ITEMS_BY_RAID);
    }
    else if (((this instanceof L2GrandBossInstance)) && (!isRaidMinion()))
    {
      categoryDropChance = (int)(categoryDropChance * Config.RATE_DROP_ITEMS_BY_GRAND);
    }
    else if ((Config.CHAMPION_ENABLE) && (isChampion()))
    {
      categoryDropChance = (int)(categoryDropChance * (Config.CHAMPION_REWARDS * Config.RATE_DROP_ITEMS));
    }
    else if (lastAttacker.getPremiumService() == 1)
    {
      categoryDropChance = (int)(categoryDropChance * (Config.RATE_DROP_ITEMS * Config.PREMIUM_RATE_DROP_ITEMS));
    }
    else
    {
      categoryDropChance = (int)(categoryDropChance * Config.RATE_DROP_ITEMS);
    }

    categoryDropChance = Math.round(categoryDropChance);

    if (categoryDropChance < 1) categoryDropChance = 1;

    if (Rnd.get(1000000) < categoryDropChance)
    {
      if (drop == null) {
        return null;
      }
      int dropChance = drop.getChance();

      if (drop.getItemId() == 57)
      {
        if (isChampion())
        {
          dropChance = (int)(dropChance * (Config.CHAMPION_ADENAS_REWARDS * Config.RATE_DROP_ADENA));
        }
        if (lastAttacker.getPremiumService() == 1)
        {
          dropChance = (int)(dropChance * (Config.RATE_DROP_ADENA * Config.PREMIUM_RATE_DROP_ADENA));
        }
        else
        {
          dropChance = (int)(dropChance * Config.RATE_DROP_ADENA);
        }
      }

      if ((drop.getItemId() == 6360) || (drop.getItemId() == 6361) || (drop.getItemId() == 6362))
      {
        if ((Config.CHAMPION_ENABLE) && (isChampion()))
        {
          dropChance = (int)(dropChance * (Config.CHAMPION_REWARD * Config.RATE_DROP_STONE));
        }
        else
        {
          dropChance = (int)(dropChance * Config.RATE_DROP_STONE);
        }

      }
      else if (drop.getItemId() != 57)
      {
        if (((this instanceof L2RaidBossInstance)) && (!isRaidMinion()))
        {
          dropChance = (int)(dropChance * Config.RATE_DROP_ITEMS_BY_RAID);
        }
        else if (((this instanceof L2GrandBossInstance)) && (!isRaidMinion()))
        {
          dropChance = (int)(dropChance * Config.RATE_DROP_ITEMS_BY_GRAND);
        }
        else if (lastAttacker.getPremiumService() == 1)
        {
          dropChance = (int)(dropChance * (Config.RATE_DROP_ITEMS * Config.PREMIUM_RATE_DROP_ITEMS));
        }
        else if ((Config.CHAMPION_ENABLE) && (isChampion()))
        {
          dropChance = (int)(dropChance * (Config.RATE_DROP_ITEMS * Config.CHAMPION_REWARD));
        }
        else
        {
          dropChance = (int)(dropChance * Config.RATE_DROP_ITEMS);
        }

      }

      dropChance = Math.round(dropChance);

      if (dropChance < 1000000) {
        dropChance = 1000000;
      }
      int min = drop.getMinDrop();
      int max = drop.getMaxDrop();

      int itemCount = 0;

      if ((dropChance > 1000000) && (!Config.PRECISE_DROP_CALCULATION))
      {
        int multiplier = dropChance / 1000000;
        if (min < max) itemCount += Rnd.get(min * multiplier, max * multiplier);
        else if (min == max) itemCount += min * multiplier; else {
          itemCount += multiplier;
        }
        dropChance %= 1000000;
      }

      int random = Rnd.get(1000000);
      while (random < dropChance)
      {
        if (min < max) itemCount += Rnd.get(min, max);
        else if (min == max) itemCount += min; else
          itemCount++;
        dropChance -= 1000000;
      }

      if (itemCount > 0)
        return new RewardItem(drop.getItemId(), itemCount);
    }
    return null;
  }

  private int calculateLevelModifierForDrop(L2PcInstance lastAttacker)
  {
    if (Config.DEEPBLUE_DROP_RULES)
    {
      int highestLevel = lastAttacker.getLevel();

      if ((getAttackByList() != null) && (!getAttackByList().isEmpty()))
      {
        for (L2Character atkChar : getAttackByList()) {
          if ((atkChar != null) && (atkChar.getLevel() > highestLevel)) highestLevel = atkChar.getLevel();
        }
      }
      if (highestLevel - 9 >= getLevel()) return (highestLevel - (getLevel() + 8)) * 9;
    }

    return 0;
  }

  public void doItemDrop(L2Character lastAttacker) {
    doItemDrop(getTemplate(), lastAttacker);
  }

  public void doItemDrop(L2NpcTemplate npcTemplate, L2Character lastAttacker)
  {
    L2PcInstance player = null;
    if ((lastAttacker instanceof L2PcInstance)) player = (L2PcInstance)lastAttacker;
    else if ((lastAttacker instanceof L2Summon)) player = ((L2Summon)lastAttacker).getOwner();
    if (player == null) return;
    int levelModifier = calculateLevelModifierForDrop(player);

    CursedWeaponsManager.getInstance().checkDrop(this, player);

    if (npcTemplate.getDropData() != null)
      for (L2DropCategory cat : npcTemplate.getDropData())
      {
        RewardItem item = null;
        if (cat.isSweep())
        {
          if (isSpoil())
          {
            FastList sweepList = new FastList();

            for (L2DropData drop : cat.getAllDrops())
            {
              item = calculateRewardItem(player, drop, levelModifier, true);
              if (item == null)
                continue;
              if (Config.DEBUG) _log.fine("Item id to spoil: " + item.getItemId() + " amount: " + item.getCount());
              sweepList.add(item);
            }

            if (!sweepList.isEmpty())
              _sweepItems = ((RewardItem[])sweepList.toArray(new RewardItem[sweepList.size()]));
          }
        }
        else
        {
          if (isSeeded())
          {
            L2DropData drop = cat.dropSeedAllowedDropsOnly();
            if (drop == null) {
              continue;
            }
            item = calculateRewardItem(player, drop, levelModifier, false);
          }
          else
          {
            item = calculateCategorizedRewardItem(player, cat, levelModifier);
          }

          if (item != null)
          {
            if (player.isAutoLoot())
            {
              if ((!(this instanceof L2RaidBossInstance)) && (!(this instanceof L2GrandBossInstance)))
              {
                player.doAutoLoot(this, item);
              }
              else if (Config.BOSS_AUTO_LOOT)
                player.doAutoLoot(this, item);
              else dropItem(player, item);
            }
            else {
              dropItem(player, item);
            }
            if (((this instanceof L2RaidBossInstance)) && (!isRaidMinion()))
            {
              SystemMessage sm = new SystemMessage(SystemMessageId.S1_DIED_DROPPED_S3_S2);
              sm.addString(getName());
              sm.addItemName(item.getItemId());
              sm.addNumber(item.getCount());
              broadcastPacket(sm);
            }
          }
        }
      }
    if ((Config.CHAMPION_ENABLE) && (isChampion()) && (Config.CHAMPION_REWARD > 0) && (Rnd.get(100) < Config.CHAMPION_REWARD))
    {
      int champqty = Rnd.get(Config.CHAMPION_REWARD_QTY);
      champqty++;

      RewardItem item = new RewardItem(Config.CHAMPION_REWARD_ID, champqty);
      if ((Config.AUTO_LOOT) || (player.isAutoLoot())) {
        player.addItem("ChampionLoot", item.getItemId(), item.getCount(), this, true);
      }
      else
      {
        dropItem(player, item);
      }
    }
    int levelDiff = player.getLevel() - getLevel();
    double rateHp = getStat().calcStat(Stats.MAX_HP, 1.0D, this, null);
    if ((rateHp <= 1.0D) && (String.valueOf(npcTemplate.type).contentEquals("L2Monster")) && (levelDiff >= -3) && (levelDiff <= 3))
    {
      boolean _hp = false;
      boolean _mp = false;
      boolean _spec = false;
      int random = Rnd.get(1000);
      if ((random < Config.RATE_DROP_SPECIAL_HERBS) && (!_spec))
      {
        RewardItem item = new RewardItem(8612, 1);
        if ((player.isAutoLoot()) && (Config.AUTO_LOOT_HERBS)) player.addItem("Loot", item.getItemId(), item.getCount(), this, true); else
          dropItem(player, item);
        _spec = true;
      } else {
        for (int i = 0; i < 3; i++)
        {
          random = Rnd.get(100);
          if (random >= Config.RATE_DROP_COMMON_HERBS)
            continue;
          RewardItem item = null;
          if (i == 0) item = new RewardItem(8606, 1);
          if (i == 1) item = new RewardItem(8608, 1);
          if (i == 2) item = new RewardItem(8610, 1);

          if ((player.isAutoLoot()) && (Config.AUTO_LOOT_HERBS)) { player.addItem("Loot", item.getItemId(), item.getCount(), this, true); break; }
          dropItem(player, item);
          break;
        }
      }
      random = Rnd.get(1000);
      if ((random < Config.RATE_DROP_SPECIAL_HERBS) && (!_spec))
      {
        RewardItem item = new RewardItem(8613, 1);
        if ((player.isAutoLoot()) && (Config.AUTO_LOOT_HERBS)) player.addItem("Loot", item.getItemId(), item.getCount(), this, true); else
          dropItem(player, item);
        _spec = true;
      } else {
        for (int i = 0; i < 2; i++)
        {
          random = Rnd.get(100);
          if (random >= Config.RATE_DROP_COMMON_HERBS)
            continue;
          RewardItem item = null;
          if (i == 0) item = new RewardItem(8607, 1);
          if (i == 1) item = new RewardItem(8609, 1);

          if ((player.isAutoLoot()) && (Config.AUTO_LOOT_HERBS)) { player.addItem("Loot", item.getItemId(), item.getCount(), this, true); break; }
          dropItem(player, item);
          break;
        }
      }
      random = Rnd.get(1000);
      if ((random < Config.RATE_DROP_SPECIAL_HERBS) && (!_spec))
      {
        RewardItem item = new RewardItem(8614, 1);
        if ((player.isAutoLoot()) && (Config.AUTO_LOOT_HERBS)) player.addItem("Loot", item.getItemId(), item.getCount(), this, true); else
          dropItem(player, item);
        _mp = true;
        _hp = true;
        _spec = true;
      }
      if (!_hp)
      {
        random = Rnd.get(100);
        if (random < Config.RATE_DROP_MP_HP_HERBS)
        {
          RewardItem item = new RewardItem(8600, 1);
          if ((player.isAutoLoot()) && (Config.AUTO_LOOT_HERBS)) player.addItem("Loot", item.getItemId(), item.getCount(), this, true); else
            dropItem(player, item);
          _hp = true;
        }
      }
      if (!_hp)
      {
        random = Rnd.get(100);
        if (random < Config.RATE_DROP_GREATER_HERBS)
        {
          RewardItem item = new RewardItem(8601, 1);
          if ((player.isAutoLoot()) && (Config.AUTO_LOOT_HERBS)) player.addItem("Loot", item.getItemId(), item.getCount(), this, true); else
            dropItem(player, item);
          _hp = true;
        }
      }
      if (!_hp)
      {
        random = Rnd.get(1000);
        if (random < Config.RATE_DROP_SUPERIOR_HERBS)
        {
          RewardItem item = new RewardItem(8602, 1);
          if ((player.isAutoLoot()) && (Config.AUTO_LOOT_HERBS)) player.addItem("Loot", item.getItemId(), item.getCount(), this, true); else
            dropItem(player, item);
        }
      }
      if (!_mp)
      {
        random = Rnd.get(100);
        if (random < Config.RATE_DROP_MP_HP_HERBS)
        {
          RewardItem item = new RewardItem(8603, 1);
          if ((player.isAutoLoot()) && (Config.AUTO_LOOT_HERBS)) player.addItem("Loot", item.getItemId(), item.getCount(), this, true); else
            dropItem(player, item);
          _mp = true;
        }
      }
      if (!_mp)
      {
        random = Rnd.get(100);
        if (random < Config.RATE_DROP_GREATER_HERBS)
        {
          RewardItem item = new RewardItem(8604, 1);
          if ((player.isAutoLoot()) && (Config.AUTO_LOOT_HERBS)) player.addItem("Loot", item.getItemId(), item.getCount(), this, true); else
            dropItem(player, item);
          _mp = true;
        }
      }
      if (!_mp)
      {
        random = Rnd.get(1000);
        if (random < Config.RATE_DROP_SUPERIOR_HERBS)
        {
          RewardItem item = new RewardItem(8605, 1);
          if ((player.isAutoLoot()) && (Config.AUTO_LOOT_HERBS)) player.addItem("Loot", item.getItemId(), item.getCount(), this, true); else
            dropItem(player, item);
        }
      }
      random = Rnd.get(100);
      if (random < Config.RATE_DROP_COMMON_HERBS)
      {
        RewardItem item = new RewardItem(8611, 1);
        if ((player.isAutoLoot()) && (Config.AUTO_LOOT_HERBS)) player.addItem("Loot", item.getItemId(), item.getCount(), this, true); else
          dropItem(player, item);
      }
    }
  }

  public void doEventDrop(L2Character lastAttacker)
  {
    L2PcInstance player = null;
    if ((lastAttacker instanceof L2PcInstance))
      player = (L2PcInstance)lastAttacker;
    else if ((lastAttacker instanceof L2Summon)) {
      player = ((L2Summon)lastAttacker).getOwner();
    }
    if (player == null) return;

    if (player.getLevel() - getLevel() > 9) return;

    for (EventDroplist.DateDrop drop : EventDroplist.getInstance().getAllDrops())
    {
      if (Rnd.get(1000000) < drop.chance)
      {
        RewardItem item = new RewardItem(drop.items[Rnd.get(drop.items.length)], Rnd.get(drop.min, drop.max));
        if (player.isAutoLoot()) player.doAutoLoot(this, item); else
          dropItem(player, item);
      }
    }
  }

  public L2ItemInstance dropItem(L2PcInstance lastAttacker, RewardItem item)
  {
    int randDropLim = 70;

    L2ItemInstance ditem = null;
    for (int i = 0; i < item.getCount(); i++)
    {
      int newX = getX() + Rnd.get(randDropLim * 2 + 1) - randDropLim;
      int newY = getY() + Rnd.get(randDropLim * 2 + 1) - randDropLim;
      int newZ = Math.max(getZ(), lastAttacker.getZ()) + 20;

      ditem = ItemTable.getInstance().createItem("Loot", item.getItemId(), item.getCount(), lastAttacker, this);
      ditem.dropMe(this, newX, newY, newZ);

      if ((!Config.LIST_PROTECTED_ITEMS.contains(Integer.valueOf(item.getItemId()))) && (
        ((Config.AUTODESTROY_ITEM_AFTER > 0) && (ditem.getItemType() != L2EtcItemType.HERB)) || ((Config.HERB_AUTO_DESTROY_TIME > 0) && (ditem.getItemType() == L2EtcItemType.HERB))))
      {
        ItemsAutoDestroy.getInstance().addItem(ditem);
      }
      ditem.setProtected(false);

      if ((ditem.isStackable()) || (!Config.MULTIPLE_ITEM_DROP)) break;
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
    return getAggroListRP().isEmpty();
  }

  public boolean containsTarget(L2Character player)
  {
    return getAggroListRP().containsKey(player);
  }

  public void clearAggroList()
  {
    getAggroList().clear();
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

  public synchronized RewardItem[] takeHarvest()
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
    if (!(this instanceof L2MonsterInstance)) {
      return;
    }

    if (attacker == null) {
      return;
    }

    if (getAbsorbLevel() == 0) {
      return;
    }

    AbsorberInfo ai = (AbsorberInfo)_absorbersList.get(attacker);

    if (ai == null)
    {
      ai = new AbsorberInfo(attacker, crystalId, getCurrentHp());
      _absorbersList.put(attacker, ai);
    }
    else
    {
      ai._absorber = attacker;
      ai._crystalId = crystalId;
      ai._absorbedHP = getCurrentHp();
    }

    absorbSoul();
  }

  private void levelSoulCrystals(L2Character attacker)
  {
    if ((!(attacker instanceof L2PcInstance)) && (!(attacker instanceof L2Summon)))
    {
      resetAbsorbList(); return;
    }

    int maxAbsorbLevel = getAbsorbLevel();
    int minAbsorbLevel = 0;

    if (maxAbsorbLevel == 0)
    {
      resetAbsorbList(); return;
    }

    if (maxAbsorbLevel > 10) {
      minAbsorbLevel = maxAbsorbLevel > 12 ? 12 : 10;
    }

    boolean isSuccess = true;
    boolean doLevelup = true;
    boolean isBossMob = maxAbsorbLevel > 10;

    L2NpcTemplate.AbsorbCrystalType absorbType = getTemplate().absorbType;

    L2PcInstance killer = (attacker instanceof L2Summon) ? ((L2Summon)attacker).getOwner() : (L2PcInstance)attacker;

    if (!isBossMob)
    {
      if (!isAbsorbed())
      {
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
    else {
      players.add(killer);
    }
    for (L2PcInstance player : players)
    {
      if (player != null) {
        crystalQTY = 0;

        L2ItemInstance[] inv = player.getInventory().getItems();
        for (L2ItemInstance item : inv)
        {
          int itemId = item.getItemId();
          for (int id : SoulCrystal.SoulCrystalTable)
          {
            if (id != itemId)
              continue;
            crystalQTY++;

            if (crystalQTY > 1)
            {
              isSuccess = false; break;
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
                else
                {
                  crystalNFO = item.getItem().getName().trim().replace(" Stage ", "").split("-");

                  crystalLVL = Integer.parseInt(crystalNFO[1].trim());

                  crystalNME = crystalNFO[0].toLowerCase();
                }

                if (crystalLVL > 9)
                {
                  for (int i = 0; i < SoulCrystal.HighSoulConvert.length; i++)
                  {
                    if (id != SoulCrystal.HighSoulConvert[i][0])
                      continue;
                    crystalNEW = SoulCrystal.HighSoulConvert[i][1]; break;
                  }
                }
                else
                  crystalNEW = id + 1;
              }
              catch (NumberFormatException nfe)
              {
                _log.log(Level.WARNING, "An attempt to identify a soul crystal failed, verify the names have not changed in etcitem table.", nfe);

                player.sendMessage("There has been an error handling your soul crystal. Please notify your server admin.");

                isSuccess = false;
                break;
              }
              catch (Exception e)
              {
                e.printStackTrace();
                isSuccess = false;
                break;
              }
            }
            else
            {
              crystalNME = item.getItem().getName().toLowerCase().trim();
              crystalNEW = id + 1;
            }

            crystalOLD = id;
            break;
          }

          if (!isSuccess) {
            break;
          }
        }
        if ((crystalLVL < minAbsorbLevel) || (crystalLVL >= maxAbsorbLevel)) {
          doLevelup = false;
        }

        if ((crystalQTY < 1) || (crystalQTY > 1) || (!isSuccess) || (!doLevelup))
        {
          if (crystalQTY > 1)
          {
            player.sendPacket(new SystemMessage(SystemMessageId.SOUL_CRYSTAL_ABSORBING_FAILED_RESONATION));
          }
          else if ((!doLevelup) && (crystalQTY > 0)) {
            player.sendPacket(new SystemMessage(SystemMessageId.SOUL_CRYSTAL_ABSORBING_REFUSED));
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
          else if (crystalNME.startsWith("blu"))
            exchangeCrystal(player, crystalOLD, 4664, true);
          resetAbsorbList();
        }
        else {
          player.sendPacket(new SystemMessage(SystemMessageId.SOUL_CRYSTAL_ABSORBING_FAILED));
        }
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
      {
        player.sendPacket(new SystemMessage(SystemMessageId.SOUL_CRYSTAL_BROKE));
      }
      else {
        player.sendPacket(new SystemMessage(SystemMessageId.SOUL_CRYSTAL_ABSORBING_SUCCEEDED));
      }

      SystemMessage sms = new SystemMessage(SystemMessageId.EARNED_ITEM);
      sms.addItemName(giveid);
      player.sendPacket(sms);

      player.sendPacket(playerIU);
    }
  }

  private void resetAbsorbList()
  {
    _absorbed = false;
    _absorbersList.clear();
  }

  private int[] calculateExpAndSp(int diff, int damage, int isPremium)
  {
    if (diff < -5) diff = -5;
    double xp = getExpReward(isPremium) * damage / getMaxHp();
    if (Config.ALT_GAME_EXPONENT_XP != 0.0F) xp *= Math.pow(2.0D, -diff / Config.ALT_GAME_EXPONENT_XP);

    double sp = getSpReward(isPremium) * damage / getMaxHp();
    if (Config.ALT_GAME_EXPONENT_SP != 0.0F) sp *= Math.pow(2.0D, -diff / Config.ALT_GAME_EXPONENT_SP);

    if ((Config.ALT_GAME_EXPONENT_XP == 0.0F) && (Config.ALT_GAME_EXPONENT_SP == 0.0F))
    {
      if (diff > 5)
      {
        double pow = Math.pow(0.8333333333333334D, diff - 5);
        xp *= pow;
        sp *= pow;
      }

      if (xp <= 0.0D)
      {
        xp = 0.0D;
        sp = 0.0D;
      }
      else if (sp <= 0.0D)
      {
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

  public void onSpawn()
  {
    super.onSpawn();
    if (Config.CHAMPION_ENABLE)
    {
      if ((!(this instanceof L2GrandBossInstance)) && (!(this instanceof L2RaidBossInstance)) && (!(this instanceof L2MinionInstance)) && ((this instanceof L2MonsterInstance)) && (Config.CHAMPION_FREQUENCY > 0) && (getLevel() >= Config.CHAMPION_MIN_LVL) && (getLevel() <= Config.CHAMPION_MAX_LVL))
      {
        int random = Rnd.get(100);
        if (random < Config.CHAMPION_FREQUENCY)
          setChampion(true);
      }
    }
    setSpoil(false);

    clearAggroList();

    _harvestItems = null;

    setSeeded(false);

    _sweepItems = null;
    resetAbsorbList();

    setWalking();

    if (!isInActiveRegion().booleanValue())
      if ((this instanceof L2SiegeGuardInstance))
        ((L2SiegeGuardAI)getAI()).stopAITask();
      else
        ((L2AttackableAI)getAI()).stopAITask();
  }

  public void firstSpawn()
  {
    super.onSpawn();
    setWalking();
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

  public void setSeeded(int id, int seederLvl)
  {
    _seeded = true;
    _seedType = id;
    int count = 1;

    Map skills = getTemplate().getSkills();
    Iterator i$;
    if (skills != null)
    {
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

    if (diff > 0)
    {
      count += diff;
    }

    FastList harvested = new FastList();

    harvested.add(new RewardItem(L2Manor.getInstance().getCropType(_seedType), count * Config.RATE_DROP_MANOR));

    _harvestItems = ((RewardItem[])harvested.toArray(new RewardItem[harvested.size()]));
  }

  public void setSeeded(boolean seeded)
  {
    _seeded = seeded;
  }

  public L2PcInstance getSeeder()
  {
    return _seeder;
  }

  public int getSeedType()
  {
    return _seedType;
  }

  public boolean isSeeded()
  {
    return _seeded;
  }

  private int getAbsorbLevel()
  {
    return getTemplate().absorbLevel;
  }

  public boolean hasRandomAnimation()
  {
    return (Config.MAX_MONSTER_ANIMATION > 0) && (!(this instanceof L2GrandBossInstance));
  }

  public boolean isMob()
  {
    return true;
  }

  protected void setCommandChannelTimer(CommandChannelTimer commandChannelTimer) {
    _commandChannelTimer = commandChannelTimer;
  }

  public CommandChannelTimer getCommandChannelTimer() {
    return _commandChannelTimer;
  }

  public L2CommandChannel getFirstCommandChannelAttacked() {
    return _firstCommandChannelAttacked;
  }

  public void setFirstCommandChannelAttacked(L2CommandChannel firstCommandChannelAttacked)
  {
    _firstCommandChannelAttacked = firstCommandChannelAttacked;
  }

  public void returnHome()
  {
    clearAggroList();

    if (hasAI())
      getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(getSpawn().getLocx(), getSpawn().getLocy(), getSpawn().getLocz(), 0));
  }

  public void teleToHome() {
    clearAggroList();
    if (hasAI())
      teleToLocation(getSpawn().getLocx(), getSpawn().getLocy(), getSpawn().getLocz());
  }

  private class CommandChannelTimer
    implements Runnable
  {
    private L2Attackable _monster;
    private L2CommandChannel _channel;

    public CommandChannelTimer(L2Attackable monster, L2CommandChannel channel)
    {
      _monster = monster;
      _channel = channel;
    }

    public void run()
    {
      _monster.setCommandChannelTimer(null);
      _monster.setFirstCommandChannelAttacked(null);
      for (L2Character player : _monster.getAggroListRP().keySet())
      {
        if ((player.isInParty()) && (player.getParty().isInCommandChannel()))
        {
          if (player.getParty().getCommandChannel().equals(_channel))
          {
            _monster.setCommandChannelTimer(this);
            _monster.setFirstCommandChannelAttacked(_channel);
            ThreadPoolManager.getInstance().scheduleGeneral(this, 300000L);
            break;
          }
        }
      }
    }
  }

  class OnKillNotifyTask
    implements Runnable
  {
    private L2Attackable _attackable;
    private Quest _quest;
    private L2PcInstance _killer;
    private boolean _isPet;

    public OnKillNotifyTask(L2Attackable attackable, Quest quest, L2PcInstance killer, boolean isPet)
    {
      _attackable = attackable;
      _quest = quest;
      _killer = killer;
      _isPet = isPet;
    }

    public void run() {
      _quest.notifyKill(_attackable, _killer, _isPet);
    }
  }

  public final class RewardItem
  {
    protected int _itemId;
    protected int _count;

    public RewardItem(int itemId, int count)
    {
      _itemId = itemId;
      _count = count;
    }
    public int getItemId() {
      return _itemId; } 
    public int getCount() { return _count;
    }
  }

  public final class AbsorberInfo
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
      if (this == obj) return true;
      if ((obj instanceof AbsorberInfo)) return ((AbsorberInfo)obj)._absorber == _absorber;
      return false;
    }

    public int hashCode()
    {
      return _absorber.getObjectId();
    }
  }

  protected final class RewardInfo
  {
    protected L2Character _attacker;
    protected int _dmg = 0;

    public RewardInfo(L2Character pAttacker, int pDmg)
    {
      _attacker = pAttacker;
      _dmg = pDmg;
    }

    public void addDamage(int pDmg)
    {
      _dmg += pDmg;
    }

    public boolean equals(Object obj)
    {
      if (this == obj) return true;
      if ((obj instanceof RewardInfo)) return ((RewardInfo)obj)._attacker == _attacker;
      return false;
    }

    public int hashCode()
    {
      return _attacker.getObjectId();
    }
  }

  public final class AggroInfo
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
      if (this == obj) return true;
      if ((obj instanceof AggroInfo)) return ((AggroInfo)obj)._attacker == _attacker;
      return false;
    }

    public int hashCode()
    {
      return _attacker.getObjectId();
    }
  }
}
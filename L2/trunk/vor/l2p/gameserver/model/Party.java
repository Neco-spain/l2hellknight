package l2p.gameserver.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import l2p.commons.collections.LazyArrayList;
import l2p.commons.threading.RunnableImpl;
import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.instancemanager.ReflectionManager;
import l2p.gameserver.model.actor.instances.player.Bonus;
import l2p.gameserver.model.actor.listener.PlayerListenerList;
import l2p.gameserver.model.base.Experience;
import l2p.gameserver.model.entity.DimensionalRift;
import l2p.gameserver.model.entity.Reflection;
import l2p.gameserver.model.entity.SevenSignsFestival.DarknessFestival;
import l2p.gameserver.model.instances.MonsterInstance;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.serverpackets.ExAskModifyPartyLooting;
import l2p.gameserver.serverpackets.ExMPCCClose;
import l2p.gameserver.serverpackets.ExMPCCOpen;
import l2p.gameserver.serverpackets.ExPartyPetWindowAdd;
import l2p.gameserver.serverpackets.ExPartyPetWindowDelete;
import l2p.gameserver.serverpackets.ExSetPartyLooting;
import l2p.gameserver.serverpackets.GetItem;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.PartyMemberPosition;
import l2p.gameserver.serverpackets.PartySmallWindowAdd;
import l2p.gameserver.serverpackets.PartySmallWindowAll;
import l2p.gameserver.serverpackets.PartySmallWindowDelete;
import l2p.gameserver.serverpackets.PartySmallWindowDeleteAll;
import l2p.gameserver.serverpackets.PartySpelled;
import l2p.gameserver.serverpackets.RelationChanged;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.serverpackets.SystemMessage2;
import l2p.gameserver.serverpackets.components.IStaticPacket;
import l2p.gameserver.taskmanager.LazyPrecisionTaskManager;
import l2p.gameserver.templates.InstantZone;
import l2p.gameserver.utils.ItemFunctions;
import l2p.gameserver.utils.Location;
import l2p.gameserver.utils.Log;

public class Party
  implements PlayerGroup
{
  public static final int MAX_SIZE = 9;
  public static final int ITEM_LOOTER = 0;
  public static final int ITEM_RANDOM = 1;
  public static final int ITEM_RANDOM_SPOIL = 2;
  public static final int ITEM_ORDER = 3;
  public static final int ITEM_ORDER_SPOIL = 4;
  private final List<Player> _members = new CopyOnWriteArrayList();

  private int _partyLvl = 0;
  private int _itemDistribution = 0;
  private int _itemOrder = 0;
  private int _dimentionalRift;
  private Reflection _reflection;
  private CommandChannel _commandChannel;
  public double _rateExp;
  public double _rateSp;
  public double _rateDrop;
  public double _rateAdena;
  public double _rateSpoil;
  private ScheduledFuture<?> positionTask;
  private int _requestChangeLoot = -1;
  private long _requestChangeLootTimer = 0L;
  private Set<Integer> _changeLootAnswers = null;
  private static final int[] LOOT_SYSSTRINGS = { 487, 488, 798, 799, 800 };
  private Future<?> _checkTask = null;

  public Party(Player leader, int itemDistribution)
  {
    _itemDistribution = itemDistribution;
    _members.add(leader);
    _partyLvl = leader.getLevel();
    _rateExp = leader.getBonus().getRateXp();
    _rateSp = leader.getBonus().getRateSp();
    _rateAdena = leader.getBonus().getDropAdena();
    _rateDrop = leader.getBonus().getDropItems();
    _rateSpoil = leader.getBonus().getDropSpoil();
  }

  public int getMemberCount()
  {
    return _members.size();
  }

  public int getMemberCountInRange(Player player, int range)
  {
    int count = 0;
    for (Player member : _members)
      if ((member == player) || (member.isInRangeZ(player, range)))
        count++;
    return count;
  }

  public List<Player> getPartyMembers()
  {
    return _members;
  }

  public List<Integer> getPartyMembersObjIds()
  {
    List result = new ArrayList(_members.size());
    for (Player member : _members)
      result.add(Integer.valueOf(member.getObjectId()));
    return result;
  }

  public List<Playable> getPartyMembersWithPets()
  {
    List result = new ArrayList();
    for (Player member : _members)
    {
      result.add(member);
      if (member.getPet() != null)
        result.add(member.getPet());
    }
    return result;
  }

  private Player getNextLooterInRange(Player player, ItemInstance item, int range)
  {
    synchronized (_members)
    {
      int antiloop = _members.size();
      while (true) { antiloop--; if (antiloop <= 0)
          break;
        int looter = _itemOrder;
        _itemOrder += 1;
        if (_itemOrder > _members.size() - 1) {
          _itemOrder = 0;
        }
        Player ret = looter < _members.size() ? (Player)_members.get(looter) : player;

        if ((ret != null) && (!ret.isDead()) && (ret.isInRangeZ(player, range)) && (ret.getInventory().validateCapacity(item)) && (ret.getInventory().validateWeight(item)))
          return ret;
      }
    }
    return player;
  }

  public boolean isLeader(Player player)
  {
    return getPartyLeader() == player;
  }

  public Player getPartyLeader()
  {
    synchronized (_members)
    {
      if (_members.size() == 0)
        return null;
      return (Player)_members.get(0);
    }
  }

  public void broadCast(IStaticPacket[] msg)
  {
    for (Player member : _members)
      member.sendPacket(msg);
  }

  public void broadcastMessageToPartyMembers(String msg)
  {
    broadCast(new IStaticPacket[] { new SystemMessage(msg) });
  }

  public void broadcastToPartyMembers(Player exclude, L2GameServerPacket msg)
  {
    for (Player member : _members)
      if (exclude != member)
        member.sendPacket(msg);
  }

  public void broadcastToPartyMembersInRange(Player player, L2GameServerPacket msg, int range)
  {
    for (Player member : _members)
      if (player.isInRangeZ(member, range))
        member.sendPacket(msg);
  }

  public boolean containsMember(Player player)
  {
    return _members.contains(player);
  }

  public boolean addPartyMember(Player player)
  {
    Player leader = getPartyLeader();
    if (leader == null) {
      return false;
    }
    synchronized (_members)
    {
      if (_members.isEmpty())
        return false;
      if (_members.contains(player))
        return false;
      if (_members.size() == 9)
        return false;
      _members.add(player);
    }

    if (_requestChangeLoot != -1) {
      finishLootRequest(false);
    }
    player.setParty(this);
    player.getListeners().onPartyInvite();

    Object addInfo = new ArrayList(4 + _members.size() * 4);
    List pplayer = new ArrayList(20);

    pplayer.add(new PartySmallWindowAll(this, player));
    pplayer.add(new SystemMessage(106).addName(leader));

    ((List)addInfo).add(new SystemMessage(107).addName(player));
    ((List)addInfo).add(new PartySpelled(player, true));
    Summon pet;
    if ((pet = player.getPet()) != null)
    {
      ((List)addInfo).add(new ExPartyPetWindowAdd(pet));
      ((List)addInfo).add(new PartySpelled(pet, true));
    }

    PartyMemberPosition pmp = new PartyMemberPosition();

    for (Player member : _members) {
      if (member != player)
      {
        List pmember = new ArrayList(((List)addInfo).size() + 4);
        pmember.addAll((Collection)addInfo);
        pmember.add(new PartySmallWindowAdd(member, player));
        pmember.add(new PartyMemberPosition().add(player));
        pmember.add(RelationChanged.update(member, player, member));
        member.sendPacket(pmember);

        pplayer.add(new PartySpelled(member, true));
        if ((pet = member.getPet()) != null)
          pplayer.add(new PartySpelled(pet, true));
        pplayer.add(RelationChanged.update(player, member, player));
        pmp.add(member);
      }
    }
    pplayer.add(pmp);

    if (isInCommandChannel()) {
      pplayer.add(ExMPCCOpen.STATIC);
    }
    player.sendPacket(pplayer);

    startUpdatePositionTask();
    recalculatePartyData();

    if ((isInReflection()) && ((getReflection() instanceof DimensionalRift))) {
      ((DimensionalRift)getReflection()).partyMemberInvited();
    }
    return true;
  }

  public void dissolveParty()
  {
    for (Player p : _members)
    {
      p.sendPacket(PartySmallWindowDeleteAll.STATIC);
      p.setParty(null);
    }

    synchronized (_members)
    {
      _members.clear();
    }

    setDimensionalRift(null);
    setCommandChannel(null);
    stopUpdatePositionTask();
  }

  public boolean removePartyMember(Player player, boolean kick)
  {
    boolean isLeader = isLeader(player);
    boolean dissolve = false;

    synchronized (_members)
    {
      if (!_members.remove(player))
        return false;
      dissolve = _members.size() == 1;
    }

    player.getListeners().onPartyLeave();

    player.setParty(null);
    recalculatePartyData();

    List pplayer = new ArrayList(4 + _members.size() * 2);

    if (isInCommandChannel())
      pplayer.add(ExMPCCClose.STATIC);
    if (kick)
      pplayer.add(Msg.YOU_HAVE_BEEN_EXPELLED_FROM_THE_PARTY);
    else
      pplayer.add(Msg.YOU_HAVE_WITHDRAWN_FROM_THE_PARTY);
    pplayer.add(PartySmallWindowDeleteAll.STATIC);

    List outsInfo = new ArrayList(3);
    Summon pet;
    if ((pet = player.getPet()) != null)
      outsInfo.add(new ExPartyPetWindowDelete(pet));
    outsInfo.add(new PartySmallWindowDelete(player));
    if (kick)
      outsInfo.add(new SystemMessage(201).addName(player));
    else {
      outsInfo.add(new SystemMessage(108).addName(player));
    }

    for (Player member : _members)
    {
      List pmember = new ArrayList(2 + outsInfo.size());
      pmember.addAll(outsInfo);
      pmember.add(RelationChanged.update(member, player, member));
      member.sendPacket(pmember);
      pplayer.add(RelationChanged.update(player, member, player));
    }

    player.sendPacket(pplayer);

    Reflection reflection = getReflection();

    if ((reflection instanceof DarknessFestival))
      ((DarknessFestival)reflection).partyMemberExited();
    else if ((isInReflection()) && ((getReflection() instanceof DimensionalRift)))
      ((DimensionalRift)getReflection()).partyMemberExited(player);
    if ((reflection != null) && (player.getReflection() == reflection) && (reflection.getReturnLoc() != null)) {
      player.teleToLocation(reflection.getReturnLoc(), ReflectionManager.DEFAULT);
    }
    Player leader = getPartyLeader();

    if (dissolve)
    {
      if (isInCommandChannel())
        _commandChannel.removeParty(this);
      else if (reflection != null)
      {
        if ((reflection.getInstancedZone() != null) && (reflection.getInstancedZone().isCollapseOnPartyDismiss()))
        {
          if (reflection.getParty() == this)
            reflection.startCollapseTimer(reflection.getInstancedZone().getTimerOnCollapse() * 1000);
          if ((leader != null) && (leader.getReflection() == reflection)) {
            leader.broadcastPacket(new L2GameServerPacket[] { new SystemMessage(2106).addNumber(1) });
          }
        }
      }
      dissolveParty();
    }
    else
    {
      if ((isInCommandChannel()) && (_commandChannel.getChannelLeader() == player)) {
        _commandChannel.setChannelLeader(leader);
      }
      if (isLeader) {
        updateLeaderInfo();
      }
    }
    if (_checkTask != null)
    {
      _checkTask.cancel(true);
      _checkTask = null;
    }

    return true;
  }

  public boolean changePartyLeader(Player player)
  {
    Player leader = getPartyLeader();

    synchronized (_members)
    {
      int index = _members.indexOf(player);
      if (index == -1)
        return false;
      _members.set(0, player);
      _members.set(index, leader);
    }

    updateLeaderInfo();

    if ((isInCommandChannel()) && (_commandChannel.getChannelLeader() == leader)) {
      _commandChannel.setChannelLeader(player);
    }
    return true;
  }

  private void updateLeaderInfo()
  {
    Player leader = getPartyLeader();
    if (leader == null) {
      return;
    }
    SystemMessage msg = new SystemMessage(1384).addName(leader);

    for (Player member : _members)
    {
      member.sendPacket(new IStaticPacket[] { PartySmallWindowDeleteAll.STATIC, new PartySmallWindowAll(this, member), msg });
    }

    for (Player member : _members)
    {
      broadcastToPartyMembers(member, new PartySpelled(member, true));
      if (member.getPet() != null)
        broadCast(new IStaticPacket[] { new ExPartyPetWindowAdd(member.getPet()) });
    }
  }

  public Player getPlayerByName(String name)
  {
    for (Player member : _members)
      if (name.equalsIgnoreCase(member.getName()))
        return member;
    return null;
  }

  public void distributeItem(Player player, ItemInstance item, NpcInstance fromNpc)
  {
    switch (item.getItemId())
    {
    case 57:
      distributeAdena(player, item, fromNpc);
      break;
    default:
      distributeItem0(player, item, fromNpc);
    }
  }

  private void distributeItem0(Player player, ItemInstance item, NpcInstance fromNpc)
  {
    Player target = null;

    List ret = null;
    switch (_itemDistribution)
    {
    case 1:
    case 2:
      ret = new ArrayList(_members.size());
      for (Player member : _members) {
        if ((member.isInRangeZ(player, Config.ALT_PARTY_DISTRIBUTION_RANGE)) && (!member.isDead()) && (member.getInventory().validateCapacity(item)) && (member.getInventory().validateWeight(item)))
          ret.add(member);
      }
      target = ret.isEmpty() ? null : (Player)ret.get(Rnd.get(ret.size()));
      break;
    case 3:
    case 4:
      synchronized (_members)
      {
        ret = new CopyOnWriteArrayList(_members);
        while ((target == null) && (!ret.isEmpty()))
        {
          int looter = _itemOrder;
          _itemOrder += 1;
          if (_itemOrder > ret.size() - 1) {
            _itemOrder = 0;
          }
          Player looterPlayer = looter < ret.size() ? (Player)ret.get(looter) : null;

          if (looterPlayer != null)
          {
            if ((!looterPlayer.isDead()) && (looterPlayer.isInRangeZ(player, Config.ALT_PARTY_DISTRIBUTION_RANGE)) && (ItemFunctions.canAddItem(looterPlayer, item)))
              target = looterPlayer;
            else {
              ret.remove(looterPlayer);
            }
          }
        }
      }
      if (target != null) break;
      return;
    case 0:
    default:
      target = player;
    }

    if (target == null) {
      target = player;
    }
    if (target.pickupItem(item, "PartyPickup"))
    {
      if (fromNpc == null) {
        player.broadcastPacket(new L2GameServerPacket[] { new GetItem(item, player.getObjectId()) });
      }
      player.broadcastPickUpMsg(item);
      item.pickupMe();

      broadcastToPartyMembers(target, SystemMessage2.obtainItemsBy(item, target));
    }
    else {
      item.dropToTheGround(player, fromNpc);
    }
  }

  private void distributeAdena(Player player, ItemInstance item, NpcInstance fromNpc) {
    if (player == null) {
      return;
    }
    List membersInRange = new ArrayList();

    if (item.getCount() < _members.size()) {
      membersInRange.add(player);
    }
    else {
      for (Player member : _members) {
        if ((!member.isDead()) && ((member == player) || (player.isInRangeZ(member, Config.ALT_PARTY_DISTRIBUTION_RANGE))) && (ItemFunctions.canAddItem(player, item)))
          membersInRange.add(member);
      }
    }
    if (membersInRange.isEmpty()) {
      membersInRange.add(player);
    }
    long totalAdena = item.getCount();
    long amount = totalAdena / membersInRange.size();
    long ost = totalAdena % membersInRange.size();

    for (Player member : membersInRange)
    {
      long count = member.equals(player) ? amount + ost : amount;
      member.getInventory().addAdena(count);
      member.sendPacket(SystemMessage2.obtainItems(57, count, 0));
    }

    if (fromNpc == null) {
      player.broadcastPacket(new L2GameServerPacket[] { new GetItem(item, player.getObjectId()) });
    }
    item.pickupMe();
  }

  public void distributeXpAndSp(double xpReward, double spReward, List<Player> rewardedMembers, Creature lastAttacker, MonsterInstance monster)
  {
    recalculatePartyData();

    List mtr = new ArrayList();
    int partyLevel = lastAttacker.getLevel();
    int partyLvlSum = 0;

    for (Player member : rewardedMembers)
    {
      if (!monster.isInRangeZ(member, Config.ALT_PARTY_DISTRIBUTION_RANGE))
        continue;
      partyLevel = Math.max(partyLevel, member.getLevel());
    }

    for (Player member : rewardedMembers)
    {
      if ((!monster.isInRangeZ(member, Config.ALT_PARTY_DISTRIBUTION_RANGE)) || 
        (member.getLevel() <= partyLevel - 15))
        continue;
      partyLvlSum += member.getLevel();
      mtr.add(member);
    }

    if (mtr.isEmpty()) {
      return;
    }

    double bonus = Config.ALT_PARTY_BONUS[(mtr.size() - 1)];

    double XP = xpReward * bonus;
    double SP = spReward * bonus;

    for (Player member : mtr)
    {
      double lvlPenalty = Experience.penaltyModifier(monster.calculateLevelDiffForDrop(member.getLevel()), 9.0D);
      int lvlDiff = partyLevel - member.getLevel();
      if ((lvlDiff >= 10) && (lvlDiff <= 14)) {
        lvlPenalty *= 0.3D;
      }

      double memberXp = XP * lvlPenalty * member.getLevel() / partyLvlSum;
      double memberSp = SP * lvlPenalty * member.getLevel() / partyLvlSum;

      memberXp = Math.min(memberXp, xpReward);
      memberSp = Math.min(memberSp, spReward);

      member.addExpAndCheckBonus(monster, ()memberXp, ()memberSp, memberXp / xpReward);
    }

    recalculatePartyData();
  }

  public void recalculatePartyData()
  {
    _partyLvl = 0;
    double rateExp = 0.0D;
    double rateSp = 0.0D;
    double rateDrop = 0.0D;
    double rateAdena = 0.0D;
    double rateSpoil = 0.0D;
    double minRateExp = 1.7976931348623157E+308D;
    double minRateSp = 1.7976931348623157E+308D;
    double minRateDrop = 1.7976931348623157E+308D;
    double minRateAdena = 1.7976931348623157E+308D;
    double minRateSpoil = 1.7976931348623157E+308D;
    int count = 0;

    for (Player member : _members)
    {
      int level = member.getLevel();
      _partyLvl = Math.max(_partyLvl, level);
      count++;

      rateExp += member.getBonus().getRateXp();
      rateSp += member.getBonus().getRateSp();
      rateDrop += member.getBonus().getDropItems();
      rateAdena += member.getBonus().getDropAdena();
      rateSpoil += member.getBonus().getDropSpoil();

      minRateExp = Math.min(minRateExp, member.getBonus().getRateXp());
      minRateSp = Math.min(minRateSp, member.getBonus().getRateSp());
      minRateDrop = Math.min(minRateDrop, member.getBonus().getDropItems());
      minRateAdena = Math.min(minRateAdena, member.getBonus().getDropAdena());
      minRateSpoil = Math.min(minRateSpoil, member.getBonus().getDropSpoil());
    }

    _rateExp = (Config.RATE_PARTY_MIN ? minRateExp : rateExp / count);
    _rateSp = (Config.RATE_PARTY_MIN ? minRateSp : rateSp / count);
    _rateDrop = (Config.RATE_PARTY_MIN ? minRateDrop : rateDrop / count);
    _rateAdena = (Config.RATE_PARTY_MIN ? minRateAdena : rateAdena / count);
    _rateSpoil = (Config.RATE_PARTY_MIN ? minRateSpoil : rateSpoil / count);
  }

  public int getLevel()
  {
    return _partyLvl;
  }

  public int getLootDistribution()
  {
    return _itemDistribution;
  }

  public boolean isDistributeSpoilLoot()
  {
    boolean rv = false;

    if ((_itemDistribution == 2) || (_itemDistribution == 4)) {
      rv = true;
    }
    return rv;
  }

  public boolean isInDimensionalRift()
  {
    return (_dimentionalRift > 0) && (getDimensionalRift() != null);
  }

  public void setDimensionalRift(DimensionalRift dr)
  {
    _dimentionalRift = (dr == null ? 0 : dr.getId());
  }

  public DimensionalRift getDimensionalRift()
  {
    return _dimentionalRift == 0 ? null : (DimensionalRift)ReflectionManager.getInstance().get(_dimentionalRift);
  }

  public boolean isInReflection()
  {
    if (_reflection != null)
      return true;
    if (_commandChannel != null)
      return _commandChannel.isInReflection();
    return false;
  }

  public void setReflection(Reflection reflection)
  {
    _reflection = reflection;
  }

  public Reflection getReflection()
  {
    if (_reflection != null)
      return _reflection;
    if (_commandChannel != null)
      return _commandChannel.getReflection();
    return null;
  }

  public boolean isInCommandChannel()
  {
    return _commandChannel != null;
  }

  public CommandChannel getCommandChannel()
  {
    return _commandChannel;
  }

  public void setCommandChannel(CommandChannel channel)
  {
    _commandChannel = channel;
  }

  public void Teleport(int x, int y, int z)
  {
    TeleportParty(getPartyMembers(), new Location(x, y, z));
  }

  public void Teleport(Location dest)
  {
    TeleportParty(getPartyMembers(), dest);
  }

  public void Teleport(Territory territory)
  {
    RandomTeleportParty(getPartyMembers(), territory);
  }

  public void Teleport(Territory territory, Location dest)
  {
    TeleportParty(getPartyMembers(), territory, dest);
  }

  public static void TeleportParty(List<Player> members, Location dest)
  {
    for (Player _member : members)
    {
      if (_member == null)
        continue;
      _member.teleToLocation(dest);
    }
  }

  public static void TeleportParty(List<Player> members, Territory territory, Location dest)
  {
    if (!territory.isInside(dest.x, dest.y))
    {
      Log.add("TeleportParty: dest is out of territory", "errors");
      Thread.dumpStack();
      return;
    }
    int base_x = ((Player)members.get(0)).getX();
    int base_y = ((Player)members.get(0)).getY();

    for (Player _member : members)
    {
      if (_member == null)
        continue;
      int diff_x = _member.getX() - base_x;
      int diff_y = _member.getY() - base_y;
      Location loc = new Location(dest.x + diff_x, dest.y + diff_y, dest.z);
      while (!territory.isInside(loc.x, loc.y))
      {
        diff_x = loc.x - dest.x;
        diff_y = loc.y - dest.y;
        if (diff_x != 0)
          loc.x -= diff_x / Math.abs(diff_x);
        if (diff_y != 0)
          loc.y -= diff_y / Math.abs(diff_y);
      }
      _member.teleToLocation(loc);
    }
  }

  public static void RandomTeleportParty(List<Player> members, Territory territory)
  {
    for (Player member : members)
      member.teleToLocation(Territory.getRandomLoc(territory, member.getGeoIndex()));
  }

  private void startUpdatePositionTask()
  {
    if (positionTask == null)
      positionTask = LazyPrecisionTaskManager.getInstance().scheduleAtFixedRate(new UpdatePositionTask(null), 1000L, 1000L);
  }

  private void stopUpdatePositionTask()
  {
    if (positionTask != null)
      positionTask.cancel(false);
  }

  public void requestLootChange(byte type)
  {
    if (_requestChangeLoot != -1)
    {
      if (System.currentTimeMillis() > _requestChangeLootTimer)
        finishLootRequest(false);
      else
        return;
    }
    _requestChangeLoot = type;
    int additionalTime = 45000;
    _requestChangeLootTimer = (System.currentTimeMillis() + additionalTime);
    _changeLootAnswers = new CopyOnWriteArraySet();
    _checkTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new ChangeLootCheck(null), additionalTime + 1000, 5000L);
    broadcastToPartyMembers(getPartyLeader(), new ExAskModifyPartyLooting(getPartyLeader().getName(), type));
    SystemMessage sm = new SystemMessage(3135);
    sm.addSystemString(LOOT_SYSSTRINGS[type]);
    getPartyLeader().sendPacket(sm);
  }

  public synchronized void answerLootChangeRequest(Player member, boolean answer)
  {
    if (_requestChangeLoot == -1)
      return;
    if (_changeLootAnswers.contains(Integer.valueOf(member.getObjectId())))
      return;
    if (!answer)
    {
      finishLootRequest(false);
      return;
    }
    _changeLootAnswers.add(Integer.valueOf(member.getObjectId()));
    if (_changeLootAnswers.size() >= getMemberCount() - 1)
    {
      finishLootRequest(true);
    }
  }

  private synchronized void finishLootRequest(boolean success)
  {
    if (_requestChangeLoot == -1)
      return;
    if (_checkTask != null)
    {
      _checkTask.cancel(false);
      _checkTask = null;
    }
    if (success)
    {
      broadCast(new IStaticPacket[] { new ExSetPartyLooting(1, _requestChangeLoot) });
      _itemDistribution = _requestChangeLoot;
      SystemMessage sm = new SystemMessage(3138);
      sm.addSystemString(LOOT_SYSSTRINGS[_requestChangeLoot]);
      broadCast(new IStaticPacket[] { sm });
    }
    else
    {
      broadCast(new IStaticPacket[] { new ExSetPartyLooting(0, 0) });
      broadCast(new IStaticPacket[] { new SystemMessage(3137) });
    }
    _changeLootAnswers = null;
    _requestChangeLoot = -1;
    _requestChangeLootTimer = 0L;
  }

  public Iterator<Player> iterator()
  {
    return _members.iterator();
  }

  private class ChangeLootCheck extends RunnableImpl
  {
    private ChangeLootCheck()
    {
    }

    public void runImpl()
      throws Exception
    {
      if (System.currentTimeMillis() > _requestChangeLootTimer)
      {
        Party.this.finishLootRequest(false);
      }
    }
  }

  private class UpdatePositionTask extends RunnableImpl
  {
    private UpdatePositionTask()
    {
    }

    public void runImpl()
      throws Exception
    {
      LazyArrayList update = LazyArrayList.newInstance();

      for (Player member : _members)
      {
        Location loc = member.getLastPartyPosition();
        if ((loc == null) || (member.getDistance(loc) > 256.0D))
        {
          member.setLastPartyPosition(member.getLoc());
          update.add(member);
        }
      }

      if (!update.isEmpty()) {
        for (Player member : _members)
        {
          PartyMemberPosition pmp = new PartyMemberPosition();
          for (Player m : update)
            if (m != member)
              pmp.add(m);
          if (pmp.size() > 0)
            member.sendPacket(pmp);
        }
      }
      LazyArrayList.recycle(update);
    }
  }
}
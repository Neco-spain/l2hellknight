package net.sf.l2j.gameserver.model;

import java.util.concurrent.ScheduledFuture;
import javolution.util.FastMap;
import javolution.util.FastTable;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.SevenSignsFestival;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SummonInstance;
import net.sf.l2j.gameserver.model.entity.DimensionalRift;
import net.sf.l2j.gameserver.model.entity.Duel;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.PartyMemberPosition;
import net.sf.l2j.gameserver.network.serverpackets.PartySmallWindowAdd;
import net.sf.l2j.gameserver.network.serverpackets.PartySmallWindowAll;
import net.sf.l2j.gameserver.network.serverpackets.PartySmallWindowDelete;
import net.sf.l2j.gameserver.network.serverpackets.PartySmallWindowDeleteAll;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.util.Location;
import net.sf.l2j.util.Log;
import net.sf.l2j.util.Rnd;

public class L2Party
{
  private static final double[] BONUS_EXP_SP = { 1.0D, 1.3D, 1.39D, 1.5D, 1.54D, 1.58D, 1.63D, 1.67D, 1.71D };

  private FastTable<L2PcInstance> _members = new FastTable();
  private int _pendingInvitation = 0;
  private int _partyLvl = 0;
  private int _itemDistribution = 0;
  private int _itemLastLoot = 0;
  private L2CommandChannel _commandChannel = null;
  private DimensionalRift _dr;
  public static final int ITEM_LOOTER = 0;
  public static final int ITEM_RANDOM = 1;
  public static final int ITEM_RANDOM_SPOIL = 2;
  public static final int ITEM_ORDER = 3;
  public static final int ITEM_ORDER_SPOIL = 4;
  private ScheduledFuture<?> positionsUpdate = null;
  private FastMap<Integer, Location> positions = new FastMap();

  public L2Party(L2PcInstance leader, int itemDistribution)
  {
    _itemDistribution = itemDistribution;
    _members.add(leader);
    _partyLvl = leader.getLevel();
    positionsUpdate = ThreadPoolManager.getInstance().scheduleGeneral(new PositionsUpdate(), 5000L);
  }

  public int getMemberCount()
  {
    return getPartyMembers().size();
  }

  public int getPendingInvitationNumber()
  {
    return _pendingInvitation;
  }

  public void decreasePendingInvitationNumber()
  {
    _pendingInvitation -= 1;
  }

  public void increasePendingInvitationNumber()
  {
    _pendingInvitation += 1;
  }

  public FastTable<L2PcInstance> getPartyMembers()
  {
    return _members;
  }

  private L2PcInstance getCheckedRandomMember(int ItemId, L2Character target)
  {
    FastTable availableMembers = new FastTable();
    for (L2PcInstance member : getPartyMembers()) {
      if ((member.getInventory().validateCapacityByItemId(ItemId)) && (Util.checkIfInRange(Config.ALT_PARTY_RANGE2, target, member, true)))
      {
        availableMembers.add(member);
      }
    }
    if (availableMembers.size() > 0) {
      return (L2PcInstance)availableMembers.get(Rnd.get(availableMembers.size()));
    }
    return null;
  }

  private L2PcInstance getCheckedNextLooter(int ItemId, L2Character target)
  {
    for (int i = 0; i < getMemberCount(); i++) {
      _itemLastLoot += 1;
      if (_itemLastLoot >= getMemberCount()) {
        _itemLastLoot = 0;
      }
      try
      {
        L2PcInstance member = (L2PcInstance)getPartyMembers().get(_itemLastLoot);
        if ((member.getInventory().validateCapacityByItemId(ItemId)) && (Util.checkIfInRange(Config.ALT_PARTY_RANGE2, target, member, true)))
        {
          return member;
        }
      }
      catch (Exception e)
      {
      }
    }
    return null;
  }

  private L2PcInstance getActualLooter(L2PcInstance player, int ItemId, boolean spoil, L2Character target)
  {
    L2PcInstance looter = player;

    switch (_itemDistribution) {
    case 1:
      if (spoil) break;
      looter = getCheckedRandomMember(ItemId, target); break;
    case 2:
      looter = getCheckedRandomMember(ItemId, target);
      break;
    case 3:
      if (spoil) break;
      looter = getCheckedNextLooter(ItemId, target); break;
    case 4:
      looter = getCheckedNextLooter(ItemId, target);
    }

    if (looter == null) {
      looter = player;
    }
    return looter;
  }

  public boolean isLeader(L2PcInstance player)
  {
    return getLeader().equals(player);
  }

  public int getPartyLeaderOID()
  {
    return getLeader().getObjectId();
  }

  public void broadcastToPartyMembers(L2GameServerPacket msg)
  {
    for (L2PcInstance member : getPartyMembers()) {
      if (member == null)
      {
        continue;
      }
      member.sendPacket(msg);
    }
  }

  public void broadcastHtmlToPartyMembers(String text) {
    NpcHtmlMessage html = NpcHtmlMessage.id(0);
    html.setHtml(new StringBuilder().append("<html><body>").append(text).append("<br></body></html>").toString());
    broadcastToPartyMembers(html);
  }

  public void broadcastToPartyMembers(L2PcInstance player, L2GameServerPacket msg)
  {
    for (L2PcInstance member : getPartyMembers())
      if ((member != null) && (!member.equals(player)))
        member.sendPacket(msg);
  }

  public void addPartyMember(L2PcInstance player)
  {
    player.sendPacket(new PartySmallWindowAll(_members));
    player.sendPacket(SystemMessage.id(SystemMessageId.YOU_JOINED_S1_PARTY).addString(getLeader().getName()));
    broadcastToPartyMembers(SystemMessage.id(SystemMessageId.S1_JOINED_PARTY).addString(player.getName()));
    broadcastToPartyMembers(new PartySmallWindowAdd(player));

    synchronized (_members) {
      _members.add(player);
    }

    recalculatePartyLevel();

    for (L2PcInstance member : getPartyMembers()) {
      member.updateEffectIcons(true);
    }
    if (isInDimensionalRift())
      _dr.partyMemberInvited();
  }

  public void removePartyMember(L2PcInstance player)
  {
    removePartyMember(player, true);
  }

  public void removePartyMember(L2PcInstance player, boolean ingame) {
    if ((player == null) || (!_members.contains(player))) {
      return;
    }

    boolean leader = isLeader(player);

    synchronized (_members) {
      _members.remove(player);
    }

    recalculatePartyLevel();

    if (player.isFestivalParticipant()) {
      SevenSignsFestival.getInstance().updateParticipants(player, this);
    }

    player.sendPacket(Static.YOU_LEFT_PARTY);
    player.sendPacket(Static.PartySmallWindowDeleteAll);
    player.setParty(null);

    SystemMessage msg = null;
    if (ingame)
      msg = SystemMessage.id(SystemMessageId.S1_LEFT_PARTY).addString(player.getName());
    else {
      msg = SystemMessage.id(SystemMessageId.S1_S2).addString(new StringBuilder().append(player.getName()).append(" \u0432\u044B\u0448\u0435\u043B \u0438\u0437 \u0438\u0433\u0440\u044B.").toString());
    }

    broadcastToPartyMembers(msg);
    broadcastToPartyMembers(new PartySmallWindowDelete(player));
    Object del_pkt;
    if (leader) {
      if (ingame) {
        msg = null;
        destroyParty();
        broadcastToPartyMembers(Static.PARTY_DISPERSED);
        return;
      }if (_members.size() > 1)
      {
        del_pkt = new PartySmallWindowDeleteAll();
        msg = SystemMessage.id(SystemMessageId.S1_HAS_BECOME_A_PARTY_LEADER).addString(((L2PcInstance)_members.get(0)).getName());
        for (L2PcInstance member : _members) {
          member.sendPacket(msg);
          member.sendPacket((L2GameServerPacket)del_pkt);
          member.sendPacket(new PartySmallWindowAll(_members));
        }
      }
    }

    if (_members.size() == 1) {
      L2PcInstance lastMember = (L2PcInstance)_members.get(0);

      if (lastMember.getDuel() != null) {
        lastMember.getDuel().onRemoveFromParty(lastMember);
      }

      lastMember.setParty(null);
      lastMember.sendPacket(Static.PARTY_DISPERSED);
    }

    if (isInDimensionalRift()) {
      _dr.partyMemberExited(player);
    }

    if (player.getDuel() != null) {
      player.getDuel().onRemoveFromParty(player);
    }

    msg = null;
  }

  public void changePartyLeader(String name)
  {
    L2PcInstance new_leader = getPlayerByName(name);
    L2PcInstance current_leader = (L2PcInstance)_members.get(0);

    if ((new_leader == null) || (current_leader == null)) {
      return;
    }

    if (current_leader.equals(new_leader)) {
      current_leader.sendPacket(Static.YOU_CANNOT_TRANSFER_RIGHTS_TO_YOURSELF);
      return;
    }

    synchronized (_members) {
      if (!_members.contains(new_leader)) {
        current_leader.sendPacket(Static.YOU_CAN_TRANSFER_RIGHTS_ONLY_TO_ANOTHER_PARTY_MEMBER);
        return;
      }

      int idx = _members.indexOf(new_leader);
      _members.set(0, new_leader);
      _members.set(idx, current_leader);

      L2GameServerPacket del_pkt = new PartySmallWindowDeleteAll();
      SystemMessage msg = SystemMessage.id(SystemMessageId.S1_HAS_BECOME_A_PARTY_LEADER).addString(name);
      for (L2PcInstance member : _members) {
        member.sendPacket(del_pkt);
        member.sendPacket(new PartySmallWindowAll(_members));
        member.sendPacket(msg);
      }
      msg = null;
    }

    if (isInCommandChannel())
      _commandChannel.setChannelLeader(new_leader);
  }

  private L2PcInstance getPlayerByName(String name)
  {
    for (L2PcInstance member : getPartyMembers()) {
      if (member.getName().equals(name)) {
        return member;
      }
    }
    return null;
  }

  public void oustPartyMember(L2PcInstance player)
  {
    if (!_members.contains(player)) {
      return;
    }

    if (isLeader(player)) {
      broadcastToPartyMembers(Static.PARTY_DISPERSED);
      destroyParty();
    }
    else
    {
      removePartyMember(player);
    }
  }

  public void oustPartyMember(String name)
  {
    oustPartyMember(getPlayerByName(name));
  }

  private void destroyParty()
  {
    if (getLeader() != null) {
      getLeader().setParty(null);
      if (getLeader().getDuel() != null) {
        getLeader().getDuel().onRemoveFromParty(getLeader());
      }
    }

    synchronized (_members) {
      for (L2PcInstance temp : _members) {
        temp.sendPacket(new PartySmallWindowDeleteAll());
        temp.setParty(null);
      }
      _members.clear();
    }

    if (positionsUpdate != null) {
      positionsUpdate.cancel(true);
      positionsUpdate = null;
    }
    positions.clear();
  }

  public void distributeItem(L2PcInstance player, L2ItemInstance item)
  {
    if (item.getItemId() == 57) {
      distributeAdena(player, item.getCount(), player);
      ItemTable.getInstance().destroyItem("Party", item, player, null);
      return;
    }

    L2PcInstance target = getActualLooter(player, item.getItemId(), false, player);
    target.addItem("Party", item, player, true);

    SystemMessage msg = null;
    if (item.getCount() > 1)
      msg = SystemMessage.id(SystemMessageId.S1_PICKED_UP_S2_S3).addString(target.getName()).addItemName(item.getItemId()).addNumber(item.getCount());
    else {
      msg = SystemMessage.id(SystemMessageId.S1_PICKED_UP_S2).addString(target.getName()).addItemName(item.getItemId());
    }

    broadcastToPartyMembers(target, msg);
    msg = null;

    player.sendChanges();
    if (Config.LOG_ITEMS) {
      String act = new StringBuilder().append(Log.getTime()).append("PICKUP ").append(item.getItemName()).append("(").append(item.getCount()).append(")(").append(item.getEnchantLevel()).append(")(").append(item.getObjectId()).append(") #(player ").append(player.getName()).append(", account: ").append(player.getAccountName()).append(", ip: ").append(player.getIP()).append(", hwid: ").append(player.getHWID()).append(")").append("\n").toString();
      Log.item(act, 6);
    }
  }

  public void distributeItem(L2PcInstance player, L2Attackable.RewardItem item, boolean spoil, L2Attackable target)
  {
    if (item == null) {
      return;
    }

    if (item.getItemId() == 57) {
      distributeAdena(player, item.getCount(), target);
      return;
    }

    L2PcInstance looter = getActualLooter(player, item.getItemId(), spoil, target);

    looter.addItem(spoil ? "Sweep" : "Party", item.getItemId(), item.getCount(), player, true);

    SystemMessage msg = null;
    if (item.getCount() > 1) {
      msg = spoil ? SystemMessage.id(SystemMessageId.S1_SWEEPED_UP_S2_S3) : SystemMessage.id(SystemMessageId.S1_PICKED_UP_S2_S3);
      msg.addString(looter.getName()).addItemName(item.getItemId()).addNumber(item.getCount());
    } else {
      msg = spoil ? SystemMessage.id(SystemMessageId.S1_SWEEPED_UP_S2) : SystemMessage.id(SystemMessageId.S1_PICKED_UP_S2);
      msg.addString(looter.getName()).addItemName(item.getItemId());
    }
    broadcastToPartyMembers(looter, msg);

    if (Config.LOG_ITEMS) {
      String act = new StringBuilder().append(Log.getTime()).append("PICKUP(").append(spoil ? "Sweep" : "Party").append(") itemId: ").append(item.getItemId()).append("(").append(item.getCount()).append(") #(player ").append(player.getName()).append(", account: ").append(player.getAccountName()).append(", ip: ").append(player.getIP()).append(", hwid: ").append(player.getHWID()).append(")").append("\n").toString();
      Log.item(act, 6);
    }
  }

  public void distributeAdena(L2PcInstance player, int adena, L2Character target)
  {
    FastTable membersList = getPartyMembers();

    FastTable ToReward = new FastTable();
    for (L2PcInstance member : membersList) {
      if (!Util.checkIfInRange(Config.ALT_PARTY_RANGE2, target, member, true)) {
        continue;
      }
      ToReward.add(member);
    }

    if ((ToReward == null) || (ToReward.isEmpty())) {
      return;
    }

    int count = adena / ToReward.size();
    for (L2PcInstance member : ToReward) {
      member.addAdena("Party", count, player, true);
    }

    ToReward.clear();
    ToReward = null;
  }

  public void distributeXpAndSp(long xpReward, int spReward, FastTable<L2PlayableInstance> rewardedMembers, int topLvl)
  {
    L2SummonInstance summon = null;
    FastTable validMembers = getValidMembers(rewardedMembers, topLvl);

    xpReward = ()(xpReward * getExpBonus(validMembers.size()));
    spReward = (int)(spReward * getSpBonus(validMembers.size()));

    double sqLevelSum = 0.0D;
    for (L2PlayableInstance character : validMembers) {
      sqLevelSum += character.getLevel() * character.getLevel();
    }

    synchronized (rewardedMembers) {
      for (L2Character member : rewardedMembers) {
        if (member.isDead())
        {
          continue;
        }
        float penalty = 0.0F;

        if ((member.getPet() != null) && (member.getPet().isSummon())) {
          summon = (L2SummonInstance)member.getPet();
          penalty = summon.getExpPenalty();
        }

        if (member.isPet()) {
          if (((L2PetInstance)member).getPetData().getOwnerExpTaken() > 0.0F)
          {
            continue;
          }
          penalty = 0.85F;
        }

        if (validMembers.contains(member)) {
          double sqLevel = member.getLevel() * member.getLevel();
          double preCalculation = sqLevel / sqLevelSum * (1.0F - penalty);

          if (!member.isDead())
            member.addExpAndSp(Math.round(member.calcStat(Stats.EXPSP_RATE, xpReward * preCalculation, null, null)), (int)member.calcStat(Stats.EXPSP_RATE, spReward * preCalculation, null, null));
        }
        else
        {
          member.addExpAndSp(0L, 0);
        }
      }
    }
  }

  public void recalculatePartyLevel()
  {
    int newLevel = 0;
    for (L2PcInstance member : getPartyMembers()) {
      if (member == null)
      {
        continue;
      }
      if (member.getLevel() > newLevel) {
        newLevel = member.getLevel();
      }
    }
    _partyLvl = newLevel;
  }

  public void teleTo(int x, int y, int z)
  {
    for (L2PcInstance member : getPartyMembers()) {
      if (member == null)
      {
        continue;
      }
      member.teleToLocation(x, y, z);
    }
  }

  private FastTable<L2PlayableInstance> getValidMembers(FastTable<L2PlayableInstance> members, int topLvl) {
    FastTable validMembers = new FastTable();
    int sqLevelSum;
    int i;
    if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("level")) {
      for (L2PlayableInstance member : members)
        if (topLvl - member.getLevel() <= Config.PARTY_XP_CUTOFF_LEVEL)
          validMembers.add(member);
    }
    else
    {
      int sqLevelSum;
      if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("percentage")) {
        sqLevelSum = 0;
        for (L2PlayableInstance member : members) {
          sqLevelSum += member.getLevel() * member.getLevel();
        }

        for (L2PlayableInstance member : members) {
          int sqLevel = member.getLevel() * member.getLevel();
          if (sqLevel * 100 >= sqLevelSum * Config.PARTY_XP_CUTOFF_PERCENT) {
            validMembers.add(member);
          }
        }
      }
      else if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("auto")) {
        sqLevelSum = 0;
        for (L2PlayableInstance member : members) {
          sqLevelSum += member.getLevel() * member.getLevel();
        }

        i = members.size() - 1;
        if (i < 1) {
          return members;
        }
        if (i >= BONUS_EXP_SP.length) {
          i = BONUS_EXP_SP.length - 1;
        }

        for (L2PlayableInstance member : members) {
          int sqLevel = member.getLevel() * member.getLevel();
          if (sqLevel >= sqLevelSum * (1.0D - 1.0D / (1.0D + BONUS_EXP_SP[i] - BONUS_EXP_SP[(i - 1)])))
            validMembers.add(member);
        }
      }
    }
    return validMembers;
  }

  private double getBaseExpSpBonus(int membersCount) {
    int i = membersCount - 1;
    if (i < 1) {
      return 1.0D;
    }
    if (i >= BONUS_EXP_SP.length) {
      i = BONUS_EXP_SP.length - 1;
    }

    return BONUS_EXP_SP[i];
  }

  private double getExpBonus(int membersCount) {
    if (membersCount < 2)
    {
      return getBaseExpSpBonus(membersCount);
    }
    return getBaseExpSpBonus(membersCount) * Config.RATE_PARTY_XP;
  }

  private double getSpBonus(int membersCount)
  {
    if (membersCount < 2)
    {
      return getBaseExpSpBonus(membersCount);
    }
    return getBaseExpSpBonus(membersCount) * Config.RATE_PARTY_SP;
  }

  public int getLevel()
  {
    return _partyLvl;
  }

  public int getLootDistribution() {
    return _itemDistribution;
  }

  public boolean isInCommandChannel() {
    return _commandChannel != null;
  }

  public L2CommandChannel getCommandChannel() {
    return _commandChannel;
  }

  public void setCommandChannel(L2CommandChannel channel) {
    _commandChannel = channel;
  }

  public boolean isInDimensionalRift() {
    return _dr != null;
  }

  public void setDimensionalRift(DimensionalRift dr) {
    _dr = dr;
  }

  public DimensionalRift getDimensionalRift() {
    return _dr;
  }

  public L2PcInstance getLeader() {
    return (L2PcInstance)getPartyMembers().get(0);
  }

  public void updateMembers() {
    L2GameServerPacket del_pkt = new PartySmallWindowDeleteAll();
    for (L2PcInstance member : _members) {
      if (member == null) {
        continue;
      }
      member.sendPacket(del_pkt);
      member.sendPacket(new PartySmallWindowAll(_members));
    }
    del_pkt = null;
  }

  class PositionsUpdate
    implements Runnable
  {
    public PositionsUpdate()
    {
    }

    public void run()
    {
      if ((_members == null) || (_members.size() < 2)) {
        L2Party.this.destroyParty();
        return;
      }

      positions.clear();
      for (L2PcInstance member : getPartyMembers()) {
        if (member == null)
        {
          continue;
        }
        positions.put(Integer.valueOf(member.getObjectId()), new Location(member.getX(), member.getY(), member.getZ()));
      }

      for (L2PcInstance member : getPartyMembers()) {
        if (member == null)
        {
          continue;
        }
        member.sendPacket(new PartyMemberPosition(positions));
      }

      L2Party.access$302(L2Party.this, ThreadPoolManager.getInstance().scheduleGeneral(new PositionsUpdate(L2Party.this), 5000L));
    }
  }
}
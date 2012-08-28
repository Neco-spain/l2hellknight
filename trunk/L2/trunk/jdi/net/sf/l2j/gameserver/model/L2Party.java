package net.sf.l2j.gameserver.model;

import java.util.List;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.SevenSignsFestival;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.instancemanager.DuelManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SummonInstance;
import net.sf.l2j.gameserver.model.entity.DimensionalRift;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.network.serverpackets.PartySmallWindowAdd;
import net.sf.l2j.gameserver.network.serverpackets.PartySmallWindowAll;
import net.sf.l2j.gameserver.network.serverpackets.PartySmallWindowDelete;
import net.sf.l2j.gameserver.network.serverpackets.PartySmallWindowDeleteAll;
import net.sf.l2j.gameserver.network.serverpackets.PartySmallWindowUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.util.Rnd;

public class L2Party
{
  private static final double[] BONUS_EXP_SP = { 1.0D, 1.3D, 1.39D, 1.5D, 1.54D, 1.58D, 1.63D, 1.67D, 1.71D };

  private List<L2PcInstance> _members = null;
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

  public L2Party(L2PcInstance leader, int itemDistribution)
  {
    _itemDistribution = itemDistribution;
    getPartyMembers().add(leader);
    _partyLvl = leader.getLevel();
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

  public List<L2PcInstance> getPartyMembers()
  {
    if (_members == null) _members = new FastList();
    return _members;
  }

  private L2PcInstance getCheckedRandomMember(int ItemId, L2Character target)
  {
    List availableMembers = new FastList();
    for (L2PcInstance member : getPartyMembers())
    {
      if ((member.getInventory().validateCapacityByItemId(ItemId)) && (Util.checkIfInRange(Config.ALT_PARTY_RANGE2, target, member, true)))
        availableMembers.add(member);
    }
    if (availableMembers.size() > 0) return (L2PcInstance)availableMembers.get(Rnd.get(availableMembers.size()));
    return null;
  }

  private L2PcInstance getCheckedNextLooter(int ItemId, L2Character target)
  {
    for (int i = 0; i < getMemberCount(); i++)
    {
      _itemLastLoot += 1;
      if (_itemLastLoot >= getMemberCount()) _itemLastLoot = 0;

      try
      {
        L2PcInstance member = (L2PcInstance)getPartyMembers().get(_itemLastLoot);
        if ((member.getInventory().validateCapacityByItemId(ItemId)) && (Util.checkIfInRange(Config.ALT_PARTY_RANGE2, target, member, true))) {
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

    switch (_itemDistribution)
    {
    case 1:
      if (spoil) break; looter = getCheckedRandomMember(ItemId, target); break;
    case 2:
      looter = getCheckedRandomMember(ItemId, target);
      break;
    case 3:
      if (spoil) break; looter = getCheckedNextLooter(ItemId, target); break;
    case 4:
      looter = getCheckedNextLooter(ItemId, target);
    }

    if (looter == null) looter = player;
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
    for (L2PcInstance member : getPartyMembers())
    {
      member.sendPacket(msg);
    }
  }

  public void broadcastToPartyMembers(L2PcInstance player, L2GameServerPacket msg)
  {
    for (L2PcInstance member : getPartyMembers())
    {
      if ((member != null) && (!member.equals(player))) member.sendPacket(msg);
    }
  }

  public void addPartyMember(L2PcInstance player)
  {
    PartySmallWindowAll window = new PartySmallWindowAll();
    window.setPartyList(getPartyMembers());
    player.sendPacket(window);

    SystemMessage msg = new SystemMessage(SystemMessageId.YOU_JOINED_S1_PARTY);
    msg.addString(getLeader().getName());
    player.sendPacket(msg);

    msg = new SystemMessage(SystemMessageId.S1_JOINED_PARTY);
    msg.addString(player.getName());
    broadcastToPartyMembers(msg);
    broadcastToPartyMembers(new PartySmallWindowAdd(player));

    getPartyMembers().add(player);
    if (player.getLevel() > _partyLvl)
    {
      _partyLvl = player.getLevel();
    }

    for (L2PcInstance member : getPartyMembers()) {
      member.updateEffectIcons(true);
    }
    if (isInDimensionalRift())
      _dr.partyMemberInvited();
  }

  public void removePartyMember(L2PcInstance player)
  {
    if (getPartyMembers().contains(player))
    {
      getPartyMembers().remove(player);
      recalculatePartyLevel();

      if (player.isFestivalParticipant()) {
        SevenSignsFestival.getInstance().updateParticipants(player, this);
      }
      if (player.isInDuel()) {
        DuelManager.getInstance().onRemoveFromParty(player);
      }
      SystemMessage msg = new SystemMessage(SystemMessageId.YOU_LEFT_PARTY);
      player.sendPacket(msg);
      player.sendPacket(new PartySmallWindowDeleteAll());
      player.setParty(null);

      msg = new SystemMessage(SystemMessageId.S1_LEFT_PARTY);
      msg.addString(player.getName());
      broadcastToPartyMembers(msg);
      broadcastToPartyMembers(new PartySmallWindowDelete(player));

      if (isInDimensionalRift()) {
        _dr.partyMemberExited(player);
      }
      if (getPartyMembers().size() == 1)
      {
        getLeader().setParty(null);
        if (getLeader().isInDuel())
          DuelManager.getInstance().onRemoveFromParty(getLeader());
      }
    }
  }

  public void changePartyLeader(String name)
  {
    L2PcInstance player = getPlayerByName(name);

    if ((player != null) && (!player.isInDuel()))
    {
      if (getPartyMembers().contains(player))
      {
        if (isLeader(player))
        {
          player.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_TRANSFER_RIGHTS_TO_YOURSELF));
        }
        else
        {
          int p1 = getPartyMembers().indexOf(player);
          L2PcInstance temp = getLeader();
          getPartyMembers().set(0, getPartyMembers().get(p1));
          getPartyMembers().set(p1, temp);

          SystemMessage msg = new SystemMessage(SystemMessageId.S1_HAS_BECOME_A_PARTY_LEADER);
          msg.addString(getLeader().getName());
          broadcastToPartyMembers(msg);
          broadcastToPartyMembers(new PartySmallWindowUpdate(getLeader()));
          if (isInCommandChannel())
          {
            _commandChannel.setChannelLeader((L2PcInstance)getPartyMembers().get(0));
          }
        }
      }
      else
      {
        player.sendPacket(new SystemMessage(SystemMessageId.YOU_CAN_TRANSFER_RIGHTS_ONLY_TO_ANOTHER_PARTY_MEMBER));
      }
    }
  }

  private L2PcInstance getPlayerByName(String name)
  {
    for (L2PcInstance member : getPartyMembers())
    {
      if (member.getName().equals(name)) return member;
    }
    return null;
  }

  public void oustPartyMember(L2PcInstance player)
  {
    if (getPartyMembers().contains(player))
    {
      if (isLeader(player))
      {
        removePartyMember(player);
        if (getPartyMembers().size() > 1)
        {
          SystemMessage msg = new SystemMessage(SystemMessageId.S1_HAS_BECOME_A_PARTY_LEADER);
          msg.addString(getLeader().getName());
          broadcastToPartyMembers(msg);
          broadcastToPartyMembers(new PartySmallWindowUpdate(getLeader()));
        }
      }
      else
      {
        removePartyMember(player);
      }

      if (getPartyMembers().size() == 1)
      {
        _members = null;
      }
    }
  }

  public void oustPartyMember(String name)
  {
    L2PcInstance player = getPlayerByName(name);

    if (player != null)
    {
      if (isLeader(player))
      {
        removePartyMember(player);
        if (getPartyMembers().size() > 1)
        {
          SystemMessage msg = new SystemMessage(SystemMessageId.S1_HAS_BECOME_A_PARTY_LEADER);
          msg.addString(getLeader().getName());
          broadcastToPartyMembers(msg);
          broadcastToPartyMembers(new PartySmallWindowUpdate(getLeader()));
        }
      }
      else
      {
        removePartyMember(player);
      }

      if (getPartyMembers().size() == 1)
      {
        _members = null;
      }
    }
  }

  public void distributeItem(L2PcInstance player, L2ItemInstance item)
  {
    if (item.getItemId() == 57)
    {
      distributeAdena(player, item.getCount(), player);
      ItemTable.getInstance().destroyItem("Party", item, player, null);
      return;
    }

    L2PcInstance target = getActualLooter(player, item.getItemId(), false, player);
    target.addItem("Party", item, player, true);

    if (item.getCount() > 1)
    {
      SystemMessage msg = new SystemMessage(SystemMessageId.S1_PICKED_UP_S2_S3);
      msg.addString(target.getName());
      msg.addItemName(item.getItemId());
      msg.addNumber(item.getCount());
      broadcastToPartyMembers(target, msg);
    }
    else
    {
      SystemMessage msg = new SystemMessage(SystemMessageId.S1_PICKED_UP_S2);
      msg.addString(target.getName());
      msg.addItemName(item.getItemId());
      broadcastToPartyMembers(target, msg);
    }
  }

  public void distributeItem(L2PcInstance player, L2Attackable.RewardItem item, boolean spoil, L2Attackable target)
  {
    if (item == null) return;

    if (item.getItemId() == 57)
    {
      distributeAdena(player, item.getCount(), target);
      return;
    }

    L2PcInstance looter = getActualLooter(player, item.getItemId(), spoil, target);

    looter.addItem(spoil ? "Sweep" : "Party", item.getItemId(), item.getCount(), player, true);

    if (item.getCount() > 1)
    {
      SystemMessage msg = spoil ? new SystemMessage(SystemMessageId.S1_SWEEPED_UP_S2_S3) : new SystemMessage(SystemMessageId.S1_PICKED_UP_S2_S3);

      msg.addString(looter.getName());
      msg.addItemName(item.getItemId());
      msg.addNumber(item.getCount());
      broadcastToPartyMembers(looter, msg);
    }
    else
    {
      SystemMessage msg = spoil ? new SystemMessage(SystemMessageId.S1_SWEEPED_UP_S2) : new SystemMessage(SystemMessageId.S1_PICKED_UP_S2);

      msg.addString(looter.getName());
      msg.addItemName(item.getItemId());
      broadcastToPartyMembers(looter, msg);
    }
  }

  public void distributeAdena(L2PcInstance player, int adena, L2Character target)
  {
    List membersList = getPartyMembers();

    List ToReward = new FastList();
    for (L2PcInstance member : membersList)
    {
      if (Util.checkIfInRange(Config.ALT_PARTY_RANGE2, target, member, true)) {
        ToReward.add(member);
      }
    }

    if ((ToReward == null) || (ToReward.isEmpty())) return;

    int count = adena / ToReward.size();
    for (L2PcInstance member : ToReward)
      member.addAdena("Party", count, player, true);
  }

  public void distributeXpAndSp(long xpReward, int spReward, List<L2PlayableInstance> rewardedMembers, int topLvl)
  {
    L2SummonInstance summon = null;
    List validMembers = getValidMembers(rewardedMembers, topLvl);

    xpReward = ()(xpReward * getExpBonus(validMembers.size()));
    spReward = (int)(spReward * getSpBonus(validMembers.size()));

    double sqLevelSum = 0.0D;
    for (L2PlayableInstance character : validMembers) {
      sqLevelSum += character.getLevel() * character.getLevel();
    }

    synchronized (rewardedMembers)
    {
      for (L2Character member : rewardedMembers)
      {
        if (member.isDead())
          continue;
        float penalty = 0.0F;

        if ((member.getPet() instanceof L2SummonInstance))
        {
          summon = (L2SummonInstance)member.getPet();
          penalty = summon.getExpPenalty();
        }

        if ((member instanceof L2PetInstance))
        {
          if (((L2PetInstance)member).getPetData().getOwnerExpTaken() > 0.0F) {
            continue;
          }
          penalty = 0.85F;
        }

        if (validMembers.contains(member))
        {
          double sqLevel = member.getLevel() * member.getLevel();
          double preCalculation = sqLevel / sqLevelSum * (1.0F - penalty);

          if (!member.isDead()) {
            member.addExpAndSp(Math.round(member.calcStat(Stats.EXPSP_RATE, xpReward * preCalculation, null, null)), (int)member.calcStat(Stats.EXPSP_RATE, spReward * preCalculation, null, null));
          }
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
    for (L2PcInstance member : getPartyMembers())
    {
      if (member.getLevel() > newLevel)
        newLevel = member.getLevel();
    }
    _partyLvl = newLevel;
  }

  private List<L2PlayableInstance> getValidMembers(List<L2PlayableInstance> members, int topLvl)
  {
    List validMembers = new FastList();
    int sqLevelSum;
    int i;
    if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("level"))
    {
      for (L2PlayableInstance member : members)
      {
        if (topLvl - member.getLevel() <= Config.PARTY_XP_CUTOFF_LEVEL)
          validMembers.add(member);
      }
    }
    else
    {
      int sqLevelSum;
      if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("percentage"))
      {
        sqLevelSum = 0;
        for (L2PlayableInstance member : members)
        {
          sqLevelSum += member.getLevel() * member.getLevel();
        }

        for (L2PlayableInstance member : members)
        {
          int sqLevel = member.getLevel() * member.getLevel();
          if (sqLevel * 100 >= sqLevelSum * Config.PARTY_XP_CUTOFF_PERCENT) {
            validMembers.add(member);
          }
        }
      }
      else if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("auto"))
      {
        sqLevelSum = 0;
        for (L2PlayableInstance member : members)
        {
          sqLevelSum += member.getLevel() * member.getLevel();
        }

        i = members.size() - 1;
        if (i < 1) return members;
        if (i >= BONUS_EXP_SP.length) i = BONUS_EXP_SP.length - 1;

        for (L2PlayableInstance member : members)
        {
          int sqLevel = member.getLevel() * member.getLevel();
          if (sqLevel >= sqLevelSum * (1.0D - 1.0D / (1.0D + BONUS_EXP_SP[i] - BONUS_EXP_SP[(i - 1)])))
            validMembers.add(member); 
        }
      }
    }
    return validMembers;
  }

  private double getBaseExpSpBonus(int membersCount)
  {
    int i = membersCount - 1;
    if (i < 1) return 1.0D;
    if (i >= BONUS_EXP_SP.length) i = BONUS_EXP_SP.length - 1;

    return BONUS_EXP_SP[i];
  }

  private double getExpBonus(int membersCount)
  {
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

  public int getLevel() {
    return _partyLvl;
  }
  public int getLootDistribution() { return _itemDistribution; }

  public boolean isInCommandChannel()
  {
    return _commandChannel != null;
  }

  public L2CommandChannel getCommandChannel()
  {
    return _commandChannel;
  }

  public void setCommandChannel(L2CommandChannel channel)
  {
    _commandChannel = channel;
  }
  public boolean isInDimensionalRift() {
    return _dr != null;
  }
  public void setDimensionalRift(DimensionalRift dr) { _dr = dr; } 
  public DimensionalRift getDimensionalRift() {
    return _dr;
  }
  public L2PcInstance getLeader() { return (L2PcInstance)getPartyMembers().get(0);
  }
}
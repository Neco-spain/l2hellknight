package l2p.gameserver.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import l2p.commons.collections.JoinedIterator;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.entity.Reflection;
import l2p.gameserver.model.instances.NpcFriendInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.model.matching.MatchingRoom;
import l2p.gameserver.serverpackets.ExMPCCClose;
import l2p.gameserver.serverpackets.ExMPCCOpen;
import l2p.gameserver.serverpackets.ExMPCCPartyInfoUpdate;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.serverpackets.components.IStaticPacket;

public class CommandChannel
  implements PlayerGroup
{
  public static final int STRATEGY_GUIDE_ID = 8871;
  public static final int CLAN_IMPERIUM_ID = 391;
  private final List<Party> _commandChannelParties = new CopyOnWriteArrayList();
  private Player _commandChannelLeader;
  private int _commandChannelLvl;
  private Reflection _reflection;
  private MatchingRoom _matchingRoom;

  public CommandChannel(Player leader)
  {
    _commandChannelLeader = leader;
    _commandChannelParties.add(leader.getParty());
    _commandChannelLvl = leader.getParty().getLevel();
    leader.getParty().setCommandChannel(this);
    broadCast(new IStaticPacket[] { ExMPCCOpen.STATIC });
  }

  public void addParty(Party party)
  {
    broadCast(new IStaticPacket[] { new ExMPCCPartyInfoUpdate(party, 1) });
    _commandChannelParties.add(party);
    refreshLevel();
    party.setCommandChannel(this);

    for (Player $member : party)
    {
      $member.sendPacket(ExMPCCOpen.STATIC);
      if (_matchingRoom != null)
        _matchingRoom.broadcastPlayerUpdate($member);
    }
  }

  public void removeParty(Party party)
  {
    _commandChannelParties.remove(party);
    refreshLevel();
    party.setCommandChannel(null);
    party.broadCast(new IStaticPacket[] { ExMPCCClose.STATIC });
    Reflection reflection = getReflection();
    if (reflection != null) {
      for (Player player : party.getPartyMembers())
        player.teleToLocation(reflection.getReturnLoc(), 0);
    }
    if (_commandChannelParties.size() < 2) {
      disbandChannel();
    }
    else
      for (Player $member : party)
      {
        $member.sendPacket(new ExMPCCPartyInfoUpdate(party, 0));
        if (_matchingRoom != null)
          _matchingRoom.broadcastPlayerUpdate($member);
      }
  }

  public void disbandChannel()
  {
    broadCast(new IStaticPacket[] { Msg.THE_COMMAND_CHANNEL_HAS_BEEN_DISBANDED });
    for (Party party : _commandChannelParties)
    {
      party.setCommandChannel(null);
      party.broadCast(new IStaticPacket[] { ExMPCCClose.STATIC });
      if (isInReflection())
        party.broadCast(new IStaticPacket[] { new SystemMessage(2106).addNumber(1) });
    }
    Reflection reflection = getReflection();
    if (reflection != null)
    {
      reflection.startCollapseTimer(60000L);
      setReflection(null);
    }

    if (_matchingRoom != null)
      _matchingRoom.disband();
    _commandChannelParties.clear();
    _commandChannelLeader = null;
  }

  public int getMemberCount()
  {
    int count = 0;
    for (Party party : _commandChannelParties)
      count += party.getMemberCount();
    return count;
  }

  public void broadCast(IStaticPacket[] gsp)
  {
    for (Party party : _commandChannelParties)
      party.broadCast(gsp);
  }

  public void broadcastToChannelPartyLeaders(L2GameServerPacket gsp)
  {
    for (Party party : _commandChannelParties)
    {
      Player leader = party.getPartyLeader();
      if (leader != null)
        leader.sendPacket(gsp);
    }
  }

  public List<Party> getParties()
  {
    return _commandChannelParties;
  }

  public List<Player> getMembers()
  {
    List members = new ArrayList(_commandChannelParties.size());
    for (Party party : getParties())
      members.addAll(party.getPartyMembers());
    return members;
  }

  public Iterator<Player> iterator()
  {
    List iterators = new ArrayList(_commandChannelParties.size());
    for (Party p : getParties())
      iterators.add(p.getPartyMembers().iterator());
    return new JoinedIterator(iterators);
  }

  public int getLevel()
  {
    return _commandChannelLvl;
  }

  public void setChannelLeader(Player newLeader)
  {
    _commandChannelLeader = newLeader;
    broadCast(new IStaticPacket[] { new SystemMessage(1589).addString(newLeader.getName()) });
  }

  public Player getChannelLeader()
  {
    return _commandChannelLeader;
  }

  public boolean meetRaidWarCondition(NpcFriendInstance npc)
  {
    if (!npc.isRaid())
      return false;
    int npcId = npc.getNpcId();
    switch (npcId)
    {
    case 29001:
    case 29006:
    case 29014:
    case 29022:
      return getMemberCount() > 36;
    case 29020:
      return getMemberCount() > 56;
    case 29019:
      return getMemberCount() > 225;
    case 29028:
      return getMemberCount() > 99;
    }
    return getMemberCount() > 18;
  }

  private void refreshLevel()
  {
    _commandChannelLvl = 0;
    for (Party pty : _commandChannelParties)
      if (pty.getLevel() > _commandChannelLvl)
        _commandChannelLvl = pty.getLevel();
  }

  public boolean isInReflection()
  {
    return _reflection != null;
  }

  public void setReflection(Reflection reflection)
  {
    _reflection = reflection;
  }

  public Reflection getReflection()
  {
    return _reflection;
  }

  public static boolean checkAuthority(Player creator)
  {
    if ((creator.getClan() == null) || (!creator.isInParty()) || (!creator.getParty().isLeader(creator)) || (creator.getPledgeClass() < 5))
    {
      creator.sendPacket(Msg.YOU_DO_NOT_HAVE_AUTHORITY_TO_USE_THE_COMMAND_CHANNEL);
      return false;
    }

    boolean haveSkill = creator.getSkillLevel(Integer.valueOf(391)) > 0;

    boolean haveItem = creator.getInventory().getItemByItemId(8871) != null;

    if ((!haveSkill) && (!haveItem))
    {
      creator.sendPacket(Msg.YOU_DO_NOT_HAVE_AUTHORITY_TO_USE_THE_COMMAND_CHANNEL);
      return false;
    }

    return true;
  }

  public MatchingRoom getMatchingRoom()
  {
    return _matchingRoom;
  }

  public void setMatchingRoom(MatchingRoom matchingRoom)
  {
    _matchingRoom = matchingRoom;
  }
}
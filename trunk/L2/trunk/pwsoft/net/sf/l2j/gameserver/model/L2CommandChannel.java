package net.sf.l2j.gameserver.model;

import java.util.List;
import javolution.util.FastList;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.ExMultiPartyCommandChannelInfo;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;

public class L2CommandChannel
{
  private List<L2Party> _partys = null;
  private L2PcInstance _commandLeader = null;
  private int _channelLvl;

  public L2CommandChannel(L2PcInstance leader)
  {
    _commandLeader = leader;
    _partys = new FastList();
    _partys.add(leader.getParty());
    _channelLvl = leader.getParty().getLevel();
    leader.getParty().setCommandChannel(this);
    leader.getParty().broadcastToPartyMembers(Static.ExOpenMPCC);
  }

  public void addParty(L2Party party)
  {
    _partys.add(party);
    if (party.getLevel() > _channelLvl)
      _channelLvl = party.getLevel();
    party.setCommandChannel(this);
    party.broadcastToPartyMembers(Static.ExOpenMPCC);
    broadcastToChannelMembers(new ExMultiPartyCommandChannelInfo(this));
  }

  public void removeParty(L2Party party)
  {
    _partys.remove(party);
    _channelLvl = 0;
    for (L2Party pty : _partys)
    {
      if (pty.getLevel() > _channelLvl)
        _channelLvl = pty.getLevel();
    }
    party.setCommandChannel(null);
    party.broadcastToPartyMembers(Static.ExCloseMPCC);
    if (_partys.size() < 2)
    {
      broadcastToChannelMembers(Static.THE_COMMAND_CHANNEL_HAS_BEEN_DISBANDED);
      disbandChannel();
    }
  }

  public void disbandChannel()
  {
    for (L2Party party : _partys)
    {
      if (party != null)
        removeParty(party);
    }
    _partys = null;
  }

  public int getMemberCount()
  {
    int count = 0;
    for (L2Party party : _partys)
    {
      if (party != null)
        count += party.getMemberCount();
    }
    return count;
  }

  public void broadcastToChannelMembers(L2GameServerPacket gsp)
  {
    if ((_partys == null) || (_partys.isEmpty())) {
      return;
    }
    for (L2Party party : _partys)
    {
      if (party != null)
        party.broadcastToPartyMembers(gsp);
    }
  }

  public List<L2Party> getPartys()
  {
    return _partys;
  }

  public List<L2PcInstance> getMembers()
  {
    List members = new FastList();
    for (L2Party party : getPartys())
    {
      members.addAll(party.getPartyMembers());
    }
    return members;
  }

  public int getLevel()
  {
    return _channelLvl;
  }

  public void setChannelLeader(L2PcInstance leader)
  {
    _commandLeader = leader;
  }

  public L2PcInstance getChannelLeader()
  {
    return _commandLeader;
  }

  public void teleTo(int x, int y, int z)
  {
    for (L2Party party : getPartys())
    {
      if (party == null) {
        continue;
      }
      party.teleTo(x, y, z);
    }
  }
}
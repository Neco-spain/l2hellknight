package net.sf.l2j.gameserver.network.serverpackets;

import javolution.util.FastList;
import net.sf.l2j.gameserver.model.L2CommandChannel;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class ExMultiPartyCommandChannelInfo extends L2GameServerPacket
{
  private String channelLeaderName;
  private int memberCount;
  private FastList<ChannelPartyInfo> parties;

  public ExMultiPartyCommandChannelInfo(L2CommandChannel channel)
  {
    if (channel == null) {
      return;
    }

    channelLeaderName = channel.getChannelLeader().getName();
    memberCount = channel.getMemberCount();

    parties = new FastList();
    for (L2Party party : channel.getPartys()) {
      if (party == null)
      {
        continue;
      }
      parties.add(new ChannelPartyInfo(party.getLeader().getName(), party.getPartyLeaderOID(), party.getMemberCount()));
    }
  }

  protected void writeImpl()
  {
    if (parties == null) {
      return;
    }

    writeC(254);
    writeH(48);
    writeS(channelLeaderName);
    writeD(0);
    writeD(memberCount);
    writeD(parties.size());

    for (ChannelPartyInfo party : parties) {
      writeS(party.Leader_name);
      writeD(party.Leader_obj_id);
      writeD(party.MemberCount);
    }
    parties.clear();
  }
  static class ChannelPartyInfo { public String Leader_name;
    public int Leader_obj_id;
    public int MemberCount;

    public ChannelPartyInfo(String _Leader_name, int _Leader_obj_id, int _MemberCount) { Leader_name = _Leader_name;
      Leader_obj_id = _Leader_obj_id;
      MemberCount = _MemberCount;
    }
  }
}
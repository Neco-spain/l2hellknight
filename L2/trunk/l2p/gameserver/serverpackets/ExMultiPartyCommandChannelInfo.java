package l2p.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2p.gameserver.model.CommandChannel;
import l2p.gameserver.model.Party;
import l2p.gameserver.model.Player;

public class ExMultiPartyCommandChannelInfo extends L2GameServerPacket
{
  private String ChannelLeaderName;
  private int MemberCount;
  private List<ChannelPartyInfo> parties;

  public ExMultiPartyCommandChannelInfo(CommandChannel channel)
  {
    ChannelLeaderName = channel.getChannelLeader().getName();
    MemberCount = channel.getMemberCount();

    parties = new ArrayList();
    for (Party party : channel.getParties())
    {
      Player leader = party.getPartyLeader();
      if (leader != null)
        parties.add(new ChannelPartyInfo(leader.getName(), leader.getObjectId(), party.getMemberCount()));
    }
  }

  protected void writeImpl()
  {
    writeEx(49);
    writeS(ChannelLeaderName);
    writeD(0);
    writeD(MemberCount);
    writeD(parties.size());

    for (ChannelPartyInfo party : parties)
    {
      writeS(party.Leader_name);
      writeD(party.Leader_obj_id);
      writeD(party.MemberCount);
    }
  }
  static class ChannelPartyInfo {
    public String Leader_name;
    public int Leader_obj_id;
    public int MemberCount;

    public ChannelPartyInfo(String _Leader_name, int _Leader_obj_id, int _MemberCount) {
      Leader_name = _Leader_name;
      Leader_obj_id = _Leader_obj_id;
      MemberCount = _MemberCount;
    }
  }
}
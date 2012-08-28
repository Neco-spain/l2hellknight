package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class ExMPCCPartyInfoUpdate extends L2GameServerPacket
{
  private final String leader_name;
  private final int leader_objId;
  private final int members_count;
  private final int _mode;

  public ExMPCCPartyInfoUpdate(L2Party party, int mode)
  {
    leader_name = party.getLeader().getName();
    leader_objId = party.getLeader().getObjectId();
    members_count = party.getMemberCount();
    _mode = mode;
  }

  protected void writeImpl()
  {
  }
}
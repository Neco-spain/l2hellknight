package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Clan.SubPledge;

public class PledgeReceiveSubPledgeCreated extends L2GameServerPacket
{
  private static final String _S__FE_3F_PLEDGERECEIVESUBPLEDGECREATED = "[S] FE:3F PledgeReceiveSubPledgeCreated";
  private L2Clan.SubPledge _subPledge;

  public PledgeReceiveSubPledgeCreated(L2Clan.SubPledge subPledge)
  {
    _subPledge = subPledge;
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(63);

    writeD(1);
    writeD(_subPledge.getId());
    writeS(_subPledge.getName());
    writeS(_subPledge.getLeaderName());
  }

  public String getType()
  {
    return "[S] FE:3F PledgeReceiveSubPledgeCreated";
  }
}
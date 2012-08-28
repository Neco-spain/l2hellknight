package l2m.gameserver.serverpackets;

import l2m.gameserver.model.pledge.SubUnit;

public class PledgeReceiveSubPledgeCreated extends L2GameServerPacket
{
  private int type;
  private String _name;
  private String leader_name;

  public PledgeReceiveSubPledgeCreated(SubUnit subPledge)
  {
    type = subPledge.getType();
    _name = subPledge.getName();
    leader_name = subPledge.getLeaderName();
  }

  protected final void writeImpl()
  {
    writeEx(64);

    writeD(1);
    writeD(type);
    writeS(_name);
    writeS(leader_name);
  }
}
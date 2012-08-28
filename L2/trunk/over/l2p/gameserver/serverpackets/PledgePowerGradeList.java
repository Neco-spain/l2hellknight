package l2p.gameserver.serverpackets;

import l2p.gameserver.model.pledge.RankPrivs;

public class PledgePowerGradeList extends L2GameServerPacket
{
  private RankPrivs[] _privs;

  public PledgePowerGradeList(RankPrivs[] privs)
  {
    _privs = privs;
  }

  protected final void writeImpl()
  {
    writeEx(60);
    writeD(_privs.length);
    for (RankPrivs element : _privs)
    {
      writeD(element.getRank());
      writeD(element.getParty());
    }
  }
}
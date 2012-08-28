package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Clan;

public class ManagePledgePower extends L2GameServerPacket
{
  private int _action;
  private L2Clan _clan;
  private int _rank;
  private int _privs;

  public ManagePledgePower(L2Clan clan, int action, int rank)
  {
    _clan = clan;
    _action = action;
    _rank = rank;
  }

  protected final void writeImpl()
  {
    if (_action == 1)
    {
      _privs = _clan.getRankPrivs(_rank);
    }
    else
    {
      return;
    }

    writeC(48);
    writeD(0);
    writeD(0);
    writeD(_privs);
  }
}
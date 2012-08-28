package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class ExPCCafePointInfo extends L2GameServerPacket
{
  private int pcBangPoints;
  private int m_AddPoint;
  private int m_PeriodType;
  private int PointType;

  public ExPCCafePointInfo(L2PcInstance player, int modify, boolean add, boolean _double)
  {
    m_AddPoint = modify;
    pcBangPoints = player.getPcPoints();
    m_PeriodType = (add ? 1 : 2);
    if (add)
      PointType = (_double ? 2 : 1);
    else
      PointType = 0;
  }

  protected final void writeImpl()
  {
    writeC(254);
    writeH(49);
    writeD(pcBangPoints);
    writeD(m_AddPoint);
    writeC(m_PeriodType);
    writeD(0);
    writeC(PointType);
  }
}
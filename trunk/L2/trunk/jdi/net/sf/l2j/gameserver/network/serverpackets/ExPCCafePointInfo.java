package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class ExPCCafePointInfo extends L2GameServerPacket
{
  private L2PcInstance _cha;
  private int m_AddPoint;
  private int m_PeriodType;
  private int RemainTime;
  private int PointType;

  public ExPCCafePointInfo(L2PcInstance user, int modify, boolean add, int hour, boolean _double)
  {
    _cha = user;
    m_AddPoint = modify;
    if (add)
    {
      m_PeriodType = 1;
      PointType = 1;
    }
    else if ((add) && (_double))
    {
      m_PeriodType = 1;
      PointType = 0;
    }
    else {
      m_PeriodType = 2;
      PointType = 2;
    }
    RemainTime = hour;
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(49);
    writeD(_cha.getPcCafeScore());
    writeD(m_AddPoint);
    writeC(m_PeriodType);
    writeD(RemainTime);
    writeC(PointType);
  }

  public String getType()
  {
    return "[S] FE:31 ExPCCafePointInfo";
  }
}
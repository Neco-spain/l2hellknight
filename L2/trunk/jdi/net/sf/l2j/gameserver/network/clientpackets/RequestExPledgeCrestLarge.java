package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.cache.CrestCache;
import net.sf.l2j.gameserver.network.serverpackets.ExPledgeCrestLarge;

public final class RequestExPledgeCrestLarge extends L2GameClientPacket
{
  private static final String _C__D0_10_REQUESTEXPLEDGECRESTLARGE = "[C] D0:10 RequestExPledgeCrestLarge";
  private int _crestId;

  protected void readImpl()
  {
    _crestId = readD();
  }

  protected void runImpl()
  {
    byte[] data = CrestCache.getInstance().getPledgeCrestLarge(_crestId);

    if (data != null)
    {
      ExPledgeCrestLarge pcl = new ExPledgeCrestLarge(_crestId, data);
      sendPacket(pcl);
    }
  }

  public String getType()
  {
    return "[C] D0:10 RequestExPledgeCrestLarge";
  }
}
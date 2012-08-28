package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.cache.CrestCache;
import net.sf.l2j.gameserver.network.serverpackets.ExPledgeCrestLarge;

public final class RequestExPledgeCrestLarge extends L2GameClientPacket
{
  private int _crestId;

  protected void readImpl()
  {
    _crestId = readD();
  }

  protected void runImpl()
  {
    byte[] data = CrestCache.getInstance().getPledgeCrestLarge(_crestId);

    if (data != null)
      sendPacket(new ExPledgeCrestLarge(_crestId, data));
  }
}
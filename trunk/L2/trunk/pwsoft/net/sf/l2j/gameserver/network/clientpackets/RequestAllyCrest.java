package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.cache.CrestCache;
import net.sf.l2j.gameserver.network.serverpackets.AllyCrest;

public final class RequestAllyCrest extends L2GameClientPacket
{
  private static Logger _log = Logger.getLogger(RequestAllyCrest.class.getName());
  private int _crestId;

  protected void readImpl()
  {
    _crestId = readD();
  }

  protected void runImpl()
  {
    byte[] data = CrestCache.getInstance().getAllyCrest(_crestId);

    if (data != null)
      sendPacket(new AllyCrest(_crestId, data));
  }
}
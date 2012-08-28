package l2m.gameserver.network.clientpackets;

import l2m.gameserver.cache.CrestCache;
import l2m.gameserver.network.serverpackets.AllianceCrest;

public class RequestAllyCrest extends L2GameClientPacket
{
  private int _crestId;

  protected void readImpl()
  {
    _crestId = readD();
  }

  protected void runImpl()
  {
    if (_crestId == 0)
      return;
    byte[] data = CrestCache.getInstance().getAllyCrest(_crestId);
    if (data != null)
    {
      AllianceCrest ac = new AllianceCrest(_crestId, data);
      sendPacket(ac);
    }
  }
}
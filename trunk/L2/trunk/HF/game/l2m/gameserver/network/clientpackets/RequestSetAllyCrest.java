package l2m.gameserver.network.clientpackets;

import java.nio.ByteBuffer;
import l2m.gameserver.cache.CrestCache;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.pledge.Alliance;
import l2m.gameserver.network.GameClient;

public class RequestSetAllyCrest extends L2GameClientPacket
{
  private int _length;
  private byte[] _data;

  protected void readImpl()
  {
    _length = readD();
    if ((_length == 192) && (_length == _buf.remaining()))
    {
      _data = new byte[_length];
      readB(_data);
    }
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    Alliance ally = activeChar.getAlliance();
    if ((ally != null) && (activeChar.isAllyLeader()))
    {
      int crestId = 0;

      if (_data != null)
        crestId = CrestCache.getInstance().saveAllyCrest(ally.getAllyId(), _data);
      else if (ally.hasAllyCrest()) {
        CrestCache.getInstance().removeAllyCrest(ally.getAllyId());
      }
      ally.setAllyCrestId(crestId);
      ally.broadcastAllyStatus();
    }
  }
}
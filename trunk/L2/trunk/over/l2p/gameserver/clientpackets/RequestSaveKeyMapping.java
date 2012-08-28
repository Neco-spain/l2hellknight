package l2p.gameserver.clientpackets;

import java.nio.ByteBuffer;
import l2p.gameserver.model.Player;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ExUISetting;

public class RequestSaveKeyMapping extends L2GameClientPacket
{
  private byte[] _data;

  protected void readImpl()
  {
    int length = readD();
    if ((length > _buf.remaining()) || (length > 32767) || (length < 0))
    {
      _data = null;
      return;
    }
    _data = new byte[length];
    readB(_data);
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if ((activeChar == null) || (_data == null))
      return;
    activeChar.setKeyBindings(_data);
    activeChar.sendPacket(new ExUISetting(activeChar));
  }
}
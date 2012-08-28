package l2m.gameserver.network.clientpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.FinishRotating;
import l2m.gameserver.network.serverpackets.L2GameServerPacket;

public class FinishRotatingC extends L2GameClientPacket
{
  private int _degree;
  private int _unknown;

  protected void readImpl()
  {
    _degree = readD();
    _unknown = readD();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null)
      return;
    activeChar.broadcastPacket(new L2GameServerPacket[] { new FinishRotating(activeChar, _degree, 0) });
  }
}
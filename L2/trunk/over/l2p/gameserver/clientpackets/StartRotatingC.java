package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.StartRotating;

public class StartRotatingC extends L2GameClientPacket
{
  private int _degree;
  private int _side;

  protected void readImpl()
  {
    _degree = readD();
    _side = readD();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null)
      return;
    activeChar.setHeading(_degree);
    activeChar.broadcastPacket(new L2GameServerPacket[] { new StartRotating(activeChar, _degree, _side, 0) });
  }
}
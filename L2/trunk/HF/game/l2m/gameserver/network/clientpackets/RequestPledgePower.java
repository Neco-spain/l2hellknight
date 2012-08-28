package l2m.gameserver.network.clientpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.model.pledge.Clan;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.ManagePledgePower;

public class RequestPledgePower extends L2GameClientPacket
{
  private int _rank;
  private int _action;
  private int _privs;

  protected void readImpl()
  {
    _rank = readD();
    _action = readD();
    if (_action == 2)
      _privs = readD();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null)
      return;
    if (_action == 2)
    {
      if ((_rank < 1) || (_rank > 9))
        return;
      if ((activeChar.getClan() != null) && ((activeChar.getClanPrivileges() & 0x10) == 16))
      {
        if (_rank == 9)
          _privs = ((_privs & 0x8) + (_privs & 0x800) + (_privs & 0x10000) + (_privs & 0x1000) + (_privs & 0x80000));
        activeChar.getClan().setRankPrivs(_rank, _privs);
        activeChar.getClan().updatePrivsForRank(_rank);
      }
    }
    else if (activeChar.getClan() != null) {
      activeChar.sendPacket(new ManagePledgePower(activeChar, _action, _rank));
    } else {
      activeChar.sendActionFailed();
    }
  }
}
package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.network.GameClient;

public class RequestPledgeMemberList extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null)
      return;
    Clan clan = activeChar.getClan();
    if (clan != null)
    {
      activeChar.sendPacket(clan.listAll());
    }
  }
}
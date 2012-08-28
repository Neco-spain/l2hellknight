package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.PledgeReceiveWarList;

public class RequestPledgeWarList extends L2GameClientPacket
{
  static int _type;
  private int _page;

  protected void readImpl()
  {
    _page = readD();
    _type = readD();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    Clan clan = activeChar.getClan();
    if (clan != null)
      activeChar.sendPacket(new PledgeReceiveWarList(clan, _type, _page));
  }
}
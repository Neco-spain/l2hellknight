package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.PledgeInfo;
import l2p.gameserver.tables.ClanTable;

public class RequestPledgeInfo extends L2GameClientPacket
{
  private int _clanId;

  protected void readImpl()
  {
    _clanId = readD();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null)
      return;
    if (_clanId < 10000000)
    {
      activeChar.sendActionFailed();
      return;
    }
    Clan clan = ClanTable.getInstance().getClan(_clanId);
    if (clan == null)
    {
      activeChar.sendActionFailed();
      return;
    }

    activeChar.sendPacket(new PledgeInfo(clan));
  }
}
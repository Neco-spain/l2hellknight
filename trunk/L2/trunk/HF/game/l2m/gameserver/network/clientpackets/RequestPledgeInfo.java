package l2m.gameserver.network.clientpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.model.pledge.Clan;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.PledgeInfo;
import l2m.gameserver.data.tables.ClanTable;

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
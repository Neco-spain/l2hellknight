package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestSurrenderPledgeWar extends L2GameClientPacket
{
  private static Logger _log = Logger.getLogger(RequestSurrenderPledgeWar.class.getName());
  private String _pledgeName;
  private L2Clan _clan;
  private L2PcInstance _activeChar;

  protected void readImpl()
  {
    _pledgeName = readS();
  }

  protected void runImpl()
  {
    _activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (_activeChar == null)
      return;
    _clan = _activeChar.getClan();
    if (_clan == null)
      return;
    L2Clan clan = ClanTable.getInstance().getClanByName(_pledgeName);

    if (clan == null)
    {
      _activeChar.sendMessage("No such clan.");
      _activeChar.sendActionFailed();
      return;
    }

    _log.info("RequestSurrenderPledgeWar by " + ((L2GameClient)getClient()).getActiveChar().getClan().getName() + " with " + _pledgeName);

    if (!_clan.isAtWarWith(clan.getClanId()))
    {
      _activeChar.sendMessage("You aren't at war with this clan.");
      _activeChar.sendActionFailed();
      return;
    }

    _activeChar.sendPacket(SystemMessage.id(SystemMessageId.YOU_HAVE_SURRENDERED_TO_THE_S1_CLAN).addString(_pledgeName));
    _activeChar.deathPenalty(false);
    ClanTable.getInstance().deleteclanswars(_clan.getClanId(), clan.getClanId());
  }

  public String getType()
  {
    return "C.SurrenderPledgeWar";
  }
}
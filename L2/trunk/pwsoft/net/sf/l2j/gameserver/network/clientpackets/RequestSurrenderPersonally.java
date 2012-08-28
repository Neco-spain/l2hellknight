package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestSurrenderPersonally extends L2GameClientPacket
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
    _log.info("RequestSurrenderPersonally by " + ((L2GameClient)getClient()).getActiveChar().getName() + " with " + _pledgeName);
    _clan = ((L2GameClient)getClient()).getActiveChar().getClan();
    L2Clan clan = ClanTable.getInstance().getClanByName(_pledgeName);

    if (_clan == null) {
      return;
    }
    if (clan == null)
    {
      _activeChar.sendMessage("No such clan.");
      _activeChar.sendActionFailed();
      return;
    }

    if ((!_clan.isAtWarWith(clan.getClanId())) || (_activeChar.getWantsPeace() == 1))
    {
      _activeChar.sendMessage("You aren't at war with this clan.");
      _activeChar.sendActionFailed();
      return;
    }

    _activeChar.setWantsPeace(1);
    _activeChar.deathPenalty(false);
    _activeChar.sendPacket(SystemMessage.id(SystemMessageId.YOU_HAVE_PERSONALLY_SURRENDERED_TO_THE_S1_CLAN).addString(_pledgeName));
    ClanTable.getInstance().checkSurrender(_clan, clan);
  }
}
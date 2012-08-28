package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.SiegeDefenderList;

public final class RequestConfirmSiegeWaitingList extends L2GameClientPacket
{
  private int _approved;
  private int _castleId;
  private int _clanId;

  protected void readImpl()
  {
    _castleId = readD();
    _clanId = readD();
    _approved = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) return;

    if (player.getClan() == null) return;

    Castle castle = CastleManager.getInstance().getCastleById(_castleId);
    if (castle == null) return;

    if ((castle.getOwnerId() != player.getClanId()) || (!player.isClanLeader())) return;

    L2Clan clan = ClanTable.getInstance().getClan(_clanId);
    if (clan == null) return;

    if (!castle.getSiege().getIsRegistrationOver())
    {
      if (_approved == 1)
      {
        if (castle.getSiege().checkIsDefenderWaiting(clan))
          castle.getSiege().approveSiegeDefenderClan(_clanId);
        else {
          return;
        }

      }
      else if ((castle.getSiege().checkIsDefenderWaiting(clan)) || (castle.getSiege().checkIsDefender(clan))) {
        castle.getSiege().removeSiegeClan(_clanId);
      }

    }

    player.sendPacket(new SiegeDefenderList(castle));
  }
}
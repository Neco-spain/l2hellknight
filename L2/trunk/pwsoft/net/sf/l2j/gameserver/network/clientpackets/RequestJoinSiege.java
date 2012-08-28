package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class RequestJoinSiege extends L2GameClientPacket
{
  private int _castleId;
  private int _isAttacker;
  private int _isJoining;

  protected void readImpl()
  {
    _castleId = readD();
    _isAttacker = readD();
    _isJoining = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) return;
    if (!player.isClanLeader()) return;

    Castle castle = CastleManager.getInstance().getCastleById(_castleId);
    if (castle == null) return;

    if (_isJoining == 1)
    {
      if (System.currentTimeMillis() < player.getClan().getDissolvingExpiryTime())
      {
        player.sendPacket(Static.CANT_PARTICIPATE_IN_SIEGE_WHILE_DISSOLUTION_IN_PROGRESS);
        return;
      }
      if (_isAttacker == 1)
        castle.getSiege().registerAttacker(player);
      else
        castle.getSiege().registerDefender(player);
    }
    else {
      castle.getSiege().removeSiegeClan(player);
    }
    castle.getSiege().listRegisterClan(player);
  }
}
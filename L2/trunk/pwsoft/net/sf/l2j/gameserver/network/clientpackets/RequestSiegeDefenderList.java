package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.network.serverpackets.SiegeDefenderList;

public final class RequestSiegeDefenderList extends L2GameClientPacket
{
  private int _castleId;

  protected void readImpl()
  {
    _castleId = readD();
  }

  protected void runImpl()
  {
    Castle castle = CastleManager.getInstance().getCastleById(_castleId);
    if (castle == null)
      return;
    sendPacket(new SiegeDefenderList(castle));
  }

  public String getType()
  {
    return "C.SiegeDefenderList";
  }
}
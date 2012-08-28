package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.FortManager;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Fort;
import net.sf.l2j.gameserver.network.serverpackets.FortSiegeDefenderList;
import net.sf.l2j.gameserver.network.serverpackets.SiegeDefenderList;

public final class RequestSiegeDefenderList extends L2GameClientPacket
{
  private static final String _C__a3_RequestSiegeDefenderList = "[C] a3 RequestSiegeDefenderList";
  private int _castleId;

  protected void readImpl()
  {
    _castleId = readD();
  }

  protected void runImpl()
  {
    if (_castleId < 100)
    {
      Castle castle = CastleManager.getInstance().getCastleById(_castleId);

      if (castle == null) {
        return;
      }
      SiegeDefenderList sdl = new SiegeDefenderList(castle);
      sendPacket(sdl);
    }
    else
    {
      Fort fort = FortManager.getInstance().getFortById(_castleId);

      if (fort == null) {
        return;
      }
      FortSiegeDefenderList sdl = new FortSiegeDefenderList(fort);
      sendPacket(sdl);
    }
  }

  public String getType()
  {
    return "[C] a3 RequestSiegeDefenderList";
  }
}
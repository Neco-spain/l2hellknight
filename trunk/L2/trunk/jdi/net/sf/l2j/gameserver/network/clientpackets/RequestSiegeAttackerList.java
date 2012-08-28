package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.FortManager;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Fort;
import net.sf.l2j.gameserver.network.serverpackets.FortSiegeAttackerList;
import net.sf.l2j.gameserver.network.serverpackets.SiegeAttackerList;

public final class RequestSiegeAttackerList extends L2GameClientPacket
{
  private static final String _C__A2_RequestSiegeAttackerList = "[C] a2 RequestSiegeAttackerList";
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
      SiegeAttackerList sal = new SiegeAttackerList(castle);
      sendPacket(sal);
    }
    else
    {
      Fort fort = FortManager.getInstance().getFortById(_castleId);

      if (fort == null) {
        return;
      }
      FortSiegeAttackerList sal = new FortSiegeAttackerList(fort);
      sendPacket(sal);
    }
  }

  public String getType()
  {
    return "[C] a2 RequestSiegeAttackerList";
  }
}
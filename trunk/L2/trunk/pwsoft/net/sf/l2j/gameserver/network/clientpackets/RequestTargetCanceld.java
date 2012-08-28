package net.sf.l2j.gameserver.network.clientpackets;

import java.util.Map;
import net.sf.l2j.gameserver.model.actor.instance.L2CubicInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class RequestTargetCanceld extends L2GameClientPacket
{
  private int _unselect;

  protected void readImpl()
  {
    _unselect = readH();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    if (System.currentTimeMillis() - player.gCPAJ() < 100L) {
      return;
    }
    player.sCPAJ();

    for (L2CubicInstance cubic : player.getCubics().values()) {
      if (cubic.getId() != 3)
        cubic.stopAction();
    }
    if (_unselect == 0)
    {
      if ((player.isCastingNow()) && (player.canAbortCast()))
        player.abortCast();
      else if (player.getTarget() != null)
        player.setTarget(null);
    }
    else if (player.getTarget() != null)
      player.setTarget(null);
  }

  public String getType()
  {
    return "C.TargetCanceld";
  }
}
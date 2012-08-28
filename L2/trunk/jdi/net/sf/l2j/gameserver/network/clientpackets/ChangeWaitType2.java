package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2StaticObjectInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.ChairSit;

public final class ChangeWaitType2 extends L2GameClientPacket
{
  private static final String _C__1D_CHANGEWAITTYPE2 = "[C] 1D ChangeWaitType2";
  private boolean _typeStand;

  protected void readImpl()
  {
    _typeStand = (readD() == 1);
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    L2Object target = player.getTarget();
    if ((getClient() != null) && (player != null))
    {
      if (player.isOutOfControl())
      {
        player.sendPacket(new ActionFailed());
        return;
      }

      if (player.getMountType() != 0)
        return;
      if ((target != null) && (!player.isSitting()) && ((target instanceof L2StaticObjectInstance)) && (((L2StaticObjectInstance)target).getType() == 1) && (CastleManager.getInstance().getCastle(target) != null) && (player.isInsideRadius(target, 150, false, false)))
      {
        ChairSit cs = new ChairSit(player, ((L2StaticObjectInstance)target).getStaticObjectId());
        player.sendPacket(cs);
        player.sitDown();
        player.broadcastPacket(cs);
      }
      if (_typeStand)
        player.standUp();
      else
        player.sitDown();
    }
  }

  public String getType()
  {
    return "[C] 1D ChangeWaitType2";
  }
}
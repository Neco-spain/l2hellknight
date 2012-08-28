package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class RequestDropItem extends L2GameClientPacket
{
  private static Logger _log = Logger.getLogger(RequestDropItem.class.getName());
  private int _objectId;
  private int _count;
  private int _x;
  private int _y;
  private int _z;

  protected void readImpl()
  {
    _objectId = readD();
    _count = readD();
    _x = readD();
    _y = readD();
    _z = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null)
      return;
    player.sendPacket(Static.CANNOT_DISCARD_THIS_ITEM);
  }
}
package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.BeginRotation;

public final class StartRotating extends L2GameClientPacket
{
  private static final String _C__4A_STARTROTATING = "[C] 4A StartRotating";
  private int _degree;
  private int _side;

  protected void readImpl()
  {
    _degree = readD();
    _side = readD();
  }

  protected void runImpl()
  {
    if (((L2GameClient)getClient()).getActiveChar() == null)
      return;
    BeginRotation br = new BeginRotation(((L2GameClient)getClient()).getActiveChar().getObjectId(), _degree, _side, 0);
    ((L2GameClient)getClient()).getActiveChar().broadcastPacket(br);
  }

  public String getType()
  {
    return "[C] 4A StartRotating";
  }
}
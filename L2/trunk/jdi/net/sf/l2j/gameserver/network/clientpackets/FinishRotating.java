package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.StopRotation;

public final class FinishRotating extends L2GameClientPacket
{
  private static final String _C__4B_FINISHROTATING = "[C] 4B FinishRotating";
  private int _degree;
  private int _unknown;

  protected void readImpl()
  {
    _degree = readD();
    _unknown = readD();
  }

  protected void runImpl()
  {
    if (((L2GameClient)getClient()).getActiveChar() == null)
      return;
    StopRotation sr = new StopRotation(((L2GameClient)getClient()).getActiveChar().getObjectId(), _degree, 0);
    ((L2GameClient)getClient()).getActiveChar().broadcastPacket(sr);
  }

  public String getType()
  {
    return "[C] 4B FinishRotating";
  }
}
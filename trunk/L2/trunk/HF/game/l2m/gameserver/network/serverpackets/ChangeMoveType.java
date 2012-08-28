package l2m.gameserver.network.serverpackets;

import l2m.gameserver.model.Creature;

public class ChangeMoveType extends L2GameServerPacket
{
  public static int WALK = 0;
  public static int RUN = 1;
  private int _chaId;
  private boolean _running;

  public ChangeMoveType(Creature cha)
  {
    _chaId = cha.getObjectId();
    _running = cha.isRunning();
  }

  protected final void writeImpl()
  {
    writeC(40);
    writeD(_chaId);
    writeD(_running ? 1 : 0);
    writeD(0);
  }
}
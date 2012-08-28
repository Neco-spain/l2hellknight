package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Creature;

public class FinishRotating extends L2GameServerPacket
{
  private int _charId;
  private int _degree;
  private int _speed;

  public FinishRotating(Creature player, int degree, int speed)
  {
    _charId = player.getObjectId();
    _degree = degree;
    _speed = speed;
  }

  protected final void writeImpl()
  {
    writeC(97);
    writeD(_charId);
    writeD(_degree);
    writeD(_speed);
    writeD(0);
  }
}
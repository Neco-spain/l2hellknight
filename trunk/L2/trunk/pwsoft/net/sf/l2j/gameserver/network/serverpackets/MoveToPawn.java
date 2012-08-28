package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Summon;

public class MoveToPawn extends L2GameServerPacket
{
  private int _charObjId;
  private int _targetId;
  private int _distance;
  private int _x;
  private int _y;
  private int _z;

  public MoveToPawn(L2Character cha, L2Character target, int distance)
  {
    _charObjId = cha.getObjectId();
    _targetId = target.getObjectId();
    _distance = distance;
    if ((cha.isL2Summon()) && (((L2Summon)cha).getNpcId() == Config.SOB_NPC))
      _distance = 4;
    _x = cha.getX();
    _y = cha.getY();
    _z = cha.getZ();
  }

  protected final void writeImpl()
  {
    writeC(96);

    writeD(_charObjId);
    writeD(_targetId);
    writeD(_distance);

    writeD(_x);
    writeD(_y);
    writeD(_z);
  }
}
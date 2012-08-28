package l2m.gameserver.network.serverpackets;

import l2m.gameserver.model.Creature;

public class ExFishingStartCombat extends L2GameServerPacket
{
  int _time;
  int _hp;
  int _lureType;
  int _deceptiveMode;
  int _mode;
  private int char_obj_id;

  public ExFishingStartCombat(Creature character, int time, int hp, int mode, int lureType, int deceptiveMode)
  {
    char_obj_id = character.getObjectId();
    _time = time;
    _hp = hp;
    _mode = mode;
    _lureType = lureType;
    _deceptiveMode = deceptiveMode;
  }

  protected final void writeImpl()
  {
    writeEx(39);

    writeD(char_obj_id);
    writeD(_time);
    writeD(_hp);
    writeC(_mode);
    writeC(_lureType);
    writeC(_deceptiveMode);
  }
}
package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Creature;

public class ExFishingHpRegen extends L2GameServerPacket
{
  private int _time;
  private int _fishHP;
  private int _HPmode;
  private int _Anim;
  private int _GoodUse;
  private int _Penalty;
  private int _hpBarColor;
  private int char_obj_id;

  public ExFishingHpRegen(Creature character, int time, int fishHP, int HPmode, int GoodUse, int anim, int penalty, int hpBarColor)
  {
    char_obj_id = character.getObjectId();
    _time = time;
    _fishHP = fishHP;
    _HPmode = HPmode;
    _GoodUse = GoodUse;
    _Anim = anim;
    _Penalty = penalty;
    _hpBarColor = hpBarColor;
  }

  protected final void writeImpl()
  {
    writeEx(40);
    writeD(char_obj_id);
    writeD(_time);
    writeD(_fishHP);
    writeC(_HPmode);
    writeC(_GoodUse);
    writeC(_Anim);
    writeD(_Penalty);
    writeC(_hpBarColor);
  }
}
package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Character;

public class ExFishingHpRegen extends L2GameServerPacket
{
  private L2Character _activeChar;
  private int _time;
  private int _fishHP;
  private int _hpMode;
  private int _anim;
  private int _goodUse;
  private int _penalty;
  private int _hpBarColor;

  public ExFishingHpRegen(L2Character character, int time, int fishHP, int HPmode, int GoodUse, int anim, int penalty, int hpBarColor)
  {
    _activeChar = character;
    _time = time;
    _fishHP = fishHP;
    _hpMode = HPmode;
    _goodUse = GoodUse;
    _anim = anim;
    _penalty = penalty;
    _hpBarColor = hpBarColor;
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(22);

    writeD(_activeChar.getObjectId());
    writeD(_time);
    writeD(_fishHP);
    writeC(_hpMode);
    writeC(_goodUse);
    writeC(_anim);
    writeD(_penalty);
    writeC(_hpBarColor);
  }
}
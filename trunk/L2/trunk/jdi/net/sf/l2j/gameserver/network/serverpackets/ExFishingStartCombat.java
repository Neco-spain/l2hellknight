package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Character;

public class ExFishingStartCombat extends L2GameServerPacket
{
  private static final String _S__FE_15_EXFISHINGSTARTCOMBAT = "[S] FE:15 ExFishingStartCombat";
  private L2Character _activeChar;
  private int _time;
  private int _hp;
  private int _lureType;
  private int _deceptiveMode;
  private int _mode;

  public ExFishingStartCombat(L2Character character, int time, int hp, int mode, int lureType, int deceptiveMode)
  {
    _activeChar = character;
    _time = time;
    _hp = hp;
    _mode = mode;
    _lureType = lureType;
    _deceptiveMode = deceptiveMode;
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(21);

    writeD(_activeChar.getObjectId());
    writeD(_time);
    writeD(_hp);
    writeC(_mode);
    writeC(_lureType);
    writeC(_deceptiveMode);
  }

  public String getType()
  {
    return "[S] FE:15 ExFishingStartCombat";
  }
}
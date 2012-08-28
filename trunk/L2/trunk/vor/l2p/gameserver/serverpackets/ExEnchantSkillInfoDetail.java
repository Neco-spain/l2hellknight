package l2p.gameserver.serverpackets;

public class ExEnchantSkillInfoDetail extends L2GameServerPacket
{
  private final int _unk = 0;
  private final int _skillId;
  private final int _skillLvl;
  private final int _sp;
  private final int _chance;
  private final int _bookId;
  private final int _adenaCount;

  public ExEnchantSkillInfoDetail(int skillId, int skillLvl, int sp, int chance, int bookId, int adenaCount)
  {
    _skillId = skillId;
    _skillLvl = skillLvl;
    _sp = sp;
    _chance = chance;
    _bookId = bookId;
    _adenaCount = adenaCount;
  }

  protected void writeImpl()
  {
    writeEx(94);

    writeD(0);
    writeD(_skillId);
    writeD(_skillLvl);
    writeD(_sp);
    writeD(_chance);

    writeD(2);
    writeD(57);
    writeD(_adenaCount);
    if (_bookId > 0)
    {
      writeD(_bookId);
      writeD(1);
    }
    else
    {
      writeD(6622);
      writeD(0);
    }
  }
}
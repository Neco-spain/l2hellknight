package net.sf.l2j.gameserver.network.serverpackets;

public class ShortBuffStatusUpdate extends L2GameServerPacket
{
  private static final String _S__F4_SHORTBUFFSTATUSUPDATE = "[S] F4 ShortBuffStatusUpdate";
  private int _skillId;
  private int _skillLvl;
  private int _duration;

  public ShortBuffStatusUpdate(int skillId, int skillLvl, int duration)
  {
    _skillId = skillId;
    _skillLvl = skillLvl;
    _duration = duration;
  }

  protected final void writeImpl()
  {
    writeC(244);
    writeD(_skillId);
    writeD(_skillLvl);
    writeD(_duration);
  }

  public String getType()
  {
    return "[S] F4 ShortBuffStatusUpdate";
  }
}
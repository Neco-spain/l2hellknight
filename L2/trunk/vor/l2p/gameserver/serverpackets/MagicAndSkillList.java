package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Creature;

public class MagicAndSkillList extends L2GameServerPacket
{
  private int _chaId;
  private int _unk1;
  private int _unk2;

  public MagicAndSkillList(Creature cha, int unk1, int unk2)
  {
    _chaId = cha.getObjectId();
    _unk1 = unk1;
    _unk2 = unk2;
  }

  protected final void writeImpl()
  {
    writeC(64);
    writeD(_chaId);
    writeD(_unk1);
    writeD(_unk2);
  }
}
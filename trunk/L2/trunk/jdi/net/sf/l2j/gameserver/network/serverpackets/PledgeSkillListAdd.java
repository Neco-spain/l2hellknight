package net.sf.l2j.gameserver.network.serverpackets;

public class PledgeSkillListAdd extends L2GameServerPacket
{
  private static final String _S__FE_3A_PLEDGESKILLLISTADD = "[S] FE:3A PledgeSkillListAdd";
  private int _id;
  private int _lvl;

  public PledgeSkillListAdd(int id, int lvl)
  {
    _id = id;
    _lvl = lvl;
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(58);

    writeD(_id);
    writeD(_lvl);
  }

  public String getType()
  {
    return "[S] FE:3A PledgeSkillListAdd";
  }
}
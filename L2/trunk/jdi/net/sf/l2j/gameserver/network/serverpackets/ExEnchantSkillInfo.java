package net.sf.l2j.gameserver.network.serverpackets;

import javolution.util.FastList;

public class ExEnchantSkillInfo extends L2GameServerPacket
{
  private static final String _S__FE_18_EXENCHANTSKILLINFO = "[S] FE:18 ExEnchantSkillInfo";
  private FastList<Req> _reqs;
  private int _id;
  private int _level;
  private int _spCost;
  private int _xpCost;
  private int _rate;

  public ExEnchantSkillInfo(int id, int level, int spCost, int xpCost, int rate)
  {
    _reqs = new FastList();
    _id = id;
    _level = level;
    _spCost = spCost;
    _xpCost = xpCost;
    _rate = rate;
  }

  public void addRequirement(int type, int id, int count, int unk)
  {
    _reqs.add(new Req(type, id, count, unk));
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(24);

    writeD(_id);
    writeD(_level);
    writeD(_spCost);
    writeQ(_xpCost);
    writeD(_rate);

    writeD(_reqs.size());

    for (Req temp : _reqs)
    {
      writeD(temp.type);
      writeD(temp.id);
      writeD(temp.count);
      writeD(temp.unk);
    }
  }

  public String getType()
  {
    return "[S] FE:18 ExEnchantSkillInfo";
  }

  class Req
  {
    public int id;
    public int count;
    public int type;
    public int unk;

    Req(int pType, int pId, int pCount, int pUnk)
    {
      id = pId;
      type = pType;
      count = pCount;
      unk = pUnk;
    }
  }
}
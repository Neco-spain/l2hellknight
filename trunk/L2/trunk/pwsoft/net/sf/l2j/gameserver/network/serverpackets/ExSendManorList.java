package net.sf.l2j.gameserver.network.serverpackets;

import javolution.util.FastList;

public class ExSendManorList extends L2GameServerPacket
{
  private FastList<String> _manors;

  public ExSendManorList(FastList<String> manors)
  {
    _manors = manors;
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(27);
    writeD(_manors.size());
    for (int i = 0; i < _manors.size(); i++)
    {
      int j = i + 1;
      writeD(j);
      writeS((CharSequence)_manors.get(i));
    }
  }

  public void gc()
  {
    _manors.clear();
    _manors = null;
  }
}
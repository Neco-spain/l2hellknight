package net.sf.l2j.gameserver.network.serverpackets;

import java.util.List;
import javolution.util.FastList;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.base.Race;
import net.sf.l2j.gameserver.templates.L2PcTemplate;

public class CharTemplates extends L2GameServerPacket
{
  private List<L2PcTemplate> _chars = new FastList();

  public void addChar(L2PcTemplate template)
  {
    _chars.add(template);
  }

  protected final void writeImpl()
  {
    writeC(23);
    writeD(_chars.size());

    for (L2PcTemplate temp : _chars)
    {
      writeD(temp.race.ordinal());
      writeD(temp.classId.getId());
      writeD(70);
      writeD(temp.baseSTR);
      writeD(10);
      writeD(70);
      writeD(temp.baseDEX);
      writeD(10);
      writeD(70);
      writeD(temp.baseCON);
      writeD(10);
      writeD(70);
      writeD(temp.baseINT);
      writeD(10);
      writeD(70);
      writeD(temp.baseWIT);
      writeD(10);
      writeD(70);
      writeD(temp.baseMEN);
      writeD(10);
    }
  }

  public void gc()
  {
    _chars.clear();
    _chars = null;
  }
}
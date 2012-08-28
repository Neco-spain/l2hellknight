package l2m.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2m.gameserver.model.base.ClassId;
import l2m.gameserver.model.base.Race;
import l2m.gameserver.templates.PlayerTemplate;

public class NewCharacterSuccess extends L2GameServerPacket
{
  private List<PlayerTemplate> _chars = new ArrayList();

  public void addChar(PlayerTemplate template)
  {
    _chars.add(template);
  }

  protected final void writeImpl()
  {
    writeC(13);
    writeD(_chars.size());

    for (PlayerTemplate temp : _chars)
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
}
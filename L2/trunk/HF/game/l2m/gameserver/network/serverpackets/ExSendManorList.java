package l2m.gameserver.network.serverpackets;

import java.util.Collection;
import l2m.gameserver.data.xml.holder.ResidenceHolder;
import l2m.gameserver.model.entity.residence.Castle;
import l2m.gameserver.model.entity.residence.Residence;

public class ExSendManorList extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(34);
    Collection residences = ResidenceHolder.getInstance().getResidenceList(Castle.class);
    writeD(residences.size());
    for (Residence castle : residences)
    {
      writeD(castle.getId());
      writeS(castle.getName().toLowerCase());
    }
  }
}
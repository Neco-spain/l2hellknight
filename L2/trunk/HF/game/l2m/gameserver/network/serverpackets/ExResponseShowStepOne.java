package l2m.gameserver.network.serverpackets;

import java.util.Collection;
import l2m.gameserver.data.xml.holder.PetitionGroupHolder;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.petition.PetitionMainGroup;
import l2m.gameserver.utils.Language;

public class ExResponseShowStepOne extends L2GameServerPacket
{
  private Language _language;

  public ExResponseShowStepOne(Player player)
  {
    _language = player.getLanguage();
  }

  protected void writeImpl()
  {
    writeEx(174);
    Collection petitionGroups = PetitionGroupHolder.getInstance().getPetitionGroups();
    writeD(petitionGroups.size());
    for (PetitionMainGroup group : petitionGroups)
    {
      writeC(group.getId());
      writeS(group.getName(_language));
    }
  }
}
package l2m.gameserver.network.serverpackets;

import java.util.Collection;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.petition.PetitionMainGroup;
import l2m.gameserver.model.petition.PetitionSubGroup;
import l2m.gameserver.utils.Language;

public class ExResponseShowStepTwo extends L2GameServerPacket
{
  private Language _language;
  private PetitionMainGroup _petitionMainGroup;

  public ExResponseShowStepTwo(Player player, PetitionMainGroup gr)
  {
    _language = player.getLanguage();
    _petitionMainGroup = gr;
  }

  protected void writeImpl()
  {
    writeEx(175);
    Collection subGroups = _petitionMainGroup.getSubGroups();
    writeD(subGroups.size());
    writeS(_petitionMainGroup.getDescription(_language));
    for (PetitionSubGroup g : subGroups)
    {
      writeC(g.getId());
      writeS(g.getName(_language));
    }
  }
}
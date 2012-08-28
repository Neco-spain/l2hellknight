package l2m.gameserver.network.clientpackets;

import l2m.gameserver.Config;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.petition.PetitionMainGroup;
import l2m.gameserver.model.petition.PetitionSubGroup;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.ExResponseShowContents;

public class RequestExShowStepThree extends L2GameClientPacket
{
  private int _subId;

  protected void readImpl()
  {
    _subId = readC();
  }

  protected void runImpl()
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if ((player == null) || (!Config.EX_NEW_PETITION_SYSTEM)) {
      return;
    }
    PetitionMainGroup group = player.getPetitionGroup();
    if (group == null) {
      return;
    }
    PetitionSubGroup subGroup = group.getSubGroup(_subId);
    if (subGroup == null) {
      return;
    }
    player.sendPacket(new ExResponseShowContents(subGroup.getDescription(player.getLanguage())));
  }
}
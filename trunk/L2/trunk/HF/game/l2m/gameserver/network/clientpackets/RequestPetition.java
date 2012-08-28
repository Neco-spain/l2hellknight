package l2m.gameserver.network.clientpackets;

import l2m.gameserver.Config;
import l2m.gameserver.handler.petition.IPetitionHandler;
import l2m.gameserver.instancemanager.PetitionManager;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.petition.PetitionMainGroup;
import l2m.gameserver.model.petition.PetitionSubGroup;
import l2m.gameserver.network.GameClient;

public final class RequestPetition extends L2GameClientPacket
{
  private String _content;
  private int _type;

  protected void readImpl()
  {
    _content = readS();
    _type = readD();
  }

  protected void runImpl()
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    if (Config.EX_NEW_PETITION_SYSTEM)
    {
      PetitionMainGroup group = player.getPetitionGroup();
      if (group == null) {
        return;
      }
      PetitionSubGroup subGroup = group.getSubGroup(_type);
      if (subGroup == null) {
        return;
      }
      subGroup.getHandler().handle(player, _type, _content);
    }
    else
    {
      PetitionManager.getInstance().handle(player, _type, _content);
    }
  }
}
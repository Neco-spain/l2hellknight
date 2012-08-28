package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.handler.petition.IPetitionHandler;
import l2p.gameserver.instancemanager.PetitionManager;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.petition.PetitionMainGroup;
import l2p.gameserver.model.petition.PetitionSubGroup;
import l2p.gameserver.network.GameClient;

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
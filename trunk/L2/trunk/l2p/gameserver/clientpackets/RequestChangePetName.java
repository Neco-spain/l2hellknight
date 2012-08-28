package l2p.gameserver.clientpackets;

import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Summon;
import l2p.gameserver.model.instances.PetInstance;
import l2p.gameserver.network.GameClient;

public class RequestChangePetName extends L2GameClientPacket
{
  private String _name;

  protected void readImpl()
  {
    _name = readS();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    PetInstance pet = (activeChar.getPet() != null) && (activeChar.getPet().isPet()) ? (PetInstance)activeChar.getPet() : null;
    if (pet == null) {
      return;
    }
    if (pet.isDefaultName())
    {
      if ((_name.length() < 1) || (_name.length() > 8))
      {
        sendPacket(Msg.YOUR_PETS_NAME_CAN_BE_UP_TO_8_CHARACTERS);
        return;
      }
      pet.setName(_name);
      pet.broadcastCharInfo();
      pet.updateControlItem();
    }
  }
}
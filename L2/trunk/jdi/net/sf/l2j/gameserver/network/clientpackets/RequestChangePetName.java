package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.datatables.PetNameTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.NpcInfo;
import net.sf.l2j.gameserver.network.serverpackets.PetInfo;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public final class RequestChangePetName extends L2GameClientPacket
{
  private static final String REQUESTCHANGEPETNAME__C__89 = "[C] 89 RequestChangePetName";
  private String _name;

  protected void readImpl()
  {
    _name = readS();
  }

  protected void runImpl()
  {
    L2Character activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    L2Summon pet = activeChar.getPet();
    if (pet == null) {
      return;
    }
    if (pet.getName() != null)
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.NAMING_YOU_CANNOT_SET_NAME_OF_THE_PET));
      return;
    }
    if (PetNameTable.getInstance().doesPetNameExist(_name, pet.getTemplate().npcId))
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.NAMING_ALREADY_IN_USE_BY_ANOTHER_PET));
      return;
    }
    if ((_name.length() < 3) || (_name.length() > 16))
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
      sm.addString("Your pet's name can be up to 16 characters.");

      activeChar.sendPacket(sm);
      sm = null;

      return;
    }
    if (!PetNameTable.getInstance().isValidPetName(_name))
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.NAMING_PETNAME_CONTAINS_INVALID_CHARS));
      return;
    }

    pet.setName(_name);
    pet.broadcastPacket(new NpcInfo(pet, activeChar));
    activeChar.sendPacket(new PetInfo(pet));

    pet.updateEffectIcons(true);

    if ((pet instanceof L2PetInstance))
    {
      L2ItemInstance controlItem = pet.getOwner().getInventory().getItemByObjectId(pet.getControlItemId());
      if (controlItem != null)
      {
        controlItem.setCustomType2(1);
        controlItem.updateDatabase();
        InventoryUpdate iu = new InventoryUpdate();
        iu.addModifiedItem(controlItem);
        activeChar.sendPacket(iu);
      }
    }
  }

  public String getType()
  {
    return "[C] 89 RequestChangePetName";
  }
}
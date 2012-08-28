package net.sf.l2j.gameserver.network.clientpackets;

import java.nio.BufferUnderflowException;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.PetNameTable;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.NpcInfo;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public final class RequestChangePetName extends L2GameClientPacket
{
  private String _name;

  protected void readImpl()
  {
    try
    {
      _name = readS();
    }
    catch (BufferUnderflowException e)
    {
      _name = "no";
    }
  }

  protected void runImpl()
  {
    if (_name.equalsIgnoreCase("no")) {
      return;
    }
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    L2Summon pet = player.getPet();
    if (pet == null) {
      return;
    }
    if (pet.getName() != null)
    {
      player.sendPacket(Static.NAMING_YOU_CANNOT_SET_NAME_OF_THE_PET);
      return;
    }
    if (PetNameTable.getInstance().doesPetNameExist(_name, pet.getTemplate().npcId))
    {
      player.sendPacket(Static.NAMING_ALREADY_IN_USE_BY_ANOTHER_PET);
      return;
    }
    if ((_name.length() < 3) || (_name.length() > 16))
    {
      player.sendMessage("\u041D\u0435 \u0431\u043E\u043B\u0435\u0435 16 \u0441\u0438\u043C\u0432\u043E\u043B\u043E\u0432");
      return;
    }
    if (!PetNameTable.getInstance().isValidPetName(_name))
    {
      player.sendPacket(Static.NAMING_PETNAME_CONTAINS_INVALID_CHARS);
      return;
    }

    pet.setName(_name);
    pet.broadcastPacket(new NpcInfo(pet, player, 1));
    pet.updateAndBroadcastStatus(1);

    pet.updateEffectIcons(true);

    if (pet.isPet())
    {
      L2ItemInstance controlItem = pet.getOwner().getInventory().getItemByObjectId(pet.getControlItemId());
      if (controlItem != null)
      {
        controlItem.setCustomType2(1);
        controlItem.updateDatabase();
        InventoryUpdate iu = new InventoryUpdate();
        iu.addModifiedItem(controlItem);
        player.sendPacket(iu);
      }
    }
  }
}
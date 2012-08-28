package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Augmentation;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class RequestUnEquipItem extends L2GameClientPacket
{
  private static final String _C__11_REQUESTUNEQUIPITEM = "[C] 11 RequestUnequipItem";
  private static Logger _log = Logger.getLogger(RequestUnEquipItem.class.getName());
  private int _slot;

  protected void readImpl()
  {
    _slot = readD();
  }

  protected void runImpl()
  {
    if (Config.DEBUG) {
      _log.fine("request unequip slot " + _slot);
    }
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();

    if (activeChar == null)
      return;
    if (activeChar._haveFlagCTF) {
      activeChar.sendMessage("You can't unequip a CTF flag.");
      return;
    }
    L2ItemInstance item = activeChar.getInventory().getPaperdollItemByL2ItemId(_slot);
    if ((item != null) && (item.isWear()))
    {
      return;
    }

    if ((_slot == 16384) && (activeChar.isCursedWeaponEquiped()))
    {
      return;
    }

    if ((activeChar.isStunned()) || (activeChar.isSleeping()) || (activeChar.isMeditation()) || (activeChar.isParalyzed()) || (activeChar.isAlikeDead()))
    {
      activeChar.sendMessage("Your status does not allow you to do that.");
      return;
    }
    if ((activeChar.isAttackingNow()) || (activeChar.isCastingNow())) {
      return;
    }

    if ((item != null) && (item.isAugmented()))
    {
      item.getAugmentation().removeBoni(activeChar);
    }

    L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInBodySlotAndRecord(_slot);

    InventoryUpdate iu = new InventoryUpdate();

    for (int i = 0; i < unequiped.length; i++)
    {
      activeChar.checkSSMatch(null, unequiped[i]);

      iu.addModifiedItem(unequiped[i]);
    }

    activeChar.sendPacket(iu);

    activeChar.abortAttack();
    activeChar.broadcastUserInfo();

    if (unequiped.length > 0)
    {
      SystemMessage sm = null;
      if (unequiped[0].getEnchantLevel() > 0)
      {
        sm = new SystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
        sm.addNumber(unequiped[0].getEnchantLevel());
        sm.addItemName(unequiped[0].getItemId());
      }
      else
      {
        sm = new SystemMessage(SystemMessageId.S1_DISARMED);
        sm.addItemName(unequiped[0].getItemId());
      }
      activeChar.sendPacket(sm);
      sm = null;
    }
  }

  public String getType()
  {
    return "[C] 11 RequestUnequipItem";
  }
}
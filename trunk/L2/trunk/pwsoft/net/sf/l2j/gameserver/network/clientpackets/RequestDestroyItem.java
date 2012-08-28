package net.sf.l2j.gameserver.network.clientpackets;

import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.instancemanager.CursedWeaponsManager;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2PetDataTable;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance.SkillDat;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;

public final class RequestDestroyItem extends L2GameClientPacket
{
  private static final Logger _log = Logger.getLogger(RequestDestroyItem.class.getName());
  private int _objectId;
  private int _count;

  protected void readImpl()
  {
    _objectId = readD();
    _count = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }

    if (System.currentTimeMillis() - player.gCPK() < 400L) {
      return;
    }

    if ((player.isAlikeDead()) || (player.isAllSkillsDisabled()) || (player.isOutOfControl()) || (player.isParalyzed())) {
      return;
    }

    player.sCPK();

    if (_count <= 0)
    {
      return;
    }

    int count = _count;

    if (player.getPrivateStoreType() != 0) {
      player.sendPacket(Static.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
      return;
    }

    L2ItemInstance itemToRemove = player.getInventory().getItemByObjectId(_objectId);

    if (itemToRemove == null) {
      return;
    }

    if ((player.isCastingNow()) && 
      (player.getCurrentSkill() != null) && (player.getCurrentSkill().getSkill().getItemConsumeId() == itemToRemove.getItemId())) {
      player.sendPacket(Static.CANNOT_DISCARD_THIS_ITEM);
      return;
    }

    int itemId = itemToRemove.getItemId();
    if ((itemToRemove == null) || (itemToRemove.isWear()) || (!itemToRemove.isDestroyable()) || (CursedWeaponsManager.getInstance().isCursed(itemId))) {
      player.sendPacket(Static.CANNOT_DISCARD_THIS_ITEM);
      return;
    }

    if ((!itemToRemove.isStackable()) && (count > 1))
    {
      return;
    }

    if (_count > itemToRemove.getCount()) {
      count = itemToRemove.getCount();
    }

    if (itemToRemove.isEquipped()) {
      L2ItemInstance[] unequiped = player.getInventory().unEquipItemInSlotAndRecord(itemToRemove.getEquipSlot());

      InventoryUpdate iu = new InventoryUpdate();
      for (int i = 0; i < unequiped.length; i++) {
        iu.addModifiedItem(unequiped[i]);
      }
      player.sendPacket(iu);
    }

    if (L2PetDataTable.isPetItem(itemId)) {
      Connect con = null;
      PreparedStatement statement = null;
      try {
        if ((player.getPet() != null) && (player.getPet().getControlItemId() == _objectId)) {
          player.getPet().unSummon(player);
        }

        con = L2DatabaseFactory.getInstance().getConnection();
        statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");
        statement.setInt(1, _objectId);
        statement.execute();
      } catch (Exception e) {
        _log.log(Level.WARNING, "could not delete pet objectid: ", e);
      } finally {
        Close.CS(con, statement);
      }
    }

    if (itemToRemove.isAugmented()) {
      itemToRemove.removeAugmentation();
    }

    L2ItemInstance removedItem = player.getInventory().destroyItem("Destroy", _objectId, count, player, null);

    if (removedItem == null) {
      return;
    }

    if (!Config.FORCE_INVENTORY_UPDATE) {
      InventoryUpdate iu = new InventoryUpdate();
      if (removedItem.getCount() == 0)
        iu.addRemovedItem(removedItem);
      else {
        iu.addModifiedItem(removedItem);
      }

      player.sendPacket(iu);
    } else {
      player.sendItems(true);
    }

    player.sendChanges();
    player.broadcastUserInfo();
  }
}
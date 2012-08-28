package net.sf.l2j.gameserver.network.clientpackets;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.model.L2Augmentation;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Multisell;
import net.sf.l2j.gameserver.model.L2Multisell.MultiSellEntry;
import net.sf.l2j.gameserver.model.L2Multisell.MultiSellIngredient;
import net.sf.l2j.gameserver.model.L2Multisell.MultiSellListContainer;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Armor;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.util.FloodProtector;
import net.sf.l2j.gameserver.util.Util;

public class MultiSellChoose extends L2GameClientPacket
{
  private static final String _C__A7_MULTISELLCHOOSE = "[C] A7 MultiSellChoose";
  private static Logger _log = Logger.getLogger(MultiSellChoose.class.getName());
  private int _listId;
  private int _entryId;
  private int _amount;
  private int _enchantment;
  private int _transactionTax;
  private int _enchantCheck = 0;
  private L2NpcInstance merchant = null;

  protected void readImpl()
  {
    _listId = readD();
    _entryId = readD();
    _amount = readD();

    _enchantment = (_entryId % 100000);
    _entryId /= 100000;
    _transactionTax = 0;
  }

  public void runImpl()
  {
    if ((_amount < 1) || (_amount > Config.MAX_MULTISELL)) {
      return;
    }
    L2Multisell.MultiSellListContainer list = L2Multisell.getInstance().getList(_listId);
    if (list == null) return;

    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) return;

    merchant = ((player.getTarget() instanceof L2NpcInstance) ? (L2NpcInstance)player.getTarget() : null);

    if ((merchant == null) || (!player.isInsideRadius(merchant, 150, false, false))) {
      return;
    }
    for (L2Multisell.MultiSellEntry entry : list.getEntries())
    {
      if (entry.getEntryId() == _entryId)
      {
        doExchange(player, entry, list.getApplyTaxes(), list.getMaintainEnchantment(), _enchantment, list.getBidNpcId());
        return;
      }
    }
  }

  private void doExchange(L2PcInstance player, L2Multisell.MultiSellEntry templateEntry, boolean applyTaxes, boolean maintainEnchantment, int enchantment, String bidNpcId)
  {
    PcInventory inv = player.getInventory();
    boolean maintainItemFound = false;
    if (!FloodProtector.getInstance().tryPerformAction(player.getObjectId(), 12))
    {
      player.sendMessage("You can buy every 1 Second!.");
      return;
    }

    L2Multisell.MultiSellEntry entry = prepareEntry(merchant, templateEntry, applyTaxes, maintainEnchantment, enchantment);

    for (L2Multisell.MultiSellIngredient e : entry.getProducts())
    {
      if ((!ItemTable.getInstance().createDummyItem(e.getItemId()).isStackable()) && (_amount > player.GetInventoryLimit() - player.getInventory().getSize()))
      {
        player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
        return;
      }
    }

    if (bidNpcId != null)
    {
      List Result = new FastList();
      for (String id : bidNpcId.split(","))
      {
        Result.add(Integer.valueOf(Integer.parseInt(id)));
      }
      if (!Result.contains(Integer.valueOf(merchant.getNpcId())))
      {
        Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to use multisell Exploit!", Config.DEFAULT_PUNISH);
        return;
      }

    }

    FastList _ingredientsList = new FastList();
    boolean newIng = true;
    for (L2Multisell.MultiSellIngredient e : entry.getIngredients())
    {
      newIng = true;

      for (L2Multisell.MultiSellIngredient ex : _ingredientsList)
      {
        if ((ex.getItemId() == e.getItemId()) && (ex.getEnchantmentLevel() == e.getEnchantmentLevel()))
        {
          if (ex.getItemCount() + e.getItemCount() > 2147483647.0D) {
            player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
            _ingredientsList.clear();
            _ingredientsList = null;
            return;
          }
          ex.setItemCount(ex.getItemCount() + e.getItemCount());
          newIng = false;
        }
      }
      if (newIng)
      {
        if ((maintainEnchantment) || (e.getMantainIngredient()))
        {
          maintainItemFound = true;
        }
        L2Multisell tmp456_453 = L2Multisell.getInstance(); tmp456_453.getClass(); _ingredientsList.add(new L2Multisell.MultiSellIngredient(tmp456_453, e));
      }
    }
    if (!maintainItemFound)
    {
      for (L2Multisell.MultiSellIngredient product : entry.getProducts())
      {
        product.setEnchantmentLevel(0);
      }
    }

    for (L2Multisell.MultiSellIngredient e : _ingredientsList)
    {
      if (e.getItemCount() * _amount > 2147483647.0D)
      {
        player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
        _ingredientsList.clear();
        _ingredientsList = null;
        return;
      }

      if ((e.getItemId() != 65336) && (e.getItemId() != 65436))
      {
        if (inv.getInventoryItemCount(e.getItemId(), maintainEnchantment ? e.getEnchantmentLevel() : -1) < ((Config.ALT_BLACKSMITH_USE_RECIPES) || (!e.getMantainIngredient()) ? e.getItemCount() * _amount : e.getItemCount()))
        {
          player.sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
          _ingredientsList.clear();
          _ingredientsList = null;
          return;
        }
      }

      if (e.getItemId() == 65336)
      {
        if (player.getClan() == null)
        {
          player.sendPacket(new SystemMessage(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER));
          return;
        }
        if (!player.isClanLeader())
        {
          player.sendPacket(new SystemMessage(SystemMessageId.ONLY_THE_CLAN_LEADER_IS_ENABLED));
          return;
        }
        if (player.getClan().getReputationScore() < e.getItemCount() * _amount)
        {
          player.sendPacket(new SystemMessage(SystemMessageId.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW));
          return;
        }
      }

      if (e.getItemId() == 65436)
      {
        if (e.getItemCount() * _amount > player.getPcCafeScore())
        {
          player.sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
          return;
        }
      }
    }

    _ingredientsList.clear();
    _ingredientsList = null;
    FastList augmentation = new FastList();

    for (L2Multisell.MultiSellIngredient e : entry.getIngredients())
    {
      if ((e.getItemId() != 65336) && (e.getItemId() != 65436))
      {
        L2ItemInstance itemToTake = inv.getItemByItemId(e.getItemId());
        if (itemToTake == null)
        {
          _log.severe("Character: " + player.getName() + " is trying to cheat in multisell, merchatnt id:" + merchant.getNpcId());
          return;
        }

        if (itemToTake.isWear())
        {
          _log.severe("Character: " + player.getName() + " is trying to cheat in multisell, merchatnt id:" + merchant.getNpcId());
          return;
        }

        L2Item tempItem = ItemTable.getInstance().createDummyItem(e.getItemId()).getItem();
        if (((tempItem instanceof L2Armor)) || ((tempItem instanceof L2Weapon))) {
          _enchantCheck = e.getEnchantmentLevel();
        }

        if ((Config.ALT_BLACKSMITH_USE_RECIPES) || (!e.getMantainIngredient()))
        {
          if (itemToTake.isStackable())
          {
            if (!player.destroyItem("Multisell", itemToTake.getObjectId(), e.getItemCount() * _amount, player.getTarget(), true)) {
              return;
            }

          }
          else if (maintainEnchantment)
          {
            L2ItemInstance[] inventoryContents = inv.getAllItemsByItemId(e.getItemId(), e.getEnchantmentLevel());
            for (int i = 0; i < e.getItemCount() * _amount; i++)
            {
              if (inventoryContents[i].isAugmented())
                augmentation.add(inventoryContents[i].getAugmentation());
              if (!player.destroyItem("Multisell", inventoryContents[i].getObjectId(), 1, player.getTarget(), true)) {
                return;
              }
            }
          }
          else
          {
            for (int i = 1; i <= e.getItemCount() * _amount; i++)
            {
              L2ItemInstance[] inventoryContents = inv.getAllItemsByItemId(e.getItemId());

              itemToTake = inventoryContents[0];

              if (itemToTake.getEnchantLevel() > 0)
              {
                for (int j = 0; j < inventoryContents.length; j++)
                {
                  if (inventoryContents[j].getEnchantLevel() >= itemToTake.getEnchantLevel())
                    continue;
                  itemToTake = inventoryContents[j];

                  if (itemToTake.getEnchantLevel() == 0) {
                    break;
                  }
                }
              }
              if (!player.destroyItem("Multisell", itemToTake.getObjectId(), 1, player.getTarget(), true)) {
                return;
              }
            }
          }

        }

      }
      else if (e.getItemId() == 65336)
      {
        int repCost = player.getClan().getReputationScore() - e.getItemCount() * _amount;
        player.getClan().setReputationScore(repCost, true);
        SystemMessage smsg = new SystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
        smsg.addNumber(e.getItemCount());
        player.sendPacket(smsg);
        player.getClan().broadcastToOnlineMembers(new PledgeShowInfoUpdate(player.getClan()));
      }
      else
      {
        player.reducePcCafeScore(e.getItemCount() * _amount);
        player.sendPacket(new SystemMessage(1709).addNumber(e.getItemCount()));
      }

    }

    for (L2Multisell.MultiSellIngredient e : entry.getProducts())
    {
      if (_enchantCheck < e.getEnchantmentLevel())
      {
        L2Item tempItem = ItemTable.getInstance().createDummyItem(e.getItemId()).getItem();
        if (((tempItem instanceof L2Armor)) || ((tempItem instanceof L2Weapon)))
        {
          player.sendMessage("Ohh Cheat dont work? You have a problem now!");
          Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " trying to enchant cheat.", Config.DEFAULT_PUNISH);
          return;
        }
      }

      if (ItemTable.getInstance().createDummyItem(e.getItemId()).isStackable())
      {
        inv.addItem("Multisell", e.getItemId(), e.getItemCount() * _amount, player, player.getTarget());
      }
      else {
        L2ItemInstance product = null;
        for (int i = 0; i < e.getItemCount() * _amount; i++)
        {
          product = inv.addItem("Multisell", e.getItemId(), 1, player, player.getTarget());

          if (Config.ENABLE_MODIFY_ENCHANT_MULTISELL)
          {
            if (Config.ENCHANT_MULTISELL_LIST.containsKey(Integer.valueOf(_listId)))
            {
              product.setEnchantLevel(((Integer)Config.ENCHANT_MULTISELL_LIST.get(Integer.valueOf(_listId))).intValue());
            }
          }

          if (!maintainEnchantment)
            continue;
          if (i < augmentation.size())
          {
            product.setAugmentation(new L2Augmentation(product, ((L2Augmentation)augmentation.get(i)).getAugmentationId(), ((L2Augmentation)augmentation.get(i)).getSkill(), true));
          }
          product.setEnchantLevel(e.getEnchantmentLevel());
        }

      }

      if (e.getItemCount() * _amount > 1)
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
        sm.addItemName(e.getItemId());
        sm.addNumber(e.getItemCount() * _amount);
        player.sendPacket(sm);
        sm = null;
      }
      else
      {
        if (_enchantment > 0)
        {
          SystemMessage sm = new SystemMessage(SystemMessageId.ACQUIRED);
          sm.addNumber(_enchantment);
          sm.addItemName(e.getItemId());
        }
        else
        {
          sm = new SystemMessage(SystemMessageId.EARNED_ITEM);
          sm.addItemName(e.getItemId());
        }
        player.sendPacket(sm);
        SystemMessage sm = null;
      }
    }
    player.sendPacket(new ItemList(player, false));

    StatusUpdate su = new StatusUpdate(player.getObjectId());
    su.addAttribute(14, player.getCurrentLoad());
    player.sendPacket(su);
    su = null;

    if ((merchant != null) && (merchant.getIsInTown()) && (merchant.getCastle().getOwnerId() > 0))
      merchant.getCastle().addToTreasury(_transactionTax * _amount);
  }

  private L2Multisell.MultiSellEntry prepareEntry(L2NpcInstance merchant, L2Multisell.MultiSellEntry templateEntry, boolean applyTaxes, boolean maintainEnchantment, int enchantLevel)
  {
    L2Multisell tmp7_4 = L2Multisell.getInstance(); tmp7_4.getClass(); L2Multisell.MultiSellEntry newEntry = new L2Multisell.MultiSellEntry(tmp7_4);
    newEntry.setEntryId(templateEntry.getEntryId());
    int totalAdenaCount = 0;

    for (L2Multisell.MultiSellIngredient ing : templateEntry.getIngredients())
    {
      L2Multisell tmp69_66 = L2Multisell.getInstance(); tmp69_66.getClass(); L2Multisell.MultiSellIngredient newIngredient = new L2Multisell.MultiSellIngredient(tmp69_66, ing);

      if ((newIngredient.getItemId() == 57) && (newIngredient.isTaxIngredient()))
      {
        double taxRate = 0.0D;
        if (applyTaxes)
        {
          if ((merchant != null) && (merchant.getIsInTown())) {
            taxRate = merchant.getCastle().getTaxRate();
          }
        }
        _transactionTax = (int)Math.round(newIngredient.getItemCount() * taxRate);
        totalAdenaCount += _transactionTax;
        continue;
      }
      if (ing.getItemId() == 57)
      {
        totalAdenaCount += newIngredient.getItemCount();
        continue;
      }

      if (maintainEnchantment)
      {
        L2Item tempItem = ItemTable.getInstance().createDummyItem(newIngredient.getItemId()).getItem();
        if (((tempItem instanceof L2Armor)) || ((tempItem instanceof L2Weapon))) {
          newIngredient.setEnchantmentLevel(enchantLevel);
        }
      }

      newEntry.addIngredient(newIngredient);
    }

    if (totalAdenaCount > 0)
    {
      L2Multisell tmp246_243 = L2Multisell.getInstance(); tmp246_243.getClass(); newEntry.addIngredient(new L2Multisell.MultiSellIngredient(tmp246_243, 57, totalAdenaCount, false, false));
    }

    for (L2Multisell.MultiSellIngredient ing : templateEntry.getProducts())
    {
      L2Multisell tmp303_300 = L2Multisell.getInstance(); tmp303_300.getClass(); L2Multisell.MultiSellIngredient newIngredient = new L2Multisell.MultiSellIngredient(tmp303_300, ing);

      if (maintainEnchantment)
      {
        L2Item tempItem = ItemTable.getInstance().createDummyItem(newIngredient.getItemId()).getItem();
        if (((tempItem instanceof L2Armor)) || ((tempItem instanceof L2Weapon)))
          newIngredient.setEnchantmentLevel(enchantLevel);
      }
      newEntry.addProduct(newIngredient);
    }
    return newEntry;
  }

  public String getType()
  {
    return "[C] A7 MultiSellChoose";
  }
}
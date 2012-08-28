package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Multisell;
import net.sf.l2j.gameserver.model.L2Multisell.MultiSellEntry;
import net.sf.l2j.gameserver.model.L2Multisell.MultiSellIngredient;
import net.sf.l2j.gameserver.model.L2Multisell.MultiSellListContainer;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Armor;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.util.Log;

public class MultiSellChoose extends L2GameClientPacket
{
  private static final Logger _log = Logger.getLogger(MultiSellChoose.class.getName());
  private int _listId;
  private int _entryId;
  private int _amount;
  private int _enchantment;
  private int _transactionTax;
  private int _sevaEnch = 0;

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
    if ((_amount < 1) || (_amount > 5000)) {
      return;
    }

    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }

    if (System.currentTimeMillis() - player.gCPAU() < 300L) {
      return;
    }

    player.sCPAU();

    L2Multisell.MultiSellListContainer list = L2Multisell.getInstance().getList(_listId);
    if (list == null) {
      _log.warning(new StringBuilder().append("[L2Multisell] can't find list with id: ").append(_listId).toString());
      player.kick();
      return;
    }

    if (player.isParalyzed()) {
      return;
    }

    int npcId = 1013;
    if ((Config.MULTISSELL_PROTECT) && 
      (!list.containsNpc(npcId))) {
      L2Object object = player.getTarget();
      if ((object == null) || (!object.isL2Npc())) {
        return;
      }

      L2NpcInstance npc = (L2NpcInstance)object;
      if (!player.isInsideRadius(npc, 120, false, false)) {
        return;
      }

      npcId = npc.getNpcId();
      if (!list.containsNpc(npcId))
      {
        _log.warning(new StringBuilder().append("Player ").append(player.getName()).append(" tryed to cheat with multisell list ").append(_listId).append(" (NpcId: ").append(npcId).append(")").toString());
        return;
      }

    }

    if ((list.getTicketId() > 0) && (player.getItemCount(list.getTicketId()) == 0)) {
      player.sendHtmlMessage("\u041C\u0430\u0433\u0430\u0437\u0438\u043D", "\u0414\u043B\u044F \u043F\u043E\u043A\u0443\u043F\u043A\u0438 \u043D\u0443\u0436\u0435\u043D \u043E\u0441\u043E\u0431\u044B\u0439 \u043F\u0440\u043E\u043F\u0443\u0441\u043A.");
      return;
    }

    for (L2Multisell.MultiSellEntry entry : list.getEntries())
      if (entry.getEntryId() == _entryId) {
        doExchange(player, entry, list.getApplyTaxes(), list.getMaintainEnchantment(), _enchantment, list.getEnchant(), npcId, list.saveEnchantment());
        return;
      }
  }

  private void doExchange(L2PcInstance player, L2Multisell.MultiSellEntry templateEntry, boolean applyTaxes, boolean maintainEnchantment, int enchantment, int listEnch, int npcId, boolean saveEnch)
  {
    PcInventory inv = player.getInventory();
    boolean maintainItemFound = false;

    L2NpcInstance merchant = null;
    if (npcId != 1013) {
      merchant = player.getTarget().isL2Npc() ? (L2NpcInstance)player.getTarget() : null;
      if (merchant == null) {
        return;
      }
    }

    ItemTable itemTable = ItemTable.getInstance();
    L2Multisell.MultiSellEntry entry = prepareEntry(merchant, templateEntry, applyTaxes, saveEnch, enchantment);
    int slots = 0;
    for (L2Multisell.MultiSellIngredient e : entry.getProducts()) {
      L2Item template = itemTable.getTemplate(e.getItemId());
      if (template == null)
      {
        continue;
      }
      if (!template.isStackable())
        slots += e.getItemCount() * _amount;
      else if (player.getInventory().getItemByItemId(e.getItemId()) == null) {
        slots++;
      }
    }

    if ((slots > 2147483647) || (slots < 0) || (!player.getInventory().validateCapacity(slots))) {
      sendPacket(Static.SLOTS_FULL);
      return;
    }

    FastList _ingredientsList = new FastList();
    boolean newIng = true;
    for (L2Multisell.MultiSellIngredient e : entry.getIngredients()) {
      newIng = true;

      for (L2Multisell.MultiSellIngredient ex : _ingredientsList)
      {
        if ((ex.getItemId() == e.getItemId()) && (ex.getEnchantmentLevel() == e.getEnchantmentLevel())) {
          if (ex.getItemCount() + e.getItemCount() > 2147483647.0D) {
            player.sendPacket(Static.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
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
        if ((saveEnch) && (e.getMantainIngredient())) {
          maintainItemFound = true;
        }

        _ingredientsList.add(new L2Multisell.MultiSellIngredient(e));
      }

    }

    if (!maintainItemFound) {
      for (L2Multisell.MultiSellIngredient product : entry.getProducts()) {
        product.setEnchantmentLevel(0);
      }

    }

    for (L2Multisell.MultiSellIngredient e : _ingredientsList) {
      if (e.getItemCount() * _amount > 2147483647.0D) {
        player.sendPacket(Static.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
        _ingredientsList.clear();
        _ingredientsList = null;
        return;
      }

      switch (e.getItemId()) {
      case 65336:
        if (player.getClan() == null) {
          player.sendPacket(Static.YOU_ARE_NOT_A_CLAN_MEMBER);
          return;
        }
        if (!player.isClanLeader()) {
          player.sendPacket(Static.ONLY_THE_CLAN_LEADER_IS_ENABLED);
          return;
        }
        if (player.getClan().getReputationScore() >= e.getItemCount() * _amount) break;
        player.sendPacket(Static.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
        return;
      case 65436:
        if (player.getPcPoints() >= e.getItemCount() * _amount) break;
        player.sendPacket(Static.NOT_ENOUGH_PCPOINTS);
        return;
      default:
        if (inv.getInventoryItemCount(e.getItemId(), saveEnch ? e.getEnchantmentLevel() : -1) >= ((Config.ALT_BLACKSMITH_USE_RECIPES) || (!e.getMantainIngredient()) ? e.getItemCount() * _amount : e.getItemCount())) break;
        player.sendPacket(Static.NOT_ENOUGH_ITEMS);
        _ingredientsList.clear();
        _ingredientsList = null;
        return;
      }

    }

    _ingredientsList.clear();
    _ingredientsList = null;

    for (L2Multisell.MultiSellIngredient e : entry.getIngredients()) {
      int totalCount = e.getItemCount() * _amount;
      switch (e.getItemId()) {
      case 65336:
        int repCost = player.getClan().getReputationScore() - totalCount;
        player.getClan().setReputationScore(repCost, true);
        player.getClan().broadcastToOnlineMembers(new PledgeShowInfoUpdate(player.getClan()));
        player.sendPacket(SystemMessage.id(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP).addNumber(totalCount));
        break;
      case 65436:
        player.updatePcPoints(-totalCount, 1, false);
        break;
      default:
        L2ItemInstance itemToTake = inv.getItemByItemId(e.getItemId());
        if (itemToTake == null) {
          _log.severe(new StringBuilder().append("Character: ").append(player.getName()).append(" is trying to cheat in multisell, merchatnt id:").append(merchant == null ? npcId : merchant.getNpcId()).toString());
          return;
        }

        if ((!Config.ALT_BLACKSMITH_USE_RECIPES) && (e.getMantainIngredient()))
          break;
        if (itemToTake.isStackable()) {
          if (player.destroyItem("Multisell", itemToTake.getObjectId(), totalCount, player.getTarget(), true)) break;
          return;
        }
        else if (saveEnch)
        {
          L2ItemInstance llop = null;
          L2ItemInstance[] inventoryContents = inv.getAllItemsByItemId(e.getItemId(), e.getEnchantmentLevel());
          for (int i = 0; i < totalCount; i++)
          {
            llop = inventoryContents[i];
            if (llop == null)
            {
              continue;
            }
            if ((llop.getEnchantLevel() > 0) && (((llop.getItem() instanceof L2Armor)) || ((llop.getItem() instanceof L2Weapon)))) {
              _sevaEnch = Math.max(_sevaEnch, llop.getEnchantLevel());
            }

            if (!player.destroyItem("Multisell", llop.getObjectId(), 1, player.getTarget(), true)) {
              return;
            }

          }

        }
        else
        {
          for (int i = 1; i <= totalCount; i++) {
            L2ItemInstance[] inventoryContents = inv.getAllItemsByItemId(e.getItemId());

            itemToTake = inventoryContents[0];

            if (itemToTake.getEnchantLevel() > 0) {
              for (int j = 0; j < inventoryContents.length; j++) {
                if (inventoryContents[j].getEnchantLevel() < itemToTake.getEnchantLevel()) {
                  itemToTake = inventoryContents[j];

                  if (itemToTake.getEnchantLevel() == 0)
                  {
                    break;
                  }
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

    String date = "";

    TextBuilder tb = null;
    boolean logItems = Config.LOG_MULTISELL_ID.contains(Integer.valueOf(_listId));
    if (logItems) {
      date = Log.getTime();
      tb = new TextBuilder();
    }
    for (L2Multisell.MultiSellIngredient e : entry.getProducts()) {
      int ench = 0;
      int count = e.getItemCount() * _amount;
      L2ItemInstance product = null;

      if (itemTable.createDummyItem(e.getItemId()).isStackable()) {
        product = inv.addItem("Multisell", e.getItemId(), count, player, player.getTarget());
        if ((logItems) && (product != null)) {
          String act = new StringBuilder().append("MULTISELL ").append(product.getItemName()).append("(").append(count).append(")(+0)(").append(product.getObjectId()).append(")(npc:").append(merchant == null ? "null" : Integer.valueOf(merchant.getTemplate().npcId)).append(") #(player ").append(player.getName()).append(", account: ").append(player.getAccountName()).append(", ip: ").append(player.getIP()).append(")").toString();
          tb.append(new StringBuilder().append(date).append(act).append("\n").toString());
        }
      } else {
        for (int i = 0; i < count; i++) {
          product = inv.addItem("Multisell", e.getItemId(), 1, player, player.getTarget());
          if (product == null)
          {
            continue;
          }

          int itemType = product.getItem().getType2();
          if ((product.canBeEnchanted()) && (product.isDestroyable()) && ((itemType == 0) || (itemType == 1) || (itemType == 2))) {
            ench = _sevaEnch > 0 ? _sevaEnch : listEnch;
            product.setEnchantLevel(ench);
          }
          if (logItems) {
            String act = new StringBuilder().append("MULTISELL ").append(product.getItemName()).append("(1)(+").append(ench).append(")(").append(product.getObjectId()).append(")(npc:").append(merchant == null ? "null" : Integer.valueOf(merchant.getTemplate().npcId)).append(") #(player ").append(player.getName()).append(", account: ").append(player.getAccountName()).append(", ip: ").append(player.getIP()).append(", hwid: ").append(player.getHWID()).append(")").toString();
            tb.append(new StringBuilder().append(date).append(act).append("\n").toString());
          }
        }
      }
      SystemMessage sm;
      SystemMessage sm;
      if (count > 1) {
        sm = SystemMessage.id(SystemMessageId.EARNED_S2_S1_S).addItemName(e.getItemId()).addNumber(count);
      }
      else
      {
        SystemMessage sm;
        if (ench > 0)
          sm = SystemMessage.id(SystemMessageId.ACQUIRED).addNumber(ench).addItemName(e.getItemId());
        else {
          sm = SystemMessage.id(SystemMessageId.EARNED_ITEM).addItemName(e.getItemId());
        }
      }
      player.sendPacket(sm);
    }
    if ((logItems) && (tb != null)) {
      Log.item(tb.toString(), 3);
      tb.clear();
      tb = null;
    }
    SystemMessage sm = null;

    player.sendItems(false);
    player.sendChanges();

    if (saveEnch)
    {
      L2Multisell.getInstance().SeparateAndSend(_listId, player, true, 0.0D);
    }

    if ((merchant != null) && (merchant.getIsInTown()) && (merchant.getCastle().getOwnerId() > 0))
      merchant.getCastle().addToTreasury(_transactionTax * _amount);
  }

  private L2Multisell.MultiSellEntry prepareEntry(L2NpcInstance merchant, L2Multisell.MultiSellEntry templateEntry, boolean applyTaxes, boolean maintainEnchantment, int enchantLevel)
  {
    L2Multisell.MultiSellEntry newEntry = new L2Multisell.MultiSellEntry();
    newEntry.setEntryId(templateEntry.getEntryId());
    int totalAdenaCount = 0;
    boolean hasIngredient = false;

    for (L2Multisell.MultiSellIngredient ing : templateEntry.getIngredients())
    {
      L2Multisell.MultiSellIngredient newIngredient = new L2Multisell.MultiSellIngredient(ing);

      if ((newIngredient.getItemId() == 57) && (newIngredient.isTaxIngredient())) {
        double taxRate = 0.0D;
        if ((applyTaxes) && 
          (merchant != null) && (merchant.getIsInTown())) {
          taxRate = merchant.getCastle().getTaxRate();
        }

        _transactionTax = (int)Math.round(newIngredient.getItemCount() * taxRate);
        totalAdenaCount += _transactionTax;
        continue;
      }if (ing.getItemId() == 57)
      {
        totalAdenaCount += newIngredient.getItemCount();
        continue;
      }

      if ((maintainEnchantment) && (newIngredient.getItemId() > 0)) {
        L2Item tempItem = ItemTable.getInstance().createDummyItem(newIngredient.getItemId()).getItem();
        if (((tempItem instanceof L2Armor)) || ((tempItem instanceof L2Weapon))) {
          newIngredient.setEnchantmentLevel(enchantLevel);
          hasIngredient = true;
        }

      }

      newEntry.addIngredient(newIngredient);
    }

    if (totalAdenaCount > 0) {
      newEntry.addIngredient(new L2Multisell.MultiSellIngredient(57, totalAdenaCount, false, false));
    }

    for (L2Multisell.MultiSellIngredient ing : templateEntry.getProducts())
    {
      L2Multisell.MultiSellIngredient newIngredient = new L2Multisell.MultiSellIngredient(ing);

      if ((maintainEnchantment) && (hasIngredient))
      {
        L2Item tempItem = ItemTable.getInstance().createDummyItem(newIngredient.getItemId()).getItem();
        if (((tempItem instanceof L2Armor)) || ((tempItem instanceof L2Weapon))) {
          newIngredient.setEnchantmentLevel(enchantLevel);
        }
      }
      newEntry.addProduct(newIngredient);
    }
    return newEntry;
  }
}
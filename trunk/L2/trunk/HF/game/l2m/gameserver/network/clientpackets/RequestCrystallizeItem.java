package l2m.gameserver.network.clientpackets;

import l2m.gameserver.cache.Msg;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.items.PcInventory;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.templates.item.ItemTemplate;
import l2m.gameserver.templates.item.ItemTemplate.Grade;
import l2m.gameserver.utils.ItemFunctions;
import l2m.gameserver.utils.Log;

public class RequestCrystallizeItem extends L2GameClientPacket
{
  private int _objectId;
  private long unk;

  protected void readImpl()
  {
    _objectId = readD();
    unk = readQ();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();

    if (activeChar == null) {
      return;
    }
    if (activeChar.isActionsDisabled())
    {
      activeChar.sendActionFailed();
      return;
    }

    if (activeChar.isInStoreMode())
    {
      activeChar.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
      return;
    }

    if (activeChar.isInTrade())
    {
      activeChar.sendActionFailed();
      return;
    }

    ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
    if (item == null)
    {
      activeChar.sendActionFailed();
      return;
    }

    if (item.isHeroWeapon())
    {
      activeChar.sendPacket(Msg.HERO_WEAPONS_CANNOT_BE_DESTROYED);
      return;
    }

    if (!item.canBeCrystallized(activeChar))
    {
      activeChar.sendActionFailed();
      return;
    }

    if (activeChar.isInStoreMode())
    {
      activeChar.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
      return;
    }

    if (activeChar.isFishing())
    {
      activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
      return;
    }

    if (activeChar.isInTrade())
    {
      activeChar.sendActionFailed();
      return;
    }

    int crystalAmount = item.getTemplate().getCrystalCount();
    int crystalId = item.getTemplate().getCrystalType().cry;

    int level = activeChar.getSkillLevel(Integer.valueOf(248));
    if ((level < 1) || (crystalId - 1458 + 1 > level))
    {
      activeChar.sendPacket(Msg.CANNOT_CRYSTALLIZE_CRYSTALLIZATION_SKILL_LEVEL_TOO_LOW);
      activeChar.sendActionFailed();
      return;
    }

    Log.LogItem(activeChar, "Crystalize", item);

    if (!activeChar.getInventory().destroyItemByObjectId(_objectId, 1L))
    {
      activeChar.sendActionFailed();
      return;
    }

    activeChar.sendPacket(Msg.THE_ITEM_HAS_BEEN_SUCCESSFULLY_CRYSTALLIZED);
    ItemFunctions.addItem(activeChar, crystalId, crystalAmount, true);
    activeChar.sendChanges();
  }
}
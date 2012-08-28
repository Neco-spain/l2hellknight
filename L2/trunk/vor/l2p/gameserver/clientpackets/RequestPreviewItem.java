package l2p.gameserver.clientpackets;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import l2p.commons.threading.RunnableImpl;
import l2p.gameserver.Config;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.data.xml.holder.BuyListHolder;
import l2p.gameserver.data.xml.holder.BuyListHolder.NpcTradeList;
import l2p.gameserver.data.xml.holder.ItemHolder;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.Race;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.model.items.Inventory;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ShopPreviewInfo;
import l2p.gameserver.serverpackets.ShopPreviewList;
import l2p.gameserver.templates.item.ArmorTemplate.ArmorType;
import l2p.gameserver.templates.item.ItemTemplate;
import l2p.gameserver.templates.item.WeaponTemplate.WeaponType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestPreviewItem extends L2GameClientPacket
{
  private static final Logger _log = LoggerFactory.getLogger(RequestPreviewItem.class);
  private int _unknow;
  private int _listId;
  private int _count;
  private int[] _items;

  protected void readImpl()
  {
    _unknow = readD();
    _listId = readD();
    _count = readD();
    if ((_count * 4 > _buf.remaining()) || (_count > 32767) || (_count < 1))
    {
      _count = 0;
      return;
    }
    _items = new int[_count];
    for (int i = 0; i < _count; i++)
      _items[i] = readD();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if ((activeChar == null) || (_count == 0)) {
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

    if ((!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP) && (activeChar.getKarma() > 0) && (!activeChar.isGM()))
    {
      activeChar.sendActionFailed();
      return;
    }

    NpcInstance merchant = activeChar.getLastNpc();
    boolean isValidMerchant = (merchant != null) && (merchant.isMerchantNpc());
    if ((!activeChar.isGM()) && ((merchant == null) || (!isValidMerchant) || (!activeChar.isInRange(merchant, 200L))))
    {
      activeChar.sendActionFailed();
      return;
    }

    BuyListHolder.NpcTradeList list = BuyListHolder.getInstance().getBuyList(_listId);
    if (list == null)
    {
      activeChar.sendActionFailed();
      return;
    }

    int slots = 0;
    long totalPrice = 0L;

    Map itemList = new HashMap();
    try
    {
      for (int i = 0; i < _count; i++)
      {
        int itemId = _items[i];
        if (list.getItemByItemId(itemId) == null)
        {
          activeChar.sendActionFailed();
          return;
        }

        ItemTemplate template = ItemHolder.getInstance().getTemplate(itemId);
        if (template == null) {
          continue;
        }
        if (!template.isEquipable()) {
          continue;
        }
        int paperdoll = Inventory.getPaperdollIndex(template.getBodyPart());
        if (paperdoll < 0) {
          continue;
        }
        if (activeChar.getRace() == Race.kamael ? 
          (template.getItemType() == ArmorTemplate.ArmorType.HEAVY) && (template.getItemType() == ArmorTemplate.ArmorType.MAGIC) && (template.getItemType() == ArmorTemplate.ArmorType.SIGIL) && (template.getItemType() == WeaponTemplate.WeaponType.NONE) : 
          (template.getItemType() == WeaponTemplate.WeaponType.CROSSBOW) || (template.getItemType() == WeaponTemplate.WeaponType.RAPIER) || (template.getItemType() == WeaponTemplate.WeaponType.ANCIENTSWORD))
        {
          continue;
        }
        if (itemList.containsKey(Integer.valueOf(paperdoll)))
        {
          activeChar.sendPacket(Msg.THOSE_ITEMS_MAY_NOT_BE_TRIED_ON_SIMULTANEOUSLY);
          return;
        }

        itemList.put(Integer.valueOf(paperdoll), Integer.valueOf(itemId));

        totalPrice += ShopPreviewList.getWearPrice(template);
      }

      if (!activeChar.reduceAdena(totalPrice))
      {
        activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
        return;
      }

    }
    catch (ArithmeticException ae)
    {
      sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
      return;
    }

    if (!itemList.isEmpty())
    {
      activeChar.sendPacket(new ShopPreviewInfo(itemList));

      ThreadPoolManager.getInstance().schedule(new RemoveWearItemsTask(activeChar), Config.WEAR_DELAY * 1000);
    }
  }

  private static class RemoveWearItemsTask extends RunnableImpl
  {
    private Player _activeChar;

    public RemoveWearItemsTask(Player activeChar) {
      _activeChar = activeChar;
    }

    public void runImpl() throws Exception
    {
      _activeChar.sendPacket(Msg.TRYING_ON_MODE_HAS_ENDED);
      _activeChar.sendUserInfo(true);
    }
  }
}
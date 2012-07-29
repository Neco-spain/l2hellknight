package handler.items;

import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.Playable;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.Element;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.serverpackets.ExChooseInventoryAttributeItem;
import l2p.gameserver.templates.item.ItemTemplate;
import l2p.gameserver.utils.ItemFunctions;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SYS
 */
public class AttributeStones extends ScriptItemHandler {
    private static final int[] _itemIds = {
            9546,
            9547,
            9548,
            9549,
            9550,
            9551,
            9552,
            9553,
            9554,
            9555,
            9556,
            9557,
            10521,
            10522,
            10523,
            10524,
            10525,
            10526};

    @Override
    public boolean useItem(Playable playable, ItemInstance item, boolean ctrl) {
        if (playable == null || !playable.isPlayer())
            return false;
        Player player = (Player) playable;

        if (player.getPrivateStoreType() != Player.STORE_PRIVATE_NONE) {
            player.sendPacket(Msg.YOU_CANNOT_ADD_ELEMENTAL_POWER_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
            return false;
        }

        if (player.getEnchantScroll() != null)
            return false;

        player.setEnchantScroll(item);
        player.sendPacket(Msg.PLEASE_SELECT_ITEM_TO_ADD_ELEMENTAL_POWER);

        List<Integer> attributable_items = new ArrayList<Integer>();
        for (ItemInstance player_item : player.getInventory().getItems()) {
            ItemTemplate itemTemplate = player_item.getTemplate();
            if (player_item.isStackable() || !player_item.canBeEnchanted(true)
                    || player_item.getCrystalType().externalOrdinal < 5 || itemTemplate.isUnderwear()
                    || itemTemplate.isCloak() || itemTemplate.isBracelet() || itemTemplate.isBelt()
                    || !itemTemplate.isAttributable() || (!player_item.isArmor() && !player_item.isWeapon())) {
                continue;
            }

            if (player_item.getLocation() != ItemInstance.ItemLocation.INVENTORY && player_item.getLocation() != ItemInstance.ItemLocation.PAPERDOLL) {
                continue;
            }

            Element element = ItemFunctions.getEnchantAttributeStoneElement(item.getItemId(), player_item.isArmor());
            if (player_item.isArmor() && player_item.getAttributeElementValue(Element.getReverseElement(element), false) != 0) {
                continue;
            } else if (player_item.isWeapon() && player_item.getAttributeElement() != Element.NONE
                    && player_item.getAttributeElement() != element) {
                continue;
            }

            int maxValue = player_item.isWeapon() ? 150 : 60;
            if (item.getTemplate().isAttributeCrystal()) {
                maxValue += player_item.isWeapon() ? 150 : 60;
            }
            if (player_item.getAttributeElementValue(element, false) >= maxValue) {
                continue;
            }
            attributable_items.add(player_item.getObjectId());
        }


        player.sendPacket(new ExChooseInventoryAttributeItem(attributable_items, item));
        return true;
    }

    @Override
    public final int[] getItemIds() {
        return _itemIds;
    }
}
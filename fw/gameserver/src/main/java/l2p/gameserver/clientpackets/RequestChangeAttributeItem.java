package l2p.gameserver.clientpackets;

import l2p.commons.dao.JdbcEntityState;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.Element;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.serverpackets.ExChangeAttributeOk;
import l2p.gameserver.serverpackets.InventoryUpdate;

public class RequestChangeAttributeItem extends L2GameClientPacket {
    public int _consumeItemId;
    public int _itemObjId;
    public int _newElementId;

    protected void readImpl() {
        _consumeItemId = readD();
        _itemObjId = readD();
        _newElementId = readD();
    }

    protected void runImpl() {
        Player activeChar = (getClient()).getActiveChar();
        if (activeChar == null) {
            return;
        }
        PcInventory inventory = activeChar.getInventory();
        ItemInstance _item = inventory.getItemByObjectId(_itemObjId);

        boolean equipped = false;
        if ((equipped = _item.isEquipped())) {
            activeChar.getInventory().isRefresh = true;
            activeChar.getInventory().unEquipItem(_item);
        }

        Element oldElement = _item.getAttackElement();
        int elementVal = _item.getAttributeElementValue(oldElement, false);
        _item.setAttributeElement(oldElement, 0);

        Element newElement = Element.VALUES[_newElementId];
        _item.setAttributeElement(newElement, _item.getAttributeElementValue(newElement, false) + elementVal);

        _item.setJdbcState(JdbcEntityState.UPDATED);
        _item.update();

        if (equipped) {
            activeChar.getInventory().equipItem(_item);
            activeChar.getInventory().isRefresh = false;
        }
        activeChar.sendPacket(new InventoryUpdate().addModifiedItem(_item));
        activeChar.sendPacket(new ExChangeAttributeOk());
    }
}
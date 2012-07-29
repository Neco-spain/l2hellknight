package l2p.gameserver.clientpackets;

import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.serverpackets.ExPutIntensiveResultForVariationMake;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.templates.item.ItemTemplate.Grade;
import l2p.gameserver.utils.ItemFunctions;

public class RequestConfirmRefinerItem extends L2GameClientPacket {
    private static final int GEMSTONE_D = 2130;
    private static final int GEMSTONE_C = 2131;
    private static final int GEMSTONE_B = 2132;

    // format: (ch)dd
    private int _targetItemObjId;
    private int _refinerItemObjId;

    @Override
    protected void readImpl() {
        _targetItemObjId = readD();
        _refinerItemObjId = readD();
    }

    @Override
    protected void runImpl() {
        Player activeChar = getClient().getActiveChar();
        if (activeChar == null)
            return;

        ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_targetItemObjId);
        ItemInstance refinerItem = activeChar.getInventory().getItemByObjectId(_refinerItemObjId);

        if (targetItem == null || refinerItem == null) {
            activeChar.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM);
            return;
        }

        int refinerItemId = refinerItem.getTemplate().getItemId();

        boolean isAccessoryLifeStone = ItemFunctions.isAccessoryLifeStone(refinerItem.getItemId());
        if (!targetItem.canBeAugmented(activeChar, isAccessoryLifeStone)) {
            activeChar.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM);
            return;
        }

        if (!isAccessoryLifeStone && !ItemFunctions.isLifeStone(refinerItem.getItemId())) {
            activeChar.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM);
            return;
        }

        Grade itemGrade = targetItem.getTemplate().getItemGrade();

        int gemstoneCount = 0;
        int gemstoneItemId = 0;

        if (isAccessoryLifeStone)
            switch (itemGrade) {
                case C:
                    gemstoneCount = 200;
                    gemstoneItemId = GEMSTONE_D;
                    break;
                case B:
                    gemstoneCount = 300;
                    gemstoneItemId = GEMSTONE_D;
                    break;
                case A:
                    gemstoneCount = 200;
                    gemstoneItemId = GEMSTONE_C;
                    break;
                case S:
                    gemstoneCount = 250;
                    gemstoneItemId = GEMSTONE_C;
                    break;
                case S80:
                    gemstoneCount = 250;
                    gemstoneItemId = GEMSTONE_B;
                    break;
                case S84:
                    gemstoneCount = 250;
                    gemstoneItemId = GEMSTONE_B;
                    break;
            }
        else
            switch (itemGrade) {
                case C:
                    gemstoneCount = 20;
                    gemstoneItemId = GEMSTONE_D;
                    break;
                case B:
                    gemstoneCount = 30;
                    gemstoneItemId = GEMSTONE_D;
                    break;
                case A:
                    gemstoneCount = 20;
                    gemstoneItemId = GEMSTONE_C;
                    break;
                case S:
                    gemstoneCount = 25;
                    gemstoneItemId = GEMSTONE_C;
                    break;
                case S80:
                    if (targetItem.getTemplate().getCrystalCount() == 10394) // Icarus
                        gemstoneCount = 36;
                    else
                        gemstoneCount = 28;
                    gemstoneItemId = GEMSTONE_B;
                    break;
                case S84:
                    gemstoneCount = 36;
                    gemstoneItemId = GEMSTONE_B;
                    break;
            }

        SystemMessage sm = new SystemMessage(SystemMessage.REQUIRES_S1_S2).addNumber(gemstoneCount).addItemName(gemstoneItemId);
        activeChar.sendPacket(new ExPutIntensiveResultForVariationMake(_refinerItemObjId, refinerItemId, gemstoneItemId, gemstoneCount), sm);
    }
}
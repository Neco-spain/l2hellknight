package l2p.gameserver.serverpackets;

import l2p.gameserver.instancemanager.commission.CommissionItemInfo;

/**
 * @author : Ragnarok
 * @date : 22.04.12  11:47
 */
public class ExResponseCommissionBuyInfo extends L2GameServerPacket {
    private CommissionItemInfo _itemInfo;

    public ExResponseCommissionBuyInfo(CommissionItemInfo itemInfo) {
        _itemInfo = itemInfo;
    }

    @Override
    protected void writeImpl() {
        writeEx(0xF7);
        writeD(1); // listSize
        writeQ(_itemInfo.getRegisteredPrice());
        writeQ(_itemInfo.getAuctionId());
        writeD(_itemInfo.getExItemType().ordinal());
        writeD(0x00); //unk maybe objId?
        writeD(_itemInfo.getItem().getItemId());
        writeQ(_itemInfo.getItem().getCount());
        writeH(_itemInfo.getItem().getTemplate().getType2ForPackets()); // item, type 2 for packets
        writeD(_itemInfo.getItem().getBodyPart()); // bodypart
        writeH(_itemInfo.getItem().getEnchantLevel()); // enchant level
        writeH(_itemInfo.getItem().getCustomType2()); // Custom type 2
        writeD(0); // Возможно, visible ID
        writeH(_itemInfo.getItem().getAttackElement().getId());
        writeH(_itemInfo.getItem().getAttackElementValue());
        writeH(_itemInfo.getItem().getDefenceFire());
        writeH(_itemInfo.getItem().getDefenceWater());
        writeH(_itemInfo.getItem().getDefenceWind());
        writeH(_itemInfo.getItem().getDefenceEarth());
        writeH(_itemInfo.getItem().getDefenceHoly());
        writeH(_itemInfo.getItem().getDefenceUnholy());
        writeH(_itemInfo.getItem().getEnchantOptions()[0]);
        writeH(_itemInfo.getItem().getEnchantOptions()[1]);
        writeH(_itemInfo.getItem().getEnchantOptions()[2]);
    }
}

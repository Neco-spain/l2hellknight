package l2p.gameserver.serverpackets;

import l2p.gameserver.instancemanager.commission.CommissionItemInfo;

import java.util.List;

/**
 * @author : Ragnarok
 * @date : 09.02.12  12:40
 * <p/>
 * Уходит в ответ на {@link l2p.gameserver.clientpackets.RequestCommissionRegisteredItem}
 * Отсылает список вещей, которые находятся на продаже в магазине
 * <p/>
 * В данном пакете отправляется максимум по 120 итемов.
 * Если итемов больше, значение part увеличивается на единицу за каждые 120 итемов
 * Пакеты отсылаются от большего part к меньшему. Наименьший этой 0.
 * В нём уходят все оставшиеся итемы но не более 120, т.к. по логике, все итемы должны были уйти ранее.
 * Всего максимум итемов - 999.
 */
public class ExResponseCommissionList extends L2GameServerPacket {
    public static final int EMPTY_LIST = -2;
    public static final int PLAYER_REGISTERED_ITEMS = 2;
    public static final int ALL_ITEMS = 3;

    private int type;
    private int currentTime;
    private int part;
    private List<CommissionItemInfo> items;

    public ExResponseCommissionList(int type) {
        this.type = type;
    }

    public ExResponseCommissionList(int type, int part, List<CommissionItemInfo> items) {
        this.type = type;
        this.part = part;
        this.items = items;

        currentTime = (int) (System.currentTimeMillis() / 1000);
    }

    @Override
    protected void writeImpl() {
        writeEx(0xF6);

        writeD(type); // List type. -2 при пустов листе, 02 - итемы, выставленные персонажем, 03 - все итемы
        if(type == EMPTY_LIST)
            return;
        writeD(currentTime); // current time
        writeD(part); // part
        writeD(items.size()); // items count
        for (CommissionItemInfo itemInfo : items) {
            writeQ(itemInfo.getAuctionId()); // auctionId
            writeQ(itemInfo.getRegisteredPrice()); // item price
            writeD(itemInfo.getExItemType().ordinal()); // Тип продаваемой вещи
            writeD(itemInfo.getSaleDays()); // sale days, 0 - 1 день, 1 - 3 дня, 2 - 5 дней, 3 - 7 дней.
            writeD((int) (itemInfo.getSaleEndTime() / 1000)); // Sale end time
            writeS(itemInfo.getSellerName()); // seller name
            writeD(0); // unknown (вероятно objectId итема), на евро всегда 0
            writeD(itemInfo.getItem().getItemId()); // itemId
            writeQ(itemInfo.getItem().getCount()); // item count
            writeH(itemInfo.getItem().getTemplate().getType2ForPackets()); // item, type 2 for packets
            writeD(itemInfo.getItem().getBodyPart()); // bodypart
            writeH(itemInfo.getItem().getEnchantLevel()); // enchant level
            writeH(itemInfo.getItem().getCustomType2()); // Custom type 2
            writeD(0); // Возможно, visible ID
            writeH(itemInfo.getItem().getAttackElement().getId());
            writeH(itemInfo.getItem().getAttackElementValue());
            writeH(itemInfo.getItem().getDefenceFire());
            writeH(itemInfo.getItem().getDefenceWater());
            writeH(itemInfo.getItem().getDefenceWind());
            writeH(itemInfo.getItem().getDefenceEarth());
            writeH(itemInfo.getItem().getDefenceHoly());
            writeH(itemInfo.getItem().getDefenceUnholy());
            writeH(itemInfo.getItem().getEnchantOptions()[0]);
            writeH(itemInfo.getItem().getEnchantOptions()[1]);
            writeH(itemInfo.getItem().getEnchantOptions()[2]);
        }
    }
}

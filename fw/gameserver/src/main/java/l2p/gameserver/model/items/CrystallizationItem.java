package l2p.gameserver.model.items;

/**
 * Created by IntelliJ IDEA.
 * User: Cain
 * Date: 30.05.12
 * Time: 23:21
 */
public class CrystallizationItem {
    private final int _itemId;
    private final long _count;
    private final float _chance;

    public CrystallizationItem(int itemId, long count, float chance)
    {
        _itemId = itemId;
        _count = count;
        _chance = chance;
    }

    public int getItemId() {
        return _itemId;
    }

    public long getCount() {
        return _count;
    }

    public float getChance() {
        return _chance;
    }

}

package l2p.gameserver.templates.item.support;

import org.napile.primitive.sets.IntSet;

/**
 * @author VISTALL
 * @date 14:02/14.07.2011
 */
public class MerchantGuard {
    private int _itemId;
    private int _npcId;
    private int _max;
    private IntSet _ssq;

    public MerchantGuard(int itemId, int npcId, int max, IntSet ssq) {
        _itemId = itemId;
        _npcId = npcId;
        _max = max;
        _ssq = ssq;
    }

    public int getItemId() {
        return _itemId;
    }

    public int getNpcId() {
        return _npcId;
    }

    public int getMax() {
        return _max;
    }
}

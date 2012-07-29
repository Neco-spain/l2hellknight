package l2p.gameserver.templates.item.support;

import l2p.gameserver.templates.item.ItemTemplate;
import org.napile.primitive.Containers;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.impl.HashIntSet;

import java.util.Collections;
import java.util.Set;

/**
 * @author VISTALL
 * @date 22:40/19.05.2011
 */
public class EnchantItem {
    private final int _itemId;
    private final int _chance;
    private final int _maxEnchant;

    private IntSet _items = Containers.EMPTY_INT_SET;
    private Set<ItemTemplate.Grade> _grades = Collections.emptySet();

    public EnchantItem(int itemId, int chance, int maxEnchant) {
        _itemId = itemId;
        _chance = chance;
        _maxEnchant = maxEnchant;
    }

    public void addItemId(int id) {
        if (_items.isEmpty())
            _items = new HashIntSet();

        _items.add(id);
    }

    public int getItemId() {
        return _itemId;
    }

    public int getChance() {
        return _chance;
    }

    public int getMaxEnchant() {
        return _maxEnchant;
    }

    public Set<ItemTemplate.Grade> getGrades() {
        return _grades;
    }

    public IntSet getItems() {
        return _items;
    }
}

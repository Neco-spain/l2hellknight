package l2p.gameserver.templates.item.support;

/**
 * @author VISTALL
 * @date 22:40/19.05.2011
 */
public class EnchantScroll extends EnchantItem {
    private final FailResultType _resultType;
    private final boolean _visualEffect;

    public EnchantScroll(int itemId, int chance, int maxEnchant, FailResultType resultType, boolean visualEffect) {
        super(itemId, chance, maxEnchant);
        _resultType = resultType;
        _visualEffect = visualEffect;
    }

    public FailResultType getResultType() {
        return _resultType;
    }

    public boolean isHasVisualEffect() {
        return _visualEffect;
    }
}

package l2p.gameserver.model.items;

public final class TradeItem extends ItemInfo
{
  private long _price;
  private long _referencePrice;
  private long _currentValue;
  private int _lastRechargeTime;
  private int _rechargeTime;

  public TradeItem()
  {
  }

  public TradeItem(ItemInstance item)
  {
    super(item);
    setReferencePrice(item.getReferencePrice());
  }

  public void setOwnersPrice(long price)
  {
    _price = price;
  }

  public long getOwnersPrice()
  {
    return _price;
  }

  public void setReferencePrice(long price)
  {
    _referencePrice = price;
  }

  public long getReferencePrice()
  {
    return _referencePrice;
  }

  public long getStorePrice()
  {
    return getReferencePrice() / 2L;
  }

  public void setCurrentValue(long value)
  {
    _currentValue = value;
  }

  public long getCurrentValue()
  {
    return _currentValue;
  }

  public void setRechargeTime(int rechargeTime)
  {
    _rechargeTime = rechargeTime;
  }

  public int getRechargeTime()
  {
    return _rechargeTime;
  }

  public boolean isCountLimited()
  {
    return getCount() > 0L;
  }

  public void setLastRechargeTime(int lastRechargeTime)
  {
    _lastRechargeTime = lastRechargeTime;
  }

  public int getLastRechargeTime()
  {
    return _lastRechargeTime;
  }
}
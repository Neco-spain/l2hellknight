package l2m.gameserver.templates.manor;

public class SeedProduction
{
  int _seedId;
  long _residual;
  long _price;
  long _sales;

  public SeedProduction(int id)
  {
    _seedId = id;
    _sales = 0L;
    _price = 0L;
    _sales = 0L;
  }

  public SeedProduction(int id, long amount, long price, long sales)
  {
    _seedId = id;
    _residual = amount;
    _price = price;
    _sales = sales;
  }

  public int getId()
  {
    return _seedId;
  }

  public long getCanProduce()
  {
    return _residual;
  }

  public long getPrice()
  {
    return _price;
  }

  public long getStartProduce()
  {
    return _sales;
  }

  public void setCanProduce(long amount)
  {
    _residual = amount;
  }
}
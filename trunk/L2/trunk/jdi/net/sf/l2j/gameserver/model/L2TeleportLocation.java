package net.sf.l2j.gameserver.model;

public class L2TeleportLocation
{
  private int _teleId;
  private int _locX;
  private int _locY;
  private int _locZ;
  private int _price;
  private boolean _forNoble;

  public void setTeleId(int id)
  {
    _teleId = id;
  }

  public void setLocX(int locX)
  {
    _locX = locX;
  }

  public void setLocY(int locY)
  {
    _locY = locY;
  }

  public void setLocZ(int locZ)
  {
    _locZ = locZ;
  }

  public void setPrice(int price)
  {
    _price = price;
  }

  public void setIsForNoble(boolean val)
  {
    _forNoble = val;
  }

  public int getTeleId()
  {
    return _teleId;
  }

  public int getLocX()
  {
    return _locX;
  }

  public int getLocY()
  {
    return _locY;
  }

  public int getLocZ()
  {
    return _locZ;
  }

  public int getPrice()
  {
    return _price;
  }

  public boolean getIsForNoble()
  {
    return _forNoble;
  }
}
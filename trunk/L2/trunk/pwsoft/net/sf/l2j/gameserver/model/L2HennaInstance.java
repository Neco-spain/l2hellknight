package net.sf.l2j.gameserver.model;

import net.sf.l2j.gameserver.templates.L2Henna;

public class L2HennaInstance
{
  private L2Henna _template;
  private int _symbolId;
  private int _itemIdDye;
  private int _price;
  private int _statINT;
  private int _statSTR;
  private int _statCON;
  private int _statMEM;
  private int _statDEX;
  private int _statWIT;
  private int _amountDyeRequire;

  public L2HennaInstance(L2Henna template)
  {
    _template = template;
    _symbolId = _template.symbolId;
    _itemIdDye = _template.dye;
    _amountDyeRequire = _template.amount;
    _price = _template.price;
    _statINT = _template.statINT;
    _statSTR = _template.statSTR;
    _statCON = _template.statCON;
    _statMEM = _template.statMEM;
    _statDEX = _template.statDEX;
    _statWIT = _template.statWIT;
  }

  public String getName() {
    String res = "";
    if (_statINT > 0) res = res + "INT +" + _statINT;
    else if (_statSTR > 0) res = res + "STR +" + _statSTR;
    else if (_statCON > 0) res = res + "CON +" + _statCON;
    else if (_statMEM > 0) res = res + "MEN +" + _statMEM;
    else if (_statDEX > 0) res = res + "DEX +" + _statDEX;
    else if (_statWIT > 0) res = res + "WIT +" + _statWIT;

    if (_statINT < 0) res = res + ", INT " + _statINT;
    else if (_statSTR < 0) res = res + ", STR " + _statSTR;
    else if (_statCON < 0) res = res + ", CON " + _statCON;
    else if (_statMEM < 0) res = res + ", MEN " + _statMEM;
    else if (_statDEX < 0) res = res + ", DEX " + _statDEX;
    else if (_statWIT < 0) res = res + ", WIT " + _statWIT;

    return res;
  }

  public L2Henna getTemplate()
  {
    return _template;
  }

  public int getSymbolId()
  {
    return _symbolId;
  }

  public void setSymbolId(int SymbolId)
  {
    _symbolId = SymbolId;
  }

  public int getItemIdDye()
  {
    return _itemIdDye;
  }

  public void setItemIdDye(int ItemIdDye)
  {
    _itemIdDye = ItemIdDye;
  }

  public int getAmountDyeRequire()
  {
    return _amountDyeRequire;
  }

  public void setAmountDyeRequire(int AmountDyeRequire)
  {
    _amountDyeRequire = AmountDyeRequire;
  }

  public int getPrice()
  {
    return _price;
  }

  public void setPrice(int Price)
  {
    _price = Price;
  }

  public int getStatINT()
  {
    return _statINT;
  }

  public void setStatINT(int StatINT)
  {
    _statINT = StatINT;
  }

  public int getStatSTR()
  {
    return _statSTR;
  }

  public void setStatSTR(int StatSTR)
  {
    _statSTR = StatSTR;
  }

  public int getStatCON()
  {
    return _statCON;
  }

  public void setStatCON(int StatCON)
  {
    _statCON = StatCON;
  }

  public int getStatMEM()
  {
    return _statMEM;
  }

  public void setStatMEM(int StatMEM)
  {
    _statMEM = StatMEM;
  }

  public int getStatDEX()
  {
    return _statDEX;
  }

  public void setStatDEX(int StatDEX)
  {
    _statDEX = StatDEX;
  }

  public int getStatWIT()
  {
    return _statWIT;
  }

  public void setStatWIT(int StatWIT)
  {
    _statWIT = StatWIT;
  }
}
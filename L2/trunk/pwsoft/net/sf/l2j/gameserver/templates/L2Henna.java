package net.sf.l2j.gameserver.templates;

public class L2Henna
{
  public final int symbolId;
  public final String symbolName;
  public final int dye;
  public final int price;
  public final int amount;
  public final int statINT;
  public final int statSTR;
  public final int statCON;
  public final int statMEM;
  public final int statDEX;
  public final int statWIT;

  public L2Henna(StatsSet set)
  {
    symbolId = set.getInteger("symbol_id");
    symbolName = "";
    dye = set.getInteger("dye");
    price = set.getInteger("price");
    amount = set.getInteger("amount");
    statINT = set.getInteger("stat_INT");
    statSTR = set.getInteger("stat_STR");
    statCON = set.getInteger("stat_CON");
    statMEM = set.getInteger("stat_MEM");
    statDEX = set.getInteger("stat_DEX");
    statWIT = set.getInteger("stat_WIT");
  }

  public int getSymbolId()
  {
    return symbolId;
  }

  public int getDyeId()
  {
    return dye;
  }

  public int getPrice()
  {
    return price;
  }

  public int getAmountDyeRequire()
  {
    return amount;
  }

  public int getStatINT()
  {
    return statINT;
  }

  public int getStatSTR()
  {
    return statSTR;
  }

  public int getStatCON()
  {
    return statCON;
  }

  public int getStatMEM()
  {
    return statMEM;
  }

  public int getStatDEX()
  {
    return statDEX;
  }

  public int getStatWIT()
  {
    return statWIT;
  }
}
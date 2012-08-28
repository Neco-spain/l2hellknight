package net.sf.l2j.gameserver.network.clientpackets;

public final class RequestEnchantItemDef extends RequestEnchantItem
{
  private int _objectId;

  protected void readImpl()
  {
    _objectId = readD();
  }

  protected void runImpl()
  {
  }
}
package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class ExStorageMaxCount extends L2GameServerPacket
{
  private L2PcInstance _activeChar;
  private int _inventory;
  private int _warehouse;
  private int _freight;
  private int _privateSell;
  private int _privateBuy;
  private int _receipeD;
  private int _recipe;

  public ExStorageMaxCount(L2PcInstance character)
  {
    _activeChar = character;
    _inventory = _activeChar.getInventoryLimit();
    _warehouse = _activeChar.getWareHouseLimit();
    _privateSell = _activeChar.getPrivateSellStoreLimit();
    _privateBuy = _activeChar.getPrivateBuyStoreLimit();
    _freight = _activeChar.getFreightLimit();
    _receipeD = _activeChar.getDwarfRecipeLimit();
    _recipe = _activeChar.getCommonRecipeLimit();
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(46);

    writeD(_inventory);
    writeD(_warehouse);
    writeD(_freight);
    writeD(_privateSell);
    writeD(_privateBuy);
    writeD(_receipeD);
    writeD(_recipe);
  }
}
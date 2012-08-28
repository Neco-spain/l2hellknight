package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class ExStorageMaxCount extends L2GameServerPacket
{
  private static final String _S__FE_2E_EXSTORAGEMAXCOUNT = "[S] FE:2E ExStorageMaxCount";
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
    _inventory = _activeChar.GetInventoryLimit();
    _warehouse = _activeChar.GetWareHouseLimit();
    _privateSell = _activeChar.GetPrivateSellStoreLimit();
    _privateBuy = _activeChar.GetPrivateBuyStoreLimit();
    _freight = _activeChar.GetFreightLimit();
    _receipeD = _activeChar.GetDwarfRecipeLimit();
    _recipe = _activeChar.GetCommonRecipeLimit();
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

  public String getType()
  {
    return "[S] FE:2E ExStorageMaxCount";
  }
}
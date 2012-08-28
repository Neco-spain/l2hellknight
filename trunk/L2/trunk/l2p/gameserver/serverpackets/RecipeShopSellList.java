package l2p.gameserver.serverpackets;

import java.util.List;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.items.ManufactureItem;

public class RecipeShopSellList extends L2GameServerPacket
{
  private int objId;
  private int curMp;
  private int maxMp;
  private long adena;
  private List<ManufactureItem> createList;

  public RecipeShopSellList(Player buyer, Player manufacturer)
  {
    objId = manufacturer.getObjectId();
    curMp = (int)manufacturer.getCurrentMp();
    maxMp = manufacturer.getMaxMp();
    adena = buyer.getAdena();
    createList = manufacturer.getCreateList();
  }

  protected final void writeImpl()
  {
    writeC(223);
    writeD(objId);
    writeD(curMp);
    writeD(maxMp);
    writeQ(adena);
    writeD(createList.size());
    for (ManufactureItem mi : createList)
    {
      writeD(mi.getRecipeId());
      writeD(0);
      writeQ(mi.getCost());
    }
  }
}
package l2p.gameserver.serverpackets;

import java.util.Collection;
import l2p.gameserver.data.xml.holder.ProductHolder;
import l2p.gameserver.model.ProductItem;

public class ExBR_ProductList extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(214);
    Collection items = ProductHolder.getInstance().getAllItems();
    writeD(items.size());

    for (ProductItem template : items)
    {
      if ((System.currentTimeMillis() < template.getStartTimeSale()) || 
        (System.currentTimeMillis() > template.getEndTimeSale())) {
        continue;
      }
      writeD(template.getProductId());
      writeH(template.getCategory());
      writeD(template.getPoints());
      writeD(template.getTabId());
      writeD((int)(template.getStartTimeSale() / 1000L));
      writeD((int)(template.getEndTimeSale() / 1000L));
      writeC(127);
      writeC(template.getStartHour());
      writeC(template.getStartMin());
      writeC(template.getEndHour());
      writeC(template.getEndMin());
      writeD(0);
      writeD(-1);
    }
  }
}
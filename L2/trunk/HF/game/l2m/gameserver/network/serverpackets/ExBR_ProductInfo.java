package l2m.gameserver.network.serverpackets;

import java.util.ArrayList;
import l2m.gameserver.data.xml.holder.ProductHolder;
import l2m.gameserver.model.ProductItem;
import l2m.gameserver.model.ProductItemComponent;

public class ExBR_ProductInfo extends L2GameServerPacket
{
  private ProductItem _productId;

  public ExBR_ProductInfo(int id)
  {
    _productId = ProductHolder.getInstance().getProduct(id);
  }

  protected void writeImpl()
  {
    if (_productId == null) {
      return;
    }
    writeEx(215);

    writeD(_productId.getProductId());
    writeD(_productId.getPoints());
    writeD(_productId.getComponents().size());

    for (ProductItemComponent com : _productId.getComponents())
    {
      writeD(com.getItemId());
      writeD(com.getCount());
      writeD(com.getWeight());
      writeD(com.isDropable() ? 1 : 0);
    }
  }
}
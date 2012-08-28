package l2m.gameserver.network.clientpackets;

import l2m.gameserver.data.xml.holder.ItemHolder;
import l2m.gameserver.data.xml.holder.ProductHolder;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.ProductItem;
import l2m.gameserver.model.ProductItemComponent;
import l2m.gameserver.model.items.PcInventory;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.ExBR_BuyProduct;
import l2m.gameserver.network.serverpackets.ExBR_GamePoint;
import l2m.gameserver.templates.item.ItemTemplate;

public class RequestExBR_BuyProduct extends L2GameClientPacket
{
  private int _productId;
  private int _count;

  protected void readImpl()
  {
    _productId = readD();
    _count = readD();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();

    if (activeChar == null) {
      return;
    }
    if ((_count > 99) || (_count < 0)) {
      return;
    }
    ProductItem product = ProductHolder.getInstance().getProduct(_productId);
    if (product == null)
    {
      activeChar.sendPacket(new ExBR_BuyProduct(-2));
      return;
    }

    if ((System.currentTimeMillis() < product.getStartTimeSale()) || (System.currentTimeMillis() > product.getEndTimeSale()))
    {
      activeChar.sendPacket(new ExBR_BuyProduct(-7));
      return;
    }

    int totalPoints = product.getPoints() * _count;

    if (totalPoints < 0)
    {
      activeChar.sendPacket(new ExBR_BuyProduct(-2));
      return;
    }

    long gamePointSize = activeChar.getPremiumPoints();

    if (totalPoints > gamePointSize)
    {
      activeChar.sendPacket(new ExBR_BuyProduct(-1));
      return;
    }

    int totalWeight = 0;
    for (ProductItemComponent com : product.getComponents()) {
      totalWeight += com.getWeight();
    }
    totalWeight *= _count;

    int totalCount = 0;

    for (ProductItemComponent com : product.getComponents())
    {
      ItemTemplate item = ItemHolder.getInstance().getTemplate(com.getItemId());
      if (item == null)
      {
        activeChar.sendPacket(new ExBR_BuyProduct(-2));
        return;
      }
      totalCount += (item.isStackable() ? 1 : com.getCount() * _count);
    }

    if ((!activeChar.getInventory().validateCapacity(totalCount)) || (!activeChar.getInventory().validateWeight(totalWeight)))
    {
      activeChar.sendPacket(new ExBR_BuyProduct(-4));
      return;
    }

    activeChar.reducePremiumPoints(totalPoints);

    for (ProductItemComponent $comp : product.getComponents()) {
      activeChar.getInventory().addItem($comp.getItemId(), $comp.getCount() * _count);
    }
    activeChar.sendPacket(new ExBR_GamePoint(activeChar));
    activeChar.sendPacket(new ExBR_BuyProduct(1));
    activeChar.sendChanges();
  }
}
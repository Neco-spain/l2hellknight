package l2p.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2p.gameserver.Config;
import l2p.gameserver.data.xml.holder.ItemHolder;
import l2p.gameserver.data.xml.holder.MultiSellHolder.MultiSellListContainer;
import l2p.gameserver.model.base.MultiSellEntry;
import l2p.gameserver.model.base.MultiSellIngredient;
import l2p.gameserver.templates.item.ItemTemplate;

public class MultiSellList extends L2GameServerPacket
{
  private final int _page;
  private final int _finished;
  private final int _listId;
  private final List<MultiSellEntry> _list;

  public MultiSellList(MultiSellHolder.MultiSellListContainer list, int page, int finished)
  {
    _list = list.getEntries();
    _listId = list.getListId();
    _page = page;
    _finished = finished;
  }

  protected final void writeImpl()
  {
    writeC(208);
    writeD(_listId);
    writeD(_page);
    writeD(_finished);
    writeD(Config.MULTISELL_SIZE);
    writeD(_list.size());

    for (MultiSellEntry ent : _list)
    {
      List ingredients = fixIngredients(ent.getIngredients());

      writeD(ent.getEntryId());
      writeC((!ent.getProduction().isEmpty()) && (((MultiSellIngredient)ent.getProduction().get(0)).isStackable()) ? 1 : 0);
      writeH(0);
      writeD(0);
      writeD(0);

      writeItemElements();

      writeH(ent.getProduction().size());
      writeH(ingredients.size());

      for (MultiSellIngredient prod : ent.getProduction())
      {
        int itemId = prod.getItemId();
        ItemTemplate template = itemId > 0 ? ItemHolder.getInstance().getTemplate(prod.getItemId()) : null;
        writeD(itemId);
        writeD(itemId > 0 ? template.getBodyPart() : 0);
        writeH(itemId > 0 ? template.getType2ForPackets() : 0);
        writeQ(prod.getItemCount());
        writeH(prod.getItemEnchant());
        writeD(0);
        writeD(0);
        writeItemElements(prod);
      }

      for (MultiSellIngredient i : ingredients)
      {
        int itemId = i.getItemId();
        ItemTemplate item = itemId > 0 ? ItemHolder.getInstance().getTemplate(i.getItemId()) : null;
        writeD(itemId);
        writeH(itemId > 0 ? item.getType2() : 65535);
        writeQ(i.getItemCount());
        writeH(i.getItemEnchant());
        writeD(0);
        writeD(0);
        writeItemElements(i);
      }
    }
  }

  private static List<MultiSellIngredient> fixIngredients(List<MultiSellIngredient> ingredients)
  {
    int needFix = 0;
    for (MultiSellIngredient ingredient : ingredients) {
      if (ingredient.getItemCount() > 2147483647L)
        needFix++;
    }
    if (needFix == 0) {
      return ingredients;
    }

    List result = new ArrayList(ingredients.size() + needFix);
    for (MultiSellIngredient ingredient : ingredients)
    {
      ingredient = ingredient.clone();
      while (ingredient.getItemCount() > 2147483647L)
      {
        MultiSellIngredient temp = ingredient.clone();
        temp.setItemCount(2000000000L);
        result.add(temp);
        ingredient.setItemCount(ingredient.getItemCount() - 2000000000L);
      }
      if (ingredient.getItemCount() > 0L) {
        result.add(ingredient);
      }
    }
    return result;
  }
}
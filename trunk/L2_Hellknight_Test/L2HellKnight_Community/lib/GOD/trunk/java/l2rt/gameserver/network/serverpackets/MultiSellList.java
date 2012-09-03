package l2rt.gameserver.network.serverpackets;

import l2rt.Config;
import l2rt.gameserver.model.L2Multisell.MultiSellListContainer;
import l2rt.gameserver.model.base.MultiSellEntry;
import l2rt.gameserver.model.base.MultiSellIngredient;
import l2rt.gameserver.templates.L2Item;
import l2rt.gameserver.xml.ItemTemplates;
import l2rt.util.GArray;

public class MultiSellList extends L2GameServerPacket
{
	protected int _page;
	protected int _finished;

	private int _listId;
	private GArray<MultiSellEntry> _possiblelist = new GArray<MultiSellEntry>();

	public MultiSellList(MultiSellListContainer list, int page, int finished)
	{
		_possiblelist = list.getEntries();
		_listId = list.getListId();
		_page = page;
		_finished = finished;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xD0);
		/*
		Новый мультисел
		2047C4A3   PUSH Engine.205A83C0                      ASCII "dddddc"
		2047C59B   PUSH Engine.205A83C8                      ASCII "dchddhhhhhhhhhh"
		2047C66D   PUSH Engine.205A83D8                      ASCII "ddhQhdddhhhhhhhh"
		2047C73F   PUSH Engine.205A83EC                      ASCII "dhQhddhhhhhhhh"
		2047C7C9   PUSH Engine.205A83FC                      UNICODE "(Receive)MultiSellListPacket"
		*/
		writeD(_listId); // list id
		writeD(1 + _page / Config.MULTISELL_SIZE); // page
		writeD(_finished); // finished
		writeD(Config.MULTISELL_SIZE); // size of pages
		writeD(_possiblelist != null ? _possiblelist.size() : 0); //list lenght
		writeC(0x00); //GOD при 1 открывается новый тип мультисела, с обменником
		if(_possiblelist == null)
			return;

		GArray<MultiSellIngredient> ingredients;
		for(MultiSellEntry ent : _possiblelist)
		{
			//ingredients = fixIngredients(ent.getIngredients());
			ingredients = ent.getIngredients();
			writeD(ent.getEntryId());
			writeC(!ent.getProduction().isEmpty() && ent.getProduction().get(0).isStackable() ? 1 : 0); // stackable?
			writeH(0x00); // unknown
			writeD(0x00); // инкрустация
			writeD(0x00); // инкрустация

			writeItemElements();

			writeH(ent.getProduction().size());
			writeH(ingredients.size());

			for(MultiSellIngredient prod : ent.getProduction())
			{
				int itemId = prod.getItemId();
				L2Item template = itemId > 0 ? ItemTemplates.getInstance().getTemplate(prod.getItemId()) : null;
				writeD(itemId);
				writeD(itemId > 0 ? template.getBodyPart() : 0);
				writeH(itemId > 0 ? template.getType2ForPackets() : 0);
				writeQ(prod.getItemCount());
				writeH(prod.getItemEnchant());
				writeD(0x00); // augment id
				writeD(0x00); // mana
				writeD(0x00); // god
				writeItemElements(prod);
			}

			for(MultiSellIngredient i : ingredients)
			{
				int itemId = i.getItemId();
				final L2Item item = itemId > 0 ? ItemTemplates.getInstance().getTemplate(i.getItemId()) : null;
				writeD(itemId); //ID
				writeH(itemId > 0 ? item.getType2() : 0xffff);
				writeQ(i.getItemCount()); //Count
				writeH((itemId > 0 ? item.getType2() : 0x00) <= L2Item.TYPE2_ACCESSORY ? i.getItemEnchant() : 0); //Enchant Level
				writeD(0x00); // augment id
				writeD(0x00); // mana
				writeItemElements(i);
			}
		}
	}

	private static GArray<MultiSellIngredient> fixIngredients(GArray<MultiSellIngredient> ingredients)
	{
		//FIXME временная затычка, пока NCSoft не починят в клиенте отображение мультиселов где кол-во больше Integer.MAX_VALUE
		int needFix = 0;
		for(MultiSellIngredient ingredient : ingredients)
			if(ingredient.getItemCount() > Integer.MAX_VALUE)
				needFix++;

		if(needFix == 0)
			return ingredients;

		MultiSellIngredient temp;
		GArray<MultiSellIngredient> result = new GArray<MultiSellIngredient>(ingredients.size() + needFix);
		for(MultiSellIngredient ingredient : ingredients)
		{
			ingredient = ingredient.clone();
			while(ingredient.getItemCount() > Integer.MAX_VALUE)
			{
				temp = ingredient.clone();
				temp.setItemCount(2000000000);
				result.add(temp);
				ingredient.setItemCount(ingredient.getItemCount() - 2000000000);
			}
			if(ingredient.getItemCount() > 0)
				result.add(ingredient);
		}

		return result;
	}
}
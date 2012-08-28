package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.model.L2Multisell.MultiSellEntry;
import net.sf.l2j.gameserver.model.L2Multisell.MultiSellIngredient;
import net.sf.l2j.gameserver.model.L2Multisell.MultiSellListContainer;

public class MultiSellList extends L2GameServerPacket
{
    private static final String _S__D0_MULTISELLLIST = "[S] D0 MultiSellList";

    protected int _listId, _page, _finished;
    protected MultiSellListContainer _list;

    public MultiSellList(MultiSellListContainer list, int page, int finished)
    {
    	_list = list;
    	_listId = list.getListId();
    	_page = page;
    	_finished = finished;
    }

    @Override
	protected void writeImpl()
    {
        writeC(0xd0);
        writeD(_listId);    // list id
        writeD(_page);		// page
        writeD(_finished);	// finished
        writeD(0x28);	// size of pages
        writeD(_list == null ? 0 : _list.getEntries().size()); //list lenght

        if(_list != null)
        {
            for(MultiSellEntry ent : _list.getEntries())
            {
            	writeD(ent.getEntryId());
            	writeD(0x00); // C6
            	writeD(0x00); // C6
            	writeC(1);
            	writeH(ent.getProducts().size());
            	writeH(ent.getIngredients().size());

            	for(MultiSellIngredient i: ent.getProducts())
            	{
	            	writeH(i.getItemId());
	            	writeD(0);
	            	try
	            	{
	            	writeH(ItemTable.getInstance().getTemplate(i.getItemId()).getType2());
	            	}
	            	catch (Exception e)
	                {
	                  _log.warning("[S] D0 MultiSellList: list ID:" + _listId + " page:" + _page + " product id:" + i.getItemId() + " ERROR! Use correct ProductId in Multisells!");
	                }
	            	writeD(i.getItemCount());
	        	    writeH(i.getEnchantmentLevel()); //enchtant lvl
	            	writeD(0x00); // C6
	            	writeD(0x00); // C6
            	}

                for(MultiSellIngredient i : ent.getIngredients())
                {
                	int items = i.getItemId();
                	int typeE = 65535;
                	//if (items != 65336 && items != 65436)
                	//	typeE = ItemTable.getInstance().getTemplate(i.getItemId()).getType2();
                	if (items > 0)
                    {
                      try
                      {
                        typeE = ItemTable.getInstance().getTemplate(i.getItemId()).getType2();
                      }
                      catch (Exception e)
                      {
                    	  if (i.getItemId() != 65436 && i.getItemId() != 65336)
                    		  _log.warning("[S] D0 MultiSellList: list ID:" + _listId + " page:" + _page + " ingredient id:" + i.getItemId() + " ERROR! Use correct IngridientId in Multisells!");
                      }
                    }
                    writeH(items);      //ID
                    writeH(typeE);
                    writeD(i.getItemCount());	//Count
                    writeH(i.getEnchantmentLevel()); //Enchant Level
                	writeD(0x00); // C6
                	writeD(0x00); // C6
                }
            }
        }
    }

    @Override
    public String getType()
    {
        return _S__D0_MULTISELLLIST;
    }

}

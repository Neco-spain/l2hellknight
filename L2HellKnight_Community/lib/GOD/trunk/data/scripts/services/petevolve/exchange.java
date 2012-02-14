package services.petevolve;

import l2rt.Config;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Summon;
import l2rt.gameserver.model.instances.L2PetInstance;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.ItemList;
import l2rt.gameserver.tables.PetDataTable;
import l2rt.gameserver.tables.PetDataTable.L2Pet;
import l2rt.gameserver.templates.L2Item;
import l2rt.gameserver.xml.ItemTemplates;
import l2rt.util.Files;
import l2rt.util.Util;

public class exchange extends Functions implements ScriptFile
{
	/** Билеты для обмена **/
	private static final int PEticketB = 7583;
	private static final int PEticketC = 7584;
	private static final int PEticketK = 7585;

	/** Дудки для вызова петов **/
	private static final int BbuffaloP = 6648;
	private static final int BcougarC = 6649;
	private static final int BkookaburraO = 6650;

	public void exch_1()
	{
		L2Player player = (L2Player) getSelf();
		if(player == null)
			return;
		if(getItemCount(player, PEticketB) >= 1)
		{
			removeItem(player, PEticketB, 1);
			addItem(player, BbuffaloP, 1);
			return;
		}
		show(Files.read("data/scripts/services/petevolve/exchange_no.htm", player), player);
	}

	public void exch_2()
	{
		L2Player player = (L2Player) getSelf();
		if(player == null)
			return;
		if(getItemCount(player, PEticketC) >= 1)
		{
			removeItem(player, PEticketC, 1);
			addItem(player, BcougarC, 1);
			return;
		}
		show(Files.read("data/scripts/services/petevolve/exchange_no.htm", player), player);
	}

	public void exch_3()
	{
		L2Player player = (L2Player) getSelf();
		if(player == null)
			return;

		if(getItemCount(player, PEticketK) >= 1)
		{
			removeItem(player, PEticketK, 1);
			addItem(player, BkookaburraO, 1);
			return;
		}
		show(Files.read("data/scripts/services/petevolve/exchange_no.htm", player), player);
	}

	public void showBabyPetExchange()
	{
		L2Player player = (L2Player) getSelf();
		if(player == null)
			return;
		if(!Config.SERVICES_EXCHANGE_BABY_PET_ENABLED)
		{
			show("Сервис отключен.", player);
			return;
		}
		L2Item item = ItemTemplates.getInstance().getTemplate(Config.SERVICES_EXCHANGE_BABY_PET_ITEM);
		String out = "";
		out += "<html><body>Вы можете в любое время обменять вашего Improved Baby пета на другой вид, без потери опыта. Пет при этом должен быть вызван.";
		out += "<br>Стоимость обмена: " + Util.formatAdena(Config.SERVICES_EXCHANGE_BABY_PET_PRICE) + " " + item.getName();
		out += "<br><button width=250 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h scripts_services.petevolve.exchange:exToCougar\" value=\"Обменять на Improved Cougar\">";
		out += "<br1><button width=250 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h scripts_services.petevolve.exchange:exToBuffalo\" value=\"Обменять на Improved Buffalo\">";
		out += "<br1><button width=250 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h scripts_services.petevolve.exchange:exToKookaburra\" value=\"Обменять на Improved Kookaburra\">";
		out += "</body></html>";
		show(out, player);
	}

	public void showErasePetName()
	{
		L2Player player = (L2Player) getSelf();
		if(player == null)
			return;
		if(!Config.SERVICES_CHANGE_PET_NAME_ENABLED)
		{
			show("Сервис отключен.", player);
			return;
		}
		L2Item item = ItemTemplates.getInstance().getTemplate(Config.SERVICES_CHANGE_PET_NAME_ITEM);
		String out = "";
		out += "<html><body>Вы можете обнулить имя у пета, для того чтобы назначить новое. Пет при этом должен быть вызван.";
		out += "<br>Стоимость обнуления: " + Util.formatAdena(Config.SERVICES_CHANGE_PET_NAME_PRICE) + " " + item.getName();
		out += "<br><button width=100 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h scripts_services.petevolve.exchange:erasePetName\" value=\"Обнулить имя\">";
		out += "</body></html>";
		show(out, player);
	}

	public void erasePetName()
	{
		L2Player player = (L2Player) getSelf();
		if(player == null)
			return;
		if(!Config.SERVICES_CHANGE_PET_NAME_ENABLED)
		{
			show("Сервис отключен.", player);
			return;
		}
		L2Summon pl_pet = player.getPet();
		if(pl_pet == null || !pl_pet.isPet())
		{
			show("Питомец должен быть вызван.", player);
			return;
		}
		L2Item item = ItemTemplates.getInstance().getTemplate(Config.SERVICES_CHANGE_PET_NAME_ITEM);
		L2ItemInstance pay = player.getInventory().getItemByItemId(item.getItemId());
		if(pay != null && pay.getCount() >= Config.SERVICES_CHANGE_PET_NAME_PRICE)
		{
			player.getInventory().destroyItem(pay, Config.SERVICES_CHANGE_PET_NAME_PRICE, true);
			pl_pet.setName(pl_pet.getTemplate().name);
			pl_pet.broadcastPetInfo();

			L2PetInstance _pet = (L2PetInstance) pl_pet;
			L2ItemInstance controlItem = _pet.getControlItem();
			if(controlItem != null)
			{
				controlItem.setCustomType2(1);
				controlItem.setPriceToSell(0);
				controlItem.updateDatabase();
				_pet.updateControlItem();
			}
			show("Имя стерто.", player);
		}
		else if(Config.SERVICES_CHANGE_PET_NAME_ITEM == 57)
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
		else
			player.sendPacket(Msg.INCORRECT_ITEM_COUNT);
	}

	public void exToCougar()
	{
		L2Player player = (L2Player) getSelf();
		if(player == null)
			return;
		if(!Config.SERVICES_EXCHANGE_BABY_PET_ENABLED)
		{
			show("Сервис отключен.", player);
			return;
		}
		L2Summon pl_pet = player.getPet();
		if(pl_pet == null || pl_pet.isDead() || !(pl_pet.getNpcId() == PetDataTable.IMPROVED_BABY_BUFFALO_ID || pl_pet.getNpcId() == PetDataTable.IMPROVED_BABY_KOOKABURRA_ID))
		{
			show("Пет должен быть вызван.", player);
			return;
		}
		L2Item item = ItemTemplates.getInstance().getTemplate(Config.SERVICES_EXCHANGE_BABY_PET_ITEM);
		L2ItemInstance pay = player.getInventory().getItemByItemId(item.getItemId());
		if(pay != null && pay.getCount() >= Config.SERVICES_EXCHANGE_BABY_PET_PRICE)
		{
			player.getInventory().destroyItem(pay, Config.SERVICES_EXCHANGE_BABY_PET_PRICE, true);
			L2ItemInstance control = player.getInventory().getItemByObjectId(player.getPet().getControlItemObjId());
			control.setItemId(L2Pet.IMPROVED_BABY_COUGAR.getControlItemId());
			control.updateDatabase(true, true);
			player.sendPacket(new ItemList(player, false));
			player.getPet().unSummon();
			show("Пет изменен.", player);
		}
		else if(Config.SERVICES_EXCHANGE_BABY_PET_ITEM == 57)
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
		else
			player.sendPacket(Msg.INCORRECT_ITEM_COUNT);
	}

	public void exToBuffalo()
	{
		L2Player player = (L2Player) getSelf();
		if(player == null)
			return;
		if(!Config.SERVICES_EXCHANGE_BABY_PET_ENABLED)
		{
			show("Сервис отключен.", player);
			return;
		}
		L2Summon pl_pet = player.getPet();
		if(pl_pet == null || pl_pet.isDead() || !(pl_pet.getNpcId() == PetDataTable.IMPROVED_BABY_COUGAR_ID || pl_pet.getNpcId() == PetDataTable.IMPROVED_BABY_KOOKABURRA_ID))
		{
			show("Пет должен быть вызван.", player);
			return;
		}
		if(Config.ALT_IMPROVED_PETS_LIMITED_USE && player.isMageClass())
		{
			show("Этот пет только для воинов.", player);
			return;
		}
		L2Item item = ItemTemplates.getInstance().getTemplate(Config.SERVICES_EXCHANGE_BABY_PET_ITEM);
		L2ItemInstance pay = player.getInventory().getItemByItemId(item.getItemId());
		if(pay != null && pay.getCount() >= Config.SERVICES_EXCHANGE_BABY_PET_PRICE)
		{
			player.getInventory().destroyItem(pay, Config.SERVICES_EXCHANGE_BABY_PET_PRICE, true);
			L2ItemInstance control = player.getInventory().getItemByObjectId(player.getPet().getControlItemObjId());
			control.setItemId(L2Pet.IMPROVED_BABY_BUFFALO.getControlItemId());
			control.updateDatabase(true, true);
			player.sendPacket(new ItemList(player, false));
			player.getPet().unSummon();
			show("Пет изменен.", player);
		}
		else if(Config.SERVICES_EXCHANGE_BABY_PET_ITEM == 57)
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
		else
			player.sendPacket(Msg.INCORRECT_ITEM_COUNT);
	}

	public void exToKookaburra()
	{
		L2Player player = (L2Player) getSelf();
		if(player == null)
			return;
		if(!Config.SERVICES_EXCHANGE_BABY_PET_ENABLED)
		{
			show("Сервис отключен.", player);
			return;
		}
		L2Summon pl_pet = player.getPet();
		if(pl_pet == null || pl_pet.isDead() || !(pl_pet.getNpcId() == PetDataTable.IMPROVED_BABY_BUFFALO_ID || pl_pet.getNpcId() == PetDataTable.IMPROVED_BABY_COUGAR_ID))
		{
			show("Пет должен быть вызван.", player);
			return;
		}
		if(Config.ALT_IMPROVED_PETS_LIMITED_USE && !player.isMageClass())
		{
			show("Этот пет только для магов.", player);
			return;
		}
		L2Item item = ItemTemplates.getInstance().getTemplate(Config.SERVICES_EXCHANGE_BABY_PET_ITEM);
		L2ItemInstance pay = player.getInventory().getItemByItemId(item.getItemId());
		if(pay != null && pay.getCount() >= Config.SERVICES_EXCHANGE_BABY_PET_PRICE)
		{
			player.getInventory().destroyItem(pay, Config.SERVICES_EXCHANGE_BABY_PET_PRICE, true);
			L2ItemInstance control = player.getInventory().getItemByObjectId(player.getPet().getControlItemObjId());
			control.setItemId(L2Pet.IMPROVED_BABY_KOOKABURRA.getControlItemId());
			control.updateDatabase(true, true);
			player.sendPacket(new ItemList(player, false));
			player.getPet().unSummon();
			show("Пет изменен.", player);
		}
		else if(Config.SERVICES_EXCHANGE_BABY_PET_ITEM == 57)
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
		else
			player.sendPacket(Msg.INCORRECT_ITEM_COUNT);
	}

	public static String DialogAppend_30731(Integer val)
	{
		return getHtmlAppends(val);
	}

	public static String DialogAppend_30827(Integer val)
	{
		return getHtmlAppends(val);
	}

	public static String DialogAppend_30828(Integer val)
	{
		return getHtmlAppends(val);
	}

	public static String DialogAppend_30829(Integer val)
	{
		return getHtmlAppends(val);
	}

	public static String DialogAppend_30830(Integer val)
	{
		return getHtmlAppends(val);
	}

	public static String DialogAppend_30831(Integer val)
	{
		return getHtmlAppends(val);
	}

	public static String DialogAppend_30869(Integer val)
	{
		return getHtmlAppends(val);
	}

	public static String DialogAppend_31067(Integer val)
	{
		return getHtmlAppends(val);
	}

	public static String DialogAppend_31265(Integer val)
	{
		return getHtmlAppends(val);
	}

	public static String DialogAppend_31309(Integer val)
	{
		return getHtmlAppends(val);
	}

	public static String DialogAppend_31954(Integer val)
	{
		return getHtmlAppends(val);
	}

	private static String getHtmlAppends(Integer val)
	{
		String ret = "";
		if(val != 0)
			return ret;
		if(Config.SERVICES_CHANGE_PET_NAME_ENABLED)
			ret = "<br>[scripts_services.petevolve.exchange:showErasePetName|Обнулить имя у пета]";
		if(Config.SERVICES_EXCHANGE_BABY_PET_ENABLED)
			ret += "<br>[scripts_services.petevolve.exchange:showBabyPetExchange|Обменять Improved Baby пета]";
		return ret;
	}

	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}
}
package services;

import l2rt.Config;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.L2Player;

public class Pushkin extends Functions implements ScriptFile
{
	public String DialogAppend_30300(Integer val)
	{
		if(val != 0 || !Config.ALT_SIMPLE_SIGNS && !Config.ALT_BS_CRYSTALLIZE)
			return "";

		StringBuilder append = new StringBuilder();

		if(((L2Player) getSelf()).isLangRus())
		{
			if(Config.ALT_SIMPLE_SIGNS)
			{
				append.append("<br>*Опции семи печатей:*<br>");
				append.append("[npc_%objectId%_Multisell 10061|Сделать S-грейд меч]<br1>");
				append.append("[npc_%objectId%_Multisell 40011|Вставить SA в оружие S-грейда]<br1>");
				append.append("[npc_%objectId%_Multisell 1008|Распечатать броню S-грейда]<br1>");
                append.append("[npc_%objectId%_Multisell 10081|Распечатать акссесуары S-грейда.]<br1>");
				append.append("[npc_%objectId%_Multisell 326150004|Распечатать броню S80 и S84 грейда]<br1>");
				append.append("[npc_%objectId%_Multisell 326150005|Распечатать акксесуары S80 и S84-грейда]<br1>");
				append.append("[npc_%objectId%_Multisell 1006|Сделать A-грейд меч]<br1>");
				append.append("[npc_%objectId%_Multisell 4001|Вставить SA в оружие A-грейда]<br1>");
				append.append("[npc_%objectId%_Multisell 1005|Распечатать броню A-грейда]<br1>");
				append.append("[npc_%objectId%_Multisell 1007|Запечатать броню A-грейда]<br1>");
				append.append("[npc_%objectId%_Multisell 4002|Удалить SA из оружия]<br1>");
				append.append("[npc_%objectId%_Multisell 9998|Обменять оружие с доплатой]<br1>");
				append.append("[npc_%objectId%_Multisell 9999|Обменять оружие на равноценное]<br1>");
				append.append("[npc_%objectId%_Multisell 311262516|Завершить редкую вещь]<br1>");
				append.append("[npc_%objectId%_Multisell 501|Купить что-нибудь]<br1>");
				append.append("[npc_%objectId%_Multisell 400|Обменять камни]<br1>");
				append.append("[npc_%objectId%_Multisell 500|Приобрести расходные материалы]");
			}
			if(Config.ALT_BS_CRYSTALLIZE)// TODO: сделать у всех кузнецов 
				append.append("<br1>[npc_%objectId%_Multisell 9997|Кристаллизация]");
		}
		else
		{
			if(Config.ALT_SIMPLE_SIGNS)
			{
				append.append("<br>*Seven Signs options:*<br>");
				append.append("[npc_%objectId%_Multisell 10061|Manufacture an S-grade sword]<br1>");
				append.append("[npc_%objectId%_Multisell 40011|Bestow the special S-grade weapon some abilities]<br1>");
				append.append("[npc_%objectId%_Multisell 1008|Release the S-grade armor seal]<br1>");
				append.append("[npc_%objectId%_Multisell 10081|Release the S-grade accessory seal]<br1>");
				append.append("[npc_%objectId%_Multisell 326150004|Release the S80-grade and S84-grade armor seal]<br1>");
				append.append("[npc_%objectId%_Multisell 326150005|Release the S80-grade and S84-grade accessory seal]<br1>");
				append.append("[npc_%objectId%_Multisell 1006|Manufacture an A-grade sword]<br1>");
				append.append("[npc_%objectId%_Multisell 4001|Bestow the special A-grade weapon some abilities]<br1>");
				append.append("[npc_%objectId%_Multisell 1005|Release the A-grade armor seal]<br1>");
				append.append("[npc_%objectId%_Multisell 1007|Seal the A-grade armor again]<br1>");
				append.append("[npc_%objectId%_Multisell 4002|Remove the special abilities from a weapon]<br1>");
				append.append("[npc_%objectId%_Multisell 9998|Upgrade weapon]<br1>");
				append.append("[npc_%objectId%_Multisell 9999|Make an even exchange of weapons]<br1>");
				append.append("[npc_%objectId%_Multisell 311262516|Complete a Foundation Item]<br1>");
				append.append("[npc_%objectId%_Multisell 501|Buy Something]<br1>");
				append.append("[npc_%objectId%_Multisell 400|Exchange Seal Stones]<br1>");
				append.append("[npc_%objectId%_Multisell 500|Purchase consumable items]<br1>");
			}
			if(Config.ALT_BS_CRYSTALLIZE)
				append.append("<br1>[npc_%objectId%_Multisell 9997|Crystallize]");
		}

		return append.toString();
	}

	public String DialogAppend_30086(Integer val)
	{
		return DialogAppend_30300(val);
	}

	public String DialogAppend_30098(Integer val)
	{
		if(val != 0 || !Config.ALT_ALLOW_TATTOO)
			return "";
		return ((L2Player) getSelf()).isLangRus() ? "<br>[npc_%objectId%_Multisell 6500|Купить тату]" : "<br>[npc_%objectId%_Multisell 6500|Buy tattoo]";
	}

	public void onLoad()
	{
		System.out.println("Loaded Service: Pushkin");
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}
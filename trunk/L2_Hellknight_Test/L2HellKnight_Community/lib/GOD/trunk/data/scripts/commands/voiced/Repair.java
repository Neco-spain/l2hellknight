package commands.voiced;

import java.util.Map.Entry;

import l2rt.database.mysql;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.handler.IVoicedCommandHandler;
import l2rt.gameserver.handler.VoicedCommandHandler;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2World;

public class Repair extends Functions implements IVoicedCommandHandler, ScriptFile
{
	private final String[] _commandList = new String[] { "repair" };

	public String[] getVoicedCommandList()
	{
		return _commandList;
	}

	public boolean useVoicedCommand(String command, L2Player activeChar, String target)
	{
		show("data/scripts/commands/voiced/repair.htm", activeChar);
		return true;
	}

	public void repair(String[] var)
	{
		if(var.length == 1)
		{
			String name = var[0];
			L2Player activeChar = (L2Player) getSelf();
			if(!activeChar.getAccountChars().containsValue(name))
			{
				show("You can't repair character not on same account!", activeChar);
				return;
			}
			if(activeChar.getName().equalsIgnoreCase(name))
			{
				show("You can't repair yourself!", activeChar);
				return;
			}
			if(L2World.getPlayer(name) != null)
			{
				show("This character is in game now. Please wait until it 'll be dropped.", activeChar);
				return;
			}
			for(Entry<Integer, String> entry : activeChar.getAccountChars().entrySet())
			{
				int obj_id = entry.getKey();
				String char_name = entry.getValue();
				if(!name.equalsIgnoreCase(char_name))
					continue;
				int karma = mysql.simple_get_int("karma", "characters", "`obj_Id`=" + obj_id);
				if(karma > 0)
					mysql.set("UPDATE `characters` SET `x`='17144', `y`='170156', `z`='-3502', `heading`='0' WHERE `obj_Id`='" + obj_id + "' LIMIT 1");
				else
				{
					mysql.set("UPDATE `characters` SET `x`='0', `y`='0', `z`='0', `heading`='0' WHERE `obj_Id`='" + obj_id + "' LIMIT 1");
					mysql.set("UPDATE `items` SET `loc`='WAREHOUSE', `loc_data`='0' WHERE `loc`='PAPERDOLL' AND `owner_id`=" + obj_id);
				}
				mysql.set("DELETE FROM `character_variables` WHERE `obj_id`='" + obj_id + "' AND `type`='user-var' AND `name`='reflection' LIMIT 1");

				show(activeChar.isLangRus() ? "Персонаж успешно отремонтирован. Все вещи перемешены на склад." : "Sucessfully repaired. All inventory moved to warehouse.", activeChar);
				break;
			}
		}
	}

	public void onLoad()
	{
		VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}
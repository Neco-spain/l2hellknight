package commands.voiced;

import l2rt.Config;
import l2rt.config.ConfigSystem;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.handler.IVoicedCommandHandler;
import l2rt.gameserver.handler.VoicedCommandHandler;
import l2rt.gameserver.model.L2Player;
import l2rt.util.Files;
import l2rt.util.PrintfFormat;

/**
 * @Author: Diamond
 * @Date: 10/07/2007
 * @Time: 15:07:08
 */
public class Lang extends Functions implements IVoicedCommandHandler, ScriptFile
{
	private String[] _commandList = new String[] { "lang", "cfg" };

	public static final PrintfFormat cfg_row = new PrintfFormat("<table><tr><td width=5></td><td width=120>%s:</td><td width=100>%s</td></tr></table>");
	public static final PrintfFormat cfg_button = new PrintfFormat("<button width=%d height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h user_cfg %s\" value=\"%s\">");

	public boolean useVoicedCommand(String command, L2Player activeChar, String args)
	{
		if(command.equals("lang"))
		{
			if(args != null)
				if(args.equalsIgnoreCase("en"))
					activeChar.setVar("lang@", "en");
				else if(args.equalsIgnoreCase("ru"))
					activeChar.setVar("lang@", "ru");
		}
		else if(command.equals("cfg"))
			if(args != null)
			{
				String[] param = args.split(" ");
				if(param.length == 2)
				{
					if(param[0].equalsIgnoreCase("dli"))
						if(param[1].equalsIgnoreCase("on"))
							activeChar.setVar("DroplistIcons", "1");
						else if(param[1].equalsIgnoreCase("of"))
							activeChar.unsetVar("DroplistIcons");

					if(param[0].equalsIgnoreCase("ssc"))
						if(param[1].equalsIgnoreCase("of") && ConfigSystem.getBoolean("SkillsShowChance"))
							activeChar.setVar("SkillsHideChance", "1");
						else if(param[1].equalsIgnoreCase("on"))
							activeChar.unsetVar("SkillsHideChance");

					if(param[0].equalsIgnoreCase("SkillsMobChance"))
						if(param[1].equalsIgnoreCase("on") && ConfigSystem.getBoolean("SkillsShowChance"))
							activeChar.setVar("SkillsMobChance", "1");
						else if(param[1].equalsIgnoreCase("of"))
							activeChar.unsetVar("SkillsMobChance");

					if(param[0].equalsIgnoreCase("noe"))
						if(param[1].equalsIgnoreCase("on"))
							activeChar.setVar("NoExp", "1");
						else if(param[1].equalsIgnoreCase("of"))
							activeChar.unsetVar("NoExp");

					if(param[0].equalsIgnoreCase("pf"))
						if(param[1].equalsIgnoreCase("of"))
							activeChar.setVar("no_pf", "1");
						else if(param[1].equalsIgnoreCase("on"))
							activeChar.unsetVar("no_pf");

					if(param[0].equalsIgnoreCase("trace"))
						if(param[1].equalsIgnoreCase("on"))
							activeChar.setVar("trace", "1");
						else if(param[1].equalsIgnoreCase("of"))
							activeChar.unsetVar("trace");

					if(param[0].equalsIgnoreCase("notraders"))
						if(param[1].equalsIgnoreCase("on"))
							activeChar.setVar("notraders", "1");
						else if(param[1].equalsIgnoreCase("of"))
							activeChar.unsetVar("notraders");

					if(param[0].equalsIgnoreCase("notShowBuffAnim"))
						if(param[1].equalsIgnoreCase("on"))
						{
							activeChar.setNotShowBuffAnim(true);
							activeChar.setVar("notShowBuffAnim", "1");
						}
						else if(param[1].equalsIgnoreCase("of"))
						{
							activeChar.setNotShowBuffAnim(false);
							activeChar.unsetVar("notShowBuffAnim");
						}

					if(param[0].equalsIgnoreCase("noShift"))
						if(param[1].equalsIgnoreCase("on"))
							activeChar.setVar("noShift", "1");
						else if(param[1].equalsIgnoreCase("of"))
							activeChar.unsetVar("noShift");

					if(param[0].equalsIgnoreCase("translit"))
						if(param[1].equalsIgnoreCase("on"))
							activeChar.setVar("translit", "tl");
						else if(param[1].equalsIgnoreCase("la"))
							activeChar.setVar("translit", "tc");
						else if(param[1].equalsIgnoreCase("of"))
							activeChar.unsetVar("translit");

					if(param[0].equalsIgnoreCase("autoloot"))
						activeChar.setAutoLoot(Boolean.parseBoolean(param[1]));

					if(param[0].equalsIgnoreCase("autolooth"))
						activeChar.setAutoLootHerbs(Boolean.parseBoolean(param[1]));
				}
			}

		String dialog = Files.read("data/scripts/commands/voiced/lang.htm", activeChar);

		dialog = dialog.replaceFirst("%lang%", activeChar.getVar("lang@").toUpperCase());
		dialog = dialog.replaceFirst("%dli%", activeChar.getVarB("DroplistIcons") ? "On" : "Off");
		dialog = dialog.replaceFirst("%noe%", activeChar.getVarB("NoExp") ? "On" : "Off");
		dialog = dialog.replaceFirst("%pf%", activeChar.getVarB("no_pf") ? "Off" : "On");
		dialog = dialog.replaceFirst("%trace%", activeChar.getVarB("trace") ? "On" : "Off");
		dialog = dialog.replaceFirst("%notraders%", activeChar.getVarB("notraders") ? "On" : "Off");
		dialog = dialog.replaceFirst("%notShowBuffAnim%", activeChar.getVarB("notShowBuffAnim") ? "On" : "Off");
		dialog = dialog.replaceFirst("%noShift%", activeChar.getVarB("noShift") ? "On" : "Off");

		if(!ConfigSystem.getBoolean("SkillsShowChance"))
		{
			dialog = dialog.replaceFirst("%ssc%", "N/A");
			dialog = dialog.replaceFirst("%SkillsMobChance%", "N/A");
		}
		else
		{
			if(!activeChar.getVarB("SkillsHideChance"))
				dialog = dialog.replaceFirst("%ssc%", "On");
			else
				dialog = dialog.replaceFirst("%ssc%", "Off");

			if(activeChar.getVarB("SkillsMobChance"))
				dialog = dialog.replaceFirst("%SkillsMobChance%", "On");
			else
				dialog = dialog.replaceFirst("%SkillsMobChance%", "Off");
		}

		String tl = activeChar.getVar("translit");
		if(tl == null)
			dialog = dialog.replaceFirst("%translit%", "Off");
		else if(tl.equals("tl"))
			dialog = dialog.replaceFirst("%translit%", "On");
		else
			dialog = dialog.replaceFirst("%translit%", "Lt");

		String additional = "";

		if(Config.AUTO_LOOT_INDIVIDUAL)
		{
			String bt;
			if(Config.AUTO_LOOT)
			{
				if(activeChar.isAutoLootEnabled())
					bt = cfg_button.sprintf(new Object[] { 100, "autoloot false",
							new CustomMessage("scripts.commands.voiced.Lang.Disable", activeChar).toString() });
				else
					bt = cfg_button.sprintf(new Object[] { 100, "autoloot true",
							new CustomMessage("scripts.commands.voiced.Lang.Enable", activeChar).toString() });
						additional += cfg_row.sprintf(new Object[] { "Auto-loot", bt });
				}
			if(Config.AUTO_LOOT_HERBS)
			{
				if(activeChar.isAutoLootHerbsEnabled())
					bt = cfg_button.sprintf(new Object[] { 100, "autolooth false",
							new CustomMessage("scripts.commands.voiced.Lang.Disable", activeChar).toString() });
				else
					bt = cfg_button.sprintf(new Object[] { 100, "autolooth true",
							new CustomMessage("scripts.commands.voiced.Lang.Enable", activeChar).toString() });
				additional += cfg_row.sprintf(new Object[] { "Auto-loot herbs", bt });
			}
		}

		dialog = dialog.replaceFirst("%additional%", additional);

		show(dialog, activeChar);

		return true;
	}

	public String[] getVoicedCommandList()
	{
		return _commandList;
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
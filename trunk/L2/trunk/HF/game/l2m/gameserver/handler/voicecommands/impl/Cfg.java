package l2m.gameserver.handler.voicecommands.impl;

import l2p.commons.text.PrintfFormat;
import l2m.gameserver.Config;
import l2m.gameserver.data.htm.HtmCache;
import l2m.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.entity.events.GlobalEvent;
import l2m.gameserver.scripts.Functions;
import l2m.gameserver.network.serverpackets.components.CustomMessage;
import org.apache.commons.lang3.math.NumberUtils;

public class Cfg extends Functions
  implements IVoicedCommandHandler
{
  private String[] _commandList = { "lang", "cfg" };

  public static final PrintfFormat cfg_row = new PrintfFormat("<table><tr><td width=5></td><td width=120>%s:</td><td width=100>%s</td></tr></table>");
  public static final PrintfFormat cfg_button = new PrintfFormat("<button width=%d back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h user_cfg %s\" value=\"%s\">");

  public boolean useVoicedCommand(String command, Player activeChar, String args)
  {
    if ((command.equals("cfg")) && 
      (args != null))
    {
      String[] param = args.split(" ");
      if (param.length == 2)
      {
        if (param[0].equalsIgnoreCase("dli")) {
          if (param[1].equalsIgnoreCase("on"))
            activeChar.setVar("DroplistIcons", "1", -1L);
          else if (param[1].equalsIgnoreCase("of"))
            activeChar.unsetVar("DroplistIcons");
        }
        if (param[0].equalsIgnoreCase("lang")) {
          if (param[1].equalsIgnoreCase("en"))
            activeChar.setVar("lang@", "en", -1L);
          else if (param[1].equalsIgnoreCase("ru"))
            activeChar.setVar("lang@", "ru", -1L);
        }
        if (param[0].equalsIgnoreCase("noe")) {
          if (param[1].equalsIgnoreCase("on"))
            activeChar.setVar("NoExp", "1", -1L);
          else if (param[1].equalsIgnoreCase("of"))
            activeChar.unsetVar("NoExp");
        }
        if (param[0].equalsIgnoreCase("notraders")) {
          if (param[1].equalsIgnoreCase("on"))
          {
            activeChar.setNotShowTraders(true);
            activeChar.setVar("notraders", "1", -1L);
          }
          else if (param[1].equalsIgnoreCase("of"))
          {
            activeChar.setNotShowTraders(false);
            activeChar.unsetVar("notraders");
          }
        }
        if (param[0].equalsIgnoreCase("notShowBuffAnim")) {
          if (param[1].equalsIgnoreCase("on"))
          {
            activeChar.setNotShowBuffAnim(true);
            activeChar.setVar("notShowBuffAnim", "1", -1L);
          }
          else if (param[1].equalsIgnoreCase("of"))
          {
            activeChar.setNotShowBuffAnim(false);
            activeChar.unsetVar("notShowBuffAnim");
          }
        }
        if (param[0].equalsIgnoreCase("noShift")) {
          if (param[1].equalsIgnoreCase("on"))
            activeChar.setVar("noShift", "1", -1L);
          else if (param[1].equalsIgnoreCase("of"))
            activeChar.unsetVar("noShift");
        }
        if (param[0].equalsIgnoreCase("sch")) {
          if (param[1].equalsIgnoreCase("on"))
            activeChar.setVar("SkillsHideChance", "1", -1L);
          else if (param[1].equalsIgnoreCase("of"))
            activeChar.unsetVar("SkillsHideChance");
        }
        if ((Config.SERVICES_ENABLE_NO_CARRIER) && (param[0].equalsIgnoreCase("noCarrier")))
        {
          int time = NumberUtils.toInt(param[1], Config.SERVICES_NO_CARRIER_DEFAULT_TIME);

          if (time > Config.SERVICES_NO_CARRIER_MAX_TIME)
            time = Config.SERVICES_NO_CARRIER_MAX_TIME;
          else if (time < Config.SERVICES_NO_CARRIER_MIN_TIME) {
            time = Config.SERVICES_NO_CARRIER_MIN_TIME;
          }
          activeChar.setVar("noCarrier", String.valueOf(time), -1L);
        }

        if (param[0].equalsIgnoreCase("translit")) {
          if (param[1].equalsIgnoreCase("on"))
            activeChar.setVar("translit", "tl", -1L);
          else if (param[1].equalsIgnoreCase("la"))
            activeChar.setVar("translit", "tc", -1L);
          else if (param[1].equalsIgnoreCase("of"))
            activeChar.unsetVar("translit");
        }
        if (param[0].equalsIgnoreCase("autoloot")) {
          activeChar.setAutoLoot(Boolean.parseBoolean(param[1]));
        }
        if (param[0].equalsIgnoreCase("autolooth")) {
          activeChar.setAutoLootHerbs(Boolean.parseBoolean(param[1]));
        }
      }
    }
    String dialog = HtmCache.getInstance().getNotNull("command/cfg.htm", activeChar);

    dialog = dialog.replaceFirst("%lang%", activeChar.getVar("lang@").toUpperCase());
    dialog = dialog.replaceFirst("%dli%", activeChar.getVarB("DroplistIcons") ? "On" : "Off");
    dialog = dialog.replaceFirst("%noe%", activeChar.getVarB("NoExp") ? "On" : "Off");
    dialog = dialog.replaceFirst("%notraders%", activeChar.getVarB("notraders") ? "On" : "Off");
    dialog = dialog.replaceFirst("%notShowBuffAnim%", activeChar.getVarB("notShowBuffAnim") ? "On" : "Off");
    dialog = dialog.replaceFirst("%noShift%", activeChar.getVarB("noShift") ? "On" : "Off");
    dialog = dialog.replaceFirst("%sch%", activeChar.getVarB("SkillsHideChance") ? "On" : "Off");
    dialog = dialog.replaceFirst("%noCarrier%", Config.SERVICES_ENABLE_NO_CARRIER ? "0" : activeChar.getVarB("noCarrier") ? activeChar.getVar("noCarrier") : "N/A");
    String tl = activeChar.getVar("translit");
    if (tl == null)
      dialog = dialog.replaceFirst("%translit%", "Off");
    else if (tl.equals("tl"))
      dialog = dialog.replaceFirst("%translit%", "On");
    else {
      dialog = dialog.replaceFirst("%translit%", "Lt");
    }
    String additional = "";

    if (Config.AUTO_LOOT_INDIVIDUAL)
    {
      String bt;
      String bt;
      if (activeChar.isAutoLootEnabled())
        bt = cfg_button.sprintf(new Object[] { Integer.valueOf(100), "autoloot false", new CustomMessage("common.Disable", activeChar, new Object[0]).toString() });
      else
        bt = cfg_button.sprintf(new Object[] { Integer.valueOf(100), "autoloot true", new CustomMessage("common.Enable", activeChar, new Object[0]).toString() });
      additional = new StringBuilder().append(additional).append(cfg_row.sprintf(new Object[] { "Auto-loot", bt })).toString();

      if (activeChar.isAutoLootHerbsEnabled())
        bt = cfg_button.sprintf(new Object[] { Integer.valueOf(100), "autolooth false", new CustomMessage("common.Disable", activeChar, new Object[0]).toString() });
      else
        bt = cfg_button.sprintf(new Object[] { Integer.valueOf(100), "autolooth true", new CustomMessage("common.Enable", activeChar, new Object[0]).toString() });
      additional = new StringBuilder().append(additional).append(cfg_row.sprintf(new Object[] { "Auto-loot herbs", bt })).toString();
    }

    dialog = dialog.replaceFirst("%additional%", additional);

    StringBuilder events = new StringBuilder();
    for (GlobalEvent e : activeChar.getEvents())
      events.append(e.toString()).append("<br>");
    dialog = dialog.replace("%events%", events.toString());

    show(dialog, activeChar);

    return true;
  }

  public String[] getVoicedCommandList()
  {
    return _commandList;
  }
}
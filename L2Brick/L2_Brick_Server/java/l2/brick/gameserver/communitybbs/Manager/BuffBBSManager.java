package l2.brick.gameserver.communitybbs.Manager;

import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;
/*
* @Author Janiko 
*/
import javolution.text.TextBuilder;
import l2.brick.gameserver.cache.HtmCache;
import l2.brick.gameserver.datatables.BuffBBSTable;
import l2.brick.gameserver.datatables.SkillTable;
import l2.brick.gameserver.model.L2Skill;
import l2.brick.gameserver.model.actor.L2Character;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.util.Util;

public class BuffBBSManager extends BaseBBSManager
{
  private static BuffBBSManager _instance = new BuffBBSManager();
  private static Logger _log = Logger.getLogger(BuffBBSManager.class.getName());

  public static BuffBBSManager getInstance()
  {
    if (_instance == null)
      _instance = new BuffBBSManager();
    return _instance;
  }

  @Override
@SuppressWarnings("rawtypes")
public void parsecmd(String command, L2PcInstance activeChar)
  {
    TextBuilder html = new TextBuilder("");

    int bufPet = 0;

    int idGroup = 0;
    String name = "";
    html.clear();
    html.append("<center>");
    html.append("<table>");
    html.append("<tr>");
    for (Map.Entry entry : BuffBBSTable.getInstance().getBBSGroups().entrySet())
    {
      idGroup = ((Integer)entry.getKey()).intValue();
      name = ((BuffBBSTable.BBSGroupBuffStat)entry.getValue()).getName();
      html.append("<td>");

      html.append("<button value=\"" + name + "\" action=\"bypass -h _bbs_buff;" + idGroup + "\" width=90 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
      html.append("</td>");
    }
    html.append("</tr>");
    html.append("</table>");
    html.append("</center><br><br>");
    String buffer_top = html.toString();

    html.clear();
    html.append("<center>");
    html.append("<table>");
    html.append("<tr>");
    html.append("<td>");
    html.append("<button value=\"Save Buff\" action=\"bypass -h _bbs_buff_save\" width=200 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
    html.append("</td>");
    html.append("<td>");
    html.append("<button value=\"Get Saved Buff\" action=\"bypass -h _bbs_buff_load\" width=200 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
    html.append("</td>");
    html.append("</tr>");
    html.append("</table>");
    html.append("</center>");
    String buffer_bottom = html.toString();

    if ((activeChar.getPet() != null) && (activeChar.getTarget() == activeChar.getPet())) bufPet = 1;

    if (command.startsWith("_bbs_buff;"))
    {
      StringTokenizer st = new StringTokenizer(command, ";");
      st.nextToken();
      int idGrp = Integer.parseInt(st.nextToken());

      if (idGrp == 0) {
        idGrp = 1;
      }
      int idSkill = 0;
      int lvlSkill = 0;
      int column = 0;
      String StringSkill = "";
      String skillIcon = "";
      String skillName = "";

      html.clear();
      html.append("<center>The cost of the buff in the group: <font color=F2C202>" + Util.formatAdena(BuffBBSTable.getInstance().getPriceGroup(idGrp)) + " Adena</font>.</center><br>");
      html.append("<table width=600>");
      html.append("<tr>");
      for (Map.Entry entry : BuffBBSTable.getInstance().getBBSBuffsForGoup(idGrp).entrySet())
      {
        column++;
        idSkill = ((Integer)entry.getKey()).intValue();
        lvlSkill = ((Integer)entry.getValue()).intValue();
        StringSkill = Integer.toString(idSkill);
        switch (StringSkill.length())
        {
        case 1:
          skillIcon = "icon.skill000" + idSkill;
          break;
        case 2:
          skillIcon = "icon.skill00" + idSkill;
          break;
        case 3:
          skillIcon = "icon.skill0" + idSkill;
          break;
        case 4:
          skillIcon = "icon.skill" + idSkill;
        }

        if ((idSkill == 4699) || (idSkill == 4700)) {
          skillIcon = "icon.skill1331";
        }
        if ((idSkill == 4702) || (idSkill == 4703)) {
          skillIcon = "icon.skill1332";
        }
        L2Skill skillBuff = SkillTable.getInstance().getInfo(idSkill, 1);
        if (skillBuff == null)
        {
          _log.warning("BuffBBSManager: skill id: " + idSkill + " not found");
          continue;
        }
        skillName = skillBuff.getName();
        html.append("<td width=150>");
        html.append("<center><img src=\"" + skillIcon + "\" width=32 height=32 align=center></center><br><center><a action=\"bypass -h _bbs_buff_skill;" + idGrp + ";" + idSkill + ";" + lvlSkill + "\">" + skillName + "</a></center>");
        html.append("</td>");
        if (column == 4)
        {
          html.append("</tr>");
          html.append("<tr>");
          column = 0;
        }
      }
      html.append("</tr>");
      html.append("</table>");
      String buffer_body = html.toString();

      String content = HtmCache.getInstance().getHtmForce(activeChar.getHtmlPrefix(), "data/html/CommunityBoard/buffer.htm");
      content = content.replace("%buffer_top%", buffer_top);
      content = content.replace("%buffer_bottom%", buffer_bottom);
      content = content.replace("%buffer_body%", buffer_body);
      separateAndSend(content, activeChar);
    }
    else if (command.startsWith("_bbs_buff_skill;"))
    {
      StringTokenizer st = new StringTokenizer(command, ";");
      st.nextToken();
      int sGrp = Integer.parseInt(st.nextToken());
      int sId = Integer.parseInt(st.nextToken());
      int sLvl = Integer.parseInt(st.nextToken());

      int price = BuffBBSTable.getInstance().getPriceGroup(sGrp);
      if (!activeChar.destroyItemByItemId("BuffBBS", 57, price, activeChar, true)) {
        return;
      }
      L2Character target = (L2Character)activeChar.getTarget();
      if (bufPet != 0)
      {
        L2Skill skill = SkillTable.getInstance().getInfo(sId, sLvl);
        skill.getEffects(target, target);
      }
      else
      {
        L2Skill skill = SkillTable.getInstance().getInfo(sId, sLvl);
        skill.getEffects(activeChar, activeChar);
      }
    }
    else if (command.startsWith("_bbs_buff_save"))
    {
      activeChar.updateBBSBuff(bufPet);
      activeChar.sendMessage("Buffs saved.");
    }
    else if (command.startsWith("_bbs_buff_load"))
    {
      int priceRebuff = activeChar.calcBBSBuff(bufPet);
      if ((priceRebuff > 0) && 
        (activeChar.destroyItemByItemId("BuffBBS", 57, priceRebuff, activeChar, true)))
        activeChar.cactBBSBuff(bufPet);
    }
  }

  @Override
public void parsewrite(String s, String s1, String s2, String s3, String s4, L2PcInstance l2pcinstance)
  {
  }
}
package scripts.commands.admincommandhandlers;

import java.util.StringTokenizer;
import java.util.logging.Logger;
import javolution.text.TextBuilder;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.SkillTreeTable;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2SkillLearn;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.PledgeSkillList;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2PcTemplate;
import scripts.commands.IAdminCommandHandler;

public class AdminSkill
  implements IAdminCommandHandler
{
  private static Logger _log = Logger.getLogger(AdminSkill.class.getName());

  private static final String[] ADMIN_COMMANDS = { "admin_show_skills", "admin_remove_skills", "admin_skill_list", "admin_skill_index", "admin_add_skill", "admin_remove_skill", "admin_get_skills", "admin_reset_skills", "admin_give_all_skills", "admin_remove_all_skills", "admin_add_clan_skill", "admin_rskills" };

  private static final int REQUIRED_LEVEL = Config.GM_CHAR_EDIT;
  private static final int REQUIRED_LEVEL2 = Config.GM_CHAR_EDIT_OTHER;
  private static L2Skill[] adminSkills;

  public boolean useAdminCommand(String command, L2PcInstance activeChar)
  {
    if ((!Config.ALT_PRIVILEGES_ADMIN) && (
      (!checkLevel(activeChar.getAccessLevel())) || (!activeChar.isGM()))) {
      return false;
    }
    GMAudit.auditGMAction(activeChar.getName(), command, activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target", "");

    if (command.equals("admin_show_skills"))
      showMainPage(activeChar);
    else if (command.startsWith("admin_remove_skills"))
    {
      try
      {
        String val = command.substring(20);
        removeSkillsPage(activeChar, Integer.parseInt(val));
      } catch (StringIndexOutOfBoundsException e) {
      }
    }
    else if (command.startsWith("admin_skill_list"))
    {
      AdminHelpPage.showHelpPage(activeChar, "skills.htm");
    }
    else if (command.startsWith("admin_skill_index"))
    {
      try
      {
        String val = command.substring(18);
        AdminHelpPage.showHelpPage(activeChar, "skills/" + val + ".htm");
      } catch (StringIndexOutOfBoundsException e) {
      }
    }
    else if (command.startsWith("admin_add_skill"))
    {
      try
      {
        String val = command.substring(15);
        if ((activeChar == activeChar.getTarget()) || (activeChar.getAccessLevel() >= REQUIRED_LEVEL2))
          adminAddSkill(activeChar, val);
      }
      catch (Exception e)
      {
        activeChar.sendAdmResultMessage("Usage: //add_skill <skill_id> <level>");
      }
    }
    else if (command.startsWith("admin_remove_skill"))
    {
      try
      {
        String id = command.substring(19);
        int idval = Integer.parseInt(id);
        if ((activeChar == activeChar.getTarget()) || (activeChar.getAccessLevel() >= REQUIRED_LEVEL2))
          adminRemoveSkill(activeChar, idval);
      }
      catch (Exception e)
      {
        activeChar.sendAdmResultMessage("Usage: //remove_skill <skill_id>");
      }
    }
    else if (command.equals("admin_get_skills"))
    {
      adminGetSkills(activeChar);
    }
    else if (command.equals("admin_reset_skills"))
    {
      if ((activeChar == activeChar.getTarget()) || (activeChar.getAccessLevel() >= REQUIRED_LEVEL2))
        adminResetSkills(activeChar);
    }
    else if (command.equals("admin_give_all_skills"))
    {
      if ((activeChar == activeChar.getTarget()) || (activeChar.getAccessLevel() >= REQUIRED_LEVEL2)) {
        adminGiveAllSkills(activeChar);
      }
    }
    else if (command.equals("admin_remove_all_skills"))
    {
      if (activeChar.getTarget().isPlayer())
      {
        L2PcInstance player = (L2PcInstance)activeChar.getTarget();
        for (L2Skill skill : player.getAllSkills())
          player.removeSkill(skill);
        activeChar.sendAdmResultMessage("You removed all skills from " + player.getName());
        player.sendAdmResultMessage("Admin removed all skills from you.");
        player.sendSkillList();
      }
    }
    else if (command.startsWith("admin_add_clan_skill"))
    {
      try
      {
        String[] val = command.split(" ");
        if ((activeChar == activeChar.getTarget()) || (activeChar.getAccessLevel() >= REQUIRED_LEVEL2))
          adminAddClanSkill(activeChar, Integer.parseInt(val[1]), Integer.parseInt(val[2]));
      }
      catch (Exception e)
      {
        activeChar.sendAdmResultMessage("Usage: //add_clan_skill <skill_id> <level>");
      }
    }
    else if (command.equals("admin_rskills"))
    {
      adminReloadSkills(activeChar);
    }
    return true;
  }

  private void adminGiveAllSkills(L2PcInstance activeChar)
  {
    L2Object target = activeChar.getTarget();
    L2PcInstance player = null;
    if (target.isPlayer()) {
      player = (L2PcInstance)target;
    }
    else {
      activeChar.sendPacket(SystemMessage.id(SystemMessageId.INCORRECT_TARGET));
      return;
    }
    boolean countUnlearnable = true;
    int unLearnable = 0;
    int skillCounter = 0;
    L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkills(player, player.getClassId());
    while (skills.length > unLearnable)
    {
      for (L2SkillLearn s : skills)
      {
        L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
        if ((sk == null) || (!sk.getCanLearn(player.getClassId())))
        {
          if (countUnlearnable)
            unLearnable++;
        }
        else {
          if (player.getSkillLevel(sk.getId()) == -1)
            skillCounter++;
          player.addSkill(sk, true);
        }
      }
      countUnlearnable = false;
      skills = SkillTreeTable.getInstance().getAvailableSkills(player, player.getClassId());
    }

    player.sendAdmResultMessage("A GM gave you " + skillCounter + " skills.");
    activeChar.sendAdmResultMessage("You gave " + skillCounter + " skills to " + player.getName());
    player.sendSkillList();
  }

  public String[] getAdminCommandList() {
    return ADMIN_COMMANDS;
  }

  private boolean checkLevel(int level) {
    return level >= REQUIRED_LEVEL;
  }

  private void removeSkillsPage(L2PcInstance activeChar, int page)
  {
    L2Object target = activeChar.getTarget();
    L2PcInstance player = null;
    if (target.isPlayer()) {
      player = (L2PcInstance)target;
    }
    else {
      activeChar.sendPacket(SystemMessage.id(SystemMessageId.TARGET_IS_INCORRECT));
      return;
    }

    L2Skill[] skills = player.getAllSkills();

    int MaxSkillsPerPage = 10;
    int MaxPages = skills.length / MaxSkillsPerPage;
    if (skills.length > MaxSkillsPerPage * MaxPages) {
      MaxPages++;
    }
    if (page > MaxPages) {
      page = MaxPages;
    }
    int SkillsStart = MaxSkillsPerPage * page;
    int SkillsEnd = skills.length;
    if (SkillsEnd - SkillsStart > MaxSkillsPerPage) {
      SkillsEnd = SkillsStart + MaxSkillsPerPage;
    }
    NpcHtmlMessage adminReply = NpcHtmlMessage.id(5);
    TextBuilder replyMSG = new TextBuilder("<html><body>");
    replyMSG.append("<table width=260><tr>");
    replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
    replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
    replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_show_skills\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
    replyMSG.append("</tr></table>");
    replyMSG.append("<br><br>");
    replyMSG.append("<center>Editing <font color=\"LEVEL\">" + player.getName() + "</font></center>");
    replyMSG.append("<br><table width=270><tr><td>Lv: " + player.getLevel() + " " + player.getTemplate().className + "</td></tr></table>");
    replyMSG.append("<br><table width=270><tr><td>Note: Dont forget that modifying players skills can</td></tr>");
    replyMSG.append("<tr><td>ruin the game...</td></tr></table>");
    replyMSG.append("<br><center>Click on the skill you wish to remove:</center>");
    replyMSG.append("<br>");
    String pages = "<center><table width=270><tr>";
    for (int x = 0; x < MaxPages; x++)
    {
      int pagenr = x + 1;
      pages = pages + "<td><a action=\"bypass -h admin_remove_skills " + x + "\">Page " + pagenr + "</a></td>";
    }
    pages = pages + "</tr></table></center>";
    replyMSG.append(pages);
    replyMSG.append("<br><table width=270>");
    replyMSG.append("<tr><td width=80>Name:</td><td width=60>Level:</td><td width=40>Id:</td></tr>");
    for (int i = SkillsStart; i < SkillsEnd; i++)
      replyMSG.append("<tr><td width=80><a action=\"bypass -h admin_remove_skill " + skills[i].getId() + "\">" + skills[i].getName() + "</a></td><td width=60>" + skills[i].getLevel() + "</td><td width=40>" + skills[i].getId() + "</td></tr>");
    replyMSG.append("</table>");
    replyMSG.append("<br><center><table>");
    replyMSG.append("Remove skill by ID :");
    replyMSG.append("<tr><td>Id: </td>");
    replyMSG.append("<td><edit var=\"id_to_remove\" width=110></td></tr>");
    replyMSG.append("</table></center>");
    replyMSG.append("<center><button value=\"Remove skill\" action=\"bypass -h admin_remove_skill $id_to_remove\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
    replyMSG.append("<br><center><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15></center>");
    replyMSG.append("</body></html>");
    adminReply.setHtml(replyMSG.toString());
    activeChar.sendPacket(adminReply);
  }

  private void showMainPage(L2PcInstance activeChar)
  {
    L2Object target = activeChar.getTarget();
    L2PcInstance player = null;
    if (target.isPlayer()) {
      player = (L2PcInstance)target;
    }
    else {
      activeChar.sendPacket(SystemMessage.id(SystemMessageId.INCORRECT_TARGET));
      return;
    }
    NpcHtmlMessage adminReply = NpcHtmlMessage.id(5);
    adminReply.setFile("data/html/admin/charskills.htm");
    adminReply.replace("%name%", player.getName());
    adminReply.replace("%level%", String.valueOf(player.getLevel()));
    adminReply.replace("%class%", player.getTemplate().className);
    activeChar.sendPacket(adminReply);
  }

  private void adminGetSkills(L2PcInstance activeChar)
  {
    L2Object target = activeChar.getTarget();
    L2PcInstance player = null;
    if (target.isPlayer()) {
      player = (L2PcInstance)target;
    }
    else {
      activeChar.sendPacket(SystemMessage.id(SystemMessageId.INCORRECT_TARGET));
      return;
    }
    if (player.getName().equals(activeChar.getName())) {
      player.sendPacket(SystemMessage.id(SystemMessageId.CANNOT_USE_ON_YOURSELF));
    }
    else {
      L2Skill[] skills = player.getAllSkills();
      adminSkills = activeChar.getAllSkills();
      for (int i = 0; i < adminSkills.length; i++)
        activeChar.removeSkill(adminSkills[i]);
      for (int i = 0; i < skills.length; i++)
        activeChar.addSkill(skills[i], true);
      activeChar.sendAdmResultMessage("You now have all the skills of " + player.getName() + ".");
      activeChar.sendSkillList();
    }
    showMainPage(activeChar);
  }

  private void adminResetSkills(L2PcInstance activeChar)
  {
    L2Object target = activeChar.getTarget();
    L2PcInstance player = null;
    if (target.isPlayer()) {
      player = (L2PcInstance)target;
    }
    else {
      activeChar.sendPacket(SystemMessage.id(SystemMessageId.INCORRECT_TARGET));
      return;
    }
    if (adminSkills == null) {
      activeChar.sendAdmResultMessage("You must get the skills of someone in order to do this.");
    }
    else {
      L2Skill[] skills = player.getAllSkills();
      for (int i = 0; i < skills.length; i++)
        player.removeSkill(skills[i]);
      for (int i = 0; i < activeChar.getAllSkills().length; i++)
        player.addSkill(activeChar.getAllSkills()[i], true);
      for (int i = 0; i < skills.length; i++)
        activeChar.removeSkill(skills[i]);
      for (int i = 0; i < adminSkills.length; i++)
        activeChar.addSkill(adminSkills[i], true);
      player.sendAdmResultMessage("[GM]" + activeChar.getName() + " updated your skills.");
      activeChar.sendAdmResultMessage("You now have all your skills back.");
      adminSkills = null;
      activeChar.sendSkillList();
    }
    showMainPage(activeChar);
  }

  private void adminAddSkill(L2PcInstance activeChar, String val)
  {
    L2Object target = activeChar.getTarget();
    L2PcInstance player = null;
    if (target.isPlayer()) {
      player = (L2PcInstance)target;
    }
    else {
      showMainPage(activeChar);
      activeChar.sendPacket(SystemMessage.id(SystemMessageId.INCORRECT_TARGET));
      return;
    }
    StringTokenizer st = new StringTokenizer(val);
    if (st.countTokens() != 2)
    {
      showMainPage(activeChar);
    }
    else
    {
      L2Skill skill = null;
      try
      {
        String id = st.nextToken();
        String level = st.nextToken();
        int idval = Integer.parseInt(id);
        int levelval = Integer.parseInt(level);
        skill = SkillTable.getInstance().getInfo(idval, levelval);
      } catch (Exception e) {
      }
      if (skill != null)
      {
        String name = skill.getName();
        player.sendAdmResultMessage("Admin gave you the skill " + name + ".");
        player.addSkill(skill, true);

        activeChar.sendAdmResultMessage("You gave the skill " + name + " to " + player.getName() + ".");
        if (Config.DEBUG)
          _log.fine("[GM]" + activeChar.getName() + " gave skill " + name + " to " + player.getName() + ".");
        activeChar.sendSkillList();
      }
      else {
        activeChar.sendAdmResultMessage("Error: there is no such skill.");
      }showMainPage(activeChar);
    }
  }

  private void adminRemoveSkill(L2PcInstance activeChar, int idval)
  {
    L2Object target = activeChar.getTarget();
    L2PcInstance player = null;
    if (target.isPlayer()) {
      player = (L2PcInstance)target;
    }
    else {
      activeChar.sendPacket(SystemMessage.id(SystemMessageId.INCORRECT_TARGET));
      return;
    }
    L2Skill skill = SkillTable.getInstance().getInfo(idval, player.getSkillLevel(idval));
    if (skill != null)
    {
      String skillname = skill.getName();
      player.sendAdmResultMessage("Admin removed the skill " + skillname + " from your skills list.");
      player.removeSkill(skill);

      activeChar.sendAdmResultMessage("You removed the skill " + skillname + " from " + player.getName() + ".");
      if (Config.DEBUG)
        _log.fine("[GM]" + activeChar.getName() + " removed skill " + skillname + " from " + player.getName() + ".");
      activeChar.sendSkillList();
    }
    else {
      activeChar.sendAdmResultMessage("Error: there is no such skill.");
    }removeSkillsPage(activeChar, 0);
  }

  private void adminAddClanSkill(L2PcInstance activeChar, int id, int level)
  {
    L2Object target = activeChar.getTarget();
    L2PcInstance player = null;
    if (target.isPlayer()) {
      player = (L2PcInstance)target;
    }
    else {
      showMainPage(activeChar);
      activeChar.sendPacket(SystemMessage.id(SystemMessageId.INCORRECT_TARGET));
      return;
    }
    if (!player.isClanLeader())
    {
      activeChar.sendPacket(SystemMessage.id(SystemMessageId.S1_IS_NOT_A_CLAN_LEADER).addString(player.getName()));
      showMainPage(activeChar);
      return;
    }
    if ((id < 370) || (id > 391) || (level < 1) || (level > 3))
    {
      activeChar.sendAdmResultMessage("Usage: //add_clan_skill <skill_id> <level>");
      showMainPage(activeChar);
      return;
    }

    L2Skill skill = SkillTable.getInstance().getInfo(id, level);
    if (skill != null)
    {
      String skillname = skill.getName();
      SystemMessage sm = SystemMessage.id(SystemMessageId.CLAN_SKILL_S1_ADDED);
      sm.addSkillName(id);
      player.sendPacket(sm);
      player.getClan().broadcastToOnlineMembers(sm);
      player.getClan().addNewSkill(skill);
      activeChar.sendAdmResultMessage("You gave the Clan Skill: " + skillname + " to the clan " + player.getClan().getName() + ".");

      activeChar.getClan().broadcastToOnlineMembers(new PledgeSkillList(activeChar.getClan()));
      for (L2PcInstance member : activeChar.getClan().getOnlineMembers(""))
      {
        member.sendSkillList();
      }

      showMainPage(activeChar);
      return;
    }

    activeChar.sendAdmResultMessage("Error: there is no such skill.");
  }

  private void adminReloadSkills(L2PcInstance activeChar)
  {
    L2Object target = activeChar.getTarget();
    L2PcInstance player = null;
    if (target.isPlayer()) {
      player = (L2PcInstance)target;
    }
    else {
      activeChar.sendPacket(SystemMessage.id(SystemMessageId.INCORRECT_TARGET));
      return;
    }

    player.reloadSkills();

    activeChar.sendAdmResultMessage("\u041E\u0442\u043A\u0430\u0442 \u0441\u043A\u0438\u043B\u043B\u043E\u0432 \u0438\u0433\u0440\u043E\u043A\u0443 " + player.getName());
  }
}
package l2m.gameserver.handler.admincommands.impl;

import java.util.Collection;
import java.util.List;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.data.xml.holder.SkillAcquireHolder;
import l2m.gameserver.handler.admincommands.IAdminCommandHandler;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.model.EffectList;
import l2m.gameserver.model.GameObject;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.SkillLearn;
import l2m.gameserver.model.base.AcquireType;
import l2m.gameserver.model.base.PlayerAccess;
import l2m.gameserver.network.serverpackets.NpcHtmlMessage;
import l2m.gameserver.network.serverpackets.SkillCoolTime;
import l2m.gameserver.network.serverpackets.SkillList;
import l2m.gameserver.skills.Calculator;
import l2m.gameserver.skills.Env;
import l2m.gameserver.skills.Stats;
import l2m.gameserver.skills.conditions.Condition;
import l2m.gameserver.skills.funcs.Func;
import l2m.gameserver.data.tables.SkillTable;
import l2m.gameserver.templates.PlayerTemplate;
import l2m.gameserver.utils.CertificationFunctions;
import l2m.gameserver.utils.Log;

public class AdminSkill
  implements IAdminCommandHandler
{
  private static Skill[] adminSkills;

  public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    if (!activeChar.getPlayerAccess().CanEditChar) {
      return false;
    }
    switch (1.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminSkill$Commands[command.ordinal()])
    {
    case 1:
      showSkillsPage(activeChar);
      break;
    case 2:
      showEffects(activeChar);
      break;
    case 3:
      removeSkillsPage(activeChar);
      break;
    case 4:
      activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/skills.htm"));
      break;
    case 5:
      if (wordList.length <= 1) break;
      activeChar.sendPacket(new NpcHtmlMessage(5).setFile(new StringBuilder().append("admin/skills/").append(wordList[1]).append(".htm").toString())); break;
    case 6:
      adminAddSkill(activeChar, wordList);
      break;
    case 7:
      adminRemoveSkill(activeChar, wordList);
      break;
    case 8:
      adminGetSkills(activeChar);
      break;
    case 9:
      adminResetSkills(activeChar);
      break;
    case 10:
      adminGiveAllSkills(activeChar);
      break;
    case 11:
      debug_stats(activeChar);
      break;
    case 12:
      activeChar.resetReuse();
      activeChar.sendPacket(new SkillCoolTime(activeChar));
      activeChar.sendMessage("\u041E\u0442\u043A\u0430\u0442 \u0432\u0441\u0435\u0445 \u0441\u043A\u0438\u043B\u043E\u0432 \u043E\u0431\u043D\u0443\u043B\u0435\u043D.");
      break;
    case 13:
      CertificationFunctions.removeAllSkill();
      break;
    case 14:
      for (int i = 7041; i <= 7064; i++)
        activeChar.addSkill(SkillTable.getInstance().getInfo(i, 1));
      activeChar.sendPacket(new SkillList(activeChar));
    }

    return true;
  }

  private void debug_stats(Player activeChar)
  {
    GameObject target_obj = activeChar.getTarget();
    if (!target_obj.isCreature())
    {
      activeChar.sendPacket(Msg.INVALID_TARGET);
      return;
    }

    Creature target = (Creature)target_obj;

    Calculator[] calculators = target.getCalculators();

    String log_str = new StringBuilder().append("--- Debug for ").append(target.getName()).append(" ---\r\n").toString();

    for (Calculator calculator : calculators)
    {
      if (calculator == null)
        continue;
      Env env = new Env(target, activeChar, null);
      env.value = calculator.getBase();
      log_str = new StringBuilder().append(log_str).append("Stat: ").append(calculator._stat.getValue()).append(", prevValue: ").append(calculator.getLast()).append("\r\n").toString();
      Func[] funcs = calculator.getFunctions();
      for (int i = 0; i < funcs.length; i++)
      {
        String order = Integer.toHexString(funcs[i].order).toUpperCase();
        if (order.length() == 1)
          order = new StringBuilder().append("0").append(order).toString();
        log_str = new StringBuilder().append(log_str).append("\tFunc #").append(i).append("@ [0x").append(order).append("]").append(funcs[i].getClass().getSimpleName()).append("\t").append(env.value).toString();
        if ((funcs[i].getCondition() == null) || (funcs[i].getCondition().test(env)))
          funcs[i].calc(env);
        log_str = new StringBuilder().append(log_str).append(" -> ").append(env.value).append(funcs[i].owner != null ? new StringBuilder().append("; owner: ").append(funcs[i].owner.toString()).toString() : "; no owner").append("\r\n").toString();
      }
    }

    Log.add(log_str, "debug_stats");
  }

  private void adminGiveAllSkills(Player activeChar)
  {
    GameObject target = activeChar.getTarget();
    Player player = null;
    if ((target != null) && (target.isPlayer()) && ((activeChar == target) || (activeChar.getPlayerAccess().CanEditCharAll))) {
      player = (Player)target;
    }
    else {
      activeChar.sendPacket(Msg.INVALID_TARGET);
      return;
    }
    int unLearnable = 0;
    int skillCounter = 0;
    Collection skills = SkillAcquireHolder.getInstance().getAvailableSkills(player, AcquireType.NORMAL);
    while (skills.size() > unLearnable)
    {
      unLearnable = 0;
      for (SkillLearn s : skills)
      {
        Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
        if ((sk == null) || (!sk.getCanLearn(player.getClassId())))
        {
          unLearnable++;
          continue;
        }
        if (player.getSkillLevel(Integer.valueOf(sk.getId())) == -1)
          skillCounter++;
        player.addSkill(sk, true);
      }
      skills = SkillAcquireHolder.getInstance().getAvailableSkills(player, AcquireType.NORMAL);
    }

    player.sendMessage(new StringBuilder().append("Admin gave you ").append(skillCounter).append(" skills.").toString());
    player.sendPacket(new SkillList(player));
    activeChar.sendMessage(new StringBuilder().append("You gave ").append(skillCounter).append(" skills to ").append(player.getName()).toString());
  }

  public Enum[] getAdminCommandEnum()
  {
    return Commands.values();
  }

  private void removeSkillsPage(Player activeChar)
  {
    GameObject target = activeChar.getTarget();
    Player player;
    if ((target.isPlayer()) && ((activeChar == target) || (activeChar.getPlayerAccess().CanEditCharAll))) {
      player = (Player)target;
    }
    else {
      activeChar.sendPacket(Msg.INVALID_TARGET);
      return;
    }
    Player player;
    Collection skills = player.getAllSkills();
    for (Skill skill : skills)
    {
      player.removeSkill(skill, true);
      player.sendPacket(new SkillList(player));
    }
  }

  private void showSkillsPage(Player activeChar)
  {
    GameObject target = activeChar.getTarget();
    Player player;
    if ((target != null) && (target.isPlayer()) && ((activeChar == target) || (activeChar.getPlayerAccess().CanEditCharAll))) {
      player = (Player)target;
    }
    else {
      activeChar.sendPacket(Msg.INVALID_TARGET);
      return;
    }
    Player player;
    NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

    StringBuilder replyMSG = new StringBuilder("<html><body>");
    replyMSG.append("<table width=260><tr>");
    replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
    replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
    replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
    replyMSG.append("</tr></table>");
    replyMSG.append("<br><br>");
    replyMSG.append(new StringBuilder().append("<center>Editing character: ").append(player.getName()).append("</center>").toString());
    replyMSG.append(new StringBuilder().append("<br><table width=270><tr><td>Lv: ").append(player.getLevel()).append(" ").append(player.getTemplate().className).append("</td></tr></table>").toString());
    replyMSG.append("<br><center><table>");
    replyMSG.append("<tr><td><button value=\"Add skills\" action=\"bypass -h admin_skill_list\" width=70 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
    replyMSG.append("<td><button value=\"Get skills\" action=\"bypass -h admin_get_skills\" width=70 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
    replyMSG.append("<tr><td><button value=\"Delete skills\" action=\"bypass -h admin_remove_skills\" width=70 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
    replyMSG.append("<td><button value=\"Reset skills\" action=\"bypass -h admin_reset_skills\" width=70 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
    replyMSG.append("<tr><td><button value=\"Give All Skills\" action=\"bypass -h admin_give_all_skills\" width=70 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
    replyMSG.append("</table></center>");
    replyMSG.append("</body></html>");

    adminReply.setHtml(replyMSG.toString());
    activeChar.sendPacket(adminReply);
  }

  private void showEffects(Player activeChar)
  {
    GameObject target = activeChar.getTarget();
    Player player;
    if ((target != null) && (target.isPlayer()) && ((activeChar == target) || (activeChar.getPlayerAccess().CanEditCharAll))) {
      player = (Player)target;
    }
    else {
      activeChar.sendPacket(Msg.INVALID_TARGET);
      return;
    }
    Player player;
    NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

    StringBuilder replyMSG = new StringBuilder("<html><body>");
    replyMSG.append("<table width=260><tr>");
    replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
    replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
    replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
    replyMSG.append("</tr></table>");
    replyMSG.append("<br><br>");
    replyMSG.append(new StringBuilder().append("<center>Editing character: ").append(player.getName()).append("</center>").toString());

    replyMSG.append("<br><center><button value=\"");
    replyMSG.append(player.isLangRus() ? "\u041E\u0431\u043D\u043E\u0432\u0438\u0442\u044C" : "Refresh");
    replyMSG.append("\" action=\"bypass -h admin_show_effects\" width=100 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" /></center>");
    replyMSG.append("<br>");

    List list = player.getEffectList().getAllEffects();
    if ((list != null) && (!list.isEmpty()))
      for (Effect e : list)
        replyMSG.append(e.getSkill().getName()).append(" ").append(e.getSkill().getLevel()).append(" - ").append(e.getSkill().isToggle() ? "Infinity" : new StringBuilder().append(e.getTimeLeft()).append(" seconds").toString()).append("<br1>");
    replyMSG.append("<br></body></html>");

    adminReply.setHtml(replyMSG.toString());
    activeChar.sendPacket(adminReply);
  }

  private void adminGetSkills(Player activeChar)
  {
    GameObject target = activeChar.getTarget();
    Player player;
    if ((target.isPlayer()) && ((activeChar == target) || (activeChar.getPlayerAccess().CanEditCharAll))) {
      player = (Player)target;
    }
    else {
      activeChar.sendPacket(Msg.INVALID_TARGET);
      return;
    }
    Player player;
    if (player.getName().equals(activeChar.getName())) {
      player.sendMessage("There is no point in doing it on your character.");
    }
    else {
      Collection skills = player.getAllSkills();
      adminSkills = activeChar.getAllSkillsArray();
      for (Skill element : adminSkills)
        activeChar.removeSkill(element, true);
      for (Skill element : skills)
        activeChar.addSkill(element, true);
      activeChar.sendMessage(new StringBuilder().append("You now have all the skills of  ").append(player.getName()).append(".").toString());
    }

    showSkillsPage(activeChar);
  }

  private void adminResetSkills(Player activeChar)
  {
    GameObject target = activeChar.getTarget();
    Player player = null;
    if ((target.isPlayer()) && ((activeChar == target) || (activeChar.getPlayerAccess().CanEditCharAll))) {
      player = (Player)target;
    }
    else {
      activeChar.sendPacket(Msg.INVALID_TARGET);
      return;
    }

    Skill[] skills = player.getAllSkillsArray();
    int counter = 0;

    player.checkSkills();
    player.sendPacket(new SkillList(player));
    player.sendMessage(new StringBuilder().append("[GM]").append(activeChar.getName()).append(" has updated your skills.").toString());
    activeChar.sendMessage(new StringBuilder().append(counter).append(" skills removed.").toString());

    showSkillsPage(activeChar);
  }

  private void adminAddSkill(Player activeChar, String[] wordList)
  {
    GameObject target = activeChar.getTarget();
    Player player;
    if ((target != null) && (target.isPlayer()) && ((activeChar == target) || (activeChar.getPlayerAccess().CanEditCharAll))) {
      player = (Player)target;
    }
    else {
      activeChar.sendPacket(Msg.INVALID_TARGET);
      return;
    }
    Player player;
    if (wordList.length == 3)
    {
      int id = Integer.parseInt(wordList[1]);
      int level = Integer.parseInt(wordList[2]);
      Skill skill = SkillTable.getInstance().getInfo(id, level);
      if (skill != null)
      {
        player.sendMessage(new StringBuilder().append("Admin gave you the skill ").append(skill.getName()).append(".").toString());
        player.addSkill(skill, true);
        player.sendPacket(new SkillList(player));
        activeChar.sendMessage(new StringBuilder().append("You gave the skill ").append(skill.getName()).append(" to ").append(player.getName()).append(".").toString());
      }
      else {
        activeChar.sendMessage("Error: there is no such skill.");
      }
    }
    showSkillsPage(activeChar);
  }

  private void adminRemoveSkill(Player activeChar, String[] wordList)
  {
    GameObject target = activeChar.getTarget();
    Player player = null;
    if ((target.isPlayer()) && ((activeChar == target) || (activeChar.getPlayerAccess().CanEditCharAll))) {
      player = (Player)target;
    }
    else {
      activeChar.sendPacket(Msg.INVALID_TARGET);
      return;
    }

    if (wordList.length == 2)
    {
      int id = Integer.parseInt(wordList[1]);
      int level = player.getSkillLevel(Integer.valueOf(id));
      Skill skill = SkillTable.getInstance().getInfo(id, level);
      if (skill != null)
      {
        player.sendMessage(new StringBuilder().append("Admin removed the skill ").append(skill.getName()).append(".").toString());
        player.removeSkill(skill, true);
        player.sendPacket(new SkillList(player));
        activeChar.sendMessage(new StringBuilder().append("You removed the skill ").append(skill.getName()).append(" from ").append(player.getName()).append(".").toString());
      }
      else {
        activeChar.sendMessage("Error: there is no such skill.");
      }
    }
    removeSkillsPage(activeChar);
  }

  private static enum Commands
  {
    admin_show_skills, 
    admin_remove_skills, 
    admin_skill_list, 
    admin_skill_index, 
    admin_add_skill, 
    admin_remove_skill, 
    admin_get_skills, 
    admin_reset_skills, 
    admin_give_all_skills, 
    admin_show_effects, 
    admin_debug_stats, 
    admin_remove_cooldown, 
    admin_remove_all_certification_skills, 
    admin_buff;
  }
}
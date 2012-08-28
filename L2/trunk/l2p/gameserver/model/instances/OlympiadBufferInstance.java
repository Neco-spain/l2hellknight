package l2p.gameserver.model.instances;

import gnu.trove.TIntHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import l2p.gameserver.ai.CtrlIntention;
import l2p.gameserver.ai.PlayerAI;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.scripts.Events;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.MagicSkillUse;
import l2p.gameserver.serverpackets.MyTargetSelected;
import l2p.gameserver.serverpackets.ValidateLocation;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.templates.npc.NpcTemplate;

public class OlympiadBufferInstance extends NpcInstance
{
  public static final long serialVersionUID = 1L;
  private TIntHashSet buffs = new TIntHashSet();

  public OlympiadBufferInstance(int objectId, NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onAction(Player player, boolean shift)
  {
    if (Events.onAction(player, this, shift))
    {
      player.sendActionFailed();
      return;
    }

    if (this != player.getTarget())
    {
      player.setTarget(this);
      MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
      player.sendPacket(my);
      player.sendPacket(new ValidateLocation(this));
    }
    else
    {
      MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
      player.sendPacket(my);
      if (!isInRange(player, 200L))
        player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
      else if (buffs.size() > 4)
        showChatWindow(player, 1, new Object[0]);
      else
        showChatWindow(player, 0, new Object[0]);
      player.sendActionFailed();
    }
  }

  public void onBypassFeedback(Player player, String command)
  {
    if (!canBypassCheck(player, this)) {
      return;
    }
    if (buffs.size() > 4) {
      showChatWindow(player, 1, new Object[0]);
    }
    if (command.startsWith("Buff"))
    {
      int id = 0;
      int lvl = 0;
      StringTokenizer st = new StringTokenizer(command, " ");
      st.nextToken();
      id = Integer.parseInt(st.nextToken());
      lvl = Integer.parseInt(st.nextToken());
      Skill skill = SkillTable.getInstance().getInfo(id, lvl);
      List target = new ArrayList();
      target.add(player);
      broadcastPacket(new L2GameServerPacket[] { new MagicSkillUse(this, player, id, lvl, 0, 0L) });
      callSkill(skill, target, true);
      buffs.add(id);
      if (buffs.size() > 4)
        showChatWindow(player, 1, new Object[0]);
      else
        showChatWindow(player, 0, new Object[0]);
    }
    else {
      showChatWindow(player, 0, new Object[0]);
    }
  }

  public String getHtmlPath(int npcId, int val, Player player)
  {
    String pom;
    String pom;
    if (val == 0)
      pom = "buffer";
    else {
      pom = "buffer-" + val;
    }

    return "olympiad/" + pom + ".htm";
  }
}
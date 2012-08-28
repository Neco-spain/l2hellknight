package l2m.gameserver.model.instances;

import l2m.gameserver.model.Player;
import l2m.gameserver.templates.npc.NpcTemplate;

public final class TrainerInstance extends NpcInstance
{
  public static final long serialVersionUID = 1L;

  public TrainerInstance(int objectId, NpcTemplate template)
  {
    super(objectId, template);
  }

  public String getHtmlPath(int npcId, int val, Player player)
  {
    String pom = "";
    if (val == 0)
      pom = "" + npcId;
    else {
      pom = npcId + "-" + val;
    }
    return "trainer/" + pom + ".htm";
  }
}
package l2m.gameserver.model.instances;

import l2m.gameserver.model.Player;
import l2m.gameserver.templates.npc.NpcTemplate;

public class NoActionNpcInstance extends NpcInstance
{
  public static final long serialVersionUID = 1L;

  public NoActionNpcInstance(int objectID, NpcTemplate template)
  {
    super(objectID, template);
  }

  public void onAction(Player player, boolean dontMove)
  {
    player.sendActionFailed();
  }
}
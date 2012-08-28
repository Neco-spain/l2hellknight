package l2p.gameserver.model.instances;

import l2p.gameserver.model.Player;
import l2p.gameserver.templates.npc.NpcTemplate;

@Deprecated
public class NoActionNpcInstance extends NpcInstance
{
  public NoActionNpcInstance(int objectID, NpcTemplate template)
  {
    super(objectID, template);
  }

  public void onAction(Player player, boolean dontMove)
  {
    player.sendActionFailed();
  }
}
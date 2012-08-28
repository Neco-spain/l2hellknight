package l2p.gameserver.model.instances;

import l2p.gameserver.templates.npc.NpcTemplate;

public class NpcNotSayInstance extends NpcInstance
{
  public static final long serialVersionUID = 1L;

  public NpcNotSayInstance(int objectID, NpcTemplate template)
  {
    super(objectID, template);
    setHasChatWindow(false);
  }
}
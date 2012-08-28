package l2m.gameserver.model.instances;

import l2m.gameserver.templates.npc.NpcTemplate;

public class NpcNotSayInstance extends NpcInstance
{
  public static final long serialVersionUID = 1L;

  public NpcNotSayInstance(int objectID, NpcTemplate template)
  {
    super(objectID, template);
    setHasChatWindow(false);
  }
}
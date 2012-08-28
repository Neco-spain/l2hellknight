package l2p.gameserver.model.petition;

import java.util.Map;
import l2p.gameserver.handler.petition.IPetitionHandler;
import l2p.gameserver.scripts.Scripts;

public class PetitionSubGroup extends PetitionGroup
{
  private final IPetitionHandler _handler;

  public PetitionSubGroup(int id, String handler)
  {
    super(id);

    Class clazz = (Class)Scripts.getInstance().getClasses().get("handler.petition." + handler);
    try
    {
      _handler = ((IPetitionHandler)clazz.newInstance());
    }
    catch (Exception e)
    {
      throw new Error(e);
    }
  }

  public IPetitionHandler getHandler()
  {
    return _handler;
  }
}
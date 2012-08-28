package l2m.gameserver.model.instances;

import l2m.gameserver.model.Player;
import l2m.gameserver.templates.npc.NpcTemplate;

public class BlockInstance extends NpcInstance
{
  public static final long serialVersionUID = 1L;
  private boolean _isRed;

  public BlockInstance(int objectId, NpcTemplate template)
  {
    super(objectId, template);
  }

  public boolean isRed()
  {
    return _isRed;
  }

  public void setRed(boolean red)
  {
    _isRed = red;
    broadcastCharInfo();
  }

  public void changeColor()
  {
    setRed(!_isRed);
  }

  public void showChatWindow(Player player, int val, Object[] arg)
  {
  }

  public boolean isNameAbove()
  {
    return false;
  }

  public int getFormId()
  {
    return _isRed ? 83 : 0;
  }

  public boolean isInvul()
  {
    return true;
  }
}
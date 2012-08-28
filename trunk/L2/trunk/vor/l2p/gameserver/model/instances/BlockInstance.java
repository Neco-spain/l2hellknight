package l2p.gameserver.model.instances;

import l2p.gameserver.model.Player;
import l2p.gameserver.templates.npc.NpcTemplate;

public class BlockInstance extends NpcInstance
{
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
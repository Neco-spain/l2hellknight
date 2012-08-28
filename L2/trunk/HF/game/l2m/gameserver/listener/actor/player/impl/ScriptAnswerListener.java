package l2m.gameserver.listener.actor.player.impl;

import l2p.commons.lang.reference.HardReference;
import l2m.gameserver.listener.actor.player.OnAnswerListener;
import l2m.gameserver.model.Player;
import l2m.gameserver.scripts.Scripts;

public class ScriptAnswerListener
  implements OnAnswerListener
{
  private HardReference<Player> _playerRef;
  private String _scriptName;
  private Object[] _arg;

  public ScriptAnswerListener(Player player, String scriptName, Object[] arg)
  {
    _scriptName = scriptName;
    _arg = arg;
    _playerRef = player.getRef();
  }

  public void sayYes()
  {
    Player player = (Player)_playerRef.get();
    if (player == null) {
      return;
    }
    Scripts.getInstance().callScripts(player, _scriptName.split(":")[0], _scriptName.split(":")[1], _arg);
  }

  public void sayNo()
  {
  }
}
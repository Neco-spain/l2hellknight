package l2m.gameserver.scripts;

import java.util.Map;
import l2m.gameserver.model.GameObject;
import l2m.gameserver.model.Player;
import l2m.gameserver.utils.Strings;

public final class Events
{
  public static boolean onAction(Player player, GameObject obj, boolean shift)
  {
    if (shift)
    {
      if (player.getVarB("noShift"))
        return false;
      Scripts.ScriptClassAndMethod handler = (Scripts.ScriptClassAndMethod)Scripts.onActionShift.get(obj.getL2ClassShortName());
      if ((handler == null) && (obj.isNpc()))
        handler = (Scripts.ScriptClassAndMethod)Scripts.onActionShift.get("NpcInstance");
      if ((handler == null) && (obj.isPet()))
        handler = (Scripts.ScriptClassAndMethod)Scripts.onActionShift.get("PetInstance");
      if (handler == null)
        return false;
      return Strings.parseBoolean(Scripts.getInstance().callScripts(player, handler.className, handler.methodName, new Object[] { player, obj })).booleanValue();
    }

    Scripts.ScriptClassAndMethod handler = (Scripts.ScriptClassAndMethod)Scripts.onAction.get(obj.getL2ClassShortName());
    if ((handler == null) && (obj.isDoor()))
      handler = (Scripts.ScriptClassAndMethod)Scripts.onAction.get("DoorInstance");
    if (handler == null)
      return false;
    return Strings.parseBoolean(Scripts.getInstance().callScripts(player, handler.className, handler.methodName, new Object[] { player, obj })).booleanValue();
  }
}
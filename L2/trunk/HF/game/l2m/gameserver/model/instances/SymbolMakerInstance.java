package l2m.gameserver.model.instances;

import l2m.gameserver.model.Player;
import l2m.gameserver.network.serverpackets.HennaEquipList;
import l2m.gameserver.network.serverpackets.HennaUnequipList;
import l2m.gameserver.templates.npc.NpcTemplate;

public class SymbolMakerInstance extends NpcInstance
{
  public static final long serialVersionUID = 1L;

  public SymbolMakerInstance(int objectID, NpcTemplate template)
  {
    super(objectID, template);
  }

  public void onBypassFeedback(Player player, String command)
  {
    if (!canBypassCheck(player, this)) {
      return;
    }
    if (command.equals("Draw"))
      player.sendPacket(new HennaEquipList(player));
    else if (command.equals("RemoveList"))
      player.sendPacket(new HennaUnequipList(player));
    else
      super.onBypassFeedback(player, command);
  }

  public String getHtmlPath(int npcId, int val, Player player)
  {
    String pom;
    String pom;
    if (val == 0)
      pom = "SymbolMaker";
    else {
      pom = "SymbolMaker-" + val;
    }
    return "symbolmaker/" + pom + ".htm";
  }
}
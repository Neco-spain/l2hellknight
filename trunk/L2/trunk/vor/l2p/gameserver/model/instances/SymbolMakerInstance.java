package l2p.gameserver.model.instances;

import l2p.gameserver.model.Player;
import l2p.gameserver.serverpackets.HennaEquipList;
import l2p.gameserver.serverpackets.HennaUnequipList;
import l2p.gameserver.templates.npc.NpcTemplate;

public class SymbolMakerInstance extends NpcInstance
{
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
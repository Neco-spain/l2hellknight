package l2m.gameserver.model.instances;

import java.util.StringTokenizer;
import l2p.commons.util.Rnd;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.model.Player;
import l2m.gameserver.scripts.Functions;
import l2m.gameserver.network.serverpackets.components.NpcString;
import l2m.gameserver.templates.npc.NpcTemplate;
import l2m.gameserver.utils.ItemFunctions;

public class WeaverInstance extends MerchantInstance
{
  public static final long serialVersionUID = 1L;

  public WeaverInstance(int objectId, NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onBypassFeedback(Player player, String command)
  {
    if (!canBypassCheck(player, this)) {
      return;
    }
    StringTokenizer st = new StringTokenizer(command, " ");
    String actualCommand = st.nextToken();

    if (actualCommand.equalsIgnoreCase("unseal"))
    {
      int cost = Integer.parseInt(st.nextToken());
      int id = Integer.parseInt(st.nextToken());

      if (player.getAdena() < cost)
      {
        player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
        return;
      }

      if (ItemFunctions.removeItem(player, id, 1L, true) != 1L)
      {
        player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
        return;
      }

      player.reduceAdena(cost, true);

      int chance = Rnd.get(1000000);
      switch (id)
      {
      case 13898:
        if (chance < 350000)
          ItemFunctions.addItem(player, 13902, 1L, true);
        else if (chance < 550000)
          ItemFunctions.addItem(player, 13903, 1L, true);
        else if (chance < 650000)
          ItemFunctions.addItem(player, 13904, 1L, true);
        else if (chance < 730000)
          ItemFunctions.addItem(player, 13905, 1L, true);
        else
          informFail(player, id);
        break;
      case 13899:
        if (chance < 350000)
          ItemFunctions.addItem(player, 13906, 1L, true);
        else if (chance < 550000)
          ItemFunctions.addItem(player, 13907, 1L, true);
        else if (chance < 650000)
          ItemFunctions.addItem(player, 13908, 1L, true);
        else if (chance < 730000)
          ItemFunctions.addItem(player, 13909, 1L, true);
        else
          informFail(player, id);
        break;
      case 13900:
        if (chance < 350000)
          ItemFunctions.addItem(player, 13910, 1L, true);
        else if (chance < 550000)
          ItemFunctions.addItem(player, 13911, 1L, true);
        else if (chance < 650000)
          ItemFunctions.addItem(player, 13912, 1L, true);
        else if (chance < 730000)
          ItemFunctions.addItem(player, 13913, 1L, true);
        else
          informFail(player, id);
        break;
      case 13901:
        if (chance < 350000)
          ItemFunctions.addItem(player, 13914, 1L, true);
        else if (chance < 550000)
          ItemFunctions.addItem(player, 13915, 1L, true);
        else if (chance < 650000)
          ItemFunctions.addItem(player, 13916, 1L, true);
        else if (chance < 730000)
          ItemFunctions.addItem(player, 13917, 1L, true);
        else
          informFail(player, id);
        break;
      case 13918:
        if (chance < 350000)
          ItemFunctions.addItem(player, 13922, 1L, true);
        else if (chance < 550000)
          ItemFunctions.addItem(player, 13923, 1L, true);
        else if (chance < 650000)
          ItemFunctions.addItem(player, 13924, 1L, true);
        else if (chance < 730000)
          ItemFunctions.addItem(player, 13925, 1L, true);
        else
          informFail(player, id);
        break;
      case 13919:
        if (chance < 350000)
          ItemFunctions.addItem(player, 13926, 1L, true);
        else if (chance < 550000)
          ItemFunctions.addItem(player, 13927, 1L, true);
        else if (chance < 650000)
          ItemFunctions.addItem(player, 13928, 1L, true);
        else if (chance < 730000)
          ItemFunctions.addItem(player, 13929, 1L, true);
        else
          informFail(player, id);
        break;
      case 13920:
        if (chance < 350000)
          ItemFunctions.addItem(player, 13930, 1L, true);
        else if (chance < 550000)
          ItemFunctions.addItem(player, 13931, 1L, true);
        else if (chance < 650000)
          ItemFunctions.addItem(player, 13932, 1L, true);
        else if (chance < 730000)
          ItemFunctions.addItem(player, 13933, 1L, true);
        else
          informFail(player, id);
        break;
      case 13921:
        if (chance < 350000)
          ItemFunctions.addItem(player, 13934, 1L, true);
        else if (chance < 550000)
          ItemFunctions.addItem(player, 13935, 1L, true);
        else if (chance < 650000)
          ItemFunctions.addItem(player, 13936, 1L, true);
        else if (chance < 730000)
          ItemFunctions.addItem(player, 13937, 1L, true);
        else
          informFail(player, id);
        break;
      case 14902:
        if (chance < 350000)
          ItemFunctions.addItem(player, 14906, 1L, true);
        else if (chance < 550000)
          ItemFunctions.addItem(player, 14907, 1L, true);
        else if (chance < 650000)
          ItemFunctions.addItem(player, 14908, 1L, true);
        else if (chance < 730000)
          ItemFunctions.addItem(player, 14909, 1L, true);
        else
          informFail(player, id);
        break;
      case 14903:
        if (chance < 350000)
          ItemFunctions.addItem(player, 14910, 1L, true);
        else if (chance < 550000)
          ItemFunctions.addItem(player, 14911, 1L, true);
        else if (chance < 650000)
          ItemFunctions.addItem(player, 14912, 1L, true);
        else if (chance < 730000)
          ItemFunctions.addItem(player, 14913, 1L, true);
        else
          informFail(player, id);
        break;
      case 14904:
        if (chance < 350000)
          ItemFunctions.addItem(player, 14914, 1L, true);
        else if (chance < 550000)
          ItemFunctions.addItem(player, 14915, 1L, true);
        else if (chance < 650000)
          ItemFunctions.addItem(player, 14916, 1L, true);
        else if (chance < 730000)
          ItemFunctions.addItem(player, 14917, 1L, true);
        else
          informFail(player, id);
        break;
      case 14905:
        if (chance < 350000)
          ItemFunctions.addItem(player, 14918, 1L, true);
        else if (chance < 550000)
          ItemFunctions.addItem(player, 14919, 1L, true);
        else if (chance < 650000)
          ItemFunctions.addItem(player, 14920, 1L, true);
        else if (chance < 730000)
          ItemFunctions.addItem(player, 14921, 1L, true);
        else
          informFail(player, id);
        break;
      default:
        return;
      }
    }
    else {
      super.onBypassFeedback(player, command);
    }
  }

  private void informFail(Player player, int itemId) {
    Functions.npcSay(this, NpcString.WHAT_A_PREDICAMENT_MY_ATTEMPTS_WERE_UNSUCCESSUFUL, new String[0]);
  }
}
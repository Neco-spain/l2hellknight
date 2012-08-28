package l2m.gameserver.model.instances;

import l2m.gameserver.Announcements;
import l2m.gameserver.Config;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.instancemanager.CoupleManager;
import l2m.gameserver.model.GameObjectsStorage;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.entity.Couple;
import l2m.gameserver.model.items.PcInventory;
import l2m.gameserver.network.serverpackets.L2GameServerPacket;
import l2m.gameserver.network.serverpackets.MagicSkillUse;
import l2m.gameserver.network.serverpackets.NpcHtmlMessage;
import l2m.gameserver.network.serverpackets.components.CustomMessage;
import l2m.gameserver.templates.npc.NpcTemplate;

public class WeddingManagerInstance extends NpcInstance
{
  public static final long serialVersionUID = 1L;

  public WeddingManagerInstance(int objectId, NpcTemplate template)
  {
    super(objectId, template);
  }

  public void showChatWindow(Player player, int val, Object[] arg)
  {
    String filename = "wedding/start.htm";
    String replace = "";
    NpcHtmlMessage html = new NpcHtmlMessage(player, this);
    html.setFile(filename);
    html.replace("%replace%", replace);
    html.replace("%npcname%", getName());
    player.sendPacket(html);
  }

  public void onBypassFeedback(Player player, String command)
  {
    if (!canBypassCheck(player, this)) {
      return;
    }

    String filename = "wedding/start.htm";
    String replace = "";

    if (player.getPartnerId() == 0)
    {
      filename = "wedding/nopartner.htm";
      sendHtmlMessage(player, filename, replace);
      return;
    }

    Player ptarget = GameObjectsStorage.getPlayer(player.getPartnerId());

    if ((ptarget == null) || (!ptarget.isOnline()))
    {
      filename = "wedding/notfound.htm";
      sendHtmlMessage(player, filename, replace);
      return;
    }
    if (player.isMaried())
    {
      filename = "wedding/already.htm";
      sendHtmlMessage(player, filename, replace);
      return;
    }
    if (command.startsWith("AcceptWedding"))
    {
      player.setMaryAccepted(true);
      Couple couple = CoupleManager.getInstance().getCouple(player.getCoupleId());
      couple.marry();

      player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2WeddingManagerMessage", player, new Object[0]));
      player.setMaried(true);
      player.setMaryRequest(false);
      ptarget.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2WeddingManagerMessage", ptarget, new Object[0]));
      ptarget.setMaried(true);
      ptarget.setMaryRequest(false);

      player.broadcastPacket(new L2GameServerPacket[] { new MagicSkillUse(player, player, 2230, 1, 1, 0L) });
      ptarget.broadcastPacket(new L2GameServerPacket[] { new MagicSkillUse(ptarget, ptarget, 2230, 1, 1, 0L) });

      player.broadcastPacket(new L2GameServerPacket[] { new MagicSkillUse(player, player, 2025, 1, 1, 0L) });
      ptarget.broadcastPacket(new L2GameServerPacket[] { new MagicSkillUse(ptarget, ptarget, 2025, 1, 1, 0L) });

      Announcements.getInstance().announceByCustomMessage("l2p.gameserver.model.instances.L2WeddingManagerMessage.announce", new String[] { player.getName(), ptarget.getName() });

      filename = "wedding/accepted.htm";
      replace = ptarget.getName();
      sendHtmlMessage(ptarget, filename, replace);
      return;
    }
    if (player.isMaryRequest())
    {
      if ((Config.WEDDING_FORMALWEAR) && (!isWearingFormalWear(player)))
      {
        filename = "wedding/noformal.htm";
        sendHtmlMessage(player, filename, replace);
        return;
      }
      filename = "wedding/ask.htm";
      player.setMaryRequest(false);
      ptarget.setMaryRequest(false);
      replace = ptarget.getName();
      sendHtmlMessage(player, filename, replace);
      return;
    }
    if (command.startsWith("AskWedding"))
    {
      if ((Config.WEDDING_FORMALWEAR) && (!isWearingFormalWear(player)))
      {
        filename = "wedding/noformal.htm";
        sendHtmlMessage(player, filename, replace);
        return;
      }
      if (player.getAdena() < Config.WEDDING_PRICE)
      {
        player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
        return;
      }

      player.setMaryAccepted(true);
      ptarget.setMaryRequest(true);
      replace = ptarget.getName();
      filename = "wedding/requested.htm";
      player.reduceAdena(Config.WEDDING_PRICE, true);
      sendHtmlMessage(player, filename, replace);
      return;
    }

    if (command.startsWith("DeclineWedding"))
    {
      player.setMaryRequest(false);
      ptarget.setMaryRequest(false);
      player.setMaryAccepted(false);
      ptarget.setMaryAccepted(false);
      player.sendMessage("You declined");
      ptarget.sendMessage("Your partner declined");
      replace = ptarget.getName();
      filename = "wedding/declined.htm";
      sendHtmlMessage(ptarget, filename, replace);
      return;
    }
    if (player.isMaryAccepted())
    {
      filename = "wedding/waitforpartner.htm";
      sendHtmlMessage(player, filename, replace);
      return;
    }
    sendHtmlMessage(player, filename, replace);
  }

  private static boolean isWearingFormalWear(Player player)
  {
    return (player != null) && (player.getInventory() != null) && (player.getInventory().getPaperdollItemId(10) == 6408);
  }

  private void sendHtmlMessage(Player player, String filename, String replace)
  {
    NpcHtmlMessage html = new NpcHtmlMessage(player, this);
    html.setFile(filename);
    html.replace("%replace%", replace);
    html.replace("%npcname%", getName());
    player.sendPacket(html);
  }
}
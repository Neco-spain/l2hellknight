package l2p.gameserver.clientpackets;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import l2p.gameserver.Config;
import l2p.gameserver.data.xml.holder.MultiSellHolder;
import l2p.gameserver.handler.admincommands.AdminCommandHandler;
import l2p.gameserver.handler.bbs.ICommunityBoardHandler;
import l2p.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2p.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2p.gameserver.instancemanager.BypassManager.DecodedBypass;
import l2p.gameserver.instancemanager.OlympiadHistoryManager;
import l2p.gameserver.model.GameObject;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.Hero;
import l2p.gameserver.model.entity.olympiad.Olympiad;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.model.instances.OlympiadManagerInstance;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.scripts.Scripts;
import l2p.gameserver.serverpackets.NpcHtmlMessage;
import l2p.gameserver.serverpackets.SystemMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestBypassToServer extends L2GameClientPacket
{
  private static final Logger _log = LoggerFactory.getLogger(RequestBypassToServer.class);
  private BypassManager.DecodedBypass bp = null;

  protected void readImpl()
  {
    String bypass = readS();
    if (!bypass.isEmpty())
      bp = ((GameClient)getClient()).getActiveChar().decodeBypass(bypass);
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if ((activeChar == null) || (bp == null))
      return;
    try
    {
      NpcInstance npc = activeChar.getLastNpc();
      GameObject target = activeChar.getTarget();
      if ((npc == null) && (target != null) && (target.isNpc())) {
        npc = (NpcInstance)target;
      }
      if (bp.bypass.startsWith("admin_")) {
        AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, bp.bypass);
      } else if ((bp.bypass.equals("come_here")) && (activeChar.isGM())) {
        comeHere((GameClient)getClient());
      } else if (bp.bypass.startsWith("player_help ")) {
        playerHelp(activeChar, bp.bypass.substring(12));
      } else if (bp.bypass.startsWith("scripts_"))
      {
        String command = bp.bypass.substring(8).trim();
        String[] word = command.split("\\s+");
        String[] args = command.substring(word[0].length()).trim().split("\\s+");
        String[] path = word[0].split(":");
        if (path.length != 2)
        {
          _log.warn("Bad Script bypass!");
          return;
        }

        Map variables = null;
        if (npc != null)
        {
          variables = new HashMap(1);
          variables.put("npc", npc.getRef());
        }

        if (word.length == 1)
          Scripts.getInstance().callScripts(activeChar, path[0], path[1], variables);
        else
          Scripts.getInstance().callScripts(activeChar, path[0], path[1], new Object[] { args }, variables);
      }
      else if (bp.bypass.startsWith("user_"))
      {
        String command = bp.bypass.substring(5).trim();
        String word = command.split("\\s+")[0];
        String args = command.substring(word.length()).trim();
        IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(word);

        if (vch != null)
          vch.useVoicedCommand(word, activeChar, args);
        else
          _log.warn("Unknow voiced command '" + word + "'");
      }
      else if (bp.bypass.startsWith("npc_"))
      {
        int endOfId = bp.bypass.indexOf('_', 5);
        String id;
        String id;
        if (endOfId > 0)
          id = bp.bypass.substring(4, endOfId);
        else
          id = bp.bypass.substring(4);
        GameObject object = activeChar.getVisibleObject(Integer.parseInt(id));
        if ((object != null) && (object.isNpc()) && (endOfId > 0) && (activeChar.isInRange(object.getLoc(), 200L)))
        {
          activeChar.setLastNpc((NpcInstance)object);
          ((NpcInstance)object).onBypassFeedback(activeChar, bp.bypass.substring(endOfId + 1));
        }
      }
      else if (bp.bypass.startsWith("_olympiad?"))
      {
        String[] ar = bp.bypass.replace("_olympiad?", "").split("&");
        String firstVal = ar[0].split("=")[1];
        String secondVal = ar[1].split("=")[1];

        if (firstVal.equalsIgnoreCase("move_op_field"))
        {
          if (!Config.ENABLE_OLYMPIAD_SPECTATING) {
            return;
          }

          if ((((activeChar.getLastNpc() instanceof OlympiadManagerInstance)) && (activeChar.getLastNpc().isInRange(activeChar, 200L))) || (activeChar.getOlympiadObserveGame() != null))
            Olympiad.addSpectator(Integer.parseInt(secondVal) - 1, activeChar);
        }
      }
      else if (bp.bypass.startsWith("_diary"))
      {
        String params = bp.bypass.substring(bp.bypass.indexOf("?") + 1);
        StringTokenizer st = new StringTokenizer(params, "&");
        int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
        int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
        int heroid = Hero.getInstance().getHeroByClass(heroclass);
        if (heroid > 0)
          Hero.getInstance().showHeroDiary(activeChar, heroclass, heroid, heropage);
      }
      else if (bp.bypass.startsWith("_match"))
      {
        String params = bp.bypass.substring(bp.bypass.indexOf("?") + 1);
        StringTokenizer st = new StringTokenizer(params, "&");
        int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
        int heropage = Integer.parseInt(st.nextToken().split("=")[1]);

        OlympiadHistoryManager.getInstance().showHistory(activeChar, heroclass, heropage);
      }
      else if (bp.bypass.startsWith("manor_menu_select?"))
      {
        GameObject object = activeChar.getTarget();
        if ((object != null) && (object.isNpc()))
          ((NpcInstance)object).onBypassFeedback(activeChar, bp.bypass);
      }
      else if (bp.bypass.startsWith("multisell ")) {
        MultiSellHolder.getInstance().SeparateAndSend(Integer.parseInt(bp.bypass.substring(10)), activeChar, 0.0D);
      } else if (bp.bypass.startsWith("Quest "))
      {
        String p = bp.bypass.substring(6).trim();
        int idx = p.indexOf(' ');
        if (idx < 0)
          activeChar.processQuestEvent(p, "", npc);
        else
          activeChar.processQuestEvent(p.substring(0, idx), p.substring(idx).trim(), npc);
      }
      else if (bp.handler != null)
      {
        if (!Config.COMMUNITYBOARD_ENABLED)
          activeChar.sendPacket(new SystemMessage(938));
        else {
          bp.handler.onBypassCommand(activeChar, bp.bypass);
        }
      }
    }
    catch (Exception e)
    {
      String st = "Bad RequestBypassToServer: " + bp.bypass;
      GameObject target = activeChar.getTarget();
      if ((target != null) && (target.isNpc()))
        st = st + " via NPC #" + ((NpcInstance)target).getNpcId();
      _log.error(st, e);
    }
  }

  private static void comeHere(GameClient client)
  {
    GameObject obj = client.getActiveChar().getTarget();
    if ((obj != null) && (obj.isNpc()))
    {
      NpcInstance temp = (NpcInstance)obj;
      Player activeChar = client.getActiveChar();
      temp.setTarget(activeChar);
      temp.moveToLocation(activeChar.getLoc(), 0, true);
    }
  }

  private static void playerHelp(Player activeChar, String path)
  {
    NpcHtmlMessage html = new NpcHtmlMessage(5);
    html.setFile(path);
    activeChar.sendPacket(html);
  }
}
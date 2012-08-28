package scripts.commands.voicedcommandhandlers;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import scripts.commands.IVoicedCommandHandler;

public class ModCommands
  implements IVoicedCommandHandler
{
  private static final String[] VOICED_COMMANDS = { "banchat", "unbanchat", "kick", "recall" };

  public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
  {
    if (activeChar.isModerator())
    {
      String[] cmdParams = command.split(" ");

      String name = cmdParams[1];
      long banLength = Integer.parseInt(cmdParams[2]);

      L2PcInstance targetPlayer = L2World.getInstance().getPlayer(name);
      String Moder = "";
      int obj = activeChar.getObjectId();

      switch (obj)
      {
      case 268948838:
        Moder = "EvilsToy";
        break;
      case 268551556:
        Moder = "MYPKA";
        break;
      case 269177626:
        Moder = "I_am_Legend";
        break;
      case 271218061:
        Moder = "d00m";
        break;
      case 270231826:
        Moder = "Zeteo";
        break;
      case 270579677:
        Moder = "ZIKaaaR";
        break;
      case 271162644:
        Moder = "(-=[Sa(.i.)nt]=-)";
        break;
      case 269833471:
        Moder = "Kpayc";
      }

      if (targetPlayer == null)
      {
        activeChar.sendMessage("\u0418\u0433\u0440\u043E\u043A \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D \u0438\u043B\u0438 \u043E\u0448\u0438\u0431\u043A\u0430 \u043F\u0440\u0438 \u043D\u0430\u0431\u043E\u0440\u0435 \u043D\u0438\u043A\u0430");
        return false;
      }

      if (command.startsWith("banchat"))
      {
        if (targetPlayer.isChatBanned())
        {
          activeChar.sendMessage("\u0427\u0430\u0442 " + targetPlayer.getName() + " \u0443\u0436\u0435 \u0437\u0430\u0431\u043B\u043E\u043A\u0438\u0440\u043E\u0432\u0430\u043D \u043A\u0435\u043C-\u0442\u043E \u0434\u0440\u0443\u0433\u0438\u043C");
          return false;
        }

        if (targetPlayer.isModerator())
        {
          activeChar.sendMessage("\u041D\u0435\u043B\u044C\u0437\u044F");
          return false;
        }

        long banLengthMins = banLength / 60L;

        if (banLengthMins > 1800L) {
          banLengthMins = 1800L;
        }
        activeChar.sendMessage("\u0427\u0430\u0442 " + targetPlayer.getName() + " \u0437\u0430\u0431\u043B\u043E\u043A\u0438\u0440\u043E\u0432\u0430\u043D \u043D\u0430 " + banLength + " \u0441\u0435\u043A\u0443\u043D\u0434. (" + banLengthMins + " \u043C\u0438\u043D\u0443\u0442)");
        targetPlayer.setChatBanned(true, banLength, "");
        targetPlayer.sendMessage("* * * * * * * * * * * * * *");
        targetPlayer.sendMessage("\u041D\u0430\u043A\u0430\u0437\u0430\u043B " + activeChar.getName() + "(" + Moder + ")");
        targetPlayer.sendMessage("\u0414\u043B\u044F \u043E\u0441\u043F\u0430\u0440\u0438\u0432\u0430\u043D\u0438\u044F, \u0440\u0430\u0441\u0442\u0435\u043D\u0438\u0442\u0435 \u0447\u0430\u0442 \u0438 \u0441\u0434\u0435\u043B\u0430\u0439\u0442\u0435 \u0441\u043A\u0440\u0438\u043D,");
        targetPlayer.sendMessage("\u0440\u0430\u0437\u0434\u0435\u043B \u043D\u0430 \u0444\u043E\u0440\u0443\u043C\u0435 - \u0410\u0440\u0431\u0438\u0442\u0440\u0430\u0436.");
        targetPlayer.sendMessage("* * * * * * * * * * * * * *");
      }
      else if (command.startsWith("unbanchat"))
      {
        targetPlayer.sendMessage("* * * * * * * * * * * * * *");
        activeChar.sendMessage("\u0427\u0430\u0442 " + targetPlayer.getName() + "\u0440\u0430\u0437\u0431\u043B\u043E\u043A\u0438\u0440\u043E\u0432\u0430\u043D");
        targetPlayer.sendMessage("\u0410\u043C\u043D\u0438\u0441\u0442\u0438\u0440\u043E\u0432\u0430\u043B " + activeChar.getName() + "(" + Moder + ")");
        targetPlayer.sendMessage("* * * * * * * * * * * * * *");
        targetPlayer.setChatBanned(false, 0L, "");
      }
      else if (command.startsWith("kick"))
      {
        if (activeChar.getPrivateStoreType() != 0)
        {
          activeChar.sendMessage(targetPlayer.getName() + " \u043A\u0438\u043A\u043D\u0443\u0442 \u0438\u0437 \u0438\u0433\u0440\u044B");
          targetPlayer.sendMessage("* * * * * * * * * * * * * *");
          targetPlayer.sendMessage("\u0412\u0430\u0441 \u043A\u0438\u043A\u043D\u0443\u043B " + activeChar.getName() + "(" + Moder + ")");
          targetPlayer.sendMessage("* * * * * * * * * * * * * *");
          targetPlayer.logout();
        }
      }
      else if (command.startsWith("recall"))
      {
        activeChar.sendMessage("\u0412\u044B \u0442\u0435\u043B\u0435\u043F\u043E\u0440\u0442\u0438\u0440\u0443\u0435\u0442\u0435 " + targetPlayer.getName());
        targetPlayer.sendMessage("* * * * * * * * * * * * * *");
        targetPlayer.sendMessage("\u0412\u0430\u0441 \u0442\u0435\u043B\u0435\u043F\u043E\u0440\u0442\u0438\u0440\u0443\u0435\u0442 " + activeChar.getName() + "(" + Moder + ")");
        targetPlayer.sendMessage("* * * * * * * * * * * * * *");
        targetPlayer.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
        targetPlayer.teleToLocation(activeChar.getX(), activeChar.getY(), activeChar.getZ(), false);
      }
      return true;
    }

    return false;
  }

  public String[] getVoicedCommandList()
  {
    return VOICED_COMMANDS;
  }
}
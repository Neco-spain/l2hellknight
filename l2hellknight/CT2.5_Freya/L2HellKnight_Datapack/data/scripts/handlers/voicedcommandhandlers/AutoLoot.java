package handlers.voicedcommandhandlers;

import l2.hellknight.Config;
import l2.hellknight.gameserver.handler.IVoicedCommandHandler;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.network.serverpackets.ExShowScreenMessage;

public class AutoLoot implements IVoicedCommandHandler
{
   private static final String[] _voicedCommands = {"autoloot", "autolootherbs"};

   public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
   {
      if (!Config.L2JMOD_AUTO_LOOT_INDIVIDUAL)
         return false;

      if (command.equalsIgnoreCase("autoloot"))
      {
         if (activeChar._useAutoLoot)
         {
            activeChar._useAutoLoot=false;
            activeChar.sendPacket(new ExShowScreenMessage(1,0,2,0,1,0,0,false,3000,1,"Autoloot items DISABLED!"));
         }
         else
         {
            activeChar._useAutoLoot=true;
            activeChar.sendPacket(new ExShowScreenMessage(1,0,2,0,1,0,0,false,3000,1,"Autoloot items ENABLED!"));
         }
      }
      else if (command.equalsIgnoreCase("autolootherbs"))
      {
         if (activeChar._useAutoLootHerbs)
         {
            activeChar._useAutoLootHerbs=false;
            activeChar.sendPacket(new ExShowScreenMessage(1,0,2,0,1,0,0,false,3000,1,"Autoloot herbs DISABLED!"));
         }
         else
         {
            activeChar._useAutoLootHerbs=true;
            activeChar.sendPacket(new ExShowScreenMessage(1,0,2,0,1,0,0,false,3000,1,"Autoloot herbs ENABLED!"));
         }
      }
      return true;
   }

   public String[] getVoicedCommandList()
   {
      return _voicedCommands;
   }
}
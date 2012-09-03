package instances.JiniasHideout;

import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.instancemanager.InstanceManager;
import l2.hellknight.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.entity.Instance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.clientpackets.Say2;
import l2.hellknight.gameserver.network.serverpackets.NpcSay;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;

public class JiniasHideout extends Quest
{
        private class JiniasWorld extends InstanceWorld
        {
                public long[] storeTime = {0,0};
                public int questId; 
                public JiniasWorld()
                {
                        
                }
        }
        
        private static final String qn = "JiniasHideout";
        
        private static final int RAFFORTY = 32020;
        private static final int JINIA = 32760;
        private static final int SIRRA = 32762;
        
        private static final int[] ENTRY_POINT = { -23530, -8963, -5413 };
        
        private class teleCoord {int instanceId; int x; int y; int z;}
        
        private void teleportplayer(L2PcInstance player, teleCoord teleto)
        {
                player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
                player.setInstanceId(teleto.instanceId);
                player.teleToLocation(teleto.x, teleto.y, teleto.z);
                return;
        }
        
        private boolean checkConditions(L2PcInstance player)
        {
                if (player.getLevel() < 82 || player.getLevel() > 85)
                {
                        SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
                        sm.addPcName(player);
                        player.sendPacket(sm);
                        return false;
                }
                
                return true; 
        }

        protected void exitInstance(L2PcInstance player, teleCoord tele)
        {
                player.setInstanceId(0);
                player.teleToLocation(tele.x, tele.y, tele.z);
        }
         
        protected int enterInstance(L2PcInstance player, String template, teleCoord teleto, int questId)
        {
                int instanceId = 0;
                InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
                //existing instance
                if (world != null)
                {
                        //this instance
                        if (!(world instanceof JiniasWorld))
                        {
                                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
                                return 0;
                        }
                        teleto.instanceId = world.instanceId;
                        teleportplayer(player,teleto);
                        return instanceId;
                }
                else
                {
                        //New instance
                        if (!checkConditions(player))
                                return 0;

                        instanceId = InstanceManager.getInstance().createDynamicInstance(template);
                        final Instance inst = InstanceManager.getInstance().getInstance(instanceId);
                        inst.setSpawnLoc(new int[] { player.getX(), player.getY(), player.getZ() });
                        world = new JiniasWorld();
                        world.instanceId = instanceId;
                        ((JiniasWorld)world).questId = questId;

                        //Template id depends of quest
                        switch(questId)
                        {
                                case 10284:
                                        world.templateId = 140;
                                        break;
                                case 10285:
                                        world.templateId = 141;
                                        break;
                                case 10286:
                                        world.templateId = 145;
                                        break;
                                case 10287:
                                        world.templateId = 146;
                                        break;
                        }

                        world.status = 0;
                        ((JiniasWorld)world).storeTime[0] = System.currentTimeMillis();
                        InstanceManager.getInstance().addWorld(world);
                        _log.info("JiniasWorld started " + template + " Instance: " + instanceId + " created by player: " + player.getName());
                        teleto.instanceId = instanceId;
                        teleportplayer(player, teleto);
                        world.allowed.add(player.getObjectId());
                        return instanceId;
                }
        }

        @Override
        public String onAdvEvent (String event, L2Npc npc, L2PcInstance player)
        {
                String htmltext = null;
                System.out.println("Here");

                if (event.startsWith("enterInstance_") && npc.getNpcId() == RAFFORTY)
                {
                        int questId = -1;
                        String tmpl = null;
                        QuestState hostQuest = null;

                        try
                        {
                                System.out.println(event.substring(14));
                                questId = Integer.parseInt(event.substring(14));
                                
                                teleCoord tele = new teleCoord();
                                tele.x = ENTRY_POINT[0];      
                                tele.y = ENTRY_POINT[1];
                                tele.z = ENTRY_POINT[2];
                                
                                switch(questId)
                                {
                                        case 10284:
                                                hostQuest = player.getQuestState("Q10284_AcquisitionOfDivineSword");
                                                tmpl = "JiniasHideout1.xml";
                                                htmltext = "10284_failed.htm";
                                                break;
                                        case 10285:
                                                hostQuest = player.getQuestState("Q10285_MeetingSirra");
                                                tmpl = "JiniasHideout2.xml";
                                                htmltext = "10285_failed.htm";
                                                break;
                                        case 10286:
                                                hostQuest = player.getQuestState("Q10286_ReunionWithSirra");
                                                tmpl = "JiniasHideout2.xml";
                                                htmltext = "10286_failed.htm";
                                                break;
                                        case 10287:
                                                hostQuest = player.getQuestState("Q10287_StoryOfThoseLeft");
                                                tmpl = "JiniasHideout2.xml";
                                                htmltext = "10287_failed.htm";
                                                break;
                                }
                                                
                                if (hostQuest != null && hostQuest.getState() == State.STARTED && hostQuest.getInt("progress") == 1)
                                {
                                        hostQuest.playSound("ItemSound.quest_middle");
                                        hostQuest.set("cond", "2");
                                }

                                
                                if (tmpl != null)
                                {
                                        if (enterInstance(player, tmpl, tele, questId) > 0)
                                                htmltext = null;
                                }
                        }
                        catch (Exception e)
                        {
                        
                        }
                }
                
                else if (event.equalsIgnoreCase("leaveInstance") && npc.getNpcId() == JINIA)
                {
                        QuestState hostQuest = null;
                        InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
                        final Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
                        world.allowed.remove(world.allowed.indexOf(player.getObjectId()));
                        teleCoord tele = new teleCoord();
                        tele.instanceId = 0;
                        tele.x = inst.getSpawnLoc()[0];    
                        tele.y = inst.getSpawnLoc()[1];
                        tele.z = inst.getSpawnLoc()[2];
                        exitInstance(player, tele);
                        
                        switch (((JiniasWorld)world).questId)
                        {
                                case 10285:
                                        hostQuest = player.getQuestState("Q10285_MeetingSirra");
                                        break;
                                case 10286:
                                        hostQuest = player.getQuestState("Q10286_ReunionWithSirra");
                                        break;
                                case 10287:
                                        hostQuest = player.getQuestState("Q10287_StoryOfThoseLeft");
                                        break;
                        }
                        
                        if (hostQuest != null && hostQuest.getState() == State.STARTED && hostQuest.getInt("progress") == 2)
                        {
                                switch (((JiniasWorld)world).questId)
                                {
                                case 10285:
                                        htmltext = "10285_goodbye.htm";
                                        break;
                                case 10286:
                                        htmltext = "10286_goodbye.htm";
                                        hostQuest.playSound("ItemSound.quest_middle");
                                        hostQuest.set("cond", "5");
                                        break;
                                case 10287:
                                        htmltext = "10287_goodbye.htm";
                                        hostQuest.playSound("ItemSound.quest_middle");
                                        hostQuest.set("cond", "5");
                                }
                        }
                }
        
                return htmltext;
        }

  @Override
        public final String onSpawn(L2Npc npc)
        {
                if (npc.getNpcId() == SIRRA)
                {
                        InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
                        if (tmpworld != null && tmpworld instanceof JiniasWorld)
                        {               
                                /*
                                //My client crashes on these fstring IDs, so I use String
                                int fstringId = -1;
                                switch tmpworld.templateId
                                {
                                        case 141:
                                                fstringId = 528551;
                                                break;
                                        case 145:
                                                fstringId = 528651;
                                }
                                if fstringId > 0
                                        npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getNpcId(), 528551));
                                */
                        
                                String msg = null;
                                        switch(tmpworld.templateId)
                                        {
                                                case 141:
                                                        msg = "There's nothing you can't say. I can't listen to you anymore!";
                                                        break;
                                                case 145:
                                                        msg = "You advanced bravely but got such a tiny result. Hohoho.";
                                        }
                                        
                                        if (msg != null)
                                                npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getNpcId(), msg));
                        }
                }

                return null;
        }

  public JiniasHideout(int questId, String name, String descr)
        {
                super(questId, name, descr);

                addStartNpc(RAFFORTY);
                addTalkId(RAFFORTY);
                addTalkId(JINIA);
                addSpawnId(SIRRA);
        }
        
        public static void main(String[] args)
        {
                new JiniasHideout(-1,qn,"instances");
        }
}
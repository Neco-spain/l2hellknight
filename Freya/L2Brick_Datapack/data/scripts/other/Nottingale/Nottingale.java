package other.Nottingale;

import l2.brick.Config;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;
import l2.brick.gameserver.model.quest.State;
import l2.brick.gameserver.network.serverpackets.RadarControl;

public class Nottingale extends Quest
{
	private static final String qn = "Nottingale";
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
	    QuestState st = player.getQuestState(qn);
	    if (st == null)
	    	return "";
	    QuestState qs = player.getQuestState("10273_GoodDayToFly");
	    if (qs == null || (qs.getState() != State.COMPLETED))
	    {
	       player.sendPacket(new RadarControl(2,2,0,0,0));
	       player.sendPacket(new RadarControl(0,2,-184545,243120,1581));
	       htmltext = "32627.htm";
	    }
	    else if (event.equalsIgnoreCase("32627-3.htm"))
	    {
	       player.sendPacket(new RadarControl(2,2,0,0,0));
	       player.sendPacket(new RadarControl(0,2,-192361,254528,3598));
	    }
	    else if (event.equalsIgnoreCase("32627-4.htm"))
	    {
	       player.sendPacket(new RadarControl(2,2,0,0,0));
	       player.sendPacket(new RadarControl(0,2,-174600,219711,4424));
	    }
	    else if (event.equalsIgnoreCase("32627-5.htm"))
	    {
	       player.sendPacket(new RadarControl(2,2,0,0,0));
	       player.sendPacket(new RadarControl(0,2,-181989,208968,4424));
	    }
	    else if (event.equalsIgnoreCase("32627-6.htm"))
	    {
	       player.sendPacket(new RadarControl(2,2,0,0,0));
	       player.sendPacket(new RadarControl(0,2,-252898,235845,5343));
	    }
	    else if (event.equalsIgnoreCase("32627-8.htm"))
	    {
	       player.sendPacket(new RadarControl(2,2,0,0,0));
	       player.sendPacket(new RadarControl(0,2,-212819,209813,4288));
	    }
	    else if (event.equalsIgnoreCase("32627-9.htm"))
	    {
	       player.sendPacket(new RadarControl(2,2,0,0,0));
	       player.sendPacket(new RadarControl(0,2,-246899,251918,4352));
	    }
	    return htmltext;
	}
	
	@Override
	public String onFirstTalk (L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
			st = newQuestState(player);
		player.setLastQuestNpcObject(npc.getObjectId());
		npc.showChatWindow(player);
		return "";
	}

	public Nottingale(int id, String name, String desc)
	{
		super(id,name,desc);
		addStartNpc(32627);
		addFirstTalkId(32627);
		addTalkId(32627);
	}
	
	public static void main(String[] args)
	{
		new Nottingale(-1,qn,"other");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Other: Nottingale");
	}
}
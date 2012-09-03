package l2rt.gameserver.model.instances;

import java.util.StringTokenizer;

import javolution.text.TextBuilder;

import l2rt.extensions.multilang.CustomMessage;
import l2rt.gameserver.tables.SkillTable;
import l2rt.gameserver.tables.SkillTreeTable;
import l2rt.gameserver.instancemanager.FishingChampionShipManager;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.L2SkillLearn;
import l2rt.gameserver.network.serverpackets.AcquireSkillList;
import l2rt.gameserver.network.serverpackets.NpcHtmlMessage;
import l2rt.gameserver.templates.L2NpcTemplate;

public class L2FishermanInstance extends L2MerchantInstance
{
	public L2FishermanInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";

		if(val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;
		return "data/html/fisherman/" + pom + ".htm";
	}

	public void onBypassFeedback(L2Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.startsWith("FishSkillList"))
		{
			player.setSkillLearningClassId(player.getClassId());
			showFishingSkillList(player);
		}
		else if(command.startsWith("FishingChampionship"))
			showChampScreen(player);
		else if(command.startsWith("fishingReward"))
			FishingChampionShipManager.getInstance().getReward(player);

		StringTokenizer st = new StringTokenizer(command, " ");
		String cmd = st.nextToken();

		if(cmd.equalsIgnoreCase("Buy") || cmd.equalsIgnoreCase("Sell"))
		{
			int val = 0;
			if(st.countTokens() > 0)
				val = Integer.parseInt(st.nextToken());
			showShopWindow(player, val, true);
		}
		else
			super.onBypassFeedback(player, command);
	}

	public void showFishingSkillList(L2Player player)
	{
		if(player.getTransformation() != 0)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			TextBuilder sb = new TextBuilder();
			sb.append("<html><head><body>");
			sb.append(new CustomMessage("l2r.gameserver.model.instances.L2NpcInstance.CantTeachBecauseTransformation", player));
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);

			return;
		}

		AcquireSkillList asl = new AcquireSkillList(AcquireSkillList.FISHING);
		int counts = 0;

		L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableFishingSkills(player);
		for(L2SkillLearn s : skills)
		{
			L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
			if(sk == null)
				continue;
			int cost = SkillTreeTable.getInstance().getSkillCost(player, sk);
			counts++;
			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), cost, 0);
		}

		if(counts == 0)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			TextBuilder sb = new TextBuilder();
			sb.append("<html><head><body>");
			sb.append("You've learned all skills.");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);
		}
		else
			player.sendPacket(asl);

		player.sendActionFailed();
	}

	public void showChampScreen(L2Player player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());

		String str = "<html><head><title>Королевский турнир по ловле рыбы</title></head>";
		str = str + "Член Гильдии Рыболовов:<br><br>";
		str = str + "Здравствуйте! У меня есть список победителей турнира по рыбной ловле прошлой недели!<br>";
		str = str + "Ваше имя есть в списке? Если да, то я вручу Вам приз!<br>";
		str = str + "Помните, что Вы можете забрать его только<font color=\"LEVEL\"> в течение этой недели</font>.<br>";
		str = str + "Не расстраивайтесь, если не удалось выиграть! Повезет в следующий раз!<br>";
		str = str + "Это сообщение будет обновлено через " + FishingChampionShipManager.getInstance().getTimeRemaining() + " мин!<br>";
		str = str + "<center><a action=\"bypass -h npc_%objectId%_fishingReward\">Получить приз</a><br></center>";
		str = str + "<table width=280 border=0 bgcolor=\"000000\"><tr><td width=70 align=center>Место</td><td width=110 align=center>Рыбак</td><td width=80 align=center>Длина</td></tr></table><table width=280>";
		for(int x = 1; x <= 5; x++)
		{
			str = str + "<tr><td width=70 align=center>" + x + " Место:</td>";
			str = str + "<td width=110 align=center>" + FishingChampionShipManager.getInstance().getWinnerName(x) + "</td>";
			str = str + "<td width=80 align=center>" + FishingChampionShipManager.getInstance().getFishLength(x) + "</td></tr>";
		}
		str = str + "<td width=80 align=center>0</td></tr></table><br>";
		str = str + "Список призов<br><table width=280 border=0 bgcolor=\"000000\"><tr><td width=70 align=center>Место</td><td width=110 align=center>Приз</td><td width=80 align=center>Количество</td></tr></table><table width=280>";
		str = str + "<tr><td width=70 align=center>1 Место:</td><td width=110 align=center>аден</td><td width=80 align=center>800000</td></tr><tr><td width=70 align=center>2 Место:</td><td width=110 align=center>аден</td><td width=80 align=center>500000</td></tr><tr><td width=70 align=center>3 Место:</td><td width=110 align=center>аден</td><td width=80 align=center>300000</td></tr>";
		str = str + "<tr><td width=70 align=center>4 Место:</td><td width=110 align=center>аден</td><td width=80 align=center>200000</td></tr><tr><td width=70 align=center>5 Место:</td><td width=110 align=center>аден</td><td width=80 align=center>100000</td></tr></table></body></html>";
		html.setHtml(str);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
}
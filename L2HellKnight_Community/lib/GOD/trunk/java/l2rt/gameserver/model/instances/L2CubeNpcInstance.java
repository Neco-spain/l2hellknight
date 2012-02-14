package l2rt.gameserver.model.instances;

import java.io.File;

import l2rt.gameserver.instancemanager.KrateisCubeManager;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.serverpackets.NpcHtmlMessage;
import l2rt.gameserver.templates.L2NpcTemplate;

public class L2CubeNpcInstance extends L2NpcInstance
{
	public L2CubeNpcInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(L2Player player, String command)
	{
		if(command.startsWith("Kratei_UnRegister"))
		{
			KrateisCubeManager.getInstance().unRegisterOnEvent(player);
			showKrateiChat(player, 4);
		}
		else if(command.startsWith("Kratei_TryRegister"))
		{
			if(player.getLevel() < 70)
				showKrateiChat(player, 0);
			else
				showKrateiChat(player, 1);
		}
		else if(command.startsWith("Kratei_SelLevel"))
		{
			if(player.getLevel() < 70)
				showKrateiChat(player, 0);
			else
				showKrateiChat(player, 2);
		}
		else if(command.startsWith("Kratei_Return"))
		{
			if( !KrateisCubeManager.getInstance().returnToCube(player))
				showKrateiChat(player, 5);
		}
		else if(command.startsWith("Kratei_Exit"))
			KrateisCubeManager.getInstance().ejectPlayer(player);
		else if(command.startsWith("Kratei_Register"))
		{
			int val = 0;

			try
			{
				val = Integer.parseInt(command.substring(16));
			}
			catch(IndexOutOfBoundsException ioobe)
			{}
			catch(NumberFormatException nfe)
			{}

			if(player.getLevel() < 70)
				showKrateiChat(player, 0);
			else
			{
				if(val == 70 || val == 76 || val == 80)
				{
					if( !KrateisCubeManager.getInstance().isRegister(player))
					{
						if(KrateisCubeManager.getInstance().registerOnEvent(player, val))
							showKrateiChat(player, 3);
						else
							showKrateiChat(player, 0);
					}
				}
			}
		}
		super.onBypassFeedback(player, command);
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		if(val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;
		String temp = "data/html/KrateiCube/" + pom + ".htm";

		File mainText = new File(temp);
		if(mainText.exists())
			return temp;

		return "data/html/npcdefault.htm";
	}

	private void showKrateiChat(L2Player player, int par)
	{
		String filename = "data/html/KrateiCube/no-lvl.htm";
		if(par == 1 || par == 2)
		{
			if(KrateisCubeManager.getInstance()._isRegPeriod)
			{
				if(KrateisCubeManager.getInstance().isRegister(player))
					filename = "data/html/KrateiCube/reg-alredy.htm";
				else
				{
					if(par == 1)
						filename = "data/html/KrateiCube/want-reg.htm";
					else
						filename = "data/html/KrateiCube/level-select.htm";
				}
			}
			else
				filename = "data/html/KrateiCube/no-reg.htm";
		}
		else if(par == 3)
			filename = "data/html/KrateiCube/reg-success.htm";
		else if(par == 4)
			filename = "data/html/KrateiCube/un-reg.htm";
		else if(par == 5)
			filename = "data/html/KrateiCube/no-enter.htm";

		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
}
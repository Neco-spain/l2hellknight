/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2.hellknight.gameserver.model.actor.templates;

import l2.hellknight.gameserver.model.StatsSet;

/**
 * @author JIV
 *
 */
public class L2DoorTemplate extends L2CharTemplate
{
	public final int doorId;
	public final int nodeX[];
	public final int nodeY[];
	public final int nodeZ;
	public final int height;
	public final int posX;
	public final int posY;
	public final int posZ;
	public final int emmiter;
	public final int childDoorId;
	public final String name;
	public final String groupName;
	public final boolean showHp;
	public final boolean isWall;
	// -1 close,  0 nothing, 1 open
	public final byte masterDoorClose;
	public final byte masterDoorOpen;
	
	public L2DoorTemplate(StatsSet set)
	{
		super(set);
		
		//stats
		doorId = set.getInteger("id");
		name = set.getString("name");

		//position
		String[] pos = set.getString("pos").split(";");
		posX = Integer.parseInt(pos[0]);
		posY = Integer.parseInt(pos[1]);
		posZ = Integer.parseInt(pos[2]);
		height = set.getInteger("height");
		
		// TODO: move to geodata system by creating door zones and load there
		nodeZ = set.getInteger("nodeZ");
		nodeX = new int[4]; // 4 * x
		nodeY = new int[4]; // 4 * y
		for (int i = 0; i < 4; i++)
		{
			String split[] = set.getString("node"+(i+1)).split(",");
			nodeX[i] = Integer.parseInt(split[0]);
			nodeY[i] = Integer.parseInt(split[1]);
		}
		
		//optional
		emmiter = set.getInteger("emitter_id", 0);
		showHp = set.getBool("hp_showable", true);
		isWall = set.getBool("is_wall", false);
		groupName = set.getString("group", null);
		
		childDoorId = set.getInteger("child_id_event", -1);
		// true if door is opening
		String masterevent = set.getString("master_close_event", "act_nothing");
		if (masterevent.equals("act_open"))
			masterDoorClose = 1;
		else if (masterevent.equals("act_close"))
			masterDoorClose = -1;
		else
			masterDoorClose = 0;
		//#2
		masterevent = set.getString("master_open_event", "act_nothing");
		if (masterevent.equals("act_open"))
			masterDoorOpen = 1;
		else if (masterevent.equals("act_close"))
			masterDoorOpen = -1;
		else
			masterDoorOpen = 0;
		
		
	}
	
}

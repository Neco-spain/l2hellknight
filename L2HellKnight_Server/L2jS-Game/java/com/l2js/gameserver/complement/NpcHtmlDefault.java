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
package com.l2js.gameserver.complement;

import com.l2js.Config;

/**
 * @author L0ngh0rn
 */
public class NpcHtmlDefault
{
	public String title(String title, String subTitle)
	{
		return "<html><title>" + title + "</title><body><center><br>" + "<b><font color=ffcc00>" + subTitle
				+ "</font></b>" + "<br><img src=\"L2UI_CH3.herotower_deco\" width=\"256\" height=\"32\"><br></center>";
	}

	public String footer(String name, String version)
	{
		return "<br><center><img src=\"L2UI_CH3.herotower_deco\" width=\"256\" height=\"32\"><br>"
				+ "<br><font color=\"303030\">" + Config.PROJECT_NAME + " - " + name + " (" + version
				+ ")</font></center></body></html>";
	}

	public String footer(String footer)
	{
		return "<br><center><img src=\"L2UI_CH3.herotower_deco\" width=\"256\" height=\"32\"><br>"
				+ "<br><font color=\"303030\">" + footer + "</font></center></body></html>";
	}

	public String footer()
	{
		return "<br><center><img src=\"L2UI_CH3.herotower_deco\" width=\"256\" height=\"32\"><br>"
				+ "<br><font color=\"303030\">" + Config.PROJECT_NAME + "</font></center></body></html>";
	}

	public String button(String questName, String value, String event, int w, int h, int type, boolean revert)
	{
		String back = "", fore = "";
		switch (type)
		{
			case 0: // Normal
				back = "L2UI_ct1.button_df";
				fore = "L2UI_ct1.button_df";
				break;
			case 1: // Yellow
				back = "L2UI_CT1.Button_DF.Gauge_DF_Attribute_earth";
				fore = "L2UI_CT1.Button_DF.Gauge_DF_Attribute_earth_bg";
				break;
			case 2: // Blue
				back = "L2UI_CT1.Button_DF.Gauge_DF_Attribute_Water";
				fore = "L2UI_CT1.Button_DF.Gauge_DF_Attribute_Water_bg";
				break;
			case 3: // Green
				back = "L2UI_CT1.Button_DF.Gauge_DF_Attribute_wind";
				fore = "L2UI_CT1.Button_DF.Gauge_DF_Attribute_wind_bg";
				break;
			case 4: // Red
				back = "L2UI_CT1.Button_DF.Gauge_DF_Attribute_fire";
				fore = "L2UI_CT1.Button_DF.Gauge_DF_Attribute_fire_bg";
				break;
			case 5: // Purple
				back = "L2UI_CT1.Button_DF.Gauge_DF_Attribute_dark";
				fore = "L2UI_CT1.Button_DF.Gauge_DF_Attribute_dark_bg";
				break;
			case 6: // Gray
				back = "L2UI_CT1.Button_DF.Gauge_DF_Attribute_divine";
				fore = "L2UI_CT1.Button_DF.Gauge_DF_Attribute_divine_bg";
				break;
			case 7:
				back = "L2UI_CT1.Windows_DF_Drawer_Bg_Darker";
				fore = "L2UI_CT1.Windows_DF_Drawer_Bg_Darker";
				break;
			default:
				back = "L2UI_ct1.button_df";
				fore = "L2UI_ct1.button_df";
				break;
		}
		return "<button value=\"" + value + "\" action=\"bypass -h Quest " + questName + " " + event + "\" "
				+ "width=\"" + Integer.toString(w) + "\" height=\"" + Integer.toString(h) + "\" " + "back=\""
				+ (revert ? fore : back) + "\" fore=\"" + (revert ? back : fore) + "\">";
	}

	public String link(String questName, String value, String event, String color)
	{
		return "<a action=\"bypass -h Quest " + questName + " " + event + "\">" + "<font color=\"" + color + "\">"
				+ value + "</font></a>";
	}

	public String topic(String title)
	{
		return "<table width=\"260\" align=\"center\"><tr><td width=\"260\" align=\"center\"> " + title
				+ " </td></tr></table><br>";
	}
}

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
package l2.hellknight.gameserver.util;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javolution.text.TextBuilder;
import javolution.util.FastList;

import l2.hellknight.Config;
import l2.hellknight.gameserver.ThreadPoolManager;
import l2.hellknight.gameserver.model.L2Object;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.network.serverpackets.NpcHtmlMessage;
import l2.hellknight.gameserver.network.serverpackets.ShowBoard;
import l2.hellknight.util.file.filter.ExtFilter;

/**
 * General Utility functions related to game server.
 */
public final class Util
{
	public static void handleIllegalPlayerAction(L2PcInstance actor, String message, int punishment)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new IllegalPlayerAction(actor, message, punishment), 5000);
	}
	
	public static String getRelativePath(File base, File file)
	{
		return file.toURI().getPath().substring(base.toURI().getPath().length());
	}
	
	/**
	 * @param obj1
	 * @param obj2
	 * @return degree value of object 2 to the horizontal line with object 1 being the origin.
	 */
	public static double calculateAngleFrom(L2Object obj1, L2Object obj2)
	{
		return calculateAngleFrom(obj1.getX(), obj1.getY(), obj2.getX(), obj2.getY());
	}
	
	/**
	 * @param obj1X
	 * @param obj1Y
	 * @param obj2X
	 * @param obj2Y
	 * @return degree value of object 2 to the horizontal line with object 1 being the origin
	 */
	public static final double calculateAngleFrom(int obj1X, int obj1Y, int obj2X, int obj2Y)
	{
		double angleTarget = Math.toDegrees(Math.atan2(obj2Y - obj1Y, obj2X - obj1X));
		if (angleTarget < 0)
		{
			angleTarget = 360 + angleTarget;
		}
		return angleTarget;
	}
	
	public static final double convertHeadingToDegree(int clientHeading)
	{
		double degree = clientHeading / 182.044444444;
		return degree;
	}
	
	public static final int convertDegreeToClientHeading(double degree)
	{
		if (degree < 0)
		{
			degree = 360 + degree;
		}
		return (int) (degree * 182.044444444);
	}
	
	public static final int calculateHeadingFrom(L2Object obj1, L2Object obj2)
	{
		return calculateHeadingFrom(obj1.getX(), obj1.getY(), obj2.getX(), obj2.getY());
	}
	
	public static final int calculateHeadingFrom(int obj1X, int obj1Y, int obj2X, int obj2Y)
	{
		double angleTarget = Math.toDegrees(Math.atan2(obj2Y - obj1Y, obj2X - obj1X));
		if (angleTarget < 0)
		{
			angleTarget = 360 + angleTarget;
		}
		return (int) (angleTarget * 182.044444444);
	}
	
	public static final int calculateHeadingFrom(double dx, double dy)
	{
		double angleTarget = Math.toDegrees(Math.atan2(dy, dx));
		if (angleTarget < 0)
		{
			angleTarget = 360 + angleTarget;
		}
		return (int) (angleTarget * 182.044444444);
	}
	
	/**
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return the distance between the two coordinates in 2D plane
	 */
	public static double calculateDistance(int x1, int y1, int x2, int y2)
	{
		return calculateDistance(x1, y1, 0, x2, y2, 0, false);
	}
	
	/**
	 * @param x1
	 * @param y1
	 * @param z1
	 * @param x2
	 * @param y2
	 * @param z2
	 * @param includeZAxis - if true, includes also the Z axis in the calculation
	 * @return the distance between the two coordinates
	 */
	public static double calculateDistance(int x1, int y1, int z1, int x2, int y2, int z2, boolean includeZAxis)
	{
		double dx = (double) x1 - x2;
		double dy = (double) y1 - y2;
		
		if (includeZAxis)
		{
			double dz = z1 - z2;
			return Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
		}
		return Math.sqrt((dx * dx) + (dy * dy));
	}
	
	/**
	 * @param obj1
	 * @param obj2
	 * @param includeZAxis - if true, includes also the Z axis in the calculation
	 * @return the distance between the two objects
	 */
	public static double calculateDistance(L2Object obj1, L2Object obj2, boolean includeZAxis)
	{
		if ((obj1 == null) || (obj2 == null))
		{
			return 1000000;
		}
		
		return calculateDistance(obj1.getPosition().getX(), obj1.getPosition().getY(), obj1.getPosition().getZ(), obj2.getPosition().getX(), obj2.getPosition().getY(), obj2.getPosition().getZ(), includeZAxis);
	}
	
	/**
	 * @param str - the string whose first letter to capitalize
	 * @return a string with the first letter of the {@code str} capitalized
	 */
	public static String capitalizeFirst(String str)
	{
		if ((str == null) || str.isEmpty())
		{
			return str;
		}
		final char[] arr = str.toCharArray();
		final char c = arr[0];
		
		if (Character.isLetter(c))
		{
			arr[0] = Character.toUpperCase(c);
		}
		return new String(arr);
	}
	
	/**
	 * (Based on ucwords() function of PHP)<br>
	 * DrHouse: still functional but must be rewritten to avoid += to concat strings
	 * @param str - the string to capitalize
	 * @return a string with the first letter of every word in {@code str} capitalized
	 */
	@Deprecated
	public static String capitalizeWords(String str)
	{
		if ((str == null) || str.isEmpty())
		{
			return str;
		}
		
		char[] charArray = str.toCharArray();
		StringBuilder result = new StringBuilder();
		
		// Capitalize the first letter in the given string!
		charArray[0] = Character.toUpperCase(charArray[0]);
		
		for (int i = 0; i < charArray.length; i++)
		{
			if (Character.isWhitespace(charArray[i]))
			{
				charArray[i + 1] = Character.toUpperCase(charArray[i + 1]);
			}
			
			result.append(charArray[i]);
		}
		
		return result.toString();
	}
	
	/**
	 * @param range
	 * @param obj1
	 * @param obj2
	 * @param includeZAxis
	 * @return {@code true} if the two objects are within specified range between each other, {@code false} otherwise
	 */
	public static boolean checkIfInRange(int range, L2Object obj1, L2Object obj2, boolean includeZAxis)
	{
		if ((obj1 == null) || (obj2 == null))
		{
			return false;
		}
		if (obj1.getInstanceId() != obj2.getInstanceId())
		{
			return false;
		}
		if (range == -1)
		{
			return true; // not limited
		}
		
		int rad = 0;
		if (obj1 instanceof L2Character)
		{
			rad += ((L2Character) obj1).getTemplate().getCollisionRadius();
		}
		if (obj2 instanceof L2Character)
		{
			rad += ((L2Character) obj2).getTemplate().getCollisionRadius();
		}
		
		double dx = obj1.getX() - obj2.getX();
		double dy = obj1.getY() - obj2.getY();
		
		if (includeZAxis)
		{
			double dz = obj1.getZ() - obj2.getZ();
			double d = (dx * dx) + (dy * dy) + (dz * dz);
			
			return d <= ((range * range) + (2 * range * rad) + (rad * rad));
		}
		double d = (dx * dx) + (dy * dy);
		return d <= ((range * range) + (2 * range * rad) + (rad * rad));
	}
	
	/**
	 * Checks if object is within short (sqrt(int.max_value)) radius, not using collisionRadius. Faster calculation than checkIfInRange if distance is short and collisionRadius isn't needed. Not for long distance checks (potential teleports, far away castles etc).
	 * @param radius
	 * @param obj1
	 * @param obj2
	 * @param includeZAxis if true, check also Z axis (3-dimensional check), otherwise only 2D
	 * @return {@code true} if objects are within specified range between each other, {@code false} otherwise
	 */
	public static boolean checkIfInShortRadius(int radius, L2Object obj1, L2Object obj2, boolean includeZAxis)
	{
		if ((obj1 == null) || (obj2 == null))
		{
			return false;
		}
		if (radius == -1)
		{
			return true; // not limited
		}
		
		int dx = obj1.getX() - obj2.getX();
		int dy = obj1.getY() - obj2.getY();
		
		if (includeZAxis)
		{
			int dz = obj1.getZ() - obj2.getZ();
			return ((dx * dx) + (dy * dy) + (dz * dz)) <= (radius * radius);
		}
		return ((dx * dx) + (dy * dy)) <= (radius * radius);
	}
	
	/**
	 * @param str - the String to count
	 * @return the number of "words" in a given string.
	 */
	public static int countWords(String str)
	{
		return str.trim().split("\\s+").length;
	}
	
	/**
	 * (Based on implode() in PHP)
	 * @param strArray - an array of strings to concatenate
	 * @param strDelim - the delimiter to put between the strings
	 * @return a delimited string for a given array of string elements.
	 */
	public static String implodeString(Iterable<String> strArray, String strDelim)
	{
		final TextBuilder sbString = TextBuilder.newInstance();
		
		for (String strValue : strArray)
		{
			sbString.append(strValue);
			sbString.append(strDelim);
		}
		
		String result = sbString.toString();
		TextBuilder.recycle(sbString);
		return result;
	}
	
	/**
	 * (Based on round() in PHP)
	 * @param number - the number to round
	 * @param numPlaces - how many digits after decimal point to leave intact
	 * @return the value of {@code number} rounded to specified number of digits after the decimal point.
	 */
	public static float roundTo(float number, int numPlaces)
	{
		if (numPlaces <= 1)
		{
			return Math.round(number);
		}
		
		float exponent = (float) Math.pow(10, numPlaces);
		return Math.round(number * exponent) / exponent;
	}
	
	/**
	 * @param text - the text to check
	 * @return {@code true} if {@code text} contains only numbers, {@code false} otherwise
	 */
	public static boolean isDigit(String text)
	{
		if ((text == null) || text.isEmpty())
		{
			return false;
		}
		for (char c : text.toCharArray())
		{
			if (!Character.isDigit(c))
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * @param text - the text to check
	 * @return {@code true} if {@code text} contains only letters and/or numbers, {@code false} otherwise
	 */
	public static boolean isAlphaNumeric(String text)
	{
		if ((text == null) || text.isEmpty())
		{
			return false;
		}
		for (char c : text.toCharArray())
		{
			if (!Character.isLetterOrDigit(c))
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Format the specified digit using the digit grouping symbol "," (comma).<br>
	 * For example, 123456789 becomes 123,456,789.
	 * @param amount - the amount of adena
	 * @return the formatted adena amount
	 */
	public static String formatAdena(long amount)
	{
		String s = "";
		long rem = amount % 1000;
		s = Long.toString(rem);
		amount = (amount - rem) / 1000;
		while (amount > 0)
		{
			if (rem < 99)
			{
				s = '0' + s;
			}
			if (rem < 9)
			{
				s = '0' + s;
			}
			rem = amount % 1000;
			s = Long.toString(rem) + "," + s;
			amount = (amount - rem) / 1000;
		}
		return s;
	}
	
	/**
	 * Format the given date on the given format
	 * @param date : the date to format.
	 * @param format : the format to correct by.
	 * @return a string representation of the formatted date.
	 */
	public static String formatDate(Date date, String format)
	{
		if (date == null)
		{
			return null;
		}
		final DateFormat dateFormat = new SimpleDateFormat(format);
		return dateFormat.format(date);
	}
	
	/**
	 * @param <T>
	 * @param array - the array to look into
	 * @param obj - the object to search for
	 * @return {@code true} if the {@code array} contains the {@code obj}, {@code false} otherwise.
	 */
	public static <T> boolean contains(T[] array, T obj)
	{
		for (T element : array)
		{
			if (element == obj)
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @param array - the array to look into
	 * @param obj - the integer to search for
	 * @return {@code true} if the {@code array} contains the {@code obj}, {@code false} otherwise
	 */
	public static boolean contains(int[] array, int obj)
	{
		for (int element : array)
		{
			if (element == obj)
			{
				return true;
			}
		}
		return false;
	}
	
	public static File[] getDatapackFiles(String dirname, String extention)
	{
		File dir = new File(Config.DATAPACK_ROOT, "data/" + dirname);
		if (!dir.exists())
		{
			return null;
		}
		return dir.listFiles(new ExtFilter(extention));
	}
	
	public static String getDateString(Date date)
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return dateFormat.format(date.getTime());
	}
	
	/**
	 * Sends the given html to the player.
	 * @param activeChar
	 * @param html
	 */
	public static void sendHtml(L2PcInstance activeChar, String html)
	{
		NpcHtmlMessage npcHtml = new NpcHtmlMessage(0);
		npcHtml.setHtml(html);
		activeChar.sendPacket(npcHtml);
	}
	
	/**
	 * Sends the html using the community board window.
	 * @param activeChar
	 * @param html
	 */
	public static void sendCBHtml(L2PcInstance activeChar, String html)
	{
		sendCBHtml(activeChar, html, "");
	}
	
	/**
	 * Sends the html using the community board window.
	 * @param activeChar
	 * @param html
	 * @param fillMultiEdit fills the multiedit window (if any) inside the community board.
	 */
	public static void sendCBHtml(L2PcInstance activeChar, String html, String fillMultiEdit)
	{
		if (activeChar == null)
			return;
		
		if (html != null)
		{
			activeChar.clearBypass();
			int len = html.length();
			for (int i = 0; i < len; i++)
			{
				int start = html.indexOf("\"bypass ", i);
				int finish = html.indexOf("\"", start + 1);
				if (start < 0 || finish < 0)
					break;
				
				if (html.substring(start + 8, start + 10).equals("-h"))
					start += 11;
				else
					start += 8;
				
				i = finish;
				int finish2 = html.indexOf("$", start);
				if (finish2 < finish && finish2 > 0)
					activeChar.addBypass2(html.substring(start, finish2).trim());
				else
					activeChar.addBypass(html.substring(start, finish).trim());
			}
		}
		
		if (fillMultiEdit != null)
		{
			activeChar.sendPacket(new ShowBoard(html, "1001"));
			fillMultiEditContent(activeChar, fillMultiEdit);
		}
		else
		{
			activeChar.sendPacket(new ShowBoard(null, "101"));
			activeChar.sendPacket(new ShowBoard(html, "101"));
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
	}
	
	/**
	*
	* Fills the community board's multiedit window with text. Must send after sendCBHtml
	* @param activeChar 
	* @param text 
	*/
	public static void fillMultiEditContent(L2PcInstance activeChar, String text)
	{
		text = text.replaceAll("<br>", "\n");
		List<String> arg = new FastList<>();
		arg.add("0");
		arg.add("0");
		arg.add("0");
		arg.add("0");
		arg.add("0");
		arg.add("0");
		arg.add(activeChar.getName());
		arg.add(Integer.toString(activeChar.getObjectId()));
		arg.add(activeChar.getAccountName());
		arg.add("9");
		arg.add(" ");
		arg.add(" ");
		arg.add(text);
		arg.add("0");
		arg.add("0");
		arg.add("0");
		arg.add("0");
		activeChar.sendPacket(new ShowBoard(arg));
	}
}

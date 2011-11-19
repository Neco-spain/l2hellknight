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

import java.util.logging.Logger;

public class L2Brick
{
	private static final Logger _log = Logger.getLogger(L2Brick.class.getName());
	
	public static void info()
	{
		_log.info("-------------------------------------------------------------------------------");
		_log.info("                           We are the Future!                                  ");
		_log.info("                           Client: High Five                                   ");
		_log.info("                                 2011                                          ");
		_log.info("-------------------------------------------------------------------------------");
		_log.info("                         www.l2brick.funsite.cz                                ");
		_log.info("-------------------------------------------------------------------------------");
	}
}
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

public class L2HellKnight
{
	private static final Logger _log = Logger.getLogger(L2HellKnight.class.getName());
	
	public static void info()
	{
		_log.info("-------------------------------------------------------------------------------");
		_log.info(" ##   ## ###### ##     ##       ##  ## ##   ## ** ###### ##   ## ######        ");
        _log.info("	##   ## ##     ##     ##       ## ##  ###  ## ## ##     ##   ##   ##          ");
        _log.info(" ####### ####   ##     ##       ####   ## # ## ## ##     #######   ##          ");
		_log.info(" ##   ## ##     ##     ##       ## ##  ##  ### ## ##  ## ##   ##   ##          ");
		_log.info(" ##   ## ###### ###### ######   ##  ## ##   ## ## ###### ##   ##   ##          ");
		_log.info("-------------------------------------------------------------------------------");
		_log.info("                         www.l2hellknight.com                                  ");
		_log.info("-------------------------------------------------------------------------------");
	}
}
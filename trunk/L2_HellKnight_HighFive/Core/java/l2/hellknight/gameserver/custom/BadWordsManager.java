/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package l2.hellknight.gameserver.custom;

public class BadWordsManager
{

    // =========================================================
    private static BadWordsManager _instance;

    public static final BadWordsManager getInstance() 
    {
        if (_instance == null)
        {
            _instance = new BadWordsManager();
        }
        return _instance;
    }
    
    private final String[] _badwords1 = new String[] 
    {
            "http", "www", ".com", ".org", ".net", ".fr", ".name",
            "dot com", "dot org", "dot net", "dot fr", "dot name"
            };
    private final String[] _badwords2 = new String[] 
    {
            "connard", "encule", "fdp", "pute", "petasse", "batard", "salope",
            "trou de balle", "pedale", "tafiole", "enfoire", "tg", "suceur", "merde",
            "http", "www", ".com", ".org", ".net", ".fr", ".name",
            "dot com", "dot org", "dot net", "dot fr", "dot name", "l2"
            };
    
    public boolean checkPresentation(String Text)
    {
        for(String mot : _badwords1)
        {
            if(Text.contains(mot))
                return false;
        }
        for(String mot : _badwords2)
        {
            if(Text.contains(mot))
                return false;
        }
        return true;
    }
    
    public boolean checkAll(String Text)
    {
    	for(String mot : _badwords2)
    	{
    		if(Text.contains(mot))
    			return false;
    	}
    	return true;
    }
    
}

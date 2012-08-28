package net.sf.l2j.gameserver.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.L2PetDataTable;

public class PetNameTable
{
  private static Logger _log = Logger.getLogger(PetNameTable.class.getName());
  private static PetNameTable _instance;

  public static PetNameTable getInstance()
  {
    if (_instance == null)
    {
      _instance = new PetNameTable();
    }
    return _instance;
  }

  public boolean doesPetNameExist(String name, int petNpcId)
  {
    boolean result = true;
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT name FROM pets p, items i WHERE p.item_obj_id = i.object_id AND name=? AND i.item_id IN (?)");
      statement.setString(1, name);

      String cond = "";
      for (int it : L2PetDataTable.getPetItemsAsNpc(petNpcId))
      {
        if (cond != "") cond = cond + ", ";
        cond = cond + it;
      }
      statement.setString(2, cond);
      ResultSet rset = statement.executeQuery();
      result = rset.next();
      rset.close();
      statement.close();
    }
    catch (SQLException e)
    {
      _log.warning("could not check existing petname:" + e.getMessage());
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
    return result;
  }

  public boolean isValidPetName(String name)
  {
    boolean result = true;

    if (!isAlphaNumeric(name)) return result;
    Pattern pattern;
    try
    {
      pattern = Pattern.compile(Config.PET_NAME_TEMPLATE);
    }
    catch (PatternSyntaxException e)
    {
      _log.warning("ERROR : Pet name pattern of config is wrong!");
      pattern = Pattern.compile(".*");
    }
    Matcher regexp = pattern.matcher(name);
    if (!regexp.matches())
    {
      result = false;
    }
    return result;
  }

  private boolean isAlphaNumeric(String text)
  {
    boolean result = true;
    char[] chars = text.toCharArray();
    for (int i = 0; i < chars.length; i++)
    {
      if (Character.isLetterOrDigit(chars[i]))
        continue;
      result = false;
      break;
    }

    return result;
  }
}
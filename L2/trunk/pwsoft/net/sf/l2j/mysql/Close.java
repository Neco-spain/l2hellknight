package net.sf.l2j.mysql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Close
{
  public static void C(Connect c)
  {
    if (c != null) {
      c.close();
    }
    c = null;
  }

  public static void S(PreparedStatement s) {
    if (s != null)
      try {
        s.close();
      }
      catch (SQLException e) {
      }
    s = null;
  }

  public static void S2(Statement s) {
    if (s != null)
      try {
        s.close();
      }
      catch (SQLException e) {
      }
    s = null;
  }

  public static void R(ResultSet r) {
    if (r != null)
      try {
        r.close();
      }
      catch (SQLException e) {
      }
    r = null;
  }

  public static void CSR(Connect c, PreparedStatement s, ResultSet r) {
    C(c);
    S(s);
    R(r);
  }

  public static void CS(Connect c, PreparedStatement s) {
    C(c);
    S(s);
  }

  public static void SR(PreparedStatement s, ResultSet r) {
    S(s);
    R(r);
  }
}
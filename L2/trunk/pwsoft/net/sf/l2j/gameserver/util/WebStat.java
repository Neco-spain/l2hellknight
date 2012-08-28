package net.sf.l2j.gameserver.util;

import java.io.PrintStream;

public class WebStat
{
  private static WebStat _instance = new WebStat();

  public static WebStat getInstance() {
    return _instance;
  }

  public static void init() {
    System.out.println("WebStat [ERROR]: only for advanced.");
  }

  public void addLogin(String ip)
  {
  }

  public void addGame(String ip)
  {
  }

  public void addKill(String killer, String victim)
  {
  }

  public void addEnchant(String name, String item, int ench, int sucess)
  {
  }
}
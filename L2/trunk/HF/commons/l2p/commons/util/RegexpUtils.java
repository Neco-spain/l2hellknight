package l2m.commons.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexpUtils
{
  private static HashMap<String, Pattern> cache = new HashMap();

  public void clearCache()
  {
    cache.clear();
  }

  public static String preg_replace_callback(String pattern, String input, Replacer by)
  {
    Pattern p = compile(pattern, false);
    Matcher m = p.matcher(input);
    int gcount = m.groupCount();
    StringBuffer sb = new StringBuffer();
    ArrayList row = new ArrayList();

    while (m.find()) {
      try {
        row.clear();
        for (int i = 0; i <= gcount; i++)
          row.add(m.group(i));
        m.appendReplacement(sb, by.onMatch(row));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    m.appendTail(sb);
    return sb.toString();
  }

  public static String preg_replace(String pattern, String input, String by)
  {
    Pattern p = compile(pattern, false);
    Matcher m = p.matcher(input);
    StringBuffer sb = new StringBuffer();
    while (m.find()) {
      try {
        m.appendReplacement(sb, by);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    m.appendTail(sb);
    return sb.toString();
  }

  public static boolean preg_match(String pattern, String input, List<String> rez)
  {
    Pattern p = compile(pattern, true);
    Matcher m = p.matcher(input);
    int gcount = m.groupCount();
    if (rez != null)
      rez.clear();
    if (m.matches()) {
      for (int i = 0; i <= gcount; i++)
        if (rez != null)
          rez.add(m.group(i));
    }
    return rez.size() > 0;
  }

  public static boolean preg_match_all(String pattern, String input, List<List<String>> rez)
  {
    Pattern p = compile(pattern, true);
    Matcher m = p.matcher(input);
    int gcount = m.groupCount();
    if (rez != null)
      rez.clear();
    while (m.find()) {
      List row = new ArrayList();
      for (int i = 0; i <= gcount; i++) {
        if (rez != null)
          row.add(m.group(i));
      }
      if (rez != null)
        rez.add(row);
    }
    return rez.size() > 0;
  }

  private static Pattern compile(String pattern, boolean surroundBy)
  {
    if (cache.containsKey(pattern)) return (Pattern)cache.get(pattern);
    String pattern_orig = pattern;

    char firstChar = pattern.charAt(0);
    char endChar = firstChar;
    if (firstChar == '(') endChar = '}';
    if (firstChar == '[') endChar = ']';
    if (firstChar == '{') endChar = '}';
    if (firstChar == '<') endChar = '>';

    int lastPos = pattern.lastIndexOf(endChar);
    if (lastPos == -1) {
      throw new RuntimeException("Invalid pattern: " + pattern);
    }
    char[] modifiers = pattern.substring(lastPos + 1).toCharArray();
    int mod = 0;
    for (int i = 0; i < modifiers.length; i++) {
      char modifier = modifiers[i];
      switch (modifier) {
      case 'i':
        mod |= 2;
        break;
      case 'd':
        mod |= 1;
        break;
      case 'x':
        mod |= 4;
        break;
      case 'm':
        mod |= 8;
        break;
      case 's':
        mod |= 32;
        break;
      case 'u':
        mod |= 64;
      }
    }

    pattern = pattern.substring(1, lastPos);
    if (surroundBy) {
      if (pattern.charAt(0) != '^')
        pattern = ".*?" + pattern;
      if (pattern.charAt(pattern.length() - 1) != '$') {
        pattern = pattern + ".*?";
      }
    }
    Pattern rezPattern = Pattern.compile(pattern, mod);
    cache.put(pattern_orig, rezPattern);
    return rezPattern;
  }

  public static abstract interface Replacer
  {
    public abstract String onMatch(List<String> paramList);
  }
}
package net.sf.l2j.gameserver.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DynamicExtension
{
  private static Logger _log = Logger.getLogger(DynamicExtension.class.getCanonicalName());
  private JarClassLoader _classLoader;
  private static final String CONFIG = "config/extensions.ini";
  private Properties _prop;
  private ConcurrentHashMap<String, Object> _loadedExtensions;
  private static DynamicExtension _instance;
  private ConcurrentHashMap<String, ExtensionFunction> _getters;
  private ConcurrentHashMap<String, ExtensionFunction> _setters;

  private DynamicExtension()
  {
    if (_instance == null)
      _instance = this;
    _getters = new ConcurrentHashMap();
    _setters = new ConcurrentHashMap();
    initExtensions();
  }
  public static DynamicExtension getInstance() {
    if (_instance == null)
      _instance = new DynamicExtension();
    return _instance;
  }
  public Object getExtension(String className) {
    return _loadedExtensions.get(className);
  }
  public String initExtensions() {
    _prop = new Properties();
    String res = "";
    _loadedExtensions = new ConcurrentHashMap();
    try {
      _prop.load(new FileInputStream("config/extensions.ini"));
    } catch (FileNotFoundException ex) {
      _log.info(ex.getMessage() + ": no extensions to load");
    } catch (Exception ex) {
      _log.log(Level.WARNING, "could not load properties", ex);
    }
    _classLoader = new JarClassLoader();
    for (Iterator i$ = _prop.keySet().iterator(); i$.hasNext(); ) { Object o = i$.next();
      String k = (String)o;
      if (k.endsWith("Class")) {
        res = res + initExtension(_prop.getProperty(k)) + "\n";
      }
    }
    return res;
  }
  public String initExtension(String name) {
    String className = name;
    String[] p = name.split("@");
    String res = name + " loaded";
    if (p.length > 1) {
      _classLoader.addJarFile(p[1]);
      className = p[0];
    }
    if (_loadedExtensions.containsKey(className))
      return "already loaded";
    try {
      Class extension = Class.forName(className, true, _classLoader);
      Object obj = extension.newInstance();
      extension.getMethod("init", new Class[0]).invoke(obj, new Object[0]);
      _log.info("Extension " + className + " loaded.");
      _loadedExtensions.put(className, obj);
    } catch (Exception ex) {
      _log.log(Level.WARNING, name, ex);
      res = ex.toString();
    }
    return res;
  }
  protected void clearCache() {
    _classLoader = new JarClassLoader();
  }
  public String unloadExtensions() {
    String res = "";
    for (String e : _loadedExtensions.keySet())
      res = res + unloadExtension(e) + "\n";
    return res;
  }
  public String[] getExtensions() {
    String[] l = new String[_loadedExtensions.size()];
    _loadedExtensions.keySet().toArray(l);
    return l;
  }
  public String unloadExtension(String name) {
    String className = name;
    String[] p = name.split("@");
    if (p.length > 1) {
      _classLoader.addJarFile(p[1]);
      className = p[0];
    }
    String res = className + " unloaded";
    try {
      Object obj = _loadedExtensions.get(className);
      Class extension = obj.getClass();
      _loadedExtensions.remove(className);
      extension.getMethod("unload", new Class[0]).invoke(obj, new Object[0]);
      _log.info("Extension " + className + " unloaded.");
    } catch (Exception ex) {
      _log.log(Level.WARNING, "could not unload " + className, ex);
      res = ex.toString();
    }
    return res;
  }

  public void reload() {
    unloadExtensions();
    clearCache();
    initExtensions();
  }
  public void reload(String name) {
    unloadExtension(name);
    clearCache();
    initExtension(name);
  }
  public void addGetter(String name, ExtensionFunction function) {
    _getters.put(name, function);
  }
  public void removeGetter(String name) {
    _getters.remove(name);
  }
  public Object get(String name, String arg) {
    ExtensionFunction func = (ExtensionFunction)_getters.get(name);
    if (func != null)
      return func.get(arg);
    return "<none>";
  }
  public void addSetter(String name, ExtensionFunction function) {
    _setters.put(name, function);
  }
  public void removeSetter(String name) {
    _setters.remove(name);
  }
  public void set(String name, String arg, Object obj) {
    ExtensionFunction func = (ExtensionFunction)_setters.get(name);
    if (func != null)
      func.set(arg, obj);
  }

  public JarClassLoader getClassLoader()
  {
    return _classLoader;
  }
}
package net.sf.l2j.gameserver.util;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class JarClassLoader extends ClassLoader
{
  private static Logger _log = Logger.getLogger(JarClassLoader.class.getCanonicalName());
  HashSet<String> _jars = new HashSet();

  public void addJarFile(String filename) {
    _jars.add(filename);
  }

  public Class<?> findClass(String name) throws ClassNotFoundException
  {
    try {
      byte[] b = loadClassData(name);
      return defineClass(name, b, 0, b.length); } catch (Exception e) {
    }
    throw new ClassNotFoundException(name);
  }

  private byte[] loadClassData(String name) throws IOException
  {
    byte[] classData = null;
    for (String jarFile : _jars) {
      try {
        File file = new File(jarFile);
        ZipFile zipFile = new ZipFile(file);
        String fileName = name.replace('.', '/') + ".class";
        ZipEntry entry = zipFile.getEntry(fileName);
        if (entry == null)
          continue;
        classData = new byte[(int)entry.getSize()];
        DataInputStream zipStream = new DataInputStream(zipFile.getInputStream(entry));
        zipStream.readFully(classData, 0, (int)entry.getSize());
      }
      catch (IOException e) {
        _log.log(Level.WARNING, jarFile + ":" + e.toString(), e);
      }
    }

    if (classData == null)
      throw new IOException("class not found in " + _jars);
    return classData;
  }
}
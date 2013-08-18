/*
 * Copyright 2008-2010 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package compiler.java;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardLocation;

import files.RelativeSourcePath;
import files.Package;
import util.Multimap;

/**
 * A JavaFileManager that collects the name and bytecode of compiled classes
 * into {@link CompiledClass} objects. Also allow the compiler to find classes
 * that were previously compiled via the {@link #list()} method.
 *
 * Based on the original work by A. Sundararajan.
 */
public final class CollectingJavaFileManager
extends ForwardingJavaFileManager<JavaFileManager>
{
  /*****************************************************************************
   * A file object that stores the bytecode into a new {@link CompiledClass}
   * object it adds to $classes.
   */
  private class ByteClass extends SimpleJavaFileObject
  {
    private final String name;

    ByteClass(String name)
    {
      super(URI.create("string:///" + name), Kind.CLASS);
      this.name = name;
      byteclasses.add(RelativeSourcePath.make(name).pkg(), this);
    }

    @Override public InputStream openInputStream()
    {
      return new ByteArrayInputStream(
        MemoryClassLoader.get().getBytecode(name));
    }

    @Override public OutputStream openOutputStream()
    {
      return new FilterOutputStream(new ByteArrayOutputStream())
      {
        @Override public void close() throws IOException
        {
          out.close();
          ByteArrayOutputStream bos = (ByteArrayOutputStream) out;
          classes.add(new CompiledClass(name, bos.toByteArray()));
        }
      };
    }
  }
  //////////////////////////////////////////////////////////////////////////////

  /****************************************************************************/
  public final List<CompiledClass> classes = new ArrayList<>();

  /****************************************************************************/
  public final Multimap<Package, JavaFileObject> byteclasses = new Multimap<>();

  /****************************************************************************/
  public CollectingJavaFileManager(JavaFileManager fileManager)
  {
    super(fileManager);
  }

  /*****************************************************************************
   * javax.tools.JavaCompiler seems to pick up the class loader even without
   * this (which is a bit surprising to me), but you're better safe than sorry.
   */
  @Override public ClassLoader getClassLoader(JavaFileManager.Location location)
  {
    return MemoryClassLoader.get();
  }

  /****************************************************************************/
  @Override public Iterable<JavaFileObject> list(Location location,
    String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse)
  throws IOException
  {
    Iterable<JavaFileObject> stdResults = fileManager.list(location, packageName, kinds, recurse);

    if (location != StandardLocation.CLASS_PATH
    ||  !kinds.contains(JavaFileObject.Kind.CLASS))
    {
        return stdResults;
    }

    Set<JavaFileObject> notOnDisk = byteclasses.get(new Package(packageName));

    if (notOnDisk == null || notOnDisk.isEmpty()) {
      return stdResults;
    }

    List<JavaFileObject> out = new ArrayList<>();

    for (JavaFileObject obj : notOnDisk) {
      out.add(obj);
    }
    for (JavaFileObject obj : stdResults) {
      out.add(obj);
    }

    return out;
  }

  /****************************************************************************/
  @Override public void close() throws IOException
  {
    classes.clear();
  }

  /****************************************************************************/
  @Override public JavaFileObject getJavaFileForOutput(
    Location location, String className, Kind kind, FileObject sibling)
  throws IOException
  {
    if (kind == Kind.CLASS) {
      return new ByteClass(className);
    } else {
      return super.getJavaFileForOutput(location, className, kind, sibling);
    }
  }

  /****************************************************************************/
  @Override public String inferBinaryName(
    Location location, JavaFileObject file)
  {
    if (file instanceof ByteClass) {
      return ((ByteClass) file).name;
    } else {
      return fileManager.inferBinaryName(location, file);
    }
  }
}

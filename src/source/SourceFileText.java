//=========================================================================
//
//  Part of PEG parser generator Mouse.
//
//  Copyright (C) 2009, 2010, 2011
//  by Roman R. Redziejowski (www.romanredz.se).
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//
//-------------------------------------------------------------------------
//
//  Change log
//    090701 License changed by the author to Apache v.2.
//    090810 Renamed from 'SourceString' and package name changed.
//   Version 1.2
//    100413 Added method 'file'.
//   Version 1.3
//    101130 Changed to default character encoding.
//    101130 Changed 'catch' from 'Exception' to 'IOException'.
//    101130 Maps file to String instead of CharBuffer.
//   Version 1.3.1
//    110113 In 'where()': changed condition for return from <= < to < <=.
//           (Bug fix for endless loop if p = end of file.)
//
//=========================================================================

package source;

import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.nio.channels.*;

import driver.Config;

//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
//
//  Wrapper for parser input in the form of a file.
//  Maps the entire file into a String using default character encoding.
//
//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

public class SourceFileText implements Source
{
  //=====================================================================
  //
  //  Data.
  //
  //=====================================================================
  //-------------------------------------------------------------------
  //  The file.
  //-------------------------------------------------------------------
  private final File f;

  //-------------------------------------------------------------------
  //  Memory-mapped file.
  //-------------------------------------------------------------------
  private final String text;

  //=====================================================================
  //
  //  Constructor. Wraps the file identified by 'fileName'.
  //
  //=====================================================================
  public SourceFileText(String fileName) throws IOException
  {
    this(fileName, Config.get().charset());
  }

  //=====================================================================
  //
  //  Constructor. Wraps the file identified by 'fileName'.
  //
  //=====================================================================
  public SourceFileText(File f) throws IOException
  {
    this(f, Config.get().charset());
  }

  //=====================================================================
  //
  //  Constructor. Wraps the file identified by 'fileName'.
  //  Specify charset with e.g. Charset.forName("8859_1")
  //
  //=====================================================================
  public SourceFileText(String fileName, Charset cs)
  throws IOException
  {
    this(new File(fileName), cs);
  }

  public SourceFileText(File f, Charset cs)
  throws IOException
  {
    this.f = f;

    FileInputStream fis = new FileInputStream(f);
    FileChannel fc = fis.getChannel();

    // Get a CharBuffer from the source file
    ByteBuffer bb =
        fc.map(FileChannel.MapMode.READ_ONLY, 0, (int)fc.size());
    CharsetDecoder cd = cs.newDecoder();
    CharBuffer cb = cd.decode(bb);
    fis.close();

    // Convert to String and unescape Unicode (\\uXXXX)
    text = unescaper.translate(cb);
  }

  //=====================================================================
  //
  //  Interface methods.
  //
  //=====================================================================

  //-------------------------------------------------------------------
  //  Returns end position.
  //-------------------------------------------------------------------
  public int end()
    { return text.length(); }

  //-------------------------------------------------------------------
  //  Returns character at position p.
  //-------------------------------------------------------------------
  public char at(int p)
    { return text.charAt(p); }

  //-------------------------------------------------------------------
  //  Returns characters at positions p through q-1.
  //-------------------------------------------------------------------
  public String at(int p, int q)
    { return text.substring(p,q); }

  //-------------------------------------------------------------------
  //  Describes position p in terms of line and column number.
  //  Lines and columns are numbered starting with 1.
  //-------------------------------------------------------------------
  public String where(int p)
    {
      int ln = 1;   // Line number
      int ls = -1;  // Line start (position of preceding newline)
      int nextnl;   // Position of next newline or end

      while (true)
      {
        nextnl = text.indexOf('\n',ls+1);
        if (nextnl<0) nextnl = text.length();
        if (ls<p && p<=nextnl)
          return ("line " + ln + " col. " + (p-ls));
        ls = nextnl;
        ln++;
      }
    }

  //=====================================================================
  //
  //  File-specific method.
  //
  //=====================================================================
  //-------------------------------------------------------------------
  //  Returns the file object.
  //-------------------------------------------------------------------
  public File file()
    { return f; }

  /****************************************************************************/
  @Override public String toString()
  {
    return f.toString();
  }
}

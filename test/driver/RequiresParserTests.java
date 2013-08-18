package driver;

import static driver.RequiresParserTests.Type.MACRO;
import static driver.RequiresParserTests.Type.REGULAR;
import static driver.RequiresParserTests.Type.STATIC;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static util.ListUtils.list;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import util.Result;

import files.RelativeSourcePath;
import files.Require;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;
import parser.Matcher;
import source.Source;
import source.SourceString;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RequiresParserTests
{
  enum Type { REGULAR, MACRO, STATIC }

  void verifyReq(Require req, Type type, List<String> classes,
    String member, String path)
  {
    assertTrue(
      req.isMacro()   && type == MACRO   ||
      req.isRegular() && type == REGULAR ||
      req.isStatic()  && type == STATIC);

    assertTrue( // unique type
      (req.isMacro() ^ req.isRegular() ^ req.isStatic()) &&
      !(req.isMacro() && req.isRegular()));

    assertEquals(classes, req.classes());
    assertEquals(member, req.isMacro() ? req.macro() : req.member());
    assertEquals(new RelativeSourcePath(Paths.get(path)), req.relativePath());
  }

  String sourceString = "package pkg;"
    + "require pkg.File:Class;"
    + "require pkg.File:Class.*;"
    + "require pkg.File:Class.Nested;"
    + "require pkg.ClassAndFile;"
    + "require pkg.ClassAndFile::;"
    + "require pkg.ClassAndFile::*;"
    + "require pkg.ClassAndFile::Nested;"
    + "require static pkg.File:Class.*;"
    + "require static pkg.File:Class.staticMeth;"
    + "require static pkg.ClassAndFile::*;"
    + "require static pkg.ClassAndFile::staticMeth;"
    + "require macro pkg.File:*;"
    + "require macro pkg.File:Macro;"
    + "require macro pkg.MacroAndFile;"
    + "require macro pkg.MacroAndFile::;"
    + "import java.util.List;";

  @Test public void aa_parseRequires()
  {
    Source source = new SourceString(sourceString);
    Matcher matcher = new Matcher(source);
    RequiresParser reqp = new RequiresParser();

    List<Require> reqs = new ArrayList<>();
    List<String>  imps = new ArrayList<>();
    Result<String> pkg = new Result<>();
    reqp.parseRequires(matcher, pkg, reqs, imps);

    assertEquals("pkg", pkg.get());
    assertEquals(15, reqs.size());

    int    i   = 0;
    String m   = "Macro";
    String c   = "Class";
    String n   = "Nested";
    String cf  = "ClassAndFile";
    String mf  = "MacroAndFile";
    String sm  = "staticMeth";
    String fp  = "pkg/File.java";
    String fpm = "pkg/File.javam";
    String cfp = "pkg/ClassAndFile.java";
    String mfp = "pkg/MacroAndFile.javam";

    verifyReq(reqs.get(i++), REGULAR, list(c),       null, fp);
    verifyReq(reqs.get(i++), REGULAR, list(c, "*"),  null, fp);
    verifyReq(reqs.get(i++), REGULAR, list(c, n),    null, fp);
    verifyReq(reqs.get(i++), REGULAR, list(cf),      null, cfp);
    verifyReq(reqs.get(i++), REGULAR, list(cf),      null, cfp);
    verifyReq(reqs.get(i++), REGULAR, list(cf, "*"), null, cfp);
    verifyReq(reqs.get(i++), REGULAR, list(cf, n),   null, cfp);

    verifyReq(reqs.get(i++), STATIC,  list(c),       "*",  fp);
    verifyReq(reqs.get(i++), STATIC,  list(c),       sm,   fp);
    verifyReq(reqs.get(i++), STATIC,  list(cf),      "*",  cfp);
    verifyReq(reqs.get(i++), STATIC,  list(cf),      sm,   cfp);

    verifyReq(reqs.get(i++), MACRO,   null,          "*",  fpm);
    verifyReq(reqs.get(i++), MACRO,   null,          m,    fpm);
    verifyReq(reqs.get(i++), MACRO,   null,          mf,   mfp);
    verifyReq(reqs.get(i++), MACRO,   null,          mf,   mfp);
  }

  @Test public void ab_processPackageImport()
  {

  }
}

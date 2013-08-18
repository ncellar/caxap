package main;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  util.ArrayUtilsTests.class,
  util.MemberAccessorTests.class,
  util.ArrayIteratorTests.class,
  files.PackageTests.class,
  files.RelativeSourcePathTests.class,
  files.RootedSourcePathTests.class,
  files.RequireTests.class,
  trees.MatchTreeIteratorTests.class,
  trees.BoundedMatchIteratorTests.class,
  trees.MatchFinderTests.class,
  driver.RequiresTests.class,
  driver.RequiresParserTests.class,
  compiler.QuoterTests.class,
  compiler.MacroExpanderTests.class,
  compiler.QuotationMacroTests.class,
  compiler.util.PEGCompilerTests.class,
  compiler.MacroCompilerTests.class,
})

/**
 * The Maven Surefire plugin picks test classes using the pattern
 * Test* | *Test | *TestCase
 *
 * Ergo, this class will be picked, but none of the other
 * tests class which end in "Tests". This allows us to
 * specify the order of tests and exclude test classes.
 */
public class AllTest {}

package main;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import grammar.Grammar;
import grammar.java.JavaGrammar;
import parser.MatchTreePrinter;
import parser.Matcher;
import parser.StatisticsReporter;
import source.Source;
import source.SourceFileText;
import util.FileUtils;

/**
 * Tests the parser by parsing the whole OpenJDK source
 * (build b147 of OpenJDK 7; June 27, 2011).
 *
 * Class {@link OpenJDKExcludes} holds files a list of files to be ignored by
 * those tests.
 */
class ParseOpenJDK
{
  /****************************************************************************/
  static long running;

  /****************************************************************************/
  public static void main(final String[] args) throws IOException,
    InterruptedException
  {
    MatchTreePrinter.class.getClass(); // inhibit unused import warning

    long start = System.nanoTime();
    running = start;

    Grammar grammar = new Grammar(JavaGrammar.class);

    List<Path> files = FileUtils.glob(Paths.get("C:\\h\\desk\\openjdk\\"), "**.java");
    //List<Path> files = FileUtils.glob(Paths.get("src"), "**.java");

    System.out.println("warmup: " + (System.nanoTime() - running) / 1000_000);

    for (Path file : files) {
      if (OpenJDKExcludes.excludes.contains(file.toString())) {
        continue;
      }

      running = System.nanoTime();
      System.out.print(file);

      SourceFileText source =
        new SourceFileText(file.toString(), Charset.forName("8859_1"));

      runMatcher(source, grammar);
//      runStatistics(source, grammar);
    }

    System.out.println((System.nanoTime() - start) / 1000_000);
  }

  /****************************************************************************/
  static void runMatcher(Source source, Grammar grammar)
  {
    Matcher matcher = new Matcher(source);

    if (matcher.matches(grammar.rule("compilationUnit"))) {
      print(true, matcher);
      new MatchTreePrinter(matcher.match(), System.out);//.print();
    }
    else {
      print(false, matcher);
      matcher.errors().report(source);
    }
  }

  /****************************************************************************/
  static void runStatistics(Source source, Grammar grammar)
  {
    StatisticsReporter reporter = new StatisticsReporter(source);

    if (reporter.matches(grammar.rule("compilationUnit"))) {
      print(true, reporter);
      System.out.println();
      reporter.report();
    }
    else {
      print(false, reporter);
    }
  }

  /****************************************************************************/
  static void print(boolean ok, Matcher matcher)
  {
    if (ok) {
      long lap = (System.nanoTime() - running) / 1000_000;
      System.out.println(" : ok (" + lap + ")"
        + ((lap > 5000) ? " #large" : ""));
    }
    else {
      System.out.println(" : no");
      //System.out.println(matcher.errors().report(matcher.source()));
    }
  }
}

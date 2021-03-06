/*
 * Copyright (c) 2012, the Dart project authors.
 * 
 * Licensed under the Eclipse Public License v1.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.dart.engine.integration;

import com.google.dart.engine.AnalysisEngine;
import com.google.dart.engine.context.AnalysisContext;
import com.google.dart.engine.element.CompilationUnitElement;
import com.google.dart.engine.element.LibraryElement;
import com.google.dart.engine.error.AnalysisError;
import com.google.dart.engine.sdk.DartSdk;
import com.google.dart.engine.source.DartUriResolver;
import com.google.dart.engine.source.FileBasedSource;
import com.google.dart.engine.source.FileUriResolver;
import com.google.dart.engine.source.Source;
import com.google.dart.engine.source.SourceFactory;
import com.google.dart.engine.utilities.io.FileUtilities;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class Co19AnalysisTest extends DirectoryBasedSuiteBuilder {
  public class ReportingTest extends TestCase {
    public ReportingTest(String methodName) {
      super(methodName);
    }

    public void reportResults() throws Exception {
      System.out.print("Analyzed ");
      System.out.print(fileCount);
      System.out.print(" files in ");
      printTime(totalTime);
      System.out.println();

      System.out.print(errorCount);
      System.out.println(" tests failed with unexpected errors");

      System.out.print(noErrorCount);
      System.out.println(" tests failed with no errors being generated");
    }

    private void printTime(long time) {
      if (time == 0) {
        System.out.print("0 ms");
      } else {
        System.out.print(time);
        System.out.print(" ms");
        if (time > 60000) {
          long seconds = time / 1000;
          long minutes = seconds / 60;
          seconds -= minutes * 60;
          System.out.print(" (");
          System.out.print(minutes);
          System.out.print(":");
          if (seconds < 10) {
            System.out.print("0");
          }
          System.out.print(seconds);
          System.out.print(")");
        }
      }
    }
  }

  /**
   * The pattern used to determine whether a file should be run as a test.
   */
  private static final Pattern FILE_NAME_PATTERN = Pattern.compile(".*_A\\d\\d_t\\d\\d.dart\\z");

  /**
   * An array containing the relative paths of test files that are expected to fail.
   */
  private static final String[] FAILING_TESTS = {};

  /**
   * Build a JUnit test suite that will analyze all of the tests in the co19 test suite.
   * 
   * @return the test suite that was built
   */
  public static Test suite() {
    String directoryName = System.getProperty("co19Directory");
    if (directoryName != null) {
      File directory = new File(directoryName);
      Co19AnalysisTest tester = new Co19AnalysisTest();
      TestSuite suite = tester.buildSuite(directory, "Analyze co19 files");
      suite.addTest(tester.new ReportingTest("reportResults"));
      return suite;
    }
    return new TestSuite("Analyze co19 files (no tests: directory not found)");
  }

  /**
   * Return {@code true} if the given file should be skipped because it is on the list of files to
   * be skipped.
   * 
   * @param file the file being tested
   * @return {@code true} if the file should be skipped
   */
  private static boolean expectedToFail(File file) {
    String fullPath = file.getAbsolutePath();
    for (String relativePath : FAILING_TESTS) {
      if (fullPath.endsWith(relativePath)) {
        return true;
      }
    }
    return false;
  }

  private long fileCount = 0L;

  private long totalTime = 0L;

  @Override
  protected void addTestForFile(TestSuite suite, File file) {
    if (FILE_NAME_PATTERN.matcher(file.getName()).matches()) {
      super.addTestForFile(suite, file);
    }
  }

  @Override
  protected void testSingleFile(File sourceFile) throws Exception {
    //
    // Determine whether the test is expected to pass or fail.
    //
    String contents = FileUtilities.getContents(sourceFile);
    boolean errorExpected = contents.indexOf("@compile-error") > 0
        || contents.indexOf("@static-warning") > 0;
    // Uncomment the lines below to stop reporting failures for files that are expected to contain
    // errors.
//    if (errorExpected) {
//      return;
//    }
    //
    // Create the analysis context in which the file will be analyzed.
    //
    DartSdk sdk = DartSdk.getDefaultSdk();
    SourceFactory sourceFactory = new SourceFactory(new DartUriResolver(sdk), new FileUriResolver());
    AnalysisContext context = AnalysisEngine.getInstance().createAnalysisContext();
    context.setSourceFactory(sourceFactory);
    //
    // Analyze the file.
    //
    Source source = new FileBasedSource(sourceFactory, sourceFile);
    long startTime = System.currentTimeMillis();
    LibraryElement library = context.getLibraryElement(source);
    long endTime = System.currentTimeMillis();
    if (library == null) {
      Assert.fail("Could not analyze " + sourceFile.getAbsolutePath());
    }
    //
    // Gather statistics.
    //
    fileCount++;
    totalTime += (endTime - startTime);
    //
    // Validate the results.
    //
    ArrayList<AnalysisError> errorList = new ArrayList<AnalysisError>();
    addErrors(errorList, library.getDefiningCompilationUnit());
    for (CompilationUnitElement part : library.getParts()) {
      addErrors(errorList, part);
    }
    assertErrors(errorExpected, expectedToFail(sourceFile), errorList);
  }
}

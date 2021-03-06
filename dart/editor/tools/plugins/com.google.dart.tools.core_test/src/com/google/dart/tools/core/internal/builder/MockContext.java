package com.google.dart.tools.core.internal.builder;

import com.google.dart.engine.ast.CompilationUnit;
import com.google.dart.engine.context.AnalysisContext;
import com.google.dart.engine.context.AnalysisException;
import com.google.dart.engine.context.ChangeNotice;
import com.google.dart.engine.context.ChangeSet;
import com.google.dart.engine.element.Element;
import com.google.dart.engine.element.ElementLocation;
import com.google.dart.engine.element.HtmlElement;
import com.google.dart.engine.element.LibraryElement;
import com.google.dart.engine.error.AnalysisError;
import com.google.dart.engine.html.parser.HtmlParseResult;
import com.google.dart.engine.source.DirectoryBasedSourceContainer;
import com.google.dart.engine.source.FileBasedSource;
import com.google.dart.engine.source.Source;
import com.google.dart.engine.source.SourceContainer;
import com.google.dart.engine.source.SourceFactory;
import com.google.dart.engine.source.SourceKind;
import com.google.dart.engine.utilities.general.ObjectUtilities;
import com.google.dart.engine.utilities.io.PrintStringWriter;
import com.google.dart.tools.core.CallList;
import com.google.dart.tools.core.CallList.Call;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Mock {@link AnalysisContext} that validates calls and returns Mocks rather than performing the
 * requested analysis.
 */
public class MockContext implements AnalysisContext {

  private final class ChangedCall extends Call {
    private ChangedCall(AnalysisContext target, ChangeSet expected) {
      super(target, CHANGED, expected);
    }

    protected boolean equalArgument(List<?> list1, List<?> list2) {
      ArrayList<Object> copy = new ArrayList<Object>(list2);
      for (Object object : list1) {
        if (!copy.remove(object)) {
          return false;
        }
      }
      return copy.isEmpty();
    }

    @Override
    protected boolean equalArguments(Object[] otherArgs) {
      if (otherArgs.length != 1 || !(otherArgs[0] instanceof ChangeSet)) {
        return false;
      }
      ChangeSet changes = (ChangeSet) args[0];
      ChangeSet otherChanges = (ChangeSet) otherArgs[0];
      return equalArgument(changes.getAddedWithContent(), otherChanges.getAddedWithContent())
          && equalArgument(changes.getChangedWithContent(), otherChanges.getChangedWithContent())
          && equalArgument(changes.getRemoved(), otherChanges.getRemoved())
          && equalArgument(changes.getRemovedContainers(), otherChanges.getRemovedContainers());
    }

    @Override
    protected void printArguments(PrintStringWriter writer, String indent, Object[] args) {
      ChangeSet changes = (ChangeSet) args[0];
      writer.print(indent);
      writer.println("ChangeSet");
      printCollection(writer, indent, "added", changes.getAddedWithContent().keySet());
      printCollection(writer, indent, "changed", changes.getChangedWithContent().keySet());
      printCollection(writer, indent, "removed", changes.getRemoved());
      printCollection(writer, indent, "removedContainers", changes.getRemovedContainers());
    }

    protected void printCollection(PrintStringWriter writer, String indent, String name,
        Collection<?> collection) {
      writer.print(indent);
      writer.print("    ");
      writer.print(name);
      writer.println(": ");
      for (Object object : collection) {
        writer.print(indent);
        writer.print("        ");
        writer.println(object != null ? object.toString() : "null");
      }
    }

    private boolean equalArgument(Map<Source, String> firstMap, Map<Source, String> secondMap) {
      ArrayList<Object> copy = new ArrayList<Object>(secondMap.keySet());
      for (Map.Entry<Source, String> entry : firstMap.entrySet()) {
        Source key = entry.getKey();
        if (!copy.remove(key) || !ObjectUtilities.equals(entry.getValue(), secondMap.get(key))) {
          return false;
        }
      }
      return copy.isEmpty();
    }
  }

  private static final String CHANGED = "changed";
  private static final String CLEAR_RESOLUTION = "clearResolution";
  private static final String DISCARDED = "discarded";
  private static final String EXTRACT_CONTEXT = "extractContext";
  private static final String MERGE_CONTEXT = "mergeContext";
  private static final String SOURCE_CHANGED = "sourceChanged";
  private static final String SOURCE_DELETED = "sourceDeleted";

  private final CallList calls = new CallList();
  private SourceFactory factory;

  @Override
  public void applyChanges(ChangeSet changes) {
    calls.add(new ChangedCall(this, changes));
  }

  public void assertChanged(IResource[] added, IResource[] changed, IResource[] removed) {
    final ChangeSet expected = new ChangeSet();
    if (added != null) {
      for (IResource file : added) {
        expected.added(new FileBasedSource(factory, file.getLocation().toFile()));
      }
    }
    if (changed != null) {
      for (IResource file : changed) {
        expected.changed(new FileBasedSource(factory, file.getLocation().toFile()));
      }
    }
    if (removed != null) {
      for (IResource resource : removed) {
        if (resource instanceof IFolder) {
          expected.removedContainer(new DirectoryBasedSourceContainer(
              resource.getLocation().toFile()));
        } else {
          expected.removed(new FileBasedSource(factory, resource.getLocation().toFile()));
        }
      }
    }
    calls.assertCall(new ChangedCall(this, expected));
  }

  public void assertClearResolution(boolean expected) {
    calls.assertExpectedCall(expected, this, CLEAR_RESOLUTION);
  }

  public void assertDiscarded(boolean expected) {
    calls.assertExpectedCall(expected, this, DISCARDED);
  }

  public void assertExtracted(IContainer expectedContainer) {
    if (expectedContainer != null) {
      calls.assertCall(this, EXTRACT_CONTEXT, new DirectoryBasedSourceContainer(
          expectedContainer.getLocation().toFile()));
    } else {
      calls.assertNoCall(new Call(this, EXTRACT_CONTEXT) {
        @Override
        protected boolean equalArguments(Object[] otherArgs) {
          return true;
        }
      });
    }
  }

  public void assertMergedContext(AnalysisContext expectedContext) {
    if (expectedContext != null) {
      calls.assertCall(this, MERGE_CONTEXT, expectedContext);
    } else {
      calls.assertNoCall(new Call(this, MERGE_CONTEXT) {
        @Override
        protected boolean equalArguments(Object[] otherArgs) {
          return true;
        }
      });
    }
  }

  public void assertNoCalls() {
    calls.assertNoCalls();
  }

  public void assertSourcesChanged(IResource... expected) {
    for (IResource resource : expected) {
      Source source = new FileBasedSource(factory, resource.getLocation().toFile());
      calls.assertCall(this, SOURCE_CHANGED, source);
    }
  }

  public void assertSourcesDeleted(IResource... expected) {
    for (IResource resource : expected) {
      File file = resource.getLocation().toFile();
      if (resource.getType() == IResource.FILE) {
        Source source = new FileBasedSource(factory, file);
        calls.assertCall(this, SOURCE_DELETED, source);
      } else {
        SourceContainer sourceContainer = new DirectoryBasedSourceContainer(file);
        calls.assertCall(this, SOURCE_DELETED, sourceContainer);
      }
    }
  }

  @Override
  public void clearResolution() {
    calls.add(this, CLEAR_RESOLUTION);
  }

  @Override
  public void discard() {
    calls.add(this, DISCARDED);
  }

  @Override
  public AnalysisContext extractContext(SourceContainer container) {
    calls.add(this, EXTRACT_CONTEXT, container);
    return new MockContext();
  }

  @Override
  public Element getElement(ElementLocation location) {
    return null;
  }

  @Override
  public AnalysisError[] getErrors(Source source) throws AnalysisException {
    return null;
  }

  @Override
  public HtmlElement getHtmlElement(Source source) {
    return null;
  }

  @Override
  public Source[] getHtmlSources() {
    return Source.EMPTY_ARRAY;
  }

  @Override
  public SourceKind getKnownKindOf(Source source) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Source[] getLaunchableClientLibrarySources() {
    return Source.EMPTY_ARRAY;
  }

  @Override
  public Source[] getLaunchableServerLibrarySources() {
    return Source.EMPTY_ARRAY;
  }

  @Override
  public Source[] getLibrariesContaining(Source source) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LibraryElement getLibraryElement(Source source) {
    return null;
  }

  @Override
  public LibraryElement getLibraryElementOrNull(Source source) {
    return null;
  }

  @Override
  public Source[] getLibrarySources() {
    return Source.EMPTY_ARRAY;
  }

  @Override
  public SourceKind getOrComputeKindOf(Source source) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SourceFactory getSourceFactory() {
    return factory;
  }

  @Override
  public void mergeContext(AnalysisContext context) {
    calls.add(this, MERGE_CONTEXT, context);
  }

  @Override
  public CompilationUnit parse(Source source) throws AnalysisException {
    return null;
  }

  @Override
  public HtmlParseResult parseHtml(Source source) throws AnalysisException {
    throw new UnsupportedOperationException();
  }

  @Override
  public ChangeNotice[] performAnalysisTask() {
    return ChangeNotice.EMPTY_ARRAY;
  }

  @Override
  public CompilationUnit resolve(Source source, LibraryElement library) throws AnalysisException {
    return null;
  }

  @Override
  public void setSourceFactory(SourceFactory sourceFactory) {
    factory = sourceFactory;
  }

  @Override
  public Iterable<Source> sourcesToResolve(Source[] changedSources) {
    throw new UnsupportedOperationException();
  }
}

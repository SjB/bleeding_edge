/*
 * Copyright (c) 2013, the Dart project authors.
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
package com.google.dart.engine.services.completion;

import com.google.dart.engine.ast.ASTNode;
import com.google.dart.engine.ast.ArgumentList;
import com.google.dart.engine.ast.AssignmentExpression;
import com.google.dart.engine.ast.Block;
import com.google.dart.engine.ast.BooleanLiteral;
import com.google.dart.engine.ast.ClassDeclaration;
import com.google.dart.engine.ast.ClassTypeAlias;
import com.google.dart.engine.ast.CompilationUnit;
import com.google.dart.engine.ast.ConstructorDeclaration;
import com.google.dart.engine.ast.ConstructorFieldInitializer;
import com.google.dart.engine.ast.ConstructorName;
import com.google.dart.engine.ast.Declaration;
import com.google.dart.engine.ast.EphemeralIdentifier;
import com.google.dart.engine.ast.Expression;
import com.google.dart.engine.ast.ExtendsClause;
import com.google.dart.engine.ast.FieldFormalParameter;
import com.google.dart.engine.ast.FormalParameter;
import com.google.dart.engine.ast.FormalParameterList;
import com.google.dart.engine.ast.FunctionTypeAlias;
import com.google.dart.engine.ast.Identifier;
import com.google.dart.engine.ast.ImplementsClause;
import com.google.dart.engine.ast.InstanceCreationExpression;
import com.google.dart.engine.ast.IsExpression;
import com.google.dart.engine.ast.MethodDeclaration;
import com.google.dart.engine.ast.MethodInvocation;
import com.google.dart.engine.ast.NodeList;
import com.google.dart.engine.ast.PrefixedIdentifier;
import com.google.dart.engine.ast.PropertyAccess;
import com.google.dart.engine.ast.RedirectingConstructorInvocation;
import com.google.dart.engine.ast.SimpleFormalParameter;
import com.google.dart.engine.ast.SimpleIdentifier;
import com.google.dart.engine.ast.Statement;
import com.google.dart.engine.ast.TypeName;
import com.google.dart.engine.ast.TypeParameter;
import com.google.dart.engine.ast.TypeParameterList;
import com.google.dart.engine.ast.VariableDeclaration;
import com.google.dart.engine.ast.VariableDeclarationList;
import com.google.dart.engine.ast.WithClause;
import com.google.dart.engine.ast.visitor.GeneralizingASTVisitor;
import com.google.dart.engine.context.AnalysisContext;
import com.google.dart.engine.element.ClassElement;
import com.google.dart.engine.element.ConstructorElement;
import com.google.dart.engine.element.Element;
import com.google.dart.engine.element.ExecutableElement;
import com.google.dart.engine.element.FieldElement;
import com.google.dart.engine.element.FunctionTypeAliasElement;
import com.google.dart.engine.element.ImportElement;
import com.google.dart.engine.element.LibraryElement;
import com.google.dart.engine.element.LocalVariableElement;
import com.google.dart.engine.element.ParameterElement;
import com.google.dart.engine.element.PrefixElement;
import com.google.dart.engine.element.TypeVariableElement;
import com.google.dart.engine.element.VariableElement;
import com.google.dart.engine.internal.element.DynamicElementImpl;
import com.google.dart.engine.internal.resolver.TypeProvider;
import com.google.dart.engine.internal.resolver.TypeProviderImpl;
import com.google.dart.engine.internal.type.DynamicTypeImpl;
import com.google.dart.engine.scanner.Token;
import com.google.dart.engine.sdk.DartSdk;
import com.google.dart.engine.search.SearchEngine;
import com.google.dart.engine.search.SearchListener;
import com.google.dart.engine.search.SearchMatch;
import com.google.dart.engine.search.SearchPattern;
import com.google.dart.engine.search.SearchPatternFactory;
import com.google.dart.engine.search.SearchScope;
import com.google.dart.engine.search.SearchScopeFactory;
import com.google.dart.engine.services.assist.AssistContext;
import com.google.dart.engine.source.Source;
import com.google.dart.engine.type.FunctionType;
import com.google.dart.engine.type.InterfaceType;
import com.google.dart.engine.type.Type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The analysis engine for code completion.
 * <p>
 * Note: During development package-private methods are used to group element-specific completion
 * utilities.
 * 
 * @coverage com.google.dart.engine.services.completion
 */
public class CompletionEngine {

  class NameCollector {
    private Map<String, List<Element>> uniqueNames = new HashMap<String, List<Element>>();

    void addNamesDefinedByExecutable(ExecutableElement execElement) {
      mergeNames(execElement.getParameters());
      mergeNames(execElement.getLocalVariables());
    }

    void addNamesDefinedByType(InterfaceType type) {
      mergeNames(type.getElement().getAccessors());
      mergeNames(type.getElement().getMethods());
      mergeNames(type.getElement().getTypeVariables());
      if (!state.areOperatorsAllowed) {
        for (ExecutableElement mth : type.getElement().getMethods()) {
          // TODO Identify operators...
        }
      }
    }

    void addNamesDefinedByTypes(InterfaceType[] types) {
      for (InterfaceType type : types) {
        addNamesDefinedByType(type);
      }
    }

    void addTopLevelNames() {
      mergeNames(findAllTypes());
      mergeNames(findAllVariables());
      mergeNames(findAllFunctions());
    }

    Collection<List<Element>> getNames() {
      return uniqueNames.values();
    }

    void remove(Element element) {
      String name = element.getName();
      List<Element> list = uniqueNames.get(name);
      if (list == null) {
        return;
      }
      list.remove(element);
      if (list.isEmpty()) {
        uniqueNames.remove(name);
      }
    }

    private void mergeNames(Element[] elements) {
      for (Element element : elements) {
        String name = element.getName();
        List<Element> dups = uniqueNames.get(name);
        if (dups == null) {
          dups = new ArrayList<Element>();
          uniqueNames.put(name, dups);
        }
        dups.add(element);
      }
    }
  }

  static class SearchCollector implements SearchListener {
    ArrayList<Element> results = new ArrayList<Element>();
    boolean isComplete = false;

    @Override
    public void matchFound(SearchMatch match) {
      Element element = match.getElement();
      results.add(element);
    }

    @Override
    public void searchComplete() {
      isComplete = true;
    }
  }

  private class Filter {
    String prefix;
    boolean isPrivateDisallowed = true;

    Filter(SimpleIdentifier ident) {
      int loc = context.getSelectionOffset();
      int pos = ident.getOffset();
      int len = loc - pos;
      if (len > 0) {
        String name = ident.getName();
        if (len <= name.length()) {
          prefix = name.substring(0, len);
        } else {
          prefix = "";
        }
      } else {
        prefix = "";
      }
      if (prefix.length() >= 1) {
        isPrivateDisallowed = !Identifier.isPrivateName(prefix);
      }
    }

    boolean match(Element elem) {
      return match(elem.getName());
    }

    boolean match(String name) {
      // Return true if the filter passes. Return false for private elements that should not be visible
      // in the current context, or for library elements that are not accessible in the context (NYI).
      if (isPrivateDisallowed) {
        if (name.length() > 0 && Identifier.isPrivateName(name)) {
          return false;
        }
      }
      return name.startsWith(prefix);
    }
  }

  private class Ident extends EphemeralIdentifier {
    private String name;

    Ident(ASTNode parent) {
      super(parent, completionLocation());
    }

    Ident(ASTNode parent, Token name) {
      super(parent, name.getOffset());
      this.name = name.getLexeme();
    }

    @Override
    public String getName() {
      return name == null ? super.getName() : name;
    }
  }

  private class IdentifierCompleter extends GeneralizingASTVisitor<Void> {
    SimpleIdentifier completionNode;

    IdentifierCompleter(SimpleIdentifier node) {
      completionNode = node;
    }

    @Override
    public Void visitArgumentList(ArgumentList node) {
      if (completionNode instanceof SimpleIdentifier) {
        analyzeLocalName(completionNode);
      }
      return null;
    }

    @Override
    public Void visitAssignmentExpression(AssignmentExpression node) {
      if (completionNode instanceof SimpleIdentifier) {
        analyzeLocalName(completionNode);
      }
      return null;
    }

    @Override
    public Void visitConstructorFieldInitializer(ConstructorFieldInitializer node) {
      // { A() : this.!x = 1; }
      if (node.getFieldName() == completionNode) {
        ClassElement classElement = ((ConstructorDeclaration) node.getParent()).getElement().getEnclosingElement();
        fieldReference(classElement, node.getFieldName());
      }
      return null;
    }

    @Override
    public Void visitConstructorName(ConstructorName node) {
      if (node.getName() == completionNode) {
        // { new A.!c(); }
        TypeName typeName = node.getType();
        if (typeName != null) {
          Type type = typeName.getType();
          Element typeElement = type.getElement();
          if (typeElement instanceof ClassElement) {
            ClassElement classElement = (ClassElement) typeElement;
            constructorReference(classElement, node.getName());
          }
        }
      }
      return null;
    }

    @Override
    public Void visitFieldFormalParameter(FieldFormalParameter node) {
      if (completionNode == node.getIdentifier()) {
        analyzeImmediateField(node.getIdentifier());
      }
      return null;
    }

    @Override
    public Void visitFunctionTypeAlias(FunctionTypeAlias node) {
      if (node.getName() == completionNode) {
        if (node.getReturnType() == null) {
          // This may be an incomplete class type alias
          state.includesUndefinedTypes();
          analyzeTypeName(node.getName(), typeDeclarationName(node));
        }
      }
      return null;
    }

    @Override
    public Void visitMethodDeclaration(MethodDeclaration node) {
      if (completionNode == node.getName()) {
        if (node.getReturnType() == null) {
          // class Foo {const F!(); }
          analyzeLocalName(completionNode); // TODO: This is too general; need to restrict to types when following const
        }
      }
      return null;
    }

    @Override
    public Void visitMethodInvocation(MethodInvocation node) {
      if (node.getMethodName() == completionNode) {
        // { x.!y() }
        Expression expr = node.getTarget();
        Type receiverType;
        if (expr == null) { // use this
          receiverType = typeOfContainingClass(node);
          analyzeDirectAccess(receiverType, node.getMethodName());
        } else {
          receiverType = typeOf(expr);
          analyzePrefixedAccess(receiverType, node.getMethodName());
        }
      } else if (node.getTarget() == completionNode) {
        // { x!.y() } -- only reached when node.getTarget() is a simple identifier. (TODO: verify)
        if (completionNode instanceof SimpleIdentifier) {
          SimpleIdentifier ident = completionNode;
          analyzeReceiver(ident);
        }
      }
      return null;
    }

    @Override
    public Void visitPrefixedIdentifier(PrefixedIdentifier node) {
      if (node.getPrefix() == completionNode) {
        // { x!.y }
//        state.sourceDeclarationIsStatic(isDefinedInStaticMethod(node));
        analyzeLocalName(node.getPrefix());
      } else {
        // { v.! }
        SimpleIdentifier receiverName = node.getPrefix();
        Element receiver = receiverName.getElement();
        if (receiver == null) {
          return null;
        }
        switch (receiver.getKind()) {
          case PREFIX: {
            // TODO: remove this case if/when prefix resolution changes
            PrefixElement prefixElement = (PrefixElement) receiver;
            // Complete lib_prefix.name
            prefixedAccess(prefixElement, node.getIdentifier());
            break;
          }
          case IMPORT: {
            ImportElement importElement = (ImportElement) receiver;
            // Complete lib_prefix.name
            prefixedAccess(importElement, node.getIdentifier());
            break;
          }
          default: {
            Type receiverType = typeOf(receiver);
            analyzePrefixedAccess(receiverType, node.getIdentifier());
            break;
          }
        }
      }
      return null;
    }

    @Override
    public Void visitPropertyAccess(PropertyAccess node) {
      // { o.!hashCode }
      if (node.getPropertyName() == completionNode) {
        Type receiverType = typeOf(node.getRealTarget());
        analyzePrefixedAccess(receiverType, node.getPropertyName());
      }
      return null;
    }

    @Override
    public Void visitRedirectingConstructorInvocation(RedirectingConstructorInvocation node) {
      // { A.Fac() : this.!b(); }
      if (node.getConstructorName() == completionNode) {
        ClassElement classElement = node.getElement().getEnclosingElement();
        constructorReference(classElement, node.getConstructorName());
      }
      return null;
    }

    @Override
    public Void visitSimpleFormalParameter(SimpleFormalParameter node) {
      if (node.getIdentifier() == completionNode) {
        if (node.getKeyword() == null && node.getType() == null) {
          Ident ident = new Ident(node);
          analyzeTypeName(node.getIdentifier(), ident);
          return null;
        }
      }
      if (node.getKeyword() != null && isCompletionBefore(node.getKeyword().getEnd())) {
        final Token token = node.getKeyword();
        Ident ident = new Ident(node, token);
        analyzeTypeName(ident, ident);
      }
      return null;
    }

    @Override
    public Void visitTypeName(TypeName node) {
      ASTNode parent = node.getParent();
      if (parent != null) {
        TypeNameCompleter visitor = new TypeNameCompleter(completionNode, node);
        return parent.accept(visitor);
      }
      return null;
    }

    @Override
    public Void visitTypeParameter(TypeParameter node) {
      // { X<!Y> }
      if (isCompletionBetween(node.getOffset(), node.getEnd())) {
        analyzeTypeName(completionNode, typeDeclarationName(node));
      }
      return null;
    }

    @Override
    public Void visitVariableDeclaration(VariableDeclaration node) {
      if (node.getName() == completionNode) {
        analyzeDeclarationName(node);
        return null;
      } else if (node.getInitializer() == completionNode) {
        analyzeLocalName((SimpleIdentifier) node.getInitializer());
      }
      return null;
    }
  }

  private class TerminalNodeCompleter extends GeneralizingASTVisitor<Void> {

    @Override
    public Void visitArgumentList(ArgumentList node) {
      if (node.getArguments().isEmpty()) {
        analyzeLocalName(new Ident(node));
      }
      return null;
    }

    @Override
    public Void visitBlock(Block node) {
      if (isCompletionBetween(node.getLeftBracket().getEnd(), node.getRightBracket().getOffset())) {
        // { {! stmt; !} }
        analyzeLocalName(new Ident(node));
        return null;
      }
      return null;
    }

    @Override
    public Void visitBooleanLiteral(BooleanLiteral node) {
      analyzeLiteralReference(node);
      return null;
    }

    @Override
    public Void visitClassDeclaration(ClassDeclaration node) {
      if (isCompletingKeyword(node.getClassKeyword())) {
        pKeyword(node.getClassKeyword()); // Other keywords are legal but not handled here.
        return null;
      }
      if (isCompletingKeyword(node.getAbstractKeyword())) {
        pKeyword(node.getAbstractKeyword());
        return null;
      }
      if (!node.getLeftBracket().isSynthetic()) {
        if (isCompletionAfter(node.getLeftBracket().getEnd())) {
          if (node.getRightBracket().isSynthetic()
              || isCompletionBefore(node.getRightBracket().getOffset())) {
            analyzeLocalName(new Ident(node));
            return null;
          }
        }
      }
      // TODO { abstract ! class ! A ! extends B implements C, D ! {}}
      return null; // visitCompilationUnitMember(node);
    }

    @Override
    public Void visitClassTypeAlias(ClassTypeAlias node) {
      if (isCompletingKeyword(node.getKeyword())) {
        pKeyword(node.getKeyword());
        return null;
      }
      // TODO { typedef ! A ! = ! B ! with C, D !; }
      return null; // TODO visitTypeAlias(node);
    }

    @Override
    public Void visitCompilationUnit(CompilationUnit node) {
      // This is not a good terminal node...
      return null;
    }

    @Override
    public Void visitExpression(Expression node) {
      analyzeLocalName(new Ident(node));
      return null;
    }

    @Override
    public Void visitExtendsClause(ExtendsClause node) {
      if (isCompletingKeyword(node.getKeyword())) {
        pKeyword(node.getKeyword());
        return null;
      } else if (node.getSuperclass() == null) {
        // { X extends ! }
        analyzeTypeName(new Ident(node), typeDeclarationName(node));
        return null;
      } else {
        // { X extends ! Y }
        analyzeTypeName(new Ident(node), typeDeclarationName(node));
        return null;
      }
    }

    @Override
    public Void visitFormalParameter(FormalParameter node) {
      return super.visitFormalParameter(node);
    }

    @Override
    public Void visitFormalParameterList(FormalParameterList node) {
      if (isCompletionBetween(
          node.getLeftParenthesis().getEnd(),
          node.getRightParenthesis().getOffset())) {
        NodeList<FormalParameter> params = node.getParameters();
        if (!params.isEmpty()) {
          FormalParameter last = params.get(params.size() - 1);
          if (isCompletionBetween(last.getEnd(), node.getRightParenthesis().getOffset())) {
            List<FormalParameter> newParams = copyWithout(params, last);
            analyzeNewParameterName(newParams, last.getIdentifier(), null);
            return null;
          } else {
            Ident ident = new Ident(node);
            analyzeTypeName(ident, ident);
            return null;
          }
        } else {
          Ident ident = new Ident(node);
          analyzeTypeName(ident, ident);
          return null;
        }
      }
      return null;
    }

    @Override
    public Void visitFunctionTypeAlias(FunctionTypeAlias node) {
      if (isCompletingKeyword(node.getKeyword())) {
        pKeyword(node.getKeyword());
        return null;
      }
      return null;
    }

    @Override
    public Void visitImplementsClause(ImplementsClause node) {
      if (isCompletingKeyword(node.getKeyword())) {
        pKeyword(node.getKeyword());
        return null;
      } else if (node.getInterfaces().isEmpty()) {
        // { X implements ! }
        analyzeTypeName(new Ident(node), typeDeclarationName(node));
        return null;
      } else {
        // { X implements ! Y }
        analyzeTypeName(new Ident(node), typeDeclarationName(node));
        return null;
      }
    }

    @Override
    public Void visitInstanceCreationExpression(InstanceCreationExpression node) {
      if (isCompletingKeyword(node.getKeyword())) {
        pKeyword(node.getKeyword());
        Ident ident = new Ident(node, node.getKeyword());
        analyzeLocalName(ident);
        return null;
      } else {
        Ident ident = new Ident(node);
        analyzeTypeName(ident, ident);
        return null;
      }
    }

    @Override
    public Void visitMethodInvocation(MethodInvocation node) {
      Token period = node.getPeriod();
      if (period != null && isCompletionAfter(period.getEnd())) {
        // { x.!y() }
        Expression expr = node.getTarget();
        Type receiverType = typeOf(expr);
        analyzePrefixedAccess(receiverType, node.getMethodName());
      }
      return null;
    }

    @Override
    public Void visitSimpleIdentifier(SimpleIdentifier node) {
      ASTNode parent = node.getParent();
      if (parent != null) {
        IdentifierCompleter visitor = new IdentifierCompleter(node);
        return parent.accept(visitor);
      }
      return null;
    }

    @Override
    public Void visitTypeParameter(TypeParameter node) {
      if (isCompletingKeyword(node.getKeyword())) {
        pKeyword(node.getKeyword());
        return null;
      } else if (node.getName().getName().isEmpty()
          && isCompletionBefore(node.getKeyword().getOffset())) {
        // { < ! extends X>
        analyzeTypeName(node.getName(), typeDeclarationName(node));
        return null;
      }
      // { <! X ! extends ! Y !>
      return null;
    }

    @Override
    public Void visitTypeParameterList(TypeParameterList node) {
      // { <X extends A,! B,! >
      if (isCompletionBetween(node.getLeftBracket().getEnd(), node.getRightBracket().getOffset())) {
        analyzeTypeName(new Ident(node), typeDeclarationName(node));
        return null;
      }
      return null;
    }

    @Override
    public Void visitWithClause(WithClause node) {
      if (isCompletingKeyword(node.getWithKeyword())) {
        pKeyword(node.getWithKeyword());
        return null;
      } else if (node.getMixinTypes().isEmpty()) {
        // { X with ! }
//        state.mustBeMixin();
        analyzeTypeName(new Ident(node), typeDeclarationName(node));
        return null;
      } else {
        // { X with ! Y }
//        state.mustBeMixin();
        analyzeTypeName(new Ident(node), typeDeclarationName(node));
        return null;
      }
    }
  }

  private class TypeNameCompleter extends GeneralizingASTVisitor<Void> {
    SimpleIdentifier identifier;
    TypeName typeName;

    TypeNameCompleter(SimpleIdentifier identifier, TypeName typeName) {
      this.identifier = identifier;
      this.typeName = typeName;
    }

    @Override
    public Void visitClassTypeAlias(ClassTypeAlias node) {
      analyzeTypeName(identifier, typeDeclarationName(node));
      return null;
    }

    @Override
    public Void visitConstructorName(ConstructorName node) {
      if (typeName == node.getType()) {
        // { new ! } { new Na!me(); }
        analyzeTypeName(identifier, null);
      }
      return null;
    }

    @Override
    public Void visitExtendsClause(ExtendsClause node) {
      analyzeTypeName(identifier, typeDeclarationName(node));
      return null;
    }

    @Override
    public Void visitFunctionTypeAlias(FunctionTypeAlias node) {
//      state.includesUndefinedTypes();
      analyzeTypeName(identifier, typeDeclarationName(node));
      return null;
    }

    @Override
    public Void visitImplementsClause(ImplementsClause node) {
      analyzeTypeName(identifier, typeDeclarationName(node));
      return null;
    }

    @Override
    public Void visitIsExpression(IsExpression node) {
      if (typeName == node.getType()) {
        // TODO Confirm that this path always has simple identifiers
        analyzeTypeName((SimpleIdentifier) node.getType().getName(), null);
      }
      return null;
    }

    @Override
    public Void visitSimpleFormalParameter(SimpleFormalParameter node) {
//      state.includesUndefinedTypes();
      analyzeTypeName(identifier, null);
      return null;
    }

    @Override
    public Void visitTypeParameter(TypeParameter node) {
      if (node.getBound() == typeName) {
        // { X<A extends !Y> }
        analyzeTypeName(identifier, typeDeclarationName(node));
      }
      return null;
    }

    @Override
    public Void visitVariableDeclarationList(VariableDeclarationList node) {
//      state.includesUndefinedTypes();
      if (node.getParent() instanceof Statement) {
        analyzeLocalName(identifier);
      } else {
        analyzeTypeName(identifier, null);
      }
      return null;
    }

    @Override
    public Void visitWithClause(WithClause node) {
//      state.mustBeMixin();
      analyzeTypeName(identifier, typeDeclarationName(node));
      return null;
    }

  }

  // Review note: It may look like literals (pNull, etc) are coded redundantly, but that's because
  // all the code hasn't been written yet.
  private static final String C_DYNAMIC = "dynamic";
  private static final String C_FALSE = "false";
  private static final String C_NULL = "null";
  private static final String C_PARAMNAME = "arg";
  private static final String C_TRUE = "true";
  private static final String C_VAR = "var";
  private static final String C_VOID = "void";

  private CompletionRequestor requestor;
  private CompletionFactory factory;
  private AssistContext context;
  private Filter filter;
  private CompletionState state;

  public CompletionEngine(CompletionRequestor requestor, CompletionFactory factory) {
    this.requestor = requestor;
    this.factory = factory;
    this.state = new CompletionState();
  }

  /**
   * Analyze the source unit in the given context to determine completion proposals at the selection
   * offset of the context.
   * 
   * @throws Exception
   */
  public void complete(AssistContext context) {
    this.context = context;
    requestor.beginReporting();
    ASTNode completionNode = context.getCoveredNode();
    if (completionNode != null) {
      state.setContext(completionNode);
      TerminalNodeCompleter visitor = new TerminalNodeCompleter();
      completionNode.accept(visitor);
    }
    requestor.endReporting();
  }

  void analyzeDeclarationName(VariableDeclaration varDecl) {
    // We might want to propose multiple names for a declaration based on types someday.
    // For now, just use whatever is already there.
    SimpleIdentifier identifier = varDecl.getName();
    filter = new Filter(identifier);
    TypeName type = ((VariableDeclarationList) varDecl.getParent()).getType();
    if (identifier.getLength() > 0) {
      pName(identifier);
    }
    if (type != null) {
      pParamName(type.getName().getName().toLowerCase());
    }
  }

  void analyzeDirectAccess(Type receiverType, SimpleIdentifier completionNode) {
    if (receiverType != null) {
      // Complete this.!y where this is absent
      Element rcvrTypeElem = receiverType.getElement();
      if (rcvrTypeElem.equals(DynamicElementImpl.getInstance())) {
        rcvrTypeElem = getObjectClassElement();
      }
      if (rcvrTypeElem instanceof ClassElement) {
        directAccess((ClassElement) rcvrTypeElem, completionNode);
      }
    }
  }

  void analyzeImmediateField(SimpleIdentifier fieldName) {
    filter = new Filter(fieldName);
    ClassDeclaration classDecl = fieldName.getAncestor(ClassDeclaration.class);
    ClassElement classElement = classDecl.getElement();
    for (FieldElement field : classElement.getFields()) {
      pName(field.getName(), ProposalKind.FIELD);
    }
  }

  void analyzeLiteralReference(BooleanLiteral literal) {
//    state.setContext(literal);
    Ident ident = new Ident(literal.getParent());
    ident.setToken(literal.getLiteral());
    filter = new Filter(ident);
    analyzeLocalName(ident);
  }

  void analyzeLocalName(SimpleIdentifier identifier) {
    // Completion x!
    filter = new Filter(identifier);
    Collection<List<Element>> uniqueNames = collectIdentifiersVisibleAt(identifier);
    for (List<Element> uniques : uniqueNames) {
      Element candidate = uniques.get(0);
      if (state.isSourceDeclarationStatic) {
        if (candidate instanceof FieldElement) {
          if (!((FieldElement) candidate).isStatic()) {
            continue;
          }
        }
      }
      pName(candidate);
    }
    if (state.areLiteralsAllowed) {
      pNull();
      pTrue();
      pFalse();
    }
  }

  void analyzeNewParameterName(List<FormalParameter> params, SimpleIdentifier typeIdent,
      String identifierName) {
    String typeName = typeIdent.getName();
    filter = new Filter(new Ident(typeIdent));
    List<String> names = new ArrayList<String>(params.size());
    for (FormalParameter node : params) {
      names.add(node.getIdentifier().getName());
    }
    // Find name similar to typeName not in names, ditto for identifierName.
    if (identifierName == null || identifierName.isEmpty()) {
      String candidate = typeName == null || typeName.isEmpty() ? C_PARAMNAME
          : typeName.toLowerCase();
      pParamName(makeNonconflictingName(candidate, names));
    } else {
      pParamName(makeNonconflictingName(identifierName, names));
      if (typeName != null && !typeName.isEmpty()) {
        pParamName(makeNonconflictingName(typeName.toLowerCase(), names));
      }
    }
  }

  void analyzePrefixedAccess(Type receiverType, SimpleIdentifier completionNode) {
    if (receiverType != null) {
      // Complete x.!y
      Element rcvrTypeElem = receiverType.getElement();
      if (rcvrTypeElem.equals(DynamicElementImpl.getInstance())) {
        rcvrTypeElem = getObjectClassElement();
      }
      if (rcvrTypeElem instanceof ClassElement) {
        prefixedAccess((ClassElement) rcvrTypeElem, completionNode);
      }
    }
  }

  void analyzeReceiver(SimpleIdentifier identifier) {
    // Completion x!.y
    filter = new Filter(identifier);
    Collection<List<Element>> uniqueNames = collectIdentifiersVisibleAt(identifier);
    for (List<Element> uniques : uniqueNames) {
      Element candidate = uniques.get(0);
      pName(candidate);
    }
  }

  void analyzeTypeName(SimpleIdentifier identifier, SimpleIdentifier nameIdent) {
    filter = new Filter(identifier);
    String name = nameIdent == null ? "" : nameIdent.getName();
    Element[] types = findAllTypes();
    for (Element type : types) {
      if (state.isForMixin) {
        if (!(type instanceof ClassElement)) {
          continue;
        }
        ClassElement classElement = (ClassElement) type;
        if (!classElement.isValidMixin()) {
          continue;
        }
      }
      if (type.getName().equals(name)) {
        continue;
      }
      pName(type);
    }
    if (!state.isForMixin) {
      ClassDeclaration classDecl = identifier.getAncestor(ClassDeclaration.class);
      if (classDecl != null) {
        ClassElement classElement = classDecl.getElement();
        for (TypeVariableElement var : classElement.getTypeVariables()) {
          pName(var);
        }
      }
    }
    if (state.isDynamicAllowed) {
      pDynamic();
    }
    if (state.isVarAllowed) {
      pVar();
    }
    if (state.isVoidAllowed) {
      pVoid();
    }
  }

  void constructorReference(ClassElement classElement, SimpleIdentifier identifier) {
    // Complete identifier when it refers to a constructor defined in classElement.
    filter = new Filter(identifier);
    for (ConstructorElement cons : classElement.getConstructors()) {
      if (filterAllows(cons)) {
        pExecutable(cons, identifier, classElement);
      }
    }
  }

  void directAccess(ClassElement classElement, SimpleIdentifier identifier) {
    filter = new Filter(identifier);
    NameCollector names = new NameCollector();
    names.addNamesDefinedByTypes(allSuperTypes(classElement));
    names.addNamesDefinedByTypes(allSubtypes(classElement));
    names.addTopLevelNames();
    proposeNames(names, classElement, identifier);
  }

  void fieldReference(ClassElement classElement, SimpleIdentifier identifier) {
    // Complete identifier when it refers to a constructor defined in classElement.
    filter = new Filter(identifier);
    for (FieldElement cons : classElement.getFields()) {
      if (filterAllows(cons)) {
        pField(cons, identifier, classElement);
      }
    }
  }

  void prefixedAccess(ClassElement classElement, SimpleIdentifier identifier) {
    // Complete identifier when it refers to field or method in classElement.
    filter = new Filter(identifier);
    NameCollector names = new NameCollector();
    names.addNamesDefinedByTypes(allSuperTypes(classElement));
    names.addNamesDefinedByTypes(allSubtypes(classElement));
    proposeNames(names, classElement, identifier);
  }

  void prefixedAccess(ImportElement libElement, SimpleIdentifier identifier) {
    // TODO: Complete identifier when it refers to a member defined in the libraryElement.
    filter = new Filter(identifier);
  }

  void prefixedAccess(PrefixElement libElement, SimpleIdentifier identifier) {
    // TODO: Complete identifier when it refers to a member defined in the libraryElement, or remove.
    filter = new Filter(identifier);
  }

  private InterfaceType[] allSubtypes(ClassElement classElement) {
    SearchEngine engine = context.getSearchEngine();
    SearchScope scope = SearchScopeFactory.createUniverseScope();
    List<SearchMatch> matches = engine.searchSubtypes(classElement, scope, null);
    InterfaceType[] subtypes = new InterfaceType[matches.size()];
    int i = 0;
    for (SearchMatch match : matches) {
      Element element = match.getElement();
      if (element instanceof ClassElement) {
        subtypes[i++] = ((ClassElement) element).getType();
      }
    }
    return subtypes;
  }

  private InterfaceType[] allSuperTypes(ClassElement classElement) {
    InterfaceType[] supertypes = classElement.getAllSupertypes();
    InterfaceType[] allTypes = new InterfaceType[supertypes.length + 1];
    allTypes[0] = classElement.getType();
    System.arraycopy(supertypes, 0, allTypes, 1, supertypes.length);
    return allTypes;
  }

  private Collection<List<Element>> collectIdentifiersVisibleAt(ASTNode ident) {
    NameCollector names = new NameCollector();
    Declaration decl = ident.getAncestor(Declaration.class);
    if (decl != null) {
      Element element = decl.getElement();
      if (element == null) {
        decl = decl.getParent().getAncestor(Declaration.class);
        if (decl != null) {
          element = decl.getElement();
        }
      }
      Element localDef = null;
      if (element instanceof LocalVariableElement) {
        decl = decl.getParent().getAncestor(Declaration.class);
        localDef = element;
        element = decl.getElement();
      }
      if (element instanceof ExecutableElement) {
        ExecutableElement execElement = (ExecutableElement) element;
        names.addNamesDefinedByExecutable(execElement);
        VariableElement[] vars = execElement.getLocalVariables();
        for (VariableElement var : vars) {
          // Remove local vars defined after ident.
          if (var.getNameOffset() >= ident.getOffset()) {
            names.remove(var);
          }
          // If ident is part of the initializer for a local var, remove that local var.
          if (localDef != null) {
            names.remove(localDef);
          }
        }
        decl = decl.getParent().getAncestor(Declaration.class);
        if (decl != null) {
          element = decl.getElement();
        }
      }
      if (element instanceof ClassElement) {
        ClassElement classElement = (ClassElement) element;
        names.addNamesDefinedByTypes(allSuperTypes(classElement));
        names.addNamesDefinedByTypes(allSubtypes(classElement));
        decl = decl.getAncestor(Declaration.class);
        if (decl != null) {
          element = decl.getElement();
        }
      }
      names.addTopLevelNames();
    }
    return names.getNames();
  }

  private int completionLocation() {
    return context.getSelectionOffset();
  }

  private int completionTokenOffset() {
    return completionLocation() - filter.prefix.length();
  }

  private <X extends ASTNode> List<FormalParameter> copyWithout(NodeList<X> oldList,
      final ASTNode deletion) {
    final List<FormalParameter> newList = new ArrayList<FormalParameter>(oldList.size() - 1);
    oldList.accept(new GeneralizingASTVisitor<Void>() {
      @Override
      public Void visitNode(ASTNode node) {
        if (node != deletion) {
          newList.add((FormalParameter) node);
        }
        return null;
      }
    });
    return newList;
  }

  private CompletionProposal createProposal(ProposalKind kind) {
    return factory.createCompletionProposal(kind, completionLocation() - filter.prefix.length());
  }

  private Element[] extractElementsFromSearchMatches(List<SearchMatch> matches) {
    Element[] funcs = new Element[matches.size()];
    int i = 0;
    for (SearchMatch match : matches) {
      funcs[i++] = match.getElement();
    }
    return funcs;
  }

  private boolean filterAllows(Element element) {
    return filter.match(element);
  }

  private boolean filterDisallows(Element element) {
    return !filter.match(element);
  }

  private boolean filterDisallows(String name) {
    return !filter.match(name);
  }

  private Element[] findAllFunctions() {
    SearchEngine engine = context.getSearchEngine();
    SearchScope scope = SearchScopeFactory.createUniverseScope();
    SearchPattern pattern = SearchPatternFactory.createWildcardPattern("*", false);
    List<SearchMatch> matches = engine.searchFunctionDeclarations(scope, pattern, null);
    return extractElementsFromSearchMatches(matches);
  }

  private Element[] findAllTypes() {
    SearchEngine engine = context.getSearchEngine();
    SearchScope scope = SearchScopeFactory.createUniverseScope();
    SearchPattern pattern = SearchPatternFactory.createWildcardPattern("*", false);
    List<SearchMatch> matches = engine.searchTypeDeclarations(scope, pattern, null);
    return extractElementsFromSearchMatches(matches);
  }

  private Element[] findAllVariables() {
    SearchEngine engine = context.getSearchEngine();
    SearchScope scope = SearchScopeFactory.createUniverseScope();
    SearchPattern pattern = SearchPatternFactory.createWildcardPattern("*", false);
    List<SearchMatch> matches = engine.searchVariableDeclarations(scope, pattern, null);
    return extractElementsFromSearchMatches(matches);
  }

  private ClassElement getObjectClassElement() {
    return getTypeProvider().getObjectType().getElement();
  }

  private TypeProvider getTypeProvider() {
    AnalysisContext ctxt = context.getCompilationUnit().getElement().getContext();
    Source coreSource = ctxt.getSourceFactory().forUri(DartSdk.DART_CORE);
    LibraryElement coreLibrary = ctxt.getLibraryElement(coreSource);
    TypeProvider provider = new TypeProviderImpl(coreLibrary);
    return provider;
  }

  private boolean isCompletingKeyword(Token keyword) {
    if (keyword == null) {
      return false;
    }
    int completionLoc = context.getSelectionOffset();
    if (completionLoc >= keyword.getOffset() && completionLoc <= keyword.getEnd()) {
      return true;
    }
    return false;
  }

  private boolean isCompletionAfter(int loc) {
    return loc <= completionLocation();
  }

  private boolean isCompletionBefore(int loc) {
    return completionLocation() <= loc;
  }

  private boolean isCompletionBetween(int firstLoc, int secondLoc) {
    return isCompletionAfter(firstLoc) && isCompletionBefore(secondLoc);
  }

  private String makeNonconflictingName(String candidate, List<String> names) {
    String possibility = candidate;
    int count = 0;
    loop : while (true) {
      String name = count == 0 ? possibility : possibility + count;
      for (String conflict : names) {
        if (name.equals(conflict)) {
          count += 1;
          continue loop;
        }
      }
      return name;
    }
  }

  private void pDynamic() {
    if (filterDisallows(C_DYNAMIC)) {
      return;
    }
    CompletionProposal prop = factory.createCompletionProposal(
        ProposalKind.VARIABLE,
        completionTokenOffset());
    prop.setCompletion(C_DYNAMIC);
    requestor.accept(prop);
  }

  private void pExecutable(ExecutableElement element, SimpleIdentifier identifier,
      ClassElement classElement) {
    // Create a completion proposal for the element: function, method, getter, setter, constructor.
    String name = element.getName();
    if (name.isEmpty() || filterDisallows(element)) {
      return; // Simple constructors are not handled here
    }
    ProposalKind kind = proposalKindOf(element);
    CompletionProposal prop = createProposal(kind);
    setParameterInfo(element, prop);
    prop.setCompletion(name).setReturnType(element.getType().getReturnType().getName());
    Element container = element.getEnclosingElement();
    if (container != null) { // TODO: may be null for functions ??
      prop.setDeclaringType(container.getName());
    }
    requestor.accept(prop);
  }

  private void pFalse() {
    if (filterDisallows(C_FALSE)) {
      return;
    }
    CompletionProposal prop = factory.createCompletionProposal(
        ProposalKind.VARIABLE,
        completionTokenOffset());
    prop.setCompletion(C_FALSE);
    requestor.accept(prop);
  }

  private void pField(FieldElement element, SimpleIdentifier identifier, ClassElement classElement) {
    // Create a completion proposal for the element: field only.
    String name = element.getName();
    if (filterDisallows(element)) {
      return;
    }
    ProposalKind kind = proposalKindOf(element);
    CompletionProposal prop = createProposal(kind);
    prop.setCompletion(name);
    Element container = element.getEnclosingElement();
    if (container != null) { // TODO: never null ??
      prop.setDeclaringType(container.getName());
    }
    requestor.accept(prop);
  }

  private void pKeyword(Token keyword) {
    // This isn't as useful as it might seem. It only works in the case that completion
    // is requested on an existing recognizable keyword.
    CompletionProposal prop = factory.createCompletionProposal( // TODO: Add keyword proposal kind
        ProposalKind.LIBRARY_PREFIX,
        keyword.getOffset());
    prop.setCompletion(keyword.getLexeme());
    requestor.accept(prop);
  }

  private void pName(Element element) {
    // Create a completion proposal for the element: variable, field, class, function.
    String name = element.getName();
    if (filterDisallows(element)) {
      return;
    }
    ProposalKind kind = proposalKindOf(element);
    CompletionProposal prop = createProposal(kind);
    prop.setCompletion(name);
    Element container = element.getEnclosingElement();
    if (container != null) { // TODO: may be null for functions ??
      prop.setDeclaringType(container.getName());
    }
    Type type = typeOf(element);
    if (type != null) {
      prop.setReturnType(type.getName());
    }
    requestor.accept(prop);
  }

  private void pName(SimpleIdentifier identifier) {
    pName(identifier.getName(), ProposalKind.VARIABLE);
  }

  private void pName(String name, ProposalKind kind) {
    if (filterDisallows(name)) {
      return;
    }
    CompletionProposal prop = createProposal(kind);
    prop.setCompletion(name);
    requestor.accept(prop);
  }

  private void pNull() {
    if (filterDisallows(C_NULL)) {
      return;
    }
    CompletionProposal prop = factory.createCompletionProposal(
        ProposalKind.VARIABLE,
        completionTokenOffset());
    prop.setCompletion(C_NULL);
    requestor.accept(prop);
  }

  private void pParamName(String name) {
    if (filterDisallows(name)) {
      return;
    }
    CompletionProposal prop = factory.createCompletionProposal(
        ProposalKind.PARAMETER,
        completionTokenOffset());
    prop.setCompletion(name);
    requestor.accept(prop);
  }

  private ProposalKind proposalKindOf(Element element) {
    ProposalKind kind;
    switch (element.getKind()) {
      case CONSTRUCTOR:
        kind = ProposalKind.CONSTRUCTOR;
        break;
      case FUNCTION:
        kind = ProposalKind.FUNCTION;
        break;
      case METHOD:
        kind = ProposalKind.METHOD;
        break;
      case GETTER:
        kind = ProposalKind.GETTER;
        break;
      case SETTER:
        kind = ProposalKind.SETTER;
        break;
      case CLASS:
        kind = ProposalKind.CLASS;
        break;
      case FIELD:
        kind = ProposalKind.FIELD;
        break;
      case IMPORT:
        kind = ProposalKind.IMPORT;
        break;
      case PARAMETER:
        kind = ProposalKind.PARAMETER;
        break;
      case PREFIX:
        kind = ProposalKind.LIBRARY_PREFIX;
        break;
      case FUNCTION_TYPE_ALIAS:
        kind = ProposalKind.CLASS_ALIAS;
        break;
      case TYPE_VARIABLE:
        kind = ProposalKind.TYPE_VARIABLE;
        break;
      case LOCAL_VARIABLE:
      case TOP_LEVEL_VARIABLE:
        kind = ProposalKind.VARIABLE;
        break;
      default:
        throw new IllegalArgumentException();
    }
    return kind;
  }

  private void proposeNames(NameCollector names, ClassElement classElement,
      SimpleIdentifier identifier) {
    for (List<Element> uniques : names.getNames()) {
      Element element = uniques.get(0);
      switch (element.getKind()) {
        case PARAMETER:
        case FUNCTION:
        case GETTER:
        case LOCAL_VARIABLE:
        case METHOD:
        case SETTER:
        case TOP_LEVEL_VARIABLE:
          ExecutableElement candidate = (ExecutableElement) uniques.get(0);
          pExecutable(candidate, identifier, classElement);
          break;
        case CLASS:
          pName(element);
          break;
        default:
          break;
      }
    }
  }

  private void pTrue() {
    if (filterDisallows(C_TRUE)) {
      return;
    }
    CompletionProposal prop = factory.createCompletionProposal(
        ProposalKind.VARIABLE,
        completionTokenOffset());
    prop.setCompletion(C_TRUE);
    requestor.accept(prop);
  }

  private void pVar() {
    if (filterDisallows(C_VAR)) {
      return;
    }
    CompletionProposal prop = factory.createCompletionProposal(
        ProposalKind.VARIABLE,
        completionTokenOffset());
    prop.setCompletion(C_VAR);
    requestor.accept(prop);
  }

  private void pVoid() {
    if (filterDisallows(C_VOID)) {
      return;
    }
    CompletionProposal prop = factory.createCompletionProposal(
        ProposalKind.VARIABLE,
        completionTokenOffset());
    prop.setCompletion(C_VOID);
    requestor.accept(prop);
  }

  private void setParameterInfo(ExecutableElement cons, CompletionProposal prop) {
    List<String> params = new ArrayList<String>();
    List<String> types = new ArrayList<String>();
    boolean named = false, positional = false;
    int posCount = 0;
    for (ParameterElement param : cons.getParameters()) {
      if (!param.isSynthetic()) {
        switch (param.getParameterKind()) {
          case REQUIRED:
            posCount += 1;
            break;
          case NAMED:
            named = true;
            break;
          case POSITIONAL:
            positional = true;
            break;
        }
        params.add(param.getName());
        types.add(param.getType().getName());
      }
    }
    prop.setParameterNames(params.toArray(new String[params.size()]));
    prop.setParameterTypes(types.toArray(new String[types.size()]));
    prop.setParameterStyle(posCount, named, positional);
  }

  // Find the parent declaration of the given node and extract the name of the type it is defining.
  private SimpleIdentifier typeDeclarationName(ASTNode node) {
    ASTNode parent = node;
    while (parent != null) {
      if (parent instanceof ClassDeclaration) {
        return ((ClassDeclaration) parent).getName();
      }
      if (parent instanceof ClassTypeAlias) {
        return ((ClassTypeAlias) parent).getName();
      }
      if (parent instanceof FunctionTypeAlias) {
        return ((FunctionTypeAlias) parent).getName();
      }
      parent = parent.getParent();
    }
    return null;
  }

  private Type typeOf(Element receiver) {
    Type receiverType;
    switch (receiver.getKind()) {
      case FIELD:
      case PARAMETER:
      case LOCAL_VARIABLE:
      case TOP_LEVEL_VARIABLE: {
        VariableElement receiverElement = (VariableElement) receiver;
        receiverType = receiverElement.getType();
        break;
      }
      case CONSTRUCTOR:
      case FUNCTION:
      case METHOD:
      case GETTER:
      case SETTER: {
        ExecutableElement receiverElement = (ExecutableElement) receiver;
        FunctionType funType = receiverElement.getType();
        receiverType = funType.getReturnType();
        break;
      }
      case CLASS: {
        ClassElement receiverElement = (ClassElement) receiver;
        receiverType = receiverElement.getType();
        break;
      }
      case DYNAMIC: {
        DynamicElementImpl receiverElement = (DynamicElementImpl) receiver;
        receiverType = receiverElement.getType();
        break;
      }
      case FUNCTION_TYPE_ALIAS: {
        FunctionTypeAliasElement receiverElement = (FunctionTypeAliasElement) receiver;
        FunctionType funType = receiverElement.getType();
        receiverType = funType.getReturnType();
        break;
      }
      default: {
        receiverType = null;
        break;
      }
    }
    return receiverType;
  }

  private Type typeOf(Expression expr) {
    Type type = expr.getPropagatedType();
    if (type == null) {
      type = expr.getStaticType();
    }
    if (type == null) {
      type = DynamicTypeImpl.getInstance();
    }
    return type;
  }

  private Type typeOfContainingClass(ASTNode node) {
    ASTNode parent = node;
    while (parent != null) {
      if (parent instanceof ClassDeclaration) {
        return ((ClassDeclaration) parent).getElement().getType();
      }
      parent = parent.getParent();
    }
    return DynamicTypeImpl.getInstance();
  }
}

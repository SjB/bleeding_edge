package com.google.dart.engine.services.completion;

import com.google.dart.engine.ast.ASTNode;
import com.google.dart.engine.ast.Expression;
import com.google.dart.engine.ast.FunctionTypeAlias;
import com.google.dart.engine.ast.Identifier;
import com.google.dart.engine.ast.MethodDeclaration;
import com.google.dart.engine.ast.SimpleFormalParameter;
import com.google.dart.engine.ast.SimpleIdentifier;
import com.google.dart.engine.ast.TypeName;
import com.google.dart.engine.ast.VariableDeclaration;
import com.google.dart.engine.ast.VariableDeclarationList;
import com.google.dart.engine.ast.WithClause;
import com.google.dart.engine.ast.visitor.GeneralizingASTVisitor;

/**
 * @coverage com.google.dart.engine.services.completion
 */
class ContextAnalyzer extends GeneralizingASTVisitor<Void> {
  CompletionState state;
  ASTNode completionNode;
  boolean inTypeName;
  boolean inIdentifier;
  boolean inExpression;

  ContextAnalyzer(CompletionState state, ASTNode completionNode) {
    this.state = state;
    this.completionNode = completionNode;
  }

  @Override
  public Void visitExpression(Expression node) {
    inExpression = true;
    state.includesLiterals();
    return super.visitExpression(node);
  }

  @Override
  public Void visitFunctionTypeAlias(FunctionTypeAlias node) {
    if (inTypeName || node.getReturnType() == null) {
      // This may be an incomplete class type alias
      state.includesUndefinedDeclarationTypes();
    }
    return super.visitFunctionTypeAlias(node);
  }

  @Override
  public Void visitIdentifier(Identifier node) {
    // Identifiers cannot safely be generalized to expressions, so just walk up one level.
    // LibraryIdentifier is never an expression. PrefixedIdentifier may be an expression, but
    // not in a catch-clause or a declaration. SimpleIdentifier may be an expression, but not
    // in a constructor name, label, or where PrefixedIdentifier is not.
    return visitNode(node);
  }

  @Override
  public Void visitMethodDeclaration(MethodDeclaration node) {
    state.sourceDeclarationIsStatic(node.isStatic());
    return super.visitMethodDeclaration(node);
  }

  @Override
  public Void visitNode(ASTNode node) {
    // Walk UP the tree, not down.
    ASTNode parent = node.getParent();
    if (parent != null) {
      parent.accept(this);
    }
    return null;
  }

  @Override
  public Void visitSimpleFormalParameter(SimpleFormalParameter node) {
    state.includesUndefinedTypes();
    return super.visitSimpleFormalParameter(node);
  }

  @Override
  public Void visitSimpleIdentifier(SimpleIdentifier node) {
    inIdentifier = true;
    return super.visitSimpleIdentifier(node);
  }

  @Override
  public Void visitTypeName(TypeName node) {
    inTypeName = true;
    return super.visitTypeName(node);
  }

  @Override
  public Void visitVariableDeclaration(VariableDeclaration node) {
    if (node.getName() == completionNode) {
      state.prohibitsLiterals();
    }
    return super.visitVariableDeclaration(node);
  }

  @Override
  public Void visitVariableDeclarationList(VariableDeclarationList node) {
    state.includesUndefinedDeclarationTypes();
    return super.visitVariableDeclarationList(node);
  }

  @Override
  public Void visitWithClause(WithClause node) {
    state.mustBeMixin();
    return super.visitWithClause(node);
  }
}

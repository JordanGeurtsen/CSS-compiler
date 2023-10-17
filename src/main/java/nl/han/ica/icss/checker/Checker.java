package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.ArrayList;
import java.util.HashMap;

public class Checker {

    private IHANLinkedList<HashMap<String, ExpressionType>> variableTypes;

    public void check(AST ast) {
        variableTypes = new HANLinkedList<>();
        variableTypes.addFirst(new HashMap<>());

        checkStylesheet(ast.root);
    }

    private void checkStylesheet(Stylesheet stylesheet) {
        for (ASTNode child : stylesheet.getChildren()) {
            if (child instanceof VariableAssignment) {
                checkVariableAssignment((VariableAssignment) child);
            }

            if (child instanceof Stylerule) {
                checkStylerule((Stylerule) child);
            }
        }
    }

    private void checkVariableAssignment(VariableAssignment variableAssignment) {
        VariableReference variableReference = variableAssignment.name;
        ExpressionType expressionType = checkExpressionType(variableAssignment.expression);

        if (expressionType == null || expressionType == ExpressionType.UNDEFINED) {
            variableAssignment.setError("Expression type is undefined");
            return;
        }

        variableTypes.getFirst().put(variableReference.name, expressionType);
    }


    private void checkStylerule(Stylerule stylerule) {
        checkRuleBody(stylerule.body);
    }

    private void checkRuleBody(ArrayList<ASTNode> ruleBody) {
        for (ASTNode rule : ruleBody) {
            if (rule instanceof Declaration) {
                checkDeclaration((Declaration) rule);
                continue;
            }

            if (rule instanceof IfClause) {
                checkIfClause((IfClause) rule);
            }
        }
    }

    private void checkDeclaration(Declaration declaration) {
        ExpressionType expressionType = checkExpressionType(declaration.expression);

        if (expressionType == ExpressionType.UNDEFINED) {
            declaration.setError("Expression type is undefined");
        }

        variableTypes.getFirst().forEach((key, value) -> {
            if (key.equals(declaration.expression.toString())) {
                if (value != expressionType) {
                    declaration.setError("Expression type does not match variable type");
                }
            }
        });
    }

    private void checkIfClause(IfClause ifClause) {
        if (checkExpressionType(ifClause.conditionalExpression) != ExpressionType.BOOL) {
            ifClause.setError("If clause conditional expression must be of type bool");
        }

        variableTypes.addFirst(new HashMap<>());
        checkRuleBody(ifClause.body);
        variableTypes.removeFirst();

        if (ifClause.elseClause != null) {
            variableTypes.addFirst(new HashMap<>());
            checkElseClause(ifClause.elseClause);
            variableTypes.removeFirst();
        }
    }

    private void checkElseClause(ElseClause elseClause) {
        checkRuleBody(elseClause.body);
    }

    private ExpressionType checkExpressionType(Expression expression) {
        if (expression instanceof VariableReference) {
            return checkVariableReferenceType((VariableReference) expression);
        }

        if (expression instanceof Operation) {
            return checkOperationType((Operation) expression);
        }

        if (expression instanceof BoolLiteral) {
            return ExpressionType.BOOL;
        } else if (expression instanceof ColorLiteral) {
            return ExpressionType.COLOR;
        } else if (expression instanceof PercentageLiteral) {
            return ExpressionType.PERCENTAGE;
        } else if (expression instanceof PixelLiteral) {
            return ExpressionType.PIXEL;
        } else if (expression instanceof ScalarLiteral) {
            return ExpressionType.SCALAR;
        }

        return ExpressionType.UNDEFINED;
    }

    private ExpressionType checkVariableReferenceType(VariableReference variableReference) {
        ExpressionType expressionType = null;

        for (int i = 0; i < variableTypes.getSize(); i++) {
            HashMap<String, ExpressionType> variableType = variableTypes.get(i);

            if (variableType.containsKey(variableReference.name)) {
                expressionType = variableType.get(variableReference.name);
            }
        }

        if (expressionType == null) {
            variableReference.setError("Variable " + variableReference.name + " is not declared or not in the scope.");
        }

        return expressionType;
    }

    private ExpressionType checkOperationType(Operation operation) {
        ExpressionType left;
        ExpressionType right;

        if (operation.lhs instanceof Operation) {
            left = checkOperationType((Operation) operation.lhs);
        } else {
            left = checkExpressionType(operation.lhs);
        }

        if (operation.rhs instanceof Operation) {
            right = checkOperationType((Operation) operation.rhs);
        } else {
            right = checkExpressionType(operation.rhs);
        }

        if (left == ExpressionType.UNDEFINED || right == ExpressionType.UNDEFINED) {
            operation.setError("Expression type is undefined");
            return ExpressionType.UNDEFINED;
        }

        if (left == ExpressionType.COLOR || left == ExpressionType.BOOL) {
            operation.setError("Left hand side contains an invalid expression type");
            return ExpressionType.UNDEFINED;
        }

        if (right == ExpressionType.COLOR || right == ExpressionType.BOOL) {
            operation.setError("Right hand side contains an invalid expression type");
            return ExpressionType.UNDEFINED;
        }

        if (operation instanceof MultiplyOperation) {
            if (left != ExpressionType.SCALAR && right != ExpressionType.SCALAR) {
                operation.setError("Multiply operation can only be used with at least one scalar literal");
                return ExpressionType.UNDEFINED;
            }
            return left != ExpressionType.SCALAR ? left : right;
        } else if (operation instanceof SubtractOperation) {
            if (left != right) {
                operation.setError("Subtract operation can only be used with the same expression types");
                return ExpressionType.UNDEFINED;
            }
        } else if (operation instanceof AddOperation) {
            if (left != right) {
                operation.setError("Operation can only be used with the same expression types");
                return ExpressionType.UNDEFINED;
            }
        }

        return left;
    }
}
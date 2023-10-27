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
    private IHANLinkedList<HashMap<String, ArrayList<ExpressionType>>> declarationTypes;


    public void check(AST ast) {
        variableTypes = new HANLinkedList<>();
        declarationTypes = new HANLinkedList<>();

        variableTypes.addFirst(new HashMap<>());
        declarationTypes.addFirst(new DeclarationTypes().getDeclarationTypes());

        checkStylesheet(ast.root);

        variableTypes.removeFirst();
        declarationTypes.removeFirst();
    }

    private void checkStylesheet(Stylesheet stylesheet) {
        for (ASTNode child : stylesheet.getChildren()) {
            if (child instanceof VariableAssignment) {
                checkVariableAssignment((VariableAssignment) child);
                continue;
            }

            if (child instanceof Stylerule) {
                checkStylerule((Stylerule) child);
            }
        }
    }

    private void checkStylerule(Stylerule stylerule) {
        checkRuleBody(stylerule.body);
    }

    private void checkRuleBody(ArrayList<ASTNode> ruleBody) {
        for (ASTNode rule : ruleBody) {
            if (rule instanceof VariableAssignment) {
                checkVariableAssignment((VariableAssignment) rule);
                continue;
            }

            if (rule instanceof Declaration) {
                checkDeclaration((Declaration) rule);
                continue;
            }

            if (rule instanceof IfClause) {
                checkIfClause((IfClause) rule);
            }
        }
    }

    private void checkVariableAssignment(VariableAssignment variableAssignment) {
        VariableReference variableReference = variableAssignment.name;
        ExpressionType expressionType = checkExpressionType(variableAssignment.expression);

        if (expressionType == ExpressionType.UNDEFINED) {
            variableAssignment.setError("Expression type is undefined");
            return;
        }

        variableTypes.getFirst().put(variableReference.name, expressionType);
    }

    private void checkDeclaration(Declaration declaration) {
        ExpressionType expressionType = checkExpressionType(declaration.expression);

        if (expressionType == ExpressionType.UNDEFINED) {
            declaration.setError("Expression type is undefined");
            return;
        }

        if (declarationTypes.getFirst().containsKey(declaration.property.name)) {
            ArrayList<ExpressionType> types = declarationTypes.getFirst().get(declaration.property.name);

            if (!types.contains(expressionType)) {
                declaration.setError("Expression type " + expressionType.name().toLowerCase() + " does not match declaration type(s) for " + declaration.property.name);
            }
            return;
        }

        if (variableTypes.getFirst().containsKey(declaration.property.name)) {
            if (variableTypes.getFirst().get(declaration.property.name) != expressionType) {
                declaration.setError("Expression type " + expressionType.name().toLowerCase() + " does not match variable type");
            }
        }
    }

    private void checkIfClause(IfClause ifClause) {
        ExpressionType expressionType = checkExpressionType(ifClause.conditionalExpression);

        if (expressionType == ExpressionType.UNDEFINED) {
            ifClause.setError("Expression type is undefined");
            return;
        }

        if (expressionType != ExpressionType.BOOL) {
            ifClause.setError("If clause conditional expression is of type " + expressionType.name().toLowerCase() + ", this must be a boolean expression");
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
        ExpressionType expressionType = ExpressionType.UNDEFINED;

        for (int i = 0; i < variableTypes.getSize(); i++) {
            HashMap<String, ExpressionType> variableType = variableTypes.get(i);

            if (variableType.containsKey(variableReference.name)) {
                expressionType = variableType.get(variableReference.name);
            }
        }

        if (expressionType == ExpressionType.UNDEFINED) {
            variableReference.setError("Variable " + variableReference.name + " is not declared or not in the scope.");
        }

        return expressionType;
    }

    private ExpressionType checkOperationType(Operation operation) {
        ExpressionType left = checkExpressionType(operation.lhs);
        ExpressionType right;

        if (operation instanceof MultiplyOperation && operation.rhs instanceof Operation && ((Operation) operation.rhs).lhs instanceof ScalarLiteral) {
            Expression previousLeft = operation.lhs;
            Expression previousRightLeftHand = ((Operation) operation.rhs).lhs;
            ((Operation) operation.rhs).lhs = previousLeft;
            operation.lhs = previousRightLeftHand;

            right = ExpressionType.SCALAR;
            ExpressionType result = checkExpressionType(operation.rhs);

            operation.lhs = previousLeft;
            ((Operation) operation.rhs).lhs = previousRightLeftHand;

            if (operation.rhs instanceof MultiplyOperation && left == ExpressionType.SCALAR) {
                left = result;
            }
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
        } else if (operation instanceof SubtractOperation || operation instanceof AddOperation) {
            if (left != right) {
                operation.setError("Add or subtract operation can only be used with the same expression types");
                return ExpressionType.UNDEFINED;
            }
        }

        return left;
    }
}
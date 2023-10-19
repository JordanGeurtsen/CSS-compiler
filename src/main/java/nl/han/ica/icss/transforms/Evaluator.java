package nl.han.ica.icss.transforms;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;

import java.util.ArrayList;
import java.util.HashMap;

public class Evaluator implements Transform {

    private IHANLinkedList<HashMap<String, Literal>> variableValues;

    @Override
    public void apply(AST ast) {
        variableValues = new HANLinkedList<>();
        variableValues.addFirst(new HashMap<>());
        evaluateStylesheet(ast.root);
        variableValues.removeFirst();
    }

    private void evaluateStylesheet(Stylesheet stylesheet) {
        ArrayList<ASTNode> childrenToRemove = new ArrayList<>();

        for (ASTNode child : stylesheet.getChildren()) {
            if (child instanceof VariableAssignment) {
                evaluateVariableAssignment((VariableAssignment) child);
                childrenToRemove.add(child);
                continue;
            }

            if (child instanceof Stylerule) {
                variableValues.addFirst(new HashMap<>());
                evaluateStylerule((Stylerule) child);
                variableValues.removeFirst();
            }
        }

        stylesheet.getChildren().removeAll(childrenToRemove);
    }

    private void evaluateStylerule(Stylerule stylerule) {
        ArrayList<ASTNode> evaluatedBody = new ArrayList<>();
        evaluateRuleBody(stylerule.body, evaluatedBody);
        stylerule.body = evaluatedBody;
    }

    private void evaluateRuleBody(ArrayList<ASTNode> ruleBody, ArrayList<ASTNode> evaluatedBody) {
        for (ASTNode rule : ruleBody) {
            if (rule instanceof VariableAssignment) {
                evaluateVariableAssignment((VariableAssignment) rule);
                continue;
            }

            if (rule instanceof Declaration) {
                evaluateDeclaration((Declaration) rule);
                evaluatedBody.add(rule);
                continue;
            }

            if (rule instanceof IfClause) {
                evaluateIfElseClause((IfClause) rule, evaluatedBody);
            }
        }
    }

    private void evaluateVariableAssignment(VariableAssignment variableAssignment) {
        variableAssignment.expression = evaluateExpression(variableAssignment.expression);
        variableValues.getFirst().put(variableAssignment.name.name, (Literal) variableAssignment.expression);
    }

    private void evaluateDeclaration(Declaration declaration) {
        declaration.expression = evaluateExpression(declaration.expression);
    }

    private void evaluateIfElseClause(IfClause ifClause, ArrayList<ASTNode> evaluatedBody) {
        ifClause.conditionalExpression = evaluateExpression(ifClause.conditionalExpression);

        if (((BoolLiteral) ifClause.conditionalExpression).value) {
            evaluateRuleBody(ifClause.body, evaluatedBody);

            if (ifClause.elseClause != null) {
                ifClause.elseClause.body.clear();
            }
        } else if (ifClause.elseClause != null) {
            evaluateRuleBody(ifClause.elseClause.body, evaluatedBody);
        }
    }

    private Literal evaluateExpression(Expression expression) {
        if (expression instanceof Operation) {
            return evaluateOperation((Operation) expression);
        }

        if (expression instanceof VariableReference) {
            Literal literal = null;

            for (int i = 0; i < variableValues.getSize(); i++) {
                HashMap<String, Literal> variableValue = variableValues.get(i);

                if (variableValue.containsKey(((VariableReference) expression).name)) {
                    literal = variableValue.get(((VariableReference) expression).name);
                    break;
                }
            }
            return literal;
        }

        return convertToLiteral(expression);
    }

    private Literal evaluateOperation(Operation operation) {
        Expression left = operation.lhs;
        Expression right = operation.rhs;

        if (left instanceof Operation || left instanceof VariableReference) {
            left = evaluateExpression(left);
        }

        if (right instanceof Operation) {
            if (right instanceof MultiplyOperation) {
                Expression leftHand;
                Expression rightHand = ((Operation) right).rhs;

                if (rightHand instanceof Operation) {
                    Operation multiplyToSolve = (Operation) right;
                    multiplyToSolve.rhs = ((Operation) rightHand).lhs;

                    leftHand = literalBuilder(left instanceof ScalarLiteral ? right : left , executeOperation(multiplyToSolve));

                    if (((Operation) rightHand).rhs instanceof Operation) {
                        rightHand = ((Operation) rightHand).rhs;
                    } else {
                        rightHand = evaluateExpression(((Operation) rightHand).rhs);
                    }
                    AddOperation addOperation = new AddOperation();
                    addOperation.lhs = leftHand;
                    addOperation.rhs = rightHand;
                    right = evaluateOperation(addOperation);
                } else {
                    right = evaluateExpression(right);
                }
            } else {
                right = evaluateOperation((Operation) right);
            }

        } else if (right instanceof VariableReference) {
            right = evaluateExpression(right);
        }

        Expression operationExpression = left instanceof ScalarLiteral ? right : left;

        operation.lhs = left;
        operation.rhs = right;

        return literalBuilder(operationExpression, executeOperation(operation));
    }

    private int executeOperation(Operation operation) {
        if (operation instanceof MultiplyOperation) {
           return executeMultiplyOperation((MultiplyOperation) operation);
        } else if (operation instanceof AddOperation) {
            return executeAddOperation((AddOperation) operation);
        } else if (operation instanceof SubtractOperation) {
            return executeSubtractOperation((SubtractOperation) operation);
        }
        return 0;
    }

    private int executeMultiplyOperation(MultiplyOperation multiplyOperation) {
        Expression left = multiplyOperation.lhs;
        Expression right = multiplyOperation.rhs;

        return getLiteralValue(left) * getLiteralValue(right);
    }

    private int executeAddOperation(AddOperation addOperation) {
        Expression left = addOperation.lhs;
        Expression right = addOperation.rhs;
        return getLiteralValue(left) + getLiteralValue(right);
    }

    private int executeSubtractOperation(SubtractOperation subtractOperation) {
        Expression left = subtractOperation.lhs;
        Expression right = subtractOperation.rhs;
        return getLiteralValue(left) - getLiteralValue(right);
    }

    private Literal convertToLiteral(Expression expression) {
        if (expression instanceof ScalarLiteral) {
            return new ScalarLiteral(((ScalarLiteral) expression).value);
        } else if (expression instanceof PixelLiteral) {
            return new PixelLiteral(((PixelLiteral) expression).value);
        } else if (expression instanceof PercentageLiteral) {
            return new PercentageLiteral(((PercentageLiteral) expression).value);
        }
        return null;
    }

    private int getLiteralValue(Expression expression) {
        if (expression instanceof ScalarLiteral) {
            return ((ScalarLiteral) expression).value;
        } else if (expression instanceof PixelLiteral) {
            return ((PixelLiteral) expression).value;
        } else if (expression instanceof PercentageLiteral) {
            return ((PercentageLiteral) expression).value;
        }
        return 0;
    }

    private Literal literalBuilder(Expression expression, int value) {
        if (expression instanceof ScalarLiteral) {
            return new ScalarLiteral(value);
        } else if (expression instanceof PixelLiteral) {
            return new PixelLiteral(value);
        } else if (expression instanceof PercentageLiteral) {
            return new PercentageLiteral(value);
        }
        return null;
    }
}

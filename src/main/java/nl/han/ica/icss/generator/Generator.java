package nl.han.ica.icss.generator;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;

public class Generator {

    public String generate(AST ast) {
        StringBuilder cssBuilder = new StringBuilder();
        generateStylesheet(ast.root, cssBuilder);
        return cssBuilder.toString();
    }

    private void generateStylesheet(Stylesheet stylesheet, StringBuilder builder) {
        for (ASTNode styleRule : stylesheet.getChildren()) {
            generateStylerule((Stylerule) styleRule, builder);
        }
    }

    private void generateStylerule(Stylerule styleRule, StringBuilder builder) {
        for (ASTNode rule : styleRule.getChildren()) {
            if (rule instanceof Selector) {
                builder.append(rule)
                        .append(" {\n");
            } else if (rule instanceof Declaration) {
                generateDeclaration((Declaration) rule, builder);
            }
        }
        builder.append("}\n\n");
    }

    private void generateDeclaration(Declaration declaration, StringBuilder builder) {
        builder.append("  ")
                .append(declaration.property.name)
                .append(": ")
                .append(generateExpression(declaration.expression))
                .append(";\n");
    }

    private String generateExpression(Expression expression) {
        if (expression instanceof ColorLiteral) {
            return "#" + ((ColorLiteral) expression).value;
        }
        if (expression instanceof PercentageLiteral) {
            return ((PercentageLiteral) expression).value + "%";
        }
        if (expression instanceof PixelLiteral) {
            return ((PixelLiteral) expression).value + "px";
        }
        return "";
    }
}

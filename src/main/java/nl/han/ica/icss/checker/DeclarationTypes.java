package nl.han.ica.icss.checker;

import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.ArrayList;
import java.util.HashMap;

public class DeclarationTypes {

    private final HashMap<String, ArrayList<ExpressionType>> declarationTypes = new HashMap<>();

    public DeclarationTypes() {
        fillDeclarationTypes();
    }

    public HashMap<String, ArrayList<ExpressionType>> getDeclarationTypes() {
        return declarationTypes;
    }

    private void fillDeclarationTypes() {
        fillColorTypes();
        fillWidthHeightTypes();
        fillFontTypes();
        fillBorderTypes();
    }

    private void fillColorTypes() {
        ArrayList<ExpressionType> colorTypes = new ArrayList<>();
        colorTypes.add(ExpressionType.COLOR);

        declarationTypes.put("color", colorTypes);
        declarationTypes.put("background-color", colorTypes);
    }

    private void fillWidthHeightTypes() {
        ArrayList<ExpressionType> widthHeightTypes = new ArrayList<>();
        widthHeightTypes.add(ExpressionType.PERCENTAGE);
        widthHeightTypes.add(ExpressionType.PIXEL);

        declarationTypes.put("width", widthHeightTypes);
        declarationTypes.put("height", widthHeightTypes);
        declarationTypes.put("max-width", widthHeightTypes);
        declarationTypes.put("max-height", widthHeightTypes);
        declarationTypes.put("min-width", widthHeightTypes);
        declarationTypes.put("min-height", widthHeightTypes);
    }

    private void fillFontTypes() {
        // Open for implementation, but not required for this assignment
    }

    private void fillBorderTypes() {
        // Open for implementation, but not required for this assignment
    }
}

grammar ICSS;

//--- LEXER: ---

// IF support:
IF: 'if';
ELSE: 'else';
BOX_BRACKET_OPEN: '[';
BOX_BRACKET_CLOSE: ']';


//Literals
TRUE: 'TRUE';
FALSE: 'FALSE';
PIXELSIZE: [0-9]+ 'px';
PERCENTAGE: [0-9]+ '%';
SCALAR: [0-9]+;


//Color value takes precedence over id idents
COLOR: '#' [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f];

//Specific identifiers for id's and css classes
ID_IDENT: '#' [a-z0-9\-]+;
CLASS_IDENT: '.' [a-z0-9\-]+;

//General identifiers
LOWER_IDENT: [a-z] [a-z0-9\-]*;
CAPITAL_IDENT: [A-Z] [A-Za-z0-9_]*;

//All whitespace is skipped
WS: [ \t\r\n]+ -> skip;

//
OPEN_BRACE: '{';
CLOSE_BRACE: '}';
SEMICOLON: ';';
COLON: ':';
PLUS: '+';
MIN: '-';
MUL: '*';
ASSIGNMENT_OPERATOR: ':=';




//--- PARSER: ---
stylesheet: (variableAssignment | styleRule)* EOF;

//--- VARIABLES: ---
variableAssignment: variableReference ASSIGNMENT_OPERATOR expression SEMICOLON;
variableReference: CAPITAL_IDENT;

//--- STYLE RULES: ---
styleRule: selector OPEN_BRACE ruleBody CLOSE_BRACE;
ruleBody: (variableAssignment | declaration | ifClause)*;

//--- SELECTORS: ---
selector: (classSelector | idSelector | tagSelector);
classSelector: CLASS_IDENT;
idSelector: ID_IDENT;
tagSelector: LOWER_IDENT;

//--- DECLARATIONS: ---
declaration: propertyName COLON expression SEMICOLON;
propertyName: LOWER_IDENT;

//--- IF CLAUSES: ---
ifClause: IF BOX_BRACKET_OPEN (boolLiteral | variableReference)* BOX_BRACKET_CLOSE OPEN_BRACE ruleBody CLOSE_BRACE (elseClause)?;
elseClause: ELSE OPEN_BRACE (declaration)* CLOSE_BRACE;

//--- EXPRESSIONS: ---
expression: literal | (addOperation | multiplyOperation | subtractOperation);

//--- LITERALS: ---
literal: (variableReference | boolLiteral | colorLiteral | percentageLiteral | pixelLiteral | scalarLiteral);
boolLiteral: (TRUE | FALSE);
colorLiteral: COLOR;
percentageLiteral: PERCENTAGE;
pixelLiteral: PIXELSIZE;
scalarLiteral: SCALAR;

//--- OPERATIONS: ---
addOperation: literal PLUS operation;
multiplyOperation: literal MUL operation;
subtractOperation: literal MIN operation;
operation: (addOperation | multiplyOperation | subtractOperation) | literal;
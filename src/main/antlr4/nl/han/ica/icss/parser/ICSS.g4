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
stylesheet: (statement)*;
statement: (selector | assignment | if_statement | if_else_statement) SEMICOLON;

selector: (id_selector | class_selector | tag_selector) (selector_op)*;
assignment: (id_selector | class_selector | tag_selector) ASSIGNMENT_OPERATOR (expression | color_expression);
if_statement: IF OPEN_BRACE (expression | color_expression) CLOSE_BRACE OPEN_BRACE (statement)* CLOSE_BRACE;
if_else_statement: IF OPEN_BRACE (expression | color_expression) CLOSE_BRACE OPEN_BRACE (statement)* CLOSE_BRACE ELSE OPEN_BRACE (statement)* CLOSE_BRACE;

selector_op: (PLUS | MIN) (id_selector | class_selector | tag_selector);

id_selector: ID_IDENT;
class_selector: CLASS_IDENT;
tag_selector: CAPITAL_IDENT;

expression: (term | term (PLUS | MIN | MUL) term);
color_expression: (COLOR | id_selector | class_selector | tag_selector);

term: (factor | factor MUL factor);
factor: (SCALAR | PIXELSIZE | PERCENTAGE | id_selector | class_selector | tag_selector | boolear);
boolear: (TRUE | FALSE);



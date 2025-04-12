// Adding these new lexer rules
fragment BYTE_UNIT  : 'B' | 'KB' | 'MB' | 'GB' | 'TB';
fragment TIME_UNIT  : 'ms' | 's' | 'm' | 'h' | 'd';

BYTE_SIZE     : DIGIT+ BYTE_UNIT;
TIME_DURATION : DIGIT+ TIME_UNIT;

// Modify the value rule
value
    : STRING
    | NUMBER
    | BYTE_SIZE
    | TIME_DURATION
    | BOOLEAN
    ;

// Add specific argument rules
byteSizeArg
    : BYTE_SIZE
    ;

timeDurationArg
    : TIME_DURATION
    ;
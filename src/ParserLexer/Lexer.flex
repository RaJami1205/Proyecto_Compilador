package Sintactico;

import java_cup.runtime.Symbol;

import java.util.List;
import java.util.ArrayList;


%%

%public
%class Lexer
%cup
%unicode
%line
%column
%state ML_COMMENT

%{
    //Lista de errores lexicos
    private java.util.List<String> erroresLexicos = new java.util.ArrayList<>();

    private Symbol symbol(int type) {
        return new Symbol(type, yyline + 1, yycolumn + 1);
    }

    private Symbol symbol(int type, Object value) {
        return new Symbol(type, yyline + 1, yycolumn + 1, value);
    }

    public java.util.List<String> getErroresLexicos(){
        return erroresLexicos;
    }
%}

LineTerminator = \r|\n|\r\n
WhiteSpace     = {LineTerminator}|[ \t\f]+
Identifier     = [a-zA-Z][a-zA-Z0-9_]*
IntegerLiteral = 0|[1-9][0-9]*
FloatLiteral   = (0|[1-9][0-9]*)\.[0-9]+
ScientificLiteral = (0|[1-9][0-9]*)e[1-9][0-9]*
FractionLiteral   = (0|[1-9][0-9]*)\/[1-9][0-9]*
CharLiteral    = \'([^\\'\r\n]|\\[btnrf\\'\"])\' 
StringLiteral  = \"([^\\\"\r\n]|\\[btnrf\\'\"])*\"

%%

<YYINITIAL> {
    {WhiteSpace}                 { /* Ignorar espacios y saltos de línea */ }

    "¡¡"[^\r\n]*               { /* Ignorar comentario de una línea */ }
    "{-"                        { yybegin(ML_COMMENT); }

    "__main__"                  { return symbol(sym.MAIN); }

    "int"                       { return symbol(sym.INT_T); }
    "float"                     { return symbol(sym.FLOAT_T); }
    "bool"                      { return symbol(sym.BOOL_T); }
    "char"                      { return symbol(sym.CHAR_T); }
    "string"                    { return symbol(sym.STRING_T); }
    "scientific"                { return symbol(sym.SCIENTIFIC_T); }
    "frac"                      { return symbol(sym.FRAC_T); }
    "empty"                     { return symbol(sym.EMPTY_T); }

    "if"                        { return symbol(sym.IF); }
    "else"                      { return symbol(sym.ELSE); }
    "do"                        { return symbol(sym.DO); }
    "while"                     { return symbol(sym.WHILE); }
    "switch"                    { return symbol(sym.SWITCH); }
    "case"                      { return symbol(sym.CASE); }
    "default"                   { return symbol(sym.DEFAULT); }
    "return"                    { return symbol(sym.RETURN); }
    "break"                     { return symbol(sym.BREAK); }
    "cin"                       { return symbol(sym.CIN); }
    "cout"                      { return symbol(sym.COUT); }

    "true"                      { return symbol(sym.TRUE); }
    "false"                     { return symbol(sym.FALSE); }

    "equal"                     { return symbol(sym.EQUAL); }
    "n_equal"                   { return symbol(sym.N_EQUAL); }
    "less_t"                    { return symbol(sym.LESS_T); }
    "less_te"                   { return symbol(sym.LESS_TE); }
    "greather_t"                { return symbol(sym.GREATHER_T); }
    "greather_te"               { return symbol(sym.GREATHER_TE); }

    "++"                        { return symbol(sym.INC); }
    "--"                        { return symbol(sym.DEC); }
    "<-"                        { return symbol(sym.ASSIGN); }
    "<|"                        { return symbol(sym.PIPE_L); }
    "|>"                        { return symbol(sym.PIPE_R); }
    "|:"                        { return symbol(sym.BLOCK_OPEN); }
    ":|"                        { return symbol(sym.BLOCK_CLOSE); }
    "<<"                        { return symbol(sym.INDEX_OPEN); }
    ">>"                        { return symbol(sym.INDEX_CLOSE); }

    "~"                         { return symbol(sym.TILDE); }
    "!"                         { return symbol(sym.BANG); }
    ","                         { return symbol(sym.COMMA); }
    "+"                         { return symbol(sym.PLUS); }
    "-"                         { return symbol(sym.MINUS); }
    "*"                         { return symbol(sym.TIMES); }
    "/"                         { return symbol(sym.DIV); }
    "%"                         { return symbol(sym.MOD); }
    "^"                         { return symbol(sym.POW); }
    "@"                         { return symbol(sym.AND); }
    "#"                         { return symbol(sym.OR); }
    "$"                         { return symbol(sym.NOT); }

    {FloatLiteral}               { return symbol(sym.FLOAT_LITERAL, yytext()); }
    {ScientificLiteral}          { return symbol(sym.SCIENTIFIC_LITERAL, yytext()); }
    {FractionLiteral}            { return symbol(sym.FRACTION_LITERAL, yytext()); }
    {IntegerLiteral}             { return symbol(sym.INTEGER_LITERAL, yytext()); }
    {CharLiteral}                { return symbol(sym.CHAR_LITERAL, yytext()); }
    {StringLiteral}              { return symbol(sym.STRING_LITERAL, yytext()); }
    {Identifier}                 { return symbol(sym.IDENTIFIER, yytext()); }

    <<EOF>>                      { return symbol(sym.EOF); }

    .                            {
                                    String msg = "Error lexico en linea: " + (yyline + 1) + 
                                            ", columna: " + (yycolumn + 1 ) + ": caracter no reconocido: '" + yytext() + "'";
                                    erroresLexicos.add(msg);
                                    //System.err.println(msg); // jflex continua con el siguiente caracter 

                                 }
}

<ML_COMMENT> {
    "-}"                        { yybegin(YYINITIAL); }
    [^]                          { /* Consumir cualquier carácter dentro del comentario */ }
    <<EOF>>                      {
                                    String msg = "Error léxico en línea " + (yyline + 1) +
                                                ": comentario multilínea sin cerrar al final del archivo";
                                    erroresLexicos.add(msg);
                                    //System.err.println(msg);
                                    return symbol(sym.EOF);
                                 }
}
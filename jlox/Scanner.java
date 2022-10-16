package jlox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static jlox.TokenType.*;

public class Scanner {
  private final String source;
  private final List<Token> tokens = new ArrayList<>();
  private int start = 0;
  private int current = 0;
  private int line = 1;
  private static final Map<String, TokenType> keywords;

  static {
    keywords = new HashMap<>();
    keywords.put("and",    AND);
    keywords.put("class",  CLASS);
    keywords.put("else",   ELSE);
    keywords.put("false",  FALSE);
    keywords.put("for",    FOR);
    keywords.put("fun",    FUN);
    keywords.put("if",     IF);
    keywords.put("nil",    NIL);
    keywords.put("or",     OR);
    keywords.put("print",  PRINT);
    keywords.put("return", RETURN);
    keywords.put("super",  SUPER);
    keywords.put("this",   THIS);
    keywords.put("true",   TRUE);
    keywords.put("var",    VAR);
    keywords.put("while",  WHILE);
  }

  Scanner(String source) {
    this.source = source;
  }

  // Return the parsed tokens from the source code
  List<Token> scanTokens() {
    while (!isAtEnd()) {
      start = current;
      scanToken();
    }
    tokens.add(new Token(EOF, "", null, line));
    return tokens;
  }


  private void scanToken() {
    char c = advance();
    switch (c) {
      case '(':
        addToken(LEFT_PAREN);
        break;
      case ')':
        addToken(RIGHT_PAREN);
        break;
      case '{':
        addToken(LEFT_BRACE);
        break;
      case '}':
        addToken(RIGHT_BRACE);
        break;
      case ',':
        addToken(COMMA);
        break;
      case '.':
        addToken(DOT);
        break;
      case '-':
        addToken(MINUS);
        break;
      case '+':
        addToken(PLUS);
        break;
      case ';':
        addToken(SEMICOLON);
        break;
      case '*':
        addToken(STAR);
        break;
      case '!':
        addToken(match('=') ? BANG_EQUAL : EQUAL);
        break;
      case '=':
        addToken(match('=') ? EQUAL_EQUAL : EQUAL);
        break;
      case '<':
        addToken(match('=') ? LESS_EQUAL : LESS);
        break;
      case '>':
        addToken(match('=') ? GREATER_EQUAL : GREATER);
        break;
      case '/':
        if (match('/')) {
          // The rest of the line should be comment. Ignore all of them.
          while (!isAtEnd() && peek() != '\n') advance();
        } else {
          addToken(SLASH);
        }
      case ' ':
      case '\r':
      case '\t':
        break;
      case '\n':
        line++;
        break;
      case '"':
        string();
        break;
      default:
        if (isDigit(c)) {
          number();
        } else if (isAlpha(c)) {
          identifier();
        } else {
          Lox.error(line, "Unexpected character.");
        }
        break;
    }
  }

  // Scan an identifier (which is either a variable or a reserved word)
  private void identifier() {
    while (isAlphaNumeric(peek())) advance();

    String text = source.substring(start, current);
    TokenType type = keywords.get(text);

    if (type == null) type = IDENTIFIER;

    addToken(type);
  }

  // Scan a string literal
  private void string() {
    while (!isAtEnd() && peek() != '"') {
      if (peek() == '\n') line++;
      advance();
    }

    if (isAtEnd()) {
      Lox.error(line, "Unterminated string.");
      return;
    }

    // The above loop terminated because of '"'
    advance();

    // Trim the surrounding quotes
    String value = source.substring(start + 1, current - 1);
    addToken(STRING, value);
  }
  
  // See whether a char is a digit
  private boolean isDigit(char c) {
    return '0' <= c && c <= '9';
  }

  // Scan a number literal
  private void number() {
    while (isDigit(peek())) advance();

    // Look for a fractional part
    if (peek() == '.' && isDigit(peekNext())) {
      // Consume the '.'
      advance();

      while (isDigit(peek())) advance();
    }

    addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
  }

  // See whether the next char is the same as the input char
  private boolean match(char expected) {
    if (isAtEnd()) return false;
    if (source.charAt(current) != expected) return false;

    current++;
    return true;
  }

  // Peak the next char without actually consuming it.
  private char peek() {
    if (isAtEnd()) return '\0';
    return source.charAt(current);
  }

  // Peak the char after the next char without consuming it
  private char peekNext() {
    if (current + 1 >= source.length()) return '\0';
    return source.charAt(current + 1);
  }

  // See whether input char is a letter
  private boolean isAlpha(char c) {
    return ('a' <= c && c <= 'z')
        || ('A' <= c && c <= 'Z')
        || c == '_';
  }
 
  // See whether input char is a letter or a number
  private boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }

  // Return current char and proceed to the next one
  private char advance() {
    return source.charAt(current++);
  }

  // Add a scanned token into the token list
  private void addToken(TokenType) {
    addToken(type, null);
  }

  // An overloaded to add a scanned token into the token list
  private void addToken(TokenType, Object literal) {
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, line));
  }

  // Detect whether we've reached the end of the source code
  private boolean isAtEnd() {
    return current >= source.length();
  }
}

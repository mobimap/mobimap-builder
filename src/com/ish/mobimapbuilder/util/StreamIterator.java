package com.ish.mobimapbuilder.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class StreamIterator implements Iterator {

    /**
     * List of chars that are treated as parts of words and aren't separated from them
     */
    private final static char[] WORD_CHARS = {'_', '-', '.', '+', '$', '#', '~'};

    private final StreamTokenizer tokenizer;
    private char[] brackets;
    private Object currentToken;

    public StreamIterator (Reader reader) {
        this (reader, " \t", "\"'");
    }

    public StreamIterator (Reader reader, String delimiters, String quotes) {
        tokenizer = new StreamTokenizer (reader);
        tokenizer.resetSyntax ();
        tokenizer.whitespaceChars (0, ' ');
        tokenizer.wordChars ('0', '9');
        tokenizer.wordChars ('a', 'z');
        tokenizer.wordChars ('A', 'Z');
        tokenizer.wordChars ('\u00A0', '\u00FF');
        tokenizer.eolIsSignificant (false);

        for (int i = 0; i < WORD_CHARS.length; i++) {
            char ch = WORD_CHARS[i];
            tokenizer.wordChars (ch, ch);
        }

        char[] delimitersChars = delimiters.toCharArray ();
        for (int i = 0; i < delimitersChars.length; i++) {
            char ch = delimitersChars[i];
            tokenizer.whitespaceChars (ch, ch);
        }

        char[] quotesChars = quotes.toCharArray ();
        for (int i = 0; i < quotesChars.length; i++) {
            char ch = quotesChars[i];
            tokenizer.quoteChar (ch);
        }

        prefetchNextToken ();
    }

    public boolean hasNext () {
        return currentToken != null;
    }

    /**
     * Gets next token from stream. Token can be String or List<String>.
     * If there're no more tokens null is returned.
     * @return Object
     */
    public Object next () {
        Object result = currentToken;
        prefetchNextToken ();
        return result;
    }

    private void prefetchNextToken () {
        currentToken = readNextToken ();
    }

    private Object readNextToken () {
        int type;
        try {
            type = tokenizer.nextToken ();
//            System.out.println (tokenizer);
        } catch (IOException ex) {
            type = StreamTokenizer.TT_EOF;
        }

        if (type == StreamTokenizer.TT_EOF) {
            return null;
        } else if (type == StreamTokenizer.TT_WORD) {
            return tokenizer.sval;
        } else {
            char ch = (char) type;
            if (ch == '(') {

                List list = new Vector ();
                while (true) {
                    Object next = readNextToken ();
                    if (next == null) {
                        list = null;
                        break;
                    } else if (next instanceof String) {
                        String s = (String) next;
                        if (s.length () == 1 && s.charAt (0) == ')') {
                            break;
                        }
                    }
                    list.add (next);
                }
                return list;
            }
            return tokenizer.sval == null ? Character.toString (ch) : tokenizer.sval;
        }
    }

    public void remove () {
        throw new UnsupportedOperationException ("TokenIterator is read-only and doesn't support remove operation");
    }
}

/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Breaks character deliminted strings into constituent fields. Unlike the
 * {@link java.util.StringTokenizer} it recognizes empty tokens. For example, using a pipe character
 * '|' as a delimiter, it would tokenize the string "a||b" into three tokens "a", "", "b". Following
 * is an example of how to use this class.
 */
public class FieldedStringTokenizer implements Enumeration<Object> {

  /** The tokens. */
  private ArrayList<String> tokens = null;

  /** Token count. */
  private int ct = 0;

  /**
   * Instantiates a {@link FieldedStringTokenizer} for the specified string. The characters in the
   * <code>delim</code> argument are the delimiters for separating tokens. Delimiter characters
   * themselves will not be treated as tokens. This tokenizer will return null tokens for adjacent
   * delimiters.
   *
   * @param str a {@link String} to be tokenized
   * @param delim a {@link String} containing delimiter characters
   */
  public FieldedStringTokenizer(final String str, final String delim) {
    //
    // Use split to get tokens
    //
    final String[] tokenArray = split(str, delim);

    // Add tokens to arraylist
    tokens = new ArrayList<String>(tokenArray.length + 1);
    for (int i = 0; i < tokenArray.length; i++) {
      tokens.add(tokenArray[i]);
    }
  }

  /**
   * Splits a line on delimiter characters and returns a {@link String} <code>[]</code> of the
   * tokens.
   *
   * @param line a {@link String} to be split
   * @param delim a {@link String} containing delimiter characters
   * @return a <code>String []</code> containing tokens from the string
   */
  public static String[] split(final String line, final String delim) {

    // Create list to contain tokens
    final List<String> tokens = new ArrayList<String>(20);

    // If no data, return empty array
    if (line == null || line.length() == 0) {
      return new String[0];
    }

    // Parse out words
    int oldIndex = 0;
    int newIndex = 0;
    final char[] chars = line.toCharArray();
    do {

      // Find next index of any delim character
      newIndex = -1;
      for (int i = oldIndex; i < chars.length; i++) {
        if (delim.indexOf(chars[i]) != -1) {
          newIndex = i;
          break;
        }
      }

      // Get next token (or last token if newIndex=-1)
      if (newIndex == -1) {

        // If the line ends with a delimiter, assume there is no
        // null token at the end
        if (oldIndex < line.length()) {
          tokens.add(line.substring(oldIndex));

        }
      } else {
        tokens.add(line.substring(oldIndex, newIndex));
        oldIndex = newIndex + 1;
      }

    } while (newIndex != -1);

    return tokens.toArray(new String[0]);
  }

  /**
   * Splits a line on delimiter characters and returns a {@link String} <code>[]</code> of the
   * tokens.
   *
   * @param line a {@link String} to be split
   * @param delim a {@link String} containing delimiter characters
   * @return a <code>String []</code> containing tokens from the string
   */
  public static Set<String> splitAsSet(final String line, final String delim) {
    final Set<String> tokens = new HashSet<String>();
    for (final String s : split(line, delim)) {
      tokens.add(s);
    }
    return tokens;
  }

  /**
   * Splits a line on delimiter characters when the number of fields is known in advance and returns
   * a string array of the tokens.
   *
   * @param line a {@link String} to be split
   * @param delim a {@link String} containing delimiter characters
   * @param fieldCt the number of fields in the line to be split.
   * @return a <code>String []</code> containing tokens from the string
   */
  public static String[] split(final String line, final String delim, final int fieldCt) {
    //
    // Prep array
    //
    final String[] tokens = new String[fieldCt];

    //
    // Return empty array if line is blank
    //
    if (line == null || line.length() == 0) {
      return new String[0];
    }

    //
    // Parse out words
    //
    int ct = 0;
    int oldIndex = 0;
    int newIndex = 0;
    int i = 0;
    final char[] chars = line.toCharArray();
    do {
      // Stop if we've reached the field count
      if (ct >= fieldCt) {
        break;
      }

      //
      // Find next index of any delim character
      //
      newIndex = -1;
      for (i = oldIndex; i < chars.length; i++) {
        if (delim.indexOf(chars[i]) != -1) {
          newIndex = i;
          break;
        }
      }

      //
      // Get next token (or last token if newIndex=-1)
      //
      if (newIndex == -1) {
        //
        // If the line ends with a delimiter, assume there is no
        // null token at the end
        //
        if (oldIndex < line.length()) {
          tokens[ct] = line.substring(oldIndex);
          ct++;
        }
      } else {
        tokens[ct++] = line.substring(oldIndex, newIndex);
        oldIndex = newIndex + 1;
      }

    } while (newIndex != -1);

    return tokens;
  }

  /**
   * Splits a line on delimiter characters when the number of fields is known in advance and
   * populates the specified String[].
   *
   * @param line a {@link String} to be split
   * @param delim a {@link String} containing delimiter characters
   * @param fieldCt <code>int</code> indicates the max number of fields to split
   * @param tokens a {@link String}[] of the right number of tokens.
   */
  public static void split(final String line, final String delim, final int fieldCt,
    final String[] tokens) {
    // return line.split("[" + delim + "]", field_ct);

    //
    // Return empty array if line is blank
    //
    if (line == null || line.length() == 0) {
      return;
    }

    //
    // Parse out words
    //
    int ct = 0;
    int oldIndex = 0;
    int newIndex = 0;
    int i = 0;
    final int len = line.length();
    final char[] chars = line.toCharArray();
    do {

      //
      // Find next index of any delim character
      //
      newIndex = -1;
      for (i = oldIndex; i < len; i++) {
        if (delim.indexOf(chars[i]) != -1) {
          newIndex = i;
          break;
        }
      }

      //
      // Get next token (or last token if new_index=-1)
      //
      if (newIndex == -1) {

        //
        // If the line ends with a delimiter, assume there is no
        // null token at the end
        //
        if (oldIndex < len) {
          tokens[ct] = line.substring(oldIndex);

        }
      } else {
        tokens[ct++] = line.substring(oldIndex, newIndex);
        oldIndex = newIndex + 1;
      }

    } while (newIndex != -1 && ct < fieldCt);

    // return tokens;

  }

  //
  // Methods
  //

  /**
   * Calculates the number of times that this tokenizer's {@link #nextToken()} method can be called
   * before it generates an exception. The current position is not advanced.
   *
   * @return an <code>int</code> count of the number of tokens
   */
  public int countTokens() {
    return tokens.size() - ct;
  }

  /**
   * Returns the same value as the {@link #hasMoreTokens()} method. It exists so that this class can
   * implement the {@link Enumeration} interface.
   *
   * @return <code>true</code> if there are more tokens; <code>false</code> otherwise
   */
  @Override
  public boolean hasMoreElements() {
    return hasMoreTokens();
  }

  /**
   * Tests if there are more tokens available from this tokenizer's string. If this method returns
   * true, then a subsequent call to {@link #nextToken()} will successfully return a token.
   *
   * @return <code>true</code> if there are more tokens; <code>false</code> otherwise
   */
  public boolean hasMoreTokens() {
    return ((tokens.size() - ct) > 0);
  }

  /**
   * Returns the same value as the {@link #nextToken()} method, except that its declared return
   * value is {@link Object} rather than {@link String}. It exists so that this class can implement
   * the {@link Enumeration} interface.
   *
   * @return An object {@link String} representation of the next token
   */
  @Override
  public Object nextElement() {
    return nextToken();
  }

  /**
   * Returns the next token in this tokenizer's string.
   *
   * @return the next token in this tokeinzer's string
   */
  public String nextToken() {
    final String token = tokens.get(ct++);
    return token;
  }

  /**
   * Join a list of strings into a single string.
   *
   * @param strs the strings to join
   * @param delim the delimiter
   * @return a joined string
   */
  public static String join(final List<String> strs, final String delim) {
    final StringBuilder sb = new StringBuilder(strs.size() * 10);
    int i = 0;
    for (final String str : strs) {
      sb.append(str).append((++i == strs.size() ? "" : delim));
    }
    return sb.toString();
  }

  /**
   * Join a set of strings into a single string.
   *
   * @param strs the strings to join
   * @param delim the delimiter
   * @return a joined string
   */
  public static String join(final Set<String> strs, final String delim) {
    final StringBuilder sb = new StringBuilder(strs.size() * 10);
    int i = 0;
    for (final String str : strs) {
      sb.append(str).append((++i == strs.size() ? "" : delim));
    }
    return sb.toString();
  }

  /**
   * Join a list of strings into a single string.
   *
   * @param strs the strings to join
   * @param delim the delimiter
   * @return a joined string
   */
  public static String join(final String[] strs, final String delim) {
    final StringBuilder sb = new StringBuilder(strs.length * 10);
    for (int i = 0; i < strs.length; i++) {
      final String str = strs[i];
      if (str != null) {
        sb.append(str);
        if (i < strs.length - 1) {
          sb.append(delim);
        }
      }
    }
    return sb.toString();
  }

}

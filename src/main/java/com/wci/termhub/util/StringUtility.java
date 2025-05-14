/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.util;

import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tartarus.snowball.ext.EnglishStemmer;

import com.google.common.base.CaseFormat;

/**
 * Utility class for interacting with Strings.
 */
public final class StringUtility {

  /** The logger. */
  @SuppressWarnings("unused")
  private static Logger logger = LoggerFactory.getLogger(StringUtility.class);

  /** The Constant PUNCTUATION. */
  private static final String PUNCTUATION = " \t-({[)}]_!@#%&*\\:;\"',.?/~+=|<>$`^";

  /** The Constant NORM_PUNCTUATION. */
  @SuppressWarnings("unused")
  private static final String NORM_PUNCTUATION = " \t-{}_!@#%&*\\:;,?/~+=|<>$`^";

  /** The Constant PUNCTUATION_REGEX. */
  private static final String PUNCTUATION_REGEX =
      "[ \\t\\-\\(\\{\\[\\)\\}\\]_!@#%&\\*\\\\:;\\\"',\\.\\?\\/~\\+=\\|<>$`^]";

  /** The Constant NORM_PUNCTUATION_REGEX. */
  @SuppressWarnings("unused")
  private static final String NORM_PUNCTUATION_REGEX = "[ \\t\\-{}_!@#%&\\*\\\\:;,?/~+=|<>$`^]";

  /** The Constant UUID_REGEX. */
  private static final String UUID_REGEX =
      "\\p{XDigit}{8}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{12}";

  /** The Constant SALTCHARS. */
  private static final String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

  /** The Constant RANDOM. */
  private static final SecureRandom RANDOM = new SecureRandom();

  // /** The stopwords. */
  // private static Set<String> stopwords = getStopWords();
  //
  // /** The unicode ascii map. */
  // private static Map<String, String> unicodeAsciiMap = getUnicodeAsciiMap();

  // /**
  // * Returns the stop words.
  // *
  // * @return the stop words
  // */
  // private static Set<String> getStopWords() {
  // try {
  // return new HashSet<>(IOUtils
  // .readLines(StringUtility.class.getClassLoader().getResourceAsStream("stopwords.txt"),
  // StandardCharsets.UTF_8));
  // } catch (final Exception e) {
  // throw new RuntimeException(e);
  // }
  // }

  // /**
  // * Returns the unicode ascii map.
  // *
  // * @return the unicode ascii map
  // */
  // private static Map<String, String> getUnicodeAsciiMap() {
  // try {
  // return IOUtils
  // .readLines(StringUtility.class.getClassLoader().getResourceAsStream("unicodeAsciiMap.txt"),
  // StandardCharsets.UTF_8)
  // .stream().filter(s -> s.startsWith("U"))
  // .filter(s -> Character.isValidCodePoint(
  // Integer.parseInt(s.replaceFirst("U\\+", "").replaceFirst("\\|.*", ""),
  // 16)))
  // .collect(Collectors.toMap(
  // k -> new String(Character.toString(
  // Integer.parseInt(k.replaceFirst("U\\+", "").replaceFirst("\\|.*", ""),
  // 16))),
  // v -> Character.toString(v.charAt(7))));
  //
  // } catch (final Exception e) {
  // throw new RuntimeException(e);
  // }
  // }

  /**
   * Instantiates an empty {@link StringUtility}.
   */
  private StringUtility() {
    // n/a
  }

  // /**
  // * To ascii.
  // *
  // * @param unicodeString the unicode string
  // * @return the string
  // * @throws Exception the exception
  // */
  // public static String toAscii(final String unicodeString) throws Exception {
  // final StringBuilder sb = new StringBuilder();
  // for (final char c : unicodeString.toCharArray()) {
  // final String str = Character.toString(c);
  // if (unicodeAsciiMap.containsKey(str)) {
  // sb.append(unicodeAsciiMap.get(str));
  // } else {
  // sb.append(str);
  // }
  // }
  // return sb.toString();
  // }

  /**
   * Convert roman numeral to arabic. For example MCMXLIX returns 1949. The roman numeral is first
   * validated and then converted.
   *
   * @param number the number
   * @return the int
   * @throws Exception the exception
   */
  public static int toArabic(final String number) throws Exception {
    if (isRomanNumeral(number)) {
      return convertToArabic(number);
    } else {
      throw new Exception("Number is not a valid roman numeral.");
    }
  }

  /**
   * To arabic.
   *
   * @param number the number
   * @return the int
   * @throws Exception the exception
   */
  private static int convertToArabic(final String number) throws Exception {
    if (number.isEmpty()) {
      return 0;
    }
    if (number.startsWith("M")) {
      return 1000 + convertToArabic(number.substring(1));
    }
    if (number.startsWith("CM")) {
      return 900 + convertToArabic(number.substring(2));
    }
    if (number.startsWith("D")) {
      return 500 + convertToArabic(number.substring(1));
    }
    if (number.startsWith("CD")) {
      return 400 + convertToArabic(number.substring(2));
    }
    if (number.startsWith("C")) {
      return 100 + convertToArabic(number.substring(1));
    }
    if (number.startsWith("XC")) {
      return 90 + convertToArabic(number.substring(2));
    }
    if (number.startsWith("L")) {
      return 50 + convertToArabic(number.substring(1));
    }
    if (number.startsWith("XL")) {
      return 40 + convertToArabic(number.substring(2));
    }
    if (number.startsWith("X")) {
      return 10 + convertToArabic(number.substring(1));
    }
    if (number.startsWith("IX")) {
      return 9 + convertToArabic(number.substring(2));
    }
    if (number.startsWith("V")) {
      return 5 + convertToArabic(number.substring(1));
    }
    if (number.startsWith("IV")) {
      return 4 + convertToArabic(number.substring(2));
    }
    if (number.startsWith("I")) {
      return 1 + convertToArabic(number.substring(1));
    }
    throw new Exception("something bad happened");
  }

  /**
   * Indicates whether or not roman numeral is the case.
   *
   * @param number the number
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public static boolean isRomanNumeral(final String number) {
    return number.matches("^M{0,4}(CM|CD|D?C{0,3})(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})$");
  }

  /**
   * Converts string field to case-insensitive string of tokens with punctuation removed For
   * example, "HIV Infection" becomes "hiv infection", while "1,2-hydroxy" becomes "1 2 hydroxy".
   *
   * @param value the value
   * @return the string
   */
  public static String normalize(final String value) {
    return ltrimNonAlpaNumeric(value).toLowerCase().replaceAll(PUNCTUATION_REGEX, " ")
        .replaceAll("\\s+", " ").trim();
  }

  /**
   * Trim non alpa numeric.
   *
   * @param value the value
   * @return the string
   */
  public static String trimNonAlpaNumeric(final String value) {
    return value.replaceFirst("^[^\\p{IsAlphabetic}\\p{IsDigit}]*", "")
        .replaceFirst("[^\\p{IsAlphabetic}\\p{IsDigit}]*$", "");
  }

  /**
   * Ltrim non alpa numeric.
   *
   * @param value the value
   * @return the string
   */
  public static String ltrimNonAlpaNumeric(final String value) {
    return value.replaceFirst("^[^\\p{IsAlphabetic}\\p{IsDigit}]*", "").trim();
  }

  // /**
  // * Reverse.
  // *
  // * @param string the string
  // * @return the string
  // */
  // public static String reverse(final String string) {
  // final List<String> list = Arrays.asList(string.split(" "));
  // Collections.reverse(list);
  // return FieldedStringTokenizer.join(list, " ").trim();
  // }

  /**
   * Wordind.
   *
   * @param name the name
   * @return the list
   */
  public static List<String> wordind(final String name) {
    final String[] tokens = FieldedStringTokenizer.split(name, PUNCTUATION);
    return Arrays.asList(tokens).stream().filter(s -> s.length() > 0).collect(Collectors.toList());
  }

  // /**
  // * Wordind no stopwords.
  // *
  // * @param name the name
  // * @return the sets the
  // */
  // public static Set<String> wordindNoStopwords(final String name) {
  // final String[] tokens = FieldedStringTokenizer.split(name, PUNCTUATION);
  // return Arrays.asList(tokens).stream().filter(s -> s.length() > 0 &&
  // !stopwords.contains(s))
  // .collect(Collectors.toSet());
  // }

  /**
   * Stem word.
   *
   * @param word the word
   * @return the string
   */
  public static String stemWord(final String word) {
    final EnglishStemmer stemmer = new EnglishStemmer();
    stemmer.setCurrent(word);
    stemmer.stem();
    return stemmer.getCurrent();
  }

  /**
   * Normalize with stemming.
   *
   * @param value the value
   * @return the string
   */
  public static String normalizeWithStemming(final String value) {
    final EnglishStemmer stemmer = new EnglishStemmer();

    final String norm = normalize(value);
    // split by spaces and stem everything, then rejoin
    return norm != null ? Arrays.stream(norm.split(" ")).map(s -> {
      stemmer.setCurrent(s);
      stemmer.stem();
      return stemmer.getCurrent();
    }).collect(Collectors.joining(" ")) : "";
  }

  /**
   * Normalize with Lucene EnglishAnalyzer.
   *
   * @param string the string
   * @return the string
   */
  public static String normalizeWithEnglishAnalyzer(final String string) {
    final List<String> result = new ArrayList<String>();
    if (string == null) {
      return null;
    }

    try (final Analyzer analyzer = new EnglishAnalyzer();
        final TokenStream stream = analyzer.tokenStream(null, new StringReader(string));) {
      // stream can't be null but needed for spotbugs
      if (stream != null) {
        stream.reset();
        while (stream.incrementToken()) {
          result.add(stream.getAttribute(CharTermAttribute.class).toString());
        }
      }
    } catch (final IOException e) {
      // not thrown b/c we're using a string reader...
      throw new RuntimeException(e);
    }
    // Collections.sort(result);
    final StringBuilder normalizedString = new StringBuilder();
    final Iterator<String> iter = result.iterator();
    while (iter.hasNext()) {
      normalizedString.append(iter.next()).append(iter.hasNext() ? " " : "");
    }
    return normalizedString.toString();

  }

  /**
   * Capitalize.
   *
   * @param value the value
   * @return the string
   */
  public static String capitalize(final String value) {
    if (StringUtility.isEmpty(value)) {
      return value;
    }
    return value.substring(0, 1).toUpperCase() + value.substring(1);
  }

  /**
   * Capitalize each word.
   *
   * @param value the value
   * @return the string
   */
  public static String capitalizeEachWord(final String value) {
    if (StringUtility.isEmpty(value)) {
      return value;
    }
    final StringBuilder sb = new StringBuilder();
    int i = 0;
    for (final String word : FieldedStringTokenizer.split(value, " ")) {
      if (i++ > 0) {
        sb.append(" ");
      }
      sb.append(capitalize(word));
    }
    return sb.toString();
  }

  /**
   * Un camel case.
   *
   * @param str the str
   * @return the string
   */
  public static String unCamelCase(final String str) {
    if (StringUtility.isEmpty(str)) {
      return str;
    }
    // insert a space between lower & upper
    return capitalize(str.replaceAll("([a-z])([A-Z])", "$1 $2")
        // space before last upper in a sequence followed by lower
        .replaceAll("\\b([A-Z]+)([A-Z])([a-z])", "$1 $2$3"));
  }

  /**
   * Camel case.
   *
   * @param str the str
   * @return the string
   */
  public static String camelCase(final String str) {
    return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, str.replaceAll(" ", "_"));
  }

  /**
   * f Camel case alpha numeric.
   *
   * @param str the str
   * @return the string
   */
  public static String camelCaseAlphaNumeric(final String str) {
    final String str2 = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL,
        str.replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}\\s]", "").replaceAll("\\s", "_"));
    if (str2.isEmpty()) {
      return "";
    }
    return Character.toLowerCase(str2.charAt(0)) + str2.substring(1);
  }

  /**
   * Substr.
   *
   * @param string the string
   * @param len the len
   * @return the string
   */
  public static String substr(final String string, final int len) {
    return string.substring(0, Math.min(len, string.length()))
        + (string.length() > len ? "..." : "");
  }

  /**
   * Mask.
   *
   * @param string the string
   * @param start the start
   * @param end the end
   * @return the string
   */
  public static String mask(final String string, final int start, final int end) {
    return string.substring(0, start) + StringUtils.repeat("X", end - start)
        + string.substring(end);
  }

  /**
   * Indicates whether or not uuid is the case.
   *
   * @param uuid the uuid
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public static boolean isUuid(final String uuid) {
    return uuid != null && uuid
        .matches("^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$");
  }

  /**
   * Indicates whether or not email is the case.
   *
   * @param email the email
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public static boolean isEmail(final String email) {
    return email != null && email.matches("^[A-Za-z0-9\\.+]+@[A-Za-z0-9.-]+\\.[A-Za-z0-9.-]+$");
  }

  /**
   * Compose url.
   *
   * @param clauses the clauses
   * @return the string
   * @throws Exception the exception
   */
  public static String composeQueryString(final Map<String, String> clauses) throws Exception {
    final StringBuilder sb = new StringBuilder();
    for (final String key : clauses.keySet()) {
      // Skip empty key or value
      if (StringUtility.isEmpty(key) || StringUtility.isEmpty(clauses.get(key))) {
        continue;
      }
      if (sb.length() > 1) {
        sb.append("&");
      }
      sb.append(key).append("=");
      final String value = clauses.get(key);
      if (value.matches("^[0-9a-zA-Z\\-\\.]*$")) {
        sb.append(value);
      } else {
        sb.append(URLEncoder.encode(value, StandardCharsets.UTF_8).replaceAll("\\+", "%20"));
      }
    }
    return (sb.length() > 0 ? "?" + sb.toString() : "");
  }

  /**
   * Compose query from a list of possibly empty/null clauses and an operator (typically OR or AND).
   *
   * @param operator the operator
   * @param clauses the clauses
   * @return the string
   */
  public static String composeQuery(final String operator, final List<String> clauses) {
    final StringBuilder sb = new StringBuilder();
    if (operator.equals("OR")) {
      sb.append("(");
    }
    for (final String clause : clauses) {
      if (StringUtility.isEmpty(clause)) {
        continue;
      }
      if (sb.length() > 0 && !operator.equals("OR")) {
        sb.append(" ").append(operator).append(" ");
      }
      if (sb.length() > 1 && operator.equals("OR")) {
        sb.append(" ").append(operator).append(" ");
      }
      sb.append(clause);
    }
    if (operator.equals("OR")) {
      sb.append(")");
    }
    if (operator.equals("OR") && sb.toString().equals("()")) {
      return "";
    }

    return sb.toString();
  }

  /**
   * Compose query.
   *
   * @param operator the operator
   * @param clauses the clauses
   * @return the string
   */
  public static String composeQuery(final String operator, final String... clauses) {
    final StringBuilder sb = new StringBuilder();
    if (operator.equals("OR")) {
      sb.append("(");
    }
    for (final String clause : clauses) {
      if (StringUtility.isEmpty(clause)) {
        continue;
      } else if (sb.length() > 0 && !operator.equals("OR")) {
        sb.append(" ").append(operator).append(" ");
      } else if (sb.length() > 1 && operator.equals("OR")) {
        sb.append(" ").append(operator).append(" ");
      }

      sb.append(clause);
    }
    if (operator.equals("OR")) {
      sb.append(")");
    }
    if (operator.equals("OR") && sb.toString().equals("()")) {
      return "";
    }

    return sb.toString();
  }

  /**
   * Compose clause.
   *
   * @param fieldName the field name
   * @param fieldValue the field value
   * @param escapeValue - whether the value can have characters that need to be escaped
   * @return the string
   * @throws Exception the exception
   */
  public static String composeClause(final String fieldName, final String fieldValue,
    final boolean escapeValue) throws Exception {

    if (!StringUtility.isEmpty(fieldValue)) {
      if (escapeValue) {
        return fieldName + ":\"" + QueryParserBase.escape(fieldValue) + "\"";
      } else {
        return fieldName + ":" + fieldValue;
      }
    } else {
      return "NOT " + fieldName + ":[* TO *]";
    }
  }

  /**
   * Indicates whether or not a string is empty.
   *
   * @param str the str
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public static boolean isEmpty(final String str) {
    return str == null || str.isEmpty();
  }

  // /**
  // * Similarity.
  // *
  // * @param s1 the s 1
  // * @param s2 the s 2
  // * @return the double
  // */
  // public static double similarity(final String s1, final String s2) {
  //
  // final String ls1 = s1.toLowerCase();
  // final String ls2 = s2.toLowerCase();
  // // https://github.com/tdebatty/java-string-similarity
  // if (s1.length() < 3) {
  // if (s1.equals(s2)) {
  // return 1;
  // } else if (ls1.equals(ls2)) {
  // return 0.9;
  // } else {
  // return 0.0;
  // }
  // }
  //
  // // Let's work with sequences of 3 characters...
  // final Cosine cosine = new Cosine(3);
  //
  // // Pre-compute the profile of strings
  // final Map<String, Integer> profile1 = getProfile(ls1, 3);
  // final Map<String, Integer> profile2 = getProfile(ls2, 3);
  // final double d1 = cosine.similarity(profile1, profile2);
  // if (d1 > 0.9) {
  // return d1;
  // }
  // final Set<String> w1 = wordindNoStopwords(ls1);
  // final Set<String> w2 = wordindNoStopwords(ls2);
  // final double d2 = (Sets.intersection(w1, w2).size() * 2.0) / (w1.size() +
  // w2.size());
  // return (Math.max(d1, d2) * 0.9) + (Math.min(d1, d2) * 0.1);
  // }

  /**
   * Returns the profile.
   *
   * @param string the string
   * @param k the k
   * @return the profile
   */
  public static Map<String, Integer> getProfile(final String string, final int k) {
    final HashMap<String, Integer> shingles = new HashMap<String, Integer>();
    for (int i = 0; i < (string.length() - k + 1); i++) {
      final String shingle = string.substring(i, i + k);
      final Integer old = shingles.get(shingle);
      if (old != null) {
        shingles.put(shingle, old + 1);
      } else {
        shingles.put(shingle, 1);
      }
    }

    return Collections.unmodifiableMap(shingles);
  }

  /**
   * Escape json.
   *
   * @param text the text
   * @return the string
   */
  public static String escapeJson(final String text) {
    return StringEscapeUtils.escapeJson(text);
  }

  /**
   * Escape regex.
   *
   * @param regex the regex
   * @return the string
   */
  public static String escapeRegex(final String regex) {
    return regex.replaceAll("([\\\\\\.\\[\\{\\(\\*\\+\\?\\^\\$\\|])", "\\\\$1");
  }

  /**
   * Escape.
   *
   * @param s the s
   * @return the string
   */
  public static String escapeQuery(final String s) {
    if (s == null) {
      return "";
    }
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      final char c = s.charAt(i);
      // These characters are part of the query syntax and must be escaped
      if (c == '\\' || c == '+' || c == '-' || c == '!' || c == '(' || c == ')' || c == ':'
          || c == '^' || c == '[' || c == ']' || c == '\"' || c == '{' || c == '}' || c == '~'
          || c == '*' || c == '?' || c == '|' || c == '&' || c == '/') {
        sb.append('\\');
      }
      sb.append(c);
    }

    // Escape "and", "or", and "not" - escape each char of the word
    final String q1 = sb.toString();
    final StringBuilder sb2 = new StringBuilder();
    boolean first = true;
    for (final String word : q1.split(" ")) {
      if (!first) {
        sb2.append(" ");
      }
      first = false;
      if (word.toLowerCase().matches("(and|or|not)")) {
        for (final String c : word.split("")) {
          sb2.append("\\").append(c);
        }
      } else {
        sb2.append(word);
      }
    }
    return sb2.toString();
  }

  /**
   * Find all uuids.
   *
   * @param value the value
   * @return the sets the
   */
  public static Set<String> findAllUuids(final String value) {
    final Pattern pairRegex = Pattern.compile(UUID_REGEX);
    final Matcher matcher = pairRegex.matcher(value);
    final Set<String> ids = new HashSet<>();
    while (matcher.find()) {
      ids.add(matcher.group(0));
    }
    return ids;
  }

  /**
   * Removes the non alphanumeric.
   *
   * @param str the str
   * @return the string
   */
  public static String removeNonAlphanumeric(final String str) {
    // replace the given string
    // with empty string except the pattern "[^a-zA-Z0-9]"
    return str.replaceAll("[^a-zA-Z0-9]", "");
  }

  /**
   * Random string.
   *
   * @param length the length
   * @return the string
   */
  public static String randomString(final int length) {
    final StringBuilder salt = new StringBuilder();
    while (salt.length() < length) {
      final int index = RANDOM.nextInt(SALTCHARS.length());
      salt.append(SALTCHARS.charAt(index));
    }
    return salt.toString();
  }
}

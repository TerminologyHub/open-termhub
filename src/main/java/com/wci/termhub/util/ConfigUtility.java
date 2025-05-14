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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.crypto.Cipher;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.primitives.UnsignedBytes;

/**
 * Utility class for interacting with the configuration, serializing to JSON and other purposes.
 */
@Component
public final class ConfigUtility {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(ConfigUtility.class);

  /** The zone map. */
  private static Map<String, String> zoneMap = new HashMap<>();

  /** The config properties map. */
  private static Map<String, Properties> configPropertiesMap = new HashMap<>();

  /** The environment. */
  private static Environment env;

  /** The config. */
  private static Properties config = null;

  /** The Constant TEST_MODE. */
  private static boolean testMode = false;

  /**
   * Sets the environment.
   *
   * @param environment the new environment
   */
  @Autowired
  public void setEnvironment(final Environment environment) {
    env = environment;
  }

  /**
   * Instantiates an empty {@link ConfigUtility}.
   */
  private ConfigUtility() {
    // n/a
  }

  /** The Constant DEFAULT. */
  public static final String DEFAULT = "DEFAULT";

  /** The Constant ATOMCLASS (search handler for atoms). */
  public static final String ATOMCLASS = "ATOMCLASS";

  /** The Constant DATE_YYYYMMDD. */
  public static final FastDateFormat RFC_3339 =
      FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ssZZZZZ");

  /** The Constant DATE_YYYYMMDD. */
  public static final FastDateFormat DATE_YYYYMMDD = FastDateFormat.getInstance("yyyyMMdd");

  /** The Constant DATE_FORMAT2. */
  public static final FastDateFormat DATE_YYYY_MM_DD = FastDateFormat.getInstance("yyyy_MM_dd");

  /** The Constant DATE_DD_MM_YYYY. */
  public static final FastDateFormat DATE_DD_MM_YYYY = FastDateFormat.getInstance("dd_MM_yyyy");

  /** The Constant DATE_MM_DD_YYYY. */
  public static final FastDateFormat DATE_MM_DD_YYYY = FastDateFormat.getInstance("MM_dd_yyyy");

  /** The Constant DATE_FORMAT3. */
  public static final FastDateFormat DATE_YYYY = FastDateFormat.getInstance("yyyy");

  /** The Constant DATE_FORMAT4. */
  public static final FastDateFormat DATE_YYYY_MM_DD_X1 =
      FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");

  /** The Constant DATE_YYYY_MM_DD_X1_XXX. */
  public static final FastDateFormat DATE_YYYY_MM_DD_X1_XXX =
      FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ssXXX");

  /** The Constant DATE_FORMAT5. */
  public static final FastDateFormat DATE_YYYY_MM_DD_X2 =
      FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS");

  /** The Constant DATE_YYYY_MM_DD_X2_XXX. */
  public static final FastDateFormat DATE_YYYY_MM_DD_X2_XXX =
      FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

  /** The Constant DATE_FORMAT4. */
  public static final FastDateFormat DATE_YYYYMMDDHHMMSS =
      FastDateFormat.getInstance("yyyyMMddHHmmss");

  /** The Constant DATE_YYYYMMDDHHMMSSXXX. */
  public static final FastDateFormat DATE_YYYYMMDDHHMMSSXXX =
      FastDateFormat.getInstance("yyyyMMddHHmmssXXX");

  /** The Constant DATE_YYYYMMDDHHMMSSZZZZZ. */
  public static final FastDateFormat DATE_YYYYMMDDHHMMSSZZZZZ =
      FastDateFormat.getInstance("yyyyMMddHHmmssZZZZZ");

  /** The Constant PUNCTUATION. */
  public static final String PUNCTUATION = " \t-({[)}]_!@#%&*\\:;\"',.?/~+=|<>$`^";

  /** The Constant NORM_PUNCTUATION. */
  public static final String NORM_PUNCTUATION = " \t-{}_!@#%&*\\:;,?/~+=|<>$`^";

  /** The Constant PUNCTUATION_REGEX. */
  public static final String PUNCTUATION_REGEX =
      "[ \\t\\-\\(\\{\\[\\)\\}\\]_!@#%&\\*\\\\:;\\\"',\\.\\?\\/~\\+=\\|<>$`^]";

  /** The Constant NORM_PUNCTUATION_REGEX. */
  public static final String NORM_PUNCTUATION_REGEX = "[ \\t\\-{}_!@#%&\\*\\\\:;,?/~+=|<>$`^]";

  /**
   * Reset config properties. Needed for testing so we can reset the state of config.properties and
   * reload it.
   */
  public static void resetConfigProperties() {
    config = null;
  }

  /**
   * Get the config label.
   *
   * @return the label
   * @throws Exception the exception
   */
  public static String getConfigLabel() throws Exception {
    // Need to determine the label (default "demo")
    String label = "";
    final Properties labelProp = new Properties();

    // If no resource is available, go with the default
    // ONLY setups that explicitly intend to override the setting
    // cause it to be something other than the default.
    try (final InputStream input = ConfigUtility.class.getResourceAsStream("/label.prop");) {
      if (input != null) {
        labelProp.load(input);
        // If a run.config.label override can be found, use it
        final String candidateLabel = labelProp.getProperty("run.config.label");
        // If the default, uninterpolated value is used, stick again with the
        // default
        if (candidateLabel != null && !candidateLabel.equals("${run.config.label}")) {
          label = candidateLabel;
        }
        // } else {
        // logger.debug(" label.prop resource cannot be found, using default");
      }
      // logger.debug(" run.config.label = " + label);

      return (ConfigUtility.isEmpty(label)) ? "" : "." + label;
    }
  }

  /**
   * Get a local resource file.
   *
   * @param path the path
   * @return the local resource file
   * @throws Exception the exception
   */
  public static URL getLocalResourceFile(final String path) throws Exception {
    return ConfigUtility.class.getResource("/" + path);
  }

  /**
   * The get local config file.
   *
   * @return the local config file
   * @throws Exception the exception
   */
  public static String getLocalConfigFile() throws Exception {
    return getLocalConfigFolder() + "config.properties";
  }

  /**
   * Gets the local config folder.
   *
   * @return the local config folder
   * @throws Exception the exception
   */
  public static String getLocalConfigFolder() throws Exception {
    // Instead of "user.home" let's use "src/resources"
    // return System.getProperty("user.home") + "/.term-server/" +
    // getConfigLabel() + "/";
    return "src/resources/";
  }

  /**
   * Put config properties.
   *
   * @param properties the properties
   * @throws Exception the exception
   */
  public static void putConfigProperties(final Properties properties) throws Exception {
    // Clear the config properties cache
    configPropertiesMap = new HashMap<>();
    getConfigProperties().putAll(properties);
  }

  /**
   * Returns the config properties.
   *
   * @return the config properties
   * @throws Exception the exception
   */
  public static Properties getConfigProperties() throws Exception {
    if (isNull(config)) {
      final String label = getConfigLabel();

      // Now get the properties from the corresponding setting
      // This is a complicated mechanism to support multiple simultaneous
      // installations within the same container (e.g. tomcat).
      // Default setups do not require this.
      final String configFileName = System.getProperty("run.config" + label);
      String path = null;
      if (configFileName != null) {
        config = new Properties();
        try (final FileReader in = new FileReader(new File(configFileName))) {
          config.load(in);
          path = new File(configFileName).getParent();
        }
      } else if (new File(getLocalConfigFile()).exists()) {
        logger.debug("Cannot find ENV 'run.config" + label + "', using " + getLocalConfigFile());
        config = new Properties();
        try (final FileReader in = new FileReader(new File(getLocalConfigFile()));) {
          config.load(in);
          path = new File(getLocalConfigFile()).getParent();
        }
      } else {
        // Use Spring's environment instead of loading directly
        config = new Properties();
        for (final String propertyName : env.getActiveProfiles()) {
          config.setProperty(propertyName, env.getProperty(propertyName));
        }
      }

      // Interpolate "env" variables
      final Iterator<Object> keys = config.keySet().iterator();
      while (keys.hasNext()) {
        final String key = keys.next().toString();
        final String value = config.getProperty(key);
        if (value != null && value.startsWith("${") && value.endsWith("}")) {
          final String envVar = value.substring(2, value.length() - 1);
          final String envValue = System.getenv(envVar);
          if (envValue != null) {
            config.setProperty(key, envValue);
          }
        }
      }
    }
    return config;
  }

  /**
   * Handle config import.
   *
   * @param config the config
   * @param path the path
   * @throws Exception the exception
   */
  private static void handleConfigImport(final Properties config, final String path)
    throws Exception {

    // Check if config has "config.import"
    if (config.containsKey("config.import")) {

      final int size = config.size();

      // Assume config-import.properties if not specified
      final String name = config.getProperty("config.import").isEmpty() ? "config-import.properties"
          : "config-" + config.getProperty("config.import") + ".properties";
      logger.debug("    import config = " + name);
      final Properties config2 = new Properties();
      // Try loading as a file
      if (new File(ConfigUtility.isEmpty(path) ? "./" : path, name).exists()) {
        try (final FileReader in =
            new FileReader(new File(ConfigUtility.isEmpty(path) ? "./" : path, name))) {
          config2.load(in);
          config.putAll(config2);
          logger.debug("    found import config file = " + config2.size());
        }
      }

      // Try loading as a resource
      else {
        try (final InputStream is = ConfigUtility.class.getResourceAsStream("/" + name)) {
          if (is != null) {
            config2.load(is);
            config.putAll(config2);
            logger.debug("    found import config resource = " + config2.size());
          }
        }
      }

      // IF import is specified and not found or no data loaded, fail
      if (size == config.size() && !config.getProperty("config.import").isEmpty()) {
        throw new Exception("Unable to find import config = " + name);
      }
    }
  }

  /**
   * Returns the config properties for default db.
   *
   * @return the config properties for default db
   * @throws Exception the exception
   */
  public static Properties getConfigPropertiesForDefaultDb() throws Exception {
    return getConfigPropertiesForDb("wcids");
  }

  /**
   * Returns the config properties.
   *
   * @param prefix the prefix to look for when selecting DB properties
   * @return the config properties
   *
   * @throws Exception the exception
   */
  public static Properties getConfigPropertiesForDb(final String prefix) throws Exception {

    // Lazy initialize
    if (configPropertiesMap.containsKey(prefix)) {
      return configPropertiesMap.get(prefix);
    }

    final Properties dbConfig = new Properties();
    dbConfig.putAll(getConfigProperties());

    final Iterator<Object> properties = dbConfig.keySet().iterator();
    final Map<String, String> renamedProperties = new HashMap<>();

    // remove any DB properties that do not start with the specified prefix and
    // rename those that do
    while (properties.hasNext()) {

      final String property = properties.next().toString();

      if (property.startsWith("persistence.")) {

        // logger.debug("************* persistence property: " + property);

        // if this is a property we want remove the prefix label from the
        // property and add it to the temp map
        if (property.startsWith("persistence." + prefix)) {

          // logger.debug("************* RENAMING persistence property: " +
          // property.replace("persistence." + prefix + ".", ""));
          renamedProperties.put(property.replace("persistence." + prefix + ".", ""),
              dbConfig.getProperty(property));
        }

        // remove the original property
        properties.remove();
      }
    }

    // logger.debug("************* all renamed persistence properties that
    // started with '" + prefix + "': " + renamedProperties);
    // add the renamed properties back into dbConfig
    dbConfig.putAll(renamedProperties);

    configPropertiesMap.put(prefix, dbConfig);
    return dbConfig;
  }

  /**
   * Returns the config dir containing config.properties.
   *
   * @return the config dir
   * @throws Exception the exception
   */
  public static File getConfigDir() throws Exception {
    final String label = getConfigLabel();

    // Now get the properties from the corresponding setting
    // This is a complicated mechanism to support multiple simultaneous
    // installations within the same container (e.g. tomcat).
    // Default setups do not require this.
    final String configFileName = System.getProperty("run.config" + label);
    if (configFileName != null) {
      return new File(configFileName).getParentFile();
    } else {
      try (final InputStream is = ConfigUtility.class.getResourceAsStream("/config.properties")) {
        if (is != null) {
          throw new Exception("No directory, from input stream");
        }

        // retrieve locally stored config file from user configuration (if
        // available)
        else if (new File(getLocalConfigFile()).exists()) {
          return new File(getLocalConfigFile()).getParentFile();
        }
      }
    }
    throw new Exception("Unable to find config dir");

  }

  /**
   * Clear config properties.
   *
   * @throws Exception the exception
   */
  public static void clearConfigProperties() throws Exception {
    config = null;
  }

  /**
   * Returns the ui config properties.
   *
   * @return the ui config properties
   * @throws Exception the exception
   */
  public static Properties getUiConfigProperties() throws Exception {
    final Properties config = getConfigProperties();
    // use "deploy.*" and "site.*" and "base.url" properties
    final Properties p = new Properties();
    for (final Object prop : config.keySet()) {
      final String str = prop.toString();

      if (str.startsWith("deploy.") || str.equals("base.url")
          || (str.startsWith("security") && str.contains("url"))) {
        p.put(prop, config.getProperty(prop.toString()));
      }

      if (str.contains("enabled")) {
        p.put(prop, config.getProperty(prop.toString()));
      }
    }
    return p;

  }

  /**
   * New handler instance.
   *
   * @param <T> the
   * @param handler the handler
   * @param handlerClass the handler class
   * @param type the type
   * @return the object
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  public static <T> T newHandlerInstance(final String handler, final String handlerClass,
    final Class<T> type) throws Exception {
    if (handlerClass == null) {
      throw new Exception("Handler class " + handlerClass + " is not defined");
    }
    final Class<?> toInstantiate = Class.forName(handlerClass);
    if (toInstantiate == null) {
      throw new Exception("Unable to find class " + handlerClass);
    }
    Object o = null;
    try {
      o = toInstantiate.getDeclaredConstructor().newInstance();
    } catch (final Exception e) {
      logger.warn("Problem creating new handler", e);
      // do nothing
    }
    if (o == null) {
      throw new Exception(
          "Unable to instantiate class " + handlerClass + ", check for default constructor.");
    }
    if (type.isAssignableFrom(o.getClass())) {
      return (T) o;
    }
    throw new Exception("Handler is not assignable from " + type.getName());
  }

  /**
   * Returns the node for path.
   *
   * @param node the node
   * @param parts the parts
   * @return the node for path
   */
  public static JsonNode getNodeForPath(final JsonNode node, final String... parts) {
    if (parts == null || parts.length == 0) {
      return null;
    }
    JsonNode subNode = node;
    for (int i = 0; i < parts.length; i++) {
      if (subNode == null && i > 0) {
        return null;
      }
      if (subNode == null) {
        // This should never happen, avoiding findbugs error
        continue;
      } else if (parts[i].equals("[]")) {

        if (subNode.isArray() && (i + 1) < parts.length && parts[i + 1] != null
            && !parts[i + 1].equals("[]")) {
          boolean found = false;
          i++;
          for (final JsonNode anode : subNode) {
            if (anode.isArray()) {
              continue;
            }
            if (anode.get(parts[i]) != null) {
              subNode = anode.get(parts[i]);
              found = true;
            }
          }
          if (!found) {
            subNode = null;
          }
        } else {
          return null;
        }
      } else {
        subNode = subNode.get(parts[i]);
      }
    }
    return subNode;

  }

  /**
   * Merge-sort two files.
   *
   * @param files1 the first set of files
   * @param files2 the second set of files
   * @param comp the comparator
   * @param dir the sort dir
   * @param headerLine the header_line
   * @return the sorted {@link File}
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static File mergeSortedFiles(final File files1, final File files2,
    final Comparator<String> comp, final File dir, final String headerLine) throws IOException {

    final File outFile = File.createTempFile("t+~", ".tmp", dir);
    try (final BufferedReader in1 = new BufferedReader(new FileReader(files1));
        final BufferedReader in2 = new BufferedReader(new FileReader(files2));
        final BufferedWriter out = new BufferedWriter(new FileWriter(outFile))) {
      String line1 = in1.readLine();
      String line2 = in2.readLine();
      String line = null;
      if (!headerLine.isEmpty()) {
        line = headerLine;
        out.write(line);
        out.newLine();
      }
      while (line1 != null || line2 != null) {
        if (line1 == null) {
          line = line2;
          line2 = in2.readLine();
        } else if (line2 == null) {
          line = line1;
          line1 = in1.readLine();
        } else if (comp.compare(line1, line2) < 0) {
          line = line1;
          line1 = in1.readLine();
        } else {
          line = line2;
          line2 = in2.readLine();
        }
        // if a header line, do not write
        if (line != null && !line.startsWith("id")) {
          out.write(line);
          out.newLine();
        }
      }
      out.flush();
      return outFile;
    }
  }

  /**
   * Reflection sort.
   *
   * @param <T> the generic type
   * @param classes the classes
   * @param clazz the clazz
   * @param sortField the sort field
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  public static <T> void reflectionSort(final List<T> classes, final Class<T> clazz,
    final String sortField) throws Exception {
    final Method getMethod =
        clazz.getMethod("get" + sortField.substring(0, 1).toUpperCase() + sortField.substring(1));
    if (getMethod.getReturnType().isAssignableFrom(Comparable.class)) {
      throw new Exception("Referenced sort field is not comparable");
    }
    Collections.sort(classes, new Comparator<T>() {
      @Override
      public int compare(final T o1, final T o2) {
        try {
          final Comparable<Object> f1 = (Comparable<Object>) getMethod.invoke(o1, new Object[] {});
          final Comparable<Object> f2 = (Comparable<Object>) getMethod.invoke(o2, new Object[] {});
          return f1.compareTo(f2);
        } catch (final Exception e) {
          // do nothing
        }
        return 0;
      }
    });
  }

  /**
   * Indicates whether or not possible code is the case.
   *
   * @param str the str
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public static boolean isPossibleCode(final String str) {
    return str != null && !str.isEmpty() && (str.matches("^[\\d\\-\\.]+$")
        || str.matches("^([a-zA-Z]+:|[a-zA-Z]|[lL][aApP])[\\d\\-\\.]+$"));
  }

  /**
   * Returns the indent for level.
   *
   * @param level the level
   * @return the indent for level
   */
  public static String getIndentForLevel(final int level) {

    final StringBuilder sb = new StringBuilder().append("  ");
    for (int i = 0; i < level; i++) {
      sb.append("  ");
    }
    return sb.toString();
  }

  /**
   * This method is intended to bypass some incorrect static code analysis from the FindBugs Eclipse
   * plugin.
   *
   * @param o the o
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public static boolean isNull(final Object o) {
    return o == null;
  }

  /**
   * Returns the profile.
   *
   * @param string the string
   * @param k the k
   * @return the profile
   */
  public static Map<String, Integer> getProfile(final String string, final int k) {
    final Map<String, Integer> shingles = new HashMap<>();
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
   * Get the lucene max boolean clause count.
   *
   * @param prefix the prefix to look for when selecting DB properties
   * @return the max clause count
   * @throws NumberFormatException the number format exception
   * @throws Exception the exception
   */
  public static int getLuceneMaxClauseCount(final String prefix)
    throws NumberFormatException, Exception {

    if (!getConfigPropertiesForDb(prefix).containsKey("hibernate.search.max.clause.count")) {
      return 100000;
    }

    return Integer
        .valueOf(getConfigPropertiesForDb(prefix).getProperty("hibernate.search.max.clause.count"));
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

  /**
   * Indicates whether or not empty is the case.
   *
   * @param collection the collection
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public static boolean isEmpty(final Collection<?> collection) {
    return collection == null || collection.isEmpty();
  }

  /**
   * Returns the byte comparator.
   *
   * @return the byte comparator
   */
  public static Comparator<String> getByteComparator() {
    return new Comparator<String>() {

      /* see superclass */
      @Override
      public int compare(final String o1, final String o2) {
        try {
          return UnsignedBytes.lexicographicalComparator()
              .compare(o1.getBytes(StandardCharsets.UTF_8), o2.getBytes(StandardCharsets.UTF_8));
        } catch (final Exception e) {
          throw new RuntimeException(e);
        }
      }

    };
  }

  /** Size of the buffer to read/write data. */
  private static final int BUFFER_SIZE = 4096;

  /**
   * Extracts a zip file specified by the zipFilePath to a directory specified by destDirectory
   * (will be created if does not exists).
   *
   * @param zipFilePath the zip file path
   * @param destDirectory the dest directory
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void unzip(final String zipFilePath, final String destDirectory)
    throws IOException {
    final File destDir = new File(destDirectory);
    if (!destDir.exists()) {
      destDir.mkdir();
    }
    try (final ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {
      ZipEntry entry = zipIn.getNextEntry();
      // iterates over entries in the zip file
      while (entry != null) {
        final String filePath = destDirectory + File.separator + entry.getName();
        final File parentDir = new File(filePath).getParentFile();
        if (!parentDir.exists()) {
          parentDir.mkdirs();
        }
        if (!entry.isDirectory()) {
          // if the entry is a file, extracts it
          extractFile(zipIn, filePath);
        } else {
          // if the entry is a directory, make the directory
          final File dir = new File(filePath);
          dir.mkdir();
        }
        zipIn.closeEntry();
        entry = zipIn.getNextEntry();
      }
    }
  }

  /**
   * Unzip.
   *
   * @param in the in
   * @param destDirectory the dest directory
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void unzip(final InputStream in, final String destDirectory) throws IOException {
    final File destDir = new File(destDirectory);
    if (!destDir.exists()) {
      destDir.mkdir();
    }
    try (final ZipInputStream zipIn = new ZipInputStream(in)) {
      ZipEntry entry = zipIn.getNextEntry();
      // iterates over entries in the zip file
      while (entry != null) {
        final String filePath = destDirectory + File.separator + entry.getName();
        final File parentDir = new File(filePath).getParentFile();
        if (!parentDir.exists()) {
          parentDir.mkdirs();
        }
        if (!entry.isDirectory()) {
          // if the entry is a file, extracts it
          extractFile(zipIn, filePath);
        } else {
          // if the entry is a directory, make the directory
          final File dir = new File(filePath);
          dir.mkdir();
        }
        zipIn.closeEntry();
        entry = zipIn.getNextEntry();
      }
    }
  }

  /**
   * Zip.
   *
   * @param dirPath the dir path
   * @throws Exception the exception
   */
  public static void zip(final String dirPath) throws Exception {
    final Path sourceDir = Paths.get(dirPath);
    final String zipFileName = dirPath.concat(".zip");
    try (final ZipOutputStream outputStream =
        new ZipOutputStream(new FileOutputStream(zipFileName));) {
      Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attributes) {
          try {
            final Path targetFile = sourceDir.relativize(file);
            outputStream.putNextEntry(new ZipEntry(targetFile.toString()));
            final byte[] bytes = Files.readAllBytes(file);
            outputStream.write(bytes, 0, bytes.length);
            outputStream.closeEntry();
          } catch (final IOException e) {
            e.printStackTrace();
          }
          return FileVisitResult.CONTINUE;
        }
      });
      outputStream.close();
    }
  }

  /**
   * Extracts a zip entry (file entry).
   *
   * @param zipIn the zip in
   * @param filePath the file path
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private static void extractFile(final ZipInputStream zipIn, final String filePath)
    throws IOException {
    try (
        final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
      final byte[] bytesIn = new byte[BUFFER_SIZE];
      int read = 0;
      while ((read = zipIn.read(bytesIn)) != -1) {
        bos.write(bytesIn, 0, read);
      }
    }
  }

  /**
   * Sign hash.
   *
   * @param hash the hash
   * @param key the key
   * @return the string
   * @throws Exception the exception
   */
  public static byte[] signWithPrivateKey(final String hash, final PrivateKey key)
    throws Exception {

    final byte[] data = hash.getBytes("UTF8");
    final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    cipher.init(Cipher.ENCRYPT_MODE, key);
    return cipher.doFinal(data);
  }

  /**
   * Sign with public key.
   *
   * @param hash the hash
   * @param key the key
   * @return the byte[]
   * @throws Exception the exception
   */
  public static byte[] signWithPublicKey(final String hash, final PublicKey key) throws Exception {

    final byte[] data = hash.getBytes("UTF8");
    final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    cipher.init(Cipher.ENCRYPT_MODE, key);
    return cipher.doFinal(data);
  }

  /**
   * Verify with public key key.
   *
   * @param hash the hash
   * @param data the data
   * @param key the key
   * @return the string
   * @throws Exception the exception
   */
  public static boolean verifyWithPublicKey(final String hash, final byte[] data,
    final PublicKey key) throws Exception {
    final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    cipher.init(Cipher.DECRYPT_MODE, key);
    final byte[] decryptedData = cipher.doFinal(data);
    final String decryptedHash = new String(decryptedData, StandardCharsets.UTF_8);
    return hash.equals(decryptedHash);
  }

  /**
   * Verify with private key.
   *
   * @param hash the hash
   * @param data the data
   * @param key the key
   * @return true, if successful
   * @throws Exception the exception
   */
  public static boolean verifyWithPrivateKey(final String hash, final byte[] data,
    final PrivateKey key) throws Exception {
    final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    cipher.init(Cipher.DECRYPT_MODE, key);
    final byte[] decryptedData = cipher.doFinal(data);
    final String decryptedHash = new String(decryptedData, StandardCharsets.UTF_8);
    return hash.equals(decryptedHash);
  }

  /**
   * Read private key.
   *
   * @param path the path
   * @return the private key
   * @throws Exception the exception
   */
  public static PrivateKey readPrivateRsaKey(final String path) throws Exception {
    final File filePrivateKey = new File(path + "/private.key");
    try (final FileInputStream fis = new FileInputStream(path + "/private.key")) {
      final byte[] encodedPrivateKey = new byte[(int) filePrivateKey.length()];
      fis.read(encodedPrivateKey);
      final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      final PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
      return keyFactory.generatePrivate(privateKeySpec);
    }

  }

  /**
   * Read public rsa key.
   *
   * @param path the path
   * @return the public key
   * @throws Exception the exception
   */
  public static PublicKey readPublicRsaKey(final String path) throws Exception {
    final File filePublicKey = new File(path + "/public.key");
    try (final FileInputStream fis = new FileInputStream(path + "/public.key")) {
      final byte[] encodedPublicKey = new byte[(int) filePublicKey.length()];
      fis.read(encodedPublicKey);

      final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      final X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedPublicKey);
      return keyFactory.generatePublic(publicKeySpec);
    }

  }

  /**
   * Returns the key pair.
   *
   * @param algorithm the algorithm, e.g. "RSA"
   * @param length the length, e.g. 2048
   * @return the key pair
   * @throws NoSuchAlgorithmException the no such algorithm exception
   */
  public static KeyPair getKeyPair(final String algorithm, final int length)
    throws NoSuchAlgorithmException {
    final KeyPairGenerator kpg = KeyPairGenerator.getInstance(algorithm);
    kpg.initialize(length);
    return kpg.genKeyPair();
  }

  /**
   * Save key pair.
   *
   * @param path the path
   * @param keyPair the key pair
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void saveKeyPair(final String path, final KeyPair keyPair) throws IOException {
    final PrivateKey privateKey = keyPair.getPrivate();
    final PublicKey publicKey = keyPair.getPublic();

    // Store Public Key.
    final X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey.getEncoded());
    try (final FileOutputStream fos = new FileOutputStream(path + "/public.key")) {
      fos.write(x509EncodedKeySpec.getEncoded());
    }

    // Store Private Key.
    final PKCS8EncodedKeySpec pkcs8EncodedKeySpec =
        new PKCS8EncodedKeySpec(privateKey.getEncoded());
    try (final FileOutputStream fos = new FileOutputStream(path + "/private.key")) {
      fos.write(pkcs8EncodedKeySpec.getEncoded());
    }
  }

  /**
   * Returns the comparator.
   *
   * @param sortColumns the sort columns
   * @return the comparator
   */
  @SuppressWarnings("unused")
  private Comparator<String> getComparator(final int[] sortColumns) {
    return new Comparator<String>() {

      /* see superclass */
      @Override
      public int compare(final String s1, final String s2) {
        final String[] v1 = s1.split("\\|");
        final String[] v2 = s2.split("\\|");
        for (final int sortColumn : sortColumns) {
          final int cmp = v1[sortColumn].compareTo(v2[sortColumn]);
          if (cmp != 0) {
            return cmp;
          }
        }
        return 0;
      }
    };
  }

  /**
   * To SHA 1.
   *
   * @param convertme the convertme
   * @return the string
   * @throws Exception the exception
   */
  public static String sha1Base64(final byte[] convertme) throws Exception {
    return Base64.getEncoder()
        .encodeToString((MessageDigest.getInstance("SHA-1").digest(convertme)));
  }

  /**
   * Compare null safe.
   *
   * @param a the a
   * @param b the b
   * @return true, if successful
   */
  public static boolean equalsNullSafe(final Object a, final Object b) {
    if ((a == null && b != null) || (a != null && b == null)) {
      return false;
    }
    return (a == null && b == null) || (a != null && a.equals(b));
  }

  /**
   * Equals null match.
   *
   * @param a the a
   * @param b the b
   * @return true, if successful
   */
  public static boolean equalsNullMatch(final Object a, final Object b) {
    if ((a == null && b != null) || (a != null && b == null)) {
      return true;
    }
    return (a == null && b == null) || (a != null && a.equals(b));
  }

  /**
   * First not null.
   *
   * @param <T> the generic type parameter
   * @param values the values
   * @return the first non-null value
   */
  @SafeVarargs
  public static <T> T firstNotNull(final T... values) {
    for (final T t : values) {
      if (t != null) {
        return t;
      }
    }
    return null;
  }

  /**
   * Merge.
   *
   * @param <T> the generic type parameter
   * @param a the first object
   * @param b the second object
   * @return the merged object
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  public static <T> T merge(final T a, final T b) throws Exception {
    final Class<T> classa = (Class<T>) a.getClass();
    final T copy = classa.getConstructor(new Class<?>[] {
        b.getClass()
    }).newInstance(new Object[] {
        b
    });
    for (final Method getMethod : classa.getMethods()) {
      if (getMethod.getName().startsWith("get") && getMethod.getParameterTypes().length == 0
          && !getMethod.getName().equals("getClass")) {

        final Method setMethod =
            classa.getMethod(getMethod.getName().replaceFirst("get", "set"), new Class<?>[] {
                getMethod.getReturnType()
            });
        // If the copy has a null value for this field,
        final Object ob = getMethod.invoke(copy, new Object[] {});
        final Object oa = getMethod.invoke(a, new Object[] {});
        if (ob == null && oa != null) {
          setMethod.invoke(copy, new Object[] {
              oa
          });
        }
      }
    }
    return copy;
  }

  /**
   * Count null fields.
   *
   * @param o the o
   * @return the int
   * @throws Exception the exception
   */
  public static int countNullFields(final Object o) throws Exception {
    final Class<?> classa = o.getClass();
    int ct = 0;
    for (final Method getMethod : classa.getMethods()) {
      if (getMethod.getName().startsWith("get") && getMethod.getParameterTypes().length == 0
          && !getMethod.getName().equals("getClass")) {

        final Object oa = getMethod.invoke(o, new Object[] {});
        if (oa == null) {
          ct++;
        }
      }
    }
    return ct;
  }

  /**
   * As map.
   *
   * @param values the values
   * @return the map
   */
  public static Map<String, String> asMap(final String... values) {
    final Map<String, String> map = new HashMap<>();
    if (values.length % 2 != 0) {
      throw new RuntimeException("Unexpected odd number of parameters");
    }
    for (int i = 0; i < values.length; i += 2) {
      map.put(values[i], values[i + 1]);
    }
    return map;
  }

  /**
   * Convert array to List.
   *
   * @param values the string array
   * @return the list of strings
   * @throws Exception if error occurs
   */
  public static List<String> asList(final String[] values) throws Exception {
    return new ArrayList<>(Arrays.asList(values));
  }

  /**
   * Convert varargs to List.
   *
   * @param <T> the generic type parameter
   * @param values the values
   * @return the list
   */
  @SafeVarargs
  public static <T> List<T> asList(final T... values) {
    final List<T> list = new ArrayList<>(values.length);
    for (final T value : values) {
      if (value != null) {
        list.add(value);
      }
    }
    return list;
  }

  /**
   * Convert varargs to Set.
   *
   * @param <T> the generic type parameter
   * @param values the values
   * @return the set
   */
  @SafeVarargs
  public static <T> Set<T> asSet(final T... values) {
    final Set<T> set = new HashSet<>(values.length);
    for (final T value : values) {
      if (value != null) {
        set.add(value);
      }
    }
    return set;
  }

  /**
   * Convert string array to Set.
   *
   * @param values the string array
   * @return the set of strings
   */
  public static Set<String> asSet(final String[] values) {
    return new HashSet<>(Arrays.asList(values));
  }

  /**
   * Indicates whether or not valid date is the case.
   *
   * @param now the now
   * @param date the date
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public static boolean isValidDate(final Date now, final Date date) {
    if (date == null) {
      return false;
    }
    // Not in the future
    final boolean future = date.after(now);
    // Not before Jan 1 1970
    final boolean past = date.before(new Date(0));
    // Not within 5 seconds before "now"
    // final boolean nowish = Math.abs(now.getTime() - date.getTime()) < 5000;
    // Not "today"
    final boolean today = DateUtils.truncate(now, Calendar.DAY_OF_MONTH)
        .equals(DateUtils.truncate(date, Calendar.DAY_OF_MONTH));
    return !future && !past && !today;
  }

  /**
   * Returns the type.
   *
   * @param interfaceType the interface type
   * @return the type
   * @throws Exception the exception
   */
  public static Class<?> getType(final String interfaceType) throws Exception {
    return Class.forName("com.wci.model.jpa."
        + (interfaceType.endsWith("Jpa") ? interfaceType : interfaceType + "Jpa"));
  }

  /**
   * Returns the type.
   *
   * @param classType the class type
   * @param packageName the package name
   * @return the type
   * @throws Exception the exception
   */
  public static Class<?> getType(final String classType, final String packageName)
    throws Exception {
    return Class
        .forName(packageName + "." + (classType.endsWith("Jpa") ? classType : classType + "Jpa"));
  }

  /**
   * Returns the base filename.
   *
   * @param filename the filename
   * @return the base filename
   */
  public static String getBaseFilename(final String filename) {
    // The \\\\$ is to handle regex based filenames for the doc service
    // derivative stuff
    return filename.replaceAll("(.*)\\.[A-Za-z0-9]+$", "$1").replaceAll("\\\\$", "");
  }

  /**
   * Returns the file extension.
   *
   * @param filename the filename
   * @return the file extension
   */
  public static String getFileExtension(final String filename) {
    if (filename.matches(".*\\.[A-Za-z0-9]+$")) {
      return filename.replaceAll(".*\\.([A-Za-z0-9]+)$", "$1");
    } else {
      return "";
    }
  }

  /**
   * As internal url.
   *
   * @param service the service
   * @param url the url
   * @return the string
   * @throws Exception the exception
   */
  public static String asInternalUrl(final String service, final String url) throws Exception {
    final String internalUrl =
        ConfigUtility.getConfigProperties().getProperty("api.url." + service);
    final String apiUrl = ConfigUtility.getConfigProperties().getProperty("api.url");
    if (internalUrl == null || apiUrl == null) {
      logger.error("Unexpected null internal or api url = " + internalUrl + ", " + apiUrl);
      return url;
    }
    return url.replaceFirst(Pattern.quote(apiUrl), internalUrl);
  }

  /**
   * As external url.
   *
   * @param service the service
   * @param url the url
   * @return the string
   * @throws Exception the exception
   */
  public static String asExternalUrl(final String service, final String url) throws Exception {
    final String internalUrl =
        ConfigUtility.getConfigProperties().getProperty("api.url." + service);
    final String apiUrl = ConfigUtility.getConfigProperties().getProperty("api.url");
    if (internalUrl == null || apiUrl == null) {
      logger.error("Unexpected null internal or api url = " + internalUrl + ", " + apiUrl);
      return url;
    }
    return url.replaceFirst(Pattern.quote(internalUrl), apiUrl);
  }

  /**
   * Indicates whether or not test mode is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   * @throws Exception the exception
   */
  public static boolean isTestMode() throws Exception {
    return testMode;
  }

  /**
   * Indicates whether or not cicd test mode is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public static boolean isCicdTestMode() {
    return System.getProperties().containsKey("cicd.rewrite");
  }

  /**
   * Returns the cicd rewrite.
   *
   * @return the cicd rewrite
   */
  public static String getCicdRewrite() {
    return System.getProperties().get("cicd.rewrite").toString();
  }

  /**
   * Sets the test mode.
   *
   * @param testMode the test mode
   */
  public static void setTestMode(final boolean testMode) {
    ConfigUtility.testMode = testMode;
  }

  /**
   * Returns the time zone offset.
   *
   * @param tz the tz
   * @param date the date
   * @return the time zone offset
   */
  public static String getTimeZoneOffset(final String tz, final Date date) {

    if ("EDT".equals(tz)) {
      return "-04:00";
    } else if ("MDT".equals(tz)) {
      return "-06:00";
    } else if ("CDT".equals(tz)) {
      return "-05:00";
    } else if ("PDT".equals(tz)) {
      return "-07:00";
    } else if ("PDT".equals(tz)) {
      return "-07:00";
    }
    final Instant instant = date == null ? Instant.now() : date.toInstant();
    // First try this style
    try {
      return ZoneId.of(tz).getRules().getOffset(instant).getId();
    } catch (final Exception e) {
      // n/a
    }
    try {
      return ZoneOffset.of(tz).getId();
    } catch (final Exception e) {
      // n/a
    }
    try {
      final String x = TimeZone.getTimeZone(tz).toZoneId().getRules().getOffset(instant).getId();
      if ("Z".equals(x)) {
        return ZoneId.of(tz, zoneMap).getRules().getOffset(instant).getId();
      }
      if (x != null) {
        return x;
      }
    } catch (final Exception e) {
      // n/a
    }
    final String y = ZoneId.of("America/Los_Angeles").getRules().getOffset(instant).getId();
    logger.warn("    REVERTING to default time zone " + y + " = " + tz);
    return y;
  }

  /**
   * Builds the api url.
   *
   * @param path the path
   * @return the string
   * @throws Exception the exception
   */
  public static String buildApiUrl(final String path) throws Exception {
    return ConfigUtility.getConfigProperties().getProperty("api.url") + path;
  }

  /**
   * Builds the api url.
   *
   * @param service the service
   * @param path the path
   * @return the string
   * @throws Exception the exception
   */
  public static String buildApiUrl(final String service, final String path) throws Exception {
    return ConfigUtility.getConfigProperties().getProperty("api.url.wci-" + service + "-service")
        + path;
  }

  /**
   * Resolve uri.
   *
   * @param uri the uri
   * @return the string
   * @throws Exception the exception
   */
  public static String resolveUri(final String uri) throws Exception {

    if (uri.startsWith("classpath:")) {
      final String fixuri = uri.replaceFirst("classpath:", "");
      try (final InputStream in = ConfigUtility.class.getClassLoader()
          .getResourceAsStream(uri.replaceFirst("classpath:", ""));) {
        if (in == null) {
          throw new Exception("UNABLE to find classpath uri = " + fixuri);
        }
        return IOUtils.toString(in, StandardCharsets.UTF_8);
      }
    } else {
      try (final InputStream is = new BufferedInputStream(new URL(uri).openStream());) {
        return IOUtils.toString(is, StandardCharsets.UTF_8);
      }
    }
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
   * Replace first word.
   *
   * @param string the string
   * @param word the word
   * @return true, if successful
   */
  public static String replaceFirstWord(final String string, final String word) {
    return string.replaceFirst("(?i:(^|\\b)" + word + "(\\b|$))", "").replaceFirst("  ", " ")
        .replaceFirst(", ,", ",").replaceFirst(" $", "").replaceFirst("^ ", "")
        .replaceFirst(",$", "").replaceFirst("^, ", "");
  }

  /**
   * Returns the stack trace.
   *
   * @param t the t
   * @return the stack trace
   */
  public static String getStackTrace(final Throwable t) {
    return getStackTrace(t, false);
  }

  /**
   * Returns the stack trace.
   *
   * @param t the t
   * @param wciFlag the wci flag
   * @return the stack trace
   */
  public static String getStackTrace(final Throwable t, final boolean wciFlag) {
    final String stackTrace = ExceptionUtils.getStackTrace(t);
    if (!wciFlag) {
      return stackTrace;
    }
    final String[] lines = FieldedStringTokenizer.split(stackTrace, "\n");
    final StringBuilder sb = new StringBuilder();
    boolean found = false;
    for (final String line : lines) {
      final boolean wciLine = line.contains("com.wci.");
      if (wciLine) {
        found = true;
      }
      // if found and not a wci line, stop
      if (found && !wciLine) {
        sb.append("\t...");
        break;
      }
      sb.append(line.replaceAll("\r", "")).append("\n");
    }
    return sb.toString();
  }

  /**
   * Returns the header token.
   *
   * @return the header token
   * @throws Exception the exception
   */
  public static String getHeaderToken() throws Exception {
    return config.getProperty("jwt.header") != null ? config.getProperty("jwt.header")
        : "X-Wci-Token";
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
   * Returns the map.
   *
   * @param path the path
   * @param delim the delim
   * @return the map
   * @throws Exception the exception
   */
  public static Map<String, String> getMap(final String path, final String delim) throws Exception {
    // Load as an "in order" map
    final Map<String, String> map = new LinkedHashMap<>();

    if (getLocalResourceFile(path) != null) {

      try (final InputStream in = ConfigUtility.class.getClassLoader().getResourceAsStream(path)) {
        for (final String line : IOUtils.readLines(in, StandardCharsets.UTF_8)) {
          if (line.isEmpty() || line.startsWith("#")) {
            continue;
          }
          final String[] tokens = FieldedStringTokenizer.split(line, delim);
          if (tokens.length != 2) {
            throw new Exception("Badly formatted = " + line);
          }
          map.put(tokens[0], tokens[1]);
        }
      }
    }

    return map;
  }

  /**
   * Returns the reverse map.
   *
   * @param path the path
   * @param delim the delim
   * @return the reverse map
   * @throws Exception the exception
   */
  public static Map<String, String> getReverseMap(final String path, final String delim)
    throws Exception {
    // Load as an "in order" map
    final Map<String, String> map = new LinkedHashMap<>();

    if (getLocalResourceFile(path) != null) {

      try (final InputStream in = ConfigUtility.class.getClassLoader().getResourceAsStream(path)) {
        for (final String line : IOUtils.readLines(in, StandardCharsets.UTF_8)) {
          if (line.isEmpty() || line.startsWith("#")) {
            continue;
          }
          final String[] tokens = FieldedStringTokenizer.split(line, delim);
          if (tokens.length != 2) {
            throw new Exception("Badly formatted = " + line);
          }
          map.put(tokens[1], tokens[0]);
        }
      }
    }

    return map;
  }

  /**
   * Returns the name from class by stripping package and putting spaces where CamelCase is used.
   *
   * @param clazz the clazz
   * @return the name from class
   */
  public static String getNameFromClass(final Class<?> clazz) {
    return clazz.getName().substring(clazz.getName().lastIndexOf('.') + 1)
        .replaceAll(String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])",
            "(?<=[A-Za-z])(?=[^A-Za-z])"), " ");
  }

}

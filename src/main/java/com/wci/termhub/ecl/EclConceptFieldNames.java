/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.ecl;

/**
 * The Class EclConceptFieldNames.
 */
public final class EclConceptFieldNames {

  /**
   * Instantiates an empty {@link EclConceptFieldNames}.
   */
  private EclConceptFieldNames() {
    // n/a
  }

  /** The id. */
  public static final String ID = "code";

  /** The internal id. */
  public static final String INTERNAL_ID = "id";

  /** THe name. */
  public static final String NAME = "name";

  /** The ancestor. */
  public static final String ANCESTOR = "ancestor";

  /** The Constant PARENT. */
  public static final String PARENT = "parent";

  /** The member of. */
  public static final String MEMBER_OF = "memberOf";

  /** The Constant MAPPING. */
  public static final String MAPPING = "MAPPING";

  /**
   * Indicates whether or not relationship ecl is the case.
   *
   * @param clause the clause
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public static boolean isRelationshipEcl(final String clause) {
    final String key = clause.replaceFirst("=.*", "");
    return !key.equals(ID) && !key.equals(INTERNAL_ID) && !key.equals(NAME) && !key.equals(ANCESTOR)
        && !key.equals(PARENT) && !key.equals(MEMBER_OF) && !key.equals(MAPPING);
  }
}

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

import java.net.URL;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.lang3.EnumUtils;
import org.apache.logging.log4j.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.wci.termhub.model.AuthContext;
import com.wci.termhub.model.AuthResponse;
import com.wci.termhub.model.enums.UserRole;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.WebApplicationException;

/**
 * Utility for interacting with JWT tokens.
 */
public final class JwtUtility {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(JwtUtility.class);

  /** The Constant jwks. */
  private static Map<String, PublicKey> keyMap = new HashMap<>();

  /** The Constant DAY. */
  private static final int DAY = 60 * 60 * 24;

  /** The Constant MAX_EXPIRES_IN. */
  private static final int MAX_EXPIRES_IN = 30 * DAY;

  /** The admin user id. */
  public static final String ADMIN_USER_ID = new UUID(1, 2).toString();

  /**
   * Instantiates an empty {@link ConfigUtility}.
   */
  private JwtUtility() {
    // n/a
  }

  /**
   * Inits the thread context.
   *
   * @param label the label
   */
  public static void initThreadContext(final String label) {
    ThreadContext.put("user-id", label);
    ThreadContext.put("correlation-id", label);
  }

  /**
   * Returns the expires in.
   *
   * @param role the role
   * @return the expires in
   */
  public static long getExpiresIn(final String role) {
    if (role == null) {
      return getExpiresIn(4);
    }
    // see mockAccountJwt
    else if (role.equals("ACCOUNT")) {
      return getExpiresIn(2);
    }
    // see mockRefreshJwt
    else if (role.equals("REFRESH") || role.equals("ADMIN")) {
      return getExpiresIn(48);
    }
    // USER, VIEWER...
    return getExpiresIn(4);
  }

  /**
   * Returns the expires in.
   *
   * @param hours the hours
   * @return the expires in
   */
  public static long getExpiresIn(final int hours) {
    // 4 hour expiration
    return hours * 60 * 60;
  }

  /**
   * Returns the local jwt.
   *
   * @return the local jwt
   */
  public static String getLocalJwt() {
    return ThreadContext.get("jwt");
  }

  /**
   * Sets the local jwt.
   *
   * @param jwt the jwt
   */
  public static void setLocalJwt(final String jwt) {
    ThreadContext.put("jwt", jwt);
  }

  /**
   * Verify.
   *
   * @param jwt the jwt
   * @throws Exception the exception
   */
  public static void verify(final DecodedJWT jwt) throws Exception {
    verify(jwt, true);
  }

  /**
   * Verfiy the decoded jwt with or without a signature check.
   *
   * @param djwt the djwt
   * @param checkSignature the check signature
   * @throws Exception the exception
   */
  public static void verify(final DecodedJWT djwt, final boolean checkSignature) throws Exception {
    final Properties prop = PropertyUtility.getProperties();
    final String secret = prop.getProperty("jwt.secret");
    if (secret == null) {
      throw new LocalException(
          "Unexpected missing property from application.properties = jwt.secret");
    }
    final String audience = getAudience();
    if (audience == null) {
      throw new LocalException("Unexpected missing audience");
    }
    final Map<String, Claim> claims = djwt.getClaims();

    // Service tokens don't have id claims
    final String userId = getUserId(claims);
    if (userId == null) {
      if (getRole(claims) == null) {
        // Log the error because authorize is called before "try"
        // block
        logger.error("Non-admin JWT unexpectedly missing ID claim = " + ModelUtility.toJson(claims),
            HttpServletResponse.SC_UNAUTHORIZED);
        throw new WebApplicationException("Non-admin JWT unexpectedly missing ID claim");
      }
    } else {
      if (!"admin".equals(userId)
          && !userId.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) {
        logger.error("JWT ID claim is unexpectedly not a valid UUID/admin= " + userId);
        throw new WebApplicationException(
            "JWT ID claim is unexpectedly not a valid UUID/admin = " + userId,
            HttpServletResponse.SC_UNAUTHORIZED);
      }
    }

    // final String aud = djwt.getAudience().get(0);
    // if (!aud.contains(getAudience())) {
    // logger.error("JWT bad audience = " + aud + ", " + getAudience());
    // throw new WebApplicationException("JWT bad audience = " + aud,
    // HttpServletResponse.);
    // }

    final Algorithm algorithm = Algorithm.HMAC256(secret);
    // Reusable verifier instance
    final JWTVerifier verifier = JWT.require(algorithm).acceptLeeway(1).acceptExpiresAt(5)
        .withAudience(getAudience()).build();

    verifier.verify(djwt.getToken());

  }

  /**
   * Verify jwks.
   *
   * @param djwt the djwt
   * @param url the url
   * @throws Exception the exception
   */
  public static void verifyJwks(final DecodedJWT djwt, final String url) throws Exception {
    PublicKey publicKey = null;
    // Check the cache
    if (keyMap.containsKey(url)) {
      publicKey = keyMap.get(url);
    }
    // Otherwise, get the public key again
    else {
      publicKey = getPublicKey(djwt, url);
      logger.info("  add public key cache entry for = " + url);
      keyMap.put(url, publicKey);
    }
    final Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) publicKey, null);
    try {
      algorithm.verify(djwt);
    } catch (final Exception e) {
      // NOTE; if they've switched keys at the URL, this key may no longer
      // be
      // valid in that case, clear the map for this url and try again

      // Get public key again
      final PublicKey publicKey2 = getPublicKey(djwt, url);
      // If not the same, try again
      if (!publicKey.equals(publicKey2)) {
        keyMap.remove(url);
        logger.info("  remove public key cache entry for = " + url);
        publicKey = publicKey2;
        logger.info("  add public key cache entry for = " + url);
        keyMap.put(url, publicKey);
        // Try a second time (if this fails, throw the exception)
        algorithm.verify(djwt);
      }
      // Otherwise, throw the exception
      else {
        logger.info("  public key matches = " + url);
        throw e;
      }
    }

    // Check expiration
    if (djwt.getExpiresAt().before(Calendar.getInstance().getTime())) {
      throw new Exception("Expired token = " + djwt.getExpiresAt());
    }

  }

  /**
   * Returns the public key.
   *
   * @param djwt the djwt
   * @param url the url
   * @return the public key
   * @throws Exception the exception
   */
  private static PublicKey getPublicKey(final DecodedJWT djwt, final String url) throws Exception {
    final UrlJwkProvider provider = new UrlJwkProvider(new URL(url), null, null);
    Jwk jwk = null;
    // IF "kid" is specified in JWT, use it
    if (djwt.getKeyId() != null) {
      jwk = provider.get(djwt.getKeyId());
    }
    // If otherwise there is just a single RSA key, use that
    else if (provider.getAll().stream().filter(k -> k.getType().equals("RSA")).count() == 1) {
      jwk = provider.getAll().stream().filter(k -> k.getType().equals("RSA")).findFirst().get();
    }
    // Otherwise, fail with "unexpected condition"
    else {
      throw new Exception("Expecting either JWT to specify a kid OR "
          + "provider URL to have exactly one RSA key = " + djwt.getKeyId() + ", "
          + provider.getAll().stream().filter(k -> k.getType().equals("RSA")).count());
    }
    return jwk.getPublicKey();
  }

  /**
   * Returns the wci id.
   *
   * @param claims the claims
   * @return the wci id
   * @throws Exception the exception
   */
  public static String getUserId(final Map<String, Claim> claims) throws Exception {
    final Claim claim = claims.get(Claims.ID.getValue());
    return claim == null ? null : claim.asString();
  }

  /**
   * Gets the project id.
   *
   * @param claims the claims
   * @return the project id
   * @throws Exception the exception
   */
  public static String getProjectId(final Map<String, Claim> claims) throws Exception {
    final Claim claim = claims.get(Claims.PROJECT_ID.getValue());
    return claim == null ? null : claim.asString();
  }

  /**
   * Gets the org id.
   *
   * @param claims the claims
   * @return the org id
   * @throws Exception the exception
   */
  public static String getOrgId(final Map<String, Claim> claims) throws Exception {
    final Claim claim = claims.get(Claims.ORG_ID.getValue());
    return claim == null ? null : claim.asString();
  }

  /**
   * Returns the role.
   *
   * @param claims the claims
   * @return the role
   * @throws Exception the exception
   */
  public static String getRole(final Map<String, Claim> claims) throws Exception {
    final Claim claim = claims.get(Claims.ROLE.getValue());
    return claim == null ? null : claim.asString();
  }

  /**
   * Gets the plan.
   *
   * @param claims the claims
   * @return the plan
   * @throws Exception the exception
   */
  public static String getPlan(final Map<String, Claim> claims) throws Exception {
    final Claim claim = claims.get(Claims.PLAN.getValue());
    return claim == null ? null : claim.asString();
  }

  /**
   * Returns the payload.
   *
   * @param djwt the djwt
   * @return the payload
   * @throws Exception the exception
   */
  public static Map<String, String> getPayload(final DecodedJWT djwt) throws Exception {
    final Map<String, String> map = new HashMap<>();
    for (final Map.Entry<String, Claim> entry : djwt.getClaims().entrySet()) {
      if (entry.getValue().asString() != null) {
        map.put(entry.getKey(), entry.getValue().asString());
      }
    }
    if (djwt.getIssuedAt() != null) {
      map.put("iat", djwt.getIssuedAt().toString());
    }
    if (djwt.getExpiresAt() != null) {
      map.put("exp", djwt.getExpiresAt().toString());
    }
    return map;
  }

  /**
   * Returns the expiration.
   *
   * @param djwt the djwt
   * @return the expiration
   */
  public static Date getExpiration(final DecodedJWT djwt) {
    return djwt.getExpiresAt();
  }

  /**
   * Mock jwt.
   *
   * @param userId the wci id
   * @return the string
   * @throws Exception the exception
   */
  public static String mockJwt(final String userId) throws Exception {
    final Properties prop = PropertyUtility.getProperties();
    return mockJwt(userId, null, null, prop.getProperty("jwt.secret"));
  }

  /**
   * Mock refresh jwt.
   *
   * @param userId the user id
   * @return the string
   * @throws Exception the exception
   */
  public static String mockRefreshJwt(final String userId) throws Exception {
    final Properties prop = PropertyUtility.getProperties();
    return mockJwt(userId, null, "REFRESH", prop.getProperty("jwt.secret"));
  }

  /**
   * Mock account jwt. Used for short-window timeout tokens used in automated
   * emails (like password reset).
   *
   * @param userId the user id
   * @return the string
   * @throws Exception the exception
   */
  public static String mockAccountJwt(final String userId) throws Exception {
    final Properties prop = PropertyUtility.getProperties();
    return mockJwt(userId, null, "ACCOUNT", prop.getProperty("jwt.secret"));
  }

  /**
   * Mock project jwt.
   *
   * @param projectId the project id
   * @param organizationId the organization id
   * @return the string
   * @throws Exception the exception
   */
  public static String mockProjectJwt(final String projectId, final String organizationId)
    throws Exception {
    final Properties prop = PropertyUtility.getProperties();
    final String secret = prop.getProperty("jwt.secret");
    final String audience = getAudience();
    final String issuer = prop.getProperty("jwt.issuer");

    // The JWT signature algorithm we will be using to sign the token
    final Algorithm algorithm = Algorithm.HMAC256(secret);
    final Builder builder = JWT.create().withAudience(audience).withIssuer(issuer)
        // Later: replace salt with expiration date when we're ready to handle
        // that
        .withClaim(Claims.SALT.getValue(), StringUtility.randomString(8))
        .withClaim(Claims.PROJECT_ID.getValue(), projectId)
        .withClaim(Claims.ORG_ID.getValue(), organizationId)
        .withClaim(Claims.ROLE.getValue(), "PROJECT");
    return builder.sign(algorithm);

  }

  /**
   * Mock jwt.
   *
   * @param userId the wci id
   * @param plan the plan
   * @param role the role
   * @return the string
   * @throws Exception the exception
   */
  public static String mockJwt(final String userId, final String plan, final String role)
    throws Exception {
    final Properties prop = PropertyUtility.getProperties();
    return mockJwt(userId, plan, role, prop.getProperty("jwt.secret"));
  }

  /**
   * Mock jwt.
   *
   * @param userId the wci id
   * @param plan the plan
   * @param role the role
   * @param jwtSecret the jwt secret
   * @return the string
   * @throws Exception the exception
   */
  public static String mockJwt(final String userId, final String plan, final String role,
    final String jwtSecret) throws Exception {

    final Properties prop = PropertyUtility.getProperties();
    final String secret = jwtSecret;

    if (secret == null) {
      throw new LocalException(
          "Unexpected missing property from application.properties = jwt.secret");
    }
    final String audience = getAudience();
    if (audience == null) {
      throw new LocalException("Unexpected missing audience");
    }
    final String issuer = prop.getProperty("jwt.issuer");
    if (issuer == null) {
      throw new LocalException(
          "Unexpected missing property from application.properties = jwt.issuer");
    }
    final String idClaimId = Claims.ID.getValue();
    final String planClaimId = Claims.PLAN.getValue();
    final String roleClaimId = Claims.ROLE.getValue();

    // The JWT signature algorithm we will be using to sign the token
    final Algorithm algorithm = Algorithm.HMAC256(secret);
    final Date now = new Date();

    // Set claims
    // .withIssuedAt(now)
    final Builder builder =
        JWT.create().withAudience(audience).withIssuer(issuer).withClaim(idClaimId, userId)
            .withExpiresAt(new Date((getExpiresIn(role) * 1000) + now.getTime()));

    if (plan != null) {
      builder.withClaim(planClaimId, plan);
    }
    if (role != null) {
      builder.withClaim(roleClaimId, role);
    }

    // Builds the JWT and serializes it to a compact, URL-safe string
    return builder.sign(algorithm);
  }

  /**
   * Mock jwt.
   *
   * @param userId the user id
   * @param plan the plan
   * @param role the role
   * @param expiresIn the expires in
   * @param claims the claims
   * @return the string
   * @throws Exception the exception
   */
  public static String mockJwt(final String userId, final String plan, final String role,
    final Integer expiresIn, final Map<String, String> claims) throws Exception {

    final Properties prop = PropertyUtility.getProperties();
    final String secret = prop.getProperty("jwt.secret");

    if (secret == null) {
      throw new LocalException("Unexpected missing property from config.properties = jwt.secret");
    }
    final String audience = getAudience();
    if (audience == null) {
      throw new LocalException("Unexpected missing audience");
    }
    final String issuer = prop.getProperty("jwt.issuer");
    if (issuer == null) {
      throw new LocalException("Unexpected missing property from config.properties = jwt.issuer");
    }
    final String customClaimId = Claims.ID.getValue();
    if (customClaimId == null) {
      throw new LocalException(
          "Unexpected missing property from config.properties = jwt.customClaimId");
    }
    final String idClaimId = Claims.ID.getValue();
    final String roleClaimId = Claims.ROLE.getValue();
    final String planClaimId = Claims.PLAN.getValue();

    // The JWT signature algorithm we will be using to sign the token
    final Algorithm algorithm = Algorithm.HMAC256(secret);
    final Date now = new Date();

    // Set claims
    // .withIssuedAt(now)
    final Builder builder =
        JWT.create().withAudience(audience).withIssuer(issuer).withClaim(idClaimId, userId)
            .withExpiresAt(new Date((getExpiresIn(role) * 1000) + now.getTime()));

    if (plan != null) {
      builder.withClaim(planClaimId, plan);
    }
    builder.withClaim(roleClaimId, role);

    if (claims != null) {
      // Check claims not allowed to be overridden
      for (final String claim : getImmutableClaims()) {
        if (claims.containsKey(claim)) {
          throw new Exception("Proxy auth unable to overwrite claim = " + claim);
        }
      }
      for (final Map.Entry<String, String> claim : claims.entrySet()) {
        builder.withClaim(claim.getKey(), claim.getValue());
      }
    }
    // if it has been specified, let's add the expiration - 24 hours
    if (expiresIn != null && expiresIn > MAX_EXPIRES_IN) {
      throw new Exception("expiresIn is greater than max allowed value = " + expiresIn);
    }

    // Builds the JWT and serializes it to a compact, URL-safe string
    return builder.sign(algorithm);

  }

  /**
   * Returns the immutable claims.
   *
   * @return the immutable claims
   */
  public static String[] getImmutableClaims() {
    return new String[] {
        "iad", "aud", "iss", "exp"
    };
  }

  /**
   * Rewrite jwt.
   *
   * @param jwt the jwt
   * @param jwtSecret the jwt secret
   * @param expires the expires
   * @return the string
   * @throws Exception the exception
   */
  public static String rewriteJwt(final String jwt, final String jwtSecret, final boolean expires)
    throws Exception {
    // {
    // "https://wciinformatics.com/id":
    // "ff9c8eaa-fa50-4b51-95e1-722427e73e4f",
    // "iss": "https://wci.auth0.com/",
    // "sub": "google-oauth2|114233355066920694392",
    // "aud": "fFiSN82LmwTyy1aq3tzDdwYiKxQ8IrR5",
    // "iat": 1522871417,
    // "exp": 1522907417
    // }

    final String secret = jwtSecret;
    if (secret == null) {
      throw new LocalException(
          "Unexpected missing property from application.properties = jwt.secret");
    }

    // The JWT signature algorithm we will be using to sign the token
    final Algorithm algorithm = Algorithm.HMAC256(secret);
    final DecodedJWT djwt = JWT.decode(jwt);

    // Set claims
    final Builder builder = JWT.create();
    if (djwt.getIssuer() != null) {
      builder.withIssuer(djwt.getIssuer());
    }
    if (djwt.getIssuedAt() != null) {
      builder.withIssuedAt(djwt.getIssuedAt());
    }
    if (expires && djwt.getExpiresAt() != null) {
      builder.withExpiresAt(djwt.getExpiresAt());
    }
    if (djwt.getAudience() != null && !djwt.getAudience().isEmpty()) {
      for (final String aud : djwt.getAudience()) {
        builder.withAudience(aud);
      }
    }
    for (final Map.Entry<String, Claim> entry : djwt.getClaims().entrySet()) {
      if (entry.getValue().asString() != null) {
        builder.withClaim(entry.getKey(), entry.getValue().asString());
      }
    }

    // Builds the JWT and serializes it to a compact, URL-safe string
    return builder.sign(algorithm);
  }

  /**
   * Rewrite jwt no expiration.
   *
   * @param jwt the jwt
   * @return the string
   * @throws Exception the exception
   */
  public static String rewriteJwtNoExpiration(final String jwt) throws Exception {
    return rewriteJwt(jwt, PropertyUtility.getProperties().getProperty("jwt.secret"), true);
  }

  /**
   * Mock service jwt.
   *
   * @return the string
   * @throws Exception the exception
   */
  public static String mockAdminJwt() throws Exception {
    final Properties prop = PropertyUtility.getProperties();
    return mockJwt(ADMIN_USER_ID, "UNLIMITED", "ADMIN", prop.getProperty("jwt.secret"));
  }

  /**
   * Mock expired jwt.
   *
   * @param userId the wci id
   * @return the string
   * @throws Exception the exception
   */
  public static String mockExpiredJwt(final String userId) throws Exception {
    // {
    // "https://wciinformatics.com/id":
    // "ff9c8eaa-fa50-4b51-95e1-722427e73e4f",
    // "iss": "https://wci.auth0.com/",
    // "sub": "google-oauth2|114233355066920694392",
    // "aud": "fFiSN82LmwTyy1aq3tzDdwYiKxQ8IrR5",
    // "iat": 1522871417,
    // "exp": 1522907417
    // }

    final Properties prop = PropertyUtility.getProperties();
    final String secret = prop.getProperty("jwt.secret");

    if (secret == null) {
      throw new LocalException(
          "Unexpected missing property from application.properties = jwt.secret");
    }
    final String audience = getAudience();
    if (audience == null) {
      throw new LocalException("Unexpected missing audience");
    }
    final String issuer = prop.getProperty("jwt.issuer");
    if (issuer == null) {
      throw new LocalException(
          "Unexpected missing property from application.properties = jwt.issuer");
    }

    // The JWT signature algorithm we will be using to sign the token
    final Algorithm algorithm = Algorithm.HMAC256(secret);
    final Date now = new Date();

    // Set claims
    final String idClaimId = Claims.ID.getValue();
    final Builder builder = JWT.create().withIssuedAt(now).withAudience(audience).withIssuer(issuer)
        .withClaim(idClaimId, userId).withExpiresAt(new Date(now.getTime() - 3600000));

    // Builds the JWT and serializes it to a compact, URL-safe string
    return builder.sign(algorithm);

  }

  /**
   * Proxy auth.
   *
   * @param context the context
   * @param expiresIn the expires in
   * @param claims the claims
   * @return the auth response
   * @throws Exception the exception
   */
  public static AuthResponse proxyAuth(final AuthContext context, final Integer expiresIn,
    final Map<String, String> claims) throws Exception {
    final Map<String, String> lclaims = claims == null ? new HashMap<>() : claims;

    if (expiresIn != null && expiresIn > (30 * 24 * 3600)) {
      throw new WebApplicationException(
          "expiresIn is greater than max allowed value = " + expiresIn);
    }

    if (expiresIn != null && expiresIn < -1) {
      throw new WebApplicationException("expiresIn is less than min value allowed = " + expiresIn);
    }

    // Check claims not allowed to be overridden
    for (final String claim : getImmutableClaims()) {
      if (lclaims.containsKey(claim)) {
        throw new WebApplicationException("Proxy auth unable to overwrite claim = " + claim,
            HttpServletResponse.SC_EXPECTATION_FAILED);
      }
    }

    // Check ADMIN only claims allowed
    if (!"ADMIN".equals(context.getRole())) {
      for (final String claim : new String[] {
          Claims.PLAN.getValue(), Claims.ID.getValue()
      }) {
        if (lclaims.containsKey(claim)) {
          throw new WebApplicationException("Proxy auth unable to overwrite claim = " + claim,
              HttpServletResponse.SC_EXPECTATION_FAILED);
        }
      }

      // Require ADMIN for role
      final String claim = Claims.ROLE.getValue();
      if (lclaims.containsKey(claim)) {
        if (!"ADMIN".equals(context.getRole())) {
          throw new WebApplicationException("Proxy auth unable to overwrite claim = " + claim,
              HttpServletResponse.SC_EXPECTATION_FAILED);
        }
      }
    }

    // Verify valid role
    String role = UserRole.USER.name();
    if (lclaims.containsKey(Claims.ROLE.getValue())) {
      role = lclaims.get(Claims.ROLE.getValue());
      if (!EnumUtils.isValidEnum(UserRole.class, role)) {
        throw new WebApplicationException(
            "Proxy auth able to overwrite role with illegal value = " + role,
            HttpServletResponse.SC_EXPECTATION_FAILED);
      }
    }

    // Verify user
    String user = context.getUserId();
    if (lclaims.containsKey(Claims.ID.getValue())) {
      user = lclaims.get(Claims.ID.getValue());
    }

    String plan = context.getPlan();
    if (lclaims.containsKey(Claims.PLAN.getValue())) {
      plan = lclaims.get(Claims.PLAN.getValue());
    }

    // Same user, plan/role calculated above, plus claims overrides
    final String jwt = JwtUtility.mockJwt(user, plan, role, expiresIn, lclaims);

    // prepare the response
    final AuthResponse response = new AuthResponse();
    response.setAccessToken(jwt);

    if (expiresIn == null || expiresIn != -1) {
      final DecodedJWT djwt = JWT.decode(jwt);
      final long exp = djwt.getExpiresAt().getTime() / 1000;
      response.setExpiresIn(exp - (new Date().getTime() / 1000));
      response.setExpiresOn(exp);
    }
    response.setTokenType("Bearer");
    return response;
  }

  /**
   * Authorize.
   *
   * @param jwt the jwt
   * @return the auth context
   * @throws Exception the exception
   */
  public static AuthContext authorize(final String jwt) throws Exception {

    final AuthContext context = new AuthContext();
    ThreadContext.put("jwt", jwt);
    context.setJwt(jwt);
    // Decode and verify
    final DecodedJWT djwt = JWT.decode(jwt);
    JwtUtility.verify(djwt);
    context.setClaims(getPayload(djwt));
    final Map<String, Claim> claims = djwt.getClaims();
    context.setUserId(JwtUtility.getUserId(claims));
    context.setPlan(JwtUtility.getPlan(claims));
    context.setRole(JwtUtility.getRole(claims));

    ThreadContext.put("user-id", context.getUserId());
    return context;
  }

  /**
   * Returns the audience.
   *
   * @return the audience
   * @throws Exception the exception
   */
  private static String getAudience() throws Exception {
    return PropertyUtility.getProperties().getProperty("jwt.audience");
  }
}

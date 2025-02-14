/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.solr.common;

import java.util.Map;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.MDC;

/**
 *
 */
public class SolrException extends RuntimeException {

  public static final String ROOT_ERROR_CLASS = "root-error-class";
  public static final String ERROR_CLASS = "error-class";
  final private Map<String, String> mdcContext;

  /**
   * This list of valid HTTP Status error codes that Solr may return in 
   * the case of a "Server Side" error.
   *
   * @since solr 1.2
   */
  public enum ErrorCode {
    BAD_REQUEST( 400 ),
    UNAUTHORIZED( 401 ),
    FORBIDDEN( 403 ),
    NOT_FOUND( 404 ),
    CONFLICT( 409 ),
    UNSUPPORTED_MEDIA_TYPE( 415 ),
    TOO_MANY_REQUESTS(429),
    SERVER_ERROR( 500 ),
    SERVICE_UNAVAILABLE( 503 ),
    INVALID_STATE( 510 ),
    UNKNOWN(0);
    public final int code;
    
    private ErrorCode( int c )
    {
      code = c;
    }
    public static ErrorCode getErrorCode(int c){
      for (ErrorCode err : values()) {
        if(err.code == c) return err;
      }
      return UNKNOWN;
    }
  };

  public SolrException(ErrorCode code, String msg) {
    super(msg);
    this.code = code.code;
    this.mdcContext = MDC.getCopyOfContextMap();
  }
  public SolrException(ErrorCode code, String msg, Throwable th) {
    super(msg, th);
    this.code = code.code;
    this.mdcContext = MDC.getCopyOfContextMap();
  }

  public SolrException(ErrorCode code, Throwable th) {
    super(th);
    this.code = code.code;
    this.mdcContext = MDC.getCopyOfContextMap();
  }

  /**
   * Constructor that can set arbitrary http status code. Not for 
   * use in Solr, but may be used by clients in subclasses to capture 
   * errors returned by the servlet container or other HTTP proxies.
   */
  protected SolrException(int code, String msg, Throwable th) {
    super(msg, th);
    this.code = code;
    this.mdcContext = MDC.getCopyOfContextMap();
  }
  
  int code=0;
  protected NamedList<String> metadata;

  /**
   * The HTTP Status code associated with this Exception.  For SolrExceptions 
   * thrown by Solr "Server Side", this should valid {@link ErrorCode}, 
   * however client side exceptions may contain an arbitrary error code based 
   * on the behavior of the Servlet Container hosting Solr, or any HTTP 
   * Proxies that may exist between the client and the server.
   *
   * @return The HTTP Status code associated with this Exception
   */
  public int code() { return code; }

  public void setMetadata(NamedList<String> metadata) {
    this.metadata = metadata;
  }

  public NamedList<String> getMetadata() {
    return metadata;
  }

  public String getMetadata(String key) {
    return (metadata != null && key != null) ? metadata.get(key) : null;
  }

  public void setMetadata(String key, String value) {
    if (key == null || value == null)
      throw new IllegalArgumentException("Exception metadata cannot be null!");

    if (metadata == null)
      metadata = new NamedList<String>();
    metadata.add(key, value);
  }
  
  public String getThrowable() {
    return getMetadata(ERROR_CLASS);
  }

  public String getRootThrowable() {
    return getMetadata(ROOT_ERROR_CLASS);
  }

  /** 
   * This method was initially created to aid in testing situations that were known to cause ERRORs.  It should no longer be used by any new code.
   *
   * @deprecated Use the Logger directly
   */
  @Deprecated
  public void log(Logger log) {
    log(log,this);
  }
  
  /** 
   * This method was initially created to aid in testing situations that were known to cause ERRORs.  It should no longer be used by any new code.
   *
   * @deprecated Use the Logger directly
   */
  @Deprecated
  public static void log(Logger log, Throwable e) {
    if (log.isErrorEnabled()) {
      log.error(e.toString(), e); // nowarn (we are inside of isErrorEnabled, toString as msg is ok)
    }
  }

  /** 
   * This method was initially created to aid in testing situations that were known to cause ERRORs.  It should no longer be used by any new code.
   *
   * @deprecated Use the Logger directly
   */
  @Deprecated
  public static void log(Logger log, String msg, Throwable e) {
    if (log.isErrorEnabled()) {
      log.error(msg, e);
    }
  }
  
  /** 
   * This method was initially created to aid in testing situations that were known to cause ERRORs.  It should no longer be used by any new code.
   *
   * @deprecated Use the Logger directly
   */
  @Deprecated
  public static void log(Logger log, String msg) {
    if (log.isErrorEnabled()) {
      log.error(msg);
    }
  }

  // TODO: This doesn't handle cause loops
    public static Throwable getRootCause(Throwable t) {
    while (true) {
      Throwable cause = t.getCause();
      if (cause!=null) {
        t = cause;
      } else {
        break;
      }
    }
    return t;
  }

  public void logInfoWithMdc(Logger logger, String msg) {
    Map<String, String> previousMdcContext = MDC.getCopyOfContextMap();
    MDC.setContextMap(mdcContext);
    try {
      logger.info(msg);
    } finally{
      MDC.setContextMap(previousMdcContext);
    }
  }

  public void logDebugWithMdc(Logger logger, String msg) {
    Map<String, String> previousMdcContext = MDC.getCopyOfContextMap();
    MDC.setContextMap(mdcContext);
    try {
      logger.debug(msg);
    } finally{
      MDC.setContextMap(previousMdcContext);
    }
  }

  public void logWarnWithMdc(Logger logger, String msg) {
    Map<String, String> previousMdcContext = MDC.getCopyOfContextMap();
    MDC.setContextMap(mdcContext);
    try {
      logger.warn(msg);
    } finally{
      MDC.setContextMap(previousMdcContext);
    }
  }

}

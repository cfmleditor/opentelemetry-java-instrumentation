/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.acf;

import static java.util.logging.Level.WARNING;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.api.instrumenter.SpanKindExtractor;
import io.opentelemetry.javaagent.bootstrap.internal.ExperimentalConfig;
import io.opentelemetry.javaagent.bootstrap.internal.InstrumentationConfig;
import io.opentelemetry.javaagent.instrumentation.acf.HttpCfmlPageInstrumentationSingletons;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.HttpJspPage;

public class HttpCfmlPageInstrumentationSingletons {
  private static final boolean CAPTURE_EXPERIMENTAL_SPAN_ATTRIBUTES =
      InstrumentationConfig.get()
          .getBoolean("otel.instrumentation.acf.experimental-span-attributes", false);

  private static final Instrumenter<HttpServletRequest, Void> INSTRUMENTER;

  static {
    INSTRUMENTER =
        Instrumenter.<HttpServletRequest, Void>builder(
                GlobalOpenTelemetry.get(),
                "io.opentelemetry.cfml",
                HttpCfmlPageInstrumentationSingletons::spanNameOnRender)
            .addAttributesExtractor(new RenderAttributesExtractor())
            .setEnabled(ExperimentalConfig.get().viewTelemetryEnabled())
            .buildInstrumenter(SpanKindExtractor.alwaysInternal());
  }

  private static String spanNameOnRender(HttpServletRequest req) {
    // get the JSP file name being rendered in an include action
    Object includeServletPath = req.getAttribute(RequestDispatcher.INCLUDE_SERVLET_PATH);
    String spanName = req.getServletPath();
    if (includeServletPath instanceof String) {
      spanName = includeServletPath.toString();
    }
    return "Render " + spanName;
  }

  public static Instrumenter<HttpServletRequest, Void> instrumenter() {
    return INSTRUMENTER;
  }

  private HttpCfmlPageInstrumentationSingletons() {}

  private static class RenderAttributesExtractor
      implements AttributesExtractor<HttpServletRequest, Void> {

    @Override
    public void onStart(
        AttributesBuilder attributes, Context parentContext, HttpServletRequest request) {
      if (!CAPTURE_EXPERIMENTAL_SPAN_ATTRIBUTES) {
        return;
      }

      Object forwardOrigin = request.getAttribute(RequestDispatcher.FORWARD_SERVLET_PATH);
      if (forwardOrigin instanceof String) {
        attributes.put("jsp.forwardOrigin", forwardOrigin.toString());
      }

      // add the request URL as a tag to provide better context when looking at spans produced by
      // actions. Tomcat 9 has relative path symbols in the value returned from
      // HttpServletRequest#getRequestURL(),
      // normalizing the URL should remove those symbols for readability and consistency
      try {
        attributes.put(
            "jsp.requestURL", new URI(request.getRequestURL().toString()).normalize().toString());
      } catch (URISyntaxException e) {
        Logger.getLogger(HttpJspPage.class.getName())
            .log(WARNING, "Failed to get and normalize request URL: {0}", e.getMessage());
      }
    }

    @Override
    public void onEnd(
        AttributesBuilder attributes,
        Context context,
        HttpServletRequest httpServletRequest,
        @Nullable Void unused,
        @Nullable Throwable error) {}
  }
}

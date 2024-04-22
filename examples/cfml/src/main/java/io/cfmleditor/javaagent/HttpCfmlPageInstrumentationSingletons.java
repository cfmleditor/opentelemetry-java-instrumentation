/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.cfmleditor.javaagent;

import coldfusion.filter.FusionContext;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.javaagent.bootstrap.internal.InstrumentationConfig;
import javax.annotation.Nullable;

public class HttpCfmlPageInstrumentationSingletons {
  private static final boolean CAPTURE_EXPERIMENTAL_SPAN_ATTRIBUTES =
      InstrumentationConfig.get()
          .getBoolean("io.cfmleditor.javaagent.experimental-span-attributes", false);

  private static final Instrumenter<FusionContext, Void> INSTRUMENTER;

  static {
    INSTRUMENTER =
        Instrumenter.<FusionContext, Void>builder(
                GlobalOpenTelemetry.get(),
                "io.cfmleditor.javaagent",
                HttpCfmlPageInstrumentationSingletons::spanNameOnRender)
            .addAttributesExtractor(new RenderAttributesExtractor())
            .setEnabled(true)
            .buildInstrumenter();
  }

  private static String spanNameOnRender(FusionContext req) {
    // get the JSP file name being rendered in an include action
    // Object includeServletPath = req.getAttribute(RequestDispatcher.INCLUDE_SERVLET_PATH);
    String spanName = req.getRequest().getServletPath() + '?' + req.getRequest().getQueryString();
    // if (includeServletPath instanceof String) {
    //   spanName = includeServletPath.toString();
    // }
    return "Render " + spanName;
  }

  public static Instrumenter<FusionContext, Void> instrumenter() {
    return INSTRUMENTER;
  }

  private HttpCfmlPageInstrumentationSingletons() {}

  private static class RenderAttributesExtractor
      implements AttributesExtractor<FusionContext, Void> {

    @Override
    public void onStart(
        AttributesBuilder attributes, Context parentContext, FusionContext request) {
      return;
      // if (!CAPTURE_EXPERIMENTAL_SPAN_ATTRIBUTES) {
      //   return;
      // }

      // Object forwardOrigin = request.getAttribute(RequestDispatcher.FORWARD_SERVLET_PATH);
      // if (forwardOrigin instanceof String) {
      //   attributes.put("jsp.forwardOrigin", forwardOrigin.toString());
      // }

      // // add the request URL as a tag to provide better context when looking at spans produced by
      // // actions. Tomcat 9 has relative path symbols in the value returned from
      // // HttpServletRequest#getRequestURL(),
      // // normalizing the URL should remove those symbols for readability and consistency
      // try {
      //   attributes.put(
      //       "jsp.requestURL", new
      // URI(request.getRequestURL().toString()).normalize().toString());
      // } catch (URISyntaxException e) {
      //   Logger.getLogger(HttpJspPage.class.getName())
      //       .log(WARNING, "Failed to get and normalize request URL: {0}", e.getMessage());
      // }
    }

    @Override
    public void onEnd(
        AttributesBuilder attributes,
        Context context,
        FusionContext fusionContext,
        @Nullable Void unused,
        @Nullable Throwable error) {}
  }
}

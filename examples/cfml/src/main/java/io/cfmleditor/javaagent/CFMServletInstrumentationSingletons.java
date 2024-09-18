/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.cfmleditor.javaagent;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.api.instrumenter.SpanKindExtractor;
import io.opentelemetry.javaagent.bootstrap.internal.ExperimentalConfig;
import javax.annotation.Nullable;
import javax.servlet.ServletResponse;

public class CFMServletInstrumentationSingletons {

  private static final Instrumenter<ServletResponse, Void> INSTRUMENTER;

  static {
    INSTRUMENTER =
        Instrumenter.<ServletResponse, Void>builder(
                GlobalOpenTelemetry.get(),
                "io.cfmleditor.javaagent",
                CFMServletInstrumentationSingletons::spanNameOnRender)
            .addAttributesExtractor(new RenderAttributesExtractor())
            .setEnabled(ExperimentalConfig.get().viewTelemetryEnabled())
            .buildInstrumenter(SpanKindExtractor.alwaysInternal());
  }

  private static String spanNameOnRender(ServletResponse response) {
    return "CFMServlet ";
  }

  public static Instrumenter<ServletResponse, Void> instrumenter() {
    return INSTRUMENTER;
  }

  private CFMServletInstrumentationSingletons() {}

  private static class RenderAttributesExtractor
      implements AttributesExtractor<ServletResponse, Void> {

    @Override
    public void onStart(
        AttributesBuilder attributes, Context parentContext, ServletResponse response) {
      return;
    }

    @Override
    public void onEnd(
        AttributesBuilder attributes,
        Context context,
        @Nullable ServletResponse response,
        @Nullable Void unused,
        @Nullable Throwable error) {}
  }
}

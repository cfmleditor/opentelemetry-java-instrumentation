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
import org.apache.catalina.connector.ResponseFacade;

public class CFMServletInstrumentationSingletons {

  private static final Instrumenter<ResponseFacade, Void> INSTRUMENTER;

  static {
    INSTRUMENTER =
        Instrumenter.<ResponseFacade, Void>builder(
                GlobalOpenTelemetry.get(),
                "io.cfmleditor.javaagent",
                CFMServletInstrumentationSingletons::spanNameOnRender)
            .addAttributesExtractor(new RenderAttributesExtractor())
            .setEnabled(ExperimentalConfig.get().viewTelemetryEnabled())
            .buildInstrumenter(SpanKindExtractor.alwaysInternal());
  }

  private static String spanNameOnRender(ResponseFacade response) {
    return "CFMServlet ";
  }

  public static Instrumenter<ResponseFacade, Void> instrumenter() {
    return INSTRUMENTER;
  }

  private CFMServletInstrumentationSingletons() {}

  private static class RenderAttributesExtractor
      implements AttributesExtractor<ResponseFacade, Void> {

    @Override
    public void onStart(
        AttributesBuilder attributes, Context parentContext, ResponseFacade response) {
      return;
    }

    @Override
    public void onEnd(
        AttributesBuilder attributes,
        Context context,
        @Nullable ResponseFacade response,
        @Nullable Void unused,
        @Nullable Throwable error) {}
  }
}

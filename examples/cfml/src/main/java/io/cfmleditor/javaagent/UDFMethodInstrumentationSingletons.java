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

public class UDFMethodInstrumentationSingletons {
  // private static final boolean CAPTURE_EXPERIMENTAL_SPAN_ATTRIBUTES =
  //     InstrumentationConfig.get()
  //         .getBoolean("io.cfmleditor.javaagent.experimental-span-attributes", false);

  private static final Instrumenter<InvokeContext, Void> INSTRUMENTER;

  static {
    INSTRUMENTER =
        Instrumenter.<InvokeContext, Void>builder(
                GlobalOpenTelemetry.get(),
                "io.cfmleditor.javaagent",
                UDFMethodInstrumentationSingletons::spanNameOnRender)
            .addAttributesExtractor(new RenderAttributesExtractor())
            .setEnabled(ExperimentalConfig.get().viewTelemetryEnabled())
            .buildInstrumenter(SpanKindExtractor.alwaysInternal());
  }

  private static String spanNameOnRender(InvokeContext invokeContext) {
    return "Invoke " + invokeContext.getFn();
  }

  public static Instrumenter<InvokeContext, Void> instrumenter() {
    return INSTRUMENTER;
  }

  private UDFMethodInstrumentationSingletons() {}

  private static class RenderAttributesExtractor
      implements AttributesExtractor<InvokeContext, Void> {

    @Override
    public void onStart(
        AttributesBuilder attributes, Context parentContext, InvokeContext invokeContext) {
      return;
    }

    @Override
    public void onEnd(
        AttributesBuilder attributes,
        Context context,
        @Nullable InvokeContext invokeContext,
        @Nullable Void unused,
        @Nullable Throwable error) {}
  }
}

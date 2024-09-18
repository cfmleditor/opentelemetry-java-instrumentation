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

public class CFPageInstrumentationSingletons {
  // private static final boolean CAPTURE_EXPERIMENTAL_SPAN_ATTRIBUTES =
  //     InstrumentationConfig.get()
  //         .getBoolean("io.cfmleditor.javaagent.experimental-span-attributes", false);

  private static final Instrumenter<CreateObjectContext, Void> INSTRUMENTER;

  static {
    INSTRUMENTER =
        Instrumenter.<CreateObjectContext, Void>builder(
                GlobalOpenTelemetry.get(),
                "io.cfmleditor.javaagent",
                CFPageInstrumentationSingletons::spanNameOnRender)
            .addAttributesExtractor(new RenderAttributesExtractor())
            .setEnabled(ExperimentalConfig.get().viewTelemetryEnabled())
            .buildInstrumenter(SpanKindExtractor.alwaysInternal());
  }

  private static String spanNameOnRender(CreateObjectContext createObjectContext) {
    return "CreateObject " + createObjectContext.getType() + ' ' + createObjectContext.getName();
  }

  public static Instrumenter<CreateObjectContext, Void> instrumenter() {
    return INSTRUMENTER;
  }

  private CFPageInstrumentationSingletons() {}

  private static class RenderAttributesExtractor
      implements AttributesExtractor<CreateObjectContext, Void> {

    @Override
    public void onStart(
        AttributesBuilder attributes,
        Context parentContext,
        CreateObjectContext createObjectContext) {
      return;
    }

    @Override
    public void onEnd(
        AttributesBuilder attributes,
        Context context,
        @Nullable CreateObjectContext createObjectContext,
        @Nullable Void unused,
        @Nullable Throwable error) {}
  }
}

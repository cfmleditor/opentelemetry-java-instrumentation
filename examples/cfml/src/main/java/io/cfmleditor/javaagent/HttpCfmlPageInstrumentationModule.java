/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.cfmleditor.javaagent;

import static java.util.Collections.singletonList;

import com.google.auto.service.AutoService;
import io.opentelemetry.javaagent.extension.instrumentation.InstrumentationModule;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import java.util.List;

/**
 * This is a demo instrumentation which hooks into servlet invocation and modifies the http
 * response.
 */
@AutoService(InstrumentationModule.class)
public final class HttpCfmlPageInstrumentationModule extends InstrumentationModule {
  public HttpCfmlPageInstrumentationModule() {
    super("acf", "cfml");
  }

  @Override
  public List<TypeInstrumentation> typeInstrumentations() {
    return singletonList(new HttpCfmlPageInstrumentation());
  }

  @Override
  public boolean isHelperClass(String className) {
    return className.startsWith("io.cfmleditor.javaagent");
  }
}

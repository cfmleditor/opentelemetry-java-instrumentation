/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.acf;

import static java.util.Arrays.asList;

import com.google.auto.service.AutoService;
import io.opentelemetry.javaagent.extension.instrumentation.InstrumentationModule;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.instrumentation.acf.HttpCfmlPageInstrumentation;
import io.opentelemetry.javaagent.instrumentation.acf.CfmlCompilationContextInstrumentation;

import java.util.List;

@AutoService(InstrumentationModule.class)
public class CfmlInstrumentationModule extends InstrumentationModule {
  public CfmlInstrumentationModule() {
    super("acf", "cfml");
  }

  @Override
  public List<TypeInstrumentation> typeInstrumentations() {
    return asList(new HttpCfmlPageInstrumentation(), new CfmlCompilationContextInstrumentation());
  }
}

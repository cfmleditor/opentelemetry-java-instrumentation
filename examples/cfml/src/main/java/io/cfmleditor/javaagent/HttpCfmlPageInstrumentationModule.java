/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

 package io.cfmleditor.javaagent;

 import static java.util.Collections.singletonList;
 
 import com.google.auto.service.AutoService;
 import io.cfmleditor.javaagent.HttpCfmlPageInstrumentation;
 import io.opentelemetry.javaagent.extension.instrumentation.InstrumentationModule;
 import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
 import io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers;
 import java.util.List;
 import net.bytebuddy.matcher.ElementMatcher;
 
 /**
  * This is a demo instrumentation which hooks into servlet invocation and modifies the http
  * response.
  */
 @AutoService(InstrumentationModule.class)
 public final class HttpCfmlPageInstrumentationModule extends InstrumentationModule {
   public HttpCfmlPageInstrumentationModule() {
     super("cfml");
   }

   @Override
   public int order() {
     return 1;
   }

   @Override
   public ElementMatcher.Junction<ClassLoader> classLoaderMatcher() {
     return AgentElementMatchers.hasClassesNamed("coldfusion.runtime.CfJspPage");
   }
 
   @Override
   public List<TypeInstrumentation> typeInstrumentations() {
     return singletonList(new HttpCfmlPageInstrumentation());
   }
 }
 
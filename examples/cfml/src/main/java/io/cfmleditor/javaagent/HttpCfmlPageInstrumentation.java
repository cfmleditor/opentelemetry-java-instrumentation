/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.cfmleditor.javaagent;

import static io.cfmleditor.javaagent.HttpCfmlPageInstrumentationSingletons.instrumenter;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

import coldfusion.filter.FusionContext;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class HttpCfmlPageInstrumentation implements TypeInstrumentation {

  @Override
  public ElementMatcher<TypeDescription> typeMatcher() {
    // System.out.printf("CfJspPageAdvice");
    return named("coldfusion.runtime.CfJspPage");
  }

  @Override
  public void transform(TypeTransformer transformer) {
    // System.out.printf("CfJspPageAdvice");
    transformer.applyAdviceToMethod(
        named("invoke")
            .and(takesArgument(0, named("coldfusion.filter.FusionContext")))
            .and(isPublic()),
        HttpCfmlPageInstrumentation.class.getName() + "$CfJspPageAdvice");
  }

  @SuppressWarnings("unused")
  public static class CfJspPageAdvice {

    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void onEnter(
        @Advice.Argument(0) FusionContext req,
        @Advice.Local("otelContext") Context context,
        @Advice.Local("otelScope") Scope scope) {

      Context parentContext =
          io.opentelemetry.javaagent.bootstrap.Java8BytecodeBridge.currentContext();
      if (!instrumenter().shouldStart(parentContext, req)) {
        return;
      }

      // System.out.printf("CfJspPageAdvice");
      context = instrumenter().start(parentContext, req);
      scope = context.makeCurrent();
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
    public static void stopSpan(
        @Advice.Argument(0) FusionContext req,
        @Advice.Thrown Throwable throwable,
        @Advice.Local("otelContext") Context context,
        @Advice.Local("otelScope") Scope scope) {
      if (scope == null) {
        return;
      }

      scope.close();
      instrumenter().end(context, req, null, throwable);
    }
  }
}

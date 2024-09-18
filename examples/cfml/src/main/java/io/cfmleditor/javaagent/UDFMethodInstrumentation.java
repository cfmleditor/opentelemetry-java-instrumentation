/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.cfmleditor.javaagent;

import static io.cfmleditor.javaagent.UDFMethodInstrumentationSingletons.instrumenter;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.javaagent.bootstrap.Java8BytecodeBridge;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class UDFMethodInstrumentation implements TypeInstrumentation {

  @Override
  public ElementMatcher<TypeDescription> typeMatcher() {
    return named("coldfusion.runtime.UDFMethod");
  }

  @Override
  public void transform(TypeTransformer transformer) {
    // System.out.printf("UDFMethodAdvice");
    transformer.applyAdviceToMethod(
        named("invoke")
            .and(takesArgument(0, named("java.lang.Object")))
            .and(takesArgument(1, named("java.lang.String")))
            .and(isPublic()),
        UDFMethodInstrumentation.class.getName() + "$UDFMethodAdvice");
  }

  @SuppressWarnings("unused")
  public static class UDFMethodAdvice {

    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void onEnter(
        @Advice.Argument(0) Object obj,
        @Advice.Argument(1) String fn,
        @Advice.Local("otelContext") Context context,
        @Advice.Local("otelScope") Scope scope) {
      Context parentContext = Java8BytecodeBridge.currentContext();
      InvokeContext invokeContext = new InvokeContext(parentContext, obj, fn);
      if (!instrumenter().shouldStart(parentContext, invokeContext)) {
        return;
      }

      context = instrumenter().start(parentContext, invokeContext);
      scope = context.makeCurrent();
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
    public static void stopSpan(
        @Advice.Argument(0) Object obj,
        @Advice.Argument(1) String fn,
        @Advice.Thrown Throwable throwable,
        @Advice.Local("otelContext") Context context,
        @Advice.Local("otelScope") Scope scope) {
      if (scope == null) {
        return;
      }
      scope.close();
      instrumenter().end(context, null, null, throwable);
    }
  }
}

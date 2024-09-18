/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.cfmleditor.javaagent;

import static io.cfmleditor.javaagent.CFPageInstrumentationSingletons.instrumenter;
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

public class CFPageInstrumentation implements TypeInstrumentation {

  @Override
  public ElementMatcher<TypeDescription> typeMatcher() {
    return named("coldfusion.runtime.CFPage");
  }

  @Override
  public void transform(TypeTransformer transformer) {
    // System.out.printf("CfPageAdvice");
    transformer.applyAdviceToMethod(
        named("CreateObject")
            .and(takesArgument(0, named("java.lang.String")))
            .and(takesArgument(1, named("java.lang.String")))
            .and(isPublic()),
        CFPageInstrumentation.class.getName() + "$CfPageAdvice");
  }

  @SuppressWarnings("unused")
  public static class CfPageAdvice {

    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void onEnter(
        @Advice.Argument(0) String type,
        @Advice.Argument(1) String name,
        @Advice.Local("otelContext") Context context,
        @Advice.Local("otelScope") Scope scope) {
      Context parentContext = Java8BytecodeBridge.currentContext();
      CreateObjectContext createObjectContext = new CreateObjectContext(parentContext, type, name);
      if (!instrumenter().shouldStart(parentContext, createObjectContext)) {
        return;
      }

      context = instrumenter().start(parentContext, createObjectContext);
      scope = context.makeCurrent();
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
    public static void stopSpan(
        @Advice.Argument(0) String type,
        @Advice.Argument(1) String name,
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

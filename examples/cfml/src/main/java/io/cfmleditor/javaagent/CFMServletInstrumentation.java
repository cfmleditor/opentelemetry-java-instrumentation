/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.cfmleditor.javaagent;

import static io.cfmleditor.javaagent.CFMServletInstrumentationSingletons.instrumenter;
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
import org.apache.catalina.connector.RequestFacade;
import org.apache.catalina.connector.ResponseFacade;

public class CFMServletInstrumentation implements TypeInstrumentation {

  @Override
  public ElementMatcher<TypeDescription> typeMatcher() {
    return named("coldfusion.CfmServlet");
  }

  @Override
  public void transform(TypeTransformer transformer) {
    // System.out.printf("CFMServletAdvice");
    transformer.applyAdviceToMethod(
        named("service")
            .and(takesArgument(0, named("org.apache.catalina.connector.RequestFacade")))
            .and(takesArgument(1, named("org.apache.catalina.connector.ResponseFacade")))
            .and(isPublic()),
        CFMServletInstrumentation.class.getName() + "$CFMServletAdvice");
  }

  @SuppressWarnings("unused")
  public static class CFMServletAdvice {

    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void onEnter(
        @Advice.Argument(0) RequestFacade request,
        @Advice.Argument(1) ResponseFacade response,
        @Advice.Local("otelContext") Context context,
        @Advice.Local("otelScope") Scope scope) {
      Context parentContext = Java8BytecodeBridge.currentContext();
      if (!instrumenter().shouldStart(parentContext, response)) {
        return;
      }

      context = instrumenter().start(parentContext, response);
      scope = context.makeCurrent();
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
    public static void stopSpan(
        @Advice.Argument(0) RequestFacade request,
        @Advice.Argument(1) ResponseFacade response,
        @Advice.Thrown Throwable throwable,
        @Advice.Local("otelContext") Context context,
        @Advice.Local("otelScope") Scope scope) {

      // HttpServerRoute.update(context, HttpServerRouteSource.SERVER, "test");

      if (scope == null) {
        return;
      }
      scope.close();
      instrumenter().end(context, null, null, throwable);
    }
  }
}

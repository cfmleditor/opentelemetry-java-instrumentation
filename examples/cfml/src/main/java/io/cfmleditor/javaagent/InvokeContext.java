/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.cfmleditor.javaagent;

import io.opentelemetry.context.Context;

public final class InvokeContext {
  private final Context parentContext;
  private final Object object;
  private final String fn;
  private Context context;

  public InvokeContext(Context parentContext, Object object, String fn) {
    this.parentContext = parentContext;
    this.object = object;
    this.fn = fn;
  }

  public Context getParentContext() {
    return parentContext;
  }

  public Object getObject() {
    return object;
  }

  public String getFn() {
    return fn;
  }

  public Context getContext() {
    return context;
  }

  public void setContext(Context context) {
    this.context = context;
  }
}

/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.cfmleditor.javaagent;

import io.opentelemetry.context.Context;

public final class CreateObjectContext {
  private final Context parentContext;
  private final String type;
  private final String name;
  private Context context;

  public CreateObjectContext(Context parentContext, String type, String name) {
    this.parentContext = parentContext;
    this.type = type;
    this.name = name;
  }

  public Context getParentContext() {
    return parentContext;
  }

  public String getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public Context getContext() {
    return context;
  }

  public void setContext(Context context) {
    this.context = context;
  }
}

/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.resources;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;

class JarPathFinder {
  private final Supplier<String[]> getProcessHandleArguments;
  private final Function<String, String> getSystemProperty;
  private final Predicate<Path> fileExists;

  private static class DetectionResult {
    private final Optional<Path> jarPath;

    private DetectionResult(Optional<Path> jarPath) {
      this.jarPath = jarPath;
    }
  }

  private static Optional<DetectionResult> detectionResult = Optional.empty();

  public JarPathFinder() {
    this(ProcessArguments::getProcessArguments, System::getProperty, Files::isRegularFile);
  }

  // visible for tests
  JarPathFinder(
      Supplier<String[]> getProcessHandleArguments,
      Function<String, String> getSystemProperty,
      Predicate<Path> fileExists) {
    this.getProcessHandleArguments = getProcessHandleArguments;
    this.getSystemProperty = getSystemProperty;
    this.fileExists = fileExists;
  }

  // visible for testing
  static void resetForTest() {
    detectionResult = Optional.empty();
  }

  Optional<Path> getJarPath() {
    if (!detectionResult.isPresent()) {
      detectionResult = Optional.of(new DetectionResult(Optional.ofNullable(detectJarPath())));
    }
    return detectionResult.get().jarPath;
  }

  private Path detectJarPath() {
    Path jarPath = getJarPathFromProcessHandle();
    if (jarPath != null) {
      return jarPath;
    }
    return getJarPathFromSunCommandLine();
  }

  @Nullable
  private Path getJarPathFromProcessHandle() {
    String[] javaArgs = getProcessHandleArguments.get();
    for (int i = 0; i < javaArgs.length; ++i) {
      if ("-jar".equals(javaArgs[i]) && (i < javaArgs.length - 1)) {
        return Paths.get(javaArgs[i + 1]);
      }
    }
    return null;
  }

  @Nullable
  private Path getJarPathFromSunCommandLine() {
    // the jar file is the first argument in the command line string
    String programArguments = getSystemProperty.apply("sun.java.command");
    if (programArguments == null) {
      return null;
    }

    // Take the path until the first space. If the path doesn't exist extend it up to the next
    // space. Repeat until a path that exists is found or input runs out.
    int next = 0;
    while (true) {
      int nextSpace = programArguments.indexOf(' ', next);
      if (nextSpace == -1) {
        return pathIfExists(programArguments);
      }
      Path path = pathIfExists(programArguments.substring(0, nextSpace));
      next = nextSpace + 1;
      if (path != null) {
        return path;
      }
    }
  }

  @Nullable
  private Path pathIfExists(String programArguments) {
    Path candidate;
    try {
      candidate = Paths.get(programArguments);
    } catch (InvalidPathException e) {
      return null;
    }
    return fileExists.test(candidate) ? candidate : null;
  }
}

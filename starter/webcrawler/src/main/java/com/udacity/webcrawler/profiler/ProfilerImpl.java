package com.udacity.webcrawler.profiler;

import javax.inject.Inject;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Objects;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

/**
 * Concrete implementation of the {@link Profiler}.
 */
final class ProfilerImpl implements Profiler {

  private final Clock clock;
  private final ProfilingState state = new ProfilingState();
  private final ZonedDateTime startTime;

  @Inject
  ProfilerImpl(Clock clock) {
    this.clock = Objects.requireNonNull(clock);
    this.startTime = ZonedDateTime.now(clock);
  }

  @Profiled
  public boolean isAnnotatedProfiled(Class<?> klass) {
    return klass != null && klass.getDeclaredMethods().length > 0 &&
            java.util.Arrays.stream(klass.getDeclaredMethods())
                    .anyMatch(method -> method.getAnnotation(Profiled.class) != null);
  }

  @Override
  public <T> T wrap(Class<T> klass, T delegate) {
    Objects.requireNonNull(klass);

    if (!isAnnotatedProfiled(klass)) {
      throw new IllegalArgumentException(klass.getName() + " has no @Profiled annotated methods.");
    }

    ProfilingMethodInterceptor profilingMethodInterceptor =
            new ProfilingMethodInterceptor(clock, delegate, state);

    Object proxy = Proxy.newProxyInstance(
            ProfilerImpl.class.getClassLoader(),
            new Class[]{klass},
            profilingMethodInterceptor
    );

    return klass.cast(proxy);
  }

  @Override
  public void writeData(Path path) {
    Objects.requireNonNull(path);

    try (FileWriter fileWriter = new FileWriter(path.toFile(), true)) {
      writeData(fileWriter);
    } catch (IOException ex) {
      throw new UncheckedIOException("Failed to write profiling data to " + path, ex);
    }
  }

  @Override
  public void writeData(Writer writer) throws IOException {
    writer.write("Run at " + RFC_1123_DATE_TIME.format(startTime));
    writer.write(System.lineSeparator());
    state.write(writer);
    writer.write(System.lineSeparator());
  }
}

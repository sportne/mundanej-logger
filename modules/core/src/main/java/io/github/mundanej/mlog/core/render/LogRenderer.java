package io.github.mundanej.mlog.core.render;

import io.github.mundanej.mlog.api.LogEvent;

/** Renders one event to one physical log line including the trailing newline. */
public interface LogRenderer {
  String render(LogEvent event);
}

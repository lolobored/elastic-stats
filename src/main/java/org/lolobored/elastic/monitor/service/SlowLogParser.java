package org.lolobored.elastic.monitor.service;

import org.lolobored.elastic.monitor.model.SlowLog;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface SlowLogParser {
  public List<SlowLog> parseSlowLogs(File slowLogPath) throws IOException;
}

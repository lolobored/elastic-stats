package org.lolobored.elastic.monitor.service;

import org.lolobored.elastic.monitor.model.QueryStat;
import org.lolobored.elastic.monitor.model.SlowLog;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface QueriesRunner {
  List<QueryStat> runQueries(String logFile, List<SlowLog> queries, String elasticUrl, String user, String pwd, Integer iterations) throws IOException, NoSuchAlgorithmException, KeyManagementException;
}

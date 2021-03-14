package org.lolobored.elastic.monitor.service.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.util.EntityUtils;
import org.lolobored.elastic.monitor.ElasticSearchStats;
import org.lolobored.elastic.monitor.model.QueryStat;
import org.lolobored.elastic.monitor.model.SlowLog;
import org.lolobored.elastic.monitor.service.QueriesRunner;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class QueriesRunnerImpl implements QueriesRunner {

  @Override
  public List<QueryStat> runQueries(String logFile, List<SlowLog> queries, String elasticUrl, String user, String pwd, Integer iterations) throws IOException, NoSuchAlgorithmException, KeyManagementException {
    HttpClient httpClient = returnInsecureClient();
    int lineNumber=0;
    String encodedCreds = Base64.getEncoder().encodeToString((user + ":" + pwd).getBytes());
    List<QueryStat> result= new ArrayList<>();

    for (SlowLog query : queries) {
      lineNumber++;
      BigDecimal queryTime = BigDecimal.ZERO;
      Integer runIterations = iterations;
      List<Long> times= new ArrayList<>();
      boolean success = true;
      while (runIterations>0) {
        String url = elasticUrl + "/_search";
        HttpPost post = new HttpPost(url);
        HttpEntity queryString = new StringEntity(query.getQuery(), "UTF-8");
        post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        post.setEntity(queryString);
        if (StringUtils.isNotEmpty(user)) {
          post.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedCreds);
        }
        HttpResponse response = httpClient.execute(post);

        if (response.getStatusLine().getStatusCode()<200 || response.getStatusLine().getStatusCode()>299){
          String body= EntityUtils.toString(response.getEntity());
          FileUtils.write(new File(ElasticSearchStats.ERROR_LOG), "File: "+query.getFile()+"\nquery["+lineNumber+"]:\n"+query.getQuery()+"\nerror:\n"+body+"\n\n",
                  "UTF-8", true);
          success= false;
        }
        else {
          String body = EntityUtils.toString(response.getEntity());
          long total = Long.parseLong(StringUtils.substringBetween(body, "\"took\":", ","));
          times.add(total);
          response.getEntity().consumeContent();
          queryTime = queryTime.add(new BigDecimal(total));
        }
        runIterations--;
      }
      if (success) {
        QueryStat stat = new QueryStat();
        stat.setFile(logFile);
        stat.setQuery(query.getQuery());
        stat.setQueryNumber(lineNumber);
        stat.setIterations(iterations);
        stat.setAverageTime(queryTime.divide(new BigDecimal(iterations), RoundingMode.CEILING));
        stat.setTimes(times);
        result.add(stat);
      }
    }
    return result;
  }

  private HttpClient returnInsecureClient() throws NoSuchAlgorithmException, KeyManagementException {
    SSLContext sslContext = SSLContext.getInstance("SSL");

// set up a TrustManager that trusts everything
    sslContext.init(null, new TrustManager[] { new X509TrustManager() {
      public X509Certificate[] getAcceptedIssuers() {
        return null;
      }

      public void checkClientTrusted(X509Certificate[] certs,
                                     String authType) {
      }

      public void checkServerTrusted(X509Certificate[] certs,
                                     String authType) {
      }
    } }, new SecureRandom());

    SSLSocketFactory sf = new SSLSocketFactory(sslContext);
    Scheme httpsScheme = new Scheme("https", 443, sf);
    SchemeRegistry schemeRegistry = new SchemeRegistry();
    schemeRegistry.register(httpsScheme);

// apache HttpClient version >4.2 should use BasicClientConnectionManager
    ClientConnectionManager cm = new SingleClientConnManager(schemeRegistry);
    return new DefaultHttpClient(cm);
  }
}

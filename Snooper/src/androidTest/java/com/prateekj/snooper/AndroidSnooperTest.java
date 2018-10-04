package com.prateekj.snooper;

import android.app.Application;

import com.prateekj.snooper.networksnooper.database.SnooperRepo;
import com.prateekj.snooper.networksnooper.model.HttpCall;
import com.prateekj.snooper.networksnooper.model.HttpCall.Builder;
import com.prateekj.snooper.networksnooper.model.HttpCallRecord;
import com.prateekj.snooper.rules.DataResetRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static androidx.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

public class AndroidSnooperTest {

  private AndroidSnooper androidSnooper;

  @Rule
  public DataResetRule dataResetRule = new DataResetRule();

  @Before
  public void setUp() throws Exception {
    androidSnooper = AndroidSnooper.getInstance();
  }

  @Test
  public void shouldReturnSameInstanceOnEveryInit() throws Exception {
    Application application = ((SnooperInstrumentationRunner) getInstrumentation()).getApplication();
    AndroidSnooper newSnooper = AndroidSnooper.init(application);
    assertThat(newSnooper, sameInstance(androidSnooper));
  }

  @Test
  public void shouldSaveHttpCallViaSpringHttpRequestInterceptor() throws Exception {
    final String url = "https://ajax.googleapis.com/ajax/services/search/web?v=1.0";
    final String responseBody = "responseBody";
    final String requestBody = "requestBody";

    HttpCall call = new Builder()
      .withUrl(url)
      .withMethod("POST")
      .withPayload(requestBody)
      .withResponseBody(responseBody)
      .withStatusCode(200)
      .withStatusText("OK")
      .build();

    androidSnooper.record(call);

    getInstrumentation().runOnMainSync(new Runnable() {
      @Override
      public void run() {
        List<HttpCallRecord> httpCallRecords = new SnooperRepo(getTargetContext()).findAllSortByDate();
        assertThat(httpCallRecords.size(), is(1));
        HttpCallRecord httpCallRecord = httpCallRecords.get(0);
        assertThat(httpCallRecord.getUrl(), is(url));
        assertThat(httpCallRecord.getPayload(), is(requestBody));
        assertThat(httpCallRecord.getMethod(), is("POST"));
        assertThat(httpCallRecord.getResponseBody(), is(responseBody));
        assertThat(httpCallRecord.getStatusCode(), is(200));
        assertThat(httpCallRecord.getStatusText(), is("OK"));
      }
    });
  }
}

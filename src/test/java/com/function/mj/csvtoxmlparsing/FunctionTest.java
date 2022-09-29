package com.function.mj.csvtoxmlparsing;

import com.microsoft.azure.functions.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.*;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


/**
 * Unit test for Function class.
 */
public class FunctionTest {
    /**
     * Unit test for HttpTriggerJava method.
     */
    @Test
    public void testparsingReflection() throws Exception {
        // Setup
        @SuppressWarnings("unchecked")
        final HttpRequestMessage<Optional<String>> req = mock(HttpRequestMessage.class);

        final Map<String, String> queryParams = new HashMap<>();
        doReturn(queryParams).when(req).getQueryParameters();

        final Optional<String> queryBody = Optional.of("""
        name,major,institution,score
        pedro,math,MIT,45
        nicolas,science,UCLA,62
        """);
        doReturn(queryBody).when(req).getBody();

        doAnswer(new Answer<HttpResponseMessage.Builder>() {
            @Override
            public HttpResponseMessage.Builder answer(InvocationOnMock invocation) {
                HttpStatus status = (HttpStatus) invocation.getArguments()[0];
                return new HttpResponseMessageMock.HttpResponseMessageBuilderMock().status(status);
            }
        }).when(req).createResponseBuilder(any(HttpStatus.class));

        final ExecutionContext context = mock(ExecutionContext.class);
        doReturn(Logger.getGlobal()).when(context).getLogger();

        // Invoke
        final HttpResponseMessage ret = new Parser().parsingReflection(req, context);

        // Verify
        assertEquals(ret.getStatus(), HttpStatus.OK);
        assertEquals(ret.getBody(), """
            <?xml version="1.0" encoding="UTF-8"?>
            <root><Score>
              <name>pedro</name>
              <major>math</major>
              <institution>MIT</institution>
              <score>45</score>
            </Score><Score>
              <name>nicolas</name>
              <major>science</major>
              <institution>UCLA</institution>
              <score>62</score>
            </Score></root>    
        """);
    }
}

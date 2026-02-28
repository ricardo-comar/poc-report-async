package com.rhsoft.function;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;


/**
 * Unit test for Function class.
*/
@Disabled
public class FunctionProviderTest extends BaseFunctionTest {

    private String executionId;

    @BeforeEach
    public void setup() {
        executionId = UUID.randomUUID().toString();
    }
    /**
     * Unit test for HttpTriggerJava method.
     */
    @Test 
    public void testQueued() throws Exception {

        // Invoke
        final HttpResponseMessage ret = new FunctionProvider().run(req, executionId, context);

        // Verify
        assertEquals(ret.getStatus(), HttpStatus.OK);
    }
    @Test
    public void testSuccess() throws Exception {

        // Invoke
        final HttpResponseMessage ret = new FunctionProvider().run(req, executionId, context);

        // Verify
        assertEquals(ret.getStatus(), HttpStatus.CREATED);
    }
    @Test 
    public void testNotFound() throws Exception {

        // Invoke
        final HttpResponseMessage ret = new FunctionProvider().run(req, executionId, context);

        // Verify
        assertEquals(ret.getStatus(), HttpStatus.BAD_REQUEST);
    }
    @Test 
    public void testServerError() throws Exception {

        // Invoke
        final HttpResponseMessage ret = new FunctionProvider().run(req, executionId, context);

        // Verify
        assertEquals(ret.getStatus(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    public void testPayloadError() throws Exception {

        // Invoke
        final HttpResponseMessage ret = new FunctionProvider().run(req, executionId, context);

        // Verify
        assertEquals(ret.getStatus(), HttpStatus.UNPROCESSABLE_ENTITY);
    }
}

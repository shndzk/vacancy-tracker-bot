package com.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExceptionTest {
    @Test
    void testExceptions() {
        assertNotNull(new ApiException("test"));
        assertNotNull(new ApiException("test", new RuntimeException()));
        assertNotNull(new RepositoryException("test", new RuntimeException()));
    }
}

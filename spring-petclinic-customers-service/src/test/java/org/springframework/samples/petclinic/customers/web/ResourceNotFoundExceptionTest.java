package org.springframework.samples.petclinic.customers.web;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ResourceNotFoundExceptionTest {

    @Test
    void testExceptionCreation() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Resource not found");
        assertEquals("Resource not found", exception.getMessage());
    }

    @Test
    void testExceptionInheritance() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Test");
        assertTrue(exception instanceof RuntimeException);
    }
} 
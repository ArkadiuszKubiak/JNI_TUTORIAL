package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the App class
 * Tests both Java and native (JNI) methods
 * 
 * Test Framework: JUnit 5 (Jupiter)
 * Execution: ./gradlew test
 */
class AppTest {
    /**
     * Test Java method getGreeting()
     * 
     * Verifies that:
     * 1. App instance can be created
     * 2. getGreeting() method returns a non-null value
     * 3. Confirms basic Java functionality works
     */
    @Test
    void appHasAGreeting() {
        App app = new App();
        assertNotNull(app.getGreeting(), "app should have a greeting");
    }

    /**
     * Test native method greetings()
     * 
     * Verifies that:
     * 1. Native library (libgreetings.so) is loaded correctly
     * 2. greetings() JNI method is callable from Java
     * 3. Native method returns a non-null value
     * 4. Native method returns a string mentioning "C++" (to distinguish from Java implementation)
     * 
     * This test ensures the JNI bridge between Java and C++ is working correctly
     */
    @Test
    void appHasNativeGreeting() {
        App app = new App();
        String nativeGreeting = app.greetings();
        assertNotNull(nativeGreeting, "app should have a native greeting");
        assertTrue(nativeGreeting.contains("C++"), "native greeting should mention C++");
    }
}

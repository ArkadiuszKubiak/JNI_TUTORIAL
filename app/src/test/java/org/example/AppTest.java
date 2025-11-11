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
     * Test Gradle-compiled native method greetingFromGradle()
     * 
     * Verifies that:
     * 1. Native library (libgreetings.so) compiled by Gradle is loaded correctly
     * 2. greetingFromGradle() JNI method is callable from Java
     * 3. Native method returns a non-null value
     * 4. Native method returns a string mentioning "Gradle"
     * 
     * This test ensures the JNI bridge between Java and Gradle C++ code is working
     */
    @Test
    void appHasGradleNativeGreeting() {
        App app = new App();
        String nativeGreeting = app.greetingFromGradle();
        assertNotNull(nativeGreeting, "app should have a Gradle native greeting");
        assertTrue(nativeGreeting.contains("Gradle"), "native greeting should mention Gradle");
    }

    /**
     * Test CMake-compiled native method greetingFromCMake()
     * 
     * Verifies that:
     * 1. Native library (libgreetings.so) compiled by CMake is loaded correctly
     * 2. greetingFromCMake() JNI method is callable from Java
     * 3. Native method returns a non-null value
     * 4. Native method returns a string mentioning "CMake"
     * 
     * This test ensures the JNI bridge between Java and CMake C++ code is working
     */
    @Test
    void appHasCMakeNativeGreeting() {
        App app = new App();
        String nativeGreeting = app.greetingFromCMake();
        assertNotNull(nativeGreeting, "app should have a CMake native greeting");
        assertTrue(nativeGreeting.contains("CMake"), "native greeting should mention CMake");
    }
}

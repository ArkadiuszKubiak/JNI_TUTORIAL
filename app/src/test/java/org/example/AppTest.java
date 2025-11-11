package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the App class
 * Tests both Java methods and two separate native (JNI) methods
 * 
 * Architecture:
 * - Two separate native libraries are loaded in App.java static initializer
 * - libgreetingsGradle.so: Built by Gradle C++ Plugin, contains greetingFromGradle()
 * - libgreetingsCMake.so: Built by CMake, contains greetingFromCMake()
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
     * 
     * This is a pure Java test, no native code involved
     */
    @Test
    void appHasAGreeting() {
        App app = new App();
        assertNotNull(app.getGreeting(), "App should have a greeting");
    }

    /**
     * Test Gradle-compiled native method greetingFromGradle()
     * 
     * Verifies that:
     * 1. Native library libgreetingsGradle.so is loaded correctly (built by Gradle)
     * 2. greetingFromGradle() JNI method is callable from Java
     * 3. Native method returns a non-null value
     * 4. Native method returns a string mentioning "Gradle"
     * 5. JNI bridge between Java and C++ code is working
     * 
     * Library: libgreetingsGradle.so
     * Compiler: Gradle C++ Plugin
     * Source: app/src/main/cpp/greetings.cpp
     * JNI Function: Java_org_example_App_greetingFromGradle()
     */
    @Test
    void appHasGradleNativeGreeting() {
        App app = new App();
        String nativeGreeting = app.greetingFromGradle();
        assertNotNull(nativeGreeting, "App should have a Gradle native greeting");
        assertTrue(nativeGreeting.contains("Gradle"), "Native greeting should mention 'Gradle' from libgreetingsGradle.so");
    }

    /**
     * Test CMake-compiled native method greetingFromCMake()
     * 
     * Verifies that:
     * 1. Native library libgreetingsCMake.so is loaded correctly (built by CMake)
     * 2. greetingFromCMake() JNI method is callable from Java
     * 3. Native method returns a non-null value
     * 4. Native method returns a string mentioning "CMake"
     * 5. JNI bridge between Java and C++ code is working
     * 
     * Library: libgreetingsCMake.so
     * Compiler: CMake
     * Source: app/src/main/cpp/greetings_cmake.cpp
     * JNI Function: Java_org_example_App_greetingFromCMake()
     */
    @Test
    void appHasCMakeNativeGreeting() {
        App app = new App();
        String nativeGreeting = app.greetingFromCMake();
        assertNotNull(nativeGreeting, "App should have a CMake native greeting");
        assertTrue(nativeGreeting.contains("CMake"), "Native greeting should mention 'CMake' from libgreetingsCMake.so");
    }
}

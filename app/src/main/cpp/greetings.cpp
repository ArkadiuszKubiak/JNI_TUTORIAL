#include <jni.h>
#include <string>
#include <iostream>

/**
 * Native C++ implementation of the greetingFromGradle() method.
 * This file is compiled using the Gradle C++ plugin only.
 * 
 * File: greetings.cpp
 * Build System: Gradle C++ Plugin (EXCLUSIVE to this library)
 * Output Library: libgreetingsGradle.so (Linux)
 * Method: greetingFromGradle() - Gradle-specific implementation
 * 
 * Note: This source file is compiled ONLY into libgreetingsGradle.so.
 * The CMake build system has its own separate source file (greetings_cmake.cpp).
 */

/**
 * Java_org_example_App_greetingFromGradle
 * 
 * JNI function name format: Java_<package>_<class>_<method>
 * Breakdown:
 *   - Java:           JNI prefix for all native functions
 *   - org_example:    Package name (dots replaced with underscores)
 *   - App:            Class name
 *   - greetingFromGradle: Method name
 * 
 * Parameters:
 *   - JNIEnv *env:  Pointer to JNI environment, provides access to JNI functions
 *   - jobject obj:  Reference to the Java object that called this method (this pointer)
 * 
 * Returns:
 *   - jstring: A Java string object created via NewStringUTF
 * 
 * Functionality:
 *   1. Creates a C++ std::string with the greeting message
 *   2. Prints the message to console using std::cout (demonstrates C++ STL)
 *   3. Converts the C++ string to a Java jstring using NewStringUTF
 *   4. Returns the Java string to the caller
 * 
 * Library: libgreetingsGradle.so
 * Built by: Gradle C++ Plugin (app/build.gradle)
 */
extern "C" {
    JNIEXPORT jstring JNICALL Java_org_example_App_greetingFromGradle(JNIEnv *env, jobject obj)
    {
        // Create C++ string with greeting message (Gradle version)
        std::string greeting = "Hello from Gradle C++ Plugin!";
        
        // Print message to standard output using C++ Standard Library
        // This demonstrates that we can use C++ features in JNI code
        std::cout << "[Gradle] " << greeting << std::endl;
        
        // Convert C++ string to Java string (jstring)
        // NewStringUTF creates a new Java string from a UTF-8 C string
        // greeting.c_str() converts std::string to const char* pointer
        return env->NewStringUTF(greeting.c_str());
    }
}

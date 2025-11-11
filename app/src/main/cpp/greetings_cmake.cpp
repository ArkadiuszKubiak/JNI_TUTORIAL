#include <jni.h>
#include <string>
#include <iostream>

/**
 * Native C++ implementation of the greetings() method
 * This file is compiled using CMake build system
 * 
 * File: greetings_cmake.cpp
 * Build System: CMake (alternative to Gradle C++ plugin)
 * Compilation: CMakeLists.txt configuration
 * Output Library: libgreetings.so (Linux)
 */

/**
 * Java_org_example_App_greetings
 * 
 * JNI function name format: Java_<package>_<class>_<method>
 * Breakdown:
 *   - Java:         JNI prefix for all native functions
 *   - org_example:  Package name (dots replaced with underscores)
 *   - App:          Class name
 *   - greetings:    Method name
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
 *   2. Prints the message to console with "[CMake]" prefix (distinguishes from Gradle version)
 *   3. Converts the C++ string to a Java jstring using NewStringUTF
 *   4. Returns the Java string to the caller
 * 
 * Note: This is the CMake version with [CMake] prefix in output
 */
extern "C" {
    JNIEXPORT jstring JNICALL Java_org_example_App_greetings(JNIEnv *env, jobject obj)
    {
        // Create C++ string with greeting message specific to CMake build
        std::string greeting = "Greetings from CMake C++ code!";
        
        // Print message to standard output with CMake prefix to distinguish from Gradle version
        // This demonstrates that we can use C++ features in JNI code
        std::cout << "[CMake] " << greeting << std::endl;
        
        // Convert C++ string to Java string (jstring)
        // NewStringUTF creates a new Java string from a UTF-8 C string
        // greeting.c_str() converts std::string to const char* pointer
        return env->NewStringUTF(greeting.c_str());
    }
}

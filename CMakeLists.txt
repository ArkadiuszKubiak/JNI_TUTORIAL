cmake_minimum_required(VERSION 3.10)
project(greetings_cmake_jni)

# ==================== FIND REQUIRED PACKAGES ====================
# Find Java Development Kit components needed for JNI compilation
find_package(Java COMPONENTS Development REQUIRED)

# Find JNI headers and libraries for linking with Java
find_package(JNI REQUIRED)

# ==================== DISPLAY CONFIGURATION INFO ====================
# Print debug information about found Java and JNI components
message(STATUS "Java found: ${Java_JAVA_EXECUTABLE}")
message(STATUS "JNI Include dirs: ${JNI_INCLUDE_DIRS}")
message(STATUS "JNI Libraries: ${JNI_LIBRARIES}")

# ==================== CREATE SHARED LIBRARY ====================
# Add the native library 'greetingsCMake' with source files
# Compiles ONLY greetings_cmake.cpp into a separate shared library
# - greetings_cmake.cpp: Contains Java_org_example_App_greetingFromCMake() implementation
# 
# Creates a shared library named libgreetingsCMake.so (Linux)
# This is loaded in Java via: System.loadLibrary("greetingsCMake")
# 
# NOTE: Gradle C++ Plugin builds greetings.cpp separately into libgreetingsGradle.so
# This CMake build system handles greetings_cmake.cpp exclusively
add_library(greetingsCMake SHARED
    app/src/main/cpp/greetings_cmake.cpp
)

# ==================== CONFIGURE INCLUDE PATHS ====================
# Add JNI header directories to the compilation include paths
# These contain jni.h and platform-specific jni_md.h files
target_include_directories(greetingsCMake PRIVATE ${JNI_INCLUDE_DIRS})

# ==================== LINK LIBRARIES ====================
# Link the native library with JVM libraries
# Required for JNI calls like NewStringUTF()
target_link_libraries(greetingsCMake PRIVATE ${JNI_LIBRARIES})

# ==================== SET OUTPUT DIRECTORY ====================
# Configure properties for the compiled shared library
set_target_properties(greetingsCMake PROPERTIES
    # Place compiled library in build_cmake/lib directory
    # This directory is added to java.library.path for Java runtime
    LIBRARY_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/lib"
    # Enable position-independent code (required for shared libraries)
    POSITION_INDEPENDENT_CODE ON
)

# ==================== COMPILATION FLAGS ====================
# Add compiler warnings and optimization flags for Linux
# -Wall: Enable all common warnings
# -Wextra: Enable extra warnings for stricter code checking
# -O2: Medium optimization level for better performance
target_compile_options(greetingsCMake PRIVATE -Wall -Wextra -O2)

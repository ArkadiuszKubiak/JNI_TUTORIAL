cmake_minimum_required(VERSION 3.10)
project(greetings_jni)

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
# Add the native library 'greetings' with source files
# Compiles BOTH source files into one shared library:
# - greetings.cpp: Contains greetingFromGradle() implementation
# - greetings_cmake.cpp: Contains greetingFromCMake() implementation
# Creates a shared library (.so on Linux) containing both functions
add_library(greetings SHARED
    app/src/main/cpp/greetings.cpp
    app/src/main/cpp/greetings_cmake.cpp
)

# ==================== CONFIGURE INCLUDE PATHS ====================
# Add JNI header directories to the compilation include paths
# These contain jni.h and platform-specific jni_md.h files
target_include_directories(greetings PRIVATE ${JNI_INCLUDE_DIRS})

# ==================== LINK LIBRARIES ====================
# Link the native library with JVM libraries
# Required for JNI calls like NewStringUTF()
target_link_libraries(greetings PRIVATE ${JNI_LIBRARIES})

# ==================== SET OUTPUT DIRECTORY ====================
# Configure properties for the compiled shared library
set_target_properties(greetings PROPERTIES
    # Place compiled library in build/lib directory
    LIBRARY_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/lib"
    # Enable position-independent code (required for shared libraries)
    POSITION_INDEPENDENT_CODE ON
)

# ==================== COMPILATION FLAGS ====================
# Add compiler warnings and optimization flags for Linux
# -Wall: Enable all common warnings
# -Wextra: Enable extra warnings for stricter code checking
# -O2: Medium optimization level for better performance
target_compile_options(greetings PRIVATE -Wall -Wextra -O2)

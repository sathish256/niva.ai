cmake_minimum_required(VERSION 3.22.1)
project(privateai_llama_jni)

set(LLAMA_CPP_DIR "${CMAKE_SOURCE_DIR}/../../../../native/llama.cpp")

add_library(privateai-llama-jni SHARED llama_jni.cpp)

find_library(log-lib log)

if(EXISTS "${LLAMA_CPP_DIR}/CMakeLists.txt")
    message(STATUS "Building with llama.cpp at ${LLAMA_CPP_DIR}")
    set(LLAMA_BUILD_COMMON ON CACHE BOOL "" FORCE)
    set(LLAMA_BUILD_TESTS OFF CACHE BOOL "" FORCE)
    set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
    set(LLAMA_BUILD_SERVER OFF CACHE BOOL "" FORCE)
    add_subdirectory(${LLAMA_CPP_DIR} llama-build EXCLUDE_FROM_ALL)
    target_compile_definitions(privateai-llama-jni PRIVATE PRIVATEAI_LLAMA_CPP=1)
    target_include_directories(privateai-llama-jni PRIVATE ${LLAMA_CPP_DIR}/include ${LLAMA_CPP_DIR}/common)
    target_link_libraries(privateai-llama-jni llama common ${log-lib})
else()
    message(WARNING "llama.cpp not found; building JNI stub (load will fail until submodule is initialized)")
    target_link_libraries(privateai-llama-jni ${log-lib})
endif()

target_compile_features(privateai-llama-jni PRIVATE cxx_std_17)

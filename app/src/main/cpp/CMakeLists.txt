cmake_minimum_required(VERSION 3.10.2)

project("injected_plugin")

set(static-lib-location ${CMAKE_CURRENT_SOURCE_DIR}/libs)

add_library(bypass_dlfcn STATIC IMPORTED)
set_target_properties(bypass_dlfcn
        PROPERTIES IMPORTED_LOCATION ${static-lib-location}/${ANDROID_ABI}/libbypass_dlfcn.a)

add_library(cydia_substrate STATIC IMPORTED)
set_target_properties(cydia_substrate
        PROPERTIES IMPORTED_LOCATION ${static-lib-location}/${ANDROID_ABI}/libcydia-substrate.a)

add_library(
        injected_plugin
        SHARED
        main.cpp)

target_include_directories(injected_plugin
        PUBLIC
        ${CMAKE_CURRENT_SOURCE_DIR}/include/
        )

find_library(
        log-lib
        log)

target_link_libraries(
        injected_plugin
        bypass_dlfcn
        cydia_substrate
        ${log-lib})
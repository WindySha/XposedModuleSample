//  Copyright (c) 2022 Windysha
//  https://github.com/WindySha/bypass_dlfunctions

#include <jni.h>
#include <string>
#include <android/log.h>
#include <pthread.h>
#include <dlfcn.h>
#include "CydiaSubstrate.h"
#include "bypass_dlfcn.h"

#define  LOG_TAG "XposedModuleSampleNative"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , LOG_TAG, __VA_ARGS__)

// int pthread_create(pthread_t* __pthread_ptr, pthread_attr_t const* __attr, void* (*__start_routine)(void*), void*);
static int (*original_pthread_create)(pthread_t *pthread_ptr,
                                      pthread_attr_t const *attr,
                                      void *(*start_routine)(void *),
                                      void *arg);

static int replaced_pthread_create(pthread_t *pthread_ptr,
                                   pthread_attr_t const *attr,
                                   void *(*start_routine)(void *),
                                   void *arg) {
    LOGE("pthread_create is called");
    return original_pthread_create(pthread_ptr, attr, start_routine, arg);
}

static void HookPthreadCreate() {
    MSHookFunction((void *) pthread_create,
                   (void *) replaced_pthread_create,
                   (void **) &original_pthread_create);
}

void *(*original_Openat64)(void *path, void *flag, void *param);

void *replaced_Openat64(void *path, void *flag, void *param) {
    LOGE("file open is called,  path: [%s]", (char *) path);
    return original_Openat64(path, flag, param);
}


static void *(*original_openat)(void *dir_fd, void *path, void *flag, void *param);

static void *replaced_Openat(void *dir_fd, void *path, void *flag, void *param) {
    LOGE("file open is called,  path: [%s]", (char *) path);
    return original_openat(dir_fd, path, flag, param);
}

static void HookFileOpen() {
#ifdef __LP64__
    void *handle = bp_dlopen("libc.so", RTLD_NOW);
    void *address = bp_dlsym(handle, "open64");
    MSHookFunction(address,
                   (void *) replaced_Openat64,
                   (void **) &original_Openat64);
#else
    void *handle = bp_dlopen("libc.so", RTLD_NOW);
    void *address = bp_dlsym(handle, "__openat");
    LOGE(" address1 = %p", address);
    MSHookFunction(address,
                             (void *) replaced_Openat,
                             (void **) &original_openat);
#endif
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    LOGD(" JNI_OnLoad is called !!!");
    JNIEnv *env = nullptr;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        LOGE("This jni version is not supported");
        return JNI_VERSION_1_6;
    }

    HookPthreadCreate();  // hook pthread_create function
    HookFileOpen();   // hook file open function
    return JNI_VERSION_1_6;
}
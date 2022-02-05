package com.wind.plugin;

import android.os.Bundle;
import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XposedEntry implements IXposedHookLoadPackage {
    private static final String TAG = "AppInjectionTag";

    static {
        System.loadLibrary("injected_plugin");
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        Log.d(TAG, String.format(" XposedEntry， handleLoadPackage packageName: [%s] processName: [%s]", lpparam.packageName, lpparam.processName));
        doHook(lpparam);
    }

    private void doHook(XC_LoadPackage.LoadPackageParam lpparam) {
        // hook Activity Method:     protected void onCreate(Bundle savedInstanceState) {
        XposedHelpers.findAndHookMethod("android.app.Activity",
                lpparam.classLoader,
                "onCreate",
                Bundle.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Log.e(TAG, "before Activity onCreate is Called, activity -> " + param.thisObject);
                        super.beforeHookedMethod(param);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                    }
                });

        // hook Thread method:    private native static void nativeCreate(Thread t, long stackSize, boolean daemon);
        // 拦截Java层所有的线程创建
        XposedHelpers.findAndHookMethod("java.lang.Thread",
                lpparam.classLoader,
                "nativeCreate",
                Thread.class,
                long.class,
                boolean.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        // 打印线程创建的堆栈
//                        Log.e(TAG, "before Thread nativeCreate is Called!! ", new Exception("Stack For Print Thread Create!"));
                        Log.e(TAG, "before Thread nativeCreate is Called!!! ");
                        super.beforeHookedMethod(param);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                    }
                });

        // hook all the method startActivity in ContextImpl
        XposedBridge.hookAllMethods(XposedHelpers.findClass("android.app.ContextImpl", lpparam.classLoader),
                "startActivity",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Log.e(TAG, "context start activity is Called! ", new Exception("Stack For Print context startAcitivity"));
                        super.beforeHookedMethod(param);
                    }
                });
    }
}

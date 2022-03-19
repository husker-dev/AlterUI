#include <jni.h>
#include <windows.h>
#include <windowsx.h>
#include <dwmapi.h>
#include <map>
#include <iostream>

#include <shobjidl.h>

#include "callbacks.h"

#if !defined(WM_DPICHANGED)
#define WM_DPICHANGED 0x02E0
#endif

static JavaVM* jvm;
static std::map<HWND, jweak> callbackObjects;
static std::map<HWND, WNDPROC> baseProcs;
static std::map<HWND, ITaskbarList3*> taskbars;

static jmethodID onDrawCallback;
static jmethodID onClosedCallback;
static jmethodID onResizedCallback;
static jmethodID onMovedCallback;
static jmethodID onDpiChangedCallback;

// WWindow
LRESULT CALLBACK WndProc(HWND hwnd, UINT uMsg, WPARAM wParam, LPARAM lParam);
void nSetVisible(jlong hwnd, jboolean visible);
void nSetTitle(jlong hwnd, jbyte* title);
void nSetSize(jlong hwnd, jint x, jint y, jint width, jint height);
jfloat nGetDpi(jlong hwnd);
void nSetIcon(jlong hwnd, jint width, jint height, jint channels, char* data, boolean isBig);
void nSetDefaultIcon(jlong hwnd);
void nSetIconState(jlong hwnd, jint state);
void nSetIconProgress(jlong hwnd, jfloat progress);

// WindowsPlatform
jobject nGetFontData(JNIEnv* env, char* name);

void nRequestRepaint(jlong hwnd);
void nPollEvents();
void nSendEmptyMessage(jlong handle);

extern "C" {

	/*
		WWindow
	*/
	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_platforms_win_WWindow_nInitCallbacks(JNIEnv* env, jobject, jlong hwnd, jobject _object) {
		env->GetJavaVM(&jvm);
		auto object = getCallbackObject(env, _object);

		// Callbacks
		onDrawCallback = getCallbackMethod(env, _object, "onDrawCallback", "()V");
		onClosedCallback = getCallbackMethod(env, _object, "onClosedCallback", "()V");
		onResizedCallback = getCallbackMethod(env, _object, "onResizedCallback", "(IIII)V");
		onMovedCallback = getCallbackMethod(env, _object, "onMovedCallback", "(II)V");
		onDpiChangedCallback = getCallbackMethod(env, _object, "onDpiChangedCallback", "(F)V");

		callbackObjects[(HWND)hwnd] = object;
		baseProcs[(HWND)hwnd] = (WNDPROC)SetWindowLongPtr((HWND)hwnd, GWLP_WNDPROC, (LONG_PTR)&WndProc);

		SetProcessDpiAwarenessContext(DPI_AWARENESS_CONTEXT_PER_MONITOR_AWARE);
		SetProcessDpiAwarenessContext(DPI_AWARENESS_CONTEXT_PER_MONITOR_AWARE_V2);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_platforms_win_WWindow_nInit(JNIEnv*, jobject, jlong hwnd) {
		if (S_OK != CoInitialize(NULL))
			return;
		ITaskbarList3* taskbar;
		if (S_OK != CoCreateInstance(CLSID_TaskbarList, 0, CLSCTX_INPROC_SERVER, __uuidof(taskbar), (void**)&taskbar)) {
			std::cout << "ERROR when initialising taskbar" << std::endl;
			return;
		}
		taskbars[(HWND)hwnd] = taskbar;
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_platforms_win_WWindow_nSetVisible(JNIEnv*, jobject, jlong hwnd, jboolean visible) {
		nSetVisible(hwnd, visible);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_platforms_win_WWindow_nSetTitle(JNIEnv* env, jobject, jlong hwnd, jbyteArray title) {
		jbyte* _title = env->GetByteArrayElements(title, 0);
		nSetTitle(hwnd, _title);
		env->ReleaseByteArrayElements(title, _title, JNI_ABORT);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_platforms_win_WWindow_nSetSize(JNIEnv* env, jobject, jlong hwnd, jint x, jint y, jint width, jint height) {
		nSetSize(hwnd, x, y, width, height);
	}

	JNIEXPORT jfloat JNICALL Java_com_huskerdev_alter_internal_platforms_win_WWindow_nGetDpi(JNIEnv*, jobject, jlong hwnd) {
		return nGetDpi(hwnd);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_platforms_win_WWindow_nSetIcon(JNIEnv* env, jobject, jlong hwnd, jint width, jint height, jint channels, jobject data, boolean isBig) {
		nSetIcon(hwnd, width, height, channels, (char*)env->GetDirectBufferAddress(data), isBig);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_platforms_win_WWindow_nSetDefaultIcon(JNIEnv*, jobject, jlong hwnd) {
		nSetDefaultIcon(hwnd);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_platforms_win_WWindow_nSetIconState(JNIEnv*, jobject, jlong hwnd, jint state) {
		nSetIconState(hwnd, state);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_platforms_win_WWindow_nSetIconProgress(JNIEnv*, jobject, jlong hwnd, jfloat progress) {
		nSetIconProgress(hwnd, progress);
	}

	/*
		WindowsPlatform
	*/
	JNIEXPORT jobject JNICALL Java_com_huskerdev_alter_internal_platforms_win_WindowsPlatform_nGetFontData(JNIEnv* env, jobject, jobject _name) {
		char* name = (char*)env->GetDirectBufferAddress(_name);
		return nGetFontData(env, name);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_platforms_win_WWindow_nRequestRepaint(JNIEnv*, jobject, jlong hwnd) {
		nRequestRepaint(hwnd);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_platforms_win_WWindow_nPollEvents(JNIEnv*, jobject) {
		nPollEvents();
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_platforms_win_WWindow_nSendEmptyMessage(JNIEnv*, jobject, jlong hwnd) {
		nSendEmptyMessage(hwnd);
	}
}
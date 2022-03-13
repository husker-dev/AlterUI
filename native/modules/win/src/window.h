#include <jni.h>
#include <windows.h>
#include <windowsx.h>
#include <dwmapi.h>
#include <map>
#include <iostream>

#include "callbacks.h"

#if !defined(WM_DPICHANGED)
#define WM_DPICHANGED 0x02E0
#endif

static JavaVM* jvm;
static std::map<HWND, jweak> callbackObjects;
static std::map<HWND, WNDPROC> baseProcs;

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
void nSetIcon(jlong hwnd, jint width, jint height, jint channels, char* data);
void nSetDefaultIcon(jlong hwnd);

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

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_platforms_win_WWindow_nSetIcon(JNIEnv* env, jobject, jlong hwnd, jint width, jint height, jint channels, jobject data) {
		nSetIcon(hwnd, width, height, channels, (char*)env->GetDirectBufferAddress(data));
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_platforms_win_WWindow_nSetDefaultIcon(JNIEnv*, jobject, jlong hwnd) {
		nSetDefaultIcon(hwnd);
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
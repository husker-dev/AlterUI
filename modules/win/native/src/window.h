#define NOMINMAX
#define VC_EXTRALEAN

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
#if !defined(DWMWA_CAPTION_COLOR)
#define DWMWA_CAPTION_COLOR DWORD(35)
#endif
#if !defined(DWMWA_TEXT_COLOR)
#define DWMWA_TEXT_COLOR DWORD(36)
#endif

struct WindowStruct {
	jweak			callbackObject;
	WNDPROC			baseProc;
	ITaskbarList3*	taskbar;

	int style = 0;	// 0 - Default, 1 - Undecorated, 2 - NoTitle
	int mx = 0;
	int my = 0;
	bool mouseTracking = false;
};

static JavaVM* jvm;
static std::map<HWND, WindowStruct> windows;

static jmethodID onDrawCallback;
static jmethodID onClosingCallback;
static jmethodID onClosedCallback;
static jmethodID onResizedCallback;
static jmethodID onMovedCallback;
static jmethodID onDpiChangedCallback;
static jmethodID onHitTestCallback;
static jmethodID onMouseMovedCallback;
static jmethodID onMouseEnteredCallback;
static jmethodID onMouseLeavedCallback;

LRESULT CALLBACK WndProc(HWND hwnd, UINT uMsg, WPARAM wParam, LPARAM lParam) {
	switch (uMsg) {
	case WM_CLOSE:
	{
		if (!callbackBoolean(jvm, windows[hwnd].callbackObject, onClosingCallback))
			return 0;
		break;
	}
		
	case WM_DESTROY:
	{
		callback(jvm, windows[hwnd].callbackObject, onClosedCallback);
		
		windows[hwnd].baseProc(hwnd, uMsg, wParam, lParam);
		windows.erase(windows.find(hwnd));

		PostQuitMessage(0);
		break;
	}
	case WM_DPICHANGED:
	{
		callback(jvm, windows[hwnd].callbackObject, onDpiChangedCallback, (float)LOWORD(wParam) / 96);

		RECT* prcNewWindow = (RECT*)lParam;
		int iWindowX = prcNewWindow->left;
		int iWindowY = prcNewWindow->top;
		int iWindowWidth = prcNewWindow->right - prcNewWindow->left;
		int iWindowHeight = prcNewWindow->bottom - prcNewWindow->top;
		SetWindowPos(hwnd, nullptr, iWindowX, iWindowY, iWindowWidth, iWindowHeight, SWP_NOZORDER | SWP_NOACTIVATE);

		return 0;
	}
	case WM_PAINT:
	{
		callback(jvm, windows[hwnd].callbackObject, onDrawCallback);
		break;
	}
	case WM_MOVE:
	{
		RECT window;
		GetWindowRect(hwnd, &window);
		callback(jvm, windows[hwnd].callbackObject, onMovedCallback, window.left, window.top);
		break;
	}
	case WM_SIZE:
	{
		LRESULT result = windows[hwnd].baseProc(hwnd, uMsg, wParam, lParam);
		RECT window;
		GetWindowRect(hwnd, &window);

		callback(jvm, windows[hwnd].callbackObject, onResizedCallback,
			GET_X_LPARAM(lParam), GET_Y_LPARAM(lParam),
			window.right - window.left, window.bottom - window.top
		);
		return result;
	}
	case WM_SHOWWINDOW:
	case WM_ERASEBKGND:
		return TRUE;

	case WM_NCCALCSIZE:
	{
		if (wParam == TRUE && windows[hwnd].style != 0) {
			auto& params = *reinterpret_cast<NCCALCSIZE_PARAMS*>(lParam);

			// If window is maximized, then set its size equal to monitor
			// If style is 'NoTitle', then just enable border
			if (IsMaximized(hwnd)) {			
				auto info = MONITORINFO{};
				info.cbSize = sizeof(MONITORINFO);
				GetMonitorInfo(MonitorFromRect(params.rgrc, MONITOR_DEFAULTTONEAREST), &info);

				params.rgrc[0] = info.rcWork;
			} else if (windows[hwnd].style == 2)
				params.rgrc[0].right -= 1;
			return 0;
		}
		break;
	}

	case WM_NCHITTEST:
	{
		int result = callbackInt(jvm, windows[hwnd].callbackObject, onHitTestCallback,
			GET_X_LPARAM(lParam), GET_Y_LPARAM(lParam)
		);
		switch (result) {
		case 0: return HTCAPTION;
		case 1: return HTCLIENT;
		case 2: return HTNOWHERE;
		case 3: return HTMINBUTTON;
		case 4: return HTMAXBUTTON;
		case 5: return HTCLOSE;
		case 6: return HTLEFT;
		case 7: return HTRIGHT;
		case 8: return HTTOP;
		case 9: return HTBOTTOM;
		case 10: return HTTOPLEFT;
		case 11: return HTTOPRIGHT;
		case 12: return HTBOTTOMLEFT;
		case 13: return HTBOTTOMRIGHT;
		}
		break;
	}
	case WM_MOUSEMOVE:
	{
		
		if (!windows[hwnd].mouseTracking) {
			TRACKMOUSEEVENT tme;
			tme.cbSize = sizeof(TRACKMOUSEEVENT);
			tme.dwFlags = TME_LEAVE;
			tme.dwHoverTime = 1;
			tme.hwndTrack = hwnd;

			TrackMouseEvent(&tme);
			windows[hwnd].mouseTracking = true;
			callbackInt(jvm, windows[hwnd].callbackObject, onMouseEnteredCallback);

			windows[hwnd].mx = GET_X_LPARAM(lParam);
			windows[hwnd].my = GET_Y_LPARAM(lParam);
		}
		callbackInt(jvm, windows[hwnd].callbackObject, onMouseMovedCallback,
			GET_X_LPARAM(lParam), GET_Y_LPARAM(lParam), 
			wParam & MK_LBUTTON, 
			wParam & MK_MBUTTON, 
			wParam & MK_RBUTTON,
			wParam & MK_CONTROL,
			wParam & MK_SHIFT);
		break;
	}
	case WM_MOUSELEAVE:
	{
		windows[hwnd].mouseTracking = false;
		callbackInt(jvm, windows[hwnd].callbackObject, onMouseLeavedCallback);
		break;
	}
	}
	return CallWindowProc(windows[hwnd].baseProc, hwnd, uMsg, wParam, lParam);
}

extern "C" {

	/*
		WWindow
	*/
	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_platforms_win_WWindowPeer_nInitCallbacks(JNIEnv* env, jobject, jlong hwnd, jobject _object) {
		env->GetJavaVM(&jvm);

		// Callbacks
		onDrawCallback = getCallbackMethod(env, _object, "onDrawCallback", "()V");
		onClosingCallback = getCallbackMethod(env, _object, "onClosingCallback", "()Z");
		onClosedCallback = getCallbackMethod(env, _object, "onClosedCallback", "()V");
		onResizedCallback = getCallbackMethod(env, _object, "onResizedCallback", "(IIII)V");
		onMovedCallback = getCallbackMethod(env, _object, "onMovedCallback", "(II)V");
		onDpiChangedCallback = getCallbackMethod(env, _object, "onDpiChangedCallback", "(F)V");
		onHitTestCallback = getCallbackMethod(env, _object, "onHitTestCallback", "(II)I");
		onMouseMovedCallback = getCallbackMethod(env, _object, "onMouseMoved", "(IIZZZZZ)V");
		onMouseEnteredCallback = getCallbackMethod(env, _object, "onMouseEntered", "()V");
		onMouseLeavedCallback = getCallbackMethod(env, _object, "onMouseLeaved", "()V");

		WindowStruct windowStruct = {};
		windowStruct.baseProc = (WNDPROC)SetWindowLongPtr((HWND)hwnd, GWLP_WNDPROC, (LONG_PTR)&WndProc);
		windowStruct.callbackObject = getCallbackObject(env, _object);
		windows[(HWND)hwnd] = windowStruct;

		SetProcessDpiAwarenessContext(DPI_AWARENESS_CONTEXT_PER_MONITOR_AWARE);
		SetProcessDpiAwarenessContext(DPI_AWARENESS_CONTEXT_PER_MONITOR_AWARE_V2);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_platforms_win_WWindowPeer_nInit(JNIEnv*, jobject, jlong hwnd) {
		if (S_OK != CoInitialize(NULL))
			return;
		ITaskbarList3* taskbar;
		if (S_OK != CoCreateInstance(CLSID_TaskbarList, 0, CLSCTX_INPROC_SERVER, __uuidof(taskbar), (void**)&taskbar)) {
			std::cout << "ERROR when initialising taskbar" << std::endl;
			return;
		}
		windows[(HWND)hwnd].taskbar = taskbar;
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_platforms_win_WWindowPeer_nSetVisible(JNIEnv*, jobject, jlong hwnd, jboolean visible) {
		ShowWindow((HWND)hwnd, visible ? SW_SHOW : SW_HIDE);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_platforms_win_WWindowPeer_nSetTitle(JNIEnv* env, jobject, jlong hwnd, jobject _title) {
		char* title = (char*)env->GetDirectBufferAddress(_title);
		SetWindowTextW((HWND)hwnd, (LPCWSTR)title);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_platforms_win_WWindowPeer_nSetSize(JNIEnv* env, jobject, jlong hwnd, jint x, jint y, jint width, jint height) {
		SetWindowPos((HWND)hwnd, nullptr, x, y, width, height, SWP_NOACTIVATE | SWP_NOZORDER);
	}

	JNIEXPORT jfloat JNICALL Java_com_huskerdev_alter_internal_platforms_win_WWindowPeer_nGetDpi(JNIEnv*, jobject, jlong hwnd) {
		return (float)GetDpiForWindow((HWND)hwnd) / 96;
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_platforms_win_WWindowPeer_nSetIcon(JNIEnv* env, jobject, jlong hwnd, jint width, jint height, jint channels, jobject _data, boolean isBig) {
		char* data = (char*)env->GetDirectBufferAddress(_data);

		char* bitmap = new char[width * height * 4];

		for (unsigned int i = 0, s = 0;
			i < width * height * 4;
			i += 4, s += channels
			) {
			bitmap[i] = data[s + 2];
			bitmap[i + 1] = data[s + 1];
			bitmap[i + 2] = data[s];
			bitmap[i + 3] = channels == 3 ? 255 : data[s + 3];
		}

		ICONINFO iconInfo = {};
		iconInfo.hbmColor = CreateBitmap(width, height, 1, 32, bitmap);
		iconInfo.hbmMask = CreateCompatibleBitmap(GetDC((HWND)hwnd), width, height);
		HICON hIcon = CreateIconIndirect(&iconInfo);

		DeleteObject(iconInfo.hbmMask);
		DeleteObject(iconInfo.hbmColor);
		delete[] bitmap;

		if (isBig) {
			SendMessage((HWND)hwnd, WM_SETICON, ICON_BIG, (LPARAM)hIcon);
		} else {
			SendMessage((HWND)hwnd, WM_SETICON, ICON_SMALL, (LPARAM)hIcon);
			SendMessage((HWND)hwnd, WM_SETICON, ICON_SMALL2, (LPARAM)hIcon);
		}
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_platforms_win_WWindowPeer_nSetDefaultIcon(JNIEnv*, jobject, jlong hwnd) {
		SHSTOCKICONINFO sii;
		sii.cbSize = sizeof(sii);
		SHGetStockIconInfo(SIID_APPLICATION, SHGSI_ICON | SHGSI_LARGEICON, &sii);

		LPARAM icon = (LPARAM)sii.hIcon;
		SendMessage((HWND)hwnd, WM_SETICON, ICON_SMALL, icon);
		SendMessage((HWND)hwnd, WM_SETICON, ICON_BIG, icon);
		SendMessage((HWND)hwnd, WM_SETICON, ICON_SMALL2, icon);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_platforms_win_WWindowPeer_nSetIconState(JNIEnv*, jobject, jlong hwnd, jint state) {
		TBPFLAG winState = TBPF_NOPROGRESS;
		if (state == 0)
			winState = TBPF_NORMAL;
		else if (state == 1)
			winState = TBPF_PAUSED;
		else if (state == 2)
			winState = TBPF_ERROR;
		else if (state == 3)
			winState = TBPF_INDETERMINATE;

		windows[(HWND)hwnd].taskbar->SetProgressState((HWND)hwnd, winState);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_platforms_win_WWindowPeer_nSetIconProgress(JNIEnv*, jobject, jlong hwnd, jfloat progress) {
		int range = 300;
		windows[(HWND)hwnd].taskbar->SetProgressValue((HWND)hwnd, progress * range, range);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_platforms_win_WWindowPeer_nSetStyle(JNIEnv*, jobject, jlong hwnd, jint style) {
		windows[(HWND)hwnd].style = style;

		RECT window;
		GetWindowRect((HWND)hwnd, &window);
		int width = window.right - window.left;
		int height = window.bottom - window.top;
		SetWindowPos((HWND)hwnd, nullptr, 0, 0, width + 1, height, SWP_FRAMECHANGED | SWP_NOMOVE | SWP_NOACTIVATE);
		SetWindowPos((HWND)hwnd, nullptr, 0, 0, width, height, SWP_FRAMECHANGED | SWP_NOMOVE | SWP_NOACTIVATE);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_platforms_win_WWindowPeer_nSetWindowTitleColor(JNIEnv*, jobject, jlong hwnd, jint color) {
		DwmSetWindowAttribute((HWND)hwnd, DWMWA_CAPTION_COLOR, color == -1 ? nullptr : &color, sizeof(COLORREF));
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_platforms_win_WWindowPeer_nSetWindowTextColor(JNIEnv*, jobject, jlong hwnd, jint color) {
		DwmSetWindowAttribute((HWND)hwnd, DWMWA_TEXT_COLOR, color == -1 ? nullptr : &color, sizeof(COLORREF));
	}

	JNIEXPORT jint JNICALL Java_com_huskerdev_alter_internal_platforms_win_WWindowPeer_nGetWindowMouseX(JNIEnv* env, jobject, jlong hwnd) {
		POINT point;
		GetCursorPos(&point);
		ScreenToClient((HWND)hwnd, &point);
		return point.x;
	}

	JNIEXPORT jint JNICALL Java_com_huskerdev_alter_internal_platforms_win_WWindowPeer_nGetWindowMouseY(JNIEnv* env, jobject, jlong hwnd) {
		POINT point;
		GetCursorPos(&point);
		ScreenToClient((HWND)hwnd, &point);
		return point.y;
	}

	/*
		WindowsPlatform
	*/
	JNIEXPORT jobject JNICALL Java_com_huskerdev_alter_internal_platforms_win_WindowsPlatform_nGetFontData(JNIEnv* env, jobject, jobject _name) {
		char* name = (char*)env->GetDirectBufferAddress(_name);

		LOGFONT logFont = {};
		HGLOBAL hGlobal = NULL;
		HDC hDC = NULL;
		LPVOID ptr = NULL;

		hDC = CreateDC(L"DISPLAY", NULL, NULL, NULL);

		wcscpy_s(logFont.lfFaceName, (LPCWSTR)name);
		HGDIOBJ hFont = CreateFontIndirect(&logFont);
		SelectObject(hDC, hFont);

		DWORD fontDataLen = GetFontData(hDC, 0, 0, NULL, 0);
		if (fontDataLen == GDI_ERROR)
			return NULL;

		hGlobal = GlobalAlloc(GMEM_MOVEABLE, fontDataLen);
		ptr = GlobalLock(hGlobal);

		GetFontData(hDC, 0, 0, ptr, fontDataLen);
		GlobalUnlock(hGlobal);

		return env->NewDirectByteBuffer(ptr, fontDataLen);
	}

	JNIEXPORT jint JNICALL Java_com_huskerdev_alter_internal_platforms_win_WindowsPlatform_nGetMouseX(JNIEnv* env, jobject) {
		POINT point;
		GetCursorPos(&point);
		return point.x;
	}

	JNIEXPORT jint JNICALL Java_com_huskerdev_alter_internal_platforms_win_WindowsPlatform_nGetMouseY(JNIEnv* env, jobject) {
		POINT point;
		GetCursorPos(&point);
		return point.y;
	}

	JNIEXPORT jfloat JNICALL Java_com_huskerdev_alter_internal_platforms_win_WindowsPlatform_nGetMouseDpi(JNIEnv* env, jobject) {
		return 1.0f;
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_platforms_win_WWindowPeer_nRequestRepaint(JNIEnv*, jobject, jlong hwnd) {
		RedrawWindow((HWND)hwnd, NULL, NULL, RDW_INVALIDATE | RDW_UPDATENOW);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_platforms_win_WWindowPeer_nPollEvents(JNIEnv*, jobject) {
		MSG msg;
		if (GetMessage(&msg, 0, 0, 0)) {
			TranslateMessage(&msg);
			DispatchMessage(&msg);
		}
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_platforms_win_WWindowPeer_nTakeEvents(JNIEnv*, jobject) {
		MSG msg;
		if (PeekMessage(&msg, NULL, 0, 0, PM_REMOVE)) {
			TranslateMessage(&msg);
			DispatchMessage(&msg);
		}
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_platforms_win_WWindowPeer_nSendEmptyMessage(JNIEnv*, jobject, jlong hwnd) {
		PostMessage((HWND)hwnd, 0, 0, 0);
	}
}
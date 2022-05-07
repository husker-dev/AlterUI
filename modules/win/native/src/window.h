#define NOMINMAX
#define VC_EXTRALEAN

#include <jni.h>
#include <windows.h>
#include <windowsx.h>
#include <dwmapi.h>
#include <map>
#include <iostream>

#include <shobjidl.h>
#include <shlobj_core.h>

#include "callbacks.h"

#if !defined(WM_DPICHANGED)
#define WM_DPICHANGED 0x02E0
#endif
#include <string>
#if !defined(DWMWA_CAPTION_COLOR)
#define DWMWA_CAPTION_COLOR DWORD(35)
#endif
#include <vector>
#if !defined(DWMWA_TEXT_COLOR)
#define DWMWA_TEXT_COLOR DWORD(36)
#endif

WCHAR* getRegistryStringValue(HKEY hkey, LPCWSTR value) {
	WCHAR id[512];
	DWORD size = 512;
	DWORD type = REG_SZ;
	RegQueryValueExW(hkey, value, NULL, &type, reinterpret_cast<LPBYTE>(&id), &size);
	return id;
}

char* getRegistryBinaryValue(HKEY hkey, LPCWSTR value) {
	char id[512];
	DWORD size = 512;
	DWORD type = REG_BINARY;
	RegQueryValueExW(hkey, value, NULL, &type, reinterpret_cast<LPBYTE>(&id), &size);
	return id;
}

WCHAR* getSubKeyName(HKEY hkey, DWORD index) {
	WCHAR buffer[255];
	DWORD size = 255;
	RegEnumKeyExW(hkey, index, buffer, &size, NULL, NULL, NULL, NULL);
	return buffer;
}

DWORD getSubKeysCount(HKEY hkey) {
	DWORD count = 0;
	RegQueryInfoKey(hkey, NULL, NULL, NULL, &count, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
	return count;
}

HKEY openRegistry(HKEY parent, LPCWSTR value) {
	HKEY hKey;
	RegOpenKeyExW(parent, value, 0, KEY_READ, &hKey);
	return hKey;
}

struct WindowStruct {
	jweak			callbackObject;
	WNDPROC			baseProc;
	ITaskbarList3* taskbar;

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
			}
			else if (windows[hwnd].style == 2)
				params.rgrc[0].right -= 1;
			return 0;
		}
		break;
	}

	case WM_NCHITTEST:
	{
		POINT point = {GET_X_LPARAM(lParam), GET_Y_LPARAM(lParam)};
		ScreenToClient((HWND)hwnd, &point);

		int result = callbackInt(jvm, windows[hwnd].callbackObject, onHitTestCallback,
			point.x, point.y
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
		static const MARGINS frame = { 0, 0, 0, 0 };
		DwmExtendFrameIntoClientArea((HWND)hwnd, &frame);
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
		}
		else {
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

	JNIEXPORT jlong JNICALL Java_com_huskerdev_alter_internal_platforms_win_WMonitorPeer_nGetPrimary(JNIEnv*, jobject) {
		const POINT ptZero = { 0, 0 };
		return (jlong)MonitorFromPoint(ptZero, MONITOR_DEFAULTTOPRIMARY);
	}

	const char* getDisplayEDID(DISPLAY_DEVICE dd) {
		std::wstring id = std::wstring(dd.DeviceID);
		std::wstring driver = id.substr(id.find(L"{"), id.length() - id.find(L"{"));

		HKEY hKey = openRegistry(HKEY_LOCAL_MACHINE, L"SYSTEM\\CurrentControlSet\\Enum\\DISPLAY");
		DWORD monitorsCount = getSubKeysCount(hKey);

		for (int i = 0; i < monitorsCount; i++) {
			HKEY subFolder1 = openRegistry(hKey, getSubKeyName(hKey, i));
			HKEY subFolder2 = openRegistry(subFolder1, getSubKeyName(subFolder1, 0));

			auto id = getRegistryStringValue(subFolder2, L"Driver");

			if (std::wstring(id).compare(driver) == 0) {
				HKEY subFolder3 = openRegistry(subFolder2, L"Device Parameters");

				RegCloseKey(hKey);
				RegCloseKey(subFolder1);
				RegCloseKey(subFolder2);
				return getRegistryBinaryValue(subFolder3, L"EDID");
			}
			RegCloseKey(subFolder1);
			RegCloseKey(subFolder2);
		}
		RegCloseKey(hKey);
	}

	JNIEXPORT jbyteArray JNICALL Java_com_huskerdev_alter_internal_platforms_win_WMonitorPeer_nGetEDID(JNIEnv* env, jobject, jlong handle) {
		MONITORINFOEX info;
		info.cbSize = sizeof(info);
		GetMonitorInfo((HMONITOR)handle, &info);
		std::wstring deviceName = info.szDevice;

		DISPLAY_DEVICE dd;
		dd.cb = sizeof(dd);

		int deviceIndex = 0;
		while (EnumDisplayDevices(0, deviceIndex, &dd, 0)) {
			int monitorIndex = 0;
			while (EnumDisplayDevices(std::wstring(dd.DeviceName).c_str(), monitorIndex, &dd, 0)) {
				if (std::wstring(dd.DeviceName).find(deviceName) == 0) {
					auto charArray = env->NewByteArray(128);
					env->SetByteArrayRegion(charArray, 0, 128, (const jbyte*)getDisplayEDID(dd));
					return charArray;
				}
				monitorIndex++;
			}
			deviceIndex++;
		}
		return 0;
	}

	jlong* monitors;
	int monitorsCount = 0;
	static BOOL CALLBACK MonitorFill(HMONITOR hMon, HDC hdc, LPRECT lprcMonitor, LPARAM pData) {
		monitors[monitorsCount++] = (jlong)hMon;
		return TRUE;
	}
	static BOOL CALLBACK MonitorCount(HMONITOR hMon, HDC hdc, LPRECT lprcMonitor, LPARAM pData) {
		monitorsCount++;
		return TRUE;
	}

	JNIEXPORT jlongArray JNICALL Java_com_huskerdev_alter_internal_platforms_win_WMonitorPeer_nGetAll(JNIEnv* env, jobject) {
		monitorsCount = 0;
		EnumDisplayMonitors(NULL, NULL, MonitorCount, NULL);

		monitors = new jlong[monitorsCount];
		monitorsCount = 0;
		EnumDisplayMonitors(NULL, NULL, MonitorFill, NULL);

		auto handles = env->NewLongArray(monitorsCount);
		env->SetLongArrayRegion(handles, 0, monitorsCount, monitors);
		delete[] monitors;
		return handles;
	}

	JNIEXPORT jint JNICALL Java_com_huskerdev_alter_internal_platforms_win_WMonitorPeer_nGetWidth(JNIEnv* env, jobject, jlong handle) {
		MONITORINFOEX info;
		info.cbSize = sizeof(info);
		GetMonitorInfo((HMONITOR)handle, &info);
		return info.rcMonitor.right - info.rcMonitor.left;
	}

	JNIEXPORT jint JNICALL Java_com_huskerdev_alter_internal_platforms_win_WMonitorPeer_nGetHeight(JNIEnv* env, jobject, jlong handle) {
		MONITORINFOEX info;
		info.cbSize = sizeof(info);
		GetMonitorInfo((HMONITOR)handle, &info);
		return info.rcMonitor.bottom - info.rcMonitor.top;
	}

	JNIEXPORT jint JNICALL Java_com_huskerdev_alter_internal_platforms_win_WMonitorPeer_nGetX(JNIEnv* env, jobject, jlong handle) {
		MONITORINFOEX info;
		info.cbSize = sizeof(info);
		GetMonitorInfo((HMONITOR)handle, &info);
		return info.rcMonitor.left;
	}

	JNIEXPORT jint JNICALL Java_com_huskerdev_alter_internal_platforms_win_WMonitorPeer_nGetY(JNIEnv* env, jobject, jlong handle) {
		MONITORINFOEX info;
		info.cbSize = sizeof(info);
		GetMonitorInfo((HMONITOR)handle, &info);
		return info.rcMonitor.top;
	}


	JNIEXPORT jfloat JNICALL Java_com_huskerdev_alter_internal_platforms_win_WMonitorPeer_nGetDpi(JNIEnv* env, jobject, jlong handle) {
		typedef enum MONITOR_DPI_TYPE {
			MDT_EFFECTIVE_DPI = 0,
			MDT_ANGULAR_DPI = 1,
			MDT_RAW_DPI = 2,
			MDT_DEFAULT = MDT_EFFECTIVE_DPI
		} MONITOR_DPI_TYPE;
		typedef HRESULT(CALLBACK* GetDpiForMonitor_)(HMONITOR, MONITOR_DPI_TYPE, UINT*, UINT*);

		static HINSTANCE shcore = LoadLibrary(L"Shcore.dll");
		if (shcore != nullptr) {
			if (auto getDpiForMonitor = GetDpiForMonitor_(GetProcAddress(shcore, "GetDpiForMonitor"))) {
				UINT xScale, yScale;
				getDpiForMonitor((HMONITOR)handle, MDT_DEFAULT, &xScale, &yScale);

				return (float)xScale / 96;
			}
		}
		return 1.0;
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

	JNIEXPORT jint JNICALL Java_com_huskerdev_alter_internal_platforms_win_WindowsPlatform_nShowMessage(JNIEnv* env, jobject, jlong hwnd, jobject _title, jobject _content, jint _icon, jint _type) {
		LPCWSTR title = (LPCWSTR)env->GetDirectBufferAddress(_title);
		LPCWSTR content = (LPCWSTR)env->GetDirectBufferAddress(_content);

		int icon = 0;
		int type = 0;

		switch (_icon) {
		case 0:
			icon = 0; break;
		case 1:
			icon = MB_ICONERROR; break;
		case 2:
			icon = MB_ICONWARNING; break;
		case 3:
			icon = MB_ICONQUESTION; break;
		case 4:
			icon = MB_ICONINFORMATION; break;
		}

		switch (_type) {
		case 0:
			type = MB_ABORTRETRYIGNORE; break;
		case 1:
			type = MB_CANCELTRYCONTINUE; break;
		case 2:
			type = MB_OK; break;
		case 3:
			type = MB_OKCANCEL; break;
		case 4:
			type = MB_RETRYCANCEL; break;
		case 5:
			type = MB_YESNO; break;
		case 6:
			type = MB_YESNOCANCEL; break;
		}

		return MessageBox(
			(HWND)hwnd,
			content,
			title,
			icon | type
		);
	}

	JNIEXPORT jobject JNICALL Java_com_huskerdev_alter_internal_platforms_win_WindowsPlatform_nShowFileDialog(JNIEnv* env, jobject, 
		jlong hwnd, 
		jboolean isSave,
		jboolean onlyDirectories,
		jboolean multipleSelect, 
		jobjectArray _filters,
		jobject _dir,
		jobject _title
	) {
		wchar_t* dir = (wchar_t*)env->GetDirectBufferAddress(_dir);
		wchar_t* title = (wchar_t*)env->GetDirectBufferAddress(_title);

		int filtersCount = env->GetArrayLength(_filters) / 2;
		COMDLG_FILTERSPEC* filters = new COMDLG_FILTERSPEC[filtersCount];

		for (int i = 0; i < env->GetArrayLength(_filters); i += 2) {
			jbyteArray filterTitle = (jbyteArray)env->GetObjectArrayElement(_filters, i);
			jbyteArray filterExt = (jbyteArray)env->GetObjectArrayElement(_filters, i + 1);

			filters[i / 2] = {
				(wchar_t*)env->GetByteArrayElements(filterTitle, 0),
				(wchar_t*)env->GetByteArrayElements(filterExt, 0)
			};
		}

		CoInitialize(NULL);

		std::wstring result;
		IShellItem* directory;
		SHCreateItemFromParsingName(dir, nullptr, IID_PPV_ARGS(&directory));
		auto options = FOS_FORCEFILESYSTEM |
			(onlyDirectories ? FOS_PICKFOLDERS : 0) |
			(multipleSelect ? FOS_ALLOWMULTISELECT : 0);

		if (isSave) {
			IFileSaveDialog* fileDialog = NULL;
			CoCreateInstance(CLSID_FileSaveDialog, NULL, CLSCTX_INPROC_SERVER, IID_PPV_ARGS(&fileDialog));

			fileDialog->SetFileTypes(filtersCount, filters);
			fileDialog->SetTitle(title);
			fileDialog->SetFolder(directory);
			fileDialog->SetOptions(options);

			if (SUCCEEDED(fileDialog->Show((HWND)hwnd))) {
				IShellItem* item;
				fileDialog->GetResult(&item);

				PWSTR filePath;
				item->GetDisplayName(SIGDN_FILESYSPATH, &filePath);
				result = std::wstring(filePath);

				// Release
				CoTaskMemFree(filePath);
				item->Release();
				fileDialog->Release();
				CoUninitialize();
			}
		}
		else {
			IFileOpenDialog* fileDialog = NULL;
			CoCreateInstance(CLSID_FileOpenDialog, NULL, CLSCTX_INPROC_SERVER, IID_PPV_ARGS(&fileDialog));

			fileDialog->SetFileTypes(filtersCount, filters);
			fileDialog->SetTitle(title);
			fileDialog->SetFolder(directory);
			fileDialog->SetOptions(options);

			if (SUCCEEDED(fileDialog->Show((HWND)hwnd))) {
				IShellItemArray* items;
				fileDialog->GetResults(&items);

				DWORD count;
				items->GetCount(&count);

				for (DWORD i = 0; i < count; i++) {
					IShellItem* item;
					items->GetItemAt(i, &item);

					PWSTR filePath;
					item->GetDisplayName(SIGDN_FILESYSPATH, &filePath);

					if (result.length() != 0)
						result += L";";
					result += filePath;

					CoTaskMemFree(filePath);
					item->Release();
				}

				items->Release();
				fileDialog->Release();
			}
		}
		CoUninitialize();

		return env->NewDirectByteBuffer((char*)result.c_str(), result.length() * 2);
	}
}
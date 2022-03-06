#include "window.h"



LRESULT CALLBACK WndProc(HWND hwnd, UINT uMsg, WPARAM wParam, LPARAM lParam) {
    switch (uMsg) {
    case WM_NCDESTROY:
    {
        return 0;
    }
    case WM_DPICHANGED:
    {
        callback(jvm, callbackObjects[hwnd], onDpiChangedCallback, (float)LOWORD(wParam) / 96);
        
        RECT* prcNewWindow = (RECT*)lParam;
        int iWindowX = prcNewWindow->left;
        int iWindowY = prcNewWindow->top;
        int iWindowWidth = prcNewWindow->right - prcNewWindow->left;
        int iWindowHeight = prcNewWindow->bottom - prcNewWindow->top;
        SetWindowPos(hwnd, nullptr, iWindowX, iWindowY, iWindowWidth, iWindowHeight, SWP_NOZORDER | SWP_NOACTIVATE);

        SendMessage(hwnd, WM_PAINT, NULL, NULL);

        return 0;
    }
    case WM_DESTROY:
    {
        callback(jvm, callbackObjects[hwnd], onClosedCallback);

        // Call base WinProc last time, and delete it
        baseProcs[hwnd](hwnd, uMsg, wParam, lParam);
        baseProcs.erase(baseProcs.find(hwnd));

        // Clear all maps
        callbackObjects.erase(callbackObjects.find(hwnd));

        return 0;
    }
    case WM_PAINT:
    {
        callback(jvm, callbackObjects[hwnd], onDrawCallback);
        break;
    }
    case WM_MOVE:
    {
        RECT window;
        GetWindowRect(hwnd, &window);
        callback(jvm, callbackObjects[hwnd], onMovedCallback, window.left, window.top);
        break;
    }
    case WM_SIZE:
    {
        RECT window;
        GetWindowRect(hwnd, &window);

        callback(jvm, callbackObjects[hwnd], onResizedCallback, 
            GET_X_LPARAM(lParam), GET_Y_LPARAM(lParam), 
            window.right - window.left, window.bottom - window.top
        );
        break;
    }
    case WM_SHOWWINDOW:
    case WM_ERASEBKGND:
        //SendMessage(hwnd, WM_PAINT, NULL, NULL);
        return TRUE;
    }
    
    return baseProcs[hwnd](hwnd, uMsg, wParam, lParam);
}

void nSetVisible(jlong hwnd, jboolean visible) {
	ShowWindow((HWND)hwnd, visible ? SW_SHOW : SW_HIDE);
}

void nSetTitle(jlong hwnd, jbyte* title) {
	SetWindowText((HWND)hwnd, (LPCWSTR)title);
}

void nSetSize(jlong hwnd, jint x, jint y, jint width, jint height) {
    SetWindowPos((HWND)hwnd, nullptr, x, y, width, height, SWP_NOACTIVATE | SWP_NOZORDER);
}

jfloat nGetDpi(jlong hwnd) {
    return (float)GetDpiForWindow((HWND)hwnd) / 96;
}

void nSetBackground(jlong hwnd, int color) {
    HBRUSH brush = CreateSolidBrush(color);
    SetClassLongPtr((HWND)hwnd, GCLP_HBRBACKGROUND, (LONG_PTR)brush);
}

// WindowsPlatform

jobject nGetFontData(JNIEnv* env, char* name) {
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

void nRequestRepaint(jlong hwnd) {
    RedrawWindow((HWND)hwnd, NULL, NULL, RDW_INVALIDATE | RDW_UPDATENOW);
}

void nPollEvents() {
    MSG msg;
    if (GetMessage(&msg, NULL, 0, 0) > 0) {
        TranslateMessage(&msg);
        DispatchMessage(&msg);
    }
}

void nSendEmptyMessage(jlong hwnd) {
    PostMessage((HWND)hwnd, 0, 0, 0);
}
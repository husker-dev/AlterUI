#include "window.h"

LRESULT CALLBACK WndProc(HWND hwnd, UINT uMsg, WPARAM wParam, LPARAM lParam) {
    switch (uMsg) {
    case WM_NCDESTROY:
    {
        return 0;
    }
    case WM_PAINT:
    {
        callback(jvm, callbackObjects[hwnd], onDrawCallback);
        break;
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
    case WM_MOVE:
    {
        callback(jvm, callbackObjects[hwnd], onMovedCallback, LOWORD(lParam), HIWORD(lParam));
        break;
    }
    case WM_SIZE:
    {
        callback(jvm, callbackObjects[hwnd], onResizedCallback, LOWORD(lParam), HIWORD(lParam));
        SendMessage(hwnd, WM_PAINT, NULL, NULL);
        break;
    }
    case WM_SHOWWINDOW:
    case WM_ERASEBKGND:
        SendMessage(hwnd, WM_PAINT, NULL, NULL);
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

void nSetBackground(jlong hwnd, int color) {
    HBRUSH brush = CreateSolidBrush(color);
    SetClassLongPtr((HWND)hwnd, GCLP_HBRBACKGROUND, (LONG_PTR)brush);
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
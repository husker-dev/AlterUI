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

        return 0;
    }
    case WM_DESTROY:
    {
        callback(jvm, callbackObjects[hwnd], onClosedCallback);

        // Call base WinProc last time, and delete it
        baseProcs[hwnd](hwnd, uMsg, wParam, lParam);
        baseProcs.erase(baseProcs.find(hwnd));

        // Clear callbacks
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

void nSetIcon(jlong hwnd, jint width, jint height, jint channels, char* data, boolean isBig) {
    int size = width > height ? width : height;

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

void nSetDefaultIcon(jlong hwnd) {
    SHSTOCKICONINFO sii;
    sii.cbSize = sizeof(sii);
    SHGetStockIconInfo(SIID_APPLICATION, SHGSI_ICON | SHGSI_LARGEICON, &sii);

    LPARAM icon = (LPARAM)sii.hIcon;
    SendMessage((HWND)hwnd, WM_SETICON, ICON_SMALL, icon);
    SendMessage((HWND)hwnd, WM_SETICON, ICON_BIG, icon);
    SendMessage((HWND)hwnd, WM_SETICON, ICON_SMALL2, icon);
}

void nSetIconState(jlong hwnd, jint state) {
    TBPFLAG winState = TBPF_NOPROGRESS;
    if (state == 0)
        winState = TBPF_NORMAL;
    else if(state == 1)
        winState = TBPF_PAUSED;
    else if(state == 2)
        winState = TBPF_ERROR;
    else if (state == 3)
        winState = TBPF_INDETERMINATE;

    taskbars[(HWND)hwnd]->SetProgressState((HWND)hwnd, winState);
}

void nSetIconProgress(jlong hwnd, jfloat progress) {
    int range = 300;
    taskbars[(HWND)hwnd]->SetProgressValue((HWND)hwnd, progress * range, range);
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
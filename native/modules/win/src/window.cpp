#include "window.h"

LRESULT hit_test(HWND hwnd, POINT cursor) {
    const POINT border{
        ::GetSystemMetrics(SM_CXFRAME) + ::GetSystemMetrics(SM_CXPADDEDBORDER),
        ::GetSystemMetrics(SM_CYFRAME) + ::GetSystemMetrics(SM_CXPADDEDBORDER)
    };
    RECT window;
    if (!::GetWindowRect(hwnd, &window)) {
        return HTNOWHERE;
    }

    enum region_mask {
        client = 0b0000,
        left = 0b0001,
        right = 0b0010,
        top = 0b0100,
        bottom = 0b1000,
    };

    const auto result =
        left * (cursor.x < (window.left + border.x)) |
        right * (cursor.x >= (window.right - border.x)) |
        top * (cursor.y < (window.top + border.y)) |
        bottom * (cursor.y >= (window.bottom - border.y));

    switch (result) {
    case left: return HTLEFT;
    case right: return HTRIGHT;
    case top: return HTTOP;
    case bottom: return HTBOTTOM;
    case top | left: return HTTOPLEFT;
    case top | right: return HTTOPRIGHT;
    case bottom | left: return HTBOTTOMLEFT;
    case bottom | right: return HTBOTTOMRIGHT;
    case client: return HTCAPTION;
    default: return HTNOWHERE;
    }
}

LRESULT CALLBACK WndProc(HWND hwnd, UINT uMsg, WPARAM wParam, LPARAM lParam) {
    switch (uMsg) {
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
    case WM_DESTROY:
    {
        callback(jvm, windows[hwnd].callbackObject, onClosedCallback);

        // Call base WinProc last time, and delete it
        windows[hwnd].baseProc(hwnd, uMsg, wParam, lParam);
        windows.erase(windows.find(hwnd));

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

            if (IsMaximized(hwnd)) {
                // Setting window size equal to monitor
                auto info = MONITORINFO {};
                info.cbSize = sizeof(MONITORINFO);
                GetMonitorInfo(MonitorFromRect(params.rgrc, MONITOR_DEFAULTTONEAREST), &info);

                params.rgrc[0] = info.rcWork;
            } else if(windows[hwnd].style == 2){
                // Used to enable border in NoTitle style
                params.rgrc[0].right -= 1;
            }
            return 0;
        }
        break;
    }

    case WM_NCHITTEST:
    {
        if (windows[hwnd].style != 0) {
            return hit_test(hwnd, POINT{
                        GET_X_LPARAM(lParam),
                        GET_Y_LPARAM(lParam)
                });
        }
        break;
    }

    }
    return windows[hwnd].baseProc(hwnd, uMsg, wParam, lParam);
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

    windows[(HWND)hwnd].taskbar->SetProgressState((HWND)hwnd, winState);
}

void nSetIconProgress(jlong hwnd, jfloat progress) {
    int range = 300;
    windows[(HWND)hwnd].taskbar->SetProgressValue((HWND)hwnd, progress * range, range);
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
#if defined WIN

#define UNICODE
#include "gl_win.h"

static std::map<HWND, HGLRC> rc_list;
static std::map<HWND, HDC> dc_list;

static bool openglInitialiased = false;
static int gl_major_version;
static int gl_minor_version;

void throwError(const char* text) {
    std::cout << "[ERROR] Internal OpenGL error: " << text << std::endl;
    std::cout << glGetError() << std::endl;
    MessageBoxA(NULL, text, "Internal OpenGL error", MB_OK | MB_ICONERROR);
    exit(1);
}

LRESULT CALLBACK WndProc(HWND hwnd, UINT uMsg, WPARAM wParam, LPARAM lParam) {
    switch (uMsg) {
    case WM_DESTROY:
    {
        wglDeleteContext(rc_list[hwnd]);
        ReleaseDC(hwnd, dc_list[hwnd]);
        return 0;
    }
    }
    return DefWindowProc(hwnd, uMsg, wParam, lParam);
}

jlong nCreateWindow(jlong shareWith) {
    // Apply basic pixel format
    PIXELFORMATDESCRIPTOR pfd = {};
    pfd.nSize = sizeof(pfd);
    pfd.dwFlags = PFD_DOUBLEBUFFER | PFD_SUPPORT_OPENGL | PFD_DRAW_TO_WINDOW;
    pfd.iPixelType = PFD_TYPE_RGBA;
    pfd.cColorBits = 24;
    pfd.cDepthBits = 16;
    pfd.iLayerType = PFD_MAIN_PLANE;

    if (!openglInitialiased) {
        // Register window class
        WNDCLASS wc = {};
        wc.style = CS_HREDRAW | CS_VREDRAW | CS_OWNDC;
        wc.lpfnWndProc = (WNDPROC)WndProc;
        wc.hInstance = GetModuleHandle(NULL);
        wc.hIcon = LoadIcon(NULL, IDI_APPLICATION);
        wc.hCursor = LoadCursor(NULL, IDC_ARROW);
        wc.lpszClassName = L"alterui_gl";
        RegisterClass(&wc);

        // Create dummy window
        HWND hwnd = CreateWindowEx(
            WS_EX_LAYERED,
            L"alterui_gl", L"",
            WS_OVERLAPPEDWINDOW,
            0, 0,
            100, 100,
            NULL, NULL,
            GetModuleHandle(NULL),
            NULL);
        HDC dc = GetDC(hwnd);

        int pixel_format = 0;
        if (!(pixel_format = ChoosePixelFormat(dc, &pfd)))
            throwError("Failed to choose pixel format");
        if (!SetPixelFormat(dc, pixel_format, &pfd))
            throwError("Failed to set pixel format");

        // Create basic context
        HGLRC rc = wglCreateContext(dc);
        wglMakeCurrent(dc, rc);

        // Load GLAD
        if (!gladLoadGL())
            throwError("Failed to load GLAD functions");
        if (!gladLoadWGL(dc))
            throwError("Failed to load GLAD-WGL functions");
    
        // Getting last OpenGL version
        gl_major_version = glGetString(GL_VERSION)[0] - '0';
        gl_minor_version = glGetString(GL_VERSION)[2] - '0';

        // Destroy dummy window
        wglMakeCurrent(nullptr, nullptr);
        wglDeleteContext(rc);
        ReleaseDC(hwnd, dc);
        DestroyWindow(hwnd);

        openglInitialiased = true;
    }

    // Create window
    HWND hwnd = CreateWindowEx(
        WS_EX_APPWINDOW,
        L"alterui_gl", L"",
        WS_CLIPSIBLINGS | WS_CLIPCHILDREN | WS_SYSMENU | WS_MINIMIZEBOX | WS_CAPTION | WS_MAXIMIZEBOX | WS_THICKFRAME,
        0, 0,
        100, 100,
        NULL, NULL,
        GetModuleHandle(NULL),
        NULL);
    EnableNonClientDpiScaling(hwnd);
    HDC dc = GetDC(hwnd);

    // Create extended pixel format
    int pixel_format_arb;
    UINT pixel_formats_count;

    int pixel_attributes[] = {
        WGL_DRAW_TO_WINDOW_ARB, GL_TRUE,
        WGL_SUPPORT_OPENGL_ARB, GL_TRUE,
        WGL_DOUBLE_BUFFER_ARB, GL_TRUE,
        WGL_COLOR_BITS_ARB, 24,
        WGL_DEPTH_BITS_ARB, 16,
        WGL_ACCELERATION_ARB, WGL_FULL_ACCELERATION_ARB,
        WGL_PIXEL_TYPE_ARB, WGL_TYPE_RGBA_ARB,
        0
    };
    if (!wglChoosePixelFormatARB(dc, pixel_attributes, NULL, 1, &pixel_format_arb, &pixel_formats_count))
        throwError("Failed to choose supported pixel format (WGL)");
    if (!SetPixelFormat(dc, pixel_format_arb, &pfd))
        throwError("Failed to set pixel format (WGL)");

    // Create real context
    GLint context_attributes[] = {
        WGL_CONTEXT_MAJOR_VERSION_ARB, gl_major_version,
        WGL_CONTEXT_MINOR_VERSION_ARB, gl_minor_version,
        WGL_CONTEXT_PROFILE_MASK_ARB, WGL_CONTEXT_CORE_PROFILE_BIT_ARB,
        0
    };
    HGLRC share_rc = rc_list.count((HWND)shareWith) ? rc_list[(HWND)shareWith] : 0;
    HGLRC rc;
    if (!(rc = wglCreateContextAttribsARB(dc, share_rc, context_attributes)))
        throwError("Failed to create context (WGL)");

    wglSwapIntervalEXT(0);
    
    // Save pointers
    rc_list[hwnd] = rc;
    dc_list[hwnd] = dc;

    wglMakeCurrent(nullptr, nullptr);
    return (jlong)hwnd;
}

void nMakeCurrent(jlong hwnd) {
    if(hwnd != 0)
        wglMakeCurrent(dc_list[(HWND)hwnd], rc_list[(HWND)hwnd]);
    else wglMakeCurrent(0, 0);
}

void nSwapBuffers(jlong hwnd) {
    SwapBuffers(dc_list[(HWND)hwnd]);
}

#endif
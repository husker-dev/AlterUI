#include "d3d11.h"

static std::map<HWND, WindowD3D> d3dWindows;
static int devices_count = 0;



LRESULT CALLBACK WndProc(HWND hwnd, UINT uMsg, WPARAM wParam, LPARAM lParam) {
    switch (uMsg) {
    case WM_DESTROY:
    {
        d3dWindows[hwnd].swapchain->Release();
        d3dWindows[hwnd].device->Release();
        d3dWindows[hwnd].context->Release();

        d3dWindows.erase(d3dWindows.find(hwnd));
        return 0;
    }
    }
    return DefWindowProc(hwnd, uMsg, wParam, lParam);
}

jlong nCreateWindow() {

    WNDCLASS wc = {};
    wc.style = CS_HREDRAW | CS_VREDRAW | CS_OWNDC;
    wc.lpfnWndProc = WndProc;
    wc.cbClsExtra = 0;
    wc.cbWndExtra = 0;
    wc.hInstance = GetModuleHandle(NULL);
    wc.hIcon = NULL;
    wc.hCursor = LoadCursor(NULL, IDC_ARROW);
    wc.hbrBackground = (HBRUSH)CreateSolidBrush(0x00000000);
    wc.lpszMenuName = NULL;
    wc.lpszClassName = L"minui_d3d11";
    RegisterClass(&wc);

    HWND hwnd = CreateWindowEx(
        WS_EX_COMPOSITED,
        L"minui_d3d11", L"",
        WS_CLIPSIBLINGS | WS_CLIPCHILDREN | WS_SYSMENU | WS_MINIMIZEBOX | WS_CAPTION | WS_MAXIMIZEBOX | WS_THICKFRAME,
        0, 0,
        100, 100,
        NULL, NULL,
        GetModuleHandle(NULL),
        NULL);

    // Remove window background
    DWM_BLURBEHIND bb = { 0 };
    HRGN hRgn = CreateRectRgn(0, 0, -1, -1);
    bb.dwFlags = DWM_BB_ENABLE | DWM_BB_BLURREGION;
    bb.hRgnBlur = hRgn;
    bb.fEnable = TRUE;
    DwmEnableBlurBehindWindow(hwnd, &bb);

    // Init DX11

    DXGI_SWAP_CHAIN_DESC scd = {};
    scd.BufferCount = 1;                                    // one back buffer
    scd.BufferDesc.Format = DXGI_FORMAT_R8G8B8A8_UNORM;     // use 32-bit color
    scd.BufferUsage = DXGI_USAGE_RENDER_TARGET_OUTPUT;      // how swap chain is to be used
    scd.OutputWindow = hwnd;                                // the window to be used
    scd.SampleDesc.Count = 1;                               // how many multisamples
    scd.Windowed = TRUE;

    d3dWindows[hwnd] = {};
    devices_count++;

    D3D11CreateDeviceAndSwapChain(NULL,
        D3D_DRIVER_TYPE_HARDWARE,
        NULL,
        NULL,
        NULL,
        NULL,
        D3D11_SDK_VERSION,
        &scd,
        &d3dWindows[hwnd].swapchain,
        &d3dWindows[hwnd].device,
        NULL,
        &d3dWindows[hwnd].context);

    D3D11_VIEWPORT viewport = {};
    viewport.TopLeftX = 0;
    viewport.TopLeftY = 0;
    viewport.Width = 800;
    viewport.Height = 600;
    d3dWindows[hwnd].context->RSSetViewports(1, &viewport);

    return (jlong)hwnd;
}

jlong nGetDevice(jlong hwnd) {
    return (jlong)d3dWindows[(HWND)hwnd].device;
}

jlong nGetContext(jlong hwnd) {
    return (jlong)d3dWindows[(HWND)hwnd].context;
}

jlong nGetSwapchain(jlong hwnd) {
    return (jlong)d3dWindows[(HWND)hwnd].swapchain;
}

void nPresent(jlong swapchainP) {
    IDXGISwapChain* swapchain = (IDXGISwapChain*)swapchainP;
    swapchain->Present(0, 0);
}

void nClear(jlong deviceP, jfloat red, jfloat green, jfloat blue, jfloat alpha) {
    ID3D11Device* device = (ID3D11Device*)deviceP;
    //device->ClearRenderTargetView(backbuffer, D3DXCOLOR(0.0f, 0.2f, 0.4f, 1.0f));
}





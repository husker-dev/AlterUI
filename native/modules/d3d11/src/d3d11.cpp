#include "d3d11.h"

static int devices_count = 0;

static ID3D11Device* device;
static ID3D11DeviceContext* context;
static IDXGIFactory4* dxgiFactory;

static std::map<HWND, IDXGISwapChain1*> swapChains;
static std::map<HWND, ID3D11RenderTargetView*> targets;

LRESULT CALLBACK WndProc(HWND hwnd, UINT uMsg, WPARAM wParam, LPARAM lParam) {
    switch (uMsg) {
    case WM_DESTROY:
    {
        swapChains[hwnd]->Release();
        targets[hwnd]->Release();

        swapChains.erase(swapChains.find(hwnd));
        targets.erase(targets.find(hwnd));
        return 0;
    }
    }
    return DefWindowProc(hwnd, uMsg, wParam, lParam);
}

void nCreateContext() {
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
    wc.lpszClassName = L"alterui_d3d11";
    RegisterClass(&wc);

    D3D11CreateDevice(
        NULL,
        D3D_DRIVER_TYPE_HARDWARE,
        NULL,
        NULL,
        NULL,
        NULL,
        D3D11_SDK_VERSION,
        &device,
        NULL,
        &context);
    devices_count++;

    CreateDXGIFactory2(0, __uuidof(IDXGIFactory4), (void**)&dxgiFactory);
}

jlong nCreateWindow() {
    HWND hwnd = CreateWindowEx(
        WS_EX_COMPOSITED,
        L"alterui_d3d11", L"",
        WS_CLIPSIBLINGS | WS_CLIPCHILDREN | WS_SYSMENU | WS_MINIMIZEBOX | WS_CAPTION | WS_MAXIMIZEBOX | WS_THICKFRAME,
        0, 0,
        100, 100,
        NULL, NULL,
        GetModuleHandle(NULL),
        NULL);

    DXGI_SWAP_CHAIN_DESC1 pp = {};
    pp.Width                = 1;
    pp.Height               = 1;
    pp.Format               = DXGI_FORMAT_R8G8B8A8_UNORM;
    pp.Stereo               = FALSE;
    pp.SampleDesc.Count     = 1;
    pp.BufferUsage          = DXGI_USAGE_RENDER_TARGET_OUTPUT;
    pp.BufferCount          = 1;
    pp.Scaling              = DXGI_SCALING_STRETCH;
    pp.SwapEffect           = DXGI_SWAP_EFFECT_DISCARD;
    pp.AlphaMode            = DXGI_ALPHA_MODE_UNSPECIFIED;
    pp.Flags                = DXGI_SWAP_CHAIN_FLAG_ALLOW_MODE_SWITCH;
    
    IDXGISwapChain1* swapChain;
    dxgiFactory->CreateSwapChainForHwnd(device, hwnd, &pp, NULL, NULL, &swapChain);
    
    ID3D11Texture2D* pBackBuffer;
    ID3D11RenderTargetView* target;
    swapChain->GetBuffer(0, __uuidof(ID3D11Texture2D), (LPVOID*)&pBackBuffer);
    device->CreateRenderTargetView(pBackBuffer, NULL, &target);
    pBackBuffer->Release();

    swapChains[hwnd] = swapChain;
    targets[hwnd] = target;

    return (jlong)hwnd;
}

void nPresent(jlong hwnd) {
    swapChains[(HWND)hwnd]->Present(NULL, NULL);
}

void nClear(jlong hwnd) {
    FLOAT colors[4]{ 1.0f, 1.0f, 1.0f, 1.0f };
    context->ClearRenderTargetView(targets[(HWND)hwnd], colors);
}





#include "d3d9.h"

static IDirect3D9 *d3d = nullptr;
static std::map<HWND, IDirect3DDevice9*> devices;
static std::map<IDirect3DDevice9*, IDirect3DSurface9*> surfaces;
static std::map<IDirect3DDevice9*, IDirect3DSwapChain9*> swapchains;

static std::map<jlong, ID3DXConstantTable*> constantTables;
static std::map<IDirect3DDevice9*, D3DPRESENT_PARAMETERS> parameters;

static std::map<int, LPDIRECT3DVERTEXBUFFER9> cachedBuffers;
static int devices_count = 0;


LRESULT CALLBACK WndProc(HWND hwnd, UINT uMsg, WPARAM wParam, LPARAM lParam) {
    switch (uMsg) {
    case WM_DESTROY:
    {
        devices[hwnd]->Release();
        devices.erase(devices.find(hwnd));

        if (--devices_count == 0)
            d3d->Release();
        return 0;
    }
    }
    return DefWindowProc(hwnd, uMsg, wParam, lParam);
}

void createCachedBuffer(IDirect3DDevice9* device, int vertices) {
    device->CreateVertexBuffer(12 * vertices, 0, D3DFVF_XYZ, D3DPOOL_MANAGED, &cachedBuffers[vertices], NULL);
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
    wc.lpszClassName = L"minui_d3d9";
    RegisterClass(&wc);

    HWND hwnd = CreateWindowEx(
        WS_EX_COMPOSITED,
        L"minui_d3d9", L"",
        WS_CLIPSIBLINGS | WS_CLIPCHILDREN | WS_SYSMENU | WS_MINIMIZEBOX | WS_CAPTION | WS_MAXIMIZEBOX | WS_THICKFRAME,
        0, 0, 
        100, 100, 
        NULL, NULL,
        GetModuleHandle(NULL),
        NULL);

    /*
    // Remove window background
    DWM_BLURBEHIND bb = { 0 };
    HRGN hRgn = CreateRectRgn(0, 0, -1, -1);
    bb.dwFlags = DWM_BB_ENABLE | DWM_BB_BLURREGION;
    bb.hRgnBlur = hRgn;
    bb.fEnable = TRUE;
    DwmEnableBlurBehindWindow(hwnd, &bb);
    */
    
    if(d3d == nullptr)
        d3d = Direct3DCreate9(D3D_SDK_VERSION);

    D3DPRESENT_PARAMETERS pp = {};
    pp.Windowed = TRUE;
    pp.SwapEffect = D3DSWAPEFFECT_DISCARD;
    pp.hDeviceWindow = hwnd;
    pp.PresentationInterval = D3DPRESENT_INTERVAL_IMMEDIATE;
    pp.BackBufferFormat = D3DFMT_A8R8G8B8;
    pp.BackBufferWidth = 0;
    pp.BackBufferHeight = 0;
    pp.BackBufferCount = 1;

    IDirect3DDevice9* device;
    d3d->CreateDevice(D3DADAPTER_DEFAULT,
        D3DDEVTYPE_HAL,
        hwnd,
        D3DCREATE_HARDWARE_VERTEXPROCESSING | D3DCREATE_MULTITHREADED | D3DCREATE_FPU_PRESERVE,
        &pp,
        &device);

    devices[hwnd] = device;
    parameters[device] = pp;
    devices_count++;

    device->SetRenderState(D3DRS_CULLMODE, D3DCULL_NONE);
    device->SetFVF(D3DFVF_XYZ);

    // Create cached vertices buffer to decrease create/release in future
    createCachedBuffer(device, 6);

    return (jlong)hwnd;
}

jlong nGetDevice(jlong hwnd) {
    return (jlong)devices[(HWND)hwnd];
}

void nSetViewport(jlong _device, jint width, jint height) {
    IDirect3DDevice9* device = (IDirect3DDevice9*)_device;

    if (surfaces[device] != 0)
        surfaces[device]->Release();
    if (swapchains[device] != 0)
        swapchains[device]->Release();

    D3DPRESENT_PARAMETERS pp = parameters[device];
    pp.BackBufferWidth = width > 0 ? 0 : 1;
    pp.BackBufferHeight = height > 0 ? 0 : 1;

    IDirect3DSwapChain9* swapChain;
    device->CreateAdditionalSwapChain(&pp, &swapChain);
    swapChain->GetBackBuffer(0, D3DBACKBUFFER_TYPE_MONO, &surfaces[device]);

    swapchains[device] = swapChain;
}

void nBeginScene(jlong _device) {
    IDirect3DDevice9* device = (IDirect3DDevice9*)_device;
    device->SetRenderTarget(0, surfaces[device]);
    device->BeginScene();
}

void nEndScene(jlong _device) {
    IDirect3DDevice9* device = (IDirect3DDevice9*)_device;
    device->EndScene();
    swapchains[device]->Present(NULL, NULL, NULL, NULL, NULL);
}

void nClear(jlong _device) {
    IDirect3DDevice9* device = (IDirect3DDevice9Ex*)_device;
    device->Clear(0, NULL, D3DCLEAR_TARGET, 0x00000000, 1.0f, 0);
}

void nDrawArrays(jlong _device, jfloat* array, jint count) {
    IDirect3DDevice9* device = (IDirect3DDevice9*)_device;

    LPDIRECT3DVERTEXBUFFER9 v_buffer;
    if (count == 6)
        v_buffer = cachedBuffers[count];
    else device->CreateVertexBuffer(12 * count, 0, D3DFVF_XYZ, D3DPOOL_MANAGED, &v_buffer, NULL);
    
    VOID* pVoid;
    v_buffer->Lock(0, 0, (void**)&pVoid, 0);
    memcpy(pVoid, array, 12 * count);
    v_buffer->Unlock();

    // Draw
    device->SetStreamSource(0, v_buffer, 0, 12);
    device->DrawPrimitive(D3DPT_TRIANGLELIST, 0, count / 3);

    if (count != 6)
        v_buffer->Release();
}

jlong nCreatePixelShader(jlong _device, char* content, jint length) {
    IDirect3DDevice9* device = (IDirect3DDevice9*)_device;

    ID3DXBuffer* buffer;
    ID3DXBuffer* error;
    ID3DXConstantTable* table;
   
    if (D3DXCompileShader(content, length, 0, 0, "main", "ps_3_0", 0, &buffer, &error, &table) != S_OK)
        std::cout << (char*)error->GetBufferPointer() << std::endl;

    IDirect3DPixelShader9* shader;
    device->CreatePixelShader((const DWORD*)buffer->GetBufferPointer(), &shader);

    constantTables[(jlong)shader] = table;
    return (jlong)shader;
}

jlong nCreateVertexShader(jlong _device, char* content, jint length) {
    IDirect3DDevice9* device = (IDirect3DDevice9*)_device;

    ID3DXBuffer* buffer;
    ID3DXBuffer* error;
    ID3DXConstantTable* table;
    if (D3DXCompileShader(content, length, 0, 0, "main", "vs_3_0", 0, &buffer, &error, &table) != S_OK)
        std::cout << (char*)error->GetBufferPointer() << std::endl;

    IDirect3DVertexShader9* shader;
    device->CreateVertexShader((const DWORD*)buffer->GetBufferPointer(), &shader);

    constantTables[(jlong)shader] = table;
    return (jlong)shader;
}

void nSetPixelShader(jlong _device, jlong _shader) {
    IDirect3DDevice9* device = (IDirect3DDevice9*)_device;
    IDirect3DPixelShader9* shader = (IDirect3DPixelShader9*)_shader;
    device->SetPixelShader(shader);
}

void nSetVertexShader(jlong _device, jlong _shader) {
    IDirect3DDevice9* device = (IDirect3DDevice9*)_device;
    IDirect3DVertexShader9* shader = (IDirect3DVertexShader9*)_shader;
    device->SetVertexShader(shader);
}

void nSetShaderValue3f(jlong _device, jlong _shader, char* name, jfloat v1, jfloat v2, jfloat v3) {
    IDirect3DDevice9* device = (IDirect3DDevice9*)_device;
    ID3DXConstantTable* table = constantTables[_shader];

    const float a[3]{ v1, v2, v3 };
    table->SetFloatArray(device, table->GetConstantByName(0, name), a, 3);
}

void nSetShaderValue4f(jlong _device, jlong _shader, char* name, jfloat v1, jfloat v2, jfloat v3, jfloat v4) {
    IDirect3DDevice9* device = (IDirect3DDevice9*)_device;
    ID3DXConstantTable* table = constantTables[_shader];

    const float a[4]{ v1, v2, v3, v4 };
    table->SetFloatArray(device, table->GetConstantByName(0, name), a, 4);
}

void nSetShaderMatrix(jlong _device, jlong _shader, char* name, jfloat* matrix) {
    IDirect3DDevice9* device = (IDirect3DDevice9*)_device;
    ID3DXConstantTable* table = constantTables[_shader];

    D3DXMATRIX m = D3DXMATRIX(matrix);
    table->SetMatrix(device, table->GetConstantByName(0, name), &m);
}




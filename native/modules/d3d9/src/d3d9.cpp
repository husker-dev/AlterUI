#include "d3d9.h"

static IDirect3D9 *d3d = nullptr;
static IDirect3DDevice9* device;
static D3DPRESENT_PARAMETERS pp;
static std::map<HWND, IDirect3DSurface9*> surfaces;
static std::map<HWND, IDirect3DSwapChain9*> swapChains;
static std::map<int, LPDIRECT3DVERTEXBUFFER9> cachedBuffers;
static int devices_count = 0;

// Shader's constants
static std::map<jlong, ID3DXConstantTable*> constantTables;


LRESULT CALLBACK WndProc(HWND hwnd, UINT uMsg, WPARAM wParam, LPARAM lParam) {
    switch (uMsg) {
    case WM_DESTROY:
    {
        surfaces[hwnd]->Release();
        surfaces.erase(surfaces.find(hwnd));

        swapChains[hwnd]->Release();
        swapChains.erase(swapChains.find(hwnd));

        if (--devices_count == 0)
            d3d->Release();
        return 0;
    }
    }
    return DefWindowProc(hwnd, uMsg, wParam, lParam);
}

void cacheBufferSize(int vertices) {
    device->CreateVertexBuffer(12 * vertices, 0, D3DFVF_XYZ, D3DPOOL_MANAGED, &cachedBuffers[vertices], NULL);
}

jlong nCreateMainWindow() {
    WNDCLASS wc = {};
    wc.style = CS_HREDRAW | CS_VREDRAW | CS_OWNDC;
    wc.lpfnWndProc = WndProc;
    wc.cbClsExtra = 0;
    wc.cbWndExtra = 0;
    wc.hInstance = GetModuleHandle(NULL);
    wc.hIcon = NULL;
    wc.hCursor = LoadCursor(NULL, IDC_ARROW);
    wc.lpszMenuName = NULL;
    wc.lpszClassName = L"alterui_d3d9";
    RegisterClass(&wc);

    HWND hwnd = CreateWindowEx(
        WS_EX_COMPOSITED,
        L"alterui_d3d9", L"",
        WS_POPUPWINDOW,
        0, 0,
        10, 10,
        NULL, NULL,
        GetModuleHandle(NULL),
        NULL);

    // Create D3D9
    pp = {};
    pp.Windowed = TRUE;
    pp.SwapEffect = D3DSWAPEFFECT_DISCARD;
    pp.hDeviceWindow = hwnd;
    pp.PresentationInterval = D3DPRESENT_INTERVAL_IMMEDIATE;
    pp.BackBufferFormat = D3DFMT_A8R8G8B8;
    pp.BackBufferWidth = 0;
    pp.BackBufferHeight = 0;
    pp.BackBufferCount = 1;

    d3d = Direct3DCreate9(D3D_SDK_VERSION);
    d3d->CreateDevice(
        D3DADAPTER_DEFAULT, D3DDEVTYPE_HAL,
        hwnd,
        D3DCREATE_HARDWARE_VERTEXPROCESSING | D3DCREATE_MULTITHREADED | D3DCREATE_FPU_PRESERVE,
        &pp, &device);
    cacheBufferSize(6);

    // Set parameters
    device->SetFVF(D3DFVF_XYZ);
    device->SetRenderState(D3DRS_CULLMODE, D3DCULL_NONE);
    device->SetRenderState(D3DRS_ALPHABLENDENABLE, TRUE);
    device->SetRenderState(D3DRS_BLENDOP, D3DBLENDOP_ADD);
    device->SetRenderState(D3DRS_SRCBLEND, D3DBLEND_SRCALPHA);
    device->SetRenderState(D3DRS_DESTBLEND, D3DBLEND_INVSRCALPHA);

    device->SetSamplerState(0, D3DSAMP_ADDRESSU, D3DTADDRESS_CLAMP);
    device->SetSamplerState(0, D3DSAMP_ADDRESSW, D3DTADDRESS_CLAMP);
    device->SetSamplerState(0, D3DSAMP_ADDRESSV, D3DTADDRESS_CLAMP);

    return (jlong)hwnd;
}

jlong nCreateWindow() {
    HWND hwnd = CreateWindowEx(
        WS_EX_COMPOSITED,
        L"alterui_d3d9", L"",
        WS_CLIPSIBLINGS | WS_CLIPCHILDREN | WS_SYSMENU | WS_MINIMIZEBOX | WS_CAPTION | WS_MAXIMIZEBOX | WS_THICKFRAME,
        0, 0,
        10, 10,
        NULL, NULL,
        GetModuleHandle(NULL),
        NULL);

    pp.hDeviceWindow = hwnd;
    pp.BackBufferWidth = 1;
    pp.BackBufferHeight = 1;
    
    IDirect3DSwapChain9* swapChain;
    IDirect3DSurface9* surface;
    device->CreateAdditionalSwapChain(&pp, &swapChain);
    swapChain->GetBackBuffer(0, D3DBACKBUFFER_TYPE_MONO, &surface);

    swapChains[hwnd] = swapChain;
    surfaces[hwnd] = surface;

    return (jlong)hwnd;
}

jlong nGetDevice() {
    return (jlong)device;
}

void nSetViewport(jlong _hwnd, jint width, jint height) {
    HWND hwnd = (HWND)_hwnd;

    if (surfaces[hwnd] != 0)
        surfaces[hwnd]->Release();
    if (swapChains[hwnd] != 0)
        swapChains[hwnd]->Release();

    pp.hDeviceWindow = hwnd;
    pp.BackBufferWidth = width > 0 ? width : 1;
    pp.BackBufferHeight = height > 0 ? height : 1;

    IDirect3DSwapChain9* swapChain;
    IDirect3DSurface9* surface;
    device->CreateAdditionalSwapChain(&pp, &swapChain);
    swapChain->GetBackBuffer(0, D3DBACKBUFFER_TYPE_MONO, &surface);

    swapChains[hwnd] = swapChain;
    surfaces[hwnd] = surface;
}

void nBeginScene(jlong _hwnd) {
    HWND hwnd = (HWND)_hwnd;
    device->SetRenderTarget(0, surfaces[hwnd]);
    device->BeginScene();
}

void nEndScene(jlong _hwnd) {
    HWND hwnd = (HWND)_hwnd;
    device->EndScene();
    swapChains[hwnd]->Present(NULL, NULL, NULL, NULL, NULL);
}

void nClear() {
    device->Clear(0, NULL, D3DCLEAR_TARGET | D3DCLEAR_ZBUFFER, NULL, 1.0f, 0);
}


void nSetTexture(jlong _texture) {
    IDirect3DTexture9* texture = (IDirect3DTexture9*)_texture;

    if (device->SetTexture(0, texture) != S_OK)
        throwError("Can't set texture");
}

void nDrawArrays(jfloat* array, jint count, jint type) {
    LPDIRECT3DVERTEXBUFFER9 v_buffer;
    
    // Create buffer
    if (count == 6)
        v_buffer = cachedBuffers[count];
    else if(device->CreateVertexBuffer(12 * count, 0, D3DFVF_XYZ, D3DPOOL_MANAGED, &v_buffer, NULL) != S_OK)
        throwError("Can't create vertex buffer");
    
    // Write data
    VOID* pVoid;
    if(v_buffer->Lock(0, 0, (void**)&pVoid, 0) != S_OK)
        throwError("Can't lock buffer");

    memcpy(pVoid, array, 12 * count);

    if(v_buffer->Unlock() != S_OK)
        throwError("Can't unlock buffer");

    // Draw
    if(device->SetStreamSource(0, v_buffer, 0, 12) != S_OK)
        throwError("Can't set stream source");

    if(device->DrawPrimitive((_D3DPRIMITIVETYPE)type, 0, count / 3) != S_OK)
        throwError("Can't draw promitives");

    // Release
    if (count != 6)
        v_buffer->Release();
}

jlong nCreatePixelShader(char* content, jint length) {
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

jlong nCreateVertexShader(char* content, jint length) {
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

void nSetPixelShader(jlong _shader) {
    IDirect3DPixelShader9* shader = (IDirect3DPixelShader9*)_shader;
    if(device->SetPixelShader(shader) != S_OK)
        throwError("Can't set pixels shader buffer");
}

void nSetVertexShader(jlong _shader) {
    IDirect3DVertexShader9* shader = (IDirect3DVertexShader9*)_shader;
    if(device->SetVertexShader(shader) != S_OK)
        throwError("Can't set vertex shader buffer");
}

void nSetShaderValue1f(jlong _shader, char* name, jfloat v) {
    ID3DXConstantTable* table = constantTables[_shader];

    table->SetFloat(device, table->GetConstantByName(0, name), v);
}

void nSetShaderValue3f(jlong _shader, char* name, jfloat v1, jfloat v2, jfloat v3) {
    ID3DXConstantTable* table = constantTables[_shader];

    const float a[3]{ v1, v2, v3 };
    table->SetFloatArray(device, table->GetConstantByName(0, name), a, 3);
}

void nSetShaderValue4f(jlong _shader, char* name, jfloat v1, jfloat v2, jfloat v3, jfloat v4) {
    ID3DXConstantTable* table = constantTables[_shader];

    const float a[4]{ v1, v2, v3, v4 };
    table->SetFloatArray(device, table->GetConstantByName(0, name), a, 4);
}

void nSetShaderMatrix(jlong _shader, char* name, jfloat* matrix) {
    ID3DXConstantTable* table = constantTables[_shader];

    D3DXMATRIX m = D3DXMATRIX(matrix);
    table->SetMatrix(device, table->GetConstantByName(0, name), &m);
}

jlong nCreateTexture(jint width, jint height, jint components, char* data) {
    IDirect3DTexture9* texture;

    _D3DFORMAT format = D3DFMT_A8R8G8B8;
    if (components == 3)
        format = D3DFMT_X8R8G8B8;
    if (components == 1)
        format = D3DFMT_L8;

    HRESULT h;
    if ((h = device->CreateTexture(width, height, 1, 0, format, D3DPOOL_MANAGED, &texture, 0)) != S_OK)
        throwError("Can't create texture");

    D3DLOCKED_RECT lockedRect;
    texture->LockRect(0, &lockedRect, 0, D3DLOCK_DISCARD);

    char* pData = (char*)lockedRect.pBits;

    if (components == 4 || components == 3) {
        for (unsigned int i = 0, s = 0;
            i < width * height * 4;
            i += 4, s += components
        ) {
            pData[i] = data[s + 2];
            pData[i + 1] = data[s + 1];
            pData[i + 2] = data[s];
            pData[i + 3] = components == 3 ? 255 : data[s + 3];
        }
    }
    if (components == 1) {
        for (int row = 0; row < height; row++) {
            for (int i = 0; i < width; i++) {
                int posSource = row * width + i;
                int posTarget = row * lockedRect.Pitch + i;

                pData[posTarget] = data[posSource];
            }
        }
    }
    
    texture->UnlockRect(0);
    
    return (jlong)texture;
}

void nSetLinearFiltering(jboolean linearFiltering) {
    device->SetSamplerState(0, D3DSAMP_MINFILTER, linearFiltering ? D3DTEXF_LINEAR : D3DTEXF_POINT);
    device->SetSamplerState(0, D3DSAMP_MAGFILTER, linearFiltering ? D3DTEXF_LINEAR : D3DTEXF_POINT);
    device->SetSamplerState(0, D3DSAMP_MIPFILTER, D3DTEXF_NONE);
}




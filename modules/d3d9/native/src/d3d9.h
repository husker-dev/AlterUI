
#include <jni.h>
#include <windows.h>
#include <dwmapi.h>
#include <iostream>
#include <map>
#include <d3d9.h>
#include <d3dx9.h>

static IDirect3D9* d3d = nullptr;
static IDirect3DDevice9* device;
static D3DPRESENT_PARAMETERS pp;
static bool wndClassInitialized = false;

static std::map<HWND, IDirect3DSurface9*> surfaces;
static std::map<HWND, IDirect3DSwapChain9*> swapChains;
static std::map<int, LPDIRECT3DVERTEXBUFFER9> cachedBuffers;
static int devices_count = 0;
static int maxMSAA = -1;

// Shader's constants
static std::map<jlong, ID3DXConstantTable*> constantTables;

void throwError(const char* text) {
	std::cout << "[ERROR] Internal Direct3D9 error: " << text << std::endl;
	MessageBoxA(NULL, text, "Internal Direct3D9 error", MB_OK | MB_ICONERROR);
	exit(1);
}

void throwJavaException(JNIEnv* env, const char* exceptionClass, const char* message) {
	env->ThrowNew(env->FindClass(exceptionClass), message);
}

void cacheBufferSize(int vertices) {
	device->CreateVertexBuffer(12 * vertices, 0, D3DFVF_XYZ, D3DPOOL_MANAGED, &cachedBuffers[vertices], NULL);
}

D3DFORMAT GetFormat(int components) {
	if (components == 4)
		return D3DFMT_A8R8G8B8;
	if (components == 3)
		return D3DFMT_X8R8G8B8;
	return D3DFMT_L8;
}

_D3DMULTISAMPLE_TYPE GetSupportedMSAA(int level) {
	if (maxMSAA == -1) {
		for (int i = 0; i < 16; i += 2) {
			if (d3d->CheckDeviceMultiSampleType(D3DADAPTER_DEFAULT, D3DDEVTYPE_HAL, D3DFMT_A8R8G8B8, FALSE, (_D3DMULTISAMPLE_TYPE)i, NULL) == S_OK)
				maxMSAA = i;
		}
	}
	if (level % 2 == 1)
		level--;
	if (level > maxMSAA)
		level = maxMSAA;
	if (level < 0)
		level = 0;
	
	return (_D3DMULTISAMPLE_TYPE)level;
}

LRESULT CALLBACK WndProc(HWND hwnd, UINT uMsg, WPARAM wParam, LPARAM lParam) {
	switch (uMsg) {
	case WM_NCCREATE:
	{
		EnableNonClientDpiScaling(hwnd);
		break;
	}
	case WM_DESTROY:
	{
		surfaces[hwnd]->Release();
		surfaces.erase(surfaces.find(hwnd));

		swapChains[hwnd]->Release();
		swapChains.erase(swapChains.find(hwnd));

		if (--devices_count == 0)
			d3d->Release();
		
		break;
	}
	case WM_SIZE:
	{
		int width = LOWORD(lParam);
		int height = HIWORD(lParam);

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
		break;
	}
	}
	
	return DefWindowProc(hwnd, uMsg, wParam, lParam);
}

extern "C" {

	/* ===============
		Platform
	   ===============
	*/
	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nInitializeDevice(JNIEnv*, jobject, jboolean vsync, jint samples) {
		SetThreadDpiAwarenessContext(DPI_AWARENESS_CONTEXT_PER_MONITOR_AWARE);

		d3d = Direct3DCreate9(D3D_SDK_VERSION);
		/*
		*	Create device without window.
		*	If BackBuffer size is 0, then DX tries to get current window size, and fails
		*/
		pp = {};
		pp.Windowed = TRUE;
		pp.SwapEffect = D3DSWAPEFFECT_DISCARD;
		pp.hDeviceWindow = NULL;
		pp.PresentationInterval = D3DPRESENT_INTERVAL_IMMEDIATE;
		pp.BackBufferFormat = D3DFMT_A8R8G8B8;
		pp.MultiSampleType = GetSupportedMSAA(samples);
		pp.MultiSampleQuality = 0;
		pp.BackBufferCount = 1;
		pp.BackBufferWidth = 100;
		pp.BackBufferHeight = 100;
		if (vsync) {
			pp.PresentationInterval = D3DPRESENT_INTERVAL_ONE;
			pp.FullScreen_RefreshRateInHz = D3DPRESENT_RATE_DEFAULT;
		}
		
		d3d->CreateDevice(
			D3DADAPTER_DEFAULT, D3DDEVTYPE_HAL,
			NULL,
			D3DCREATE_HARDWARE_VERTEXPROCESSING | D3DCREATE_MULTITHREADED | D3DCREATE_PUREDEVICE,
			&pp, &device);
		
		cacheBufferSize(6);

		// Set properties
		device->SetFVF(D3DFVF_XYZ);
		device->SetRenderState(D3DRS_CULLMODE, D3DCULL_NONE);
		device->SetRenderState(D3DRS_ALPHABLENDENABLE, TRUE);
		device->SetRenderState(D3DRS_BLENDOP, D3DBLENDOP_ADD);
		device->SetRenderState(D3DRS_SRCBLEND, D3DBLEND_SRCALPHA);
		device->SetRenderState(D3DRS_DESTBLEND, D3DBLEND_INVSRCALPHA);
		device->SetRenderState(D3DRS_MULTISAMPLEANTIALIAS, TRUE);

		device->SetSamplerState(0, D3DSAMP_ADDRESSU, D3DTADDRESS_CLAMP);
		device->SetSamplerState(0, D3DSAMP_ADDRESSW, D3DTADDRESS_CLAMP);
		device->SetSamplerState(0, D3DSAMP_ADDRESSV, D3DTADDRESS_CLAMP);
	}

	JNIEXPORT jlong JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nCreateWindow(JNIEnv*, jobject) {
		if (!wndClassInitialized) {
			wndClassInitialized = true;

			WNDCLASS wc = {};
			wc.style = CS_HREDRAW | CS_VREDRAW | CS_OWNDC;
			wc.lpfnWndProc = WndProc;
			wc.cbClsExtra = 0;
			wc.cbWndExtra = 0;
			wc.hInstance = GetModuleHandle(NULL);
			wc.hCursor = LoadCursor(NULL, IDC_ARROW);
			wc.lpszMenuName = NULL;
			wc.lpszClassName = L"alterui_d3d9";

			// Icon
			SHSTOCKICONINFO sii;
			sii.cbSize = sizeof(sii);
			SHGetStockIconInfo(SIID_APPLICATION, SHGSI_ICON | SHGSI_LARGEICON, &sii);
			wc.hIcon = sii.hIcon;

			RegisterClass(&wc);
		}
		return (jlong)CreateWindow(
			L"alterui_d3d9", L"",
			WS_OVERLAPPEDWINDOW,
			0, 0,
			10, 10,
			NULL, NULL,
			GetModuleHandle(NULL),
			NULL);
	}

	JNIEXPORT jlong JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nGetWindowSurface(JNIEnv*, jobject, jlong hwnd) {
		return (jlong)surfaces[(HWND)hwnd];
	}

	JNIEXPORT jlong JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nGetTextureSurface(JNIEnv*, jobject, jlong _texture) {
		IDirect3DTexture9* texture = (IDirect3DTexture9*)_texture;
		IDirect3DSurface9* surface;
		texture->GetSurfaceLevel(0, &surface);
		return (jlong)surface;
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nReleaseTexture(JNIEnv*, jobject, jlong _texture) {
		IDirect3DTexture9* texture = (IDirect3DTexture9*)_texture;
		texture->Release();
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nReleaseSurface(JNIEnv*, jobject, jlong _surface) {
		IDirect3DSurface9* surface = (IDirect3DSurface9*)_surface;
		surface->Release();
	}

	/* ===============
		Default
	   ===============
	*/
	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nSetRenderTarget(JNIEnv*, jobject, jlong surface) {
		device->SetRenderTarget(0, (IDirect3DSurface9*)surface);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nBeginScene(JNIEnv*, jobject) {
		device->BeginScene();
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nEndScene(JNIEnv*, jobject) {
		device->EndScene();
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nPresent(JNIEnv*, jobject, jlong hwnd) {
		swapChains[(HWND)hwnd]->Present(NULL, NULL, NULL, NULL, NULL);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nClear(JNIEnv*, jobject) {
		device->Clear(0, NULL, D3DCLEAR_TARGET, 0, 1.0f, 0);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nSetTexture(JNIEnv*, jobject, jint index, jlong texture) {
		device->SetTexture(index, (IDirect3DTexture9*)texture);
	}

	/* ===============
		Custom
	   ===============
	*/

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nDrawArrays(JNIEnv* env, jobject, jobject _array, jint count, jint type) {
		jfloat* array = (jfloat*)env->GetDirectBufferAddress(_array);
		
		LPDIRECT3DVERTEXBUFFER9 v_buffer;

		// Create buffer
		if (count == 6)
			v_buffer = cachedBuffers[count];
		else if (device->CreateVertexBuffer(12 * count, 0, D3DFVF_XYZ, D3DPOOL_MANAGED, &v_buffer, NULL) != S_OK)
			throwError("Can't create vertex buffer");

		// Write data
		VOID* pVoid;
		if (v_buffer->Lock(0, 0, (void**)&pVoid, 0) != S_OK)
			throwError("Can't lock buffer");

		memcpy(pVoid, array, 12 * count);

		if (v_buffer->Unlock() != S_OK)
			throwError("Can't unlock buffer");

		// Draw
		if (device->SetStreamSource(0, v_buffer, 0, 12) != S_OK)
			throwError("Can't set stream source");

		if (device->DrawPrimitive((D3DPRIMITIVETYPE)type, 0, count / 3) != S_OK)
			throwError("Can't draw promitives");

		// Release
		if (count != 6)
			v_buffer->Release();
	}

	JNIEXPORT jlong JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nCreatePixelShader(JNIEnv* env, jobject, jobject _content, jint length) {
		char* content = (char*)env->GetDirectBufferAddress(_content);
		
		ID3DXBuffer* buffer;
		ID3DXBuffer* error;
		ID3DXConstantTable* table;

		if (D3DXCompileShader(content, length, 0, 0, "main", "ps_3_0", 0, &buffer, &error, &table) != S_OK) {
			throwJavaException(env, "java/lang/RuntimeException", (char*)error->GetBufferPointer());
			return 0;
		}

		IDirect3DPixelShader9* shader;
		device->CreatePixelShader((const DWORD*)buffer->GetBufferPointer(), &shader);

		constantTables[(jlong)shader] = table;
		return (jlong)shader;
	}

	JNIEXPORT jlong JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nCreateVertexShader(JNIEnv* env, jobject, jobject _content, jint length) {
		char* content = (char*)env->GetDirectBufferAddress(_content);

		ID3DXBuffer* buffer;
		ID3DXBuffer* error;
		ID3DXConstantTable* table;
		if (D3DXCompileShader(content, length, 0, 0, "main", "vs_3_0", 0, &buffer, &error, &table) != S_OK) {
			throwJavaException(env, "java/lang/RuntimeException", (char*)error->GetBufferPointer());
			return 0;
		}

		IDirect3DVertexShader9* shader;
		device->CreateVertexShader((const DWORD*)buffer->GetBufferPointer(), &shader);

		constantTables[(jlong)shader] = table;
		return (jlong)shader;
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nSetPixelShader(JNIEnv* env, jobject, jlong _shader) {
		IDirect3DPixelShader9* shader = (IDirect3DPixelShader9*)_shader;
		device->SetPixelShader(shader);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nSetVertexShader(JNIEnv* env, jobject, jlong _shader) {
		IDirect3DVertexShader9* shader = (IDirect3DVertexShader9*)_shader;
		device->SetVertexShader(shader);
	}

	JNIEXPORT jlong JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nGetShaderVariableHandle(JNIEnv* env, jobject, jlong _shader, jobject _name) {
		char* name = (char*)env->GetDirectBufferAddress(_name);
		ID3DXConstantTable* table = constantTables[_shader];

		return (jlong)table->GetConstantByName(0, name);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nSetShaderValue1f(JNIEnv* env, jobject, jlong _shader, jlong varHandle, jfloat v) {
		ID3DXConstantTable* table = constantTables[_shader];
		table->SetFloat(device, (D3DXHANDLE)varHandle, v);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nSetShaderValue3f(JNIEnv* env, jobject, jlong _shader, jlong varHandle, jfloat v1, jfloat v2, jfloat v3) {
		ID3DXConstantTable* table = constantTables[_shader];

		const float a[3]{ v1, v2, v3 };
		table->SetFloatArray(device, (D3DXHANDLE)varHandle, a, 3);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nSetShaderValue4f(JNIEnv* env, jobject, jlong _shader, jlong varHandle, jfloat v1, jfloat v2, jfloat v3, jfloat v4) {
		ID3DXConstantTable* table = constantTables[_shader];

		const float a[4]{ v1, v2, v3, v4 };
		table->SetFloatArray(device, (D3DXHANDLE)varHandle, a, 4);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nSetShaderMatrix(JNIEnv* env, jobject, jlong _shader, jlong varHandle, jobject _matrix) {
		jfloat* matrix = (jfloat*)env->GetDirectBufferAddress(_matrix);
		ID3DXConstantTable* table = constantTables[_shader];

		D3DXMATRIX m = D3DXMATRIX(matrix);
		table->SetMatrix(device, (D3DXHANDLE)varHandle, &m);
	}

	JNIEXPORT jlong JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nCreateEmptySurface(JNIEnv* env, jobject, jint width, jint height, jint components, jint samples) {
		IDirect3DSurface9* surface;

		HRESULT h;
		if ((h = device->CreateRenderTarget(width, height, GetFormat(components), (_D3DMULTISAMPLE_TYPE)samples, 0, false, &surface, 0)) != S_OK)
			throwJavaException(env, "java/lang/RuntimeException", "Can't create render surface");

		return (jlong)surface;
	}

	JNIEXPORT jlong JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nCreateSurface(JNIEnv* env, jobject, jint width, jint height, jint components, jint samples, jobject _data) {
		char* data = (char*)env->GetDirectBufferAddress(_data);
		IDirect3DSurface9* targetSurface;
		IDirect3DSurface9* sourceSurface;

		HRESULT h;
		if ((h = device->CreateRenderTarget(width, height, GetFormat(components), GetSupportedMSAA(samples), 0, false, &targetSurface, NULL)) != S_OK)
			throwJavaException(env, "java/lang/RuntimeException", "Can't create render surface");
		if ((h = device->CreateOffscreenPlainSurface(width, height, GetFormat(components), D3DPOOL_DEFAULT, &sourceSurface, NULL)) != S_OK)
			throwJavaException(env, "java/lang/RuntimeException", "Can't create offscreen plain surface");

		D3DLOCKED_RECT lockedRect;
		sourceSurface->LockRect(&lockedRect, NULL, 0);
		char* pData = (char*)lockedRect.pBits;
		int destComponents = components == 1 ? 1 : 4;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int posSource = (y * width + x) * components;
				int posTarget = y * lockedRect.Pitch + x * destComponents;

				if (destComponents == 4) {
					pData[posTarget] = data[posSource + 2];
					pData[posTarget + 1] = data[posSource + 1];
					pData[posTarget + 2] = data[posSource];
					pData[posTarget + 3] = components == 3 ? 255 : data[posSource + 3];
				}
				else
					pData[posTarget] = data[posSource];
			}
		}
		sourceSurface->UnlockRect();

		// Copy image content to render target
		device->StretchRect(sourceSurface, NULL, targetSurface, NULL, D3DTEXF_POINT);
		sourceSurface->Release();

		return (jlong)targetSurface;
	}

	JNIEXPORT jlong JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nCreateTexture(JNIEnv* env, jobject, jint width, jint height, jint components) {
		IDirect3DTexture9* targetTexture;

		HRESULT h;
		if ((h = device->CreateTexture(width, height, 0, D3DUSAGE_RENDERTARGET, GetFormat(components), D3DPOOL_DEFAULT, &targetTexture, 0)) != S_OK)
			throwJavaException(env, "java/lang/RuntimeException", "Can't create empty texture");

		return (jlong)targetTexture;
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nStretchRect(JNIEnv* env, jobject, jlong _surfaceSource, jlong _surfaceTarget) {
		IDirect3DSurface9* sourceSurface = (IDirect3DSurface9*)_surfaceSource;
		IDirect3DSurface9* targetSurface = (IDirect3DSurface9*)_surfaceTarget;

		device->StretchRect(sourceSurface, 0, targetSurface, 0, D3DTEXF_POINT);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nSetLinearFiltering(JNIEnv*, jobject, jboolean linearFiltering) {
		device->SetSamplerState(0, D3DSAMP_MINFILTER, linearFiltering ? D3DTEXF_LINEAR : D3DTEXF_POINT);
		device->SetSamplerState(0, D3DSAMP_MAGFILTER, linearFiltering ? D3DTEXF_LINEAR : D3DTEXF_POINT);
		device->SetSamplerState(0, D3DSAMP_MIPFILTER, D3DTEXF_NONE);
	}

}
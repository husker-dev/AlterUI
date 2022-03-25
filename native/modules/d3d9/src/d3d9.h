
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
	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nInitializeDevice(JNIEnv*, jobject) {
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
		pp.BackBufferCount = 1;
		pp.BackBufferWidth = 100;
		pp.BackBufferHeight = 100;
		
		d3d = Direct3DCreate9(D3D_SDK_VERSION);
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
		return (jlong)CreateWindowEx(
			WS_EX_COMPOSITED,
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

	JNIEXPORT jlong JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nCreateEmptyTexture(JNIEnv* env, jobject, jint width, jint height, jint components) {
		IDirect3DTexture9* targetTexture;

		D3DFORMAT format = D3DFMT_A8R8G8B8;
		if (components == 3)
			format = D3DFMT_X8R8G8B8;
		if (components == 1)
			format = D3DFMT_L8;

		HRESULT h;
		if ((h = device->CreateTexture(width, height, 0, D3DUSAGE_RENDERTARGET, format, D3DPOOL_DEFAULT, &targetTexture, 0)) != S_OK)
			throwJavaException(env, "java/lang/RuntimeException", "Can't create empty texture");

		return (jlong)targetTexture;
	}

	JNIEXPORT jlong JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nCreateTexture(JNIEnv* env, jobject, jint width, jint height, jint components, jobject _data) {
		char* data = (char*)env->GetDirectBufferAddress(_data);
		IDirect3DTexture9* sourceTexture;
		IDirect3DTexture9* targetTexture;

		D3DFORMAT format = D3DFMT_A8R8G8B8;
		if (components == 3)
			format = D3DFMT_X8R8G8B8;
		if (components == 1)
			format = D3DFMT_L8;
			
		HRESULT h;
		if ((h = device->CreateTexture(width, height, 1, 0, format, D3DPOOL_SYSTEMMEM, &sourceTexture, 0)) != S_OK)
			throwJavaException(env, "java/lang/RuntimeException", "Can't create temporary texture");
		if ((h = device->CreateTexture(width, height, 0, D3DUSAGE_RENDERTARGET, format, D3DPOOL_DEFAULT, &targetTexture, 0)) != S_OK)
			throwJavaException(env, "java/lang/RuntimeException", "Can't create texture");
		
		D3DLOCKED_RECT lockedRect;
		sourceTexture->LockRect(0, &lockedRect, 0, D3DLOCK_DISCARD);

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
		sourceTexture->UnlockRect(0);

		IDirect3DSurface9* sourceSurface;
		IDirect3DSurface9* targetSurface;
		sourceTexture->GetSurfaceLevel(0, &sourceSurface);
		targetTexture->GetSurfaceLevel(0, &targetSurface);
		device->UpdateSurface(sourceSurface, NULL, targetSurface, NULL);
		sourceSurface->Release();
		sourceTexture->Release();

		return (jlong)targetTexture;
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nSetLinearFiltering(JNIEnv*, jobject, jboolean linearFiltering) {
		device->SetSamplerState(0, D3DSAMP_MINFILTER, linearFiltering ? D3DTEXF_LINEAR : D3DTEXF_POINT);
		device->SetSamplerState(0, D3DSAMP_MAGFILTER, linearFiltering ? D3DTEXF_LINEAR : D3DTEXF_POINT);
		device->SetSamplerState(0, D3DSAMP_MIPFILTER, D3DTEXF_NONE);
	}

}
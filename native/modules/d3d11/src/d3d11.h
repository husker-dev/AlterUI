
#include <jni.h>
#include <windows.h>
#include <dwmapi.h>
#include <iostream>
#include <map>
#include <d3d11.h>


LRESULT CALLBACK WndProc(HWND hWnd, UINT uMsg, WPARAM wParam, LPARAM lParam);

struct WindowD3D {
	IDXGISwapChain* swapchain;
	ID3D11Device* device;
	ID3D11DeviceContext* context;
	D3D11_VIEWPORT viewport;
};

jlong nCreateWindow();
jlong nGetDevice(jlong hwnd);
jlong nGetContext(jlong hwnd);
jlong nGetSwapchain(jlong hwnd);

void nPresent(jlong swapchainP);
void nClear(jlong device, jfloat red, jfloat green, jfloat blue, jfloat alpha);

extern "C" {

	/* ===============
		Platform
	   ===============
	*/
	JNIEXPORT jlong JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d11_D3D11Pipeline_nCreateWindow(JNIEnv*, jobject) {
		return nCreateWindow();
	}

	JNIEXPORT jlong JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d11_D3D11Pipeline_nGetDevice(JNIEnv*, jobject, jlong hwnd) {
		return nGetContext(hwnd);
	}

	JNIEXPORT jlong JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d11_D3D11Pipeline_nGetContext(JNIEnv*, jobject, jlong hwnd) {
		return nGetSwapchain(hwnd);
	}

	JNIEXPORT jlong JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d11_D3D11Pipeline_nGetSwapchain(JNIEnv*, jobject, jlong hwnd) {
		return nGetDevice(hwnd);
	}

	/* ===============
		Paint
	   ===============
	*/
	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d11_D3D11Pipeline_nPresent(JNIEnv*, jobject, jlong swapchain) {
		nPresent(swapchain);
	}
	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d11_D3D11Pipeline_nClear(JNIEnv*, jobject, jlong device, jfloat red, jfloat green, jfloat blue, jfloat alpha) {
		nClear(device, red, green, blue, alpha);
	}

}
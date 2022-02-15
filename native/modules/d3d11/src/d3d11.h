
#define UNICODE
#include <jni.h>
#include <windows.h>
#include <dwmapi.h>

#include <d3d11.h>
#include <dxgi1_4.h>

#include <iostream>
#include <map>

LRESULT CALLBACK WndProc(HWND hWnd, UINT uMsg, WPARAM wParam, LPARAM lParam);

void nCreateContext();
jlong nCreateWindow();

void nPresent(jlong hwnd);
void nClear(jlong hwnd);

extern "C" {

	/* ===============
		Platform
	   ===============
	*/
	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d11_D3D11Pipeline_nCreateContext(JNIEnv*, jobject) {
		nCreateContext();
	}

	JNIEXPORT jlong JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d11_D3D11Pipeline_nCreateWindow(JNIEnv*, jobject) {
		return nCreateWindow();
	}

	/* ===============
		Paint
	   ===============
	*/
	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d11_D3D11Pipeline_nPresent(JNIEnv*, jobject, jlong hwnd) {
		nPresent(hwnd);
	}
	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d11_D3D11Pipeline_nClear(JNIEnv*, jobject, jlong hwnd) {
		nClear(hwnd);
	}

}
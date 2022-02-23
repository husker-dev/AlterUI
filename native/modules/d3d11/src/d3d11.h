
#define UNICODE
#include <jni.h>
#include <windows.h>
#include <dwmapi.h>

#include <d3d11.h>
#include <dxgi1_4.h>
#include <d3dcompiler.h>

#include <iostream>
#include <map>

void throwError(const char* text) {
	std::cout << "[ERROR] Internal Direct3D11 error: " << text << std::endl;
	MessageBoxA(NULL, text, "Internal Direct3D11 error", MB_OK | MB_ICONERROR);
	exit(1);
}


LRESULT CALLBACK WndProc(HWND hWnd, UINT uMsg, WPARAM wParam, LPARAM lParam);

void nCreateContext();
jlong nCreateWindow();

void setRenderTarget(jlong hwnd);
void setViewport(jint width, jint height);
void nPresent(jlong hwnd);
void nClear(jlong hwnd);

jlong nCreatePixelShader(char* content, jint length);
jlong nCreateVertexShader(char* content, jint length);
void nSetPixelShader(jlong pointer);
void nSetVertexShader(jlong pointer);
void nSetShaderValue4f(jlong pointer, char* name, jfloat v1, jfloat v2, jfloat v3, jfloat v4);
void nSetShaderValue3f(jlong pointer, char* name, jfloat v1, jfloat v2, jfloat v3);
void nSetShaderValue1f(jlong pointer, char* name, jfloat v1);
void nSetShaderMatrix(jlong pointer, char* name, jfloat* matrix);


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
	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d11_D3D11Pipeline_setRenderTarget(JNIEnv*, jobject, jlong hwnd) {
		setRenderTarget(hwnd);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d11_D3D11Pipeline_setViewport(JNIEnv*, jobject, jint width, jint height) {
		setViewport(width, height);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d11_D3D11Pipeline_nPresent(JNIEnv*, jobject, jlong hwnd) {
		nPresent(hwnd);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d11_D3D11Pipeline_nClear(JNIEnv*, jobject, jlong hwnd) {
		nClear(hwnd);
	}

	/* ===============
		Shaders
	   ===============
	*/
	JNIEXPORT jlong JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d11_D3D11Pipeline_nCreatePixelShader(JNIEnv* env, jobject, jobject _content, jint length) {
		char* content = (char*)env->GetDirectBufferAddress(_content);
		return nCreatePixelShader(content, length);
	}

	JNIEXPORT jlong JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d11_D3D11Pipeline_nCreateVertexShader(JNIEnv* env, jobject, jobject _content, jint length) {
		char* content = (char*)env->GetDirectBufferAddress(_content);
		return nCreateVertexShader(content, length);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d11_D3D11Pipeline_nSetPixelShader(JNIEnv*, jobject, jlong pointer) {
		nSetPixelShader(pointer);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d11_D3D11Pipeline_nSetVertexShader(JNIEnv*, jobject, jlong pointer) {
		nSetVertexShader(pointer);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d11_D3D11Pipeline_nSetShaderValue4f(JNIEnv* env, jobject, jlong pointer, jobject _name, jfloat v1, jfloat v2, jfloat v3, jfloat v4) {
		char* name = (char*)env->GetDirectBufferAddress(_name);
		nSetShaderValue4f(pointer, name, v1, v2, v3, v4);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d11_D3D11Pipeline_nSetShaderValue3f(JNIEnv* env, jobject, jlong pointer, jobject _name, jfloat v1, jfloat v2, jfloat v3) {
		char* name = (char*)env->GetDirectBufferAddress(_name);
		nSetShaderValue3f(pointer, name, v1, v2, v3);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d11_D3D11Pipeline_nSetShaderValue1f(JNIEnv* env, jobject, jlong pointer, jobject _name, jfloat v1) {
		char* name = (char*)env->GetDirectBufferAddress(_name);
		nSetShaderValue1f(pointer, name, v1);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d11_D3D11Pipeline_nSetShaderMatrix(JNIEnv* env, jobject, jlong pointer, jobject _name, jobject _matrix) {
		char* name = (char*)env->GetDirectBufferAddress(_name);
		jfloat* matrix = (jfloat*)env->GetDirectBufferAddress(_matrix);
		nSetShaderMatrix(pointer, name, matrix);
	}
}
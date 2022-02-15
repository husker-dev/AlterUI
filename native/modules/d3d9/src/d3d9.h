
#include <jni.h>
#include <windows.h>
#include <dwmapi.h>
#include <iostream>
#include <map>
#include <d3d9.h>
#include <d3dx9.h>

void throwError(const char* text) {
	std::cout << "[ERROR] Internal Direct3D9 error: " << text << std::endl;
	MessageBoxA(NULL, text, "Internal Direct3D9 error", MB_OK | MB_ICONERROR);
	exit(1);
}

LRESULT CALLBACK WndProc(HWND hWnd, UINT uMsg, WPARAM wParam, LPARAM lParam);

jlong nCreateMainWindow();
jlong nCreateWindow();
jlong nGetDevice();

void nBeginScene(jlong hwnd);
void nEndScene(jlong hwnd);
void nClear();
void nSetTexture(jlong texture);

void nSetViewport(jlong hwnd, jint width, jint height);
void nDrawArrays(jfloat* array, jint count, jint type);
jlong nCreatePixelShader(char* content, jint length);
jlong nCreateVertexShader(char* content, jint length);
void nSetPixelShader(jlong shader);
void nSetVertexShader(jlong shader);
void nSetShaderValue1f(jlong shader, char* name, jfloat v);
void nSetShaderValue3f(jlong shader, char* name, jfloat v1, jfloat v2, jfloat v3);
void nSetShaderValue4f(jlong shader, char* name, jfloat v1, jfloat v2, jfloat v3, jfloat v4);
void nSetShaderMatrix(jlong shader, char* name, jfloat* matrix);
jlong nCreateTexture(jint width, jint height, jint components, char* data);
//jlong nCreateEmptyTexture(jlong device, char* data);

extern "C" {

	/* ===============
		Platform
	   ===============
	*/
	JNIEXPORT jlong JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nCreateMainWindow(JNIEnv*, jobject) {
		return nCreateMainWindow();
	}

	JNIEXPORT jlong JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nCreateWindow(JNIEnv*, jobject) {
		return nCreateWindow();
	}

	JNIEXPORT jlong JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nGetDevice(JNIEnv*, jobject) {
		return nGetDevice();
	}

	/* ===============
		Default
	   ===============
	*/
	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nBeginScene(JNIEnv*, jobject, jlong device) {
		nBeginScene(device);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nEndScene(JNIEnv*, jobject, jlong device) {
		nEndScene(device);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nClear(JNIEnv*, jobject) {
		nClear();
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nSetTexture(JNIEnv*, jobject, jlong texture) {
		nSetTexture(texture);
	}

	/* ===============
		Custom
	   ===============
	*/
	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nSetViewport(JNIEnv*, jobject, jlong device, jint width, jint height) {
		nSetViewport(device, width, height);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nDrawArrays(JNIEnv* env, jobject, jobject _array, jint count, jint type) {
		jfloat* array = (jfloat*)env->GetDirectBufferAddress(_array);
		nDrawArrays(array, count, type);
	}

	JNIEXPORT jlong JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nCreatePixelShader(JNIEnv* env, jobject, jobject _content, jint length) {
		char* content = (char*)env->GetDirectBufferAddress(_content);
		return nCreatePixelShader(content, length);
	}

	JNIEXPORT jlong JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nCreateVertexShader(JNIEnv* env, jobject, jobject _content, jint length) {
		char* content = (char*)env->GetDirectBufferAddress(_content);
		return nCreateVertexShader(content, length);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nSetPixelShader(JNIEnv* env, jobject, jlong shader) {
		nSetPixelShader(shader);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nSetVertexShader(JNIEnv* env, jobject, jlong shader) {
		nSetVertexShader(shader);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nSetShaderValue1f(JNIEnv* env, jobject, jlong shader, jobject _name, jfloat v) {
		char* name = (char*)env->GetDirectBufferAddress(_name);
		nSetShaderValue1f(shader, name, v);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nSetShaderValue3f(JNIEnv* env, jobject, jlong shader, jobject _name, jfloat v1, jfloat v2, jfloat v3) {
		char* name = (char*)env->GetDirectBufferAddress(_name);
		nSetShaderValue3f(shader, name, v1, v2, v3);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nSetShaderValue4f(JNIEnv* env, jobject, jlong shader, jobject _name, jfloat v1, jfloat v2, jfloat v3, jfloat v4) {
		char* name = (char*)env->GetDirectBufferAddress(_name);
		nSetShaderValue4f(shader, name, v1, v2, v3, v4);
	}

	JNIEXPORT void JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nSetShaderMatrix(JNIEnv* env, jobject, jlong shader, jobject _name, jobject _matrix) {
		char* name = (char*)env->GetDirectBufferAddress(_name);
		jfloat* matrix = (jfloat*)env->GetDirectBufferAddress(_matrix);
		nSetShaderMatrix(shader, name, matrix);
	}

	JNIEXPORT jlong JNICALL Java_com_huskerdev_alter_internal_pipelines_d3d9_D3D9Pipeline_nCreateTexture(JNIEnv* env, jobject, jint width, jint height, jint components, jobject _data) {
		char* data = (char*)env->GetDirectBufferAddress(_data);
		return nCreateTexture(width, height, components, data);
	}

}
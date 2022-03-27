#include <jni.h>

jweak getCallbackObject(JNIEnv* env, jobject object);
jmethodID getCallbackMethod(JNIEnv* env, jweak callbackObject, const char* name, const char* sig);

void callback(JavaVM*, jweak callbackObject, jmethodID method, ...);
jboolean callbackBoolean(JavaVM*, jweak callbackObject, jmethodID method, ...);
jfloat callbackFloat(JavaVM*, jweak callbackObject, jmethodID method, ...);
jdouble callbackDouble(JavaVM*, jweak callbackObject, jmethodID method, ...);
jint callbackInt(JavaVM*, jweak callbackObject, jmethodID method, ...);

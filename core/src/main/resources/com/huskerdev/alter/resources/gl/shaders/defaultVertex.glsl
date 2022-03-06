#version 330 core
layout (location = 0) in vec3 a_Position;
uniform mat4 u_Matrix;


void main(){
   gl_Position = u_Matrix * vec4(a_Position.xyz, 1.0);
}

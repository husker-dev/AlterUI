#version 330 core
layout (location = 0) in vec3 a_Position;

uniform float u_ViewportWidth;
uniform float u_ViewportHeight;

void main(){
   mat4 ortho = mat4(
      2/u_ViewportWidth,   0,                   0,  -1,
      0,                   2/u_ViewportHeight,  0,  -1,
      0,                   0,                   1,  0,
      0,                   0,                   0,  1
   );
   ortho = transpose(ortho);

   gl_Position = ortho * vec4(a_Position.x, u_ViewportHeight - a_Position.y, a_Position.z, 1.0);
}

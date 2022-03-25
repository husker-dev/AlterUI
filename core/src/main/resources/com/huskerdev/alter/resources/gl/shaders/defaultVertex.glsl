#version 330 core
layout (location = 0) in vec3 a_Position;

uniform float u_ViewportWidth;
uniform float u_ViewportHeight;
uniform float u_InverseY;

void main(){
   mat4 ortho = mat4(
      2/u_ViewportWidth,   0,                   0,  -1,
      0,                   2/u_ViewportHeight,  0,  -1,
      0,                   0,                   1,  0,
      0,                   0,                   0,  1
   );
   ortho = transpose(ortho);

   float y = a_Position.y;   // In OpenGL Y is inversed by default
   if(u_InverseY == 0)
      y = u_ViewportHeight - a_Position.y;

   gl_Position = ortho * vec4(a_Position.x, y, a_Position.z, 1.0);
}

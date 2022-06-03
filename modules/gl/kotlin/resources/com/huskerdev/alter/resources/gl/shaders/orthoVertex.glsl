#version 330 core
layout (location = 0) in vec3 a_Position;

uniform float u_ViewportWidth;
uniform float u_ViewportHeight;
uniform float u_InverseY;
uniform float u_Dpi;

void main(){
   mat4 ortho = mat4(
      2/u_ViewportWidth,   0,                   0,  -1,
      0,                   2/u_ViewportHeight,  0,  -1,
      0,                   0,                   1,  0,
      0,                   0,                   0,  1
   );
   ortho = transpose(ortho);

   vec4 coords = vec4(a_Position.x, a_Position.y, a_Position.z, 1.0);
   coords *= vec4(u_Dpi, u_Dpi, u_Dpi, 1.0);

   // In OpenGL Y is inversed by default, so flip if needed
   if(u_InverseY == 0)
      coords.y = u_ViewportHeight - coords.y;

   gl_Position = ortho * coords;
}

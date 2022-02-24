#version 330 core
out vec4 color;

uniform vec4 u_Bounds;
uniform vec4 u_Color;
uniform sampler2D u_Texture;
uniform float u_Dpi;

void main(){
    float tex_x = (gl_FragCoord.x - u_Bounds.x) / u_Bounds.z;
    float tex_y = u_Dpi - (gl_FragCoord.y - u_Bounds.y) / u_Bounds.w;

    color = texture(u_Texture, vec2(tex_x / u_Dpi, tex_y / u_Dpi));
}
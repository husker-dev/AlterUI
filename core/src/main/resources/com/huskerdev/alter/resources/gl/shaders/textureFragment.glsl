#version 330 core
out vec4 color;

uniform vec4 u_Bounds;
uniform vec4 u_Color;
uniform sampler2D u_Texture;

void main(){
    float tex_x = mod((gl_FragCoord.x - u_Bounds.x) / u_Bounds.z, 1);
    float tex_y = mod(1 - (gl_FragCoord.y - u_Bounds.y) / u_Bounds.w, 1);

    color = texture(u_Texture, vec2(tex_x, tex_y));
}
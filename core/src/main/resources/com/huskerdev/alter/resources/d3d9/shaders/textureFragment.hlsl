float4 u_Color: COLOR;
vector u_Bounds;
float u_Height;
float u_Dpi;

sampler u_Texture;

float4 main(float4 Pos : SV_POSITION) : COLOR {
    float tex_x = (Pos.x - u_Bounds.x) / u_Bounds.z;
    float tex_y = 1 - ((u_Height - Pos.y) - u_Bounds.y) / u_Bounds.w;

	return tex2D(u_Texture, float2(tex_x / u_Dpi, tex_y / u_Dpi));
}
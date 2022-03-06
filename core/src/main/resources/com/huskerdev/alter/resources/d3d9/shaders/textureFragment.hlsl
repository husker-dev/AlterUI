float4 u_Color: COLOR;
vector u_Bounds;
float u_Height;
float u_Dpi;
float u_TextureColors;

sampler u_Texture;

float4 main(float4 Pos : SV_POSITION) : COLOR {
    vector bounds = u_Bounds * vector(u_Dpi, u_Dpi, u_Dpi, u_Dpi);
    bounds.y -= 0.1;    // 0.1 - for pixel-perfect on HiDPI (idk why)

    float2 texCoord = float2(
        (Pos.x - bounds.x + 0.5) / bounds.z,
        1 - ((u_Height - Pos.y) - bounds.y - 0.5) / bounds.w
    );

	if(u_TextureColors != 1)
        return tex2D(u_Texture, texCoord) * u_Color;
    else {
        float a = tex2D(u_Texture, texCoord).r;
        return vector(1.0, 1.0, 1.0, a) * u_Color;
    }
}
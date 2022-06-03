
float u_ViewportWidth;
float u_ViewportHeight;
float u_Dpi;

float4 main(float3 pos : POSITION) : POSITION {

    float4x4 ortho = float4x4(
        2/u_ViewportWidth,   0,                   0,  -1,
        0,                   2/u_ViewportHeight,  0,  -1,
        0,                   0,                         1,  0,
        0,                   0,                         0,  1
    );
    float4 coords = float4(pos.x, pos.y, pos.z, 1.0);

    coords *= float4(u_Dpi, u_Dpi, u_Dpi, 1.0); // Scale to DPI
    coords.x -= 0.5;
    coords.y -= 0.5;
    coords.y = u_ViewportHeight - coords.y;     // Flip vertically

	return mul(ortho, coords);
}

float4 main(float3 pos : POSITION) : POSITION {
	return float4(pos.x, pos.y, 0, 0);
}
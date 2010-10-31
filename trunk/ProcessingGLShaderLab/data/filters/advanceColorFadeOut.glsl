
uniform sampler2D src_tex_unit0; // previous values
uniform float factor;
uniform float increment;

void main(void) // fragment
{
	vec4 prev = texture2D(src_tex_unit0,gl_TexCoord[0].st);
    gl_FragColor.xyz = prev.xyz * factor + increment;
}

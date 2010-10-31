// original shader from http://www.gamerendering.com/2008/10/11/gaussian-blur-filter-shader/
// vertical blur fragment shader

uniform sampler2D src_tex_unit0;
uniform vec2 pixelSize = vec2(1.0/1024.0, 1.0/1024.0);

void main(void) // fragment
{
	float vertical = pixelSize.y;
	vec4 sum = vec4(0.0);
	sum += texture2D(src_tex_unit0, vec2(gl_TexCoord[0].s, - 4.0*vertical + gl_TexCoord[0].t) ) * 0.05;
	sum += texture2D(src_tex_unit0, vec2(gl_TexCoord[0].s, - 3.0*vertical + gl_TexCoord[0].t) ) * 0.09;
	sum += texture2D(src_tex_unit0, vec2(gl_TexCoord[0].s, - 2.0*vertical + gl_TexCoord[0].t) ) * 0.12;
	sum += texture2D(src_tex_unit0, vec2(gl_TexCoord[0].s, - 1.0*vertical + gl_TexCoord[0].t) ) * 0.15;
	sum += texture2D(src_tex_unit0, vec2(gl_TexCoord[0].s, + 0.0*vertical + gl_TexCoord[0].t) ) * 0.16;
	sum += texture2D(src_tex_unit0, vec2(gl_TexCoord[0].s, + 1.0*vertical + gl_TexCoord[0].t) ) * 0.15;
	sum += texture2D(src_tex_unit0, vec2(gl_TexCoord[0].s, + 2.0*vertical + gl_TexCoord[0].t) ) * 0.12;
	sum += texture2D(src_tex_unit0, vec2(gl_TexCoord[0].s, + 3.0*vertical + gl_TexCoord[0].t) ) * 0.09;
	sum += texture2D(src_tex_unit0, vec2(gl_TexCoord[0].s, + 4.0*vertical + gl_TexCoord[0].t) ) * 0.05;


    gl_FragColor.xyz = sum.xyz/0.98;
}

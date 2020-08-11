#version 330

in vec4 outVertexColor;
in vec2 outTextureCoordinate;

out vec4 fragmentColor;

uniform sampler2D textureImage;

void main() {
	vec4 textureColor = texture(textureImage, outTextureCoordinate);
	fragmentColor = outVertexColor * textureColor;
}

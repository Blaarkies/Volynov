#version 330

in vec3 inPosition;
in vec4 inColor;
in vec2 inTextureCoordinate;

out vec4 outVertexColor;
out vec2 outTextureCoordinate;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

void main() {
    outVertexColor = inColor;
    outTextureCoordinate = inTextureCoordinate;

    mat4 mvp = projection * view * model;
    gl_Position = mvp * vec4(inPosition, 1.0);
}

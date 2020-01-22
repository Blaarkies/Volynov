#version 330

in vec3 position;
in vec4 color;

out vec4 vertexColor;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

void main() {
    vertexColor = color;
    mat4 mvp = projection * view * model;
    gl_Position = mvp * vec4(position, 1.0);
}

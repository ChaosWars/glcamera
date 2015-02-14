package com.zendeka.glcamera;

import android.content.Context;
import android.opengl.Matrix;
import android.util.Log;

import com.zendeka.glesutils.gles20.VertexBufferObject;
import com.zendeka.glesutils.gles20.shader.Shader;
import com.zendeka.glesutils.gles20.shader.ShaderProgram;
import com.zendeka.glesutils.utils.GLGetError;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE1;
import static android.opengl.GLES20.GL_TEXTURE2;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDrawElements;

/**
 * Created by Lawrence on 9/14/13.
 */
public class YV12CameraRenderer implements CameraRenderer<YV12CameraPreviewCallback> {
    private static final String SHADER_TAG = "CameraShaderProgram";
    private static final String FRAGMENT_SHADER_TAG = "CameraFragmentShader";
    private static final String VERTEX_SHADER_TAG = "CameraVertexShader";

    private static final String PROJECTION_MATRIX = "projectionMatrix";
    private static final String VERTEX_COORD = "vertexCoord";
    private static final String TEXTURE_COORD = "textureCoord";
    private static final String Y_TEXTURE_SAMPLER = "yTextureSampler";
    private static final String U_TEXTURE_SAMPLER = "uTextureSampler";
    private static final String V_TEXTURE_SAMPLER = "vTextureSampler";

    private static final int BYTES_PER_FLOAT = Float.SIZE / 8;
    private static final int BYTES_PER_SHORT = Short.SIZE / 8;

    private static final int VERTEX_COORDINATE_SIZE = 3;
    private static final int TEXTURE_COORDINATE_SIZE = 2;
    private static final int VERTEX_DATA_SIZE = VERTEX_COORDINATE_SIZE + TEXTURE_COORDINATE_SIZE;
    private static final int VERTEX_DATA_BYTE_SIZE = VERTEX_DATA_SIZE * BYTES_PER_FLOAT;
    private static final int VERTEX_COORDINATE_BYTE_OFFSET = 0;
    private static final int TEXTURE_COORDINATE_BYTE_OFFSET = VERTEX_COORDINATE_SIZE * BYTES_PER_FLOAT;

    private final String mTag;

    private OnRenderCallback mOnRenderCallback;
    private YV12CameraPreviewCallback mCameraPreviewCallback;

    private VertexBufferObject mVbo;
    private VertexBufferObject mIbo;

    private boolean mBuffersCreated;

    private ShaderProgram mShaderProgram;

    public YV12CameraRenderer(Context context, String tag) {
        mTag = tag;
        createShaderProgram(context);
    }

    @Override
    public void setOnRenderCallback(OnRenderCallback onRenderCallback) {
        mOnRenderCallback = onRenderCallback;
    }

    @Override
    public void setCameraPreviewCallback(YV12CameraPreviewCallback cameraPreviewCallback) {
        mCameraPreviewCallback = cameraPreviewCallback;
    }

    @Override
    public void render() {
        if (mOnRenderCallback != null) {
            mOnRenderCallback.preRender();
        }

        renderHelper();

        if (mOnRenderCallback != null) {
            mOnRenderCallback.postRender();
        }
    }

    @Override
    public void createBuffers(Size screenSize, Size cameraSize) {
        Log.d(mTag, "Creating buffers");

        if (mBuffersCreated) {
            releaseBuffers();
        }

        float width = screenSize.width / 2.0f;
        float height = screenSize.height / 2.0f;

        float[] projection = new float[16];
        Matrix.orthoM(projection, 0, -width, width, -height, height, 0.0f, 1.0f);

        float sX = width / cameraSize.height;
        float sY = height / cameraSize.width;
        float s = Math.max(sX, sY);

        float[] vertices = {
                s * -cameraSize.height, s * -cameraSize.width, 0.0f, 1.0f, 1.0f,
                s *  cameraSize.height, s * -cameraSize.width, 0.0f, 1.0f, 0.0f,
                s * -cameraSize.height, s *  cameraSize.width, 0.0f, 0.0f, 1.0f,
                s *  cameraSize.height, s *  cameraSize.width, 0.0f, 0.0f, 0.0f
        };

        short[] indices = {0, 1, 2, 3};

        FloatBuffer vertexBuffer = ByteBuffer.allocateDirect(vertices.length * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.put(vertices).position(0);

        ShortBuffer indexBuffer = ByteBuffer.allocateDirect(indices.length * BYTES_PER_SHORT).order(ByteOrder.nativeOrder()).asShortBuffer();
        indexBuffer.put(indices).position(0);

        mShaderProgram.use();
        mShaderProgram.setUniformMatrix4fv(PROJECTION_MATRIX, 1, false, projection, 0);

        mVbo = new VertexBufferObject(VertexBufferObject.Target.ARRAY_BUFFER, VertexBufferObject.Usage.STATIC_DRAW, vertexBuffer, vertices.length, vertexBuffer.capacity() * BYTES_PER_FLOAT);
        mIbo = new VertexBufferObject(VertexBufferObject.Target.ELEMENT_ARRAY_BUFFER, VertexBufferObject.Usage.STATIC_DRAW, indexBuffer, indices.length, indexBuffer.capacity() * BYTES_PER_SHORT);

        mBuffersCreated = true;
    }

    @Override
    public boolean getBuffersCreated() {
        return mBuffersCreated;
    }

    @Override
    public void releaseBuffers() {
        Log.d(mTag, "Releasing buffers");

        if (mVbo != null) {
            mVbo.release();
            mVbo = null;
        }

        if (mIbo != null) {
            mIbo.release();
            mIbo = null;
        }

        mBuffersCreated = false;
    }

    @Override
    public void releaseShaderProgram() {
        if (mShaderProgram != null) {
            Log.d(mTag, "Releasing shader program");
            mShaderProgram.release();
            mShaderProgram = null;
        }
    }

    private void createShaderProgram(Context context) throws IllegalStateException {
        Log.d(mTag, "Creating shader program");

        String fragmentShaderSrc = TextUtils.readTextFileFromRawResource(context, R.raw.camera_yv12_fragment_shader);
        String vertexShaderSrc = TextUtils.readTextFileFromRawResource(context, R.raw.camera_yv12_vertex_shader);

        Shader fragmentShader = new Shader(Shader.Type.FRAGMENT, fragmentShaderSrc, FRAGMENT_SHADER_TAG);
        Shader vertexShader = new Shader(Shader.Type.VERTEX, vertexShaderSrc, VERTEX_SHADER_TAG);

        mShaderProgram = new ShaderProgram(SHADER_TAG);

        mShaderProgram.addShader(fragmentShader);
        mShaderProgram.addShader(vertexShader);

        mShaderProgram.build();

        mShaderProgram.removeShader(fragmentShader);
        mShaderProgram.removeShader(vertexShader);

        if (BuildConfig.DEBUG) {
            Log.d(mTag, mShaderProgram.getBuildLog());
            mShaderProgram.validate();
            Log.d(mTag, mShaderProgram.getValidationLog());
        }
    }

    private void renderHelper() {
        if (!mBuffersCreated || mShaderProgram == null) {
            return;
        }

        if (!mShaderProgram.isBuilt()) {
            return;
        }

        int yTexture = mCameraPreviewCallback.getYTexture();
        int uTexture = mCameraPreviewCallback.getUTexture();
        int vTexture = mCameraPreviewCallback.getVTexture();

        if (yTexture == 0 || uTexture == 0 || vTexture == 0) {
            return;
        }

        glActiveTexture(GL_TEXTURE0); GLGetError.getOpenGLErrors(mTag);
        glBindTexture(GL_TEXTURE_2D, yTexture); GLGetError.getOpenGLErrors(mTag);

        glActiveTexture(GL_TEXTURE1); GLGetError.getOpenGLErrors(mTag);
        glBindTexture(GL_TEXTURE_2D, uTexture); GLGetError.getOpenGLErrors(mTag);

        glActiveTexture(GL_TEXTURE2); GLGetError.getOpenGLErrors(mTag);
        glBindTexture(GL_TEXTURE_2D, vTexture); GLGetError.getOpenGLErrors(mTag);

        mShaderProgram.use();

        mShaderProgram.setUniform(Y_TEXTURE_SAMPLER, yTexture);
        mShaderProgram.setUniform(U_TEXTURE_SAMPLER, uTexture);
        mShaderProgram.setUniform(V_TEXTURE_SAMPLER, vTexture);

        mVbo.bind();

        mShaderProgram.enableAttribute(VERTEX_COORD); GLGetError.getOpenGLErrors(mTag);
        mShaderProgram.setAttributePointer(VERTEX_COORD, VERTEX_COORDINATE_SIZE, GL_FLOAT, false, VERTEX_DATA_BYTE_SIZE, VERTEX_COORDINATE_BYTE_OFFSET); GLGetError.getOpenGLErrors(mTag);

        mShaderProgram.enableAttribute(TEXTURE_COORD); GLGetError.getOpenGLErrors(mTag);
        mShaderProgram.setAttributePointer(TEXTURE_COORD, TEXTURE_COORDINATE_SIZE, GL_FLOAT, false, VERTEX_DATA_BYTE_SIZE, TEXTURE_COORDINATE_BYTE_OFFSET); GLGetError.getOpenGLErrors(mTag);

        mIbo.bind();
        glDrawElements(GL_TRIANGLE_STRIP, mIbo.getNumElements(), GL_UNSIGNED_SHORT, 0); GLGetError.getOpenGLErrors(mTag);

        mShaderProgram.disableAttribute(VERTEX_COORD); GLGetError.getOpenGLErrors(mTag);
        mShaderProgram.disableAttribute(TEXTURE_COORD); GLGetError.getOpenGLErrors(mTag);

        //Unbind the texture bound to GL_TEXTURE2
        glBindTexture(GL_TEXTURE_2D, 0); GLGetError.getOpenGLErrors(mTag);

        //Unbind the texture bound to GL_TEXTURE1
        glActiveTexture(GL_TEXTURE1); GLGetError.getOpenGLErrors(mTag);
        glBindTexture(GL_TEXTURE_2D, 0); GLGetError.getOpenGLErrors(mTag);

        //Unbind the texture bound to GL_TEXTURE0
        glActiveTexture(GL_TEXTURE0); GLGetError.getOpenGLErrors(mTag);
        glBindTexture(GL_TEXTURE_2D, 0); GLGetError.getOpenGLErrors(mTag);

        glBindBuffer(GL_ARRAY_BUFFER, 0); GLGetError.getOpenGLErrors(mTag);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0); GLGetError.getOpenGLErrors(mTag);
    }
}

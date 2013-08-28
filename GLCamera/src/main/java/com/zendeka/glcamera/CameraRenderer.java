package com.zendeka.glcamera;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.zendeka.glesutils.gles20.VertexBufferObject;
import com.zendeka.glesutils.gles20.VertexBufferObject.Target;
import com.zendeka.glesutils.gles20.VertexBufferObject.Usage;
import com.zendeka.glesutils.gles20.shader.Shader;
import com.zendeka.glesutils.gles20.shader.ShaderProgram;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;

/**
 * Created by Lawrence on 8/2/13.
 */
public class CameraRenderer {

    public class Size {
        public float width;
        public float height;
    }

    public interface OnRenderCallback {
        public void preRender();
        public void postRender();
    }

    private static final String TAG = "CameraRenderer";

    private static final String CAMERA_FRAGMENT_SHADER = "CameraFragmentShader";
    private static final String CAMERA_VERTEX_SHADER = "CameraVertexShader";

    private static final String PROJECTION_MATRIX = "projectionMatrix";
    private static final String VERTEX_COORD = "vertexCoord";
    private static final String TEXTURE_COORD = "textureCoord";
    private static final String Y_TEXTURE_SAMPLER = "yTextureSampler";
    private static final String UV_TEXTURE_SAMPLER = "uvTextureSampler";

    private static final int VERTEX_COORDINATE_SIZE = 3;
    private static final int TEXTURE_COORDINATE_SIZE = 2;
    private static final int BYTES_PER_FLOAT = 4;
    private static final int VERTEX_DATA_SIZE = 5;
    private static final int VERTEX_DATA_BYTE_SIZE = VERTEX_DATA_SIZE * BYTES_PER_FLOAT;
    private static final int TEXTURE_COORDINATE_BYTE_OFFSET = VERTEX_COORDINATE_SIZE * BYTES_PER_FLOAT;

    private OnRenderCallback mOnRenderCallback;
    private CameraPreviewCallback mCameraPreviewCallback;

    private VertexBufferObject mVbo;
    private VertexBufferObject mIbo;
    private ShaderProgram mShaderProgram;

    public void setOnRenderCallback(OnRenderCallback onRenderCallback) {
        mOnRenderCallback = onRenderCallback;
    }

    public void setCameraPreviewCallback(CameraPreviewCallback cameraPreviewCallback) {
        mCameraPreviewCallback = cameraPreviewCallback;
    }

    CameraRenderer(Context context) {
        createShaderProgram(context);
    }

    public void render() {
        if (mOnRenderCallback != null) {
            mOnRenderCallback.preRender();
        }

        renderHelper();

        if (mOnRenderCallback != null) {
            mOnRenderCallback.postRender();
        }
    }

    private void createShaderProgram(Context context) throws IllegalStateException {
        String fragmentShaderSrc = readTextFileFromRawResource(context, R.raw.camera_fragment_shader);
        String vertexShaderSrc = readTextFileFromRawResource(context, R.raw.camera_vertex_shader);

        Shader fragmentShader = new Shader(Shader.Type.FRAGMENT, fragmentShaderSrc, CAMERA_FRAGMENT_SHADER);
        Shader vertexShader = new Shader(Shader.Type.VERTEX, vertexShaderSrc, CAMERA_VERTEX_SHADER);

        mShaderProgram = new ShaderProgram();
        mShaderProgram.addShader(fragmentShader);
        mShaderProgram.addShader(vertexShader);
        mShaderProgram.build();

        if (mShaderProgram.isBuilt())
        {
            mShaderProgram.use();
            mShaderProgram.setUniform(Y_TEXTURE_SAMPLER, 0);
            mShaderProgram.setUniform(UV_TEXTURE_SAMPLER, 0);
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Build log: " + mShaderProgram.getBuildLog());
            mShaderProgram.validate();
            Log.d(TAG, "Validation log: " + mShaderProgram.getValidationLog());
        }
    }

    public void createVertexBuffer(Size screenSize, Size cameraSize) {
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

        float[] indices = {0, 1, 2, 3};

        FloatBuffer vertexBuffer = FloatBuffer.wrap(vertices);
        FloatBuffer indexBuffer = FloatBuffer.wrap(indices);

        mShaderProgram.use();
        mShaderProgram.setUniformMatrix4fv(PROJECTION_MATRIX, 16, false, projection, 0);

        mVbo = new VertexBufferObject(Target.ARRAY_BUFFER, Usage.STATIC_DRAW, vertexBuffer, vertices.length);
        mIbo = new VertexBufferObject(Target.ELEMENT_ARRAY_BUFFER, Usage.STATIC_DRAW, indexBuffer, indices.length);
    }

    private void renderHelper() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mCameraPreviewCallback.getYTexture());

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mCameraPreviewCallback.getUVTexture());

        mShaderProgram.use();
        mVbo.bind();

        mShaderProgram.enableAttribute(VERTEX_COORD);
        mShaderProgram.setAttributePointer(VERTEX_COORD, VERTEX_COORDINATE_SIZE, GLES20.GL_FLOAT, false, VERTEX_DATA_BYTE_SIZE, 0);

        mShaderProgram.enableAttribute(TEXTURE_COORD);
        mShaderProgram.setAttributePointer(TEXTURE_COORD, TEXTURE_COORDINATE_SIZE, GLES20.GL_FLOAT, false, VERTEX_DATA_BYTE_SIZE, TEXTURE_COORDINATE_BYTE_OFFSET);

        mIbo.bind();
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, mIbo.getSize(), GLES20.GL_UNSIGNED_SHORT, 0);

        mShaderProgram.disableAttribute(VERTEX_COORD);
        mShaderProgram.disableAttribute(TEXTURE_COORD);
    }

    private static String readTextFileFromRawResource(final Context context, final int resourceId)
    {
        final InputStream inputStream = context.getResources().openRawResource(resourceId);
        final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        String nextLine;
        final StringBuilder body = new StringBuilder();

        try
        {
            while ((nextLine = bufferedReader.readLine()) != null)
            {
                body.append(nextLine);
                body.append('\n');
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }

        return body.toString();
    }
}

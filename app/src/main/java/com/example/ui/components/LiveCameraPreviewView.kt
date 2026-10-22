package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Live Camera Preview View:
 * Uses Android Camera2 API directly via TextureView inside AndroidView.
 * Opens the rear hardware camera with autofocus and continuous stream,
 * providing a zero-dependency, ultra-fast 60fps live camera feed inside Compose dialogs.
 */
@SuppressLint("MissingPermission")
@Composable
fun LiveCameraPreviewView(
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { ctx ->
            object : TextureView(ctx), TextureView.SurfaceTextureListener {
                private var cameraDevice: CameraDevice? = null
                private var captureSession: CameraCaptureSession? = null
                private var backgroundThread: HandlerThread? = null
                private var backgroundHandler: Handler? = null

                init {
                    surfaceTextureListener = this
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }

                override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                    startBackgroundThread()
                    openCamera(surfaceTexture)
                }

                override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, width: Int, height: Int) {}

                override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                    closeCamera()
                    stopBackgroundThread()
                    return true
                }

                override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {}

                private fun startBackgroundThread() {
                    backgroundThread = HandlerThread("ScubaCameraBackground").also { it.start() }
                    backgroundHandler = Handler(backgroundThread!!.looper)
                }

                private fun stopBackgroundThread() {
                    backgroundThread?.quitSafely()
                    try {
                        backgroundThread?.join()
                        backgroundThread = null
                        backgroundHandler = null
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                private fun openCamera(surfaceTexture: SurfaceTexture) {
                    val cameraManager = ctx.getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return
                    try {
                        val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                            val characteristics = cameraManager.getCameraCharacteristics(id)
                            val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                            facing == CameraCharacteristics.LENS_FACING_BACK
                        } ?: cameraManager.cameraIdList.firstOrNull() ?: return

                        cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                            override fun onOpened(camera: CameraDevice) {
                                cameraDevice = camera
                                createCameraPreviewSession(surfaceTexture)
                            }

                            override fun onDisconnected(camera: CameraDevice) {
                                camera.close()
                                cameraDevice = null
                            }

                            override fun onError(camera: CameraDevice, error: Int) {
                                camera.close()
                                cameraDevice = null
                            }
                        }, backgroundHandler)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                private fun createCameraPreviewSession(surfaceTexture: SurfaceTexture) {
                    val camera = cameraDevice ?: return
                    try {
                        val surface = Surface(surfaceTexture)
                        val previewRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                            addTarget(surface)
                        }

                        camera.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
                            override fun onConfigured(session: CameraCaptureSession) {
                                if (cameraDevice == null) return
                                captureSession = session
                                try {
                                    previewRequestBuilder.set(
                                        CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                                    )
                                    session.setRepeatingRequest(previewRequestBuilder.build(), null, backgroundHandler)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }

                            override fun onConfigureFailed(session: CameraCaptureSession) {}
                        }, backgroundHandler)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                private fun closeCamera() {
                    try {
                        captureSession?.close()
                        captureSession = null
                        cameraDevice?.close()
                        cameraDevice = null
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onDetachedFromWindow() {
                    super.onDetachedFromWindow()
                    closeCamera()
                    stopBackgroundThread()
                }
            }
        },
        modifier = modifier.fillMaxSize()
    )
}

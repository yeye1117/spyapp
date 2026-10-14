package com.spy.app

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import java.io.File

class CameraController(private val ctx: Context) {

    private var device: CameraDevice? = null
    private var reader: ImageReader? = null

    fun open() {
        val manager = ctx.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val id = manager.cameraIdList.firstOrNull() ?: return

        manager.openCamera(id, object : CameraDevice.StateCallback() {
            override fun onOpened(cam: CameraDevice) {
                device = cam
                reader = ImageReader.newInstance(1920, 1080, ImageFormat.JPEG, 2).apply {
                    setOnImageAvailableListener({ r ->
                        val img = r.acquireLatestImage() ?: return@setOnImageAvailableListener
                        val buf = img.planes[0].buffer
                        val bytes = ByteArray(buf.remaining())
                        buf.get(bytes)
                        File(ctx.filesDir, "shot.jpg").writeBytes(bytes)
                        img.close()
                    }, null)
                }

                cam.createCaptureSession(
                    listOf(reader!!.surface),
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(session: CameraCaptureSession) {
                            val req = cam.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                            req.addTarget(reader!!.surface)
                            session.setRepeatingRequest(req.build(), null, null)
                        }
                        override fun onConfigureFailed(session: CameraCaptureSession) {}
                    }, null
                )
            }

            override fun onDisconnected(cam: CameraDevice) { cam.close() }
            override fun onError(cam: CameraDevice, error: Int) { cam.close() }
        }, null)
    }
}
